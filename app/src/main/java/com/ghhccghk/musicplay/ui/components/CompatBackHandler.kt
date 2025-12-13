package com.ghhccghk.musicplay.ui.components

class UniBackEvent(
    val touchX: Float,
    val touchY: Float,
    val progress: Float,
    val swipeEdge: SwipeEdge
) {
    enum class SwipeEdge(val value: Int) {
        LEFT(0),
        RIGHT(1)
    }

    override fun toString(): String {
        return "BackEventCompat{touchX=$touchX, touchY=$touchY, progress=$progress, " +
                "swipeEdge=$swipeEdge}"
    }
}


