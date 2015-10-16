package me.ccrama.redditslide.Visuals;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import codetail.graphics.drawables.DrawableHotspotTouch;
import codetail.graphics.drawables.LollipopDrawable;
import codetail.graphics.drawables.LollipopDrawablesCompat;
import me.ccrama.redditslide.R;

/**
 * Created by carlo_000 on 10/12/2015.
 */
public class RippleDraw {
    public static void forceRippleAnimation(View view, Context c) {
        view.setBackgroundDrawable(getDrawable2(R.drawable.ripple, c));
        view.setClickable(true);// if we don't set it true, ripple will not be played
        view.setOnTouchListener(
                new DrawableHotspotTouch((LollipopDrawable) view.getBackground()));
    }

    public static Drawable getDrawable2(int id, Context c){
        return LollipopDrawablesCompat.getDrawable(c.getResources(), id, c.getTheme());
    }

}