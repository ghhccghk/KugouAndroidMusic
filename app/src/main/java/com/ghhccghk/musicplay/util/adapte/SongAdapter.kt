package com.ghhccghk.musicplay.util.adapte

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.user.playListDetail.songlist.Song
import com.ghhccghk.musicplay.util.adapte.SongAdapter.SongAdapterHolder
import kotlin.text.isNotBlank

class SongAdapter (
    private val items: List<Song>?,
    private val onItemClick: ((Song) -> Unit)? = null
) : RecyclerView.Adapter<SongAdapterHolder>() {

    class SongAdapterHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cover: ImageView = view.findViewById(R.id.song_img_cover)
        val song_title: TextView = view.findViewById(R.id.song_title)
        val song_singer: TextView = view.findViewById(R.id.song_singer)
        val song_album: TextView = view.findViewById(R.id.song_album)
        val song_button: ImageButton = view.findViewById(R.id.song_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapterHolder {
        val tv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return SongAdapterHolder(tv)
    }

    override fun getItemCount() = items?.size?: 0

    override fun onBindViewHolder(holder: SongAdapterHolder, position: Int) {
        val item = items?.get(position)
        val shield = item?.shield

        if (shield == 0 ){
            holder.song_title.text = item?.name
            holder.song_singer.visibility = View.GONE
            holder.song_album.text = item?.albuminfo?.name?.takeIf { it.isNotBlank() } ?: "未知专辑"

            holder.itemView.setOnClickListener {
                item?.let { p1 -> onItemClick?.invoke(p1) }
            }
            val secureUrl = item?.trans_param?.union_cover?.replaceFirst("http://", "https://")
                ?.replaceFirst("/{size}/", "/400/")
            // 使用 Glide 加载图片
            Glide.with(holder.itemView)
                .load(secureUrl)
                .into(holder.cover)
        } else {
            holder.song_album.visibility = View.GONE
            holder.song_title.visibility = View.GONE
            holder.cover.visibility = View.GONE
            holder.song_singer.visibility = View.GONE
            holder.song_button.visibility = View.GONE
            holder.itemView.visibility = View.GONE
        }
    }
}