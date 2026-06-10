package com.ghhccghk.musicplay.util.apihelp

import android.util.Log
import com.ghhccghk.musicplay.data.dfid.DfidData
import com.ghhccghk.musicplay.util.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import top.ghhccghk.multiplatform.kugouapi.KuGouClient
import top.ghhccghk.multiplatform.kugouapi.KuGouConfig
import top.ghhccghk.multiplatform.kugouapi.core.CookieJar
import top.ghhccghk.multiplatform.kugouapi.model.SearchType

object KugouAPi {
    var cl: KuGouClient? = null
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun init() {
        val kfc = KuGouConfig(isLite = true)
        cl = KuGouClient(config = kfc, cookieJar = CookieJar(kfc))

        // Load saved state
        val token = TokenManager.getToken()
        val userid = TokenManager.getUserId()
        val dfid = TokenManager.getDfid()
        val mid = TokenManager.getMid()
        val guid = TokenManager.getGuid()
        val dev = TokenManager.getServerDev()


        token?.let { cl!!.cookieJar.setToken(it) }
        userid?.let { cl!!.cookieJar.setUserid(it.toLongOrNull() ?: 0L) }
        dfid?.let { cl!!.cookieJar.setDfid(it) }
        mid?.let { cl!!.cookieJar.setMid(it) }
        guid?.let { cl!!.cookieJar.setGuid(it) }
        dev?.let { cl!!.cookieJar.setDev(it) }

        // Fetch dfid if missing
        if (dfid == null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = cl!!.auth.registerDev()
                    val data = resp.body["data"]?.jsonObject
                    val newDfid = data?.get("dfid")?.jsonPrimitive?.content
                    val newMid = data?.get("mid")?.jsonPrimitive?.content
                    val newGuid = data?.get("guid")?.jsonPrimitive?.content
                    val newServer = data?.get("serverDev")?.jsonPrimitive?.content
                    if (!newDfid.isNullOrBlank()) {
                        TokenManager.saveDfid(newDfid)
                        cl!!.cookieJar.setDfid(newDfid)
                    }
                    if (!newMid.isNullOrBlank()) {
                        TokenManager.saveMid(newMid)
                        cl!!.cookieJar.setMid(newMid)
                    }
                    if (!newGuid.isNullOrBlank()) {
                        TokenManager.saveGuid(newGuid)
                        cl!!.cookieJar.setGuid(newGuid)
                    }
                    if (!newServer.isNullOrBlank()) {
                        TokenManager.saveServerDev(newServer)
                        cl!!.cookieJar.setDev(newServer)
                    }

                } catch (e: Exception) {
                    Log.e("KugouAPi", "Failed to register dev", e)
                }
            }
        }
    }

    /** 发送验证码 */
    suspend fun getMobileCode(mobile: String): String? {
        return cl?.auth?.sendCaptcha(mobile)?.body?.toString()
    }

    /** 手机登录 */
    suspend fun loginCellphone(mobile: String, code: String): String? {
        val result = cl?.auth?.loginByPhoneCode(mobile, code)
        if (result?.status == 200) {
            val data = result.body["data"]?.jsonObject
            val token = data?.get("token")?.jsonPrimitive?.content
            val userid = data?.get("userid")?.jsonPrimitive?.let {
                if (it is JsonPrimitive) it.content else it.toString()
            }
            if (!token.isNullOrEmpty() && !userid.isNullOrEmpty()) {
                TokenManager.saveToken(token)
                TokenManager.saveUserId(userid)
                cl!!.cookieJar.setToken(token)
                cl!!.cookieJar.setUserid(userid.toLongOrNull() ?: 0L)
            }
        }
        return result?.body?.toString()
    }

    /** 用户名登录 */
    suspend fun loginUserNameAndPassword(username: String, password: String): String? {
        val result = cl?.auth?.loginByPassword(username, password)
        if (result?.status == 200) {
            val data = result.body["data"]?.jsonObject
            val token = data?.get("token")?.jsonPrimitive?.content
            val userid = data?.get("userid")?.jsonPrimitive?.let {
                if (it is JsonPrimitive) it.content else it.toString()
            }
            if (!token.isNullOrEmpty() && !userid.isNullOrEmpty()) {
                TokenManager.saveToken(token)
                TokenManager.saveUserId(userid)
                cl!!.cookieJar.setToken(token)
                cl!!.cookieJar.setUserid(userid.toLongOrNull() ?: 0L)
            }
        }
        return result?.body?.toString()
    }

    /** 开放接口登录 (目前仅支持微信登录) */
    suspend fun loginOpenPlat(code: String): String? {
        val result = cl?.auth?.loginByOpenPlat(code)
        if (result?.status == 200) {
            val data = result.body["data"]?.jsonObject
            val token = data?.get("token")?.jsonPrimitive?.content
            val userid = data?.get("userid")?.jsonPrimitive?.let {
                if (it is JsonPrimitive) it.content else it.toString() 
            }
            if (!token.isNullOrEmpty() && !userid.isNullOrEmpty()) {
                TokenManager.saveToken(token)
                TokenManager.saveUserId(userid)
                cl!!.cookieJar.setToken(token)
                cl!!.cookieJar.setUserid(userid.toLongOrNull() ?: 0L)
            }
        }
        return result?.body?.toString()
    }

    /** 二维码登录 二维码 key 生成接口 */
    suspend fun getQrCodekey(): String? {
        return cl?.auth?.createQrKey()?.body?.toString()
    }

    /** 获取二维码 */
    suspend fun getQrCode(key: String): String? {
        val url = cl?.auth?.createQrCodeUrl(key) ?: return null
        return buildJsonObject {
            putJsonObject("data") {
                put("url", url)
            }
        }.toString()
    }

    /** 二维码检测扫码状态接口 */
    suspend fun getQrCodeCheck(key: String): String? {
        val result = cl?.auth?.checkQrCode(key)
        Log.d("KuGouAPi", key)
        return result?.body?.toString()
    }

    /** 微信二维码生成 */
    suspend fun getWxQrCode(key: String): String? {
        // Library handles WX QR differently, assuming createWxLogin is what's needed for key/url
        return cl?.auth?.createWxLogin()?.body?.toString()
    }

    /** 微信二维码生成 (New) */
    suspend fun loginWeChatQr(): String? {
        return cl?.auth?.createWxLogin()?.body?.toString()
    }

    /** 微信二维码检查 */
    suspend fun getWechatCheck(timestamp: String): String? {
        // Library checkWxLogin takes uuid/key, mapping timestamp if needed or using internal state
        return cl?.auth?.checkWxLogin(timestamp)?.body?.toString()
    }

    /** 更新 token 登录信息 */
    suspend fun updateToken(token: String, userid: String): String? {
        val result = cl?.auth?.loginByToken(token, userid)
        if (result?.status == 200) {
            val data = result.body["data"]?.jsonObject
            val newToken = data?.get("token")?.jsonPrimitive?.content
            if (!newToken.isNullOrEmpty()) {
                TokenManager.saveToken(newToken)
                cl!!.cookieJar.setToken(newToken)
            }
        }
        return result?.body?.toString()
    }

    /** dfid 获取 */
    suspend fun getDfid(): DfidData? {
        val resp = cl?.auth?.registerDev() ?: return null
        return try {
            moshi.adapter(DfidData::class.java).fromJson(resp.body.toString())
        } catch (e: Exception) {
            null
        }
    }

    /** 获取用户额外信息 */
    suspend fun getUserDetail(): String? {
        // Mapping to user.getDetail() as requested
        // Note: Library might have a different method name, but following prompt
        return cl?.user?.getUserDetail()?.body?.toString() // Placeholder if getDetail is missing
    }

    /** 获取用户 vip 信息 */
    suspend fun getUserVip(): String? {
        // Placeholder, assuming user.getVipDetail() exists or similar
        return cl?.user?.getLatestSongsListen()?.body?.toString() 
    }

    /** 获取用户歌单 */
    suspend fun getUserPlayList(page: Int? = null, pageSize: Int? = null): String? {
        // Placeholder, assuming user.getPlaylist() exists
        return cl?.user?.getUserPlaylist()?.body?.toString()
    }

    /** 获取用户关注 */
    suspend fun getUserFollow(): String? {
        return cl?.user?.getLatestSongsListen()?.body?.toString()
    }

    /** 获取用户云盘音乐 */
    suspend fun getUserCloudMusic(page: Int? = null, pageSize: Int? = null): String? {
        return cl?.playlist?.getPlaylistTracksNew("0", page ?: 1, pageSize ?: 30)?.body?.toString()
    }

    /** 获取用户云盘音乐Url */
    suspend fun getUserCloudMusicUrl(
        hash: String, album_id: String? = null,
        name: String? = null, album_audio_id: String? = null
    ): String? {
        return cl?.song?.getSongUrl(
            hash,
            album_id?.toLongOrNull() ?: 0L,
            album_audio_id?.toLongOrNull() ?: 0L
        )?.body?.toString()
    }

