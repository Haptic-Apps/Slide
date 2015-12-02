package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.Utils;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

public class BaseActivity extends AppCompatActivity implements SwipeBackActivityBase {
    private SwipeBackActivityHelper mHelper;
    protected Toolbar mToolbar;

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

        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
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
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
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
     * @param subreddit The subreddit to base the theme on
     */
    protected void applyColorTheme(String subreddit) {
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit), true);
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     * @param toolbar The toolbar's id
     * @param title String resource for the toolbar's title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, @StringRes int title, boolean enableUpButton) {
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getDefaultColor());
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(getString(title));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getStatusBarColor());
        }
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar.
     * @param toolbar The toolbar's id
     * @param title String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     */
    protected void setupAppBar(@IdRes int toolbar, String title, boolean enableUpButton) {
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getDefaultColor());
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getStatusBarColor());
        }
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the username specified.
     * @param toolbar The toolbar's id
     * @param title String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param username The username to base the theme on
     */
    protected void setupUserAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                                   String username) {
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColorUser(username));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getUserStatusBarColor(username));
        }
    }

    /**
     * Sets up the activity's support toolbar and colorizes the status bar. Applies color theming
     * based on the theme for the subreddit specified.
     * @param toolbar The toolbar's id
     * @param title String to be set as the toolbar title
     * @param enableUpButton Whether or not the toolbar should have up navigation
     * @param subreddit The subreddit to base the theme on
     */
    protected void setupSubredditAppBar(@IdRes int toolbar, String title, boolean enableUpButton,
                               String subreddit) {
        mToolbar = (Toolbar) findViewById(toolbar);
        mToolbar.setBackgroundColor(Palette.getColor(subreddit));
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enableUpButton);
            getSupportActionBar().setTitle(title);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getSubredditStatusBarColor(subreddit));
        }
    }

    /**
     * Sets the status bar color for the activity based on a specific subreddit.
     * @param subreddit The subreddit to base the color on.
     */
    protected void themeStatusBar(String subreddit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getSubredditStatusBarColor(subreddit));
        }
    }
}
