package go.planner.plannergo

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils.navigateUpFromSameTask
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        about_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccentDark)
        setSupportActionBar(about_toolbar)

        val text = "Version ${BuildConfig.VERSION_NAME}"
        version.text = text

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val colorScheme = if (prefs.getBoolean(Settings.darkMode, true)) {
            ColorScheme.SCHEME_DARK
        } else {
            ColorScheme.SCHEME_LIGHT
        }
        background.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG))
        val textColor = colorScheme.getColor(this, Field.MAIN_CARD_TEXT)
        version.setTextColor(textColor)
        text_body.setTextColor(textColor)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
    //Returns to the Settings activity
        android.R.id.home -> consume { navigateUpFromSameTask(this) }
        else -> super.onOptionsItemSelected(item)
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