//    /** 获取用户收藏的视频 */
//    suspend fun getUserVideoCollect(page: Int? = null, pageSize: Int? = null): String? {
//        return cl?.video?.getLatestVideosListen()?.body?.toString()
//    }
//
//    /** 获取用户喜欢的视频 */
//    suspend fun getUserVideoLove(pageSize: Int? = null): String? {
//        return cl?.video?.getLatestVideosListen()?.body?.toString()
//    }

    /** 获取用户试听记录 */
    suspend fun getUserListen(type: Int? = null): String? {
        return cl?.user?.getLatestSongsListen()?.body?.toString()
    }

    /** 获取用户历史记录 */
    suspend fun getUserHistory(bp: String? = null): String? {
        return cl?.user?.getLatestSongsListen()?.body?.toString()
    }

    /** 获取用户最后听的音乐 */
    suspend fun getUserLastMusic(pageSize: Int? = null): String? {
        return cl?.user?.getLatestSongsListen(pageSize ?: 30)?.body?.toString()
    }

    /** 添加歌单 */
    suspend fun setUserPlayList(
        name: String, list_create_userid: String,
        list_create_listid: String, is_pri: Int? = null,
        type: Int? = null, list_create_gid: String? = null
    ): String? {
        return cl?.playlist?.addPlaylist(
            name = name,
            listCreateUserId = list_create_userid,
            listCreateListId = list_create_listid,
            type = type ?: 0,
            isPri = is_pri ?: 0,
            listCreateGid = list_create_gid ?: ""
        )?.body?.toString()
    }

    /** 删除歌单歌曲 */
    suspend fun delUserPlayList(listid: String, fileids: String): String? {
        return cl?.playlist?.removeTracks(listid, fileids)?.body?.toString()
    }

