package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.youtube.player.YouTubeBaseActivity;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SwipeLayout.SwipeBackLayout;
import me.ccrama.redditslide.SwipeLayout.Utils;
import me.ccrama.redditslide.SwipeLayout.app.SwipeBackActivityBase;
import me.ccrama.redditslide.SwipeLayout.app.SwipeBackActivityHelper;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by carlo_000 on 2/9/2016.
 */
public class BaseYoutubePlayer extends YouTubeBaseActivity implements SwipeBackActivityBase {
    @Nullable
    protected Toolbar mToolbar;
    protected SwipeBackActivityHelper mHelper;
    protected boolean overrideRedditSwipeAnywhere = false;
    protected boolean enableSwipeBackLayout = true;
    protected boolean overrideSwipeFromAnywhere = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_up_fade_in, 0);

        if (enableSwipeBackLayout) {
            mHelper = new SwipeBackActivityHelper(this);
            mHelper.onActivityCreate();
            DisplayMetrics metrics = getResources().getDisplayMetrics();


            if (SettingValues.swipeAnywhere || overrideRedditSwipeAnywhere) {
                if (overrideSwipeFromAnywhere) {
                    Log.v(LogUtil.getTag(), "WONT SWIPE FROM ANYWHERE");
                    mHelper.getSwipeBackLayout().mDragHelper.override = false;

                } else {


                    Log.v(LogUtil.getTag(), "WILL SWIPE FROM ANYWHERE");

                    mHelper.getSwipeBackLayout().mDragHelper.override = true;
                    mHelper.getSwipeBackLayout().setEdgeSize(metrics.widthPixels);

                    Log.v(LogUtil.getTag(), "EDGE SIZE IS " + metrics.widthPixels);
                }
            } else {
                mHelper.getSwipeBackLayout().mDragHelper.override = false;


            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (enableSwipeBackLayout) mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }

    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        if (enableSwipeBackLayout) return mHelper.getSwipeBackLayout();
        else return null;
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        if (enableSwipeBackLayout) getSwipeBackLayout().setEnableGesture(enable);
    }


    @Override
    public void scrollToFinishActivity() {
        if (enableSwipeBackLayout) {
            Utils.convertActivityToTranslucent(this);
            getSwipeBackLayout().scrollToFinishActivity();
        }
    }

    /**
     * Disables the Swipe-Back-Layout. Should be called before calling super.onCreate()
     */
    protected void disableSwipeBackLayout() {
        enableSwipeBackLayout = false;
    }

    protected void overrideSwipeFromAnywhere() {
        overrideSwipeFromAnywhere = true;
    }

    protected void overrideRedditSwipeAnywhere() {
        overrideRedditSwipeAnywhere = true;
    }


}
