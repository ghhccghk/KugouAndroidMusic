package com.ghhccghk.musicplay.ui.home

import SimpleFragmentPagerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ghhccghk.musicplay.databinding.FragmentHomeBinding
import com.ghhccghk.musicplay.databinding.ItemInputphoneBinding
import androidx.core.view.isVisible
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.ui.login.LoginPassWord
import com.ghhccghk.musicplay.ui.login.LoginQrcode
import com.ghhccghk.musicplay.util.TokenManager.isLoggedIn
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val login_tabLayout = binding.loginTabLayout
        val login_viewPager = binding.loginViewPager

        // 你的两个 Fragment
        val fragments = listOf(
            LoginPassWord(),
            LoginQrcode()
        )

        // 设置适配器
        val adapter = SimpleFragmentPagerAdapter(this, fragments)
        login_viewPager.adapter = adapter

        // 设置 Tab 标题
        val titles = listOf("密码登录", "二维码登录")
        TabLayoutMediator(login_tabLayout, login_viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        binding.apply {
            if (!isLoggedIn()) {
                statusIcon.setImageResource(R.drawable.ic_round_error_outline)
                statusTitle.text = getString(R.string.unactivated)
                statusSummary.text = getString(R.string.unactivated_summary)
                status.apply {
                    setBackgroundColor(
                        MaterialColors.getColor(
                            requireContext(),
                            android.R.attr.colorError,
                            Color.RED
                        )
                    )
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}