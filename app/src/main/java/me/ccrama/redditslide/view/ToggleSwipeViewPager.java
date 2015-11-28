package me.ccrama.redditslide.view;

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
        if (mEnableSwiping) return super.onInterceptTouchEvent(event);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEnableSwiping) return super.onTouchEvent(event);
        return false;
    }

    public void setSwipingEnabled(boolean enabled) {
        mEnableSwiping = enabled;
    }
}