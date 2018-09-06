package go.planner.plannergo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import kotlinx.android.synthetic.main.toolbar.*
import android.os.Looper
import android.support.annotation.Nullable
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import go.planner.plannergo.billing.BillingManager
import go.planner.plannergo.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED
import go.planner.plannergo.billing.BillingProvider
import go.planner.plannergo.skulist.AcquireFragment

/**
 * Stores miscellaneous options that are not settings.
 * Labeled "Help and Feedback" in the app.
 * Uses billing library methods.
 */
class FeedbackActivity : AppCompatActivity(), BillingProvider {

    // Debug tag, for logging
    private val tag = "BaseGamePlayActivity"

    private val dialogTag = "dialog"

    private lateinit var mViewController: MainViewController
    private lateinit var mBillingManager: BillingManager
    private lateinit var mAcquireFragment: AcquireFragment

    private lateinit var mScreenMain: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.iconBlack))
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        setSupportActionBar(toolbar)

        mViewController = MainViewController(this)

        mAcquireFragment = if (savedInstanceState != null)
            supportFragmentManager.findFragmentByTag(dialogTag) as AcquireFragment
        else
            AcquireFragment()

        mBillingManager = BillingManager(this, mViewController.updateListener)

        mScreenMain = findViewById(R.id.screen_main)


    }

    override fun onResume() {
        super.onResume()

        if (mBillingManager.billingClientResponseCode == BillingClient.BillingResponse.OK) {
            mBillingManager.queryPurchases()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        NavUtils.navigateUpFromSameTask(this)
        return true
    }
    override fun getBillingManager(): BillingManager {
        return mBillingManager
    }

    fun replayTutorial(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, TutorialActivity::class.java))
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
        val intent = Intent(android.content.Intent.ACTION_SENDTO)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "Planner Agenda Feedback")
        intent.data = Uri.parse("mailto:benjaminphillipsdeveloper@gmail.com")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Could not find an email app.", Toast.LENGTH_LONG).show()
        }
    }

    fun gotoAppRating(@Suppress("UNUSED_PARAMETER") view: View) {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
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

    fun getDialogFragment(): DialogFragment {
        return mAcquireFragment
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

}
