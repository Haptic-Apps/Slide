package me.ccrama.redditslide.Activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.MultiredditView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditOverview extends BaseActivityAnim {

    public static final String EXTRA_PROFILE = "profile";

    public static MultiReddit          searchMulti;
    public        OverviewPagerAdapter adapter;
    private       ViewPager            pager;
    private       String               profile;
    private       TabLayout            tabs;
    private       List<MultiReddit>    usedArray;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_multireddits, menu);

        if (!profile.isEmpty()) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.create).setVisible(false);
        }

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /* removed for now
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return ((MultiredditView) adapter.getCurrentFragment()).onKeyDown(keyCode);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return ((MultiredditView) adapter.getCurrentFragment()).onKeyDown(keyCode);
            default:
                return super.dispatchKeyEvent(event);
        }*/
        return super.dispatchKeyEvent(event);
    }

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager
                && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position =
                    ((LinearLayoutManager) ((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstVisibleItemPosition() - 1;
        } else if (((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems =
                    ((CatchStaggeredGridLayoutManager) ((MultiredditView) adapter.getCurrentFragment()).rv
                            .getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position =
                    ((PreCachingLayoutManager) ((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstVisibleItemPosition() - 1;
        }
        return position;
    }

    String term;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                try {
                    onBackPressed();
                } catch (Exception ignored) {

                }
                return true;
            case R.id.action_edit: {
                if (profile.isEmpty()
                        && (UserSubscriptions.multireddits != null)
                        && !UserSubscriptions.multireddits.isEmpty()) {
                    Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                    i.putExtra(CreateMulti.EXTRA_MULTI, UserSubscriptions.getMultireddits()
                            .get(pager.getCurrentItem())
                            .getDisplayName());
                    startActivity(i);
                }
            }
            return true;
            case R.id.search: {
                List<MultiReddit> multireddits = getMultireddits();
                if ((multireddits != null) && !multireddits.isEmpty()) {
                    searchMulti = multireddits.get(pager.getCurrentItem());
                    MaterialDialog.Builder builder =
                            new MaterialDialog.Builder(this).title(R.string.search_title)
                                    .alwaysCallInputCallback()
                                    .input(getString(R.string.search_msg), "",
                                            new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(MaterialDialog materialDialog,
                                                        CharSequence charSequence) {
                                                    term = charSequence.toString();
                                                }
                                            });

                    //Add "search current sub" if it is not frontpage/all/random
                    builder.positiveText(getString(R.string.search_subreddit,
                            "/m/" + searchMulti.getDisplayName()))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MultiredditOverview.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_MULTIREDDIT,
                                            searchMulti.getDisplayName());
                                    startActivity(i);
                                }
                            });

                    builder.show();
                }
            }
            return true;
            case R.id.create:
                if (profile.isEmpty()) {
                    Intent i2 = new Intent(MultiredditOverview.this, CreateMulti.class);
                    startActivity(i2);
                }
                return true;
            case R.id.action_sort:
                openPopup();
                return true;

            case R.id.subs:
                ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.RIGHT);
                return true;
            case R.id.gallery:
                if (SettingValues.tabletUI) {
                    List<Submission> posts =
                            ((MultiredditView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Gallery.class);
                        i2.putExtra(Gallery.EXTRA_PROFILE, profile);
                        i2.putExtra(Gallery.EXTRA_MULTIREDDIT,
                                ((MultiredditView) adapter.getCurrentFragment()).posts.multiReddit.getDisplayName());
                        startActivity(i2);
                    }
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this).setTitle(
                            R.string.general_gallerymode_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton(getString(R.string.pro_previews, SettingValues.previews),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SettingValues.prefs.edit()
                                                .putInt(SettingValues.PREVIEWS_LEFT,
                                                        SettingValues.previews - 1)
                                                .apply();
                                        SettingValues.previews = SettingValues.prefs.getInt(
                                                SettingValues.PREVIEWS_LEFT, 10);
                                        List<Submission> posts =
                                                ((MultiredditView) adapter.getCurrentFragment()).posts.posts;
                                        if (posts != null && !posts.isEmpty()) {
                                            Intent i2 = new Intent(MultiredditOverview.this,
                                                    Gallery.class);
                                            i2.putExtra(Gallery.EXTRA_PROFILE, profile);
                                            i2.putExtra(Gallery.EXTRA_MULTIREDDIT,
                                                    ((MultiredditView) adapter.getCurrentFragment()).posts.multiReddit
                                                            .getDisplayName());
                                            startActivity(i2);
                                        }
                                    }
                                });
                    }
                    b.show();
                }
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    List<Submission> posts =
                            ((MultiredditView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i = new Intent(this, Shadowbox.class);
                        i.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i.putExtra(Shadowbox.EXTRA_PROFILE, profile);
                        i.putExtra(Shadowbox.EXTRA_MULTIREDDIT,
                                ((MultiredditView) adapter.getCurrentFragment()).posts.multiReddit.getDisplayName());
                        startActivity(i);
                    }
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this).setTitle(
                            R.string.general_shadowbox_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse(
                                                                        "market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                                    } catch (ActivityNotFoundException e) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                                Uri.parse(
                                                                        "http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                                    }
                                                }
                                            })
                                    .setNegativeButton(R.string.btn_no_danks,
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                                    dialog.dismiss();
                                                }
                                            });
                    if (SettingValues.previews > 0
                            && adapter != null
                            && ((MultiredditView) adapter.getCurrentFragment()).posts != null
                            && ((MultiredditView) adapter.getCurrentFragment()).posts.posts != null
                            && !((MultiredditView) adapter.getCurrentFragment()).posts.posts.isEmpty()) {
                        b.setNeutralButton(getString(R.string.pro_previews, SettingValues.previews),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SettingValues.prefs.edit()
                                                .putInt(SettingValues.PREVIEWS_LEFT,
                                                        SettingValues.previews - 1)
                                                .apply();
                                        SettingValues.previews = SettingValues.prefs.getInt(
                                                SettingValues.PREVIEWS_LEFT, 10);
                                        List<Submission> posts =
                                                ((MultiredditView) adapter.getCurrentFragment()).posts.posts;
                                        if (posts != null && !posts.isEmpty()) {
                                            Intent i = new Intent(MultiredditOverview.this,
                                                    Shadowbox.class);
                                            i.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                                            i.putExtra(Shadowbox.EXTRA_PROFILE, profile);
                                            i.putExtra(Shadowbox.EXTRA_MULTIREDDIT,
                                                    ((MultiredditView) adapter.getCurrentFragment()).posts.multiReddit
                                                            .getDisplayName());
                                            startActivity(i);
                                        }
                                    }
                                });
                    }
                    b.show();
                }
                return true;
            default:
                return false;
        }
    }

    private void buildDialog() {
        buildDialog(false);
    }

    private void buildDialog(boolean wasException) {
        try {
            AlertDialogWrapper.Builder b =
                    new AlertDialogWrapper.Builder(MultiredditOverview.this).setCancelable(false)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            });
            if (wasException) {
                b.setTitle(R.string.err_title)
                        .setMessage(R.string.err_loading_content)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
            } else if (profile.isEmpty()) {
                b.setTitle(R.string.multireddit_err_title)
                        .setMessage(R.string.multireddit_err_msg)
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                                startActivity(i);
                            }
                        })
                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
            } else {
                b.setTitle(R.string.public_multireddit_err_title)
                        .setMessage(R.string.public_multireddit_err_msg)
                        .setNegativeButton(R.string.btn_go_back,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
            }
            b.show();
        } catch (Exception e) {

        }
    }

    private List<MultiReddit> getMultireddits() {
        if (profile.isEmpty()) {
            return UserSubscriptions.getMultireddits();
        } else {
            return UserSubscriptions.getPublicMultireddits(profile);
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme("");
        setContentView(R.layout.activity_multireddits);
        setupAppBar(R.id.toolbar, R.string.title_multireddits, true, false);

        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        pager = (ViewPager) findViewById(R.id.content_view);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

        profile = "";
        if (getIntent().getExtras() != null) {
            profile = getIntent().getExtras().getString(EXTRA_PROFILE, "");
        }
        if (profile.equalsIgnoreCase(Authentication.name)) {
            profile = "";
        }

        new AsyncTask<Void, Void, List<MultiReddit>>() {
            @Override
            protected List<MultiReddit> doInBackground(Void... params) {
                return getMultireddits();
            }

            @Override
            protected void onPostExecute(List<MultiReddit> multiReddits) {
                if (multiReddits != null && !multiReddits.isEmpty()) {
                    setDataSet(multiReddits);
                } else {
                    buildDialog();
                }
            }
        }.execute();

    }


    public void openPopup() {
        PopupMenu popup =
                new PopupMenu(MultiredditOverview.this, findViewById(R.id.anchor), Gravity.RIGHT);
        String id =
                ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).posts.multiReddit
                        .getDisplayName()
                        .toLowerCase();
        final String[] base = Reddit.getSortingStrings(getBaseContext(), "multi" + id, true);
        for (String s : base) {
            MenuItem m = popup.getMenu().add(s);
            if (s.startsWith("» ")) {
                SpannableString spanString = new SpannableString(s.replace("» ", ""));
                spanString.setSpan(new ForegroundColorSpan(
                                new ColorPreferences(MultiredditOverview.this).getColor(id)), 0,
                        spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                m.setTitle(spanString);
            }
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int i = 0;
                for (String s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                LogUtil.v("Chosen is " + i);
                if (pager.getAdapter() != null) {
                    switch (i) {
                        case 0:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.HOT);
                            reloadSubs();
                            break;
                        case 1:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.NEW);
                            reloadSubs();
                            break;
                        case 2:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.RISING);
                            reloadSubs();
                            break;
                        case 3:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.HOUR);
                            reloadSubs();
                            break;
                        case 4:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.DAY);
                            reloadSubs();
                            break;
                        case 5:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.WEEK);
                            reloadSubs();
                            break;
                        case 6:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.MONTH);
                            reloadSubs();
                            break;
                        case 7:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.YEAR);
                            reloadSubs();
                            break;
                        case 8:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.TOP);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.ALL);
                            reloadSubs();
                            break;
                        case 9:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.HOUR);
                            reloadSubs();
                            break;
                        case 10:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.DAY);
                            reloadSubs();
                            break;
                        case 11:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.WEEK);
                            reloadSubs();
                            break;
                        case 12:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.MONTH);
                            reloadSubs();
                            break;
                        case 13:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.YEAR);
                            reloadSubs();
                            break;
                        case 14:
                            Reddit.setSorting("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), Sorting.CONTROVERSIAL);
                            Reddit.setTime("multi"
                                    + ((MultiredditView) (((OverviewPagerAdapter) pager.getAdapter())
                                    .getCurrentFragment())).posts.multiReddit.getDisplayName()
                                    .toLowerCase(), TimePeriod.ALL);
                            reloadSubs();
                            break;
                    }
                }
                return true;
            }
        });
        popup.show();


    }

    private void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    private void setDataSet(List<MultiReddit> data) {
        try {
            usedArray = data;
            if (usedArray.isEmpty()) {
                buildDialog();
            } else {

                if (adapter == null) {
                    adapter = new OverviewPagerAdapter(getSupportFragmentManager());
                } else {
                    adapter.notifyDataSetChanged();
                }
                pager.setAdapter(adapter);
                pager.setOffscreenPageLimit(1);
                tabs.setupWithViewPager(pager);
                tabs.setSelectedTabIndicatorColor(
                        new ColorPreferences(MultiredditOverview.this).getColor(
                                usedArray.get(0).getDisplayName()));
                doDrawerSubs(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = this.getWindow();
                    window.setStatusBarColor(
                            Palette.getDarkerColor(usedArray.get(0).getDisplayName()));
                }
                final View header = findViewById(R.id.header);
                tabs.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager) {
                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                        int[] firstVisibleItems;
                        int pastVisiblesItems = 0;
                        firstVisibleItems =
                                ((CatchStaggeredGridLayoutManager) (((MultiredditView) adapter.getCurrentFragment()).rv
                                        .getLayoutManager())).findFirstVisibleItemPositions(null);
                        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                            for (int firstVisibleItem : firstVisibleItems) {
                                pastVisiblesItems = firstVisibleItem;
                            }
                        }
                        if (pastVisiblesItems > 8) {
                            ((MultiredditView) adapter.getCurrentFragment()).rv.scrollToPosition(0);
                            if (header != null) {
                                header.animate()
                                        .translationY(header.getHeight())
                                        .setInterpolator(new LinearInterpolator())
                                        .setDuration(0);
                            }
                        } else {
                            ((MultiredditView) adapter.getCurrentFragment()).rv.smoothScrollToPosition(
                                    0);
                        }
                    }
                });
                findViewById(R.id.header).setBackgroundColor(
                        Palette.getColor(usedArray.get(0).getDisplayName()));
            }
        } catch (NullPointerException e) {
            buildDialog(true);
            Log.e(LogUtil.getTag(), "Cannot load multis:\n" + e);
        }

    }

    public void doDrawerSubs(int position) {
        MultiReddit current = usedArray.get(position);
        LinearLayout l = (LinearLayout) findViewById(R.id.sidebar_scroll);
        l.removeAllViews();

        ArrayList<String> toSort = new ArrayList<>();

        for (MultiSubreddit s : current.getSubreddits()) {
            toSort.add(s.getDisplayName().toLowerCase());
        }

        for (String sub : UserSubscriptions.sortNoExtras(toSort)) {
            final View convertView = getLayoutInflater().inflate(R.layout.subforsublist, l, false);

            final String subreddit = sub;
            final TextView t = ((TextView) convertView.findViewById(R.id.name));
            t.setText(subreddit);

            convertView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            convertView.findViewById(R.id.color)
                    .getBackground()
                    .setColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MultiredditOverview.this, SubredditView.class);
                    inte.putExtra(SubredditView.EXTRA_SUBREDDIT, subreddit);
                    MultiredditOverview.this.startActivityForResult(inte, 4);
                }
            });
            l.addView(convertView);
        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    findViewById(R.id.header).animate()
                            .translationY(0)
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);
                    findViewById(R.id.header).setBackgroundColor(
                            Palette.getColor(usedArray.get(position).getDisplayName()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(
                                Palette.getDarkerColor(usedArray.get(position).getDisplayName()));
                    }
                    tabs.setSelectedTabIndicatorColor(
                            new ColorPreferences(MultiredditOverview.this).getColor(
                                    usedArray.get(position).getDisplayName()));
                    doDrawerSubs(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = new MultiredditView();
            Bundle args = new Bundle();

            args.putInt("id", i);
            args.putString(EXTRA_PROFILE, profile);

            f.setArguments(args);

            return f;
        }

        private Fragment mCurrentFragment;

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.size();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return usedArray.get(position).getFullName();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 940 && adapter != null && adapter.getCurrentFragment() != null) {
            if (resultCode == RESULT_OK) {
                LogUtil.v("Doing hide posts");
                ArrayList<Integer> posts = data.getIntegerArrayListExtra("seen");
                ((MultiredditView) adapter.getCurrentFragment()).adapter.refreshView(posts);
                if (data.hasExtra("lastPage")
                        && data.getIntExtra("lastPage", 0) != 0
                        && ((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) ((MultiredditView) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .scrollToPositionWithOffset(data.getIntExtra("lastPage", 0) + 1,
                                    mToolbar.getHeight());
                }
            } else {
                ((MultiredditView) adapter.getCurrentFragment()).adapter.refreshView();
            }
        }

    }

}
