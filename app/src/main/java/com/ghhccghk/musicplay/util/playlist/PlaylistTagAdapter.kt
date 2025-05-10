package com.ghhccghk.musicplay.util.playlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.PlayListTag

class PlaylistTagAdapter(
    private val items: List<PlayListTag>,
    private val onItemClick: ((PlayListTag) -> Unit)? = null
) :
    RecyclerView.Adapter<PlaylistTagAdapter.PlaylistTagViewHolder>() {

    class PlaylistTagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tab = view.findViewById<Button>(R.id.playlist_tab_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistTagViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_tag, parent, false)
        return PlaylistTagViewHolder(view)


    }
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PlaylistTagViewHolder, position: Int) {
        val item = items[position]

        holder.tab.text = item.tag_name

        holder.tab.setOnClickListener { onItemClick }
    }

}
