package me.ccrama.redditslide.Views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A simple ViewPager subclass that allows swiping between pages to be enabled or disabled at
 * runtime.
 */
public class ToggleSwipeViewPager extends ViewPager {
    private boolean mEnableSwiping = true;
    private boolean swipeLeftOnly = false;

    public ToggleSwipeViewPager(Context context) {
        super(context);
    }

    public ToggleSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return (mEnableSwiping && super.onInterceptTouchEvent(event)) || (!mEnableSwiping && swipeLeftOnly && IsSwipeAllowed(event) && super.onInterceptTouchEvent(event));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (mEnableSwiping && super.onTouchEvent(event)) || (!mEnableSwiping && swipeLeftOnly && IsSwipeAllowed(event) && super.onTouchEvent(event));
    }
    private float initialXValue;
    private SwipeDirection direction;
    public enum SwipeDirection {
        all, left, right, none ;
    }
    public void setSwipeLeftOnly(boolean enabled) {
        swipeLeftOnly = enabled;
        if(enabled){
            setAllowedSwipeDirection(SwipeDirection.left);
        } else {
            setAllowedSwipeDirection(SwipeDirection.all);
        }
    }

    public void setSwipingEnabled(boolean enabled) {
        mEnableSwiping = enabled;
    }

    private boolean IsSwipeAllowed(MotionEvent event) {
        if(this.direction == SwipeDirection.all) return true;

        if(direction == SwipeDirection.none )//disable any swipe
            return false;

        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            initialXValue = event.getX();
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_MOVE) {
            try {
                float diffX = event.getX() - initialXValue;
                if (diffX > 0 && direction == SwipeDirection.right ) {
                    // swipe from left to right detected
                    return false;
                }else if (diffX < 0 && direction == SwipeDirection.left ) {
                    // swipe from right to left detected
                    return false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return true;
    }

    public void setAllowedSwipeDirection(SwipeDirection direction) {
        this.direction = direction;
    }
}