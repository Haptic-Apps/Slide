package me.ccrama.redditslide.Views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by carlo_000 on 10/12/2015.
 */
public class PreCachingLayoutManagerComments extends LinearLayoutManager {
    private static final int DEFAULT_EXTRA_LAYOUT_SPACE = 0;
    private int extraLayoutSpace = 0;
    private Context context;

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
        if (extraLayoutSpace > 0) {
            return extraLayoutSpace;
        }
        return DEFAULT_EXTRA_LAYOUT_SPACE;
    }
}