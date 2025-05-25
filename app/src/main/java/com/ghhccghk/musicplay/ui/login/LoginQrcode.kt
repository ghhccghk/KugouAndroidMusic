package com.ghhccghk.musicplay.ui.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.login.QrImg
import com.ghhccghk.musicplay.data.login.getLoginQr
import com.ghhccghk.musicplay.databinding.FragmentLoginQrBinding
import com.ghhccghk.musicplay.util.Tools.generateQRCode
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.data.login.QrLoginkey
import com.ghhccghk.musicplay.util.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginQrcode: Fragment() {

    private var _binding: FragmentLoginQrBinding? = null
    private val binding get() = _binding!!
    private var key : String? = ""
    private var s : Int = 11
    val handler = Handler(Looper.getMainLooper())
    val updateRunnable = object : Runnable {
        override fun run() {
            if (s == 4){
                handler.removeCallbacks(this)
            }
            if (s == 0){
                setui()
            }
            update()
            handler.postDelayed(this, 1100)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginQrBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (MainActivity.isNodeRunning) {
            setui()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        handler.post(updateRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }
    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    fun setui() {
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                val a = KugouAPi.getQrCodekey()
                val gson = Gson()
                val result = gson.fromJson(a, getLoginQr::class.java)
                val img = result.data.qrcode
                key = result.data.qrcode
                KugouAPi.getQrCode(img)
            }
            if (json == null || json == "502" || json == "404") {
                Toast.makeText(context, "失败", Toast.LENGTH_LONG).show()
            } else {
                try {
                    val gson = Gson()
                    val result = gson.fromJson(json, QrImg::class.java)
                    val img = result.data.url
                    Log.d("a", result.data.url)
                    binding.qrimage.setImageBitmap(generateQRCode(img, 512))
                    update()

                } catch (e: Exception) {
                    e.printStackTrace()
                    s = 0
                    Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }

    }



    fun update(){
        TokenManager.init(requireContext())
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                key?.let { KugouAPi.getQrCodeCheck(it, System.currentTimeMillis().toString()) }
            }
            if (json == null || json == "502" || json == "404") {
                Toast.makeText(context, "失败", Toast.LENGTH_LONG).show()
            } else {
                val gson = Gson()
                val result = gson.fromJson(json, QrLoginkey::class.java)
                when {
                    result.data.status == 4 -> {
                        TokenManager.saveToken(result.data.token)
                        TokenManager.saveUserId(result.data.userid.toString())
                        Toast.makeText(context, "登录成功", Toast.LENGTH_LONG).show()
                        s = 4
                    }
                    result.data.status == 0 -> {
                        if (s != 0) {
                            Toast.makeText(context, "二维码已过期", Toast.LENGTH_LONG).show()
                            s = 0
                        }

                    }
                    result.data.status == 1 -> {
                        if (s != 1) {
                            Toast.makeText(context, "等待扫码", Toast.LENGTH_LONG).show()
                            s = 1
                        }
                    }
                    result.data.status == 2 -> {
                        if (s != 2) {
                            Toast.makeText(context, "等待确认", Toast.LENGTH_LONG).show()
                            s = 2
                        }
                    }
                }
            }

        }
    }

}