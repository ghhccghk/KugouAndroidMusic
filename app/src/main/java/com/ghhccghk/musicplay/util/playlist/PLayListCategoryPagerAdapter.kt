package com.ghhccghk.musicplay.util.playlist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ghhccghk.musicplay.data.PlayCategory
import com.ghhccghk.musicplay.ui.playlisttag.PlayTagListFragment

class PLayListCategoryPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val categories: List<PlayCategory> // 你的 parent 列表
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]
        return PlayTagListFragment.newInstance(category.son)
    }

}