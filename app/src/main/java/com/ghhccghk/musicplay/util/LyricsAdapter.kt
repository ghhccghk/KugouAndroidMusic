package com.ghhccghk.musicplay.util

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.LyricLine

class LyricsAdapter(private val lyrics: List<LyricLine>) :
    RecyclerView.Adapter<LyricsAdapter.LyricViewHolder>() {

    var highlightedPosition = -1
        set(value) {
            val old = field
            field = value
            notifyItemChanged(old)
            notifyItemChanged(value)
        }

    inner class LyricViewHolder(val textView: TextView) :
        RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric_line, parent, false) as TextView
        return LyricViewHolder(textView)
    }

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        val item = lyrics[position]
        holder.textView.text = item.text
        holder.textView.setTextColor(
            if (position == highlightedPosition)
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.black)
        )
    }

    override fun getItemCount(): Int = lyrics.size
}
