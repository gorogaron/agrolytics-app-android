package com.agrolytics.agrolytics_android.types


object ConfigInfo {
	const val STORAGE_CODE = 100
	const val CAMERA_CODE = 101
	const val IMAGE_CAPTURE = 102
	const val IMAGE_BROWSE = 103
	const val CROPPER = 104
	const val ROD_SELECTOR = 105
	const val SESSION = 106
	const val GPS_TURN_ON_REQUEST = 107
	const val IMAGE = "image"
	const val CROPPED_RESIZED_IMG_PATH = "path"
	const val ORIGINAL_HEIGHT = "original_height"
	const val ID = "id"
	const val PROCESSED_IMAGE_ITEM = "processed.image.item"
	const val METHOD = "method"
	const val UPLOAD_RESPONSE = "upload_response"
	const val MAP_BOX_KEY = "sk.eyJ1IjoiZ29yb2dhcm9uIiwiYSI6ImNrbXFiaDlhcTJtbWEydnFvNXl4OWplcTMifQ.GxtuYq8wqkuHU8S3GD2-qg"
	const val PROCESSED_IMAGE_ITEM_TIMESTAMP = "processed_image_item_timestamp"
	const val CACHED_IMAGE_ITEM_FIRESTORE_ID = "cached_image_item_firestore_id"

	/**Firebase login result codes**/
	enum class LOGIN {
		SUCCESS,
		AUTH_FAILED,
		USER_EXPIRED,
		WRONG_INPUT,
		NO_INTERNET,
		ERROR,
		UNDEFINED
	}

	/**Local database item types*/
	enum class IMAGE_ITEM_TYPE {
		CACHED,
		PROCESSED,
		UNPROCESSED
	}

	enum class IMAGE_ITEM_STATE {
		UNDEFINED,
		UPLOADED,
		READY_TO_UPLOAD,
		BEING_UPLOADED,
		WAITING_FOR_PROCESSING,
		BEING_DELETED
	}
}