package com.ghhccghk.musicplay.util.hotsearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.KeywordGroup
import com.ghhccghk.musicplay.data.KeywordItem

class HotKeywordAdapter(
    private val items: List<KeywordItem>,
    private val onItemClick: ((KeywordItem) -> Unit)? = null
) : RecyclerView.Adapter<HotKeywordAdapter.KeywordViewHolder>() {

    class KeywordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeyword: TextView = view.findViewById(R.id.tv_keyword)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeywordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keyword, parent, false)
        return KeywordViewHolder(view)
    }

    override fun onBindViewHolder(holder: KeywordViewHolder, position: Int) {
        val item = items[position]
        holder.tvKeyword.text = item.keyword

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
    }

    override fun getItemCount(): Int = items.size
}


class HotGroupAdapter(private val groups: List<KeywordGroup>, private val onItemClick: (KeywordItem) -> Unit) :
    RecyclerView.Adapter<HotGroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupName: TextView = view.findViewById(R.id.tv_group_name)
        val rvKeywords: RecyclerView = view.findViewById(R.id.rv_keywords)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_card, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupName.text = group.name
        holder.rvKeywords.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.rvKeywords.adapter = HotKeywordAdapter(group.keywords, onItemClick)
    }

    override fun getItemCount(): Int = groups.size

}

