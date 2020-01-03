package com.agrolytics.agrolytics_android.utils.gallery;

import android.graphics.Bitmap;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragmentList;
    private Fragment mCurrentFragment;

    public GalleryAdapter(FragmentManager fm, List<String> imageUrlList, List<Bitmap> bitmapList) {
        super(fm);
        fragmentList = new ArrayList<>();
        if (imageUrlList != null) {
            for (String imageUrl : imageUrlList) {
                fragmentList.add(GalleryItemFragment.getInstance(imageUrl));
            }
        }
        if (bitmapList != null) {
            for (Bitmap bitmap : bitmapList) {
                fragmentList.add(GalleryItemFragment.getInstance(bitmap));
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public int getCurrentFragmentPosition() {
        return fragmentList.indexOf(getCurrentFragment());
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            mCurrentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    void deleteImage(int position) {
        fragmentList.remove(position);
        notifyDataSetChanged();
    }
}