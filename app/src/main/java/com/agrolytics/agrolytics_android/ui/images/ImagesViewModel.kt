package com.agrolytics.agrolytics_android.ui.images

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.utils.SessionManager
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
        val sessionItemList = ArrayList<ImagesActivity.SessionItem>()

        // Minden session-höz előállítjuk az item-eket
        for (sessionId in sessionIds) {
            // SessionItem-ek alapméretezett attribútumainak inicializálása
            var woodLength = -1.0
            var woodVolume = -1.0
            var woodType = ""

            // Kigyűjtött értékek listái
            val woodLengths = ArrayList<Double>()
            val woodVolumes = ArrayList<Double>()
            val woodTypes = ArrayList<String>()

            // Egy adott session-höz lekérdezzük az összes típusú item-eket
            val cachedImages = dataClient.local.cache.getBySessionId(sessionId)
            val processedImages = dataClient.local.processed.getBySessionId(sessionId)
            val unprocessedImages = dataClient.local.unprocessed.getBySessionId(sessionId)

            for (cachedImage in cachedImages) {
                woodLengths.add(cachedImage.woodLength)
                woodTypes.add(cachedImage.woodType)
                woodVolumes.add(cachedImage.woodVolume)
            }

            for (processedImage in processedImages) {
                woodLengths.add(processedImage.woodLength)
                woodTypes.add(processedImage.woodType)
                woodVolumes.add(processedImage.woodVolume)
            }

            for (unprocessedImage in unprocessedImages) {
                woodLengths.add(unprocessedImage.woodLength)
                woodTypes.add(unprocessedImage.woodType)
            }

            if (woodLengths.distinct().size == 1) {
                woodLength = woodLengths[0]
            }

            if (woodTypes.distinct().size == 1) {
                woodType = woodTypes[0]
            }

            if (unprocessedImages.isNotEmpty()) {
                woodVolume = woodVolumes.sum()
            }

            // SessionItem hozzáadása a listához
            sessionItemList.add(ImagesActivity.SessionItem(
                woodLength = woodLength,
                woodType = woodType,
                woodVolume = woodVolume,
                timestamp = sessionId))
        }
        sessionItems.value = sessionItemList
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

    private fun getSessionIdList() : ArrayList<String> {
        val sessionIdListUnprocessed = dataClient.local.unprocessed.getAllSessionIds()
        val sessionIdListProcessed = dataClient.local.processed.getAllSessionIds()
        val sessionIdListCached = dataClient.local.cache.getAllSessionIds()

        val sessionIdList = ArrayList<String>()
        sessionIdList.addAll(sessionIdListCached)
        sessionIdList.addAll(sessionIdListProcessed)
        sessionIdList.addAll(sessionIdListUnprocessed)
        return ArrayList(sessionIdList.distinct())
    }
}