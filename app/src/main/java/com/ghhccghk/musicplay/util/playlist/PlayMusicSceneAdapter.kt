package com.ghhccghk.musicplay.util.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.ThemeMusicScene

class PlayMusicSceneAdapter (private val items: List<ThemeMusicScene>,
                             private val onItemClick: ((ThemeMusicScene) -> Unit)? = null) :
    RecyclerView.Adapter<PlayMusicSceneAdapter.MusicSceneViewHolder>() {

    class MusicSceneViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCover: ImageView = view.findViewById(R.id.musicplaylist_image_cover)
        val textTitle: TextView = view.findViewById(R.id.musicplaylist_text_title)
        val textIntro: TextView = view.findViewById(R.id.musicplaylist_text_intro)
        val textPlayCount: TextView = view.findViewById(R.id.musicplaylist_text_play_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicSceneViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_musicplaylist_scene, parent, false)
        return MusicSceneViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicSceneViewHolder, position: Int) {
        val item = items[position]
        holder.textTitle.text = item.title
        holder.textIntro.text = item.intro
        holder.textPlayCount.text = "播放量：${item.play_count}"

        // 点击动画 + 回调
        holder.itemView.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    onItemClick?.invoke(item)
                }
                .start()
        }

        val secureUrl = item.pic?.replaceFirst("http://", "https://")
        // 使用 Glide 加载图片
        Glide.with(holder.itemView)
            .load(secureUrl)
            .into(holder.imageCover)

    }

    override fun getItemCount(): Int = items.size
}