package org.tensorflow.lite.examples.classification.plantList

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.examples.classification.R

class ParentAdapter(private var parentList: List<ParentItem>,private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<ParentAdapter.ParentViewHolder>() {

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logoIv: ImageView = itemView.findViewById(R.id.parentLogoIv)
        val titleTv: TextView = itemView.findViewById(R.id.parentTitleTv)
        val childRecyclerView: RecyclerView = itemView.findViewById(R.id.langRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parent_item, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val parentItem = parentList[position]
        holder.logoIv.setImageResource(parentItem.logo)
        holder.titleTv.text = parentItem.title

        // Set the background color based on the position
        // Set the background color based on the parent item's title
        val backgroundColor = when (parentItem.title) {
            "Eatable Plants" -> Color.parseColor("#55C65A")
            "Neutral And Non-Eatable Plants" -> Color.parseColor("#ECDA3E")
            "Toxic Plants" -> Color.parseColor("#D63B30")
            "Extreme Toxic Plants" -> Color.parseColor("#800080") // Purple
            else -> Color.WHITE
        }
        holder.childRecyclerView.setBackgroundColor(backgroundColor)

        holder.childRecyclerView.setHasFixedSize(true)
        holder.childRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 3)
        val adapter = ChildAdapter(parentItem.mList,fragmentManager)
        holder.childRecyclerView.adapter = adapter
    }

    override fun getItemCount(): Int {
        return parentList.size
    }

    fun setFilteredList(parentList: List<ParentItem>){
        this.parentList = parentList
        notifyDataSetChanged()
    }

}