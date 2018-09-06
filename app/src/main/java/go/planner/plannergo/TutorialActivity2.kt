package go.planner.plannergo

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
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

const val TAG = "TutorialActivity2"

class TutorialActivity2 : AppCompatActivity() {

    private lateinit var mP: MediaPlayer
    private lateinit var touchHelper: ItemTouchHelper
    private var stage = 0

    //TODO: set strings as resources/make method with resource parameter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)

        fab.setOnClickListener { initStage1() }

        addTextView("The plus button in the bottom right corner creates assignments.")
        addTextView("Tap it to create an mAssignment.")
    }

    private fun initStage1() {
        resetViews()

        fab.setOnClickListener(null)

        setSupportActionBar(toolbar)

        touchHelper = feedRecyclerView(recycler_view, makeArray(), ItemTouchHelper.RIGHT)
        recycler_view.visibility = View.VISIBLE

        addTextView("Here's your first mAssignment (We've filled it out for you)")
        addTextView("The title is in bold, and the due date is on the right.")
        addTextView("Swipe it to the right to mark it as done.")
    }

    private fun feedRecyclerView(recyclerView: RecyclerView, assignments: ArrayList<NewAssignment>, direction: Int): ItemTouchHelper {
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

        addTextView("All right!")
        addTextView("Note the words at the top of your screen. They mean you'll find your incomplete assignments here.")
        addTextView("Now tap the icon in the top left to open the menu drawer, and select 'Completed'")
    }

    private fun initNavDrawer() {
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

        toolbar.title = "Completed"
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.p1_completed))
        window.statusBarColor = ContextCompat.getColor(this, R.color.p1_completed_dark)

        touchHelper.attachToRecyclerView(null)
        touchHelper = feedRecyclerView(recycler_view, makeArray(), ItemTouchHelper.LEFT)

        addTextView("Checked off assignments show up here.")
        addTextView("We've had enough of this mAssignment--\ndelete it by swiping it to the left.")
    }

    private fun initStage4() {
        resetViews()

        recycler_view.visibility = View.GONE

        //resets options menu (top right); becomes usable for this step
        invalidateOptionsMenu()

        addTextView("Great")
        addTextView("Now note there's three little dots in the top")
        addTextView("They open a menu primarily for changing the organization of the assignments")

        val b = Button(this)
        b.text = "Got it"
        b.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        b.setPadding(16, 16, 16, 16)
        b.visibility = View.GONE
        b.setOnClickListener {
            run {
                resetViews()
                stage--
                addTextView("Right now we're going to use it for something else:")
                addTextView("We're going to add a category you can use to organize your assignments.")
                addTextView("Tap the 3 dots at the top right and select 'Edit categories'")
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

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)
        toolbar.title = "Categories"
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.textBlack))
        window.statusBarColor = ContextCompat.getColor(this, R.color.textBlack)

        fab.setOnClickListener {

            val body = layoutInflater.inflate(
                    R.layout.dialog_tutorial_list_add,
                    findViewById(android.R.id.content),
                    false) as ConstraintLayout


            val dialog = AlertDialog.Builder(this)
                    .setTitle("Add a new category! ")
                    .setView(body)
                    .create()
            dialog.setCanceledOnTouchOutside(false)


            val editText = body.findViewById<EditText>(R.id.editText)
            val saveButton = body.findViewById<Button>(R.id.button_save)
            saveButton.setOnClickListener {
                run {
                    FileIO.types.add(editText.text.toString())
                    FileIO.writeFiles(this)
                    dialog.dismiss()
                    initStage6(editText.text.toString())
                }
            }
            val suggestion1 = body.findViewById<TextView>(R.id.priority_text)
            suggestion1.setOnClickListener(createListener(suggestion1.text.toString(), dialog))
            val suggestion2 = body.findViewById<TextView>(R.id.textView3)
            suggestion2.setOnClickListener(createListener(suggestion2.text.toString(), dialog))
            val suggestion3 = body.findViewById<TextView>(R.id.textView4)
            suggestion3.setOnClickListener(createListener(suggestion3.text.toString(), dialog))

            dialog.show()
        }

        addTextView("Press the plus button to make a new category")
    }

    private fun createListener(text: String, dialog: AlertDialog): View.OnClickListener {
        return View.OnClickListener {
            run {
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
                R.layout.list_activity_item,
                linear_layout, false
        ) as LinearLayout
        val tV = v.findViewById<TextView>(R.id.textView)
        tV.text = newCategory

        linear_layout.addView(v)

        addTextView("Now, when you make a new mAssignment, you can select \"$newCategory\" as the category")
        addTextView("Well that's it for this tutorial!")
        addTextView("Press the back arrow at the top left to end the tutorial")
    }


    private fun makeArray(): ArrayList<NewAssignment> {
        val newAssignment = NewAssignment()
        newAssignment.title = "Homework #1"
        newAssignment.className = "Math"

        val array = ArrayList<NewAssignment>()
        array.add(newAssignment)
        return array
    }

    private val views = ArrayList<TextView>()

    private var millis = 500L

    private fun addTextView(id: Int){
        addTextView(resources.getString(id))
    }

    private fun addTextView(text: String){
        addTextView(text, millis)
        millis += 1500
    }

    private fun addTextView(text: String, delayMillis: Long) {
        val t = layoutInflater.inflate(
                R.layout.tutorial_text_view,
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
}
