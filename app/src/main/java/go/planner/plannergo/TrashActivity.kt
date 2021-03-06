package go.planner.plannergo

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.api.services.drive.DriveScopes
import go.planner.plannergo.planner_billing.BillingActivity
import kotlinx.android.synthetic.main.activity_trash.*

/**
 * Displays deleted assignments. Does not do any sorting in order to handle larger quantities of
 * assignments than would be expected in MainActivity, for example.
 */
class TrashActivity : BillingActivity(), ColorSchemeActivity, SignInActivity {
    init {
        tag = "TrashActivity"
    }

    private lateinit var prefs: SharedPreferences
    private lateinit var colorScheme: ColorScheme
    private var schemeSet = false

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerAdapter: DrawerAdapter
    private lateinit var mDrawerList: ListView

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var account: GoogleSignInAccount? = null

    private val signInRC = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trash)

        setSupportActionBar(toolbar)

        FileIO.readFiles(this)

        val gso: GoogleSignInOptions? = Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso!!)
    }

    override fun onStart() {
        super.onStart()

        account = GoogleSignIn.getLastSignedInAccount(this)
        Log.i(tag, "account=$account")
    }

    override fun onResume() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        checkForColorSchemeUpdate()

        invalidateOptionsMenu()
        setUpNavDrawer()

        updateUI(account)

        FileIO.readFiles(this)

        loadPanels()

        toolbar.setTitle(R.string.trash)

        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            signInRC -> {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
        }
    }

    override fun setColorScheme() {
        val darkMode = prefs.getBoolean(Settings.darkMode, true)
        colorScheme = if (darkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
//        setTheme(colorScheme.theme)
        Log.d(tag, "scheme=$darkMode")
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun checkForColorSchemeUpdate() {
        val darkMode = prefs.getBoolean(Settings.darkMode, true)
        val newScheme = if (darkMode) ColorScheme.SCHEME_DARK else ColorScheme.SCHEME_LIGHT
        if (newScheme != colorScheme)
            recreate()
        else if (!schemeSet)
            applyColors()
    }

    override fun applyColors() {
        val navView = findViewById<NavigationView>(R.id.navigation)
        navView.setBackgroundColor(colorScheme.getColor(this, Field.DW_BG))
        val coordinatorLayout = findViewById<CoordinatorLayout>(R.id.coordinator)
        coordinatorLayout.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_BG))
        toolbar.setTitleTextColor(colorScheme.getColor(this, Field.TR_APP_BAR_TEXT))
        toolbar.navigationIcon = colorScheme.getDrawable(this, Field.TR_APP_BAR_HAM)
        toolbar.setBackgroundColor(colorScheme.getColor(this, Field.TR_APP_BAR_BG))
        toolbar.overflowIcon = colorScheme.getDrawable(this, Field.TR_APP_BAR_OPT)
        schemeSet = true
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        Log.d(tag, "" + account)
        this.account = account
        mDrawerAdapter.account = account
        mDrawerList.adapter = mDrawerAdapter
    }

    private fun setUpNavDrawer() {
        val drawerOptions = resources.getStringArray(R.array.drawer_options_array)
        val tArray = resources.obtainTypedArray(R.array.drawer_icons_array)
        val count = tArray.length()
        val drawerIcons = IntArray(count)
        for (i in drawerIcons.indices) {
            drawerIcons[i] = tArray.getResourceId(i, 0)
        }
        tArray.recycle()

        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerList = findViewById(R.id.drawer_list)

        mDrawerAdapter = DrawerAdapter(this, drawerOptions, drawerIcons, 3)
        mDrawerList.adapter = mDrawerAdapter


        mDrawerList.onItemClickListener = AdapterView.OnItemClickListener { _, view, position, _ ->
            when (position) {
                DrawerAdapter.iInProgress -> {
                    mDrawerAdapter.setSelectedPos(1)
                    mDrawerList.adapter = mDrawerAdapter
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", true)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                DrawerAdapter.iCompleted -> {
                    mDrawerAdapter.setSelectedPos(2)
                    mDrawerList.adapter = mDrawerAdapter
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("mode_InProgress", false)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    finish()
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
                DrawerAdapter.iTrash -> loadPanels()
                DrawerAdapter.iSettings -> startActivity(Intent(this@TrashActivity,
                        SettingsActivity::class.java))
                DrawerAdapter.iFeedback -> startActivity(Intent(this@TrashActivity,
                        FeedbackActivity::class.java))
                DrawerAdapter.iRateAndReview -> gotoAppRating()
                DrawerAdapter.iDonate -> openDonationDialog(view)
            }
            mDrawerLayout.closeDrawers()
        }
    }


    override fun signIn(view: View?) {
        Log.v(tag, "Signing in...")
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, signInRC)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            Log.w(tag, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this, R.string.login_error, Toast.LENGTH_LONG).show()
            updateUI(null)
        }
    }


    override fun signOut(view: View?) {
        Log.d(tag, "Signing out")
        mGoogleSignInClient!!.signOut().addOnCompleteListener {
            account = null
            updateUI(null)
        }
    }

    private fun gotoAppRating() {
        val uri: Uri? = Uri.parse("market://details?id=" + this.packageName)
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
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)))
        }
    }

    private fun loadPanels() {
        body.removeAllViews()
        Log.v(tag, FileIO.deletedAssignments.toString())

        addHeading(R.string.deletion_is_permanent)
        if (FileIO.deletedAssignments.isEmpty())
            addHeading(R.string.trash_is_empty)
        else {
            for (assignment: Assignment in FileIO.deletedAssignments) {
                val nextView = layoutInflater.inflate(
                        R.layout.view_deleted_item,
                        findViewById(android.R.id.content),
                        false
                )

                nextView.setBackgroundColor(colorScheme.getColor(this, Field.MAIN_CARD_BG))

                val textColor = colorScheme.getColor(this, Field.MAIN_CARD_TEXT)

                val titleView = nextView.findViewById<TextView>(R.id.title)
                titleView.text = assignment.title
                titleView.setTextColor(textColor)
                val className = nextView.findViewById<TextView>(R.id.className)
                className.text = assignment.className
                className.setTextColor(textColor)
                nextView.findViewById<ImageView>(R.id.restore).drawable.setTint(textColor)
                nextView.findViewById<ImageView>(R.id.delete).drawable.setTint(textColor)


                nextView.findViewById<ImageView>(R.id.restore).setOnClickListener {
                    FileIO.deletedAssignments.remove(assignment)
                    FileIO.addAssignment(assignment)
                    FileIO.writeFiles(this)
                    loadPanels()
                }

                nextView.findViewById<ImageView>(R.id.delete).setOnClickListener {
                    val title = if (assignment.title == "") getString(R.string.mThis) else "'${assignment.title}'"
                    AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
                        R.style.DarkDialogTheme
                    else
                        R.style.LightDialogTheme)
                            .setTitle(R.string.perm_delete_check)
                            .setMessage("$title ${getString(R.string.gone_forever)}")
                            .setPositiveButton(R.string.delete) { _, _ ->
                                run {
                                    FileIO.deletedAssignments.remove(assignment)
                                    FileIO.writeFiles(this)
                                    loadPanels()
                                }
                            }
                            .setNegativeButton(R.string.keep, null)
                            .show()
                }
                body.addView(nextView)
            }
        }
    }

    private fun addHeading(inText: Int) {
        val header = layoutInflater.inflate(
                R.layout.view_sort_header,
                findViewById<View>(android.R.id.content) as ViewGroup,
                false
        ) as TextView
        header.setText(inText)
        header.setTextColor(colorScheme.getColor(this, Field.MAIN_HEADER))
        body.addView(header)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val icon = if (colorScheme == ColorScheme.SCHEME_DARK) {
            getDrawable(R.drawable.ic_trash_white_24dp)
        } else {
            getDrawable(R.drawable.ic_trash_black_24dp)
        }

        menu?.getItem(0)?.icon = icon
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.trash_menu, menu)
        for (i in 0 until menu!!.size()) {
            val item: MenuItem = menu.getItem(i)
            val s = SpannableString(item.title)
            s.setSpan(ForegroundColorSpan(colorScheme.getColor(this, Field.DG_HEAD_TEXT)), 0,
                    s.length, 0)
            item.title = s
        }
        return true
    }


    override fun onCreateView(parent: View?, name: String, context: Context,
                              attrs: AttributeSet): View? {
        if (name == "androidx.appcompat.view.menu.ListMenuItemView" &&
                parent?.parent is FrameLayout) {
            val view = parent.parent as View
            // change options menu bg color


            view.setBackgroundColor(colorScheme.getColor(this, Field.DG_BG))
        }
        return super.onCreateView(parent, name, context, attrs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.empty_trash -> {
                AlertDialog.Builder(this, if (colorScheme == ColorScheme.SCHEME_DARK)
                    R.style.DarkDialogTheme
                else
                    R.style.LightDialogTheme)
                        .setTitle(R.string.empty_trash_check)
                        .setMessage(R.string.no_undo)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            run {
                                FileIO.deletedAssignments.clear()
                                FileIO.writeFiles(this)
                                loadPanels()
                            }
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
