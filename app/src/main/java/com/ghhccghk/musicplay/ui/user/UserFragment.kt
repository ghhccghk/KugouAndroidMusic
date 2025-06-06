package com.ghhccghk.musicplay.ui.user

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.updateToken.updateToken
import com.ghhccghk.musicplay.data.user.UserDetail
import com.ghhccghk.musicplay.data.user.likeplaylist.LikePlayListBase
import com.ghhccghk.musicplay.databinding.FragmentUserBinding
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
import kotlin.jvm.java

class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userViewModel =
            ViewModelProvider(this).get(UserViewModel::class.java)

        _binding = FragmentUserBinding.inflate(inflater, container, false)
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
        if (MainActivity.isNodeRunning) {
            setui()
            setUserPlayList()
        }
    }


    fun setui() {
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

                val gen = when (data.gender) {
                    1 -> getString(R.string.gender_male)
                    0 -> getString(R.string.gender_female)
                    else -> getString(R.string.gender_secret)
                }
                binding.userGender.text = gen + " "
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

                    }
                    requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
                        .navigate(R.id.playlistDetailFragment, bundle)
                }
                binding.recyclerViewUserLikePlaylist.adapter = adapter
            }
        }

    }
}