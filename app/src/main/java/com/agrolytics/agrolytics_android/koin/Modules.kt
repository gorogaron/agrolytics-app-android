package com.agrolytics.agrolytics_android.koin

import com.agrolytics.agrolytics_android.database.DataClient
import com.agrolytics.agrolytics_android.database.firestore.FireStoreDB
import com.agrolytics.agrolytics_android.database.local.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.ui.measurement.presenter.ApproveMeasurementPresenter
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
    single { DataClient(androidContext()) }

    // Views
    single { ApproveMeasurementActivity() }

    // Presenter
    factory { MainPresenter(androidContext()) }
    factory { ImagesPresenter(androidContext()) }
    factory { ApproveMeasurementPresenter() }
    factory { MapPresenter() }
    factory { LoginPresenter(androidContext()) }
    factory {
        RodSelectorPresenter(
            androidContext()
        )
    }

}