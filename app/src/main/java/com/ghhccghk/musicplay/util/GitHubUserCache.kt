package com.ghhccghk.musicplay.util

import android.content.Context
import android.util.Log
import com.ghhccghk.musicplay.BuildConfig
import com.ghhccghk.musicplay.data.CachedGitHubUser
import com.ghhccghk.musicplay.data.GitHubUser
import com.ghhccghk.musicplay.util.apihelp.githubApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object GitHubUserCache {
    private const val CACHE_FILE_NAME = "github_user_cache.json"
    private const val MAX_AGE = 1 * 24 * 60 * 60 * 1000L // 缓存的最大有效期，1天

    private val memoryCache = mutableMapOf<String, CachedGitHubUser>()

    // 从内存中获取缓存的 GitHub 用户数据
    suspend fun getUser(context: Context, username: String): GitHubUser? {
        // 1. 先检查内存缓存
        memoryCache[username]?.takeIf { !isExpired(it) }?.let { return it.user }

        // 2. 如果内存缓存无效，检查本地文件缓存
        loadFromFile(context)

        // 3. 检查本地文件缓存
        memoryCache[username]?.takeIf { !isExpired(it) }?.let { return it.user }

        // 4. 如果都没有缓存，从网络请求数据
        return try {
            val user = githubApi.getUser(username)
            memoryCache[username] = CachedGitHubUser(user)  // 将数据存入内存缓存
            saveToFile(context)  // 同时保存到本地缓存
            user
        } catch (e: Exception) {
            if (BuildConfig.DEBUG){
                Log.e("GitHubUserCache", "getUser", e)
            }
            null
        }
    }

    // 判断缓存是否过期
    private fun isExpired(entry: CachedGitHubUser): Boolean {
        return System.currentTimeMillis() - entry.timestamp > MAX_AGE
    }

    // 清除过期的缓存
    private fun cleanExpired() {
        memoryCache.entries.removeIf { isExpired(it.value) }
    }

    // 从文件中加载缓存
    private fun loadFromFile(context: Context) {
        val file = File(context.cacheDir, CACHE_FILE_NAME)
        if (!file.exists()) return

        try {
            val mapType = object : TypeToken<Map<String, CachedGitHubUser>>() {}.type
            val jsonMap: Map<String, CachedGitHubUser> =
                Gson().fromJson(file.readText(), mapType)

            memoryCache.clear()  // 加载前先清空内存缓存
            memoryCache.putAll(jsonMap)
            cleanExpired()  // 清除过期的缓存
        } catch (e: Exception) {
            memoryCache.clear()  // 加载失败时清空缓存
        }
    }

    // 将缓存数据保存到文件
    private fun saveToFile(context: Context) {
        cleanExpired()  // 保存前先清理过期缓存
        val file = File(context.cacheDir, CACHE_FILE_NAME)
        file.writeText(Gson().toJson(memoryCache))
    }

    // 清除所有缓存（手动触发）
    fun clearAll(context: Context) {
        memoryCache.clear()
        File(context.cacheDir, CACHE_FILE_NAME).delete()
    }
}
