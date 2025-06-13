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

package com.ghhccghk.musicplay.ui.setting

import android.os.Bundle
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.ui.preference.BasePreferenceFragment

class AudioSettingsActivity : BaseSettingsActivity(R.string.settings_audio,
    { AudioSettingsFragment() })

class AudioSettingsFragment : BasePreferenceFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "play_setting_prefs"
        setPreferencesFromResource(R.xml.settings_audio, rootKey)
    }
}
