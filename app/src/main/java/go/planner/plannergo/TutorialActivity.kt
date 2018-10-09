package go.planner.plannergo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_tutorial2.*
import java.io.File

const val TAG = "TutorialActivity2"

class TutorialActivity : AppCompatActivity(), ColorSchemeActivity {

    private lateinit var mP: MediaPlayer
    private lateinit var touchHelper: ItemTouchHelper
    private var stage = 0

    private lateinit var colorScheme: ColorScheme

    override fun onCreate(savedInstanceState: Bundle?) {
        setColorScheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)

        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        fab.setOnClickListener { initStage1() }

        addTextView(R.string.tut_text_01)
        addTextView(R.string.tut_text_02)
    }

    private fun initStage1() {
        resetViews()

        fab.setOnClickListener(null)

        setSupportActionBar(toolbar)

        touchHelper = feedRecyclerView(recycler_view, makeArray(), ItemTouchHelper.RIGHT)
        recycler_view.visibility = View.VISIBLE

        addTextView(R.string.tut_text_03)
        addTextView(R.string.tut_text_04)
        addTextView(R.string.tut_text_05)
    }

    private fun feedRecyclerView(recyclerView: RecyclerView, assignments: ArrayList<Assignment>, direction: Int): ItemTouchHelper {
        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0f)
        recyclerView.layoutParams = params

        val adapter = TutorialAssignmentAdapter(assignments, this)
        recyclerView.adapter = adapter

        recyclerView.isNestedScrollingEnabled = false

        val swipeCallback = object : SwipeCallback(this) {
            var direction = 0

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                this.direction = direction
                return ItemTouchHelper.Callback.makeMovementFlags(0, direction)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
                assert(viewHolder != null)



                if (direction == ItemTouchHelper.RIGHT) {
                    adapter.notifyItemChanged(viewHolder!!.adapterPosition)
                    initStage2()
                } else {
                    adapter.notifyItemRemoved(viewHolder!!.adapterPosition)
                    initStage4()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
        return itemTouchHelper
    }

    fun initStage2() {
        resetViews()

        recycler_view.visibility = View.GONE

        initNavDrawer()

        addTextView(R.string.tut_text_06)
        addTextView(R.string.tut_text_07)
        addTextView(R.string.tut_text_08)
    }

    private fun initNavDrawer() {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        val drawerOptions = resources.getStringArray(R.array.drawer_options_array)
        val tArray = resources.obtainTypedArray(R.array.drawer_icons_array)
        val count = tArray.length()
        val drawerIcons = IntArray(count)
        for (i in drawerIcons.indices) {
            drawerIcons[i] = tArray.getResourceId(i, 0)
        }
        //Recycles the TypedArray, to be re-used by a later caller.
        //After calling this function you must not ever touch the typed array again.
        tArray.recycle()


        drawer_list.adapter = DrawerAdapter(this, drawerOptions, drawerIcons, 1)


        drawer_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                2 -> {
                    initStage3()
                }
            }
            drawer_layout.closeDrawers()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (stage == 2) {
            drawer_layout.openDrawer(Gravity.START)
            return true
        }
        if (stage == 4 && item?.itemId == R.id.action_open_types) {
            initStage5()
        }
        if (stage == 6 && item?.itemId == android.R.id.home) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initStage3() {
        resetViews()

        recycler_view.visibility = View.VISIBLE

        drawer_list.adapter = null
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        toolbar.setTitle(R.string.header_completed)
        toolbar.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.nav_color_2_bright))

        touchHelper.attachToRecyclerView(null)
        touchHelper = feedRecyclerView(recycler_view, makeArray(), ItemTouchHelper.LEFT)

        addTextView(R.string.tut_text_09)
        addTextView(R.string.tut_text_10)
    }

    private fun initStage4() {
        resetViews()

        recycler_view.visibility = View.GONE

        //resets options menu (top right); becomes usable for this step
        invalidateOptionsMenu()

        addTextView(R.string.tut_text_11)
        addTextView(R.string.tut_text_12)
        addTextView(R.string.tut_text_13)

        val b = Button(this)
        b.setText(R.string.got_it)
        b.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        b.setPadding(16, 16, 16, 16)
        b.setTextColor(ContextCompat.getColor(this, R.color.textBlack))
        b.visibility = View.GONE
        b.setOnClickListener {
            run {
                resetViews()
                stage--
                addTextView(R.string.tut_text_14)
                addTextView(R.string.tut_text_15)
                addTextView(R.string.tut_text_16)
            }
        }
        linear_layout.addView(b)
        views.add(b)

        Log.i(TAG, "millis= $millis")

        b.postDelayed({
            b.visibility = View.VISIBLE

        }, millis - 3400)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (stage == 4) {
            menuInflater.inflate(R.menu.main_menu, menu); true
        } else super.onCreateOptionsMenu(menu)
    }

    private fun initStage5() {
        resetViews()

        invalidateOptionsMenu()

        toolbar.setTitle(R.string.categories)
        toolbar.setTitleTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar.navigationIcon?.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))

        fab.setOnClickListener { _ ->

            val body = layoutInflater.inflate(
                    R.layout.dialog_tutorial_list_add,
                    findViewById(android.R.id.content),
                    false) as ConstraintLayout

            val dialog = AlertDialog.Builder(this, R.style.DarkDialogTheme)
                    .setTitle(R.string.add_category)
                    .setView(body)
                    .create()
            dialog.setCanceledOnTouchOutside(false)


            val editText = body.findViewById<EditText>(R.id.editText)
            val saveButton = body.findViewById<Button>(R.id.button_save)
            saveButton.setOnClickListener {
                run {
                    val newCategory = editText.text.toString()
                    if (!FileIO.types.contains(newCategory))
                        FileIO.types.add(newCategory)
                    FileIO.writeFiles(this@TutorialActivity)
                    dialog.dismiss()
                    initStage6(editText.text.toString())
                }
            }
            val suggestion1 = body.findViewById<TextView>(R.id.priority_text)
            suggestion1.setOnClickListener(createListener(suggestion1.text.toString(), dialog))
            val suggestion2 = body.findViewById<TextView>(R.id.className)
            suggestion2.setOnClickListener(createListener(suggestion2.text.toString(), dialog))
            val suggestion3 = body.findViewById<TextView>(R.id.textView4)
            suggestion3.setOnClickListener(createListener(suggestion3.text.toString(), dialog))

            dialog.show()
        }

        addTextView(R.string.tut_text_17)
    }

    private fun createListener(text: String, dialog: AlertDialog): View.OnClickListener {
        return View.OnClickListener {
            run {
                //writes types to files
                val fileName = "planner.assignments.types"
                val file = File(filesDir, fileName)
                if (file.createNewFile()) {
                    Log.v(TAG, "$fileName created")
                }
                if (!FileIO.types.contains(text))
                    FileIO.types.add(text)
                FileIO.writeFiles(this)
                dialog.dismiss()
                initStage6(text)
            }
        }
    }

    private fun initStage6(newCategory: String) {
        resetViews()

        fab.setOnClickListener(null)


        val v = layoutInflater.inflate(
                R.layout.view_list_activity_item,
                linear_layout, false
        ) as LinearLayout
        val tV = v.findViewById<TextView>(R.id.title)
        tV.text = newCategory
        v.findViewById<ImageView>(R.id.edit).drawable.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        v.findViewById<ImageView>(R.id.remove).drawable.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))


        linear_layout.addView(v)

        addTextView(getString(R.string.tut_text_18) + newCategory + getString(R.string.tut_text_18_))
        addTextView(R.string.tut_text_19)
        addTextView(R.string.tut_text_20)
        addTextView(R.string.tut_text_21)
    }


    private fun makeArray(): ArrayList<Assignment> {
        val newAssignment = Assignment()
        newAssignment.title = getString(R.string.hw_1)
        newAssignment.className = getString(R.string.math)

        val array = ArrayList<Assignment>()
        array.add(newAssignment)
        return array
    }

    private val views = ArrayList<TextView>()

    private var millis = 500L

    private fun addTextView(id: Int) {
        addTextView(resources.getString(id))
    }

    private fun addTextView(text: String) {
        addTextView(text, millis)
        millis += 1500
    }

    private fun addTextView(text: String, delayMillis: Long) {
        val t = layoutInflater.inflate(
                R.layout.view_tutorial_text,
                linear_layout, false
        ) as TextView
        t.text = text
        linear_layout.addView(t)
        views.add(t)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val overrideDelay = if (volume > 8) delayMillis else delayMillis * 2 / 5
        t.postDelayed({
            t.visibility = View.VISIBLE
            mP = MediaPlayer.create(this, R.raw.receive_message)
            mP.start()
        }, overrideDelay)
    }

    private fun resetViews() {
        stage++
        for (i in views)
            linear_layout.removeView(i)
        millis = 500
    }

    override fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    override fun setColorScheme() {
        colorScheme = ColorScheme(true, this)
        setTheme(colorScheme.theme)
    }

    override fun checkForColorSchemeUpdate() {

    }

    override fun applyColors() {
        val navView = findViewById<NavigationView>(R.id.navigation)
        navView.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
        coordinator.setBackgroundColor(colorScheme.getColor(ColorScheme.PRIMARY))
    }
}
