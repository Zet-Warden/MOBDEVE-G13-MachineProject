package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import android.R.attr.left
import android.R.attr.right
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mobdeve.s11s13.group13.mp.vaccineph.R
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.HomeScreenRvAdapter.*


class HomeScreenRvAdapter(private val dataSet: MutableList<HomeFeedData>) :
    RecyclerView.Adapter<ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFeedTitle: TextView = view.findViewById(R.id.tvFeedTitle)
        val tvFeedInfo: TextView = view.findViewById(R.id.tvFeedInfo)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.template_home_feed, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val (title, info) = dataSet[position]
        viewHolder.tvFeedTitle.text = title
        viewHolder.tvFeedInfo.text = info
    }

    override fun getItemCount() = dataSet.size
}