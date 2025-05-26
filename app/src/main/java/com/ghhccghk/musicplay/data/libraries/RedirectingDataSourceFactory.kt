package com.ghhccghk.musicplay.data.libraries

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.common.util.Log
import androidx.media3.datasource.TransferListener

@UnstableApi
class RedirectingDataSourceFactory(
    private val defaultFactory: DataSource.Factory
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        return RedirectingDataSource(defaultFactory.createDataSource())
    }
}

@UnstableApi
class RedirectingDataSource(
    private val actualDataSource: DataSource
) : DataSource {

    private var currentUri: Uri? = null
    private var id: String? = null

    override fun addTransferListener(transferListener: TransferListener) {

    }

    override fun open(dataSpec: DataSpec): Long {

        if (dataSpec.uri.scheme == "musicplay" && dataSpec.uri.host == "playurl") {
            val cid = dataSpec.uri.getQueryParameter("id")?: "0"
            val url = dataSpec.uri.getQueryParameter("url")?: ""
            id = cid
            currentUri = url.toUri()
        } else {
            currentUri = dataSpec.uri
        }
        Log.d("RedirectingDataSource", "Redirecting URI: ${dataSpec.uri} to $currentUri")

        val redirectedSpec = dataSpec.buildUpon().setUri(currentUri!!).setKey(id!!).build()
        return actualDataSource.open(redirectedSpec)
    }

    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        return actualDataSource.read(buffer, offset, readLength)
    }

    override fun getUri(): Uri? = currentUri

    override fun close() {
        actualDataSource.close()
    }
}
