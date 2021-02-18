package com.agrolytics.agrolytics_android.data

import android.content.Context
import com.agrolytics.agrolytics_android.data.firestore.FireBaseDataClient
import com.agrolytics.agrolytics_android.data.database.LocalDataClient


class DataClient(context: Context) {
    val local: LocalDataClient = LocalDataClient(context)
    val fireStore: FireBaseDataClient = FireBaseDataClient()
}