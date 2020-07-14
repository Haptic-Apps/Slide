package me.ccrama.redditslide.Fragments;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by PKhurana on 7/8/16.
 * Adopted from http://illusionsandroid.blogspot.com/2011/05/adding-fling-gesture-listener-to-view.html
 */
public abstract class OnFlingGestureListener implements View.OnTouchListener {

    private final GestureDetector gdt = new GestureDetector(null, new GestureListener());

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        return gdt.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 60;
        private static final int SWIPE_THRESHOLD_VELOCITY = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onRightToLeft();
                return true;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                onLeftToRight();
                return true;
            }
            if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onBottomToTop();
                return true;
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                onTopToBottom();
                return true;
            }
            return false;
        }
    }

    public abstract void onRightToLeft();

    public abstract void onLeftToRight();

    public abstract void onBottomToTop();

    public abstract void onTopToBottom();

}

