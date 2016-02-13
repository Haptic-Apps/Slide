package me.ccrama.redditslide.Activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 *
 * This Activity allows for fullscreen viewing without the statusbar visible
 */
public class FullScreenActivity extends BaseActivityAnim {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        findViewById(android.R.id.content).setPadding(0, dpToPx(20), 0, 0);

        setRecentBar(null, Palette.getDefaultColor());
    }
    public  int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
