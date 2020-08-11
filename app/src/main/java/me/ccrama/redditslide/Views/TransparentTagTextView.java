package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import me.ccrama.redditslide.R;

/**
 * Created by carlos on 3/14/16.
 */
public class TransparentTagTextView extends AppCompatTextView {
    Bitmap mMaskBitmap;
    Canvas mMaskCanvas;
    Paint mPaint;

    Drawable mBackground;
    Bitmap mBackgroundBitmap;
    Canvas mBackgroundCanvas;
    boolean mSetBoundsOnSizeAvailable = false;

    public TransparentTagTextView(Context context) {
        super(context);

        init(context);
    }

    public TransparentTagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mSetBoundsOnSizeAvailable = true;
        mPaint = new Paint();
        super.setTextColor(Color.BLACK);
        super.setBackground(new ColorDrawable(Color.TRANSPARENT));
        setBackground(context.getResources().getDrawable(R.drawable.flairback));
    }


    Drawable backdrop;
    public void resetBackground(Context context) {
        mPaint = new Paint();
        super.setTextColor(Color.BLACK);
        super.setBackground(new ColorDrawable(Color.TRANSPARENT));
        backdrop = context.getResources().getDrawable(R.drawable.flairback);
        setBackground(backdrop);
    }

    @Override
    public void setBackground(Drawable bg) {
        if(bg != null) {
            mBackground = bg;
            int w = bg.getIntrinsicWidth();
            int h = bg.getIntrinsicHeight();

            // Drawable has no dimensions, retrieve View's dimensions
            if (w == -1 || h == -1) {
                w = getWidth();
                h = getHeight();
            }

            // Layout has not run
            if (w == 0 || h == 0) {
                mSetBoundsOnSizeAvailable = true;
                return;
            }

            mBackground.setBounds(0, 0, w, h);
        }
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        setBackground(new ColorDrawable(color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w > 0 && h > 0) {
            mBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mBackgroundCanvas = new Canvas(mBackgroundBitmap);
            mMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mMaskCanvas = new Canvas(mMaskBitmap);

            if (mSetBoundsOnSizeAvailable) {
                mBackground.setBounds(0, 0, w, h);
                mSetBoundsOnSizeAvailable = false;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.setBackground(backdrop);

        // Draw background
        mBackground.draw(mBackgroundCanvas);

        // Draw mask
        if(mMaskCanvas != null) {
            mMaskCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
            super.onDraw(mMaskCanvas);
            mBackgroundCanvas.drawBitmap(mMaskBitmap, 0.f, 0.f, mPaint);
            canvas.drawBitmap(mBackgroundBitmap, 0.f, 0.f, null);
        }
    }
}