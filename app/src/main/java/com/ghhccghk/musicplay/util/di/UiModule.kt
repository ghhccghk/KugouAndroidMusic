package com.ghhccghk.musicplay.util.di

import com.ghhccghk.musicplay.ui.components.share.ShareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel {
        ShareViewModel()
    }
}