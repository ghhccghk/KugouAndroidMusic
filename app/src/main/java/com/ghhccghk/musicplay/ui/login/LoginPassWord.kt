package com.ghhccghk.musicplay.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.databinding.FragmentHomeBinding
import com.ghhccghk.musicplay.databinding.FragmentLoginPasswordBinding
import com.ghhccghk.musicplay.ui.home.HomeViewModel
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPassWord : Fragment() {

    private var _binding: FragmentLoginPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentLoginPasswordBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val login_Pwd: Button = binding.loginPwd
        val login_Username: EditText = binding.loginUsername
        val login_Password: EditText = binding.loginPassword
        login_Pwd.setOnClickListener {
            if (MainActivity.isNodeRunning) {
                lifecycleScope.launch {
                    val json = withContext(Dispatchers.IO) {
                        KugouAPi.loginUserNameAndPassword(
                            login_Username.text.toString(),
                            login_Password.text.toString()
                        )
                    }
                    if (json == null || json == "502" || json == "404") {
                        Toast.makeText(context, "失败", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "成功", Toast.LENGTH_LONG).show()
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
}