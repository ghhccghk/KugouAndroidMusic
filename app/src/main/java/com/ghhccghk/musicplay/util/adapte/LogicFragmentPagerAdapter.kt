package com.ghhccghk.musicplay.util.adapte

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LogicFragmentPagerAdapter(
    fragment: Fragment,
    private val fragmentList: List<Fragment>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]
}