package go.planner.plannergo

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import java.util.*

class ListActivityAdapter internal constructor(private val data: ArrayList<String>) : RecyclerView.Adapter<ListActivityAdapter.ViewHolder>() {

    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val textView : TextView
        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener { Log.d("ListActivityAdapter", "Element $adapterPosition clicked.") }
            textView = v.findViewById(R.id.textView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_activity_item, parent, false) as LinearLayout
        return ViewHolder(view)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]
    }

    override fun getItemCount(): Int {
        return 0
    }
}
