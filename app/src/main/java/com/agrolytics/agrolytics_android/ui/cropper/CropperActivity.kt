package com.agrolytics.agrolytics_android.ui.cropper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.ImagesActivity
import com.agrolytics.agrolytics_android.ui.info.InfoActivity
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.map.MapActivity
import com.agrolytics.agrolytics_android.ui.setLength.LengthActivity
import com.agrolytics.agrolytics_android.utils.MenuItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_cropper.*
import kotlinx.android.synthetic.main.activity_info.*
import kotlinx.android.synthetic.main.activity_info.btn_back
import kotlinx.android.synthetic.main.nav_bar.*
import org.koin.android.ext.android.inject

class CropperActivity: BaseActivity(), View.OnClickListener  {

    private val TAG = "GuideActivity"
    private val sessionManager: SessionManager by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cropper)
        btn_back.setOnClickListener { onBackPressed() }
        var bitmap = BitmapFactory.decodeResource(resources, R.drawable.dummy)
        cropImageView.setImageBitmap(bitmap);

    }

    override fun onClick(v: View?) {
    }


}