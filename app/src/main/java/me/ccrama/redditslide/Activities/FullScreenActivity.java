package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.Window;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by tomer aka rosenpin on 11/27/15.
 *
 * This Activity allows for fullscreen viewing without the statusbar visible
 */
public class FullScreenActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Reddit.peek) {
            overridePendingTransition(R.anim.pop_in, 0);
        } else {
            overridePendingTransition(R.anim.slide_in, 0);
        }

        setRecentBar(null, Palette.getDefaultColor());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out);
    }
}
