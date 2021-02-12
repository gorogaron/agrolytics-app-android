package com.agrolytics.agrolytics_android.database

import android.content.Context
import com.agrolytics.agrolytics_android.database.firestore.FireStoreDataClient
import com.agrolytics.agrolytics_android.database.local.LocalDataClient


class DataClient(context: Context) {
    val local: LocalDataClient = LocalDataClient(context)
    val fireStore: FireStoreDataClient = FireStoreDataClient()
}