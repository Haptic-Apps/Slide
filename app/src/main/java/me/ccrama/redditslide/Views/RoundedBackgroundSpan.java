package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.text.style.ReplacementSpan;

/**
 * Created by carlo_000 on 3/11/2016.
 */
public class RoundedBackgroundSpan extends ReplacementSpan {

    private static int CORNER_RADIUS = 8;
    private int backgroundColor = 0;
    private int textColor = 0;
    boolean half;

    public RoundedBackgroundSpan(Context context, @ColorRes int textColor, @ColorRes int backgroundColor, boolean half) {
        super();
        this.backgroundColor = context.getResources().getColor(backgroundColor);
        this.textColor = context.getResources().getColor(textColor);
        this.half = half;
    }

    public RoundedBackgroundSpan(@ColorInt int textColor, @ColorInt int backgroundColor, boolean half) {
        super();
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.half = half;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        RectF rect = new RectF(x, top - ((top - bottom) * (half ? 0.25f : 0f)), x + measureText(paint, text, start, end), bottom + ((top - bottom) * (half ? 0.25f : 0f)));
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);

        float baseLine = paint.getFontSpacing() * (half?0.75f:0.5f);
        if(half) paint.setFakeBoldText(true);
        canvas.drawText(text, start, end, x, rect.bottom - ((rect.bottom - rect.top - baseLine) / 2), paint); //center the text in the parent span
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }
}