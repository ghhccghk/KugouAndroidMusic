package com.ghhccghk.musicplay.data


data class CachedGitHubUser(
    val user: GitHubUser,  // 缓存的 GitHub 用户数据
    val timestamp: Long = System.currentTimeMillis()  // 缓存时间戳
)

data class GitHubUser(
    val login: String = "",
    val name: String = "",
    val avatar_url: String = "",
    val contribute : String = "",
    val bio: String = ""
)