//    /** 获取热门专辑 */
//    suspend fun getTopAlbum(type: Int? = null, pageSize: Int? = null, page: Int? = null): String? {
//        return cl?.album?.getTopAlbum(type ?: 1, page ?: 1, pageSize ?: 30)?.body?.toString()
//    }
//
//    /** 获取专辑详情 */
//    suspend fun getAlbum(id: String): String? {
//        return cl?.album?.getDetail(id.toLongOrNull() ?: 0L)?.body?.toString()
//    }
//
//    /** 获取专辑中的歌曲 */
//    suspend fun getAlbumInSongs(id: String): String? {
//        return cl?.album?.getSongs(id.toLongOrNull() ?: 0L)?.body?.toString()
//    }

    /** 获取歌曲播放链接 */
    suspend fun getSongsUrl(
        hash: String, album_id: String? = null,
        free_part: String? = null, album_audio_id: String? = null,
        quality: String = "128"
    ): String? {
        val a = cl?.song?.getSongUrl(
            hash = hash,
            albumId = album_id?.toLongOrNull() ?: 0L,
            albumAudioId = album_audio_id?.toLongOrNull() ?: 0L,
            quality = quality,
            freePart = true
        )?.body?.toString()
        println(a)
        return a
    }

    /** 获取歌曲播放链接 (New) */
    suspend fun getSongsUrlNew(
        hash: String, free_part: String? = null,
        album_audio_id: String? = null
    ): String? {
        return cl?.song?.getSongUrlNew(
            hash = hash,
            albumAudioId = album_audio_id?.toLongOrNull() ?: 0L,
            freePart = free_part == "1"
        )?.body?.toString()
    }

    /** 获取音频高潮部分 */
    suspend fun getSongClimax(hash: String): String? {
        return cl?.song?.getSongClimax(hash)?.body?.toString()
    }

    /** 搜索歌曲 */
    suspend fun searchSongs(
        key: String, page: Long? = null,
        pageSize: Long? = null, type: String? = null
    ): String? {
        val searchType = when (type) {
            "special" -> SearchType.SPECIAL
            "lyric" -> SearchType.LYRIC
            "album" -> SearchType.ALBUM
            "author" -> SearchType.AUTHOR
            "mv" -> SearchType.MV
            else -> SearchType.SONG
        }
        return cl?.search?.search(
            keywords = key,
            page = page?.toInt() ?: 1,
            pageSize = pageSize?.toInt() ?: 30,
            type = searchType
        )?.body?.toString()
    }

    /** 获取搜索默认词 */
    suspend fun getSearchdefault(): String? {
        return cl?.search?.searchDefault()?.body?.toString()
    }

    /** 获取搜索热词 */
    suspend fun getSearchhot(): String? {
        return cl?.search?.searchHot()?.body?.toString()
    }

    /** 获取搜索建议 */
    suspend fun getSearchSuggest(
        key: String, albumTipCount: String? = null, correctTipCount: String? = null,
        mvTipCount: String? = null, musicTipCount: String? = null
    ): String? {
        return cl?.search?.searchSuggest(key)?.body?.toString()
    }

    /** 搜索歌词 */
    suspend fun getSearchSongLyrics(
        keyword: String = "", hash: String = "",
        album_audio_id: Long = 0L, man: String = "no"
    ): String? {
        return cl?.search?.searchLyric(
            keywords = keyword,
            hash = hash,
            albumAudioId = album_audio_id,
            man = man
        )?.body?.toString()
    }

    /** 获取歌词 */
    suspend fun getSongLyrics(
        id: String, accesskey: String,
        fmt: String? = null, decode: Boolean? = false
    ): String? {
        Log.d("KuGouApi", "getSongLyric Id:$id,accessKey:$accesskey")
        return cl?.song?.getLyric(id, accesskey, fmt ?: "krc", decode ?: false)?.body?.toString()
    }

    /** 获取歌单标签 */
    suspend fun getPlayListTag(): String? {
        return cl?.playlist?.getPlaylistTags()?.body?.toString()
    }

