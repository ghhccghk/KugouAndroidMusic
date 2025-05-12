package com.ghhccghk.musicplay.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.MainActivity
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.HotSearchResponse
import com.ghhccghk.musicplay.data.KeywordGroup
import com.ghhccghk.musicplay.data.KeywordItem
import com.ghhccghk.musicplay.data.PlayCategory
import com.ghhccghk.musicplay.data.PlayCategoryBase
import com.ghhccghk.musicplay.data.SearchBase
import com.ghhccghk.musicplay.data.SearchData
import com.ghhccghk.musicplay.data.ThemeMusicList
import com.ghhccghk.musicplay.databinding.FragmentDashboardBinding
import com.ghhccghk.musicplay.util.apihelp.KugouAPi
import com.ghhccghk.musicplay.util.hotsearch.HotGroupAdapter
import com.ghhccghk.musicplay.util.playlist.PLayListCategoryPagerAdapter
import com.ghhccghk.musicplay.util.playlist.PlayMusicSceneAdapter
import com.ghhccghk.musicplay.util.search.RecommendationAdapter
import com.google.android.material.tabs.TabLayoutMediator
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
            addsearch()
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
            addgetPlayListTag()
        }
    }

    fun addgetSearchhotView() {
        lifecycleScope.launch {
            val loadView = binding.loadingLayoutTestaaa
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getSearchhot()
            }

            if (json == null || json == "502" || json == "404") {
                loadView.loadingSpinner.visibility = View.GONE
                loadView.retryButton.visibility = View.VISIBLE
                loadView.retryButton.setOnClickListener {
                    addgetSearchhotView()
                }
            } else {
                try {
                    val gson = Gson()
                    val result = gson.fromJson(json, HotSearchResponse::class.java)
                    val groups = result.data.list.map { listItem ->
                        KeywordGroup(
                            name = listItem.name,
                            keywords = listItem.keywords.take(5).mapIndexed { index, keywordItem ->
                                KeywordItem(
                                    keyword = "${index + 1}. ${keywordItem.keyword}",
                                    keywordItem.reason
                                )
                            }
                        )
                    }

                    binding.testaaa.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    binding.testaaa.adapter = HotGroupAdapter(groups) { keywordItem ->
                        // 在这里处理点击事件
                        // 你可以对点击的 KeywordItem 进行任何操作，例如显示详细信息或跳转页面
                        binding.searchView.show()
                        binding.searchView.editText.setText(keywordItem.reason)
                        binding.searchView.editText.setSelection(keywordItem.reason.length)
                    }
                    binding.testaaa.isNestedScrollingEnabled = false
                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.loadingFooterLayout.visibility = View.GONE
                    binding.testaaa.visibility = View.VISIBLE

                } catch (e: Exception) {
                    e.printStackTrace()
                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.retryButton.visibility = View.VISIBLE
                    Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    fun addgetPlayListTheme() {
        lifecycleScope.launch {
            val loadView = binding.loadingPlaymusicscene
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getPlayListTheme()
            }
            if (json == null || json == "502" || json == "404") {
                loadView.retryButton.visibility = View.VISIBLE
                loadView.loadingSpinner.visibility = View.GONE
                loadView.retryButton.setOnClickListener {
                    addgetPlayListTheme()
                }
            } else {
                try {
                    val gson = Gson()
                    val result = gson.fromJson(json, ThemeMusicList::class.java)
                    val themeList = result.data?.theme_list ?: emptyList()

                    val adapter = PlayMusicSceneAdapter(themeList) { item ->
                        Toast.makeText(context, "点击了：${item.title}", Toast.LENGTH_SHORT).show()
                    }

                    binding.playmusicscene.layoutManager =
                        GridLayoutManager(context, 2, RecyclerView.HORIZONTAL, false)
                    binding.playmusicscene.adapter = adapter
                    binding.playmusicscene.isNestedScrollingEnabled = false
                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.loadingFooterLayout.visibility = View.GONE
                    binding.playmusicscene.visibility = View.VISIBLE

                } catch (e: Exception) {
                    e.printStackTrace()
                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.retryButton.visibility = View.VISIBLE
                    Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                }

            }


        }
    }

    fun addgetPlayListTag() {
        lifecycleScope.launch {
            val loadView = binding.loadingPlaylist
            val json = withContext(Dispatchers.IO) {
                KugouAPi.getPlayListTag()
            }
            if (json == null || json == "502" || json == "404") {
                loadView.retryButton.visibility = View.VISIBLE
                loadView.loadingSpinner.visibility = View.GONE
                loadView.retryButton.setOnClickListener {
                    addgetPlayListTag()
                }
            } else {
                try {
                    val gson = Gson()
                    val result = gson.fromJson(json, PlayCategoryBase::class.java)
                    val themeList = result.data
                    val adapter = PLayListCategoryPagerAdapter(requireActivity(), themeList)

                    val viewPager = binding.playlistTabViewPager
                    val tabLayout = binding.playlistTabLayout

                    viewPager.adapter = adapter

                    TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                        tab.text = themeList[position].tag_name
                    }.attach()


                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.loadingFooterLayout.visibility = View.GONE
                    viewPager.visibility = View.VISIBLE
                    tabLayout.visibility = View.VISIBLE

                } catch (e: Exception) {
                    e.printStackTrace()
                    loadView.loadingSpinner.visibility = View.GONE
                    loadView.retryButton.visibility = View.VISIBLE
                    Toast.makeText(context, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                }

            }


        }
    }

    fun addsearch(){
        val bar = binding.searchBar
        val view = binding.searchView

        view.setupWithSearchBar(bar)
        bar.setupWithNavController(requireActivity().findNavController(R.id.nav_host_fragment_activity_main ))

        // 输入监听
        view.editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = view.text.toString()
                Toast.makeText(context, "搜索: $query", Toast.LENGTH_SHORT).show()
                true
            } else {
                false
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (view.isShowing) {
                    view.hide()
                } else {
                    // 禁用当前 callback，让系统处理返回栈
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        // 或监听文字变化（实时）
        view.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    // 实时搜索或过滤推荐
                    lifecycleScope.launch {
                        val json = withContext(Dispatchers.IO) {
                            KugouAPi.getSearchSuggest(query)
                        }
                        if (json == null || json == "502" || json == "404") {
                            Toast.makeText(context, "数据加载失败", Toast.LENGTH_LONG).show()
                        } else {
                            try {
                                val gson = Gson()
                                val result = gson.fromJson(json, SearchBase::class.java)

                                val match = result.data.first()

                                println(match)

                                match?.let { item ->
                                    val adapter = RecommendationAdapter(item.RecordDatas) {
                                        view.show()
                                        view.editText.setText(it.HintInfo)
                                        view.editText.setSelection(it.HintInfo.length)
                                    }
                                    binding.searchRecommendationList.apply {
                                        layoutManager = LinearLayoutManager(context)
                                        this.adapter = adapter
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    "数据加载失败: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    }
                }

            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // 拦截返回键
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)
    }
}