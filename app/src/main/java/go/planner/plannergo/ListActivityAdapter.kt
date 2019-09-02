package go.planner.plannergo

import android.app.AlertDialog
import android.content.Context
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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_list.*


class ListActivityAdapter internal constructor(private val data: ArrayList<String>, private val c: ListActivity, private val mRecyclerView: RecyclerView)
    : RecyclerView.Adapter<ListActivityAdapter.ViewHolder>() {

    private val notifyEmptyTextView = TextView(c)

    /**
     * Provides reference points for the views in a view_list_activity_item
     */
    class ViewHolder(val v: View) : RecyclerView.ViewHolder(v) {

        val textView: TextView = v.findViewById(R.id.title)
        val edit: ImageView = v.findViewById(R.id.edit)
        val remove: ImageView = v.findViewById(R.id.remove)

    }


    /**
     * Called when the RecyclerView needs a new ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //inflate layout
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_list_activity_item, parent, false) as LinearLayout

        val colorScheme = c.getColorScheme()

        val bg = c.getDrawable(R.drawable.bg_list_item)
        bg.setTint(colorScheme.getColor(ColorScheme.ASSIGNMENT_VIEW_BG))
        view.background = bg

        //create ViewHolder from layout and attach OnClickListeners
        val holder = ViewHolder(view)
        holder.textView.setTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        holder.edit.drawable.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))
        holder.remove.drawable.setTint(colorScheme.getColor(ColorScheme.TEXT_COLOR))

        holder.edit.setOnClickListener {
            val editText = c.layoutInflater.inflate(
                    R.layout.dialog_edit_text,
                    c.findViewById(android.R.id.content) as ViewGroup,
                    false) as EditText
            editText.text = SpannableStringBuilder(data[holder.adapterPosition])
            editText.setTextColor(colorScheme.getColor(ColorScheme.TEXT_COLOR))
            editText.setHintTextColor(colorScheme.getColor(ColorScheme.SUB_TEXT_COLOR))


            val imm = c.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val builder = AlertDialog.Builder(c, if (colorScheme == ColorScheme.SCHEME_DARK)
                R.style.DarkDialogTheme
            else
                R.style.LightDialogTheme)
                    .setTitle(R.string.edit)
                    .setView(editText)
                    .setPositiveButton(R.string.save) { _, _ ->
                        run {
                            updateData(view, editText.text.toString())
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
                        }
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        run {
                            imm.hideSoftInputFromWindow(editText.windowToken, 0)
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
        if (data.size == 0) {
            c.recycler_view_label.setText(R.string.empty_list)
        } else {
            c.recycler_view_label.text = ""
        }
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

        notifyItemChanged(oldPos)
        Log.v("ListActivity", "data=$data")
    }
}
