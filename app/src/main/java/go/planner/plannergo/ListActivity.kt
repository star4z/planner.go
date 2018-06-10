package go.planner.plannergo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import kotlinx.android.synthetic.main.toolbar.*

/**
 * For displaying and editing lists.
 */
abstract class ListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager



    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initToolbar()

        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.iconBlack))
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back_white_24dp)

        setSupportActionBar(toolbar)

        FileIO.readAssignmentsFromFile(this)

        val data = getData()
        Log.v("ListActivity", "data=$data")

        viewManager = LinearLayoutManager(this)
        viewAdapter = ListActivityAdapter(data)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    abstract fun initToolbar()

    abstract fun getData():Bag<String>
}
