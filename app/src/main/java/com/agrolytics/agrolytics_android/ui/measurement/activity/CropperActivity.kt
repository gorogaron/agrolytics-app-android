package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_cropper.*
import kotlinx.android.synthetic.main.activity_info.btn_back
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import android.os.Environment.getExternalStorageDirectory
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


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
        val croppedImg : Bitmap?
        if (rectangle_cropper_view.visibility == View.VISIBLE){
            croppedImg = rectangle_cropper_view.getCroppedImage()
        }
        else{
            //TODO: Send finalImgBlackBackground to server, draw mask on finalImgBlurredBackground. On rodSelector screen, show finalImgBlurredBackground
            val (finalImgBlackBackground, finalImgBlurredBackground) = poly_cropper_view.crop()
            croppedImg = finalImgBlurredBackground
        }
        if (croppedImg != null)
        {
            val cropImgUri = BitmapToTempUri(Bitmap.createScaledBitmap(croppedImg, 640, 480, true), "cropped_img")

            val intent = Intent(this, RodSelectorActivity::class.java)
            intent.putExtra(ConfigInfo.PATH, cropImgUri?.path)
            RodSelectorActivity.bitmap = croppedImg
            startActivity(intent)
        }
        else {
            toast("Jelöljön ki megfelelő területet a képen.")
        }
    }

    private fun BitmapToTempUri(inImage: Bitmap, title: String): Uri? {
        var tempDir = getExternalStorageDirectory()
        tempDir = File(tempDir.getAbsolutePath() + "/.temp/")
        tempDir.mkdir()
        val tempFile = File.createTempFile(title, ".jpg", tempDir)
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(tempFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
        return Uri.fromFile(tempFile)
    }

    override fun onResume() {
        super.onResume()
        poly_cropper_view.resetFinalImages()
    }
}