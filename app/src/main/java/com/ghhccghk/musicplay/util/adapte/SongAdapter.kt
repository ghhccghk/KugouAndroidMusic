package com.ghhccghk.musicplay.util.adapte

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.util.adapte.SongAdapter.SongAdapterHolder

class SongAdapter (
    private val items: List<Song>,
    private val onItemClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapterHolder>() {

    class SongAdapterHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.song_img_cover)
        val song_title: TextView = view.findViewById(R.id.song_title)
        val song_singer: TextView = view.findViewById(R.id.song_singer)
        val song_album: TextView = view.findViewById(R.id.song_album)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapterHolder {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return SongAdapterHolder(tv)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: SongAdapterHolder, position: Int) {
        val item = items[position]
        holder.song_title.text = item.name
        holder.song_singer.text = item.singerinfo[0].name
        holder.song_album.text = item.albuminfo.name
        holder.itemView.setOnClickListener {
                onItemClick?.invoke(item)
        }
        val secureUrl = item.trans_param.union_cover.replaceFirst("http://", "https://")
        // 使用 Glide 加载图片
        Glide.with(holder.itemView)
            .load(secureUrl)
            .into(holder.cover)
    }
}