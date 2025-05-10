package com.ghhccghk.musicplay.util

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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

    inner class LyricViewHolder(
        val root : View,
        val lyric_item: TextView,
        val lyric_translation: TextView) :
        RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LyricViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lyric_line, parent, false)
        val lyric_item = textView.findViewById<TextView>(R.id.lyric_line)
        val lyric_translation = textView.findViewById<TextView>(R.id.lyric_translation)
        return LyricViewHolder(textView,lyric_item, lyric_translation)
    }

    override fun onBindViewHolder(holder: LyricViewHolder, position: Int) {
        val item = lyrics[position]
        holder.lyric_item.text = item.text
        item.translation.let{ holder.lyric_translation.text = it }
        holder.lyric_item.setTextColor(
            if (position == highlightedPosition)
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            else
                ContextCompat.getColor(holder.itemView.context, R.color.black)
        )
        holder.lyric_item.setTypeface(null,
            if (position == highlightedPosition) Typeface.BOLD else Typeface.NORMAL)

        if (item.translation != null){
            holder.lyric_translation.setTextColor(
                if (position == highlightedPosition)
                    ContextCompat.getColor(holder.itemView.context, R.color.white)
                else
                    ContextCompat.getColor(holder.itemView.context, R.color.black)
            )
        }
    }

    override fun getItemCount(): Int = lyrics.size
}
