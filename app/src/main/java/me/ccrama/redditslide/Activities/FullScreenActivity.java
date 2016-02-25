package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.Window;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 *
 * This Activity allows for fullscreen viewing without the statusbar visible
 */
public class FullScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //TODO something like this getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
             //   WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        overridePendingTransition(R.anim.slide_up_fade_in, 0);

        setRecentBar(null, Palette.getDefaultColor());

    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down_fade_out);
    }


}
