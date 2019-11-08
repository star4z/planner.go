package go.planner.plannergo;

/**
 * Stores preferences keys as constants
 */
@SuppressWarnings("WeakerAccess")
public class Settings {
    public static final String timeEnabled = "pref_time_enabled";
    public static final String overdueLast = "pref_overdue_last";
    @Deprecated
    public static final String colorScheme = "pref_color_scheme";
    public static final String darkMode = "pref_dark_mode";
    public static final String classColorsEnabled = "pref_class_colors_enabled";
    public static final String priorityFirst = "pref_priority_first";
    public static final String notifEnabled = "pref_notif_enabled";
    public static final String notif1Time = "pref_notif_time";
    public static final String notif1DaysBefore = "pref_notif_days_before";
    public static final String notif2Enabled = "pref_extra_notif_enabled";
    public static final String notif2Time = "pref_notif_time_extra";
    public static final String notif2DaysBefore = "pref_notif_days_before_extra";
    public static final String promptSave = "pref_save_remind";
    public static final String driveSingleMode = "pref_drive_single_mode";
}
