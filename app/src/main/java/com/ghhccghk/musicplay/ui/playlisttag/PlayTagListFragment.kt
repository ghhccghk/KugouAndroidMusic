package com.ghhccghk.musicplay.ui.playlisttag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghhccghk.musicplay.R
import com.ghhccghk.musicplay.data.PlayListTag
import com.ghhccghk.musicplay.util.adapte.playlist.PlaylistTagAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlayTagListFragment: Fragment() {


    private lateinit var tags: List<PlayListTag> // 你需要传进来的 son 列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tags = Gson().fromJson(requireArguments().getString("tags_json"), object : TypeToken<List<PlayListTag>>() {}.type)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_playlist_tag, container, false).apply {
            val recyclerView = findViewById<RecyclerView>(R.id.tag_recycler_view)
            recyclerView.layoutManager = GridLayoutManager(context, 3)
            recyclerView.adapter = PlaylistTagAdapter(tags)
        }
    }

    companion object {
        fun newInstance(tags: List<PlayListTag>): PlayTagListFragment {
            val fragment = PlayTagListFragment()
            val bundle = Bundle()
            bundle.putString("tags_json", Gson().toJson(tags))
            fragment.arguments = bundle
            return fragment
        }
    }

}