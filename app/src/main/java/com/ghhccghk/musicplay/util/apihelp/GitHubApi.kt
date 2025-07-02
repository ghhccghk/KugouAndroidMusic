package com.ghhccghk.musicplay.util.apihelp

import android.content.Context
import com.ghhccghk.musicplay.data.GitHubUser
import com.ghhccghk.musicplay.util.GitHubUserCache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {
    @GET("users/{username}")
    suspend fun getUser(@Path("username") username: String): GitHubUser
}

val githubApi: GitHubApi by lazy {
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val requestWithAuth = originalRequest.newBuilder()
                .build()
            chain.proceed(requestWithAuth)
        }
        .build()

    Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubApi::class.java)
}

suspend fun fetchGitHubUser(username: String, context: Context): GitHubUser? {
    return GitHubUserCache.getUser(context, username)
}

