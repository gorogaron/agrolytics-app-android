package com.agrolytics.agrolytics_android.utils.drawView;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;

public class DrawView extends ImageView {

    Paint zoomPaint = new Paint();
    Paint pointPaint = new Paint();
    Paint circlePaint = new Paint();
    Paint linesPaint = new Paint();

    ArrayList<Rect> lines = new ArrayList<Rect>();
    int x1, y1;
    int x2, y2;
    boolean drawing = false;
    boolean isTop = false;
    private int circleRadius = 60;
    private Circle topCircle = new Circle();
    private Circle bottomCircle = new Circle();
    private Activity activity;
    private Bitmap bitmap;
    private int firstInit = 0;
    private Double originalScaleY = 1.0;
    private Double originalScaleX = 1.0;

    // ZOOM
    private Matrix matrix;
    private int maximumX;
    private PointF zoomPos;
    private int maxY = 0;
    private int maxX = 0;

    public DrawView(Activity activity) {
        super(activity);
        this.activity = activity;
        init();
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //lines.add(new Rect(150,100,150,400));
        pointPaint.setColor(Color.RED);
        // ZOOM
        matrix = new Matrix();
        zoomPaint.setStrokeWidth(10f);
        zoomPaint.setColor(Color.GREEN);
        zoomPos = new PointF(0, 0);
        getMaximumCoordinates();
        this.setScaleType(ScaleType.CENTER_INSIDE);
        this.setAdjustViewBounds(true);
    }

    public void setImage(Bitmap image, double originalScaleX, double originalScaleY) {
        setImageBitmap(image);
//        setBackgroundColor(Color.WHITE);

        maxY = image.getHeight();
        maxX = image.getWidth();

        this.originalScaleX = originalScaleX;
        this.originalScaleY = originalScaleY;

        //Drawable d = new BitmapDrawable(getResources(), image);

        Log.d("HGXQR", "loaded bitmap height: " + image.getHeight());
        //Log.d("HGXQR", "loaded drawable height: " + d.getIntrinsicHeight());
    }

    public void setDefaultLine() {
        lines.add(new Rect(150, 100, 150, 400));
        invalidate();
    }

    private void getMaximumCoordinates() {
        if (activity != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            maximumX = size.x;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        linesPaint.setColor(Color.GREEN);
        linesPaint.setStrokeWidth(5f);

        Log.d("IMAGE", "this height: " + this.getHeight());

        int ih = this.getMeasuredHeight();//height of imageView
        int iw = this.getMeasuredWidth();//width of imageView
        int iH = this.getDrawable().getIntrinsicHeight();//original height of underlying image
        int iW = this.getDrawable().getIntrinsicWidth();//original width of underlying image

        Log.d("IMAGE", "height of imageView: " + ih);

        Log.d("IMAGE", "original height of underlying image: " + iH);


        // If you build drawing cache by drawing a line the default bar won't stuck on the screen
        if (firstInit == 0) {
            buildDrawingCache();
            canvas.drawLine(100, 100, 100, 100, linesPaint);
            bitmap = getDrawingCache();
            firstInit++;
        }

        circlePaint.setStrokeWidth(5f);
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.STROKE);

        if (lines.size() != 0) {
            Rect last = lines.get(lines.size() - 1);

            if (drawing) {
                if (isTop) {
                    canvas.drawLine(x1, y1, last.right, last.bottom, linesPaint);
                    // Top circle
                    drawTopCircle(canvas, x1, y1);
                    // Bottom circle
                    drawBottomCircle(canvas, last.right, last.bottom);
                } else {
                    canvas.drawLine(last.left, last.top, x2, y2, linesPaint);
                    // Top circle
                    drawTopCircle(canvas, last.left, last.top);
                    // Bottom circle
                    drawBottomCircle(canvas, x2, y2);
                }
                // ZOOM
                zoom(canvas);
            } else {
                Log.d("Touch event", "STOPPED drawing");
                buildDrawingCache();
                if (isTop) {
                    canvas.drawLine(last.left, last.top, last.right, last.bottom, linesPaint);
                    // Top circle
                    drawTopCircle(canvas, last.left, last.top);
                    // Bottom circle
                    drawBottomCircle(canvas, last.right, last.bottom);
                } else {
                    canvas.drawLine(last.left, last.top, last.right, last.bottom, linesPaint);
                    // Top circle
                    drawTopCircle(canvas, last.left, last.top);
                    // Bottom circle
                    drawBottomCircle(canvas, last.right, last.bottom);
                }
            }
        }
    }

