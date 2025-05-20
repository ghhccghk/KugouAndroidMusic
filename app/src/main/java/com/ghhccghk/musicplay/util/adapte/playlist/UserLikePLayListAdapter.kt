package com.ghhccghk.musicplay.util.adapte.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.ThemeMusicScene
import com.ghhccghk.musicplay.data.user.likeplaylist.Info
import com.ghhccghk.musicplay.util.adapte.playlist.UserLikePLayListAdapter.UserLikePLayListHolder

class UserLikePLayListAdapter (private val items: List<Info>,
                               private val onItemClick: ((Info) -> Unit)? = null) :
    RecyclerView.Adapter<UserLikePLayListHolder>() {

    class  UserLikePLayListHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageCover: ImageView = view.findViewById(R.id.musicplaylist_image_cover)
        val textTitle: TextView = view.findViewById(R.id.musicplaylist_text_title)
        val textIntro: TextView = view.findViewById(R.id.musicplaylist_text_intro)
        val textPlayCount: TextView = view.findViewById(R.id.musicplaylist_text_play_count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLikePLayListHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_musicplaylist_scene, parent, false)
        return  UserLikePLayListHolder(view)
    }

    override fun onBindViewHolder(holder: UserLikePLayListHolder, position: Int) {
        val item = items[position]
        holder.textTitle.text = item.name
        holder.textIntro.visibility = View.GONE
        holder.textPlayCount.visibility = View.GONE

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

        if (item.name != "我喜欢"){
            val secureUrl =
                item.pic.replaceFirst("http://", "https://").replaceFirst("/{size}/", "/")
            // 使用 Glide 加载图片
            Glide.with(holder.itemView)
                .load(secureUrl)
                .into(holder.imageCover)
        } else {
            holder.imageCover.setImageBitmap(holder.itemView.context.getDrawable(R.drawable.ic_favorite_filled)?.toBitmap())

        }

    }

    override fun getItemCount(): Int = items.size


    }