package com.agrolytics.agrolytics_android.data

import android.content.Context
import com.agrolytics.agrolytics_android.data.firebase.FireBaseDataClient
import com.agrolytics.agrolytics_android.data.local.LocalDataClient


class DataClient(context: Context) {
    val local: LocalDataClient = LocalDataClient(context)
    val fireBase: FireBaseDataClient = FireBaseDataClient()
}