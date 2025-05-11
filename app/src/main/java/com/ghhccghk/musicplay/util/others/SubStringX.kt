package com.ghhccghk.musicplay.util.others

fun String.subStringX(beforeString: String?, afterString: String?): String? {
    val startIndex =
        beforeString?.let { this.indexOf(it) }?.takeIf { it != -1 }?.plus(beforeString.length) ?: 0
    val endIndex =
        afterString?.let { this.indexOf(it, startIndex) }?.takeIf { it != -1 } ?: this.length
    return if (startIndex <= endIndex) this.substring(startIndex, endIndex) else null
}