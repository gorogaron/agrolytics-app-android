package com.agrolytics.agrolytics_android.koin

import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedPresenter
import com.agrolytics.agrolytics_android.ui.login.LoginPresenter
import com.agrolytics.agrolytics_android.ui.main.MainPresenter
import com.agrolytics.agrolytics_android.ui.rodSelector.RodSelectorPresenter
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {

    // Common
    single { AppServer() }
    single { SessionManager(androidContext()) }
    single { FireStoreDB() }

    // Views
    single { UploadFinishedActivity() }

    // Presenter
    factory { MainPresenter(androidContext()) }
    factory { UploadFinishedPresenter() }
    factory { LoginPresenter(androidContext()) }
    factory { RodSelectorPresenter(androidContext()) }

}