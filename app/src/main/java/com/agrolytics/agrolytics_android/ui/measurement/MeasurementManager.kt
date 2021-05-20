package com.agrolytics.agrolytics_android.ui.measurement

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.network.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.network.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.ui.measurement.activity.CropperActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.SessionActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageSegmentation
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response

object MeasurementManager : KoinComponent{

    private val appServer: AppServer by inject()
    private val sessionManager : SessionManager by inject()
    private val dataClient: DataClient by inject()

    var currentSessionId : Long = 0L
    var recentlyAddedItemTimestamps = ArrayList<Long>()

    enum class ImagePickerID {
        ID_CAMERA, ID_BROWSER
    }

    fun startNewMeasurementSession(callingActivity : BaseActivity, imagePickerID: ImagePickerID) {
        val currentTimeStamp = System.currentTimeMillis() / 1000
        if (currentSessionId == 0L) {
            currentSessionId = currentTimeStamp
            hookImage(callingActivity, imagePickerID)
        }
        else {
            /**Ide nem szabadna jutnunk, új sessiont csak akkor indíthatunk ha az előzőt lezártuk,
             * tehát currentSessionId = 0.*/
            callingActivity.toast(callingActivity.getString(R.string.unexpected_error))
        }
    }

    fun addNewMeasurementForSession(callingActivity : BaseActivity, sessionId: Long?) {
        if (sessionId == null || sessionId == 0L) {
            callingActivity.toast(callingActivity.getString(R.string.unexpected_error))
            return
        }

        val browseListener = {
            currentSessionId = sessionId
            hookImage(callingActivity, ImagePickerID.ID_BROWSER)
        }

        val cameraListener = {
            currentSessionId = sessionId
            hookImage(callingActivity, ImagePickerID.ID_CAMERA)
        }

        callingActivity.show2OptionDialog(callingActivity.getString(R.string.select_image),
                callingActivity.getString(R.string.camera),
                callingActivity.getString(R.string.browse),
                cameraListener,
                browseListener)
    }

    fun hookImage(callingActivity : BaseActivity, imagePickerID : ImagePickerID){
        val currentTimeStamp = System.currentTimeMillis() / 1000
        sessionManager.measurementStartTimestamp = currentTimeStamp

        if (imagePickerID == ImagePickerID.ID_CAMERA){
            ImageObtainer.openCamera(callingActivity)
        }
        else if (imagePickerID == ImagePickerID.ID_BROWSER){
            ImageObtainer.openGallery(callingActivity)
        }
    }

    fun startCropperActivity(callingActivity : BaseActivity, imgUri: Uri){
        val intent = Intent(callingActivity, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        callingActivity.startActivity(intent)
    }

    fun startRodSelectorActivity(callingActivity : CropperActivity, croppedImgBlackBg: Bitmap, croppedImgBlurredBg: Bitmap) {
        /**A fekete hátterű képet fogjuk feldolgozni*/
        val croppedImgBlackBgUri = ImageUtils.createTempFileFromBitmap(Bitmap.createScaledBitmap(croppedImgBlackBg, 640, 480, true))
        //TODO: Handle if temporary file could not have been created
        val intent = Intent(callingActivity, RodSelectorActivity::class.java)
        intent.putExtra(ConfigInfo.CROPPED_RESIZED_IMG_PATH, croppedImgBlackBgUri!!.path)

        /**Az elmosott hátterű képet fogjuk megjeleníteni*/
        RodSelectorActivity.croppedImageBlurredBg = croppedImgBlurredBg
        RodSelectorActivity.correspondingCropperActivity = callingActivity
        callingActivity.startActivityForResult(intent, ConfigInfo.ROD_SELECTOR)
    }

    fun startApproveMeasurementActivity(callingActivity : BaseActivity, processedImageItem: ProcessedImageItem, unprocessedImageItem: UnprocessedImageItem, method: String) {
        ApproveMeasurementActivity.method = method
        ApproveMeasurementActivity.processedImageItem = processedImageItem
        ApproveMeasurementActivity.unprocessedImageItem = unprocessedImageItem
        //Ez akkor igaz, ha utólagos feldolgozással indítjuk az approve activity-t
        if (currentSessionId == 0L) {
            if (callingActivity is SessionActivity) callingActivity.finish() //Az approve activityben csinálunk egy új példányt helyette
            currentSessionId = processedImageItem.sessionId
        }
        if (SessionActivity.correspondingApproveMeasurementActivity != null) { //Ha van korábbi approve activity megnyitva, azt zárjuk be
            SessionActivity.correspondingApproveMeasurementActivity!!.finish()
        }
        val intent = Intent(callingActivity, ApproveMeasurementActivity::class.java)
        callingActivity.startActivity(intent)
    }

    suspend fun startOnlineMeasurement(bitmap: Bitmap) : Response<ImageUploadResponse>{
        val request = createImageUploadRequest(bitmap)
        return appServer.uploadImage(request)
    }

    fun startOfflineMeasurement(unprocessedImageItem : UnprocessedImageItem, croppedResizedImageBlackBg : Bitmap) : ProcessedImageItem {
        val (mask, numOfWoodPixels) = ImageSegmentation.segment(croppedResizedImageBlackBg)
        //TODO: drawMaskOnInputImage és ImageSegmentation.segment egyesítése
        val (maskedImg, _) = ImageUtils.drawMaskOnInputImage(unprocessedImageItem.image!!, mask)
        return ProcessedImageItem(unprocessedImageItem, numOfWoodPixels, maskedImg)
    }

    private fun createImageUploadRequest(bitmap: Bitmap?): ImageUploadRequest {
        val imageUploadRequest = ImageUploadRequest()
        bitmap?.let { imageUploadRequest.image = ImageUtils.bitmapToBase64(bitmap) }
        return imageUploadRequest
    }

    fun showSession(callingActivity : BaseActivity, sessionId : Long?) {
        if (sessionId != null){
            SessionActivity.sessionId = sessionId

            if (callingActivity is ApproveMeasurementActivity) {
                SessionActivity.correspondingApproveMeasurementActivity = callingActivity
            }

            val intent = Intent(callingActivity, SessionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            callingActivity.startActivityForResult(intent, ConfigInfo.SESSION)
        }
    }

    fun showCloseMeasurementConfirmationDialog(callingActivity : BaseActivity){
        val exitListener = {
            for (imageItemTimestamps in recentlyAddedItemTimestamps) {
                //cache-d item törlésére itt nincs szükség, mert itt a nem mentett képeket töröljük.
                doAsync {
                    dataClient.local.unprocessed.deleteByTimestamp(imageItemTimestamps)
                    dataClient.local.processed.deleteByTimestamp(imageItemTimestamps)
                }
            }
            clearMeasurementSession()
            callingActivity.finish()
        }

        val cancelListener = {
            //Ne csináljunk semmit, csak zárjuk be a popup window-t
        }

        if (recentlyAddedItemTimestamps.isNotEmpty()) {
            callingActivity.show2OptionDialog(
                callingActivity.getString(R.string.quit_confirm, recentlyAddedItemTimestamps.size),
                    callingActivity.getString(R.string.exit),
                    callingActivity.getString(R.string.cancel),
                exitListener,
                cancelListener)
        }
        else {
            exitListener()
        }
    }

    fun clearMeasurementSession(){
        currentSessionId = 0
        recentlyAddedItemTimestamps.clear()
    }

    fun deleteFromRecentlyAddedItemTimestamps(timeStampToDelete : Long) {
        recentlyAddedItemTimestamps.remove(timeStampToDelete)
    }

}