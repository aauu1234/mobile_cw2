package org.tensorflow.lite.examples.classification.plantList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.child_item.view.*
import kotlinx.android.synthetic.main.custom_info_contents.view.*
import org.tensorflow.lite.examples.classification.Map
import org.tensorflow.lite.examples.classification.PlantInfoDialog
import org.tensorflow.lite.examples.classification.R

private val TAG: String = Map::class.java.simpleName

class ChildAdapter(private var childList: List<ChildItem>,private val fragmentManager: FragmentManager) :
    RecyclerView.Adapter<ChildAdapter.ChildViewHolder>() {

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo: ImageView = itemView.findViewById(R.id.childLogoIv)
        val title: TextView = itemView.findViewById(R.id.childTitleTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.child_item, parent, false)

        return ChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.logo.setImageBitmap(childList[position].logo)
    //    holder.logo.setImageResource(childList[position].logo)
        holder.title.text = childList[position].title
        holder.itemView.setOnClickListener(View.OnClickListener{
            //  Toast.makeText(requireContext(),"Failed get current location", Toast.LENGTH_SHORT)
            Log.d(TAG,"sad"+holder.title.text)
            val bundle= Bundle()
            bundle.putString("plantName", holder.title.text as String)
            var dialog= PlantInfoDialog()
            dialog.arguments=bundle

             dialog.show(fragmentManager.beginTransaction(),"plantInfoDialog")
        })
    }


    override fun getItemCount(): Int {
        return childList.size
    }

    fun setFilteredList(childList: List<ChildItem>){

        this.childList = childList
        notifyDataSetChanged()
    }


}