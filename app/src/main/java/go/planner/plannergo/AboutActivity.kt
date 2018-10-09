package go.planner.plannergo

import android.os.Bundle
import android.support.v4.app.NavUtils.navigateUpFromSameTask
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        about_toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccentDark)
        setSupportActionBar(about_toolbar)

        val text = "Version ${BuildConfig.VERSION_NAME}"
        version.text = text
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
