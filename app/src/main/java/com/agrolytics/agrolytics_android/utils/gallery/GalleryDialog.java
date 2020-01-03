package com.agrolytics.agrolytics_android.utils.gallery;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;
import com.agrolytics.agrolytics_android.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryDialog extends DialogFragment {

    private static final String KEY_IMAGE_URLS = "image_urls";
    private static final String KEY_IMAGE_BITMAP = "image_bitmap";
    private static final String KEY_POSITION = "position";

    private List<String> imageUrls;
    private List<Bitmap> bitmapList;
    private int position;
    static boolean showDelete;
    public static GalleryCallback galleryCallback;

    ViewPager viewPager;
    ImageButton btnClose;
    Button btnDelete;
    //GetsbyCirclePageIndicator indicator;
    GalleryAdapter adapter;

    public static GalleryDialog getInstance(List<String> imageUrls, List<Bitmap> bitmapList, int position, boolean showDeleteButton) {
        showDelete = showDeleteButton;
        GalleryDialog dialog = new GalleryDialog();
        Bundle args = new Bundle();
        if (imageUrls != null) {
            args.putStringArrayList(KEY_IMAGE_URLS, (ArrayList<String>) imageUrls);
        }
        if (bitmapList != null) {
            args.putParcelableArrayList(KEY_IMAGE_BITMAP, (ArrayList<Bitmap>) bitmapList);
        }
        args.putInt(KEY_POSITION, position);
        dialog.setArguments(args);
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.GalleryDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_image_viewer, container, false);
        viewPager = view.findViewById(R.id.view_pager);
        //indicator = view.findViewById(R.id.indicator);
        btnClose = view.findViewById(R.id.btn_close);
        btnDelete = view.findViewById(R.id.btn_delete);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrls = getArguments().getStringArrayList(KEY_IMAGE_URLS);
            bitmapList = getArguments().getParcelableArrayList(KEY_IMAGE_BITMAP);
            position = getArguments().getInt(KEY_POSITION);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new GalleryAdapter(getChildFragmentManager(), imageUrls, bitmapList);

        viewPager.setAdapter(adapter);
        //indicator.setViewPager(viewPager);
        viewPager.setCurrentItem(position);

        if (!showDelete) {
            btnDelete.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryCallback.deleteItem(adapter.getCurrentFragmentPosition());
                adapter.deleteImage(adapter.getCurrentFragmentPosition());
                if (adapter.getCount() == 0) {
                    dismiss();
                }
            }
        });
    }
}
