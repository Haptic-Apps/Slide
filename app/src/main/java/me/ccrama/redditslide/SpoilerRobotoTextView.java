package me.ccrama.redditslide;

import android.content.Context;
import android.util.AttributeSet;

import com.devspark.robototextview.widget.RobotoTextView;

/**
 * Created by carlo_000 on 1/11/2016.
 */
public class SpoilerRobotoTextView extends RobotoTextView {
    public SpoilerRobotoTextView(Context context) {
        super(context);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpoilerRobotoTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isSpoilerClicked() {
        return spoilerClicked;
    }

    public void resetSpoilerClicked() {
        spoilerClicked = false;
    }

    public boolean spoilerClicked = false;

}
