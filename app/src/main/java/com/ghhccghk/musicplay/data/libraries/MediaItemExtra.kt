package com.ghhccghk.musicplay.data.libraries

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.compose.runtime.Stable
import androidx.media3.common.MediaItem

const val defaultArtistsName = "Unknown Artist"
val defaultArtists = listOf(defaultArtistsName)
const val defaultTitle = "Unknown Work"
const val defaultAlbum = "Unknown Album"

val MediaItem.uri: Uri?
    get() = this.localConfiguration?.uri

val MediaItem.title: String?
    get() = this.mediaMetadata.title?.toString()

val MediaItem.writer: String?
    get() = this.mediaMetadata.writer?.toString()

val MediaItem.compilation: String?
    get() = this.mediaMetadata.compilation?.toString()

val MediaItem.composer: String?
    get() = this.mediaMetadata.composer?.toString()

val MediaItem.artists: List<String>?
    get() = this.mediaMetadata.artist?.toString()?.toMultipleArtists()

val MediaItem.artistsName: String?
    get() = this.mediaMetadata.artist?.toString()?.toMultipleArtists()?.toArtistsString()

val MediaItem.album: String?
    get() = this.mediaMetadata.albumTitle?.toString()

val MediaItem.albumArtists: String?
    get() = this.mediaMetadata.albumArtist?.toString()

val MediaItem.thumb: Uri?
    get() = this.mediaMetadata.artworkUri

val MediaItem.trackNumber: Int?
    get() = this.mediaMetadata.trackNumber

val MediaItem.discNumber: Int?
    get() = this.mediaMetadata.discNumber

val MediaItem.genre: String?
    get() = this.mediaMetadata.genre?.toString()

val MediaItem.recordingDay: Int?
    get() = this.mediaMetadata.recordingDay

val MediaItem.recordingMonth: Int?
    get() = this.mediaMetadata.recordingMonth

val MediaItem.recordingYear: Int?
    get() = this.mediaMetadata.recordingYear

val MediaItem.releaseYear: Int?
    get() = this.mediaMetadata.releaseYear

val MediaItem.extras: Bundle?
    get() = this.mediaMetadata.extras

val MediaItem.artistId: Long?
    get() = this.mediaMetadata.extras?.getLong("ArtistId")

val MediaItem.albumId: Long?
    get() = this.mediaMetadata.extras?.getLong("AlbumId")

val MediaItem.genreId: Long?
    get() = this.mediaMetadata.extras?.getLong("GenreId")

val MediaItem.author: String?
    get() = this.mediaMetadata.extras?.getString("Author")

val MediaItem.addDate: Long?
    get() = this.mediaMetadata.extras?.getLong("AddDate")

val MediaItem.duration: Long
    get() = this.mediaMetadata.extras?.getLong("Duration") ?: 0

val MediaItem.modifiedDate: Long?
    get() = this.mediaMetadata.extras?.getLong("ModifiedDate")

val MediaItem.cdTrackNumber: Int?
    get() = this.mediaMetadata.extras?.getInt("CdTrackNumber")

val MediaItem.songHash: Int?
    get() = this.mediaMetadata.extras?.getInt("songHash")

val MediaItem.lrcId: String?
    get() = this.mediaMetadata.extras?.getString("lrcId")

val MediaItem.lrcAccesskey: String?
    get() = this.mediaMetadata.extras?.getString("lrcAccesskey")

val MediaItem.songtitle: String?
    get() = this.mediaMetadata.extras?.getString("songtitle")




/*val MediaItem.samplingRate: Int
    get() = this.mediaMetadata.extras?.getInt("SamplingRate")?:0*/

/*
val MediaItem.bitrate: Int
    get() = this.mediaMetadata.extras?.getInt("Bitrate")?:0
*/

fun String.toMultipleArtists(): List<String> {
    val delimiters = listOf("、", "/", "&", ";", "；", ",")
    var mostFrequentDelimiter: String? = null
    var maxCount = 0

    for (delimiter in delimiters) {
        val count = this.split(delimiter).size - 1
        if (count > maxCount) {
            maxCount = count
            mostFrequentDelimiter = delimiter
        }
    }

    return mostFrequentDelimiter?.let { delimiter ->
        this.split(delimiter).map { it.trim() }
    } ?: listOf(this.trim())
}

fun List<String>.toArtistsString(): String {
    return this.joinToString("、")
}

@SuppressLint("ParcelCreator")
@Stable
abstract class YosMediaItem(
    val uri: Uri?,
    val mediaId: String?,
    val mimeType: String?,
    val title: String?,
    val writer: String?,
    val compilation: String?,
    val composer: String?,
    val artists: String?,
    val album: String?,
    val albumArtists: String?,
    val thumb: Uri?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val genre: String?,
    val recordingDay: Int?,
    val recordingMonth: Int?,
    val recordingYear: Int?,
    val releaseYear: Int?,
    val artistId: Long?,
    val albumId: Long?,
    val genreId: Long?,
    val author: String?,
    val addDate: Long?,
    val duration: Long,
    val modifiedDate: Long?,
    val cdTrackNumber: Int?,
    val songHash: String?,
    val lrcId: String?,
    var lrcAccesskey: String?


    //val samplingRate: Int,
    //val bitrate: Int
) : Parcelable
