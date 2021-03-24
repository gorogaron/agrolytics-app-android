package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_cropper.*
import kotlinx.android.synthetic.main.activity_info.btn_back
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import org.jetbrains.anko.toast


class CropperActivity: BaseActivity(), View.OnClickListener {

    private var image : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.agrolytics.agrolytics_android.R.layout.activity_cropper)
        btn_back.setOnClickListener { onBackPressed() }

        val imageUri : Uri = intent.getParcelableExtra("IMAGE")
        image = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        rectangle_cropper_view.setImageBitmap(image)
        poly_cropper_view.setImageBitmap(image!!)
        polycropper.isSelected = true

        polycropper.setOnClickListener {
            it.isSelected = true
            rectanglecropper.isSelected = false
            rectangle_cropper_view.visibility = View.GONE
            poly_cropper_view.visibility = View.VISIBLE
        }
        rectanglecropper.setOnClickListener {
            it.isSelected = true
            polycropper.isSelected = false
            poly_cropper_view.visibility = View.GONE
            rectangle_cropper_view.visibility = View.VISIBLE
        }
        crop.setOnClickListener { cropImg() }
    }

    override fun onClick(v: View?) {
    }

    private fun cropImg(){
        var croppedImgBlackBg: Bitmap?
        var croppedImgBlurredBg: Bitmap?
        if (rectangle_cropper_view.visibility == View.VISIBLE) {
            /**Téglalap kijelölésnél nincs különbség a küldendő és megjelenítendő képek közt*/
            croppedImgBlackBg = rectangle_cropper_view.croppedImage
            croppedImgBlurredBg = croppedImgBlackBg
        }
        else {
            poly_cropper_view.crop().let {
                croppedImgBlackBg = it.first
                croppedImgBlurredBg = it.second
            }
        }
        if (croppedImgBlackBg != null && croppedImgBlurredBg != null) {
            MeasurementManager.startRodSelectorActivity(this, croppedImgBlackBg!!, croppedImgBlurredBg!!)
        }
        else {
            toast("Jelöljön ki megfelelő területet a képen.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK ) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        poly_cropper_view.resetFinalImages()
    }

    override fun onBackPressed() {
        MeasurementManager.showCloseMeasurementConfirmationDialog(this)
    }
}