    private void zoom(Canvas canvas) {

        // Frame of zoom
        Paint framePaint = new Paint();
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(7f);
        framePaint.setColor(Color.GREEN);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        zoomPaint = new Paint();
        zoomPaint.setShader(shader);
        matrix.reset();
        matrix.postScale(2f, 2f, zoomPos.x, zoomPos.y);
        zoomPaint.getShader().setLocalMatrix(matrix);

        int fixedXPosition = (maximumX - 220);
        int fixedYPosition = 220;

        int widthRatio = 1;
        int heightRatio = 1;
        matrix.reset();
        matrix.postScale(2f, 2f, zoomPos.x * widthRatio, zoomPos.y * heightRatio);
        matrix.postTranslate(fixedXPosition - zoomPos.x * widthRatio, fixedYPosition - zoomPos.y * heightRatio);
        zoomPaint.getShader().setLocalMatrix(matrix);
        int sizeOfMagnifier = 200;
        // Zoom circle
        canvas.drawCircle(fixedXPosition, fixedYPosition, sizeOfMagnifier, zoomPaint);
        // Zoom circle frame
        canvas.drawCircle(fixedXPosition, fixedYPosition, sizeOfMagnifier, framePaint);
        canvas.drawCircle(fixedXPosition, fixedYPosition, 5, pointPaint);
        // Finger zoom
        //canvas.drawCircle(zoomPos.x, zoomPos.y, 100, zoomPaint);
    }

    private void drawTopCircle(Canvas canvas, int x, int y) {
        canvas.drawCircle(x, y, circleRadius, circlePaint);
        topCircle.setX(x);
        topCircle.setY(y);
        topCircle.setRadius(circleRadius);
    }

    private void drawBottomCircle(Canvas canvas, int x, int y) {
        canvas.drawCircle(x, y, circleRadius, circlePaint);
        bottomCircle.setX(x);
        bottomCircle.setY(y);
        bottomCircle.setRadius(circleRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        boolean topTouched = getTopTouched(event);
        boolean bottomTouched = getBottomTouched(event);


        isTop = topTouched;

//        if (bitmap.getWidth() > (int) event.getX()) {
//            int pixel = bitmap.getPixel((int) event.getY(), (int) event.getX());
//
//            Log.d("COLOR", "color of pixel" + pixel);
//            Log.d("COLOR", "event x" + (int) event.getX());
//            int redValue = Color.red(pixel);
//            int blueValue = Color.blue(pixel);
//            int greenValue = Color.green(pixel);
//
//            if (pixel == Color.WHITE) {
//                Toast.makeText(getContext(), "White", Toast.LENGTH_SHORT).show();
//            }
//        }

        if (topTouched && bottomTouched) {
            int topY = topCircle.getY();
            int topX = topCircle.getX();
            int bottomY = bottomCircle.getY();
            int bottomX = bottomCircle.getX();

            long distanceFromTop = Math.round(
                    Math.sqrt(
                            (topX - event.getX()) * (topX - event.getX()) +
                                    (topY - event.getY()) * (topY - event.getY())));

            long distanceFromBottom = Math.round(
                    Math.sqrt(
                            (bottomX - event.getX()) * (bottomX - event.getX()) +
                                    (bottomY - event.getY()) * (bottomY - event.getY())));

//            Log.d("Touch event","distance from TOP " + distanceFromTop);
//            Log.d("Touch event","distance from BOTTOM " + + distanceFromBottom);

            Log.d("Touch event", "top touched " + topTouched);
            Log.d("Touch event", "bottom touched " + bottomTouched);

            if (distanceFromTop < distanceFromBottom) {
                bottomTouched = false;
            } else {
                bottomTouched = true;
            }
        }

        // Stops if event y is bigger than the image maximum size. - refactor this

        if (event.getY() > this.getHeight() + 60 || event.getY() < 0) {
            drawing = false;
            invalidate();
            return false;
        }

        if (!topTouched && !bottomTouched) {
            drawing = false;
            invalidate();
            return false;
        } else {
            zoomPos.x = event.getX();
            zoomPos.y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = x2 = (int) event.getX();
                    y1 = y2 = (int) event.getY();
                    drawing = true;
                    result = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (topTouched) {
                        //isTop = true;
                        x1 = (int) event.getX();
                        y1 = (int) event.getY();
                    } else {
                        //isTop = false;
                        x2 = (int) event.getX();
                        y2 = (int) event.getY();
                    }
                    result = true;
                    break;
                case MotionEvent.ACTION_UP:
                    if (topTouched) {
                        x1 = (int) event.getX();
                        y1 = (int) event.getY();
                    } else {
                        x2 = (int) event.getX();
                        y2 = (int) event.getY();
                    }
                    if (topTouched) {
                        updateTopLine();
                    } else {
                        updateBottomLine();
                    }
                    drawing = false;
                    result = true;
                    break;
            }

            if (result) {
                invalidate();
            }
            return result;
        }
    }

