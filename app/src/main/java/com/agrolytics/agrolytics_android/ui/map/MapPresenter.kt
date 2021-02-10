package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.google.firebase.firestore.QueryDocumentSnapshot
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MapPresenter: BasePresenter<MapScreen>() {

	var fireBaseList = arrayListOf<ImageItem>()

	fun getAllUploadedImage() {
		when (sessionManager?.userRole) {
			"admin" -> subscribeAdminImages()
			"leader" -> subscribeLeaderImages()
			"worker" -> subscribeWorkerImages()
		}
	}

	private fun subscribeAdminImages() {
		screen?.showLoading()
		fireStoreDB?.db?.collection("images")
			?.whereEqualTo("forestryID",sessionManager?.forestryID)
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
		fireStoreDB?.db?.collection("images")
			?.whereEqualTo("leaderID", sessionManager?.userID)
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
		fireStoreDB?.db?.collection("images")
			?.whereEqualTo("userID", sessionManager?.userID)
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
		fireStoreDB?.db?.collection("images")
			?.whereEqualTo("leaderID", sessionManager?.userID)
			?.get()?.addOnSuccessListener {
				for (document in it) {
					val imageItem = createImageItem(document)
					addItemToList(imageItem)
				}
				fireStoreDB?.db?.collection("images")
					?.whereEqualTo("userID", sessionManager?.userID)
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
		var length = document["length"]

		if (length is Long || length is Int) {
			length = length.toString().toDouble()
		}

		var woodType = if (document["wood_type"] == null) "" else document["wood_type"] as String?

		return ImageItem(
			id = document.id,
			isPushedToServer = true,
			latitude = document["lat"] as Double?,
			longitude = document["long"] as Double?,
			length = length as Double?,
			volume = document["volume"] as Double?,
			time = document["time"] as String?,
			serverImage = document["url"] as String?,
			serverPath = document["imageRef"] as String?,
			userID = document["userID"] as String?,
			leaderID = document["leaderID"] as String?,
			forestryID = document["forestryID"] as String?,
			thumbnailPath = document["thumbnailRef"] as String?,
			thumbnailUrl = document["thumbnailUrl"] as String?,
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

	fun getItem(id: String) {
		doAsync {
			roomModule?.database?.imageItemDao()?.getImageById(id)?.let {
				screen?.showDetails(it)
			}
		}
	}
}