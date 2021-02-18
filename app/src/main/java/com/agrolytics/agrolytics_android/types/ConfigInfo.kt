package com.agrolytics.agrolytics_android.types


object ConfigInfo {
	const val STORAGE_CODE = 100
	const val CAMERA_CODE = 101
	const val IMAGE_CAPTURE = 102
	const val IMAGE_BROWSE = 103
	const val CROPPER = 104
	const val IMAGE = "image"
	const val CROPPED_RESIZED_IMG_PATH = "path"
	const val ORIGINAL_HEIGHT = "original_height"
	const val ID = "id"
	const val METHOD = "method"
	const val UPLOAD_RESPONSE = "upload_response"
	const val MAP_BOX_KEY = "pk.eyJ1IjoidGhlMDExMWRlbW9wcm9qZWN0IiwiYSI6ImNqeXVlaDdiNTBlMjUzaW9iYXpwN21lazIifQ.UT3O8C9YAocVt-zvExjbAQ"

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
}