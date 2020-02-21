package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import io.codetail.animation.RevealViewGroup;
import io.codetail.animation.ViewRevealManager;

//Adapted from https://github.com/MajeurAndroid/CircularReveal/commit/a87e3ad4daac96f942be0e240ebfc098a79f5419

public class RevealRelativeLayout extends RelativeLayout implements RevealViewGroup {
    private ViewRevealManager manager;
    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    public RevealRelativeLayout(Context context) {
        this(context, null);
    }

    public RevealRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        manager = new ViewRevealManager();
    }

    @Override protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        try {
            canvas.save();

            manager.transform(canvas, child);
            return super.drawChild(canvas, child, drawingTime);
        } finally {
            canvas.restore();
        }
    }

    @Override public ViewRevealManager getViewRevealManager() {
        return manager;
    }
}