package me.ccrama.redditslide.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * A simple ViewPager subclass that allows swiping between pages to be enabled or disabled at
 * runtime.
 */
public class ToggleSwipeViewPager extends ViewPager {
    private boolean mEnableSwiping = true;
    private boolean swipeLeftOnly = false;
    private boolean mSwipeDisabledUntilRelease = false;
    private float mStartDragX;

    public ToggleSwipeViewPager(Context context) {
        super(context);
    }

    public ToggleSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return (mEnableSwiping || swipeLeftOnly) && super.onTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mSwipeDisabledUntilRelease) {
                mEnableSwiping = true;
                mSwipeDisabledUntilRelease = false;
            }
        }
        try {
            return (mEnableSwiping || swipeLeftOnly) && super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setSwipeLeftOnly(boolean enabled) {
        swipeLeftOnly = enabled;
    }

    public void setSwipingEnabled(boolean enabled) {
        mEnableSwiping = enabled;
    }

    public void disableSwipingUntilRelease() {
        mEnableSwiping = false;
        mSwipeDisabledUntilRelease = true;
    }

}
