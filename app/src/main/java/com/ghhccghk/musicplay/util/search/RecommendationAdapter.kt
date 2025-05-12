package com.ghhccghk.musicplay.util.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.RecordData

class RecommendationAdapter(
    private val items: List<RecordData>,
    private val onItemClick: ((RecordData) -> Unit)? = null
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationAdapterHolder>() {

    class RecommendationAdapterHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hot: TextView = view.findViewById(R.id.item_search_hot)
        val name: TextView = view.findViewById(R.id.item_search_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationAdapterHolder {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search, parent, false)
        return RecommendationAdapterHolder(tv)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecommendationAdapterHolder, position: Int) {
        holder.name.text = items[position].HintInfo
        holder.hot.setText("热度：${items[position].Hot}")
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(items[position])
        }
    }
}
