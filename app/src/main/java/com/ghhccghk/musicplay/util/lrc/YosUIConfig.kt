package com.ghhccghk.musicplay.util.lrc

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

/**
 * YosLyricView UI 控制类
 *
 * 该类仅管理在日常使用中不经常调节的选项，如果是 翻译开关、模糊效果 的开关请直接在调用 YosLyricView() 时修改
 *
 * @param edgeFade 是否启用 YosLyricView 的边缘渐隐效果
 * @param formatText 是否启用 YosLyricView 的歌词规整功能。启用后会将歌词的格式规整。例如，将歌词中的多个连续空格替换为单个空格，去除歌词首尾空格等
 * @param blankHeight YosLyricView 列表首尾的填充块高度，单位 dp
 * @param mainTextSize 主要文本的大小，单位 sp
 * @param subTextSize 次要文本的大小，单位 sp，若不填写将根据主要文本的大小自动计算
 * @param mainTextBasicColor 主要文本的底色
 * @param subTextBasicColor 次要文本的底色
 * @param normalMainTextAlpha 当该行主要文本不在高亮状态下的文本透明度
 * @param normalSubTextAlpha 当该行次要文本不在高亮状态下的文本透明度
 * @param currentMainTextAlpha 当该行主要文本高亮时的文本透明度
 * @param currentSubTextAlpha 当该行次要文本高亮时的文本透明度
 */
@Stable
data class YosUIConfig(
    val edgeFade: Boolean = false,
    val formatText: Boolean = false,
    val noLrcText: String = "No lyrics",
    val blankHeight: Int = 40,
    val mainTextSize: Int = 34,
    val subTextSize: Int = mainTextSize - 18,
    val mainTextBasicColor: () -> Color = { Color(0xFFF2F2F2) },
    val subTextBasicColor: () -> Color = { Color(0xFF919191) },
    val normalMainTextAlpha: Float = 0.6f,
    val normalSubTextAlpha: Float = 0.6f,
    val currentMainTextAlpha: Float = 1f,
    val currentSubTextAlpha: Float = 1f
)