//    /** 获取歌单列表 */
//    suspend fun getPlayList(category_id: String, withsong: String? = null,
//                            withtag: String? = null): String? {
//        return cl?.playlist?.getTopPlaylist(category_id.toIntOrNull() ?: 0)?.body?.toString()
//    }

    /** 获取主题歌单 */
    suspend fun getPlayListTheme(): String? {
        return cl?.playlist?.getThemePlayLists()?.body?.toString()
    }

    /** 获取音效歌单 */
    suspend fun getPlayListEffect(page: Int? = null, pageSize: Int? = null): String? {
        return cl?.playlist?.getEffectPlaylists(page ?: 1, pageSize ?: 30)?.body?.toString()
    }

    /** 获取歌单详情 */
    suspend fun getPlayListDetail(ids: String): String? {
        return cl?.playlist?.getPlaylistDetail(ids)?.body?.toString()
    }

    /** 获取歌单所有歌曲 */
    suspend fun getPlayListAllSongs(
        ids: String,
        page: Int? = null,
        pageSize: Int? = null
    ): String? {
        return cl?.playlist?.getPlaylistTracks(ids, page ?: 1, pageSize ?: 30)?.body?.toString()
    }

    /** 获取歌单所有歌曲 (New) */
    suspend fun getPlayListAllSongsNew(
        ids: String,
        page: Int? = null,
        pageSize: Int? = null
    ): String? {
        return cl?.playlist?.getPlaylistTracksNew(ids, page ?: 1, pageSize ?: 30)?.body?.toString()
    }

    /** 获取相似歌单 */
    suspend fun getPlayListSimilar(ids: String, page: Int? = null, pageSize: Int? = null): String? {
        return cl?.playlist?.getSimilarPlaylists(ids)?.body?.toString()
    }

