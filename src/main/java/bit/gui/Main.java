package bit.gui;

import bit.Bit;
import bit.gui.view.ChatView;
import bit.storage.ChatLog;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the JavaFX GUI.
 * Main is responsible only for bootstrapping the app (not UI logic).
 */
public class Main extends Application {

    /** File used to persist chat history between app runs. */
    private static final String CHAT_LOG_PATH = "./data/chatlog.txt";

    /** Keep only the last N days of history. */
    private static final int RETENTION_DAYS = 7;

    private final Bit bot = new Bit();
    private final ChatLog chatLog = new ChatLog(CHAT_LOG_PATH, RETENTION_DAYS);

    @Override
    public void start(Stage stage) {
        ChatView view = new ChatView();

        Scene scene = new Scene(view.getRoot(), 420, 640);
        stage.setTitle("Bit");
        stage.setScene(scene);
        stage.show();

        view.afterShow();
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