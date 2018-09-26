package go.planner.plannergo

/**
 * For use with activities that use a dynamic color scheme.
 * Must have a private data member of type ColorScheme.
 * Must call setColorScheme() before super call in onCreate() method.
 *
 */
internal interface ColorSchemeActivity {
    /**
     * Handles retrieving color scheme from preferences (PreferenceManager.getDefaultSharedPreferences())
     * Must call setTheme(colorScheme.theme)
     * Must be called
     */
    fun setColorScheme()

    /**
     * returns the color scheme currently applied to the Activity.
     * Useful for applying the color scheme to any views inflated with a class other than the Activity.
     */
    fun getColorScheme(): ColorScheme

    /**
     * If color scheme has been changed, restarts activity (so that theme can be reapplied).
     * If the color scheme has not been applied yet, calls applyColors() to do so.
     */
    fun checkForColorSchemeUpdate()

    /**
     * Sets the colors in the activity layout based on the colorScheme.
     * Once completed, sets schemeSet to true.
     */
    fun applyColors()
}