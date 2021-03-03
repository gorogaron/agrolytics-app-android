package com.agrolytics.agrolytics_android.koin

import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.ui.measurement.presenter.ApproveMeasurementPresenter
import com.agrolytics.agrolytics_android.ui.login.LoginPresenter
import com.agrolytics.agrolytics_android.ui.measurement.activity.SessionActivity
import com.agrolytics.agrolytics_android.ui.measurement.presenter.RodSelectorPresenter
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // Common
    single { AppServer() }
    single { SessionManager(androidContext()) }
    single { DataClient(androidContext()) }

    // Views
    single { ApproveMeasurementActivity() }
    single { SessionActivity() }

    // Presenter
    factory { ApproveMeasurementPresenter() }
    factory { LoginPresenter(androidContext()) }
    factory {
        RodSelectorPresenter(
            androidContext()
        )
    }

}