package com.ghhccghk.musicplay.ui.playlisttag

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.ghhccghk.musicplay.data.PlayListTag
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class PlayTagListFragment : Fragment() {

    private lateinit var tags: List<PlayListTag>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, PlayListTag::class.java)
        tags = moshi.adapter<List<PlayListTag>>(type)
            .fromJson(requireArguments().getString("tags_json")!!)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(LocalContext.current)
                    else dynamicLightColorScheme(LocalContext.current)
                } else {
                    if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
                }

                MaterialTheme(colorScheme = colorScheme) {
                    PlayTagListScreen(tags = tags)
                }
            }
        }
    }

    companion object {
        fun newInstance(tags: List<PlayListTag>): PlayTagListFragment {
            val fragment = PlayTagListFragment()
            val bundle = Bundle()
            bundle.putString(
                "tags_json",
                Moshi.Builder().build().adapter<List<PlayListTag>>(
                    Types.newParameterizedType(
                        List::class.java,
                        PlayListTag::class.java
                    )
                ).toJson(tags)
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}

@Composable
fun PlayTagListScreen(
    tags: List<PlayListTag>,
    onTagClick: (PlayListTag) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tags) { tag ->
            Button(
                onClick = { onTagClick(tag) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = tag.tag_name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        }
    }
}
