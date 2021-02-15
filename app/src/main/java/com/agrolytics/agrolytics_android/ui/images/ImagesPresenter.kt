package com.agrolytics.agrolytics_android.ui.images

import android.content.Context
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem
import com.agrolytics.agrolytics_android.ui.base.BasePresenter


class ImagesPresenter(val context: Context) : BasePresenter<ImagesScreen>() {

    var isProcessed = true
    private var activity: ImagesActivity? = null
    var fireBaseList = arrayListOf<CachedImageItem>()

    fun setActivity(activity: ImagesActivity) {
        this.activity = activity
    }

//    fun subscribeForImageEvents() {
//        when (sessionManager?.userRole) {
//            "admin" -> subscribeAdminImages()
//            "leader" -> subscribeLeaderImages()
//            "worker" -> subscribeWorkerImages()
//        }
//    }

//    private fun subscribeAdminImages() {
//        screen?.showLoading()
//        fireStoreDB?.db?.collection("images")
//            ?.whereEqualTo("forestryID", sessionManager?.forestryID)
//            ?.addSnapshotListener { value, e ->
//                if (e != null) {
//                    e.printStackTrace()
//                    return@addSnapshotListener
//                }
//
//                if (fireBaseList.isEmpty()) {
//                    for (document in value!!) {
//                        val imageItem = createImageItem(document)
//                        saveImage(imageItem)
//                        fireBaseList.add(imageItem)
//                    }
//                } else {
//                    for (document in value!!.documentChanges) {
//                        val imageItem = createImageItem(document.document)
//                        val alreadyContains = fireBaseList.filter { it.id == imageItem.id }.isNotEmpty()
//                        if (!alreadyContains) {
//                            saveImage(imageItem)
//                            fireBaseList.add(imageItem)
//                        }
//                    }
//                }
//                loadImagesFromFireBase()
//            }
//    }
//
//
//    private fun subscribeLeaderImages() {
//        screen?.showLoading()
//        getLeaderImages()
//    }
//
//    private fun getLeaderImages() {
//        fireStoreDB?.db?.collection("images")
//            ?.whereEqualTo("leaderID", sessionManager?.userID)
//            ?.get()?.addOnSuccessListener {
//                for (document in it) {
//                    val imageItem = createImageItem(document)
//                    val alreadyContains = fireBaseList.filter { it.id == imageItem.id }.isNotEmpty()
//                    if (!alreadyContains) {
//                        saveImage(imageItem)
//                        fireBaseList.add(imageItem)
//                    }
//                }
//                fireStoreDB?.db?.collection("images")
//                    ?.whereEqualTo("userID", sessionManager?.userID)
//                    ?.get()?.addOnSuccessListener {
//                        for (document in it) {
//                            val imageItem = createImageItem(document)
//                            val alreadyContains = fireBaseList.filter { it.id == imageItem.id }.isNotEmpty()
//                            if (!alreadyContains) {
//                                saveImage(imageItem)
//                                fireBaseList.add(imageItem)
//                            }
//                        }
//                        fireStoreDB?.db?.collection("images")
//                            ?.whereEqualTo("leaderID", sessionManager?.userID)
//                            ?.addSnapshotListener { value, e ->
//                                if (e != null) {
//                                    e.printStackTrace()
//                                    return@addSnapshotListener
//                                }
//
//                                if (value!!.documentChanges.size == 1) {
//                                    for (document in value.documentChanges) {
//                                        val imageItem = createImageItem(document.document)
//                                        val alreadyContains = fireBaseList.filter { it.id == imageItem.id }.isNotEmpty()
//                                        if (!alreadyContains) {
//                                            saveImage(imageItem)
//                                            fireBaseList.add(imageItem)
//                                        }
//                                    }
//                                }
//                            }
//                        loadImagesFromFireBase()
//                    }
//            }
//    }
//
//    private fun subscribeWorkerImages() {
//        screen?.showLoading()
//        fireStoreDB?.db?.collection("images")
//            ?.whereEqualTo("userID", sessionManager?.userID)
//            ?.addSnapshotListener { value, e ->
//                if (e != null) {
//                    e.printStackTrace()
//                    return@addSnapshotListener
//                }
//
//                if (fireBaseList.isEmpty()) {
//                    for (document in value!!) {
//                        val imageItem = createImageItem(document)
//                        saveImage(imageItem)
//                        fireBaseList.add(imageItem)
//                    }
//                } else {
//                    for (document in value!!.documentChanges) {
//                        val imageItem = createImageItem(document.document)
//                        val alreadyContains = fireBaseList.filter { it.id == imageItem.id }.isNotEmpty()
//                        if (!alreadyContains) {
//                            saveImage(imageItem)
//                            fireBaseList.add(imageItem)
//                        }
//                    }
//                }
//                loadImagesFromFireBase()
//            }
//    }
//
//
//    fun getImages(isPushedToServer: Boolean) {
//        var images: List<ImageItem>
//        doAsync {
//            dataClient?.local?.getAllPushedImages(isPushedToServer)!!.let {
//                images = it
//                uiThread {
//                    screen?.loadImages(ArrayList(images))
//                    screen?.hideLoading()
//                }
//            } ?: run {
//                uiThread {
//                    screen?.loadImages(arrayListOf())
//                    screen?.hideLoading()
//                }
//            }
//        }
//    }
//
//    fun loadImagesFromFireBase() {
//        if (Util.isNetworkAvailable()) {
//            screen?.hideLoading()
//            Collections.sort(fireBaseList, object : Comparator<ImageItem> {
//                override fun compare(s1: ImageItem, s2: ImageItem): Int {
//                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//                    val date1 = LocalDate.parse(s1.time).format(formatter)
//                    val date2 = LocalDate.parse(s2.time).format(formatter)
//                    return date1.compareTo(date2)
//                }
//            })
//            fireBaseList.reverse()
//            screen?.loadImages(fireBaseList)
//        } else {
//            getImages(true)
//        }
//    }
//
//    private fun deleteDuplicates(): ArrayList<ImageItem> {
//        val clearList = arrayListOf<ImageItem>()
//        if (fireBaseList.isNotEmpty()) {
//            //clearList.add(fireBaseList[0])
//            for (item in fireBaseList) {
//                if (!isContains(item, clearList)) {
//                    clearList.add(item)
//                }
//            }
//        }
//        return clearList
//    }
//
//    private fun isContains(item: ImageItem, list: ArrayList<ImageItem>): Boolean {
//        var contains = false
//        for (listItem in list) {
//            if (listItem.id == item.id) {
//                contains = true
//            }
//        }
//        return contains
//    }
//
//    private fun createImageItem(document: QueryDocumentSnapshot): ImageItem {
//        var length = document["length"]
//
//        if (length is Long || length is Int) {
//            length = length.toString().toDouble()
//        }
//
//        var woodType = if (document["wood_type"] == null) "" else document["wood_type"] as String?
//
//        return ImageItem(
//            id = document.id,
//            session_id = "",
//            isPushedToServer = true,
//            latitude = document["lat"] as Double?,
//            longitude = document["long"] as Double?,
//            length = length as Double?,
//            volume = document["volume"] as Double?,
//            time = document["time"] as String?,
//            imageUrl = document["url"] as String?,
//            imageRef = document["imageRef"] as String?,
//            userID = document["userID"] as String?,
//            leaderID = document["leaderID"] as String?,
//            forestryID = document["forestryID"] as String?,
//            thumbnailRef = document["thumbnailRef"] as String?,
//            thumbnailUrl = document["thumbnailUrl"] as String?,
//            woodType = woodType
//        )
//    }
//
//    fun sendImages(imageList: ArrayList<ImageItem>?) {
//        if (!Util.isNetworkAvailable()) {
//            activity?.let {
//                screen?.showAlertDialog(
//                    "Nincs internet kapcsolat",
//                    "Jelenleg nincs internetkapcsolat. Kérlek próbálja meg később."
//                )
//            }
//        } else {
//            /*imageList?.let {
//                screen?.showLoading()
//                val calls = arrayListOf<Observable<*>?>()
//
//                for (item in imageList) {
//                    val imageUploadRequest =
//                        createImageUploadRequest(item)
//                    calls.add(appServer?.uploadImage(imageUploadRequest))
//                }
//
//                val upload = Observable.zip(calls) { it.map { it as Response<*> } }
//                    ?.subscribeOn(Schedulers.io())
//                    ?.observeOn(Schedulers.newThread())
//                    ?.map { response ->
//                        val resultList = arrayListOf<Pair<MeasurementResult, Pair<String, String>>>()
//
//                        if (response.firstOrNull { !it.isSuccessful } == null) {
//                            for (item in response) {
//                                val imageUploadResponse = item.body() as ImageUploadResponse
//                                val idx = response.indexOf(item)
//                                var input = BitmapFactory.decodeFile(imageList[idx].localPath)
//                                resultList.add(
//                                    Pair(
//                                        MeasurementResult(imageUploadResponse.mask, input, imageList[idx].rodLength!!, imageList[idx].rodLengthPixel!!, imageList[idx].length!!.toFloat(), imageList[idx].time!!, imageList[idx].woodType!!, imageList[idx].latitude!!, imageList[idx].longitude!!),
//                                        Pair(
//                                            imageList[response.indexOf(item)].localPath ?: "",
//                                            imageList[response.indexOf(item)].id!!
//                                        )
//                                    )
//                                )
//                            }
//                            resultList
//                        } else {
//                            arrayListOf()
//                        }
//                    }
//                    ?.observeOn(AndroidSchedulers.mainThread())
//                    ?.subscribe({ resultList ->
//                        screen?.sendResultsToFragment(resultList)
//                        screen?.hideLoading()
//                    }, { error ->
//                        screen?.hideLoading()
//                        error.printStackTrace()
//                        if (error is SocketTimeoutException) {
//                            activity?.let {
//                                screen?.showAlertDialog("Nincs internet kapcsolat",
//                                    "Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
//                                    it, true, "Mentés")
//                            }
//                        } else {
//                            screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
//                        }
//                    })
//                upload?.let { subscriptions?.add(it) }
//            }*/
//        }
//    }
//
//
//    private fun createImageUploadRequest(item: ImageItem): ImageUploadRequest {
//        val file = File(item.localPath)
//        val imageUploadRequest = ImageUploadRequest()
//        imageUploadRequest.image = convertImageFileToBase64(file)
//        return imageUploadRequest
//    }
//
//    private fun convertImageFileToBase64(file: File): String {
//        return FileInputStream(file).use { inputStream ->
//            ByteArrayOutputStream().use { outputStream ->
//                Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
//                    inputStream.copyTo(base64FilterStream)
//                    base64FilterStream.flush()
//                    outputStream.toString()
//                }
//            }
//        }
//    }
//
//    private fun saveImages() {
//        for (item in fireBaseList) {
//            doAsync {
//                roomModule?.database?.imageItemDao()?.addImage(item)
//            }
//        }
//    }
//
//    private fun saveImage(imageItem: ImageItem) {
//        doAsync {
//            roomModule?.database?.imageItemDao()?.addImage(imageItem)
//        }
//    }
//
//    private fun deleteImagesFromFirebase(imageList: ArrayList<ImageItem>) {
//        for (item in imageList) {
//            fireStoreDB?.db?.collection("images")?.document(item.id)
//                ?.delete()
//                ?.addOnSuccessListener {
//                    item.imageRef?.let {
//                        deleteFromStorage(it, imageList.last() == item, item)
//                    }
//                    item.thumbnailRef?.let {
//                        deleteFromStorage(it, imageList.last() == item, item)
//                    }
//                }
//                ?.addOnFailureListener { e ->
//                    e.printStackTrace()
//                }
//        }
//    }
//
//    private fun deleteFromStorage(imageRef: String, isLast: Boolean, imageItem: ImageItem) {
//        val storageRef = fireStoreDB?.storage?.reference
//
//        val desertRef = storageRef?.child(imageRef)
//
//        desertRef?.delete()?.addOnSuccessListener {
//            deleteImageFromLocalStorage(imageItem)
//            if (isLast) {
//                screen?.deleted()
//                screen?.hideLoading()
//            }
//        }?.addOnFailureListener { e ->
//            e.printStackTrace()
//            screen?.hideLoading()
//        }
//    }
//
//    private fun deleteImageFromLocalStorage(imageItem: ImageItem) {
//        doAsync {
//            roomModule?.database?.imageItemDao()?.deleteImage(imageItem)
//        }
//    }
//
//    fun deleteImages(imageList: ArrayList<ImageItem>) {
//        if (Util.isNetworkAvailable()) {
//            if (isProcessed) {
//                deleteImagesFromFirebase(imageList)
//            } else {
//                for (item in imageList) {
//                    doAsync {
//                        roomModule?.database?.imageItemDao()?.deleteImage(item)
//                        getImages(isProcessed)
//                        uiThread {
//                            screen?.deleted()
//                        }
//                    }
//                }
//            }
//        } else {
//            activity?.let {
//                screen?.showAlertDialog(
//                    "No internet connection",
//                    "You don't have internet connection right now. Please try again later."
//                )
//            }
//        }
//
//    }
}