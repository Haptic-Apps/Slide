package me.ccrama.redditslide.Views;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;

/**
 * A custom implementation of {@link QuoteSpan} that implements stripe and gap width
 * while maintaining support for APIs 27 and below. Also adds transparent background support.
 */
public class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {

    @ColorInt
    private final int mColor;
    @Px
    private final int mStripeWidth;
    @Px
    private final int mGapWidth;

    public CustomQuoteSpan(@ColorInt int color, @IntRange(from = 0) int stripeWidth,
                           @IntRange(from = 0) int gapWidth) {
        mColor = color;
        mStripeWidth = stripeWidth;
        mGapWidth = gapWidth;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return mStripeWidth + mGapWidth;
    }

    @Override
    public void drawLeadingMargin(@NonNull Canvas c, @NonNull Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  @NonNull CharSequence text, int start, int end,
                                  boolean first, @NonNull Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        c.drawRect(x, top, x + dir * mStripeWidth, bottom, p);

        p.setStyle(style);
        p.setColor(color);
    }

    @Override
    public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                               @Px int left, @Px int right,
                               @Px int top, @Px int baseline, @Px int bottom,
                               @NonNull CharSequence text, int start, int end,
                               int lineNumber) {
        final int paintColor = paint.getColor();
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(paintColor);
    }
}
