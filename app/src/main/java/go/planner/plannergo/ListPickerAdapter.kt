package go.planner.plannergo

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

class ListPickerAdapter(data: ArrayList<String>, private val c: ListActivity,
                        mRecyclerView: RecyclerView): ListActivityAdapter(data, c, mRecyclerView) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_list_activity_item, parent, false) as LinearLayout

        val colorScheme = c.getColorScheme()

        val holder = createViewHolder(colorScheme, parent, view)

        holder.edit.visibility = View.GONE
        holder.remove.visibility = View.GONE

        view.setOnClickListener {
            val intent = Intent()
            intent.putExtra("item_no", holder.adapterPosition)
            intent.extras!!.putAll(c.intent.extras)
            c.setResult(MainActivity.RC_PICK_FILE, intent)
            c.finish()
        }

        return holder
    }
}