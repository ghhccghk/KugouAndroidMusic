package com.ghhccghk.musicplay.data.libraries

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String?, // Uri 转为字符串保存
    val mediaId: String? = "",
    val title: String? = "",
    val writer: String? = "",
    val compilation: String? = "",
    val composer: String? = "",
    val artists: String? = "",
    val album: String?  = "",
    val albumArtists: String?  = "",
    val thumb: String?, // Uri -> String
    val trackNumber: Int? = 0,
    val discNumber: Int? = 0,
    val genre: String? = "",
    val recordingDay: Int? = 0,
    val recordingMonth: Int? = 0,
    val recordingYear: Int? = 0,
    val releaseYear: Int? = 0,
    val artistId: Long? = 0,
    val albumId: Long? = 0,
    val genreId: Long? = 0,
    val author: String? = "",
    val addDate: Long? = 0,
    val duration: Long = 0,
    val modifiedDate: Long? = 0,
    val cdTrackNumber: Int? = 0,
    val songHash: String?  = "",
    val lrcId: String?  = "",
    val lrcAccesskey: String?  = "",
    val songtitle: String? = "",
)
