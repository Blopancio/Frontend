package com.example.blopa.frontendapp;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.PointF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.Color;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private GestureDetectorCompat mDetector;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
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
            // Delayed display of UI elements
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
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        mDetector = new GestureDetectorCompat(this,new MyGestureListener(this));
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onTouchEvent(event);
            }
        });


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button0).setOnTouchListener(mDelayHideTouchListener);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
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
        image.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.tercerpisodcc));
    }

    public void setPosition(View view) {
        PointF point= new PointF(20,20);
        ShapeDrawable sd = new ShapeDrawable(new OvalShape());
        sd.setIntrinsicHeight(100);
        sd.setIntrinsicWidth(100);
        sd.getPaint().setColor(Color.parseColor("#abcd123"));
        ImageView iv = (ImageView) findViewById(R.id.fullscreen_content);
        iv.setBackground(sd);
        ImageView image= ((ImageView)this.findViewById(R.id.fullscreen_content));
    }
}
class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String DEBUG_TAG = "Gestures";
    private FullscreenActivity fsActivity;
    private Boolean singleTap= true;
    private Boolean zoom= true;

    MyGestureListener(FullscreenActivity fsActivity){
        this.fsActivity=fsActivity;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        ((TextView)fsActivity.findViewById(R.id.CoordinateX)).setText(String.format("Coordinate X: %s", event.getX()));
        ((TextView)fsActivity.findViewById(R.id.CoordinateY)).setText(String.format("Coordinate Y: %s", event.getY()));
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }
    /*
        @Override
        public void onLongPress(MotionEvent event) {
            Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        }
    */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        if(zoom) {
            ((ImageView) fsActivity.findViewById(R.id.fullscreen_content)).setScaleType(ImageView.ScaleType.CENTER);
            zoom=false;
        }
        else {
            ((ImageView) fsActivity.findViewById(R.id.fullscreen_content)).setScaleType(ImageView.ScaleType.CENTER_CROP);
            zoom=true;
        }
        singleTap= false;
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {

        if(singleTap) {
            fsActivity.toggle();
            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
            return true;
        }
        singleTap=true;
        return true;
    }

}