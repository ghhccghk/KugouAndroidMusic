package com.ghhccghk.musicplay.ui.user

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.user.Data
import com.ghhccghk.musicplay.data.user.UserDetail
import com.ghhccghk.musicplay.data.user.likeplaylist.LikePlayListBase
import com.ghhccghk.musicplay.data.user.vipdata.VipResponse
import com.ghhccghk.musicplay.databinding.FragmentUserBinding
import com.ghhccghk.musicplay.ui.setting.MainSettingsActivity
import com.ghhccghk.musicplay.util.TokenManager
import com.ghhccghk.musicplay.util.TokenManager.isLoggedIn
import com.ghhccghk.musicplay.util.adapte.playlist.UserLikePLayListAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var prefs =
        MainActivity.lontext.getSharedPreferences("play_setting_prefs", MODE_PRIVATE)
    private val vipState = mutableStateOf<VipResponse?>(null)

    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory()) // 让 Moshi 支持 Kotlin 默认值和非空
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        _binding = FragmentUserBinding.inflate(inflater, container, false)
        val vipupdate = prefs.getString("vipupdate", "")

        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = format.format(calendar.time)
        if (vipupdate != today){
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val a = KugouAPi.getlitevip()
                        if (a == null || a == "502" || a == "404") {
                            Toast.makeText(
                                requireContext(),
                                R.string.token_update_error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                withContext(Dispatchers.IO) {
                    try {
                        KugouAPi.updateToken(
                            TokenManager.getToken().toString(),
                            TokenManager.getUserId().toString()
                        ).toString()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            MainActivity.lontext,
                            R.string.token_update_error,
                            Toast.LENGTH_SHORT
                        ).show()
                        null
                    }
                }
                prefs.edit { putString("vipupdate", today) }
            }
        }

        val root: View = binding.root
        binding.toolbar.inflateMenu(R.menu.toolbar_menu_user)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_update_token -> {
                    lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val a = KugouAPi.getlitevip()
                                    if (a == null || a == "502" || a == "404") {
                                        Toast.makeText(
                                            MainActivity.lontext,
                                            R.string.token_update_error,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(
                                        MainActivity.lontext,
                                        R.string.token_update_error,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    null
                                }
                            }
                        withContext(Dispatchers.IO) {
                            try {
                                KugouAPi.updateToken(
                                    TokenManager.getToken().toString(),
                                    TokenManager.getUserId().toString()
                                ).toString()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    MainActivity.lontext,
                                    R.string.token_update_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                                null
                            }
                        }
                    }; true

                }
                R.id.action_settings -> {
                    requireActivity().startActivity(Intent(requireActivity(), MainSettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
        TokenManager.init(requireContext())
        KugouAPi.init()
        //  初始化 ComposeView UI（只执行一次）
        binding.vipComposeView.setContent {
            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme())
                    dynamicDarkColorScheme(requireContext())
                else
                    dynamicLightColorScheme(requireContext())
            } else {
                if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                vipState.value?.let {
                    VipInfoScreen(it.data) // 显示 VIP 信息
                } ?: Text(text = "解析失败",style = MaterialTheme.typography.titleMedium ) // 没数据时显示
            }
        }
        if (MainActivity.isNodeRunning && isLoggedIn()) {
            setUserInfoUi()
            setUserPlayList()
            setVipInfoUi()
        } else {
            binding.notLoggedIn.visibility = View.VISIBLE
            binding.layoutUserInfo.visibility = View.GONE
            binding.userLikePlaylistViewBase.visibility = View.GONE
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        if (MainActivity.isNodeRunning  && isLoggedIn() ) {
            setUserInfoUi()
            setUserPlayList()
            setVipInfoUi()
        }
    }

    private fun setVipInfoUi() {
        // 请求数据
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getUserVip()
            }

            if (json.isNullOrEmpty() || json == "502" || json == "404") {
                vipState.value = null
                return@launch
            }

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val adapter = moshi.adapter(VipResponse::class.java)

            try {
                vipState.value = adapter.fromJson(json) // 成功解析更新 UI
            } catch (e: Exception) {
                e.printStackTrace()
                vipState.value = null // 解析失败
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setUserInfoUi() {
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getUserDetail()
            }

            // 提前返回，减少 if 嵌套
            if (json.isNullOrEmpty() || json == "502" || json == "404") {
                showNotLoggedIn()
                return@launch
            }

            val userinfo = moshi.adapter(UserDetail::class.java).fromJson(json)
            val data = userinfo?.data ?: run {
                showNotLoggedIn()
                return@launch
            }

            showUserInfo(data)
        }

    }

    // 抽取显示未登录UI
    private fun showNotLoggedIn() {
        binding.notLoggedIn.isVisible = true
        binding.layoutUserInfo.isVisible = false
        binding.userLikePlaylistViewBase.isVisible = false
        binding.vipComposeView.isVisible = false
    }

    // 抽取显示用户信息UI
    private fun showUserInfo(data: Data) {
        binding.notLoggedIn.isVisible = false
        binding.layoutUserInfo.isVisible = true

        binding.textNickname.text = data.nickname
        binding.userGender.text = getGenderLabel(data.gender)
        binding.textGrade.text = " ${data.p_grade}"
        binding.textListTimeDuration.text = data.duration.toString()
        binding.textLocation.text = "${data.province} ${data.city}"
        binding.textBirthday.text = data.birthday
        binding.textOccupation.text = data.occupation
        binding.textFans.text = data.fans.toString()
        binding.textFollows.text = data.follows.toString()
        binding.textVisitors.text = data.visitors.toString()

        binding.textLastTime.text = data.logintime.toLocalDateTimeString()
        binding.imageAvatar.loadHttpsImage(data.pic)
    }

    // 性别映射
    private fun getGenderLabel(gender: Int): String = when (gender) {
        1 -> getString(R.string.gender_male)
        0 -> getString(R.string.gender_female)
        else -> getString(R.string.gender_secret)
    }

    // 时间戳转格式化字符串
    private fun Long.toLocalDateTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochSecond(this))
    }

    // 图片加载扩展
    private fun ImageView.loadHttpsImage(url: String) {
        val secureUrl = url.replaceFirst("http://", "https://")
        Glide.with(context)
            .load(secureUrl)
            .into(this)
    }


    fun setUserPlayList() {
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getUserPlayList()
            }
            if (json == null || json == "502" || json == "404") {
                Toast.makeText(requireContext(), "获取用户歌单失败", Toast.LENGTH_SHORT).show()
            } else {
//                val userplaylist = gson.fromJson(json, LikePlayListBase::class.java)
                val adapters = moshi.adapter(LikePlayListBase::class.java)
                val userplaylist = adapters.fromJson(json)
                val data = userplaylist!!.data.info
                binding.recyclerViewUserLikePlaylist.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val adapter = UserLikePLayListAdapter(data) {
                    val bundle = Bundle().apply {
                        putString("playlistId", it.global_collection_id)
                        if (it.name != "我喜欢") {
                            putString("picurl", it.pic.replaceFirst("/{size}/", "/"))
                        }

                    }
                    requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
                        .navigate(R.id.playlistDetailFragment, bundle)
                }
                binding.recyclerViewUserLikePlaylist.adapter = adapter
            }
        }

    }
}