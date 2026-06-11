package androidx.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.ghhccghk.musicplay.R

open class DecibelSeekBarPreference(
    context: Context, attrs: AttributeSet?
) : SeekBarPreference(context, attrs) {
    override fun updateLabelValue(value: Int) {
        (textViewField.get(this) as TextView?)?.text = getText(value)
    }

    private val textViewField by lazy {
        SeekBarPreference::class.java.getDeclaredField("mSeekBarValueTextView").apply {
            isAccessible = true
        }
    }

    protected open fun getText(value: Int): String {
        return context.getString(R.string.d_db, value)
    }
}

class Minus15SeekBarPreference(
    context: Context, attrs: AttributeSet?
) : DecibelSeekBarPreference(context, attrs) {
    override fun getText(value: Int): String {
        return super.getText(value - 15)
    }
}