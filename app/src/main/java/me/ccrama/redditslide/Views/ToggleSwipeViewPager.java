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

    public ToggleSwipeViewPager(Context context) {
        super(context);
    }

    public ToggleSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mEnableSwiping && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mEnableSwiping && super.onTouchEvent(event);
    }

    public void setSwipingEnabled(boolean enabled) {
        mEnableSwiping = enabled;
    }
}