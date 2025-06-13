package com.ghhccghk.musicplay.util.others

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.libraries.addDate
import com.ghhccghk.musicplay.data.libraries.album
import com.ghhccghk.musicplay.data.libraries.albumArtists
import com.ghhccghk.musicplay.data.libraries.albumId
import com.ghhccghk.musicplay.data.libraries.artistId
import com.ghhccghk.musicplay.data.libraries.artistsName
import com.ghhccghk.musicplay.data.libraries.author
import com.ghhccghk.musicplay.data.libraries.cdTrackNumber
import com.ghhccghk.musicplay.data.libraries.compilation
import com.ghhccghk.musicplay.data.libraries.composer
import com.ghhccghk.musicplay.data.libraries.discNumber
import com.ghhccghk.musicplay.data.libraries.duration
import com.ghhccghk.musicplay.data.libraries.genre
import com.ghhccghk.musicplay.data.libraries.genreId
import com.ghhccghk.musicplay.data.libraries.lrcAccesskey
import com.ghhccghk.musicplay.data.libraries.lrcId
import com.ghhccghk.musicplay.data.libraries.modifiedDate
import com.ghhccghk.musicplay.data.libraries.recordingDay
import com.ghhccghk.musicplay.data.libraries.recordingMonth
import com.ghhccghk.musicplay.data.libraries.recordingYear
import com.ghhccghk.musicplay.data.libraries.releaseYear
import com.ghhccghk.musicplay.data.libraries.songHash
import com.ghhccghk.musicplay.data.libraries.songtitle
import com.ghhccghk.musicplay.data.libraries.thumb
import com.ghhccghk.musicplay.data.libraries.title
import com.ghhccghk.musicplay.data.libraries.trackNumber
import com.ghhccghk.musicplay.data.libraries.uri
import com.ghhccghk.musicplay.data.libraries.writer

fun MediaItem.toEntity(): MediaItemEntity {
    return MediaItemEntity(
        uri = this.uri?.toString(),
        mediaId = this.mediaId,
        title = this.title,
        writer = this.writer,
        compilation = this.compilation,
        composer = this.composer,
        artists = this.artistsName,
        album = this.album,
        albumArtists = this.albumArtists,
        thumb = this.thumb?.toString(),
        trackNumber = this.trackNumber,
        discNumber = this.discNumber,
        genre = this.genre,
        recordingDay = this.recordingDay,
        recordingMonth = this.recordingMonth,
        recordingYear = this.recordingYear,
        releaseYear = this.releaseYear,
        artistId = this.artistId,
        albumId = this.albumId,
        genreId = this.genreId,
        author = this.author,
        addDate = this.addDate,
        duration = this.duration,
        modifiedDate = this.modifiedDate,
        cdTrackNumber = this.cdTrackNumber,
        songHash = this.songHash?.toString(),
        lrcId = this.lrcId,
        lrcAccesskey = this.lrcAccesskey,
        songtitle = this.songtitle

    )
}


fun MediaItemEntity.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setWriter(writer)
        .setCompilation(compilation)
        .setComposer(composer)
        .setArtist(artists)
        .setAlbumTitle(album)
        .setAlbumArtist(albumArtists)
        .setArtworkUri(thumb?.let { Uri.parse(it) })
        .setTrackNumber(trackNumber ?: 0)
        .setDiscNumber(discNumber ?: 0)
        .setGenre(genre)
        .setRecordingDay(recordingDay ?: 0)
        .setRecordingMonth(recordingMonth ?: 0)
        .setRecordingYear(recordingYear ?: 0)
        .setReleaseYear(releaseYear ?: 0)
        .setExtras(Bundle().apply {
            putLong("ArtistId", artistId ?: -1)
            putLong("AlbumId", albumId ?: -1)
            putLong("GenreId", genreId ?: -1)
            putString("Author", author)
            putLong("AddDate", addDate ?: 0)
            putLong("Duration", duration)
            putLong("ModifiedDate", modifiedDate ?: 0)
            putInt("CdTrackNumber", cdTrackNumber ?: 0)
            songHash?.toIntOrNull()?.let { putInt("songHash", it) }
            putString("lrcId", lrcId)
            putString("lrcAccesskey", lrcAccesskey)
            putString("songtitle",songtitle)
        })
        .build()

    val builder = MediaItem.Builder()
        .setMediaMetadata(metadata)
        .setMediaId(mediaId ?: "")
    uri?.let {
        builder.setUri(it.toUri())
    }

    return builder.build()
}

