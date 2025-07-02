@file:OptIn(ExperimentalMaterial3Api::class)

package com.ghhccghk.musicplay.ui.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.GitHubUser
import com.ghhccghk.musicplay.util.apihelp.fetchGitHubUser

class ContributorsSettingsActivity : BaseSettingsActivity(
    R.string.settings_contributors, { ContributorsFragment() })

class ContributorsFragment : BaseFragment(null) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                PreviewContributorsSettings() // 这里是你的 Compose Composable 函数
            }
        }
    }

    @Composable
    fun ContributorCard(contributor: GitHubUser) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            onClick = {
                val url = "https://github.com/${contributor.login}"
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                startActivity(intent) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 头像
                GlideComposeImage(
                    contributor.avatar_url,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                )
                Spacer(modifier = Modifier.width(16.dp))
                // 贡献者信息
                Column {
                    Row {
                        Text(
                            text = contributor.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "@${contributor.login}",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = contributor.contribute,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    @Composable
    fun ContributorList(contributors: List<GitHubUser>) {
        LazyColumn {
            items(contributors) { contributor ->
                ContributorCard(contributor)
            }
        }
    }

    @Composable
    fun ContributorsSettingsScreen() {
        val contributors = remember { mutableStateOf<List<GitHubUser>>(emptyList()) }

        LaunchedEffect(Unit) {
            contributors.value = getContributorsList(requireContext())
        }
        MaterialTheme(
            colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme())
                    dynamicDarkColorScheme(requireContext())
                else
                    dynamicLightColorScheme(requireContext())
            } else {
                if (isSystemInDarkTheme()) {
                    darkColorScheme()
                } else {
                    lightColorScheme()
                }
            }
        ) {
            ContributorList(contributors = contributors.value)

        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewContributorsSettings() {
        ContributorsSettingsScreen()
    }


    @SuppressLint("CheckResult")
    @Composable
    fun GlideComposeImage(
        url: String?,
        modifier: Modifier = Modifier,
        placeholderResId: Int? = null,
        errorResId: Int? = null,
        circleCrop: Boolean = false,
    ) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imageView ->
                val request = Glide.with(imageView.context)
                    .load(url?.toUri())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)

                if (placeholderResId != null) {
                    request.placeholder(placeholderResId)
                }
                if (errorResId != null) {
                    request.error(errorResId)
                }
                if (circleCrop) {
                    request.apply(RequestOptions.circleCropTransform())
                }

                request.into(imageView)
            }
        )
    }

    // 获取 contributors_user 数组中的用户名并获取 GitHubUser 信息
    suspend fun getContributorsList(context: Context): List<GitHubUser> {
        val usernames = context.resources.getStringArray(R.array.contributors_user)  // 读取字符串数组
        val contributorsList = mutableListOf<GitHubUser>()

        val contribute = context.resources.getStringArray(R.array.contributors_contribute)

        // 遍历用户名，获取每个用户的 GitHub 信息
        for (username in usernames) {
            try {
                val user =
                    fetchGitHubUser(context = context, username = username)  // 从缓存中获取 GitHub 用户信息
                if (user != null) {
                    contributorsList.add(user.copy(contribute = contribute[usernames.indexOf(username)]))
                } else {
                    // 错误处理：可以记录日志或者添加空用户
                    contributorsList.add(GitHubUser(username, "$username", "", "${contribute[usernames.indexOf(username)]}"))
                }
            } catch (e: Exception) {
                // 错误处理：可以记录日志或者添加空用户
                contributorsList.add(GitHubUser(username, "$username", "", "${contribute[usernames.indexOf(username)]}"))
            }
        }

        return contributorsList
    }
}

