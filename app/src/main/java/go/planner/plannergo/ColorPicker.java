package go.planner.plannergo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Random;


/**
 * At one time it handled themes, but has since been superseded by ColorScheme
 * It's still around because it handles class color options.
 */
public class ColorPicker {

    private static SharedPreferences preferences;
    private static Resources res;

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

    private static HashMap<String, Integer> classColors;

    /**
     * Updates theme from palette
     *
     * @param palette Indicates which theme to use
     */
    private static void setColors(String palette) {
        int[] colors;
        switch (palette) {
            case "Bright":
                colors = res.getIntArray(R.array.bright_palette);
                break;
            case "Earthy":
                colors = res.getIntArray(R.array.earthy_palette);
                break;
            case "Ice Cream":
                colors = res.getIntArray(R.array.ice_cream_palette);
                break;
            case "Patriotic":
                colors = res.getIntArray(R.array.patriotic_palette);
                break;
            case "Mint":
                colors = res.getIntArray(R.array.mint_palette);
                break;
            case "Monochrome":
            default:
                colors = res.getIntArray(R.array.monochrome_palette);
                break;
            case "Space White":
                colors = res.getIntArray(R.array.space_white_palette);
        }

        updateColors(colors);
    }

    /**
     * Checks the color scheme from shared preferences and then sets it as the current one.
     *
     * @param context Used for reading preferences.
     */
    public static void setColors(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        res = context.getResources();

        setColors(preferences.getString("pref_color_scheme", "Mint"));
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

    /**
     * All-in-one class color code handler
     * Creates new set of colors if necessary
     * Adds a new item to set if necessary
     * Based on class name, returns color int
     * @param className school class name; compared to FileIO.classes data
     * @return color as int from classColors
     */
    public static int getClassColor(String className) {
        if (classColors == null) {
            classColors = new HashMap<>();

            Random random = new Random(System.currentTimeMillis());

            for (String c : FileIO.classNames) {
                int color = generateClassColor(random);
                classColors.put(c, color);
            }
        }

        if (!classColors.containsKey(className)){
            Random random = new Random(System.currentTimeMillis());

            int color = generateClassColor(random);
            classColors.put(className, color);
        }

        return classColors.get(className);
    }

    private static int generateClassColor(Random r){
        return Color.argb(
                255,
                r.nextInt(200),
                r.nextInt(200),
                r.nextInt(200)
        );
    }
}