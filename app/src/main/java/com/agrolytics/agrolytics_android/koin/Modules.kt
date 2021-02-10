package com.agrolytics.agrolytics_android.koin

import com.agrolytics.agrolytics_android.database.firebase.FireStoreDB
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.measurement.activity.UploadFinishedActivity
import com.agrolytics.agrolytics_android.ui.measurement.presenter.UploadFinishedPresenter
import com.agrolytics.agrolytics_android.ui.images.ImagesPresenter
import com.agrolytics.agrolytics_android.ui.login.LoginPresenter
import com.agrolytics.agrolytics_android.ui.main.MainPresenter
import com.agrolytics.agrolytics_android.ui.map.MapPresenter
import com.agrolytics.agrolytics_android.ui.measurement.presenter.RodSelectorPresenter
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // Common
    single { AppServer() }
    single { SessionManager(androidContext()) }
    single { RoomModule(androidContext()) }
    single { FireStoreDB() }

    // Views
    single { UploadFinishedActivity() }

    // Presenter
    factory { MainPresenter(androidContext()) }
    factory { ImagesPresenter(androidContext()) }
    factory { UploadFinishedPresenter() }
    factory { MapPresenter() }
    factory { LoginPresenter(androidContext()) }
    factory {
        RodSelectorPresenter(
            androidContext()
        )
    }

}