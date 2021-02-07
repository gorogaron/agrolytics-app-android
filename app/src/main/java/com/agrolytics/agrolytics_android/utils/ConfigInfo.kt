package com.agrolytics.agrolytics_android.utils

object ConfigInfo {
	const val STORAGE_CODE = 100
	const val CAMERA_CODE = 101
	const val CAMERA_CAPTURE = 102
	const val PICK_IMAGE = 103
	const val CROPPER = 104
	const val IMAGE = "image"
	const val PATH = "path"
	const val ORIGINAL_HEIGHT = "original_height"
	const val ID = "id"
	const val METHOD = "method"
	const val UPLOAD_RESPONSE = "upload_response"
	const val MAP_BOX_KEY = "pk.eyJ1IjoidGhlMDExMWRlbW9wcm9qZWN0IiwiYSI6ImNqeXVlaDdiNTBlMjUzaW9iYXpwN21lazIifQ.UT3O8C9YAocVt-zvExjbAQ"

	/**Firebase login result codes**/
	object LOGIN {
		const val SUCCESS 			= 0
		const val NO_INTERNET 		= 1
		const val AUTH_FAILED 		= 2
		const val ERROR   			= 3
		const val USER_EXPIRED 		= 4
		const val WRONG_INPUT 		= 5
		const val UNDEFINED   		= 6
	}
}