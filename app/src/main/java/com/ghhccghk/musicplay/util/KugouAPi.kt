package com.ghhccghk.musicplay.util

import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.core.net.toUri

object KugouAPi {
    val client = OkHttpClient()
    val apiaddress = "http:/127.0.0.1:9600"



    fun getMobileCode(mobile: String): String? {
        val url = "$apiaddress/captcha/sent?mobile=$mobile"
        println(url)
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMobileCode failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }


    }

    fun loginCellphone(mobile: String, code: String): String? {
        val url = "$apiaddress/login/cellphone?phone=$mobile&code=$code"
        val request = Request.Builder().url(url).build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("loginCellphone failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun loginUserNameAndPassword(username: String, password: String): String?{
        val url = "$apiaddress/login?username=$username&password=$password"
        val request = Request.Builder().url(url).build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("loginUserNameAndPassword failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun loginOpenPlat(code: String): String?{
        val url = "$apiaddress/login/openplat?$code"
        val request = Request.Builder().url(url).build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("loginOpenPlat failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getQrCode(): String?{
        val url = "$apiaddress/login/qr/key"
        val request = Request.Builder().url(url).build()


        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getQrCode failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getQrCodeCheck(key: String): String?{
        val url = "$apiaddress/login/qr/check?key=$key"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("loginCellphone failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun loginWeChatQr(): String? {
        val url = "$apiaddress/login/wx/create"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("loginWeChatQr failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getWechatCheck(timestamp: String): String?{
        val url = "$apiaddress/login/wx/check?timestamp=$timestamp"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getWechatCheck failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun updateToken(token: String): String?{
        val url = "$apiaddress/login/token?token$token"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("updatetoken failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getDfid(): String?{
        val url = "$apiaddress/register/dev"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getDfid failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getUserDetail(): String?{
        val url = "$apiaddress/user/detail"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUesrDetail failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserVip(): String?{
        val url = "$apiaddress/user/vip/detail"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUesrDetail failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserPlayList(page: Int? = null, pageSize: Int? = null): String?{
        val url = "$apiaddress/user/playlist".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUesrDetail failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


    fun getUserCloudMusic(page: Int? = null, pageSize: Int? = null): String?{
        val url = "$apiaddress/user/cloud".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUesrCloudMusic failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserCloudMusicUrl(hash: String, album_id: String? = null,
                             name: String? = null,album_audio_id: String? = null): String?{
        val url = "$apiaddress/user/cloud/url?hash=$hash".toUri().buildUpon().apply {
            album_id?.let { appendQueryParameter("album_id", it.toString())}
            album_audio_id?.let { appendQueryParameter("album_audio_id", it.toString())}
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserCloudMusicUrl failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserVideoCollect(page: Int? = null, pageSize: Int? = null): String?{
        val url = "$apiaddress/video/collect".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserVideoCollect failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserVideoLove(pageSize: Int? = null): String?{
        val url = "$apiaddress/video/love".toUri().buildUpon().apply {
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserVideoLove failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getUserListen(type: Int? = null): String? {
        val url = "$apiaddress/user/listen".toUri().buildUpon().apply {
            type?.let { appendQueryParameter("type", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserListen failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getUserHistory(bp: String? = null): String? {
        val url = "$apiaddress/user/history".toUri().buildUpon().apply {
            bp?.let { appendQueryParameter("vp", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserHistory failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


    fun getUserLastMusic(pageSize: Int? = null): String? {
        val url = "$apiaddress/user/history".toUri().buildUpon().apply {
            pageSize?.let { appendQueryParameter("pagesize", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getUserLastMusic failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun setUserPlayList(name: String,list_create_userid: String,
                        list_create_listid: String,is_pri:Int? = null,
                        type: Int? = null,list_create_gid: String? = null,): String? {

        val url = "$apiaddress/playlist/add".toUri().buildUpon().apply {
            appendQueryParameter("name", name)
            appendQueryParameter("list_create_userid", list_create_userid)
            appendQueryParameter("list_create_listid", list_create_listid)
            if (type == 0){
                is_pri?.let { appendQueryParameter("is_pri", it.toString()) }
            }
            type?.let { appendQueryParameter("type", it.toString()) }
            list_create_gid?.let { appendQueryParameter("list_create_gid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("setUserPlayList failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun delUserPlayList(listid: String,fileids: String): String? {

        val url = "$apiaddress/playlist/tracks/del".toUri().buildUpon().apply {
            appendQueryParameter("listid", listid)
            appendQueryParameter("fileids", fileids)
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("setUserPlayList failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getTopAlbum(type: Int? = null,pageSize: Int? = null,page: Int? = null): String? {
        val url = "$apiaddress/top/album".toUri().buildUpon().apply {
            type.let { appendQueryParameter("type", it.toString()) }
            pageSize.let { appendQueryParameter("pageSize", it.toString()) }
            page.let { appendQueryParameter("page", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("setUserPlayList failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


}