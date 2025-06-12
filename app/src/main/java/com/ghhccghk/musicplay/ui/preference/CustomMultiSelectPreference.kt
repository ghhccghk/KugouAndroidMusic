package com.ghhccghk.musicplay.ui.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.ghhccghk.musicplay.R

class CustomSingleSelectPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : DialogPreference(context, attrs) {

    private var _selectedValue: String? = null

    var entries: Array<String> = emptyArray()
    var entryValues: Array<String> = emptyArray()

    init {
        summaryProvider = SummaryProvider<DialogPreference> {
            _selectedValue ?: "未选择"
        }
        // 解析 XML 中的 android:entries 和 android:entryValues
        val a = context.obtainStyledAttributes(attrs, R.styleable.CustomSingleSelectPreference)
        val entriesResId = a.getResourceId(
            R.styleable.CustomSingleSelectPreference_android_entries,
            0
        )
        val entryValuesResId = a.getResourceId(
            R.styleable.CustomSingleSelectPreference_android_entryValues,
            0
        )
        a.recycle()

        if (entriesResId != 0) {
            entries = context.resources.getStringArray(entriesResId)
        }

        if (entryValuesResId != 0) {
            entryValues = context.resources.getStringArray(entryValuesResId)
        }
    }

    fun setSelectedValue(value: String?) {
        _selectedValue = entries[entryValues.indexOf(value)]
        persistString(value)
        notifyChanged()
    }

    fun getPersistedValue(): String? {
        return getPersistedString(null)
    }

    // 你可以重写 onSetInitialValue 恢复数据
    override fun onSetInitialValue(defaultValue: Any?) {
        val value = getPersistedString(defaultValue as? String)
        _selectedValue = entries[entryValues.indexOf(value)]
    }
}
