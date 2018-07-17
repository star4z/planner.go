package go.planner.plannergo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ColorPicker {

    private static int colorPrimary;
    private static int colorPrimaryAccent;
    private static int colorPrimaryText;
    private static int colorSecondary;
    private static int colorSecondaryAccent;
    private static int colorSecondaryText;
    private static int colorCompleted;
    private static int colorCompletedAccent;
    private static int colorCompletedText;
    private static int colorAssignment;
    private static int colorAssignmentAccent;
    private static int colorAssignmentText;

    private static void setColors(String palette, Context context) {
        int[] colors;
        switch (palette) {
            case "Bright":
                colors = context.getResources().getIntArray(R.array.bright_palette);
                break;
            case "Earthy":
                colors = context.getResources().getIntArray(R.array.earthy_palette);
                break;
            case "Ice Cream":
                colors = context.getResources().getIntArray(R.array.ice_cream_palette);
                break;
            case "Patriotic":
                colors = context.getResources().getIntArray(R.array.patriotic_palette);
                break;
            case "Mint":
                colors = context.getResources().getIntArray(R.array.mint_palette);
                break;
            case "Monochrome":
            default:
                colors = context.getResources().getIntArray(R.array.monochrome_palette);
                break;
            case "Space White":
                colors = context.getResources().getIntArray(R.array.space_white_palette);
        }

        updateColors(colors);
    }

    /**
     * Checks the color scheme from shared preferences and then sets it as the current one.
     * @param context Used for reading preferences.
     */
    public static void setColors(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        setColors(preferences.getString("pref_color_scheme", "Mint"), context);
    }

    /**
     * Updates the statically stored values of the current color palette for reference by other
     * classes.
     *
     * @param array contains colors for a color scheme as int values.
     */
    private static void updateColors(int[] array) {
        colorPrimary = array[0];
        colorPrimaryAccent = array[1];
        colorPrimaryText = array[2];

        colorSecondary = array[3];
        colorSecondaryAccent = array[4];
        colorSecondaryText = array[5];


        colorCompleted = array[6];
        colorCompletedAccent = array[7];
        colorCompletedText = array[8];

        colorAssignment = array[9];
        colorAssignmentAccent = array[10];
        colorAssignmentText = array[11];
    }

    public static int getColorPrimaryText() {
        return colorPrimaryText;
    }

    public static int getColorSecondaryText() {
        return colorSecondaryText;
    }

    public static int getColorCompletedText() {
        return colorCompletedText;
    }

    public static int getColorAssignmentText() {
        return colorAssignmentText;
    }



    public static int getColorPrimary() {
        return colorPrimary;
    }

    public static int getColorPrimaryAccent() {
        return colorPrimaryAccent;
    }

    public static int getColorSecondary() {
        return colorSecondary;
    }

    public static int getColorSecondaryAccent() {
        return colorSecondaryAccent;
    }

    public static int getColorCompleted() {
        return colorCompleted;
    }

    public static int getColorCompletedAccent() {
        return colorCompletedAccent;
    }

    public static int getColorAssignment() {
        return colorAssignment;
    }

    public static int getColorAssignmentAccent() {
        return colorAssignmentAccent;
    }
}
