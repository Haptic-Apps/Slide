package me.ccrama.redditslide.handler;

import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ccrama on 2/18/2016.
 * Adapted from http://rylexr.tinbytes.com/2015/04/27/how-to-hideshow-android-toolbar-when-scrolling-google-play-musics-behavior/
 */
public class ToolbarScrollHideHandler extends RecyclerView.OnScrollListener {

    Toolbar tToolbar;
    View mAppBar;
    View extra;
    View opposite;

    public ToolbarScrollHideHandler(Toolbar t, View appBar) {
        tToolbar = t;
        mAppBar = appBar;
    }

    public ToolbarScrollHideHandler(Toolbar t, View appBar, View extra, View opposite) {
        tToolbar = t;
        mAppBar = appBar;
        this.extra = extra;
        this.opposite = opposite;
    }

    public int verticalOffset;

    boolean scrollingUp;

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if(reset){
                verticalOffset = 0;
                reset = false;
            }
            if (scrollingUp) {
                if (verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
                if (opposite != null)
                    if (verticalOffset > opposite.getHeight()) {
                        oppositeAnimateHide();
                    } else {
                        oppositeAnimateShow(verticalOffset);
                    }
            } else {
                if (mAppBar.getTranslationY() < tToolbar.getHeight() * -0.6 && verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
                if (opposite != null)
                    if (opposite.getTranslationY() < opposite.getHeight() * -0.6 && verticalOffset > opposite.getHeight()) {
                        oppositeAnimateHide();
                    } else {
                        oppositeAnimateShow(verticalOffset);
                    }
            }
        }
    }
    public boolean reset = false;
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if(verticalOffset == 0 && dy < 0){ //if scrolling begins halfway through an adapter, don't treat it like going negative and instead reset the start position to 0
            dy = 0;
        }
        verticalOffset += dy;
        scrollingUp = dy > 0;
        int toolbarYOffset = (int) (dy - mAppBar.getTranslationY());
        mAppBar.animate().cancel();
        if (scrollingUp) {
            if (toolbarYOffset < tToolbar.getHeight()) {
                mAppBar.setTranslationY(-toolbarYOffset);
                if (extra != null)
                    extra.setTranslationY(-toolbarYOffset);
            } else {
                mAppBar.setTranslationY(-tToolbar.getHeight());
                if (extra != null)
                    extra.setTranslationY(-tToolbar.getHeight());
            }
        } else {
            if (toolbarYOffset < 0) {
                mAppBar.setTranslationY(0);
                if (extra != null)
                    extra.setTranslationY(0);
            } else {
                mAppBar.setTranslationY(-toolbarYOffset);
                if (extra != null)
                    extra.setTranslationY(-toolbarYOffset);
            }
        }
        if(opposite != null){
            toolbarYOffset = (int) (dy + opposite.getTranslationY());
            opposite.animate().cancel();
            if (scrollingUp) {
                if (toolbarYOffset < opposite.getHeight()) {
                    opposite.setTranslationY(toolbarYOffset);
                } else {
                    opposite.setTranslationY(opposite.getHeight());
                }
            } else {
                opposite.setTranslationY(Math.max(toolbarYOffset, 0));
            }
        }
    }

    public void toolbarShow()
    {
        mAppBar.setTranslationY(0);
    }

    private void toolbarAnimateShow(final int verticalOffset) {
        mAppBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
        if (extra != null)
            extra.animate()
                    .translationY(0)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(180);
    }

    private void toolbarAnimateHide() {
        mAppBar.animate()
                .translationY(-tToolbar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
        if (extra != null)
            extra.animate()
                    .translationY(-tToolbar.getHeight())
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(180);
    }

    private void oppositeAnimateShow(final int verticalOffset) {
        opposite.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }

    private void oppositeAnimateHide() {
        opposite.animate()
                .translationY(opposite.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }
}
