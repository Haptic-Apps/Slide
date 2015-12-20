package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class BaseActivity extends AppCompatActivity implements SwipeBackActivityBase {
    protected Toolbar mToolbar;
    private SwipeBackActivityHelper mHelper;
    private boolean enableSwipeBackLayout = true;

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
        if (enableSwipeBackLayout) {
            mHelper = new SwipeBackActivityHelper(this);
            mHelper.onActivityCreate();

            if (Reddit.single) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                getSwipeBackLayout().setEdgeSize(metrics.widthPixels - 10);
                Log.v("Slide", "EDGE SIZE IS " + metrics.widthPixels);
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

    /**
     * Applies the activity's base color theme. Should be called before inflating any layouts.
     */
    protected void applyColorTheme() {
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
    }

    /**
     * Applies the activity's base color theme based on the theme of a specific subreddit. Should
     * be called before inflating any layouts.
     *
     * @param subreddit The subreddit to base the theme on
     */
    protected void applyColorTheme(String subreddit) {
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit), true);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     *
     * @param toolbar        The toolbar's id
     * @param title          String resource for the toolbar's title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, @StringRes int title, boolean enableUpButton) {
        setupAppBar(toolbar, getString(title), enableUpButton);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, String title, boolean enableUpButton) {
        int statusBarColor = Palette.getStatusBarColor();
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getDefaultColor());
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(statusBarColor);
        }
        setRecentBar(title, statusBarColor);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the username specified.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param username       The username to base the theme on
     */
    protected void setupUserAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                                   String username) {
        int statusBarColor = Palette.getUserStatusBarColor(username);
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColorUser(username));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            if (title != null) {
                getSupportActionBar().setTitle(title);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(statusBarColor);
        }
        setRecentBar(title, statusBarColor);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the subreddit specified.
     *
     * @param toolbar        The toolbar's id
     * @param title          String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param subreddit      The subreddit to base the theme on
     */
    protected void setupSubredditAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                                        String subreddit) {
        int statusBarColor = Palette.getSubredditStatusBarColor(subreddit);
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColor(subreddit));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(statusBarColor);
        }
        setRecentBar(title, statusBarColor);
    }

    /**
     * Sets the status bar color for the activity based on a specific subreddit.
     *
     * @param subreddit The subreddit to base the color on.
     */
    protected void themeStatusBar(String subreddit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getSubredditStatusBarColor(subreddit));
        }
    }

    /**
     * Sets the title and color of the recent bar based on the subreddit
     *
     * @param subreddit Name of the subreddit
     */
    protected void setRecentBar(String subreddit) {
        setRecentBar(subreddit, Palette.getColor(subreddit));
    }

    /**
     * Sets the title in the recent overview with the given title and the default color
     *
     * @param title Title as string for the recent app bar
     * @param color Color for the recent app bar
     */
    protected void setRecentBar(String title, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BitmapDrawable drawable = ((BitmapDrawable) ContextCompat.getDrawable(this,
                    title.equals("androidcirclejerk") ? R.drawable.matiasduarte : R.drawable.ic_launcher));

            setTaskDescription(new ActivityManager.TaskDescription(title, drawable.getBitmap(), color));
        }
    }
}
