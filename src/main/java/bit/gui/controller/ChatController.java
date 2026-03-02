package bit.gui.controller;

import bit.Bit;
import bit.cli.Ui;
import bit.storage.ChatLog;
import bit.gui.view.ChatView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatController coordinates:
 * - user input -> bot response
 * - persistence via ChatLog
 * - date chips + midnight refresh
 * - welcome sequence (ephemeral)
 *
 * It does NOT build the whole UI layout (that's ChatView's job).
 */
public class ChatController {

    private static final Duration TYPING_DELAY = Duration.millis(420);
    private static final Duration EXIT_DELAY = Duration.millis(800);
    private static final int WELCOME_DELAY_MS = 1350;

    private static final DateTimeFormatter CHIP_DATE_FMT = DateTimeFormatter.ofPattern("EEE, MMM d");

    private final Bit bot;
    private final ChatLog chatLog;
    private final ChatView view;

    private LocalDate lastDateShown = null;
    private Text lastChipTextNode = null;

    /**
     * Creates controller and wires event handlers.
     *
     * @param bot chatbot logic
     * @param chatLog persistence for history
     * @param view UI renderer
     */
    public ChatController(Bit bot, ChatLog chatLog, ChatView view) {
        this.bot = bot;
        this.chatLog = chatLog;
        this.view = view;

        wireInputHandlers();
        loadHistory();
        ensureDateChip();

        playWelcomeSequenceEphemeral(); // <-- this restores your “3 bubbles” behavior
        scheduleMidnightUpdate();
    }

    private void wireInputHandlers() {
        view.getSendButton().setOnAction(e -> handleUserInput());
        view.getUserInput().setOnAction(e -> handleUserInput());
    }

    private void handleUserInput() {
        String input = view.getUserInput().getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String trimmed = input.trim();
        view.getUserInput().clear();

        ensureDateChip();
        chatLog.append(ChatLog.TYPE_ME, trimmed);
        view.addUserMessage(trimmed);

        view.showTypingIndicator();

        PauseTransition delay = new PauseTransition(TYPING_DELAY);
        delay.setOnFinished(e -> {
            view.hideTypingIndicator();

            String response = bot.getResponse(trimmed);
            if (response != null && !response.isBlank()) {
                ensureDateChip();
                String cleaned = cleanOutput(response);

                chatLog.append(ChatLog.TYPE_BIT, cleaned);
                view.addBotMessage(cleaned);
            }

            if (trimmed.equalsIgnoreCase("bye")) {
                chatLog.append(ChatLog.TYPE_SESSION_END, "BYE");
                PauseTransition exitDelay = new PauseTransition(EXIT_DELAY);
                exitDelay.setOnFinished(e2 -> Platform.exit());
                exitDelay.play();
            }
        });
        delay.play();
    }

    private void loadHistory() {
        LocalDate lastChipDate = null;

        for (ChatLog.Record r : chatLog.loadRecentAndPrune()) {
            if (ChatLog.TYPE_DATE.equals(r.type)) {
                LocalDate chipDay = ChatLog.toLocalDate(r.ts);
                String label = ChatLog.normalizeChipLabel(r.payload, chipDay, CHIP_DATE_FMT);

                Node chipRow = buildDateChipRow(label);
                view.addCustomNode(chipRow);
                lastChipDate = chipDay;

            } else if (ChatLog.TYPE_ME.equals(r.type)) {
                view.addUserMessage(r.payload);

            } else if (ChatLog.TYPE_BIT.equals(r.type)) {
                view.addBotMessage(r.payload);
            }
        }

        lastDateShown = lastChipDate;
    }

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

    private void addNewDateChip(String label) {
        chatLog.append(ChatLog.TYPE_DATE, label);
        view.addCustomNode(buildDateChipRow(label));
    }

    /**
     * Builds a date chip row node. Controller owns this logic so ChatView stays focused on chat bubbles.
     */
    private Node buildDateChipRow(String label) {
        Text t = new Text(label);
        t.setStyle("-fx-fill: #0F172A; -fx-font-size: 12.5px; -fx-font-weight: 700;");
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
     * Splits Ui.showWelcome() into 3 bubbles: intro, commands, tips (ephemeral).
     */
    private void playWelcomeSequenceEphemeral() {
        Ui ui = new Ui();
        ui.beginCapture();
        ui.showWelcome();
        String welcome = ui.endCapture();

        String cleaned = welcome.replace(Ui.LINE, "").trim();

        int commandsIdx = cleaned.indexOf("📌 Available Commands:");
        int tipsIdx = cleaned.indexOf("💡 Helpful Tips:");

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
        addBotMessagesWithDelay(blocks, WELCOME_DELAY_MS);
    }

    private void addBotMessagesWithDelay(String[] messages, int delayMs) {
        int shown = 0;

        for (String msg : messages) {
            String m = (msg == null) ? "" : msg.trim();
            if (m.isEmpty()) continue;

            int indexDelay = shown * delayMs;
            PauseTransition pt = new PauseTransition(Duration.millis(indexDelay));
            pt.setOnFinished(e -> view.addBotMessageEphemeral(m));
            pt.play();

            shown++;
        }
    }

    private String cleanOutput(String raw) {
        if (raw == null) return "";
        return raw.replace(Ui.LINE, "").trim();
    }
}
