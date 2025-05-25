package com.ghhccghk.musicplay.util.others

import android.os.Bundle
import androidx.media3.common.MediaItem
import com.ghhccghk.musicplay.data.libraries.MediaItemEntity
import com.ghhccghk.musicplay.data.libraries.*
import android.net.Uri
import androidx.media3.common.MediaMetadata
import androidx.core.net.toUri

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
        lrcAccesskey = this.lrcAccesskey
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

