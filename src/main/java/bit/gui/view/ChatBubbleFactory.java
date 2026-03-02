package bit.gui.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;

/**
 * Factory for constructing chat message rows and related UI bubbles.
 * Keeps ChatView focused on layout and wiring.
 */
public class ChatBubbleFactory {

    /**
     * Builds a message row (name label + bubble + timestamp).
     *
     * @param text message content (non-null)
     * @param isUser true for user messages (right aligned), false for bot (left aligned)
     * @param viewportWidthProbe probe node used to bind width to scroll viewport
     * @param bubbleWidthRatio max bubble width ratio
     * @param gutterProvider function to compute side gutter at runtime
     * @return a fully assembled message row
     */
    public HBox buildMessageRow(
            String text,
            boolean isUser,
            Region viewportWidthProbe,
            double bubbleWidthRatio,
            GutterProvider gutterProvider
    ) {
        Text name = new Text(isUser ? "Me" : "Bit");
        name.setStyle("-fx-fill: " + ChatStyles.MUTED + "; -fx-font-size: 11px; -fx-font-weight: 700;");

        Label msg = new Label(text);
        msg.setWrapText(true);
        msg.maxWidthProperty().bind(viewportWidthProbe.minWidthProperty().multiply(bubbleWidthRatio));
        msg.setStyle("-fx-text-fill: " + (isUser ? "white" : ChatStyles.TEXT) + "; -fx-font-size: 13px;");
        msg.setMinWidth(0);

        String time = LocalDateTime.now().format(ChatStyles.TIME_FMT);
        Text ts = new Text(time);
        ts.setStyle(isUser
                ? "-fx-fill: rgba(255,255,255,0.85); -fx-font-size: 10.5px;"
                : "-fx-fill: " + ChatStyles.TS_BOT + "; -fx-font-size: 10.5px;"
        );

        VBox bubble = new VBox(6, msg, ts);
        bubble.maxWidthProperty().bind(viewportWidthProbe.minWidthProperty().multiply(bubbleWidthRatio));
        bubble.setPadding(new Insets(12, 14, 12, 14));
        bubble.setStyle(
                "-fx-background-radius: 22;" +
                        "-fx-background-color: " + (isUser ? ChatStyles.PRIMARY : ChatStyles.BOT_BUBBLE) + ";" +
                        "-fx-border-color: " + (isUser ? "transparent" : ChatStyles.BORDER) + ";" +
                        "-fx-border-radius: 22;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0.2, 0, 4);"
        );

        VBox stack = new VBox(4, name, bubble);
        stack.setAlignment(isUser ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

        stack.paddingProperty().bind(Bindings.createObjectBinding(() -> {
            double g = gutterProvider.currentSideGutter();
            return isUser
                    ? new Insets(2, 2, 2, g)
                    : new Insets(2, g, 2, 2);
        }, viewportWidthProbe.minWidthProperty()));

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

    /**
     * Builds a reusable typing indicator row ("Bit is typing...").
     *
     * @return typing indicator row node
     */
    public HBox buildTypingIndicator() {
        Text t = new Text("Bit is typing...");
        t.setStyle("-fx-fill: #14B8A6; -fx-font-size: 12px; -fx-font-weight: 700;");

        VBox bubble = new VBox(t);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
                "-fx-background-radius: 22;" +
                        "-fx-background-color: " + ChatStyles.SURFACE + ";" +
                        "-fx-border-color: " + ChatStyles.BORDER + ";" +
                        "-fx-border-radius: 22;"
        );
        bubble.setMaxWidth(Region.USE_PREF_SIZE);

        HBox row = new HBox(bubble);
        row.setPadding(new Insets(2, 6, 2, 6));
        row.setStyle("-fx-alignment: CENTER_LEFT;");
        return row;
    }

    /**
     * Functional interface for computing side gutter width.
     */
    @FunctionalInterface
    public interface GutterProvider {
        /**
         * Returns the current side gutter width in pixels.
         *
         * @return gutter width
         */
        double currentSideGutter();
    }
}
