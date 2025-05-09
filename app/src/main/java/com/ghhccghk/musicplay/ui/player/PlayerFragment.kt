package com.ghhccghk.musicplay.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.databinding.FragmentPlayerBinding
import com.google.android.material.button.MaterialButton

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.slideDown.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.lyrics.setOnClickListener {
            findNavController().navigate(R.id.lyricFragment)

        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }




}