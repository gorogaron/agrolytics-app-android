package com.agrolytics.agrolytics_android.ui.images

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.jetbrains.anko.doAsync
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class ImagesViewModel: ViewModel(), KoinComponent {

    private val dataClient: DataClient by inject()
    private val sessionManager: SessionManager by inject()

    var sessionItems = MutableLiveData<List<ImagesActivity.SessionItem>>()

    fun getSessionItems() {
        val sessionIds = getSessionIdList()

        /** Kedves Józsi!
         *
         * Itt a sessionIds-ben van az összes session ID. Minden sessionhöz ezeket kell meghatározni a megjelenítéshez:
         * -hossz : Ha a session alatt lévő összes itemben a wood_length megegyezik, akkor az az érték, egyébként -1
         * -fajta : Ha a session alatt lévő összes itemben a wood_volume megegyezik, akkor az az érték, egyébként -1
         * -térfogat : Ha a session alatt nincs unprocessedImageItem akkor a session alatt lévő képek össztérfogata, egyébként -1
         * -dátum : A session első képének a dátuma
         *
         * Ezekből a paraméterekből ImagesActivity.SessionItem objektumokat kell csinálni, és beletenni őket a sessionItems LiveData-ba.
         * Ez alapján fogjuk megjeleníteni a sessionök listáját.
         *
         * Felebaráti szeretettel,
         * Áron
        */
    }

    fun getSessionIdList() : ArrayList<String> {
        val sessionIdListUnprocessed = dataClient.local.unprocessed.getAllSessionIds()
        val sessionIdListProcessed = dataClient.local.unprocessed.getAllSessionIds()
        val sessionIdListCached = dataClient.local.unprocessed.getAllSessionIds()

        val sessionIdList = ArrayList<String>()
        sessionIdList.addAll(sessionIdListCached)
        sessionIdList.addAll(sessionIdListProcessed)
        sessionIdList.addAll(sessionIdListUnprocessed)
        return ArrayList(sessionIdList.distinct())
    }
}