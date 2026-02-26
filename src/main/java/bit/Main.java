package bit;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * JavaFX GUI for the Bit chatbot.
 *
 * <p>This GUI resembles a modern messaging app:
 * <ul>
 *     <li>Bottom-anchored conversation</li>
 *     <li>Date chips ("Today", converted to real date after midnight)</li>
 *     <li>Per-message timestamps</li>
 *     <li>Typing indicator</li>
 *     <li>Enter-to-send + Send button</li>
 *     <li>Chat history persisted (last 7 days)</li>
 *     <li>Welcome sequence from {@link Ui#showWelcome()} displayed every app run</li>
 * </ul>
 *
 * <p>The GUI calls {@link Bit#getResponse(String)} to generate replies.
 */
public class Main extends Application {

    // ===== Persistence =====
    /** File used to persist chat history between app runs. */
    private static final String CHAT_LOG_PATH = "./data/chatlog.txt";

    /** Keep only the last N days of history. */
    private static final int RETENTION_DAYS = 7;

    /** Log line separator token. */
    private static final String SEP = "|";

    // ===== UX timing =====
    /** Simulated typing delay to make the bot feel more natural. */
    private static final Duration TYPING_DELAY = Duration.millis(420);

    /** Message entrance animation duration. */
    private static final Duration ANIM_DURATION = Duration.millis(300);

    /** Delay between welcome bubbles. */
    private static final int WELCOME_DELAY_MS = 1350;

    /** Delay before exiting after "bye". */
    private static final Duration EXIT_DELAY = Duration.millis(800);

    // ===== Layout constants =====
    /** Max bubble width as a fraction of the visible chat viewport width. */
    private static final double BUBBLE_WIDTH_RATIO = 0.72;

    // ===== Date/Time formatting =====
    /** Timestamp under each bubble. */
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /** Date format used when converting an old "Today" chip into a real date. */
    private static final DateTimeFormatter CHIP_DATE_FMT = DateTimeFormatter.ofPattern("EEE, MMM d");

    // ===== Theme: Bit (Indigo + Mint) =====
    private static final String BG = "#F7F8FC";          // Background
    private static final String SURFACE = "#FFFFFF";     // Header / input surfaces
    private static final String BORDER = "#E5E7EB";      // Subtle borders

    private static final String PRIMARY = "#4F46E5";     // Indigo (user bubble + send)
    private static final String BOT_BUBBLE = "#E8F7F5";  // Soft mint (bot bubble)

    private static final String TEXT = "#0F172A";        // Main text
    private static final String MUTED = "#6B7280";       // Secondary labels
    private static final String ONLINE = "#16A34A";      // Online indicator
    private static final String TS_BOT = "#94A3B8";      // Bot timestamp

    // ===== Bot logic =====
    /** Core chatbot logic. */
    private final Bit bot = new Bit();

    // ===== UI nodes: chat layout =====
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox chatContent = new VBox();
    private final VBox messageBox = new VBox(10);
    private final Region topSpacer = new Region();

    // ===== UI nodes: input =====
    private final TextField userInput = new TextField();
    private final Button sendButton = new Button("➤");

    // ===== Reusable nodes =====
    private final HBox typingIndicator = buildTypingIndicator();

    // ===== Date chip tracking =====
    /** Last date for which a chip was inserted (based on runtime tracking). */
    private LocalDate lastDateShown = null;

    /**
     * Text node inside the latest chip row.
     * Kept so we can convert the previous "Today" chip into a real date after midnight.
     */
    private Text lastChipTextNode = null;

    @Override
    public void start(Stage stage) {
        HBox header = buildHeader();
        configureChatArea();
        VBox inputBar = buildInputBar();

        VBox root = new VBox(header, scrollPane, inputBar);
        root.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 420, 640);
        stage.setTitle("Bit");
        stage.setScene(scene);
        stage.show();

        // Hide visible scrollbar (still scrollable with mouse/trackpad).
        Platform.runLater(this::hideScrollBars);

        // Load last 7 days of chat.
        loadChatLogIfAny();

        // Make sure today's chip exists for the current run.
        ensureDateChip();

        // Always show welcome sequence every app run (NOT saved to file).
        playWelcomeSequenceFromUiEphemeral();

        // Auto-update date chips at midnight (even if user doesn't send messages).
        scheduleMidnightUpdate();

        scrollToBottom();
    }

    /**
     * Builds a PulseAI-style header:
     * centered title + online indicator (no back button, no three dots).
     *
     * @return header bar
     */
    private HBox buildHeader() {
        Text title = new Text("Bit");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-fill: " + TEXT + ";");

        Text status = new Text("● online");
        status.setStyle("-fx-font-size: 13px; -fx-fill: " + ONLINE + ";");

        VBox titleBox = new VBox(2, title, status);
        titleBox.setAlignment(Pos.CENTER);

        HBox header = new HBox(titleBox);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(16, 18, 14, 18));
        header.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );
        return header;
    }

    /**
     * Configures the chat ScrollPane with bottom anchoring:
     * the expandable spacer sits above messageBox and pushes content downward.
     */
    private void configureChatArea() {
        messageBox.setPadding(new Insets(14, 16, 14, 16));
        messageBox.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        chatContent.getChildren().addAll(topSpacer, messageBox);
        chatContent.setFillWidth(true);
        chatContent.setStyle("-fx-background-color: " + BG + ";");

        scrollPane.setContent(chatContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: " + BG + "; -fx-background-color: transparent;");

        chatContent.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getHeight(),
                scrollPane.viewportBoundsProperty()
        ));
    }

    /**
     * Builds the bottom input bar.
     *
     * @return input wrapper
     */
    private VBox buildInputBar() {
        userInput.setPromptText("Type a command...");
        userInput.setOnAction(e -> handleUserInput());
        userInput.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 11 12 11 12;" +
                        "-fx-font-size: 13px;"
        );

        sendButton.setOnAction(e -> handleUserInput());
        sendButton.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-background-color: " + PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 800;" +
                        "-fx-padding: 10 16 10 16;"
        );

        HBox input = new HBox(10, userInput, sendButton);
        input.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(userInput, Priority.ALWAYS);

        input.setPadding(new Insets(10));
        input.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0.2, 0, 4);"
        );

        VBox wrapper = new VBox(input);
        wrapper.setPadding(new Insets(12, 14, 14, 14));
        wrapper.setStyle("-fx-background-color: " + BG + ";");
        return wrapper;
    }

    /**
     * Handles input submission:
     * user bubble -> typing indicator -> bot response.
     * If user types "bye", exit after showing goodbye bubble.
     */
    private void handleUserInput() {
        String input = userInput.getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String trimmed = input.trim();

        ensureDateChip();
        addUserMessage(trimmed);
        userInput.clear();

        showTypingIndicator();

        PauseTransition delay = new PauseTransition(TYPING_DELAY);
        delay.setOnFinished(e -> {
            hideTypingIndicator();

            String response = bot.getResponse(trimmed);
            if (response != null && !response.isBlank()) {
                ensureDateChip();
                addBotMessage(cleanOutput(response));
            }

            if (trimmed.equalsIgnoreCase("bye")) {
                appendToChatLog("SESSION_END", "BYE");
                PauseTransition exitDelay = new PauseTransition(EXIT_DELAY);
                exitDelay.setOnFinished(e2 -> Platform.exit());
                exitDelay.play();
            }
        });
        delay.play();
    }

    /**
     * Inserts a date chip once per day.
     * If date changes, rename previous "Today" chip to the actual date,
     * then add a fresh "Today" chip.
     */
    private void ensureDateChip() {
        LocalDate today = LocalDate.now();

        if (lastDateShown == null) {
            addNewDateChip("Today");
            lastDateShown = today;
            return;
        }

        if (today.equals(lastDateShown)) {
            return;
        }

        if (lastChipTextNode != null) {
            lastChipTextNode.setText(lastDateShown.format(CHIP_DATE_FMT));
        }

        addNewDateChip("Today");
        lastDateShown = today;
    }

    /**
     * Creates and appends a centered date chip row and saves it to chat history.
     *
     * @param label chip label
     */
    private void addNewDateChip(String label) {
        appendToChatLog("DATE", label);

        HBox chipRow = buildDateChipRow(label);
        messageBox.getChildren().add(chipRow);
        animateIn(chipRow);
        scrollToBottom();
    }

    /**
     * Builds a centered date chip row.
     *
     * @param label chip text
     * @return row node
     */
    private HBox buildDateChipRow(String label) {
        Text t = new Text(label);
        t.setStyle("-fx-fill: " + TEXT + "; -fx-font-size: 12.5px; -fx-font-weight: 700;");
        lastChipTextNode = t;

        VBox chip = new VBox(t);
        chip.setPadding(new Insets(8, 18, 8, 18));
        chip.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-background-color: rgba(17,24,39,0.06);" +
                        "-fx-border-color: rgba(17,24,39,0.10);" +
                        "-fx-border-radius: 18;"
        );

        HBox row = new HBox(chip);
        row.setPadding(new Insets(12, 0, 6, 0));
        row.setStyle("-fx-alignment: CENTER;");
        return row;
    }

    /**
     * Schedules an update at the next midnight to refresh chips even without user input.
     */
    private void scheduleMidnightUpdate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long ms = java.time.Duration.between(now, nextMidnight).toMillis();

        PauseTransition pt = new PauseTransition(Duration.millis(Math.max(ms, 1)));
        pt.setOnFinished(e -> {
            ensureDateChip();
            scheduleMidnightUpdate();
        });
        pt.play();
    }

    /**
     * Adds a user bubble on the right and saves it.
     *
     * @param text message content
     */
    private void addUserMessage(String text) {
        appendToChatLog("ME", text);
        appendMessage(buildMessageRow(text, true));
    }

    /**
     * Adds a bot bubble on the left and saves it.
     *
     * @param text message content
     */
    private void addBotMessage(String text) {
        appendToChatLog("BIT", text);
        appendMessage(buildMessageRow(text, false));
    }

    /**
     * Adds a bot bubble without saving it to history.
     * Used for the welcome sequence that should appear every run.
     *
     * @param text message content
     */
    private void addBotMessageEphemeral(String text) {
        appendMessage(buildMessageRow(text, false));
    }

    /**
     * Appends a message row to chat and animates it.
     *
     * @param node message row
     */
    private void appendMessage(HBox node) {
        messageBox.getChildren().add(node);
        animateIn(node);
        scrollToBottom();
    }

    /**
     * Builds one message row:
     * name label + bubble (compact for short text) + timestamp.
     *
     * @param text message content
     * @param isUser true if user message (right side)
     * @return message row
     */
    private HBox buildMessageRow(String text, boolean isUser) {
        Text name = new Text(isUser ? "Me" : "Bit");
        name.setStyle("-fx-fill: " + MUTED + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        // Label keeps short bubbles compact (unlike Text which often stretches).
        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.maxWidthProperty().bind(scrollPane.widthProperty().multiply(BUBBLE_WIDTH_RATIO));
        msg.setStyle("-fx-text-fill: " + (isUser ? "white" : TEXT) + "; -fx-font-size: 13px;");
        msg.setMinWidth(Region.USE_PREF_SIZE);

        String time = LocalDateTime.now().format(TIME_FMT);
        Text ts = new Text(time);
        ts.setStyle(isUser
                ? "-fx-fill: rgba(255,255,255,0.85); -fx-font-size: 10.5px;"
                : "-fx-fill: " + TS_BOT + "; -fx-font-size: 10.5px;"
        );

        VBox bubble = new VBox(6, msg, ts);
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setStyle(
                "-fx-background-radius: 22;" +
                        "-fx-background-color: " + (isUser ? PRIMARY : BOT_BUBBLE) + ";" +
                        "-fx-border-color: " + (isUser ? "transparent" : BORDER) + ";" +
                        "-fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0.2, 0, 4);"
        );
        bubble.setMaxWidth(Region.USE_PREF_SIZE);

        VBox stack = new VBox(4, name, bubble);
        stack.setPadding(new Insets(2, 6, 2, 6));
        stack.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox row = new HBox(stack);
        row.setPadding(new Insets(2, 0, 2, 0));
        row.setStyle(isUser ? "-fx-alignment: CENTER_RIGHT;" : "-fx-alignment: CENTER_LEFT;");
        return row;
    }

    /**
     * Builds a reusable typing indicator bubble.
     *
     * @return typing indicator row
     */
    private HBox buildTypingIndicator() {
        Text t = new Text("Bit is typing...");
        t.setStyle("-fx-fill: #14B8A6; -fx-font-size: 12px; -fx-font-weight: 700;");

        VBox bubble = new VBox(t);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
                "-fx-background-radius: 22;" +
                        "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 22;"
        );
        bubble.setMaxWidth(Region.USE_PREF_SIZE);

        HBox row = new HBox(bubble);
        row.setPadding(new Insets(2, 6, 2, 6));
        row.setStyle("-fx-alignment: CENTER_LEFT;");
        return row;
    }

    /** Shows typing indicator if not already visible. */
    private void showTypingIndicator() {
        if (!messageBox.getChildren().contains(typingIndicator)) {
            messageBox.getChildren().add(typingIndicator);
            animateIn(typingIndicator);
            scrollToBottom();
        }
    }

    /** Hides typing indicator if it is present. */
    private void hideTypingIndicator() {
        messageBox.getChildren().remove(typingIndicator);
    }

    /**
     * Animates a node in using a fade + slight slide.
     *
     * @param node node to animate
     */
    private void animateIn(Node node) {
        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(ANIM_DURATION, node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(ANIM_DURATION, node);
        slide.setFromY(6);
        slide.setToY(0);

        fade.play();
        slide.play();
    }

    /**
     * Scrolls to the bottom safely after layout updates.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /**
     * Hides scrollbars (still scrollable with mouse/trackpad).
     * This is your "6-line code" trick.
     */
    private void hideScrollBars() {
        scrollPane.lookupAll(".scroll-bar").forEach(n -> n.setStyle("-fx-opacity: 0;"));
    }

    /**
     * Plays the CLI welcome message from {@link Ui#showWelcome()} as 3 bubbles with delays.
     * This welcome sequence is NOT saved to history, so it appears every run without duplication.
     */
    private void playWelcomeSequenceFromUiEphemeral() {
        Ui ui = new Ui();
        ui.beginCapture();
        ui.showWelcome();
        String welcome = ui.endCapture();

        String cleaned = welcome.replace(Ui.LINE, "").trim();

        int commandsIdx = cleaned.indexOf("\uD83D\uDCCC Available Commands:");
        int tipsIdx = cleaned.indexOf("\uD83D\uDCA1 Helpful Tips:");

        String intro = cleaned;
        String commands = "";
        String tips = "";

        if (commandsIdx >= 0) {
            intro = cleaned.substring(0, commandsIdx).trim();
        }

        if (commandsIdx >= 0) {
            int commandsEnd = (tipsIdx >= 0) ? tipsIdx : cleaned.length();
            commands = cleaned.substring(commandsIdx, commandsEnd).trim();
        }

        if (tipsIdx >= 0) {
            tips = cleaned.substring(tipsIdx).trim();
        }

        String[] blocks = { intro, commands, tips };
        addBotMessagesWithDelayEphemeral(blocks, WELCOME_DELAY_MS);
    }

    /**
     * Adds multiple bot messages with delay between bubbles (ephemeral = not saved).
     *
     * @param messages messages to display
     * @param delayMs delay between bubbles
     */
    private void addBotMessagesWithDelayEphemeral(String[] messages, int delayMs) {
        int shown = 0;

        for (String msg : messages) {
            String cleaned = (msg == null) ? "" : msg.trim();
            if (cleaned.isEmpty()) {
                continue;
            }

            int indexDelay = shown * delayMs;
            PauseTransition pt = new PauseTransition(Duration.millis(indexDelay));
            pt.setOnFinished(e -> addBotMessageEphemeral(cleaned));
            pt.play();

            shown++;
        }
    }

    /**
     * Appends a timestamped record to the chat log.
     * Format: ISO_INSTANT|TYPE|payload
     *
     * @param type record type (DATE / ME / BIT / SESSION_END)
     * @param payload record data
     */
    private void appendToChatLog(String type, String payload) {
        try {
            Path p = Paths.get(CHAT_LOG_PATH);
            Files.createDirectories(p.getParent());

            String safePayload = payload == null ? "" : payload.replace("\n", "\\n");
            String line = Instant.now().toString() + SEP + type + SEP + safePayload + System.lineSeparator();

            Files.writeString(
                    p,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Non-fatal: GUI still works even if logging fails.
        }
    }

    /**
     * Loads chat history from disk and reconstructs the UI using only the last 7 days.
     * Also prunes the file so it does not grow forever.
     */
    private void loadChatLogIfAny() {
        try {
            Path p = Paths.get(CHAT_LOG_PATH);
            if (!Files.exists(p)) {
                return;
            }

            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            StringBuilder kept = new StringBuilder();

            LocalDate lastChipDate = null;

            for (String raw : lines) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }

                // Expected: ts|TYPE|payload
                String[] parts = raw.split("\\|", 3);
                if (parts.length < 3) {
                    continue; // skip corrupted/legacy
                }

                Instant ts;
                try {
                    ts = Instant.parse(parts[0]);
                } catch (Exception ex) {
                    continue;
                }

                if (!withinRetention(ts)) {
                    continue;
                }

                String type = parts[1];
                String payload = parts[2].replace("\\n", "\n");

                kept.append(raw).append(System.lineSeparator());

                if ("DATE".equals(type)) {
                    LocalDate chipDay = toLocalDate(ts);

                    String label = payload;
                    if ("Today".equals(label) && !chipDay.equals(LocalDate.now())) {
                        label = chipDay.format(CHIP_DATE_FMT);
                    }

                    messageBox.getChildren().add(buildDateChipRow(label));
                    lastChipDate = chipDay;

                } else if ("ME".equals(type)) {
                    messageBox.getChildren().add(buildMessageRow(payload, true));

                } else if ("BIT".equals(type)) {
                    messageBox.getChildren().add(buildMessageRow(payload, false));
                }
            }

            Files.writeString(
                    p,
                    kept.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            lastDateShown = lastChipDate;

        } catch (IOException ignored) {
            // Non-fatal
        }
    }

    /**
     * Returns true if ts is within the last {@link #RETENTION_DAYS} days.
     *
     * @param ts timestamp
     * @return whether to keep
     */
    private boolean withinRetention(Instant ts) {
        Instant cutoff = Instant.now().minus(java.time.Duration.ofDays(RETENTION_DAYS));
        return !ts.isBefore(cutoff);
    }

    /**
     * Converts an instant to local date in system timezone.
     *
     * @param ts timestamp
     * @return local date
     */
    private LocalDate toLocalDate(Instant ts) {
        return ts.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Cleans bot output so bubbles look nicer:
     * removes divider line and trims excessive whitespace.
     *
     * @param raw raw output from Bit
     * @return cleaned output
     */
    private String cleanOutput(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace(Ui.LINE, "").trim();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        launch(args);
    }
}