package com.ghhccghk.musicplay.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.ui.components.BaseTheme
import com.ghhccghk.musicplay.ui.login.LoginPassWord
import com.ghhccghk.musicplay.ui.login.LoginQrcode
import com.ghhccghk.musicplay.util.TokenManager.isLoggedIn

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.compose_homeView)

        composeView.apply {
            setContent {
                BaseTheme {
                    HomeScreen(
                        isLoggedIn = isLoggedIn(),
                        activity = requireActivity()
                    )
                }
            }
        }

        return view

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    activity: FragmentActivity
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 状态卡片
            StatusCard(isLoggedIn = isLoggedIn)

            // 登录区域（未登录时显示）
            if (!isLoggedIn) {
                Spacer(modifier = Modifier.height(16.dp))
                LoginSection(activity = activity)
            }
        }
    }
}

@Composable
private fun StatusCard(isLoggedIn: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoggedIn) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isLoggedIn) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isLoggedIn) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onError
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(if (isLoggedIn) R.string.activated else R.string.unactivated),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLoggedIn) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onError
                    }
                )
                Text(
                    text = stringResource(if (isLoggedIn) R.string.welcome else R.string.unactivated_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isLoggedIn) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onError.copy(alpha = 0.8f)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginSection(activity: FragmentActivity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("密码登录", "二维码登录")

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // 使用 AndroidView 包装现有的 Fragment
        Spacer(modifier = Modifier.height(8.dp))

        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                FragmentContainerView(context).apply {
                    id = View.generateViewId()
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                val fragment = if (selectedTab == 0) LoginPassWord() else LoginQrcode()
                activity.supportFragmentManager.commit {
                    replace(view.id, fragment)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
    }
}
