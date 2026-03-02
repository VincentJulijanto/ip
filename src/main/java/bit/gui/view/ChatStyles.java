package bit.gui.view;

import javafx.util.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Centralized styling + UI constants for the chat UI.
 * Keeps the view code clean and avoids duplicated magic numbers.
 */
public final class ChatStyles {

    /** Prevent instantiation. */
    private ChatStyles() { }

    // ===== Theme =====
    public static final String BG = "#F7F8FC";
    public static final String SURFACE = "#FFFFFF";
    public static final String BORDER = "#E5E7EB";

    public static final String PRIMARY = "#4F46E5";
    public static final String BOT_BUBBLE = "#E8F7F5";

    public static final String TEXT = "#0F172A";
    public static final String MUTED = "#6B7280";
    public static final String ONLINE = "#16A34A";
    public static final String TS_BOT = "#94A3B8";

    // ===== Layout =====
    public static final double BUBBLE_WIDTH_RATIO = 0.82;

    public static final double MIN_SIDE_GUTTER = 12;
    public static final double MAX_SIDE_GUTTER = 72;
    public static final double SIDE_GUTTER_RATIO = 0.12;

    // ===== Animation =====
    public static final Duration ANIM_DURATION = Duration.millis(300);

    // ===== Time formatting =====
    public static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
}
