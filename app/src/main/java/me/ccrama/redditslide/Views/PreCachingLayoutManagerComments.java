package me.ccrama.redditslide.Views;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 10/12/2015.
 */
public class PreCachingLayoutManagerComments extends LinearLayoutManager {
    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 900;
    private final Context context;
    private int extraLayoutSpace = 0;
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            LogUtil.v("Met a IOOBE in RecyclerView");
        }
    }
    public PreCachingLayoutManagerComments(Context context) {
        super(context);
        this.context = context;
    }

    public PreCachingLayoutManagerComments(Context context, int extraLayoutSpace) {
        super(context);
        this.context = context;
        this.extraLayoutSpace = extraLayoutSpace;
    }

    public PreCachingLayoutManagerComments(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.context = context;
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        this.extraLayoutSpace = extraLayoutSpace;
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
     /*   if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE;
        */
        return super.getExtraLayoutSpace(state);

    }
}