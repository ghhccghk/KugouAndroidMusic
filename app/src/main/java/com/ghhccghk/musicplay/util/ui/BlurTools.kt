/*
 * StatusBarLyric
 * Copyright (C) 2021-2022 fkj@fkj233.cn
 * https://github.com/Block-Network/StatusBarLyric
 *
 * This software is free opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as
 * published by Block-Network contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/Block-Network/StatusBarLyric/blob/main/LICENSE>.
 */

package com.ghhccghk.musicplay.util.ui

import android.view.View

object BlurTools {
    private val setBackgroundBlur by lazy {
        View::class.java.getDeclaredMethod(
            "setBackgroundBlur",
            Integer.TYPE,
            FloatArray::class.java,
            Array<IntArray>::class.java
        )
    }

    fun View.setBackgroundBlur(
        blurRadius: Int,
        cornerRadius: FloatArray,
        blendModes: Array<IntArray>
    ) {
        setBackgroundBlur.invoke(this, blurRadius, cornerRadius, blendModes)
    }

    fun cornerRadius(radius: Float) = floatArrayOf(radius, radius, radius, radius)

}
