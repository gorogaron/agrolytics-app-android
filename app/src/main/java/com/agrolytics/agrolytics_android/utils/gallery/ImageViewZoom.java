package com.agrolytics.agrolytics_android.utils.gallery;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class ImageViewZoom extends AppCompatImageView {

    private static final float SCALE_ON_DOUBLE_TAP = 2.0f;
    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 3.0f;

    private static final int ANIMATION_DURATION = 300;

    private float mOriginalX = -1;
    private float mOriginalY = -1;

    private float[] mMatrixArray;

    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mGestureDetector;

    private boolean mIsScaling;
    private boolean mDoubleTab;

    public ImageViewZoom(Context context) {
        super(context);
        init();
    }

    public ImageViewZoom(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewZoom(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        GestureDetector.OnGestureListener gestureListener = new GestureListener();
        ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleListener();
        this.mScaleDetector = new ScaleGestureDetector(getContext(), scaleListener);
        this.mGestureDetector = new GestureDetector(getContext(), gestureListener, null, true);
        mMatrixArray = new float[9];
    }

    private float getCurrentScale() {
        getImageMatrix().getValues(mMatrixArray);
        return mMatrixArray[Matrix.MSCALE_X];
    }

    private float getCurrentTranslateX() {
        getImageMatrix().getValues(mMatrixArray);
        return mMatrixArray[Matrix.MTRANS_X];
    }

    private float getCurrentTransalteY() {
        getImageMatrix().getValues(mMatrixArray);
        return mMatrixArray[Matrix.MTRANS_Y];
    }

    private float getScaledImageHeight() {
        if (getDrawable() == null) {
            return 0;
        }
        return getDrawable().getIntrinsicHeight() * getCurrentScale();
    }

    private float getScaledImageWidth() {
        if (getDrawable() == null) {
            return 0;
        }
        return getDrawable().getIntrinsicWidth() * getCurrentScale();
    }

    private float getOriginalX() {
        if (mOriginalX == -1) {
            if (getWidth() > 0 && getDrawable() != null && getDrawable().getIntrinsicWidth() > 0) {
                mOriginalX = (getWidth() - getDrawable().getIntrinsicWidth()) / 2;
            }
        }
        return mOriginalX;
    }

    private float getOriginalY() {
        if (mOriginalY == -1) {
            if (getHeight() > 0 && getDrawable() != null && getDrawable().getIntrinsicHeight() > 0) {
                mOriginalY = (getHeight() - getDrawable().getIntrinsicHeight()) / 2;
            }
        }
        return mOriginalY;
    }


    protected void scaleAnimate(float targetScale, final float pivotX, final float pivotY) {
        final ValueAnimator animScale = ValueAnimator.ofFloat(getCurrentScale(), targetScale).setDuration(ANIMATION_DURATION);
        animScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueS = (Float) animScale.getAnimatedValue();
                float diff = valueS / getCurrentScale();
                getImageMatrix().postScale(diff, diff, pivotX, pivotY);
                postInvalidateOnAnimation();
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animScale);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    protected void scaleAndTranslateAnimate(final float targetX, final float targetY, float targetScale, final float pivotX, final float pivotY) {
        final ValueAnimator animX = ValueAnimator.ofFloat(new float[]{getCurrentTranslateX(), targetX}).setDuration(ANIMATION_DURATION);
        final ValueAnimator animY = ValueAnimator.ofFloat(new float[]{getCurrentTransalteY(), targetY}).setDuration(ANIMATION_DURATION);
        final ValueAnimator animScale = ValueAnimator.ofFloat(new float[]{getCurrentScale(), targetScale}).setDuration(ANIMATION_DURATION);
        animScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueX = (Float) animX.getAnimatedValue();
                float valueY = (Float) animY.getAnimatedValue();
                float distanceX = valueX - getCurrentTranslateX();
                float distanceY = valueY - getCurrentTransalteY();
                float valueS = (Float) animScale.getAnimatedValue();
                float diff = valueS / getCurrentScale();
                getImageMatrix().postTranslate(distanceX, distanceY);
                getImageMatrix().postScale(diff, diff, pivotX, pivotY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    postInvalidateOnAnimation();
                } else {
                    postInvalidate();
                }
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animX, animY, animScale);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    protected void translateAnimate(final float targetX, final float targetY) {
        final ValueAnimator animX = ValueAnimator.ofFloat(new float[]{getCurrentTranslateX(), targetX}).setDuration(ANIMATION_DURATION);
        final ValueAnimator animY = ValueAnimator.ofFloat(new float[]{getCurrentTransalteY(), targetY}).setDuration(ANIMATION_DURATION);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueX = (Float) animX.getAnimatedValue();
                float valueY = (Float) animY.getAnimatedValue();
                float distanceX = valueX - getCurrentTranslateX();
                float distanceY = valueY - getCurrentTransalteY();
                getImageMatrix().postTranslate(distanceX, distanceY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    postInvalidateOnAnimation();
                } else {
                    postInvalidate();
                }
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animX, animY);
        animatorSet.setDuration(ANIMATION_DURATION);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.start();
    }

    public boolean onTouchEvent(MotionEvent event) {

        this.mScaleDetector.onTouchEvent(event);
        if (!this.mScaleDetector.isInProgress()) {
            this.mGestureDetector.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mDoubleTab) {
                mDoubleTab = false;

            } else {

                mIsScaling = false;

                if (getCurrentScale() < 1.0f) {
                    scaleAndTranslateAnimate(getOriginalX(), getOriginalY(), 1.0f, event.getX(), event.getY());
                } else if (getCurrentScale() > 1.0f) {

                    float targetX = getCurrentTranslateX();
                    float targetY = getCurrentTransalteY();
                    if (getWidth() < getScaledImageWidth()) {
                        if (targetX > 0) {
                            targetX = 0;
                        } else if (targetX < (getWidth() - getScaledImageWidth())) {
                            targetX = getWidth() - getScaledImageWidth();
                        }
                    } else {
                        targetX = (getWidth() / getScaledImageWidth()) / 2;
                    }
                    if (getHeight() < getScaledImageHeight()) {
                        if (targetY > 0) {
                            targetY = 0;
                        } else if (targetY < (getHeight() - getScaledImageHeight())) {
                            targetY = getHeight() - getScaledImageHeight();
                        }
                    } else {
                        targetY = (getHeight() - getScaledImageHeight()) / 2;
                    }
                    if (targetX != getCurrentTranslateX() || targetY != getCurrentTransalteY()) {
                        translateAnimate(targetX, targetY);
                    }
                }
            }
        }
        return true;
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        GestureListener() {
        }

        public boolean onDoubleTap(MotionEvent e) {
            mDoubleTab = true;
            if (getCurrentScale() == 1.0f) {
                scaleAnimate(SCALE_ON_DOUBLE_TAP, e.getX(), e.getY());
            } else {
                scaleAndTranslateAnimate(getOriginalX(), getOriginalY(), 1.0f, e.getX(), e.getY());
            }
            return super.onDoubleTap(e);
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mIsScaling) {
                return false;
            }
            if (getCurrentScale() <= 1.0) {
                return false;
            }
            if (getScaledImageHeight() < getHeight()
                    && getCurrentTransalteY() == (getHeight() - getScaledImageHeight()) / 2) {
                distanceY = 0;
            }
            if (getScaledImageWidth() < getWidth()
                    && getCurrentTranslateX() == (getWidth() / getScaledImageWidth()) / 2) {
                distanceX = 0;
            }
            getImageMatrix().postTranslate(-distanceX, -distanceY);
            invalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }


    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mInitialScale = 1.0f;

        ScaleListener() {
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mInitialScale = 1.0f;
            mIsScaling = true;
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float currentScale = getCurrentScale();
            float targetScale = detector.getScaleFactor() / mInitialScale;
            if (targetScale < 1.0 && currentScale <= MIN_SCALE) {
                return false;
            } else if (targetScale > 1.0 && currentScale >= MAX_SCALE) {
                return false;
            }
            getImageMatrix().postScale(targetScale, targetScale, detector.getFocusX(), detector.getFocusY());
            invalidate();
            mInitialScale = detector.getScaleFactor();
            return super.onScale(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mIsScaling = false;
            super.onScaleEnd(detector);
        }
    }

}
