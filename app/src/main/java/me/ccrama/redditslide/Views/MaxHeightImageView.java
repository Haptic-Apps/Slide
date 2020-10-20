package me.ccrama.redditslide.Views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Carlos on 6/2/2016.
 */
public class MaxHeightImageView extends AppCompatImageView {
    public static final int maxHeight = 3200;

    public MaxHeightImageView(Context context) {
        super(context);
    }

    public MaxHeightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);

        switch (hMode) {
            case MeasureSpec.AT_MOST:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
                break;
            case MeasureSpec.UNSPECIFIED:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                break;
            case MeasureSpec.EXACTLY:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
                break;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
