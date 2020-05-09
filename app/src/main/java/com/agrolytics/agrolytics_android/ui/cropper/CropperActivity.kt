package com.agrolytics.agrolytics_android.ui.cropper

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.base.BaseActivity
import kotlinx.android.synthetic.main.activity_cropper.*
import kotlinx.android.synthetic.main.activity_info.btn_back
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.ui.rodSelector.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.google.common.io.Flushables.flush
import android.system.Os.mkdir
import android.os.Environment.getExternalStorageDirectory
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class CropperActivity: BaseActivity(), View.OnClickListener {

    private var image : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.agrolytics.agrolytics_android.R.layout.activity_cropper)
        btn_back.setOnClickListener { onBackPressed() }

        var imageUri : Uri = intent.getParcelableExtra("IMAGE")
        image = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)


        rectangle_cropper_view.setImageBitmap(image)
        poly_cropper_view.setImageBitmap(image!!)

        polycropper.setOnClickListener {
            rectangle_cropper_view.visibility = View.GONE
            poly_cropper_view.visibility = View.VISIBLE
        }
        rectanglecropper.setOnClickListener {
            poly_cropper_view.visibility = View.GONE
            rectangle_cropper_view.visibility = View.VISIBLE
        }
        crop.setOnClickListener{
            val intent = Intent(this, RodSelectorActivity::class.java)
            intent.putExtra(ConfigInfo.PATH, imageUri.path)
            RodSelectorActivity.bitmap = image
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
    }

    fun BitmapToTempUri(inImage: Bitmap, title: String): Uri? {
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

}