package go.planner.plannergo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.toolbar.*

/**
 * For displaying and editing lists.
 */
abstract class ListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        initToolbar()

        setSupportActionBar(toolbar)

        val data = getData()

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
