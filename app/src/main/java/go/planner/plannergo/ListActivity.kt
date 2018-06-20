package go.planner.plannergo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.toolbar.*

/**
 * For displaying and editing lists.
 */
abstract class ListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var data: Bag<String>


    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initToolbar()

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.iconBlack))
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)

        setSupportActionBar(toolbar)

        FileIO.readFiles(this)

        data = getData()
        Log.v("ListActivity", "data=$data")

        recyclerView = findViewById(R.id.recycler_view)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ListActivityAdapter(data, this, recyclerView)

        recyclerView.apply {

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    abstract fun initToolbar()

    abstract fun getData(): Bag<String>

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_menu_1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.empty_trash -> {
                AlertDialog.Builder(this)
                        .setTitle("Are you sure you want to delete all items?")
                        .setMessage("You will not be able to undo this action.")
                        .setPositiveButton("Yes", { _, _ ->
                            run {
                                val size = data.size()
                                data.clear()
                                FileIO.writeFiles(this)
                                viewAdapter.notifyItemRangeChanged(0, size)
                            }
                        })
                        .setNegativeButton("No", null)
                        .show()
                true
            }
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                true
            }
            else -> {
                consume { return super.onOptionsItemSelected(item) }
            }
        }
    }

    abstract fun onEdit(oldString: String, newString: String)

    fun addNew(view: View) {
        val editText = layoutInflater.inflate(
                R.layout.view_edit_text_list_item,
                findViewById(android.R.id.content),
                false) as EditText
        AlertDialog.Builder(this)
                .setTitle("Add new item:")
                .setView(editText)
                .setPositiveButton("Save", { _: DialogInterface, _: Int ->
                    run {
                        if (!data.contains(editText.text.toString())) {
                            val pos = data.add(editText.text.toString())
                            FileIO.writeFiles(this)
                            viewAdapter.notifyItemInserted(pos)
                        } else {
                            val text = "You can't have duplicates!"
                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show()
    }

    private inline fun consume(f: () -> Unit): Boolean {
        f()
        return true
    }
}
