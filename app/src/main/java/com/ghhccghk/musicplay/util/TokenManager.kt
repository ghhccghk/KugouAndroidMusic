package com.ghhccghk.musicplay.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_ID = "user_id"
    private const val KEY_TOKEN = "user_token"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun saveUserId(id: String) {
        prefs?.edit()?.putString(KEY_ID, id)?.apply()
    }

    fun getToken(): String? {
        return prefs?.getString(KEY_TOKEN, null)
    }

    fun getUserId(): String? {
        return prefs?.getString(KEY_ID, null)
    }

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun clearUserId() {
        prefs?.edit()?.remove(KEY_ID)?.apply()
    }

    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty() && !getUserId().isNullOrEmpty()
    }
}
