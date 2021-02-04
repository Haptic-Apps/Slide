package me.ccrama.redditslide.handler;

import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ccrama on 2/18/2016.
 * Adapted from http://rylexr.tinbytes.com/2015/04/27/how-to-hideshow-android-toolbar-when-scrolling-google-play-musics-behavior/
 */
public class ToolbarScrollHideHandler extends RecyclerView.OnScrollListener {

    public int verticalOffset;
    public boolean reset = false;
    Toolbar tToolbar;
    View mAppBar;
    View extra;
    View opposite;
    boolean scrollingUp;

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

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (reset) {
                verticalOffset = 0;
                reset = false;
            }
            if (scrollingUp) {
                if (verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow();
                }
                if (opposite != null)
                    if (verticalOffset > opposite.getHeight()) {
                        oppositeAnimateHide();
                    } else {
                        oppositeAnimateShow();
                    }
            } else {
                if (mAppBar.getTranslationY() < tToolbar.getHeight() * -0.6 && verticalOffset > tToolbar.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow();
                }
                if (opposite != null)
                    if (opposite.getTranslationY() < opposite.getHeight() * -0.6 && verticalOffset > opposite.getHeight()) {
                        oppositeAnimateHide();
                    } else {
                        oppositeAnimateShow();
                    }
            }
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (verticalOffset == 0 && dy < 0) { //if scrolling begins halfway through an adapter, don't treat it like going negative and instead reset the start position to 0
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
                toolbarShow();
                if (extra != null)
                    extra.setTranslationY(0);
            } else {
                mAppBar.setTranslationY(-toolbarYOffset);
                if (extra != null)
                    extra.setTranslationY(-toolbarYOffset);
            }
        }
        if (opposite != null) {
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

    public void toolbarShow() {
        mAppBar.setTranslationY(0);
    }

    private void toolbarAnimateShow() {
        toolbarAnimate(0);
    }

    private void toolbarAnimateHide() {
        toolbarAnimate(-tToolbar.getHeight());
    }

    private void toolbarAnimate(final int i) {
        animate(mAppBar, i);
        if (extra != null)
            animate(extra, i);
    }

    private void oppositeAnimateShow() {
        oppositeAnimate(0);
    }

    private void oppositeAnimateHide() {
        oppositeAnimate(opposite.getHeight());
    }

    private void oppositeAnimate(final int i) {
        animate(opposite, i);
    }

    private void animate(final View v, final int i) {
        v.animate()
                .translationY(i)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
    }
}
