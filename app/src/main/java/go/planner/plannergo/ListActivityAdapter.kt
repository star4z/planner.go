package go.planner.plannergo

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class ListActivityAdapter internal constructor(private val data: ArrayList<String>, private val c: ListActivity, private val mRecyclerView: RecyclerView)
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

            val imm = c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val builder = AlertDialog.Builder(c)
                    .setTitle("Edit")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        run {
                            updateData(view, editText.text.toString())
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        run {
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        }
                    }
                    //Not used since couldn't get it to work
                    .setOnCancelListener {
                        run {
                            editText.postDelayed({
                                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                            }, 1)
                        }
                    }
            val dialog = builder.create()

            //disables ability to cancel dialog with method other than cancel button. I don't like it,
            //but I don't see an alternative
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
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
        return data.size
    }

    /**
     * Changes the value of an item in the recyclerView
     * @param view      used to find index of the piece of data in the recyclerView before the change
     * @param newStr    contains the data which will be replacing the old data
     */
    private fun updateData(view: View, newStr: String) {
        val oldPos = mRecyclerView.getChildLayoutPosition(view)
        val oldStr = data[oldPos]
        c.onEdit(oldStr, newStr)
        data[oldPos] = newStr

//        Log.v("ListActivityAdapter", "$oldPos to $newPos")
        notifyItemChanged(oldPos)
//        when {
//            newPos == oldPos -> notifyItemChanged(newPos)
//            newPos > oldPos -> notifyItemRangeChanged(oldPos, newPos)
//            else -> notifyItemRangeChanged(newPos, oldPos)
//        }
//        notifyDataSetChanged()
        Log.v("ListActivity", "data=$data")
    }
}
