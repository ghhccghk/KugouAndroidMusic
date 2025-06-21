package com.ghhccghk.musicplay.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ghhccghk.musicplay.R

class ContributorsSettingsActivity : BaseSettingsActivity(
    R.string.settings_contributors, { ContributorsFragment() })

class ContributorsFragment : BaseFragment(null) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(requireActivity()).also {
            it.text = requireContext().getString(R.string.settings_contributors_long)
        }
    }
}