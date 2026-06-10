package com.ghhccghk.musicplay.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_ID = "user_id"
    private const val KEY_TOKEN = "user_token"
    private const val KEY_DFID = "user_dfid"
    private const val KEY_MID = "mid"
    private const val KEY_GUID = "guid"
    private const val KEY_serverDev = "server_dev"


    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // sanitize existing stored dfid if it is the literal "null" or blank
        try {
            val existing = prefs?.getString(KEY_DFID, null)
            if (!existing.isNullOrBlank() && existing == "null") {
                prefs?.edit()?.remove(KEY_DFID)?.apply()
                Log.i("TokenManager", "Removed invalid stored dfid value: '$existing'")
            } else if (existing.isNullOrBlank()) {
                // also remove empty strings
                prefs?.edit()?.remove(KEY_DFID)?.apply()
            }
        } catch (t: Throwable) {
            Log.w("TokenManager", "Failed to sanitize stored dfid", t)
        }
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun saveUserId(id: String) {
        prefs?.edit()?.putString(KEY_ID, id)?.apply()
    }

    // Accept nullable and avoid saving literal "null" or blank values
    fun saveDfid(dfid: String?) {
        // capture caller info for diagnostics
        val stack = Exception().stackTrace.drop(1).take(6).joinToString(" -> ") { "${it.className}.${it.methodName}:${it.lineNumber}" }
        if (dfid.isNullOrBlank() || dfid == "null") {
            Log.w("TokenManager", "Refusing to save empty or 'null' dfid: $dfid; caller: $stack")
            return
        }
        prefs?.edit()?.putString(KEY_DFID, dfid)?.apply()
    }
    fun saveMid(mid: String) {
        prefs?.edit()?.putString(KEY_MID, mid)?.apply()
    }

    fun saveGuid(guid: String) {
        prefs?.edit()?.putString(KEY_GUID, guid)?.apply()
    }

    fun saveServerDev(s: String) {
        prefs?.edit()?.putString(KEY_serverDev, s)?.apply()
    }

    fun getToken(): String? {
        return prefs?.getString(KEY_TOKEN, null)
    }

    fun getUserId(): String? {
        return prefs?.getString(KEY_ID, null)
    }

    fun getDfid(): String? {
        val v = prefs?.getString(KEY_DFID, null)
        return if (v.isNullOrBlank() || v == "null") {
            null
        } else {
            v
        }

    }

    fun getMid(): String? {
        return prefs?.getString(KEY_MID, null)
    }

    fun getGuid(): String? {
        return prefs?.getString(KEY_GUID, null)
    }

    fun getServerDev(): String? {
        return prefs?.getString(KEY_serverDev, null)
    }

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun clearUserId() {
        prefs?.edit()?.remove(KEY_ID)?.apply()
    }

    fun clearDfid(){
        prefs?.edit()?.remove(KEY_DFID)?.apply()

    }

    fun cleanMid() {
        prefs?.edit()?.remove(KEY_MID)?.apply()
    }

    fun cleanGuid() {
        prefs?.edit()?.remove(KEY_GUID)?.apply()
    }

    fun cleanServerDev() {
        prefs?.edit()?.remove(KEY_serverDev)?.apply()
    }

    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty() && !getUserId().isNullOrEmpty()
    }
}
