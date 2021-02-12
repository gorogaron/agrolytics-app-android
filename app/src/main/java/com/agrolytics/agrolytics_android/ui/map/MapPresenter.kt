package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.database.firestore.FireStoreCollection
import com.agrolytics.agrolytics_android.database.firestore.FireStoreImagesField
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.database.local.ImageItem
import com.agrolytics.agrolytics_android.types.UserRole
import com.google.firebase.firestore.QueryDocumentSnapshot
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MapPresenter: BasePresenter<MapScreen>() {

	var fireBaseList = arrayListOf<ImageItem>()

	fun getAllUploadedImage() {
		when (sessionManager?.userRole) {
			UserRole.ADMIN.name -> subscribeAdminImages()
			UserRole.LEADER.name -> subscribeLeaderImages()
			UserRole.WORKER.name -> subscribeWorkerImages()
		}
	}

	private fun subscribeAdminImages() {
		screen?.showLoading()
		fireStoreDB?.db?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.FORESTRY_ID.tag, sessionManager?.forestryID)
			?.addSnapshotListener { value, e ->
				if (e != null) {
					e.printStackTrace()
					return@addSnapshotListener
				}

				if (fireBaseList.isEmpty()) {
					for (document in value!!) {
						val imageItem = createImageItem(document)
						if (imageItem.latitude != null && imageItem.longitude != null) {
							addItemToList(imageItem)
						}
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
		fireStoreDB?.db?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.LEADER_ID.tag, sessionManager?.userID)
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
		fireStoreDB?.db?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager?.userID)
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
		fireStoreDB?.db?.collection(FireStoreCollection.IMAGES.tag)
			?.whereEqualTo(FireStoreImagesField.LEADER_ID.tag, sessionManager?.userID)
			?.get()?.addOnSuccessListener { it ->
				for (document in it) {
					val imageItem = createImageItem(document)
					addItemToList(imageItem)
				}
				fireStoreDB?.db?.collection(FireStoreCollection.IMAGES.tag)
					?.whereEqualTo(FireStoreImagesField.USER_ID.tag, sessionManager?.userID)
					?.get()?.addOnSuccessListener {
						for (document in it) {
							val imageItem = createImageItem(document)
							addItemToList(imageItem)
						}
						loadImagesFromFireBase()
					}
			}
	}

	private fun addItemToList(imageItem: ImageItem) {
		if (imageItem.latitude != null && imageItem.longitude != null) {
			fireBaseList.add(imageItem)
		}
	}

	private fun createImageItem(document: QueryDocumentSnapshot): ImageItem {
		var length = document[FireStoreImagesField.WOOD_LENGTH.tag]

		if (length is Long || length is Int) {
			length = length.toString().toDouble()
		}

		val woodType = if (document[FireStoreImagesField.WOOD_TYPE.tag] == null) "" else document[FireStoreImagesField.WOOD_TYPE.tag] as String?

		return ImageItem(
			id = document.id,
			session_id = "",
			isPushedToServer = true,
			latitude = document[FireStoreImagesField.LOC_LAT.tag] as Double?,
			longitude = document[FireStoreImagesField.LOC_LON.tag] as Double?,
			length = length as Double?,
			volume = document[FireStoreImagesField.WOOD_VOLUME.tag] as Double?,
			time = document[FireStoreImagesField.TIME.tag] as String?,
			serverImage = document[FireStoreImagesField.IMAGE_URL.tag] as String?,
			serverPath = document[FireStoreImagesField.IMAGE_REFERENCE.tag] as String?,
			userID = document[FireStoreImagesField.USER_ID.tag] as String?,
			leaderID = document[FireStoreImagesField.LEADER_ID.tag] as String?,
			forestryID = document[FireStoreImagesField.FORESTRY_ID.tag] as String?,
			thumbnailPath = document[FireStoreImagesField.IMAGE_THUMBNAIL_REFERENCE.tag] as String?,
			thumbnailUrl = document[FireStoreImagesField.IMAGE_THUMBNAIL_URL.tag] as String?,
			woodType = woodType)
	}

	fun getAllLocalImage() {
		var images: List<ImageItem>
		doAsync {
			roomModule?.database?.imageItemDao()?.getAllImage(false)?.let {
				images = it
				uiThread {
					fireBaseList.addAll(images)
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