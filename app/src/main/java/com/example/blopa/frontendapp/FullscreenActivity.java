package com.example.blopa.frontendapp;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
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

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //private static final float MIN_ZOOM = 0.005f,MAX_ZOOM = 100f;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;
    private float[] values;

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
    static private int MAX_DURATION_CLICK = 250;
    private float accScale = oldDist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnTouchListener(this);
        startTime=0;


        // Set up the user interaction to manually show or hide the system UI
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale = oldDist;

        //dumpEvent(event);
        // Handle touch events here...

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                startTime = System.currentTimeMillis();
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d("Probando", "mode=DRAG"); // write to LogCat
                mode = DRAG;
                int[] values = new int[2];
                view.getLocationOnScreen(values);
                Log.d("X & Y",values[0]+" "+values[1]);
                break;

            case MotionEvent.ACTION_UP: // first finger lifted
                long time = System.currentTimeMillis() - startTime;
                if(start.equals(event.getX(),event.getY())) {
                    if (time > MAX_DURATION_CLICK) {
                        ((TextView) findViewById(R.id.CoordinateX)).setText(String.format("Coordinate X: %s", event.getX()));
                        ((TextView) findViewById(R.id.CoordinateY)).setText(String.format("Coordinate Y: %s", event.getY()));
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
                        scale = newDist / oldDist;
                         // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                       /* Log.d("asdfSC", ""+scale);
                        if(accScale > MAX_ZOOM && scale > 1)
                            scale=1;
                        else if(accScale < MIN_ZOOM && scale < 1)
                            scale=1;
                        accScale=accScale*scale;
                        Log.d("asdfAC", ""+accScale);*/
                        matrix.postScale(scale, scale, mid.x, mid.y);
                        
                    }
                }
                break;
        }
        view.setMaxHeight(760);
        view.setMaxWidth(1200);
        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }
    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private float rotation(MotionEvent event)
    {
        float x = start.x - event.getX(0);
        float y = start.y - event.getY(0);
        return 0;
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
        image.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.tercerpisodcc));
        Log.d("setMap", "("+image.getMeasuredWidth()+","+image.getMeasuredHeight()+")");
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
