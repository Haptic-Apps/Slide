package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

import com.devspark.robototextview.RobotoTypefaces;

/**
 * Created by carlo_000 on 3/11/2016.
 */
public class RoundedBackgroundSpan extends ReplacementSpan {

    private int backgroundColor = 0;
    private int textColor = 0;
    private boolean half;
    private Context c;

    public RoundedBackgroundSpan(Context context, @ColorRes int textColor, @ColorRes int backgroundColor, boolean half) {
        super();
        this.backgroundColor = context.getResources().getColor(backgroundColor);
        this.textColor = context.getResources().getColor(textColor);
        this.half = half;
        this.c = context;
    }

    public RoundedBackgroundSpan(@ColorInt int textColor, @ColorInt int backgroundColor, boolean half, Context context) {
        super();
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.half = half;
        this.c = context;
    }


    @Override
    public void draw(Canvas canvas, CharSequence oldText, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        int offset = 0;
        if (half) {
            offset = (bottom - top) / 6;
        }

        paint.setTypeface(RobotoTypefaces.obtainTypeface(c, RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_BOLD));

        if (half) {
            paint.setTextSize(paint.getTextSize() / 2);
        }

        final RectF rect = new RectF(x, top + offset, x + measureText(paint, oldText, start, end), bottom - offset);
        paint.setColor(backgroundColor);
        final int CORNER_RADIUS = 8;
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);

        final float baseLine = paint.descent();
        canvas.drawText(oldText, start, end, x, rect.bottom - ((rect.bottom - rect.top) / 2) + (baseLine * 1.5f), paint); //center the text in the parent span
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        paint.setTypeface(RobotoTypefaces.obtainTypeface(c, RobotoTypefaces.TYPEFACE_ROBOTO_CONDENSED_BOLD));
        final int size = Math.round(paint.measureText(text, start, end));

        if (half) {
            return size / 2;
        } else {
            return size;
        }
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}