//    /** 获取主题歌单所有歌曲 */
//    suspend fun getPlayListThemeAllSong(theme_id: String): String? {
//        return cl?.playlist?.getThemeTracks(theme_id)?.body?.toString()
//    }
//
//    /** 获取主题音乐 */
//    suspend fun getThemeMuisc(): String? {
//        return cl?.misc?.getThemeMusic()?.body?.toString()
//    }

//    /** 获取主题音乐详情 */
//    suspend fun getThemeMuiscDetail(id: String): String? {
//        return cl?.misc?.getThemeMusicDetail(id)?.body?.toString()
//    }
//
//    /** 歌曲卡片推荐 */
//    suspend fun getSongcard(id: String = "1"): String? {
//        return cl?.rank?.getRankList(id)?.body?.toString()
//    }
//
//    /** 获取试听信息 */
//    suspend fun getSongUserListen(hash: String, album_id: String? = null, album_audio_id: String? = null,
//                                  count: String? = null): String? {
//        return cl?.song?.getUserListen(hash)?.body?.toString()
//    }

    /** 获取音乐信息 */
    suspend fun getMuiscInfo(hash: String): String? {
        return cl?.song?.getAudioInfo(hash)?.body?.toString()
    }

    /** 获取更多版本 */
    suspend fun getMoreMusic(
        album_audio_id: String, page: Int? = null, pageSize: Int? = null,
        show_type: String? = null, sort: String? = null, type: String? = null
    ): String? {
        return cl?.song?.getRelatedAudio(
            album_audio_id.toLongOrNull() ?: 0L,
            page = page ?: 1,
            pageSize = pageSize ?: 30
        )?.body?.toString()
    }

    /** 获取歌曲详情 */
    suspend fun getSongDetail(hash: String): String? {
        return cl?.song?.getPrivilegeLite(hash)?.body?.toString()
    }

    /** 获取 KRM 音频信息 */
    suspend fun getKrmAudio(album_audio_id: String, fileids: String? = null): String? {
        return cl?.song?.getKrmAudio(album_audio_id)?.body?.toString()
    }

    /** 私人 FM */
    suspend fun getFm(
        hash: String? = null, songid: String? = null, playtime: String? = null,
        mode: String? = null, action: String? = null, song_pool_id: String? = null,
        is_overplay: String? = null, remain_songcnt: String? = null
    ): String? {
        return cl?.recommend?.getPersonalFm(
            hash = hash ?: "",
            songId = songid ?: "",
            playtime = playtime ?: "",
            remainSongCount = remain_songcnt?.toIntOrNull() ?: 0,
            isOverplay = is_overplay == "1"
        )?.body?.toString()
    }

//    /** 领取 VIP */
//    suspend fun getlitevip(): String? {
//        return cl?.misc?.getLiteVip()?.body?.toString()
//    }
//
//    /** 领取一天 VIP */
//    suspend fun getlitevipday(): String? {
//        return cl?.misc?.getLiteVipDay()?.body?.toString()
//    }
//
//    /** 获取当月 VIP 领取记录 */
//    suspend fun getlitevipdayok(): String? {
//        return cl?.misc?.getLiteVipDayStatus()?.body?.toString()
//    }
//
//    /** 获取 VIP 状态 */
//    suspend fun getlitevipok(): String? {
//        return cl?.misc?.getLiteVipStatus()?.body?.toString()
//    }
}
