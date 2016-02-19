package me.ccrama.redditslide.handler;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by ccrama on 2/18/2016.
 * Adapted from http://rylexr.tinbytes.com/2015/04/27/how-to-hideshow-android-toolbar-when-scrolling-google-play-musics-behavior/
 */
public class ToolbarScrollHideHandler extends RecyclerView.OnScrollListener {

    Toolbar tToolbar;
    View mAppBar;

    public ToolbarScrollHideHandler(Toolbar t, View appBar) {
        tToolbar = t;
        mAppBar = appBar;
    }


    int verticalOffset;

    boolean scrollingUp;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollingUp) {
                if (verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            } else {
                if (mAppBar.getTranslationY() < tToolbar.getHeight() * -0.6 && verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        verticalOffset += dy;
        scrollingUp = dy > 0;
        int toolbarYOffset = (int) (dy - mAppBar.getTranslationY());
        mAppBar.animate().cancel();
        if (scrollingUp) {
            if (toolbarYOffset < tToolbar.getHeight()) {
                mAppBar.setTranslationY(-toolbarYOffset);
            } else {
                mAppBar.setTranslationY(-tToolbar.getHeight());
            }
        } else {
            if (toolbarYOffset < 0) {
                mAppBar.setTranslationY(0);
            } else {
                mAppBar.setTranslationY(-toolbarYOffset);
            }
        }
    }

    private void toolbarAnimateShow(final int verticalOffset) {
        mAppBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }

    private void toolbarAnimateHide() {
        mAppBar.animate()
                .translationY(-tToolbar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }
}
