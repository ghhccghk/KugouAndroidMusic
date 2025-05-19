package com.ghhccghk.musicplay.ui.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.getLyricCode
import com.ghhccghk.musicplay.data.login.QrImg
import com.ghhccghk.musicplay.data.login.getLoginQr
import com.ghhccghk.musicplay.databinding.FragmentLoginQrBinding
import com.ghhccghk.musicplay.util.Tools.generateQRCode
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class LoginQrcode: Fragment() {

    private var _binding: FragmentLoginQrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginQrBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val imageView = binding.qrimage
        if (MainActivity.isNodeRunning) {
            lifecycleScope.launch {
                val key : String
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
                        imageView.setImageBitmap(generateQRCode(img, 512))

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun urlToBitmap(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}