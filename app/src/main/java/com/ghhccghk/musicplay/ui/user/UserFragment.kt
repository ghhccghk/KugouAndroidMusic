package com.ghhccghk.musicplay.ui.user

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.user.UserDetail
import com.ghhccghk.musicplay.data.user.likeplaylist.LikePlayListBase
import com.ghhccghk.musicplay.databinding.FragmentUserBinding
import com.ghhccghk.musicplay.ui.setting.MainSettingsActivity
import com.ghhccghk.musicplay.util.TokenManager
import com.ghhccghk.musicplay.util.TokenManager.isLoggedIn
import com.ghhccghk.musicplay.util.adapte.playlist.UserLikePLayListAdapter
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
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
        lifecycleScope.launch {
            if (today != vipupdate) {
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
                prefs.edit { putString("vipupdate", today) }
            }
        }

        val root: View = binding.root
        binding.toolbar.inflateMenu(R.menu.toolbar_menu_user)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_update_token -> {
                    lifecycleScope.launch {
                        if (today != vipupdate) {
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
                            prefs.edit { putString("vipupdate", today) }
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
        if (MainActivity.isNodeRunning && isLoggedIn()) {
            setui()
            setUserPlayList()
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
            setui()
            setUserPlayList()
        }
    }


    @SuppressLint("SetTextI18n")
    fun setui() {
        lifecycleScope.launch {
            val gson = Gson()
            val json = withContext(Dispatchers.IO) {
                val a = KugouAPi.getUserPlayList()
                KugouAPi.getUserDetail()
            }
            if (json == null || json == "502" || json == "404") {
                binding.notLoggedIn.visibility = View.VISIBLE
                binding.layoutUserInfo.visibility = View.GONE
                binding.userLikePlaylistViewBase.visibility = View.GONE
            } else {
                binding.layoutUserInfo.visibility = View.VISIBLE
                val userinfo = gson.fromJson(json, UserDetail::class.java)
                var data = userinfo.data
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault()) // 使用本地时区
                binding.textNickname.text = data.nickname

                val gen = when (data.gender) {
                    1 -> getString(R.string.gender_male)
                    0 -> getString(R.string.gender_female)
                    else -> getString(R.string.gender_secret)
                }
                binding.userGender.text = "$gen "
                binding.textGrade.text = " " + data.p_grade.toString()
                binding.textListTimeDuration.text = data.duration.toString()
                binding.textLocation.text = data.province + " " + data.city
                binding.textBirthday.text = data.birthday
                binding.textOccupation.text = data.occupation
                binding.textFans.text = data.fans.toString()
                binding.textFollows.text = data.follows.toString()
                binding.textVisitors.text = data.visitors.toString()
                val utcMillis = data.logintime.toLong() * 1000
                binding.textLastTime.text = formatter.format(Instant.ofEpochMilli(utcMillis))
                val secureUrl = data.pic.replaceFirst("http://", "https://")

                Glide.with(requireContext())
                    .load(secureUrl)
                    .into(binding.imageAvatar)
            }
        }

    }

    fun setUserPlayList() {
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