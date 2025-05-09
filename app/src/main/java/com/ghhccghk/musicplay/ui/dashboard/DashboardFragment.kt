package com.ghhccghk.musicplay.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.data.HotSearchResponse
import com.ghhccghk.musicplay.data.KeywordGroup
import com.ghhccghk.musicplay.data.KeywordItem
import com.ghhccghk.musicplay.data.ThemeMusicList
import com.ghhccghk.musicplay.databinding.FragmentDashboardBinding
import com.ghhccghk.musicplay.util.KugouAPi
import com.ghhccghk.musicplay.util.hotsearch.HotGroupAdapter
import com.ghhccghk.musicplay.util.playlist.PlayMusicSceneAdapter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (MainActivity.isNodeRunning) {
            addgetSearchhotView()
            addgetPlayListTheme()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.isNodeRunning) {
            addgetSearchhotView()
            addgetPlayListTheme()

        }
    }

    fun addgetSearchhotView(){
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSearchhot()
            }


            val gson = Gson()
            val result = gson.fromJson(json, HotSearchResponse::class.java)
            val groups = result.data.list.map { listItem ->
                Log.d("debug",listItem.name)
                KeywordGroup(
                    name = listItem.name,
                    keywords = listItem.keywords.take(5).mapIndexed { index,keywordItem ->
                        KeywordItem(keyword = "${index + 1}. ${keywordItem.keyword}", keywordItem.reason)
                    }
                )
            }

            binding.testaaa.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.testaaa.adapter = HotGroupAdapter(groups) { keywordItem ->
                // 在这里处理点击事件
                // 你可以对点击的 KeywordItem 进行任何操作，例如显示详细信息或跳转页面
                Toast.makeText(context, "点击了：${keywordItem.keyword}", Toast.LENGTH_SHORT).show()
            }
            binding.testaaa.isNestedScrollingEnabled = false
        }

    }

    fun addgetPlayListTheme(){
        lifecycleScope.launch {
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getPlayListTheme()
            }
            val gson = Gson()
            val result = gson.fromJson(json, ThemeMusicList::class.java)
            val themeList = result.data?.theme_list ?: emptyList()
            val groups =  PlayMusicSceneAdapter(themeList) { keywordItem ->
                // 在这里处理点击事件
                // 你可以对点击的 KeywordItem 进行任何操作，例如显示详细信息或跳转页面
                Toast.makeText(context, "点击了：${keywordItem.title}", Toast.LENGTH_SHORT).show()
            }

            binding.playmusicscene.layoutManager = GridLayoutManager(context, 2, RecyclerView.HORIZONTAL, false)
            binding.playmusicscene.adapter = groups
            binding.playmusicscene.isNestedScrollingEnabled = false


        }
    }
}