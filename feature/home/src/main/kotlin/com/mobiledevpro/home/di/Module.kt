package com.mobiledevpro.home.di

import com.mobiledevpro.home.ui.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val featureHomeModule = module {
    scope<HomeViewModel> {
        viewModelOf(::HomeViewModel)
    }
}
