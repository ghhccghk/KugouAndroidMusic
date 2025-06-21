package com.ghhccghk.musicplay.ui.home

import com.ghhccghk.musicplay.util.adapte.LogicFragmentPagerAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ghhccghk.musicplay.databinding.FragmentHomeBinding
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.ui.login.LoginPassWord
import com.ghhccghk.musicplay.ui.login.LoginQrcode
import com.ghhccghk.musicplay.util.NodeBridge
import com.ghhccghk.musicplay.util.TokenManager.isLoggedIn
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val nodeReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == NodeBridge.ACTION_NODE_READY) {
                if (_binding != null ){
                    if (!isLoggedIn()) {
                        binding.statusIcon.setImageResource(R.drawable.ic_round_error_outline)
                        binding.statusTitle.text = getString(R.string.unactivated)
                        binding.statusSummary.text = getString(R.string.unactivated_summary)
                        binding.status.apply {
                            setBackgroundColor(
                                MaterialColors.getColor(
                                    requireContext(),
                                    android.R.attr.colorError,
                                    Color.RED
                                )
                            )
                        }
                    } else {
                        binding.statusIcon.setImageResource(R.drawable.ic_round_check_circle)
                        binding.statusTitle.text = getString(R.string.activated)
                        binding.status.apply {
                            setBackgroundColor(
                                MaterialColors.getColor(
                                    requireContext(),
                                    com.google.android.material.R.attr.colorPrimary,
                                    com.google.android.material.R.attr.colorPrimary
                                )
                            )
                        }

                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val filter = IntentFilter(NodeBridge.ACTION_NODE_READY)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(nodeReadyReceiver, filter)

        val login_tabLayout = binding.loginTabLayout
        val login_viewPager = binding.loginViewPager

        // 你的两个 Fragment
        val fragments = listOf(
            LoginPassWord(),
            LoginQrcode()
        )

        if (!isLoggedIn()) {        // 设置适配器
            val adapter = LogicFragmentPagerAdapter(this, fragments)
            login_viewPager.adapter = adapter

            // 设置 Tab 标题
            val titles = listOf("密码登录", "二维码登录")
            TabLayoutMediator(login_tabLayout, login_viewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }

        binding.apply {
            if (MainActivity.isNodeRunning) {
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
                } else {
                    statusIcon.setImageResource(R.drawable.ic_round_check_circle)
                    statusTitle.text = getString(R.string.activated)

                }
            } else {
                statusIcon.setImageResource(R.drawable.ic_round_error_outline)
                statusTitle.text = getString(R.string.api_no_ok)
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