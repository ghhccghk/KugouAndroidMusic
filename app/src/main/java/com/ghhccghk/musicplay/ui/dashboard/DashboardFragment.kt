package com.ghhccghk.musicplay.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.HotSearchResponse
import com.ghhccghk.musicplay.data.KeywordGroup
import com.ghhccghk.musicplay.data.KeywordItem
import com.ghhccghk.musicplay.data.PlayCategory
import com.ghhccghk.musicplay.data.PlayCategoryBase
import com.ghhccghk.musicplay.data.ThemeMusicList
import com.ghhccghk.musicplay.data.ThemeMusicScene
import com.ghhccghk.musicplay.ui.components.BaseTheme
import com.ghhccghk.musicplay.ui.playlisttag.PlayTagListFragment
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_dashView)

        composeView.apply {
            setContent {
                BaseTheme {
                    DashboardScreen(activity = requireActivity())
                }
            }
        }

        return view

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(activity: FragmentActivity) {
    var hotSearchGroups by remember { mutableStateOf<List<KeywordGroup>>(emptyList()) }
    var categories by remember { mutableStateOf<List<PlayCategory>>(emptyList()) }
    var themeMusicList by remember { mutableStateOf<List<ThemeMusicScene>>(emptyList()) }
    var isLoadingHot by remember { mutableStateOf(true) }
    var isLoadingCategory by remember { mutableStateOf(true) }
    var isLoadingTheme by remember { mutableStateOf(true) }
    var errorHot by remember { mutableStateOf<String?>(null) }
    var errorCategory by remember { mutableStateOf<String?>(null) }
    var errorTheme by remember { mutableStateOf<String?>(null) }

    // 加载热词数据
    LaunchedEffect(Unit) {
        loadHotSearch(
            onSuccess = { hotSearchGroups = it },
            onError = { errorHot = it },
            onLoading = { isLoadingHot = it }
        )
    }

    // 加载歌单分类
    LaunchedEffect(Unit) {
        loadCategories(
            onSuccess = { categories = it },
            onError = { errorCategory = it },
            onLoading = { isLoadingCategory = it }
        )
    }

    // 加载主题歌单
    LaunchedEffect(Unit) {
        loadThemeMusic(
            onSuccess = { themeMusicList = it },
            onError = { errorTheme = it },
            onLoading = { isLoadingTheme = it }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发现音乐") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 搜索栏占位
            item {
                SearchBarPlaceholder()
            }

            // 排行榜部分
            item {
                SectionTitle("排行榜")
            }

            item {
                HotSearchSection(
                    groups = hotSearchGroups,
                    isLoading = isLoadingHot,
                    error = errorHot,
                    onRetry = {
                        isLoadingHot = true
                        errorHot = null
                    }
                )
            }

            // 歌单分类部分
            item {
                SectionTitle("歌单分类")
            }

            item {
                CategorySection(
                    categories = categories,
                    isLoading = isLoadingCategory,
                    error = errorCategory,
                    activity = activity,
                    onRetry = {
                        isLoadingCategory = true
                        errorCategory = null
                    }
                )
            }

            // 主题歌单部分
            item {
                SectionTitle("主题歌单")
            }

            item {
                ThemeMusicSection(
                    items = themeMusicList,
                    isLoading = isLoadingTheme,
                    error = errorTheme,
                    onRetry = {
                        isLoadingTheme = true
                        errorTheme = null
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SearchBarPlaceholder() {
    // 搜索功能需要使用 AndroidView 包装 Material SearchView
    // 这里先留空，后续可以添加
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

@Composable
private fun HotSearchSection(
    groups: List<KeywordGroup>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            ErrorCard(message = error, onRetry = onRetry)
        }

        else -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(groups) { group ->
                    HotGroupCard(group = group)
                }
            }
        }
    }
}

@Composable
private fun HotGroupCard(group: KeywordGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            group.keywords.forEach { keyword ->
                Text(
                    text = keyword.keyword,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    categories: List<PlayCategory>,
    isLoading: Boolean,
    error: String?,
    activity: FragmentActivity,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            ErrorCard(message = error, onRetry = onRetry)
        }

        else -> {
            // 使用 AndroidView 包装 ViewPager + TabLayout
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { context ->
                    androidx.fragment.app.FragmentContainerView(context).apply {
                        id = View.generateViewId()
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                },
                update = { view ->
                    // 加载第一个分类的标签
                    if (categories.isNotEmpty()) {
                        val fragment = PlayTagListFragment.newInstance(categories.first().son)
                        activity.supportFragmentManager.commit {
                            replace(view.id, fragment)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }
    }
}

@Composable
private fun ThemeMusicSection(
    items: List<ThemeMusicScene>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            ErrorCard(message = error, onRetry = onRetry)
        }

        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.take(5).forEach { scene ->
                    ThemeMusicCard(scene = scene)
                }
            }
        }
    }
}

@Composable
private fun ThemeMusicCard(scene: ThemeMusicScene) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = scene.title ?: "未知",
                style = MaterialTheme.typography.titleSmall
            )
            scene.intro?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = "播放量：",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("重试")
            }
        }
    }
}

private suspend fun loadHotSearch(
    onSuccess: (List<KeywordGroup>) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val json = KugouAPi.getSearchhot()
            if (json == null || json == "502" || json == "404") {
                withContext(Dispatchers.Main) {
                    onError("数据加载失败")
                    onLoading(false)
                }
            } else {
                val result =
                    Moshi.Builder().build().adapter(HotSearchResponse::class.java).fromJson(json!!)
                val groups = result?.data?.list?.map { listItem ->
                    KeywordGroup(
                        name = listItem.name,
                        keywords = listItem.keywords.take(5).mapIndexed { index, keywordItem ->
                            KeywordItem(
                                keyword = ". ",
                                keywordItem.keyword
                            )
                        }
                    )
                }
                withContext(Dispatchers.Main) {
                    onSuccess(groups ?: emptyList())
                    onLoading(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("数据加载失败: ")
                onLoading(false)
            }
        }
    }
}

private suspend fun loadCategories(
    onSuccess: (List<PlayCategory>) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val json = KugouAPi.getPlayListTag()
            if (json == null || json == "502" || json == "404") {
                withContext(Dispatchers.Main) {
                    onError("数据加载失败")
                    onLoading(false)
                }
            } else {
                val result =
                    Moshi.Builder().build().adapter(PlayCategoryBase::class.java).fromJson(json!!)
                withContext(Dispatchers.Main) {
                    onSuccess(result?.data ?: emptyList())
                    onLoading(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("数据加载失败: ")
                onLoading(false)
            }
        }
    }
}

private suspend fun loadThemeMusic(
    onSuccess: (List<ThemeMusicScene>) -> Unit,
    onError: (String) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val json = KugouAPi.getPlayListTheme()
            if (json == null || json == "502" || json == "404") {
                withContext(Dispatchers.Main) {
                    onError("数据加载失败")
                    onLoading(false)
                }
            } else {
                val result =
                    Moshi.Builder().build().adapter(ThemeMusicList::class.java).fromJson(json!!)
                withContext(Dispatchers.Main) {
                    onSuccess(result?.data?.themeList ?: emptyList())
                    onLoading(false)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("数据加载失败: ")
                onLoading(false)
            }
        }
    }
}

