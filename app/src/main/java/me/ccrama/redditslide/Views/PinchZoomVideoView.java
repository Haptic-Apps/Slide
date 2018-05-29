package me.ccrama.redditslide.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.VideoView;

/**
 * Code from https://github.com/father2sisters/scale_videoview
 */
public class PinchZoomVideoView extends VideoView {

    ScaleGestureDetector mScaleGestureDetector;
    GestureDetector      mGestureDetector;

    public PinchZoomVideoView(Context context) {
        super(context);
        mScaleGestureDetector =
                new ScaleGestureDetector(getContext(), new MyScaleGestureListener());
        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    public PinchZoomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinchZoomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Resize video view by using SurfaceHolder.setFixedSize(...). See {@link android.view.SurfaceHolder#setFixedSize}
     *
     * @param width
     * @param height
     */
    public void setFixedVideoSize(int width, int height) {
        getHolder().setFixedSize(width, height);
    }

    private class MyScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {
        private int mW, mH;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // scale our video view
            mW *= detector.getScaleFactor();
            mH *= detector.getScaleFactor();
            if (mW < 200) { // limits width
                mW = getWidth();
                mH = getHeight();
            }
            Log.d("onScale", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
            setFixedVideoSize(mW, mH); // important
            getLayoutParams().width = mW;
            getLayoutParams().height = mH;
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mW = getWidth();
            mH = getHeight();
            Log.d("onScaleBegin", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d("onScaleEnd", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
        }

    }
}