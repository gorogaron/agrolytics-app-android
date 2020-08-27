package com.agrolytics.agrolytics_android.koin
import com.agrolytics.agrolytics_android.ui.main.MainPresenter
import com.agrolytics.agrolytics_android.ui.rodSelector.RodSelectorPresenter
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    // Common
    single { SessionManager(androidContext()) }

    // Presenter
    factory { MainPresenter(androidContext()) }
    factory { RodSelectorPresenter(androidContext()) }

}