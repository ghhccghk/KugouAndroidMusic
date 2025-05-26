package com.ghhccghk.musicplay

import androidx.lifecycle.ViewModel
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture

class MainViewModel : ViewModel() {
    lateinit var controllerFuture: ListenableFuture<MediaController>
}
