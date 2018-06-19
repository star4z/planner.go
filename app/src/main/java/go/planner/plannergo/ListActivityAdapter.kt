package go.planner.plannergo

import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class ListActivityAdapter internal constructor(private val data: Bag<String>, private val c: ListActivity, private val mRecyclerView: RecyclerView)
    : RecyclerView.Adapter<ListActivityAdapter.ViewHolder>() {

    /**
     * Provides reference points for the views in a list_activity_item
     */
    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
        val textView: TextView = v.findViewById(R.id.textView)
        val edit: ImageView = v.findViewById(R.id.edit)
        val remove: ImageView = v.findViewById(R.id.remove)
    }

    /**
     * Called when the RecyclerView needs a new ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //inflate layout
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_activity_item, parent, false) as LinearLayout

        //create ViewHolder from layout and attach OnClickListeners
        val holder = ViewHolder(view)
        holder.edit.setOnClickListener {
            val editText = c.layoutInflater.inflate(
                    R.layout.view_edit_text_list_item,
                    c.findViewById(android.R.id.content) as ViewGroup,
                    false) as EditText
            editText.text = SpannableStringBuilder(data[holder.adapterPosition])
            AlertDialog.Builder(c)
                    .setTitle("Edit")
                    .setView(editText)
                    .setPositiveButton("Save", { _, _ ->
                        run {
                            updateData(view, editText.text.toString()
                            )
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show()

        }
        holder.remove.setOnClickListener {
            data.remove(holder.textView.text as String)
            notifyItemRemoved(holder.adapterPosition)
            FileIO.writeFiles(c)
        }
        return holder
    }

    /**
     * Called when the RecyclerView adds or replaces the data in a ViewHolder
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = data[position]
    }

    /**
     * Returns number of items in the data set
     * Determines the length of the array
     */
    override fun getItemCount(): Int {
        return data.size()
    }

    /**
     * Changes the value of an item in the recyclerView
     * @param view      used to find index of the piece of data in the recyclerView before the change
     * @param newStr    contains the data which will be replacing the old data
     */
    private fun updateData(view: View, newStr: String) {
        val oldPos = mRecyclerView.getChildLayoutPosition(view)
        val oldStr = data[oldPos]
        val newPos = data.replace(oldStr, newStr)
        c.onEdit(oldStr, newStr)
        Log.v("ListActivityAdapter", "$oldPos to $newPos")
        if (newPos >= oldPos)
            notifyItemRangeChanged(oldPos, newPos)
        else
            notifyItemRangeChanged(newPos, oldPos)

        Log.v("ListActivity", "data=$data")
    }
}
