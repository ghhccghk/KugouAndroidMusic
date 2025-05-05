package com.ghhccghk.musicplay.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ghhccghk.musicplay.databinding.FragmentHomeBinding
import com.ghhccghk.musicplay.databinding.ItemInputphoneBinding
import com.ghhccghk.musicplay.util.KugouAPi

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

        binding.listContainer.apply {
            val sendcode = ItemInputphoneBinding.inflate(LayoutInflater.from(context))
            addView(sendcode.root)
            val numberInput: EditText = sendcode.numberInput
            val sendcodeButton = sendcode.sendcode

            sendcodeButton.setOnClickListener {
                val text = numberInput.text.toString().trim()  // 去除空格
                Log.d("Debug", "用户输入的值: $text") // 检查是否正确获取到输入值
                val number = text.toLongOrNull()
                if (number != null) {
                    Thread {
                        KugouAPi.getMobileCode(number.toString())
                    }.start()
                } else {
                    Toast.makeText(this.context, "请输入有效数字", Toast.LENGTH_SHORT).show()
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