package com.example.blopa.frontendapp;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.Color;
import android.widget.TextView;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements View.OnTouchListener {

    //private static final boolean AUTO_HIDE = true;
    //private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    Matrix matrix = new Matrix();
    Matrix matrixDot = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    private long startTime;

    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private Drawable drawable;
    private float heightRatio;
    private float widthRatio;
    private float coordinateX;
    private float coordinateY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnTouchListener(this);
        startTime=0;
        heightRatio=0;
        widthRatio=0;
        coordinateX=0;
        coordinateY=0;
        View dot= findViewById(R.id.greenDot);
        ((ImageView)dot).setImageAlpha(0);

        // Set up the user interaction to manually show or hide the system UI
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);


        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                startTime = System.currentTimeMillis();
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d("Probando", "mode=DRAG"); // write to LogCat
                mode = DRAG;
                break;

            case MotionEvent.ACTION_UP: // first finger lifted
                long time = System.currentTimeMillis() - startTime;
                if(start.equals(event.getX(),event.getY())) {
                    int MAX_DURATION_CLICK = 250;
                    if (time > MAX_DURATION_CLICK) {
                        ((TextView) findViewById(R.id.CoordinateX)).setText(String.format("Coordinate X: %s", event.getX()));
                        ((TextView) findViewById(R.id.CoordinateY)).setText(String.format("Coordinate Y: %s", event.getY()));
                        setPosition(event);
                        Log.d("Probando", "mode=LONGPRESS");
                    } else {
                        toggle();
                        Log.d("Probando", "mode=CLICK");
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP: // second finger lifted
                mode = NONE;
                Log.d("Probando", "mode=SECONDFINGERUP");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                start.set(event.getX(), event.getY());
                oldDist = spacing(event);
                Log.d("Probando", "mode=SECONDFINGERDOWN");
                Log.d("Probando", "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d("Probando", "mode=ZOOM");
                }
                break;
/*
            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    Log.d("Imagen de tamaño","("+findViewById(R.id.fullscreen_content).getMeasuredWidth()+","+findViewById(R.id.fullscreen_content).getHeight()+")");
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d("Probando", "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                         // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        //Log.d("asdfSC", ""+scale);
                        //if(accScale > MAX_ZOOM && scale > 1)
                        //    scale=1;
                        //else if(accScale < MIN_ZOOM && scale < 1)
                        //    scale=1;
                        //accScale=accScale*scale;
                        //Log.d("asdfAC", ""+accScale);
                        matrix.postScale(scale, scale, mid.x, mid.y);

                    }
                }
                break;*/
        }
        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }
    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }


    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void setMap(View view) {
        ImageView image= ((ImageView)this.findViewById(R.id.fullscreen_content));
        //image.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.tercerpisodcc));
        image.setBackground(ContextCompat.getDrawable(this,R.drawable.tercerpisodcc));
        setCoordinate(image);
        image.setScaleType(ImageView.ScaleType.MATRIX);
        matrix.postScale(Math.min(1/widthRatio,1/heightRatio),Math.min(1/widthRatio,1/heightRatio));
        image.setImageMatrix(matrix);
        Log.d("setMap", "("+image.getMeasuredWidth()+","+image.getMeasuredHeight()+")");
    }

    private void setCoordinate(ImageView imageView) {
        drawable = imageView.getBackground();

        //original height and width of the bitmap
        int intrinsicHeight = drawable.getIntrinsicHeight();
        int intrinsicWidth = drawable.getIntrinsicWidth();
        Log.d("Intri", intrinsicHeight + " "+ intrinsicWidth);

        //height and width of the visible (scaled) image
        float scaledHeight = imageView.getMeasuredHeight();
        float scaledWidth = imageView.getMeasuredWidth();

        Log.d("scaled", scaledHeight + " "+ scaledWidth);
        //Find the ratio of the original image to the scaled image
        //Should normally be equal unless a disproportionate scaling
        //(e.g. fitXY) is used.
        heightRatio = intrinsicHeight / scaledHeight;
        widthRatio = intrinsicWidth / scaledWidth;

        Log.d("ratios", heightRatio + " "+ widthRatio);
        //do whatever magic to get your touch point
        //MotionEvent event;

        //get the distance from the left and top of the image bounds
        //int scaledImageOffsetX = event.getX() - imageBounds.left;
        //int scaledImageOffsetY = event.getY() - imageBounds.top;

        //scale these distances according to the ratio of your scaling
        //For example, if the original image is 1.5x the size of the scaled
        //image, and your offset is (10, 20), your original image offset
        //values should be (15, 30).
        //int originalImageOffsetX = scaledImageOffsetX * widthRatio;
        //int originalImageOffsetY = scaledImageOffsetY * heightRatio;
    }

    public void setPosition(MotionEvent event) {
        //ImageView iv = (ImageView) findViewById(R.id.fullscreen_content);
        ImageView dot = (ImageView) findViewById(R.id.greenDot);
        dot.setImageAlpha(255);
        dot.setScaleType(ImageView.ScaleType.MATRIX);
        dot.setImageMatrix(matrixDot);
        matrixDot.postTranslate(event.getX()-coordinateX, event.getY()-coordinateY);
        coordinateX = event.getX();
        coordinateY = event.getY();
        dot.setImageMatrix(matrixDot);


    }

    public void RotateLeft(View v) {
        matrix.postTranslate(0,0);
        matrix.postRotate(-90,findViewById(R.id.fullscreen_content).getMeasuredHeight()/2,findViewById(R.id.fullscreen_content).getMeasuredWidth()/2);
        ((ImageView)findViewById(R.id.fullscreen_content)).setImageMatrix(matrix);
    }

    public void RotateRight(View v) {
        matrix.postTranslate(0,0);
        matrix.postRotate(90,findViewById(R.id.fullscreen_content).getMeasuredHeight()/2,findViewById(R.id.fullscreen_content).getMeasuredWidth()/2);
        ((ImageView)findViewById(R.id.fullscreen_content)).setImageMatrix(matrix);

    }
}