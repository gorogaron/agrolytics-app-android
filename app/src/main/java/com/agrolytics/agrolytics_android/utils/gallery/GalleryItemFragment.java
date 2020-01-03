package com.agrolytics.agrolytics_android.utils.gallery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.agrolytics.agrolytics_android.R;
import com.agrolytics.agrolytics_android.base.BaseFragment;
import com.bumptech.glide.Glide;

public class GalleryItemFragment extends BaseFragment {

    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_IMAGE_BITMAP = "image_bitmap";

    ImageView imageView;

    private String imageUrl;
    private Bitmap imageBitmap;

    public static GalleryItemFragment getInstance(String imageUrl) {
        GalleryItemFragment fragment = new GalleryItemFragment();
        Bundle args = new Bundle();
        args.putString(KEY_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public static GalleryItemFragment getInstance(Bitmap bitmap) {
        GalleryItemFragment fragment = new GalleryItemFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_IMAGE_BITMAP, bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.image);

        if (imageUrl != null) {
            Glide.with(getContext()).load(imageUrl).into(imageView);
        } else if (imageBitmap != null) {
            imageView.setImageBitmap(imageBitmap);
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrl = getArguments().getString(KEY_IMAGE_URL);
        }
        if (getArguments() != null) {
            imageBitmap = getArguments().getParcelable(KEY_IMAGE_BITMAP);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_gallery_item;
    }

}