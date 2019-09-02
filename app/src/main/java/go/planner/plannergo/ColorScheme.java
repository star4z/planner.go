package go.planner.plannergo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ColorScheme {
    //indices
    static final int PRIMARY = 0; //App bar, layout background, and navigation bar
    static final int PRIMARY_DARK = 1; //Notification bar
    static final int ACCENT = 2; //FAB
    static final int ASSIGNMENT_VIEW_BG = 3; //Tile background
    static final int TEXT_COLOR = 4; //Text color
    static final int SUB_TEXT_COLOR = 5;
    private static final String TAG = "ColorScheme";
    @SuppressLint("StaticFieldLeak")
    public static final ColorScheme SCHEME_DARK = new ColorScheme(true);
    @SuppressLint("StaticFieldLeak")
    static final ColorScheme SCHEME_LIGHT = new ColorScheme(false);

    private ArrayList<Integer> colors;
    private int theme;
    private boolean isDarkMode;
    private Context c;

    ColorScheme(boolean isDarkMode, Context c) {
        this.c = c;
        this.isDarkMode = isDarkMode;

        if (isDarkMode) {
            theme = R.style.DarkTheme;

            colors = new ArrayList<>();
            addColor(PRIMARY, R.color.darkPrimary);
            addColor(PRIMARY_DARK, R.color.darkPrimaryDark);
            addColor(ACCENT, R.color.darkAccent);
            addColor(ASSIGNMENT_VIEW_BG, R.color.darkTextViewColor);
            addColor(TEXT_COLOR, R.color.darkTextPrimary);
            addColor(SUB_TEXT_COLOR, R.color.darkTextSecondary);
        } else {
            theme = R.style.LightTheme;
            colors = new ArrayList<>();
            addColor(PRIMARY, R.color.lightPrimary);
            addColor(PRIMARY_DARK, R.color.lightPrimaryDark);
            addColor(ACCENT, R.color.lightAccent);
            addColor(ASSIGNMENT_VIEW_BG, R.color.lightTextViewColor);
            addColor(TEXT_COLOR, R.color.lightText);
            addColor(SUB_TEXT_COLOR, R.color.lightTextSecondary);
        }
    }

    private ColorScheme(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        if (isDarkMode) {
            theme = R.style.DarkTheme;
        } else {
            theme = R.style.LightTheme;
        }
    }

    public int getColor(int pos) {
        if (pos < 0 || pos >= colors.size()) {
            Log.e(TAG, "Invalid color pos received: " + pos + ", " + colors);
            return Color.CYAN; //Error color
        }
        return colors.get(pos);
    }

    public int getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorScheme that = (ColorScheme) o;

        return isDarkMode == that.isDarkMode;
    }

    private void addColor(int pos, int color) {
        colors.add(pos, ContextCompat.getColor(c, color));
    }
}
