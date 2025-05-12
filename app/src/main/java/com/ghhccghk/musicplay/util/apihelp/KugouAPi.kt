package com.ghhccghk.musicplay.util.apihelp

import androidx.core.net.toUri
import okhttp3.OkHttpClient
import okhttp3.Request

object KugouAPi {
    val client = OkHttpClient()
    val apiaddress = "http:/127.0.0.1:9600"

    /** 发送验证码*/
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

    /**手机登录 */
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
    /**  用户名登录(该登录可能需要验证，不推荐使用) */
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

    /** 开放接口登录(目前仅支持微信登录) */
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

    /** 二维码登录 二维码 key 生成接口 */
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

    /** 二维码检测扫码状态接口  */
    fun getQrCodeCheck(key: String): String?{
        val url = "$apiaddress/login/qr/check?key=$key"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getQrCodeCheck failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }
    /** 二维码生成接口 */
    fun getQrCode(key: String): String? {
        val url = "$apiaddress/login/wx/create?key=$key"
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


    /** 微信二维码生成 */
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

    /** 微信二维码检查*/

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


    /** 更新 token 登录信息 */
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

    /** dfid 获取 */
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

    /** 获取用户额外信息 */
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

    /** 获取用户 vip 信息 */
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

    /** 获取用户歌单*/
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

    /** 获取用户关注 */
    fun getUserFollow(): String? {
        val url = "$apiaddress/user/follow"
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

    /** 获取用户云盘音乐 */
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
    /** 获取用户云盘音乐Url */
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
    /** 获取用户收藏的视频 */
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

    /** 获取用户喜欢的视频 */
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
                println("getTopAlbum failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getAlbum(id: String): String? {
        val url = "$apiaddress/album".toUri().buildUpon().apply {
            appendQueryParameter("type",id)
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getAlbum failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getAlbumInSongs(id: String): String? {
        val url = "$apiaddress/album/songs".toUri().buildUpon().apply {
            appendQueryParameter("type",id)
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getAlbumInSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getSongsUrl(hash: String,album_id: String? = null
                    ,free_part: String? = null , album_audio_id: String? = null,
                    quality: String? = null ): String? {
        val url = "$apiaddress/song/url".toUri().buildUpon().apply {
            appendQueryParameter("hash",hash)
            album_id.let { appendQueryParameter("album_id",it) }
            free_part.let { appendQueryParameter("free_part",it) }
            album_audio_id.let { appendQueryParameter("album_audio_id",it) }
            quality.let { appendQueryParameter("quality",it) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongsUrl failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getSongsUrlNew(hash: String,free_part: String? = null ,
                       album_audio_id: String? = null): String? {
        val url = "$apiaddress/song/url/new".toUri().buildUpon().apply {
            appendQueryParameter("hash",hash)
            free_part.let { appendQueryParameter("free_part",it) }
            album_audio_id.let { appendQueryParameter("album_audio_id",it) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongsUrlNew failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getSongClimax(hash: String): String? {
        val url = "$apiaddress/song/climax".toUri().buildUpon().apply {
            appendQueryParameter("hash",hash)
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongClimax failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun searchSongs(key: String,page: Long? = null,
                    pageSize: Long? = null,type: String? = null): String? {

        val url = "$apiaddress/search".toUri().buildUpon().apply {
            appendQueryParameter("keyword",key)
            page.let { appendQueryParameter("page",it.toString()) }
            pageSize.let { appendQueryParameter("pageSize",it.toString()) }
            type.let { appendQueryParameter("type",it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("searchSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getSearchdefault(): String? {
        val url = "$apiaddress/search/default"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSearchdefault failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getSearchhot(): String? {
        val url = "$apiaddress/search/hot"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSearchhot failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getSearchSuggest(key: String,albumTipCount : String?= null,correctTipCount : String? = null,
                           mvTipCount : String? = null,musicTipCount : String? = null): String? {

        val url = "$apiaddress/search/suggest".toUri().buildUpon().apply {
            appendQueryParameter("keywords",key)
            if (albumTipCount != null){
                appendQueryParameter("albumTipCount",albumTipCount)
            }
            if (correctTipCount != null){
                appendQueryParameter("correctTipCount",correctTipCount)
            }
            if (mvTipCount != null){
                appendQueryParameter("mvTipCount",mvTipCount)
            }
            if (musicTipCount != null) {
                appendQueryParameter("musicTipCount", musicTipCount)
            }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("searchSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getSearchSongLyrics(keyword: String? = null,hash: String? = null,
                      album_audio_id: String? = null,man:String? = null): String? {

        val url = "$apiaddress/search/lyric".toUri().buildUpon().apply {
            keyword.let {
                if (hash == null) appendQueryParameter("keyword",it)
            }
            hash.let {
                if (keyword == null) appendQueryParameter("hash",it)
            }
            album_audio_id.let { appendQueryParameter("album_audio_id ",it )}
            man.let { appendQueryParameter("man ",it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSearchSongLyrics failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getSongLyrics(id: String,accesskey: String,
                      fmt: String? = null,decode: Boolean? = false): String? {

        val url = "$apiaddress/lyric".toUri().buildUpon().apply {
            appendQueryParameter("id",id)
            appendQueryParameter("accesskey",accesskey)
            fmt.let { appendQueryParameter("fmt",it.toString() )}
            appendQueryParameter("decode", decode.toString())
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSearchSongLyrics failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }
    fun getPlayListTag(): String? {
        val url = "$apiaddress/playlist/tags"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListTag failed: ${response.code}")
                return response.code.toString()
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    fun getPlayList(category_id: String, withsong: String? = null,
                    withtag: String? = null): String? {

        val url = "$apiaddress/top/playlist".toUri().buildUpon().apply {
            appendQueryParameter("category_id",category_id)
            withsong.let { appendQueryParameter("withsong ",withsong )}
            withtag.let { appendQueryParameter("withtag ",withtag )}
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayList failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    fun getPlayListTheme(): String? {
        val url = "$apiaddress/theme/playlist"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListTag failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

}