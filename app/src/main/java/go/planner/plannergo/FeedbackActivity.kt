package go.planner.plannergo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.billingclient.api.BillingClient
import go.planner.plannergo.planner_billing.billing.BillingManager
import go.planner.plannergo.planner_billing.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED
import go.planner.plannergo.planner_billing.billing.BillingProvider
import go.planner.plannergo.planner_billing.skulist.AcquireFragment
import kotlinx.android.synthetic.main.activity_feedback.*
import kotlinx.android.synthetic.main.toolbar.*

/**
 * Stores miscellaneous options that are not settings.
 * Labeled "Help and Feedback" in the app.
 * Uses billing library methods.
 */
class FeedbackActivity : AppCompatActivity(), BillingProvider, ColorSchemeActivity {
    private lateinit var prefs: SharedPreferences
    private lateinit var colorScheme: ColorScheme
    private var schemeSet = false

    // Debug tag, for logging
    private val tag = "BaseGamePlayActivity"

    private val dialogTag = "dialog"

    private lateinit var mViewController: MainViewController
    private lateinit var mBillingManager: BillingManager
    private lateinit var mAcquireFragment: AcquireFragment

    private lateinit var mScreenMain: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)

        mViewController = MainViewController(this)

        //This only threw this error when applying changes while FeedbackActivity was active.
        //No apparent way to make it break this way in the wild, but the catch is there just to be safe.
        mAcquireFragment = try {
            if (savedInstanceState != null)
                supportFragmentManager.findFragmentByTag(dialogTag) as AcquireFragment
            else
                AcquireFragment()
        } catch (e: TypeCastException) {
            AcquireFragment()
        }


        mBillingManager = BillingManager(this, @Suppress("INACCESSIBLE_TYPE") mViewController.updateListener)

        mScreenMain = findViewById(R.id.screen_main)


    }

    override fun onResume() {
        super.onResume()
        checkForColorSchemeUpdate()
        if (mBillingManager.billingClientResponseCode == BillingClient.BillingResponse.OK) {
            mBillingManager.queryPurchases()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            toolbar.setTitle(R.string.settings)
            supportFragmentManager.popBackStack()
        }
        return true
    }
    override fun getBillingManager(): BillingManager {
        return mBillingManager
    }

    fun replayTutorial(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, TutorialActivityTitle::class.java))
    }

    fun openDonationDialog(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.d(tag, "Donation button clicked")
        if (!::mAcquireFragment.isInitialized) {
            mAcquireFragment = AcquireFragment()
        }

        if (!isAcquireFragmentShown()) {
            mAcquireFragment.show(supportFragmentManager, dialogTag)
            if (mBillingManager.billingClientResponseCode > BILLING_MANAGER_NOT_INITIALIZED)
                mAcquireFragment.onManagerReady(this)
        }
    }

    fun openAboutActivity(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    fun openEmailForFeedback(@Suppress("UNUSED_PARAMETER") view: View) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Planner Agenda Feedback")//English, since it's for me to read
        intent.data = Uri.parse("mailto:benjaminphillipsdeveloper@gmail.com")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.email_app_missing, Toast.LENGTH_LONG).show()
        }
    }

    fun gotoAppRating(@Suppress("UNUSED_PARAMETER") view: View) {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
        }

    }

    /**
     * Show an alert dialog to the user
     * @param messageId String id to display inside the alert dialog
     * @param optionalParam Optional attribute for the string
     */
    @UiThread
    fun alert(@StringRes messageId: Int, @Nullable optionalParam: Any?) {
        if (Looper.getMainLooper().thread !== Thread.currentThread()) {
            throw RuntimeException("Dialog could be shown only from the main thread")
        }

        val bld = AlertDialog.Builder(this)
        bld.setNeutralButton("OK", null)

        if (optionalParam == null) {
            bld.setMessage(messageId)
        } else {
            bld.setMessage(resources.getString(messageId, optionalParam))
        }

        bld.create().show()
    }

    fun onBillingManagerSetupFinished() {
        mAcquireFragment.onManagerReady(this)
    }

    private fun isAcquireFragmentShown(): Boolean {
        return !::mAcquireFragment.isInitialized && mAcquireFragment.isVisible
    }

    override fun isOneDollarDonor(): Boolean {
        return mViewController.isOneDollarDonor
    }

    override fun isFiveDollarDonor(): Boolean {
        return mViewController.isFiveDollarDonor
    }

    fun showRefreshedUi(){
        if (::mAcquireFragment.isInitialized) {
            mAcquireFragment.refreshUI()
        }
    }

    override fun setColorScheme() {
        val isDarkMode = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = if (isDarkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
//        setTheme(colorScheme.theme)
        Log.d(tag, "scheme=$isDarkMode")
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val isDarkMode = prefs.getBoolean(Settings.darkMode, true)
        val newScheme = if (isDarkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
        if (newScheme != colorScheme)
            recreate()
        else if (!schemeSet)
            applyColors()
    }

    override fun applyColors() {
        val textColor = colorScheme.getColor(this, Field.HF_TEXT)
        val primaryColor = colorScheme.getColor(this, Field.HF_BG)
        toolbar.setBackgroundColor(primaryColor)
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon = colorScheme.getDrawable(this, Field.HF_APP_BAR_BACK)
        greeting.setTextColor(textColor)
        donate_plea.setTextColor(textColor)
        donate1.setTextColor(colorScheme.getColor(this, Field.HF_BUTTON_TEXT))
        donate1.background.setTint(colorScheme.getColor(this, Field.HF_BUTTON_BG))
        repeat_tutorial.setTextColor(textColor)
        rate_us.setTextColor(textColor)
        send_feedback.setTextColor(textColor)
        about.setTextColor(textColor)
        mScreenMain.setBackgroundColor(primaryColor)
    }

}
