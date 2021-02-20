package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreCollection
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImagesField
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.types.UserRole
import com.agrolytics.agrolytics_android.utils.MeasurementUtils
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QueryDocumentSnapshot
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MapPresenter: BasePresenter<MapScreen>() {

	var fireBaseList = arrayListOf<CachedImageItem>()

	fun getAllUploadedImage() {
		when (sessionManager?.userRole) {
			UserRole.ADMIN.name -> subscribeAdminImages()
			UserRole.LEADER.name -> subscribeLeaderImages()
			UserRole.WORKER.name -> subscribeWorkerImages()
		}
	}

	private fun subscribeAdminImages() {
		screen?.showLoading()
		dataClient?.fireBase?.fireStore?.firestore?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.FORESTRY_ID.tag, sessionManager?.forestryId)
			?.addSnapshotListener { value, e ->
				if (e != null) {
					e.printStackTrace()
					return@addSnapshotListener
				}

				if (fireBaseList.isEmpty()) {
					for (document in value!!) {
						val imageItem = createImageItem(document)
						addItemToList(imageItem)
					}
				} else {
					for (document in value!!.documentChanges) {
						val imageItem = createImageItem(document.document)
						addItemToList(imageItem)
					}
				}

				loadImagesFromFireBase()
			}
	}

	private fun subscribeLeaderImages() {
		screen?.showLoading()
		getLeaderImages()
		dataClient?.fireBase?.fireStore?.firestore?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.LEADER_ID.tag, sessionManager?.userId)
			?.addSnapshotListener { value, e ->
				if (e != null) {
					e.printStackTrace()
					return@addSnapshotListener
				}

				if (value!!.documentChanges.size == 1) {
					for (document in value.documentChanges) {
						val imageItem = createImageItem(document.document)
						addItemToList(imageItem)
					}
					loadImagesFromFireBase()
				}

			}
	}

	private fun subscribeWorkerImages() {
		screen?.showLoading()
		dataClient?.fireBase?.fireStore?.firestore?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager?.userId)
			?.addSnapshotListener { value, e ->
				if (e != null) {
					e.printStackTrace()
					return@addSnapshotListener
				}

				if (fireBaseList.isEmpty()) {
					for (document in value!!) {
						val imageItem = createImageItem(document)
						addItemToList(imageItem)
					}
				} else {
					for (document in value!!.documentChanges) {
						val imageItem = createImageItem(document.document)
						addItemToList(imageItem)
					}
				}
				loadImagesFromFireBase()
			}
	}

	private fun loadImagesFromFireBase() {
		screen?.hideLoading()
		screen?.loadImages(fireBaseList, true)
	}

	private fun getLeaderImages() {
		dataClient?.fireBase?.fireStore?.firestore?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.LEADER_ID.tag, sessionManager?.userId)
			?.get()?.addOnSuccessListener { it ->
				for (document in it) {
					val imageItem = createImageItem(document)
					addItemToList(imageItem)
				}
				dataClient?.fireBase?.fireStore?.firestore?.collection(FireStoreCollection.IMAGES.tag)
					?.whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager?.userId)
					?.get()?.addOnSuccessListener {
						for (document in it) {
							val imageItem = createImageItem(document)
							addItemToList(imageItem)
						}
						loadImagesFromFireBase()
					}
			}
	}

	private fun addItemToList(imageItem: CachedImageItem) {
		fireBaseList.add(imageItem)
	}

	private fun createImageItem(document: QueryDocumentSnapshot): CachedImageItem {
		var length = document[FireStoreImagesField.WOOD_LENGTH.tag]

		if (length is Long || length is Int) {
			length = length.toString().toDouble()
		}

		val woodType = if (document[FireStoreImagesField.WOOD_TYPE.tag] == null) "" else document[FireStoreImagesField.WOOD_TYPE.tag] as String

		// TODO: Az id generálás nem jó, mert autogenerálásra van állítva mégis meg kell adni. LocalPath és firestoreId sem elérhető itt még.
		//  SessionId-t sem itt kell legenerálni.
		return CachedImageItem(
			id = 0,
			sessionId = MeasurementUtils.generateSessionId(),
			location = document[FireStoreImagesField.LOCATION.tag] as GeoPoint,
			woodLength = length as Double,
			woodVolume = document[FireStoreImagesField.WOOD_VOLUME.tag] as Double,
			timestamp = document[FireStoreImagesField.TIME.tag] as Long,
			imageUrl = document[FireStoreImagesField.IMAGE_URL.tag] as String,
			imageRef = document[FireStoreImagesField.IMAGE_REFERENCE.tag] as String,
			userId = document[FireStoreImagesField.USER_ID.tag] as String,
			leaderId = document[FireStoreImagesField.LEADER_ID.tag] as String?,
			userRole = sessionManager?.userRole!!,
			forestryId = document[FireStoreImagesField.FORESTRY_ID.tag] as String,
			thumbRef = document[FireStoreImagesField.IMAGE_THUMBNAIL_REFERENCE.tag] as String,
			thumbUrl = document[FireStoreImagesField.IMAGE_THUMBNAIL_URL.tag] as String,
			woodType = woodType,
			localPath = "path",
			firestoreId = "")
	}

	fun getAllLocalImage() {
		doAsync {
			dataClient?.local?.cache?.getAllImages().let { images ->
				uiThread {
					images?.let { it1 -> fireBaseList.addAll(it1) }
					screen?.loadImages(ArrayList(images), false)
				}
			} ?: run {
				uiThread {
					screen?.loadImages(arrayListOf(), false)
				}
			}
		}
	}

	fun getItemFromList(position: Int) {
		screen?.showDetails(fireBaseList[position])
	}
}