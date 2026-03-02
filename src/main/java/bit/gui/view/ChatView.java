package bit.gui.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatView renders the JavaFX UI for Bit:
 * header, chat area, input bar, message bubbles, typing indicator, animations.
 *
 * No bot logic, no persistence, no date chip logic here.
 */
public class ChatView {

    private static final String BG = "#F7F8FC";
    private static final String SURFACE = "#FFFFFF";
    private static final String BORDER = "#E5E7EB";

    private static final String PRIMARY = "#4F46E5";
    private static final String BOT_BUBBLE = "#E8F7F5";

    private static final String TEXT = "#0F172A";
    private static final String MUTED = "#6B7280";
    private static final String ONLINE = "#16A34A";
    private static final String TS_BOT = "#94A3B8";

    // ===== Layout constants =====
    private static final double BUBBLE_WIDTH_RATIO = 0.82;

    private static final double MIN_SIDE_GUTTER = 12;
    private static final double MAX_SIDE_GUTTER = 72;
    private static final double SIDE_GUTTER_RATIO = 0.12;

    // ===== Anim =====
    private static final Duration ANIM_DURATION = Duration.millis(300);

    // ===== Time formatting =====
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // ===== Root layout =====
    private final VBox root = new VBox();

    // ===== Header =====
    private final HBox header = buildHeader();

    // ===== Chat area =====
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox chatContent = new VBox();
    private final VBox messageBox = new VBox(10);
    private final Region topSpacer = new Region();

    // used to bind bubble widths to viewport width
    private final Region viewportWidthProbe = new Region();

    // ===== Input =====
    private final TextField userInput = new TextField();
    private final Button sendButton = new Button("➤");

    // ===== Typing indicator =====
    private final HBox typingIndicator = buildTypingIndicator();

    public ChatView() {
        configureChatArea();
        VBox inputBar = buildInputBar();

        root.getChildren().addAll(header, scrollPane, inputBar);
        root.setStyle("-fx-background-color: " + BG + ";");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    // =========================
    // Public getters (controller uses these)
    // =========================

    public VBox getRoot() {
        return root;
    }

    public TextField getUserInput() {
        return userInput;
    }

    public Button getSendButton() {
        return sendButton;
    }

    public VBox getMessageBox() {
        return messageBox;
    }

    public void afterShow() {
        // hide scrollbars after scene is rendered
        Platform.runLater(this::hideScrollBars);
        scrollToBottom();
    }

    // =========================
    // Public UI actions
    // =========================

    public void addUserMessage(String text) {
        appendMessage(buildMessageRow(text, true));
    }

    public void addBotMessage(String text) {
        appendMessage(buildMessageRow(text, false));
    }

    public void addBotMessageEphemeral(String text) {
        // same render, controller decides what "ephemeral" means
        appendMessage(buildMessageRow(text, false));
    }

    public void showTypingIndicator() {
        if (!messageBox.getChildren().contains(typingIndicator)) {
            messageBox.getChildren().add(typingIndicator);
            animateIn(typingIndicator);
            scrollToBottom();
        }
    }

    public void hideTypingIndicator() {
        messageBox.getChildren().remove(typingIndicator);
    }

    public void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /**
     * Optional: allow controller to insert any custom node (e.g., date chip row).
     */
    public void addCustomNode(Node node) {
        messageBox.getChildren().add(node);
        animateIn(node);
        scrollToBottom();
    }

    // =========================
    // Build UI sections
    // =========================

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

    private void configureChatArea() {
        messageBox.setPadding(new Insets(14, 0, 14, 22));
        messageBox.setStyle("-fx-background-color: transparent;");

        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        chatContent.getChildren().addAll(topSpacer, messageBox);
        chatContent.setFillWidth(true);
        chatContent.setStyle("-fx-background-color: " + BG + ";");

        scrollPane.setContent(chatContent);

        viewportWidthProbe.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getWidth(),
                scrollPane.viewportBoundsProperty()
        ));

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: " + BG + "; -fx-background-color: transparent;");

        chatContent.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> scrollPane.getViewportBounds().getHeight(),
                scrollPane.viewportBoundsProperty()
        ));
    }

    private VBox buildInputBar() {
        userInput.setPromptText("Type a command...");
        userInput.setStyle(
                "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 11 12 11 12;" +
                        "-fx-font-size: 13px;"
        );

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

    // =========================
    // Message rendering
    // =========================

    private void appendMessage(HBox node) {
        messageBox.getChildren().add(node);
        animateIn(node);
        scrollToBottom();
    }

    private HBox buildMessageRow(String text, boolean isUser) {
        Text name = new Text(isUser ? "Me" : "Bit");
        name.setStyle("-fx-fill: " + MUTED + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.maxWidthProperty().bind(viewportWidthProbe.minWidthProperty().multiply(BUBBLE_WIDTH_RATIO));
        msg.setStyle("-fx-text-fill: " + (isUser ? "white" : TEXT) + "; -fx-font-size: 13px;");
        msg.setMinWidth(0);

        String time = LocalDateTime.now().format(TIME_FMT);
        Text ts = new Text(time);
        ts.setStyle(isUser
                ? "-fx-fill: rgba(255,255,255,0.85); -fx-font-size: 10.5px;"
                : "-fx-fill: " + TS_BOT + "; -fx-font-size: 10.5px;"
        );

        VBox bubble = new VBox(6, msg, ts);
        bubble.maxWidthProperty().bind(viewportWidthProbe.minWidthProperty().multiply(BUBBLE_WIDTH_RATIO));
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setStyle(
                "-fx-background-radius: 22;" +
                        "-fx-background-color: " + (isUser ? PRIMARY : BOT_BUBBLE) + ";" +
                        "-fx-border-color: " + (isUser ? "transparent" : BORDER) + ";" +
                        "-fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0.2, 0, 4);"
        );

        VBox stack = new VBox(4, name, bubble);
        stack.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        stack.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double g = currentSideGutter();
            return isUser
                    ? new Insets(2, 2, 2, g)
                    : new Insets(2, g, 2, 2);
        }, scrollPane.viewportBoundsProperty()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(8);
        if (isUser) {
            row.getChildren().addAll(spacer, stack);
        } else {
            row.getChildren().addAll(stack, spacer);
        }

        row.prefWidthProperty().bind(viewportWidthProbe.minWidthProperty());
        row.setMaxWidth(Double.MAX_VALUE);
        row.setPadding(new Insets(2, 0, 2, 0));

        return row;
    }

    // =========================
    // Helpers
    // =========================

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

    private void hideScrollBars() {
        scrollPane.lookupAll(".scroll-bar").forEach(n -> n.setStyle("-fx-opacity: 0;"));
    }

    private double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private double currentSideGutter() {
        double w = scrollPane.getViewportBounds().getWidth();
        return clamp(w * SIDE_GUTTER_RATIO, MIN_SIDE_GUTTER, MAX_SIDE_GUTTER);
    }
}