package me.ccrama.redditslide.ForceTouch.util;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import me.ccrama.redditslide.ForceTouch.PeekViewActivity;
import me.ccrama.redditslide.ForceTouch.builder.Peek;


public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private PeekViewActivity activity;
    private View             base;
    private Peek             peek;

    public GestureListener(PeekViewActivity activity, View base, Peek peek) {
        this.activity = activity;
        this.base = base;
        this.peek = peek;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        base.performClick();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        peek.show(activity, event);
    }
}
