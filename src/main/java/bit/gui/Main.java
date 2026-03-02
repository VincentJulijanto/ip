package bit.gui;

import bit.Bit;
import bit.storage.ChatLog;
import bit.gui.view.ChatView;
import bit.gui.controller.ChatController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main entry point for the JavaFX GUI.
 * Creates app dependencies and wires View + Controller.
 */
public class Main extends Application {

    private static final String CHAT_LOG_PATH = "./data/chatlog.txt";
    private static final int RETENTION_DAYS = 7;

    @Override
    public void start(Stage stage) {
        Bit bot = new Bit();
        ChatLog chatLog = new ChatLog(CHAT_LOG_PATH, RETENTION_DAYS);

        ChatView view = new ChatView();
        new ChatController(bot, chatLog, view); // controller wires events + loads history + welcome

        Scene scene = new Scene(view.getRoot(), 420, 640);
        stage.setTitle("Bit");
        stage.setScene(scene);
        stage.show();

        view.afterShow();
    }

    public static void main(String[] args) {
        launch(args);
    }
}