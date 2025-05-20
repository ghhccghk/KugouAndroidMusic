package com.ghhccghk.musicplay.util.apihelp

import androidx.core.net.toUri
import com.ghhccghk.musicplay.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Request

object KugouAPi {
    val client = OkHttpClient()
    val apiaddress = "http:/127.0.0.1:9600"
    var token : String? = null
    var userid : String? = null


    fun init() {
        token = TokenManager.getToken()
        userid = TokenManager.getUserId()
    }
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
    fun getQrCodekey(): String?{
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

    /** 获取二维码  */
    fun getQrCode(key: String): String?{
        val url = "$apiaddress/login/qr/create?key=$key"
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
    fun getWxQrCode(key: String): String? {
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
    fun updateToken(token: String,userid: String): String?{
        val url = "$apiaddress/login/token?token$token&userid=$userid"
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
        val url = "$apiaddress/register/dev".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
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
        val url = "$apiaddress/user/detail".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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

    /** 获取用户 vip 信息 */
    fun getUserVip(): String?{
        val url = "$apiaddress/user/vip/detail".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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

    /** 获取用户歌单*/
    fun getUserPlayList(page: Int? = null, pageSize: Int? = null): String?{
        val url = "$apiaddress/user/playlist".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
        val url = "$apiaddress/user/follow".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            type?.let { appendQueryParameter("type", it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize", it.toString()) }
            page?.let { appendQueryParameter("page", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            album_id?.let { appendQueryParameter("album_id",it) }
            free_part?.let { appendQueryParameter("free_part",it) }
            album_audio_id?.let { appendQueryParameter("album_audio_id",it) }
            quality?.let { appendQueryParameter("quality",it) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            free_part?.let { appendQueryParameter("free_part",it) }
            album_audio_id?.let { appendQueryParameter("album_audio_id",it) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
            page?.let { appendQueryParameter("page",it.toString()) }
            pageSize?.let { appendQueryParameter("pageSize",it.toString()) }
            type?.let { appendQueryParameter("type",it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
        val url = "$apiaddress/search/default".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
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
        val url = "$apiaddress/search/hot".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
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
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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


    /** 歌词搜索
     * 说明: 调用此接口, 可以搜索歌词，该接口需配合 /lyric 使用。
     *
     * 必选参数：
     *
     * @param keyword: 关键词，与 hash 二选一
     *
     * @param hash: 歌曲 hash，与 keyword 二选一
     *
     * 可选参数：
     *
     * @param album_audio_id: 专辑音乐 id,
     *
     * @param man: 是否返回多个歌词，yes：返回多个， no：返回一个。 默认为no
     * */
    fun getSearchSongLyrics(keyword: String? = null,hash: String? = null,
                      album_audio_id: String? = null,man:String? = null): String? {

        val url = "$apiaddress/search/lyric".toUri().buildUpon().apply {
            keyword?.let { appendQueryParameter("keyword",it) }
            hash?.let { appendQueryParameter("hash",it) }
            album_audio_id?.let { appendQueryParameter("album_audio_id ",it )}
            man?.let { appendQueryParameter("man ",it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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

    /** 获取歌词
     * 说明 : 调用此接口，可以获取歌词，调用该接口前则需要调用/search/lyric 获取完整参数
     *
     * 必选参数：
     *
     * @param id: 歌词 id, 可以从 getSearchSongLyrics 接口中获取
     *
     * @param accesskey: 歌词 accesskey, 可以从getSearchSongLyrics接口中获取
     *
     * 可选参数：
     *
     * @param fmt: 歌词类型，lrc 为普通歌词，krc 为逐字歌词
     *
     * @param decode: 是否解码，传入该参数这返回解码后的歌词*/
    fun getSongLyrics(id: String,accesskey: String,
                      fmt: String? = null,decode: Boolean? = false): String? {

        val url = "$apiaddress/lyric".toUri().buildUpon().apply {
            appendQueryParameter("id",id)
            appendQueryParameter("accesskey",accesskey)
            fmt?.let { appendQueryParameter("fmt",it.toString() )}
            appendQueryParameter("decode", decode.toString())
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
        val url = "$apiaddress/playlist/tags".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
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
            withsong?.let { appendQueryParameter("withsong ",withsong )}
            withtag?.let { appendQueryParameter("withtag ",withtag )}
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
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
        val url = "$apiaddress/theme/playlist".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
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


    fun getPlayListEffect(page: Int? = null, pageSize: Int? = null,): String? {
        val url = "$apiaddress/playlist/effect".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page ",it.toString() )}
            pageSize?.let { appendQueryParameter("pagesize ",it.toString() )}
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListEffect failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }
    /** 获取歌单详情 */
    fun getPlayListDetail(ids: String): String? {
        val url = "$apiaddress/playlist/detail?ids=$ids".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListDetail failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取歌单所有歌曲 */
    fun getPlayListAllSongs(ids: String,page: Int? = null, pageSize: Int? = null,): String?{
        val url = "$apiaddress/playlist/track/all".toUri().buildUpon().apply {
            appendQueryParameter("ids",ids)
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pagesize", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }

        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListAllSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }


    }

    /** 获取歌单所有歌曲 (New)*/
    fun getPlayListAllSongsNew(ids: String,page: Int? = null, pageSize: Int? = null,): String?{
        val url = "$apiaddress/playlist/track/all/new".toUri().buildUpon().apply {
            appendQueryParameter("listid",ids)
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pagesize", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }

        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListAllSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }


    }

    /** 获取相似歌单 */
    fun getPlayListSimilar(ids: String,page: Int? = null, pageSize: Int? = null,): String?{
        val url = "$apiaddress/playlist/similar".toUri().buildUpon().apply {
            appendQueryParameter("ids",ids)
            page?.let { appendQueryParameter("page", it.toString()) }
            pageSize?.let { appendQueryParameter("pagesize", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }

        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListAllSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }

    /** 获取主题歌单所有歌曲*/
    fun getPlayListThemeAllSong(theme_id: String): String? {

        val url = "$apiaddress/theme/playlist/track".toUri().buildUpon().apply {
            appendQueryParameter("theme_id",theme_id)
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }

        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getPlayListAllSongs failed: ${response.code}")
                return response.code.toString()
            }

            val responseBody = response.body?.string() ?: return null
            return responseBody
        }

    }
    /** 获取主题音乐 */
    fun getThemeMuisc(): String? {
        val url = "$apiaddress/theme/music".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getThemeMuisc failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取主题音乐详情 */
    fun getThemeMuiscDetail(id: String): String? {
        val url = "$apiaddress/theme/music/detail?id=$id".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getThemeMuiscDetail failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 歌曲推荐
     * : 1：对应安卓 精选好歌随心听 || 私人专属好歌，2：对应安卓 经典怀旧金曲，3：对应安卓 热门好歌精选，4：对应安卓 小众宝藏佳作，5：未知，6：对应 vip 专属推荐 */
    fun getSongcard(id : String = "1"): String? {
        val url = "$apiaddress/top/card".toUri().buildUpon().apply {
            appendQueryParameter("card_id", id)
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongcard failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


    /** 获取歌手和专辑图片 */
    fun getSongUserListen(hash: String,album_id : String? = null,album_audio_id: String? = null,
                          count: String? = null): String? {
        val url = "$apiaddress/user/listen".toUri().buildUpon().apply {
           appendQueryParameter("hash", hash)
            album_id?.let { appendQueryParameter("album_id", it) }
            album_audio_id?.let { appendQueryParameter("album_audio_id", it) }
            count?.let { appendQueryParameter("count", it) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()

        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongUserListen failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取音乐信息 */
    fun getMuiscInfo(hash: String): String? {
        val url = "$apiaddress/audio?hash=$hash".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取更多音乐版本
     * 必选参数：
     *
     * @param album_audio_id：音乐的 mixsongid/album_audio_id
     *
     * 可选参数：
     *
     * @param page： 页码
     *
     * @param pageSize: 每页页数, 默认为 30
     *
     * @param show_type：是否返回分类
     *
     * @param sort：排序，支持 all，hot，new
     *
     * @param  type: 分类
     *
     * show_detail：是否返回详情，否则只返回总数，0：只返回总数，不传或者其他都返回详情*/
    fun getMoreMusic(album_audio_id: String,page: Int? = null, pageSize: Int? = null,
                     show_type: String? = null,sort: String? = null,type: String? = null,): String?{
        val url = "$apiaddress/audio/related?album_audio_id=$album_audio_id".toUri().buildUpon().apply {
            page?.let { appendQueryParameter("page", it.toString())  }
            pageSize?.let { appendQueryParameter("pagesize", it.toString()) }
            show_type?.let { appendQueryParameter("show_type", it.toString()) }
            sort?.let { appendQueryParameter("sort", it.toString()) }
            type?.let { appendQueryParameter("type", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取音乐详情
     * @param hash: 歌曲 hash, 可以传多个，每个以逗号分开*/
    fun getSongDetail(hash: String): String? {
        val url = "$apiaddress/privilege/lite".toUri().buildUpon().apply {
            appendQueryParameter("hash", hash)
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getSongDetail failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /**获取音乐专辑/歌手信息
     * 必选参数：
     *
     * @param album_audio_id: 专辑音乐 id (album_audio_id/MixSongID 均可以), 可以传多个，每个以逗号分开
     *
     * 可选参数
     *
     * @param fields: 可以传 album_info authors.base base audio_info, authors.ip, extra, tags, tagmap 每个 field 以逗号分开
     */
    fun getKrmAudio(album_audio_id: String,fileids: String? = null): String? {
        val url = "$apiaddress/krm/audio".toUri().buildUpon().apply {
            appendQueryParameter("album_audio_id", album_audio_id)
            fileids?.let { appendQueryParameter("fileids", it.toString())  }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()

        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getKrmAudio failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


    /** 私人 FM(对应手机和 pc 端的猜你喜欢)
     * 说明 : 私人 FM
     *
     * 可选参数：
     *
     * @param hash: 音乐 hash, 建议
     *
     * @param songid: 音乐 songid, 建议
     *
     * @param playtime: 已播放时间, 建议
     *
     * @param mode: 获取模式，默认为 normal, normal：发现，small： 小众，peak：30s
     *
     * @param action: 默认为 play, garbage: 为不喜欢
     *
     * @param song_pool_id： 手机版的 AI，0：Alpha 根据口味推荐相似歌曲, 1：Beta 根据风格推荐相似歌曲, 2：Gamma
     *
     * @param is_overplay: 是否已播放完成
     *
     * @param remain_songcnt: 剩余未播放歌曲数, 默认为 0，大于 4 不返回推荐歌曲，建议*/

    fun getFm(hash: String? = null,songid: String? = null,playtime: String? = null,
                       mode: String? = null,action: String? = null,song_pool_id: String? = null,
                       is_overplay: String? = null,remain_songcnt: String? = null): String? {

        val url = "$apiaddress/personal/fm".toUri().buildUpon().apply {
            hash?.let { appendQueryParameter("hash", it.toString())  }
            songid?.let { appendQueryParameter("songid", it.toString()) }
            playtime?.let { appendQueryParameter("playtime", it.toString()) }
            mode?.let { appendQueryParameter("mode", it.toString()) }
            action?.let { appendQueryParameter("action", it.toString()) }
            song_pool_id?.let { appendQueryParameter("song_pool_id", it.toString()) }
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()

        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getFm failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }



    }

    /** 领取 VIP（需要登陆，该接口为测试接口,仅限概念版使用） */
    fun getlitevip(): String? {
        val url = "$apiaddress/youth/vip".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 领取 一天 VIP（需要登陆，该接口为测试接口,仅限概念版使用） */
    fun getlitevipday(): String? {
        val url = "$apiaddress/youth/day/vip".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取当月已领取 VIP 天数（需要登陆，该接口为测试接口,仅限概念版使用） */
    fun getlitevipdayok(): String? {
        val url = "$apiaddress/youth/month/vip/record".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }

    /** 获取已领取 VIP 状态（需要登陆，该接口为测试接口,仅限概念版使用） */
    fun getlitevipok(): String? {
        val url = "$apiaddress/youth/union/vip".toUri().buildUpon().apply {
            token?.let { appendQueryParameter("token", it.toString()) }
            userid?.let { appendQueryParameter("userid", it.toString()) }
        }.build().toString()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                println("getMuiscInfo failed: ${response.code}")
            }
            val responseBody = response.body?.string() ?: return null
            return responseBody
        }
    }


}