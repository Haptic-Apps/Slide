package me.ccrama.redditslide.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.ColorRes;

import com.makeramen.roundedimageview.RoundedImageView;

/**
 * Created by Carlos on 9/13/2016.
 */

public class RoundImageTriangleView extends RoundedImageView {

    public RoundImageTriangleView(Context context) {
        super(context);
    }

    public RoundImageTriangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RoundImageTriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    int color = Color.TRANSPARENT;

    public void setFlagColor(@ColorRes int color){
        this.color = color;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* maybe soon int w = getWidth() / 5;

        Path path = new Path();
        path.moveTo( w*4, 0);
        path.lineTo( 5 * w , 0);
        path.lineTo( 5 * w , w);
        path.lineTo( w*4 , 0);
        path.close();

        Paint p = new Paint();
        p.setColor( color );

        canvas.drawPath(path, p);*/
    }


}