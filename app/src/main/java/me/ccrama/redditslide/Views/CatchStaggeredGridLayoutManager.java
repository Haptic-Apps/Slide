package me.ccrama.redditslide.Views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 4/8/2016.
 */
public class CatchStaggeredGridLayoutManager extends StaggeredGridLayoutManager {
    public CatchStaggeredGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CatchStaggeredGridLayoutManager(int spanCount, int orientation) {
        super(spanCount, orientation);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            LogUtil.v("Met a IOOBE in RecyclerView");
        }
    }
}
