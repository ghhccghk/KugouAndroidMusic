package com.ghhccghk.musicplay.util.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.SongLists
import com.ghhccghk.musicplay.data.PlayListTag
import com.ghhccghk.musicplay.util.search.SearchResultItemAdapter.SearchResultItemAdapterHolder

class SearchResultItemAdapter(private val items: List<SongLists>,
                                  private val onItemClick: ((PlayListTag) -> Unit)? = null
) :
    RecyclerView.Adapter<SearchResultItemAdapterHolder>() {

    class SearchResultItemAdapterHolder(view: View) : RecyclerView.ViewHolder(view) {
        val song_img_cover = view.findViewById<ImageView>(R.id.song_img_cover)
        val song_title = view.findViewById<TextView>(R.id.song_title)
        val song_singer = view.findViewById<TextView>(R.id.song_singer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemAdapterHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return SearchResultItemAdapterHolder(view)


    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchResultItemAdapterHolder, position: Int) {
        val item = items[position]
        holder.song_singer.text = item.SingerName
        holder.song_title.text = item.SongName
        // 使用 HTTPS URL
        val secureUrl = item.Image?.replaceFirst("http://", "https://")?.replaceFirst("/{size}/","/")
        // 使用 Glide 加载图片
        Glide.with(holder.itemView)
            .load(secureUrl)
            .into(holder.song_img_cover)


        holder.itemView.setOnClickListener { onItemClick }
    }
}