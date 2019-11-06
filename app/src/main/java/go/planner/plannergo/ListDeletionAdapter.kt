package go.planner.plannergo

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ListDeletionAdapter(c: ListActivity, data: ArrayList<String>, mRecyclerView: RecyclerView) :
        ListActivityAdapter(data, c, mRecyclerView) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = super.onCreateViewHolder(parent, viewType)
        holder.edit.visibility = View.GONE
        return holder
    }
}