package com.ghhccghk.musicplay.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.login.getLoginQr
import com.ghhccghk.musicplay.data.user.UserDetail
import com.ghhccghk.musicplay.data.user.likeplaylist.LikePlayListBase
import com.ghhccghk.musicplay.databinding.FragmentNotificationsBinding
import com.ghhccghk.musicplay.util.TokenManager
import com.ghhccghk.musicplay.util.adapte.playlist.UserLikePLayListAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        TokenManager.init(requireContext())
        KugouAPi.init()
        if (MainActivity.isNodeRunning) {
            setui()
            setUserPlayList()
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        setui()
    }


    fun setui(){
            lifecycleScope.launch {
                val gson = Gson()
                val json = withContext(Dispatchers.IO) {
                    val a = KugouAPi.getUserPlayList()
                    Log.d("test", a.toString())
                    KugouAPi.getUserDetail()
                }
                if (json == null || json == "502" || json == "404") {
                    binding.notLoggedIn.visibility = View.VISIBLE
                } else {
                    binding.layoutUserInfo.visibility = View.VISIBLE
                    val userinfo = gson.fromJson(json, UserDetail::class.java)
                    var data = userinfo.data
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault()) // 使用本地时区
                    binding.textNickname.text = data.nickname

                    val gen = if (data.gender == 1) "男" else if (data.gender == 0) "女" else "保密"
                    binding.textGenderGrade.text = gen + "｜等级：Lv" + data.p_grade.toString()
                    binding.textListTimeDuration.text = "听歌时长：" + data.duration + " 分钟"
                    binding.textLocation.text = data.province + " " +data.city
                    binding.textBirthday.text = "生日：" + data.birthday
                    binding.textOccupation.text = "职业：" + data.occupation
                    binding.textFans.text = data.fans.toString()
                    binding.textFollows.text = data.follows.toString()
                    binding.textVisitors.text = data.visitors.toString()
                    val utcMillis = data.logintime.toLong() * 1000
                    binding.textLastTime.text = "上次登录时间：" + formatter.format(Instant.ofEpochMilli(utcMillis))
                    val secureUrl = data.pic.replaceFirst("http://", "https://")

                    Glide.with(requireContext())
                        .load(secureUrl)
                        .into(binding.imageAvatar)
                }
            }

    }

    fun setUserPlayList(){
        lifecycleScope.launch {
            val gson = Gson()
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getUserPlayList()
            }
            if (json == null || json == "502" || json == "404") {
                Toast.makeText(requireContext(), "获取用户歌单失败", Toast.LENGTH_SHORT).show()
            } else {
                val userplaylist = gson.fromJson(json, LikePlayListBase::class.java)
                val data = userplaylist.data.info

                binding.recyclerViewUserLikePlaylist.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val adapter = UserLikePLayListAdapter(data)
                binding.recyclerViewUserLikePlaylist.adapter = adapter
            }
        }

    }
}