    private void updateTopLine() {
        Rect last = lines.get(lines.size() - 1);
//        last.left = x1;
//        last.top = y1;
        if (lines.size() != 0) {
            lines.remove(0);
        }
        int newX1 = x1;
        int newY1 = y1;
        int oldX2 = last.right;
        int oldY2 = last.bottom;
        lines.add(new Rect(newX1, newY1, oldX2, oldY2));
    }

    private void updateBottomLine() {
        Rect last = lines.get(lines.size() - 1);
        last.right = x2;
        last.bottom = y2;
    }

    private boolean getTopTouched(MotionEvent event) {
        if (topCircle.getX() != null && topCircle.getY() != null) {
            double centerX = topCircle.getX();
            double centerY = topCircle.getY();
            double distanceX = event.getX() - centerX;
            double distanceY = event.getY() - centerY;
            return (distanceX * distanceX) + (distanceY * distanceY) <= circleRadius * circleRadius;
        } else {
            return false;
        }
    }

    private boolean getBottomTouched(MotionEvent event) {
        if (bottomCircle.getX() != null && bottomCircle.getY() != null) {
            double centerX = bottomCircle.getX();
            double centerY = bottomCircle.getY();
            double distanceX = event.getX() - centerX;
            double distanceY = event.getY() - centerY;
            return (distanceX * distanceX) + (distanceY * distanceY) <= circleRadius * circleRadius;
        } else {
            return false;
        }
    }

    private Double yScale() {
        return (double) 480 / (double) maxY;
    }

    private Double xScale() {
        return (double) 640 / (double) maxX;
    }

    private Double distanceXY(int x1, int y1, int x2, int y2) {
        int xSquare = (x2 - x1) * (x2 - x1);
        int ySquare = (y2 - y1) * (y2 - y1);
        return Math.sqrt(xSquare + ySquare);
    }

    private int distance(int cord1, int cord2) {
        return Math.abs(cord2 - cord1);
    }

    public int getPixelCount() {
        Rect line = lines.get(lines.size() - 1);
        int xSquare = (bottomCircle.getX() - topCircle.getX()) * (bottomCircle.getX() - topCircle.getX());
        int ySquare = (bottomCircle.getY() - topCircle.getY()) * (bottomCircle.getY() - topCircle.getY());
        double middleLength = Math.sqrt(xSquare + ySquare);

        // Do not delete this. If we need to count the length from the coordinates, or
        // need to send them to the server this is the way.

//        double xScale = xScale();
//        double yScale = yScale();
//        double dxOrig = distance(topCircle.getX(), bottomCircle.getX());
//        double dyOrig = distance(topCircle.getY(), bottomCircle.getY());
//
//        double dxScaled = dxOrig * xScale * (1 + originalScaleX);
//        double dyScaled = dyOrig * yScale * (1 + originalScaleY);
//
//        double scaledXSquare = dxScaled * dxScaled;
//        double scaledYSquare = dyScaled * dyScaled;
//        double rodLength = Math.sqrt(scaledXSquare + scaledYSquare);


        double bitmapImageRatio = (double) maxY / (double) this.getHeight();
        double originalLength = middleLength * bitmapImageRatio;
        double scaledLength = originalLength * ((double) 480 / (double) maxY);

        Log.d("HGXQR", "bitmapImageRatio " + bitmapImageRatio);
        Log.d("HGXQR", "middleLength " + middleLength);
        Log.d("HGXQR", "originalLength " + originalLength);
        Log.d("HGXQR", "scaledLength " + scaledLength);


        return (int) scaledLength;
    }

}