/*
 *     Copyright (C) 2024 Akane Foundation
 *
 *     Gramophone is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Gramophone is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ghhccghk.musicplay.ui.preference

import android.R
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.util.Tools.allowDiskAccessInStrictMode
import com.ghhccghk.musicplay.util.Tools.dpToPx
import com.ghhccghk.musicplay.util.Tools.enableEdgeToEdgePaddingListener
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * BasePreferenceFragment:
 *   A base fragment for all SettingsTopFragment. It
 * is used to make overlapping color easier.
 *
 * @author AkaneTan
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(MaterialColors.getColor(view, R.attr.colorBackground))
        view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view).apply {
            setPadding(paddingLeft, paddingTop + 12.dpToPx(context), paddingRight, paddingBottom)
            enableEdgeToEdgePaddingListener()
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is CustomSingleSelectPreference) {
            val entries = preference.entries
            val entryValues = preference.entryValues

            // 获取当前选中项的值
            val selected = preference.getPersistedValue()
            // 找到当前选中项的索引，没有则-1
            val checkedItem = entryValues.indexOf(selected)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(preference.title)
                .setSingleChoiceItems(entries, checkedItem) { dialog, which ->
                    // 选中时，直接保存并关闭对话框
                    preference.setSelectedValue(entryValues[which])
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()

        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    override fun setPreferencesFromResource(preferencesResId: Int, key: String?) {
        allowDiskAccessInStrictMode { super.setPreferencesFromResource(preferencesResId, key) }
    }

    override fun setDivider(divider: Drawable?) {
        super.setDivider(Color.TRANSPARENT.toDrawable())
    }

    override fun setDividerHeight(height: Int) {
        super.setDividerHeight(0)
    }

    fun startActivity(target: Class<*>) {
        startActivity(Intent(requireActivity(), target))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
    }

    override fun onStart() {
        super.onStart()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

}