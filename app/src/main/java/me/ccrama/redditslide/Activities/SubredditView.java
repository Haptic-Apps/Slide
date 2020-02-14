package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.text.Spannable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.ImageFlairs;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SortingUtil;
import me.ccrama.redditslide.util.SubmissionParser;

public class SubredditView extends BaseActivity {

    public static final String  EXTRA_SUBREDDIT = "subreddit";
    public              boolean canSubmit       = true;
    public String               subreddit;
    public Submission           openingComments;
    public int                  currentComment;
    public OverviewPagerAdapter adapter;
    public String               term;
    public ToggleSwipeViewPager pager;
    public boolean              singleMode;
    public boolean              commentPager;
    public boolean              loaded;
    View      header;
    Subreddit sub;
    private DrawerLayout drawerLayout;
    private boolean currentlySubbed = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        } else if (requestCode == 1) {
            restartTheme();
        } else if (requestCode == 940) {
            if (adapter != null && adapter.getCurrentFragment() != null) {
                if (resultCode == RESULT_OK) {
                    LogUtil.v("Doing hide posts");
                    ArrayList<Integer> posts = data.getIntegerArrayListExtra("seen");
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView(posts);
                    if (data.hasExtra("lastPage")
                            && data.getIntExtra("lastPage", 0) != 0
                            && ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager) {
                        ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager())
                                .scrollToPositionWithOffset(data.getIntExtra("lastPage", 0) + 1,
                                        mToolbar.getHeight());
                    }
                } else {
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)
                || drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (commentPager && pager.getCurrentItem() == 2) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        if (SettingValues.commentPager && SettingValues.single) {
            disableSwipeBackLayout();
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);
        if (!restarting) {
            overridePendingTransition(R.anim.slideright, 0);
        } else {
            restarting = false;
        }


        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        header = findViewById(R.id.header);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setResult(3);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);
        singleMode = SettingValues.single;
        commentPager = false;
        if (singleMode) commentPager = SettingValues.commentPager;
        if (commentPager) {
            adapter = new OverviewPagerAdapterComment(getSupportFragmentManager());
            pager.setSwipeLeftOnly(false);
            pager.setSwipingEnabled(true);
        } else {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        }
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] firstVisibleItems;
                int pastVisiblesItems = 0;
                firstVisibleItems =
                        ((CatchStaggeredGridLayoutManager) ((SubmissionsView) (adapter.getCurrentFragment())).rv
                                .getLayoutManager()).findFirstVisibleItemPositions(null);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    for (int firstVisibleItem : firstVisibleItems) {
                        pastVisiblesItems = firstVisibleItem;
                    }
                }
                if (pastVisiblesItems > 8) {
                    ((SubmissionsView) (adapter.getCurrentFragment())).rv.scrollToPosition(0);
                    header.animate()
                            .translationY(header.getHeight())
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);
                } else {
                    ((SubmissionsView) (adapter.getCurrentFragment())).rv.smoothScrollToPosition(0);
                }
                ((SubmissionsView) (adapter.getCurrentFragment())).resetScroll();
            }
        });
        if (!subreddit.equals("random")
                && !subreddit.equals("all")
                && !subreddit.equals("frontpage")
                && !subreddit.equals("friends")
                && !subreddit.equals("mod")
                && !subreddit.equals("myrandom")
                && !subreddit.equals("randnsfw")
                && !subreddit.equals("popular")
                && !subreddit.contains("+")) {
            executeAsyncSubreddit(subreddit);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (SettingValues.expandedToolbar) {
            inflater.inflate(R.menu.menu_single_subreddit_expanded, menu);
        } else {
            inflater.inflate(R.menu.menu_single_subreddit, menu);
        }

        if (SettingValues.fab && SettingValues.fabType == Constants.FAB_DISMISS) {
            menu.findItem(R.id.hide_posts).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //Hide the "Submit" menu item if the currently viewed sub is the frontpage or /r/all.
        if (subreddit.equals("frontpage") || subreddit.equals("all") || subreddit.equals("popular") || subreddit.equals("friends") || subreddit.equals("mod")) {
            menu.findItem(R.id.submit).setVisible(false);
            menu.findItem(R.id.sidebar).setVisible(false);
        }

        mToolbar.getMenu()
                .findItem(R.id.theme)
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int style = new ColorPreferences(SubredditView.this).getThemeSubreddit(
                                subreddit);
                        final Context contextThemeWrapper =
                                new ContextThemeWrapper(SubredditView.this, style);
                        LayoutInflater localInflater =
                                getLayoutInflater().cloneInContext(contextThemeWrapper);
                        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(subreddit);
                        SettingsSubAdapter.showSubThemeEditor(arrayList, SubredditView.this,
                                dialoglayout);
                        return false;
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.filter:
                filterContent(subreddit);
                return true;
            case R.id.submit:
                Intent i = new Intent(this, Submit.class);
                if (canSubmit) i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                startActivity(i);
                return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null) {
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                }
                return true;
            case R.id.action_sort:
                if (subreddit.equalsIgnoreCase("friends")) {
                    Snackbar s = Snackbar.make(findViewById(R.id.anchor),
                            getString(R.string.friends_sort_error), Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    openPopup();
                }
                return true;
            case R.id.gallery:
                if (SettingValues.isPro) {
                    List<Submission> posts =
                            ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Gallery.class);
                        i2.putExtra("offline",
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                        != null
                                        ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                        : 0L);
                        i2.putExtra(Gallery.EXTRA_SUBREDDIT,
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this).setTitle(
                            R.string.general_gallerymode_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=" + getString(
                                                                        R.string.ui_unlock_package))));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id="
                                                                        + getString(
                                                                        R.string.ui_unlock_package))));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
                return true;
            case R.id.search:
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
                                        })
                                .neutralText(R.string.search_all)
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog,
                                            @NonNull DialogAction dialogAction) {
                                        Intent i = new Intent(SubredditView.this, Search.class);
                                        i.putExtra(Search.EXTRA_TERM, term);
                                        startActivity(i);
                                    }
                                });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage")
                        && !subreddit.equalsIgnoreCase("all")
                        && !subreddit.equalsIgnoreCase("random")
                        && !subreddit.equalsIgnoreCase("popular")
                        && !subreddit.equals("myrandom")
                        && !subreddit.equals("randnsfw")
                        && !subreddit.equalsIgnoreCase("friends")
                        && !subreddit.equalsIgnoreCase("mod")) {
                    builder.positiveText(getString(R.string.search_subreddit, subreddit))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(SubredditView.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(),
                                            "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();
                return true;
            case R.id.sidebar:
                drawerLayout.openDrawer(Gravity.RIGHT);
                return true;
            case R.id.hide_posts:
                ((SubmissionsView) adapter.getCurrentFragment()).clearSeenPosts(false);
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.isPro) {
                    List<Submission> posts =
                            ((SubmissionsView) ((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this).setTitle(R.string.general_shadowbox_ispro)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,

                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=" + getString(
                                                                        R.string.ui_unlock_package))));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id="
                                                                        + getString(
                                                                        R.string.ui_unlock_package))));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sub != null) {
            if (sub.isNsfw() && (!SettingValues.storeHistory || !SettingValues.storeNSFWHistory)) {
                SharedPreferences.Editor e = Reddit.cachedData.edit();
                for (String s : OfflineSubreddit.getAll(sub.getDisplayName())) {
                    e.remove(s);
                }
                e.apply();
            } else if (!SettingValues.storeHistory) {
                SharedPreferences.Editor e = Reddit.cachedData.edit();
                for (String s : OfflineSubreddit.getAll(sub.getDisplayName())) {
                    e.remove(s);
                }
                e.apply();
            }
        }
    }

    public int adjustAlpha(float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    public void doPageSelectedComments(int position) {
        header.animate().translationY(0).setInterpolator(new LinearInterpolator()).setDuration(180);
        pager.setSwipeLeftOnly(false);
        Reddit.currentPosition = position;
        if (position == 1 && adapter != null && adapter.getCurrentFragment() != null) {
            ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
        }
    }

    public void doSubSidebar(final String subOverride) {
        findViewById(R.id.loader).setVisibility(View.VISIBLE);

        invalidateOptionsMenu();

        if (!subOverride.equalsIgnoreCase("all")
                && !subOverride.equalsIgnoreCase("frontpage")
                && !subOverride.equalsIgnoreCase("random")
                && !subOverride.equalsIgnoreCase("popular")
                && !subOverride.equalsIgnoreCase("myrandom")
                && !subOverride.equalsIgnoreCase("randnsfw")
                &&
                !subOverride.equalsIgnoreCase("friends")
                && !subOverride.equalsIgnoreCase("mod")
                &&
                !subOverride.contains("+")
                && !subOverride.contains(".")
                && !subOverride.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }
            loaded = true;

            final View dialoglayout = findViewById(R.id.sidebarsub);
            {
                View submit = (dialoglayout.findViewById(R.id.submit));

                if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                    submit.setVisibility(View.GONE);
                }
                if (SettingValues.fab && SettingValues.fabType == Constants.FAB_POST) {
                    submit.setVisibility(View.GONE);
                }

                submit.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        Intent inte = new Intent(SubredditView.this, Submit.class);
                        if (!subOverride.contains("/m/") && canSubmit) {
                            inte.putExtra(Submit.EXTRA_SUBREDDIT, subOverride);
                        }
                        SubredditView.this.startActivity(inte);
                    }
                });
            }

            dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(SubredditView.this, Wiki.class);
                    i.putExtra(Wiki.EXTRA_SUBREDDIT, subOverride);
                    startActivity(i);
                }
            });
            dialoglayout.findViewById(R.id.syncflair)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageFlairs.syncFlairs(SubredditView.this, subreddit);
                        }
                    });
            dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(SubredditView.this, Submit.class);
                    if ((!subOverride.contains("/m/") || !subOverride.contains(".")) && canSubmit) {
                        i.putExtra(Submit.EXTRA_SUBREDDIT, subOverride);
                    }
                    startActivity(i);
                }
            });

            final TextView sort = dialoglayout.findViewById(R.id.sort);
            Sorting sortingis = Sorting.HOT;
            if(SettingValues.hasSort(subreddit)) {
                sortingis = SettingValues.getBaseSubmissionSort(subreddit);
                sort.setText(sortingis.name()
                        + ((sortingis == Sorting.CONTROVERSIAL || sortingis == Sorting.TOP)?" of "
                        + SettingValues.getBaseTimePeriod(subreddit).name():""));
            } else {
                sort.setText("Set default sorting");

            }
            final int sortid = SortingUtil.getSortingId(sortingis);
            dialoglayout.findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final DialogInterface.OnClickListener l2 =
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            sorts = Sorting.HOT;
                                            break;
                                        case 1:
                                            sorts = Sorting.NEW;
                                            break;
                                        case 2:
                                            sorts = Sorting.RISING;
                                            break;
                                        case 3:
                                            sorts = Sorting.TOP;
                                            askTimePeriod(sorts, subreddit, dialoglayout);
                                            return;
                                        case 4:
                                            sorts = Sorting.CONTROVERSIAL;
                                            askTimePeriod(sorts, subreddit, dialoglayout);
                                            return;
                                    }

                                    SettingValues.setSubSorting(sorts,time,subreddit);
                                    Sorting sortingis = SettingValues.getBaseSubmissionSort(subreddit);
                                    sort.setText(sortingis.name()
                                            + ((sortingis == Sorting.CONTROVERSIAL || sortingis == Sorting.TOP)?" of "
                                            + SettingValues.getBaseTimePeriod(subreddit).name():""));
                                    reloadSubs();

                                }
                            };
                    AlertDialogWrapper.Builder builder =
                            new AlertDialogWrapper.Builder(SubredditView.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(SortingUtil.getSortingStrings(),
                            sortid, l2);
                    builder.setNegativeButton("Reset default sorting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SettingValues.prefs.edit().remove("defaultSort" + subreddit.toLowerCase(Locale.ENGLISH)).apply();
                            SettingValues.prefs.edit().remove("defaultTime" + subreddit.toLowerCase(Locale.ENGLISH)).apply();
                            final TextView sort = dialoglayout.findViewById(R.id.sort);
                            if(SettingValues.hasSort(subreddit)) {
                                Sorting sortingis = SettingValues.getBaseSubmissionSort(subreddit);
                                sort.setText(sortingis.name()
                                        + ((sortingis == Sorting.CONTROVERSIAL || sortingis == Sorting.TOP)?" of "
                                        + SettingValues.getBaseTimePeriod(subreddit).name():""));
                            } else {
                                sort.setText("Set default sorting");

                            }
                            reloadSubs();
                        }
                    });
                    builder.show();
                }
            });

            dialoglayout.findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int style =
                            new ColorPreferences(SubredditView.this).getThemeSubreddit(subOverride);

                    final Context contextThemeWrapper =
                            new ContextThemeWrapper(SubredditView.this, style);
                    LayoutInflater localInflater =
                            getLayoutInflater().cloneInContext(contextThemeWrapper);

                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);

                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(subOverride);
                    SettingsSubAdapter.showSubThemeEditor(arrayList, SubredditView.this,
                            dialoglayout);
                }
            });
            dialoglayout.findViewById(R.id.mods).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog d = new MaterialDialog.Builder(SubredditView.this).title(
                            R.string.sidebar_findingmods)
                            .cancelable(true)
                            .content(R.string.misc_please_wait)
                            .progress(true, 100)
                            .show();
                    new AsyncTask<Void, Void, Void>() {
                        ArrayList<UserRecord> mods;

                        @Override
                        protected Void doInBackground(Void... params) {
                            mods = new ArrayList<>();
                            UserRecordPaginator paginator =
                                    new UserRecordPaginator(Authentication.reddit, subOverride,
                                            "moderators");
                            paginator.setSorting(Sorting.HOT);
                            paginator.setTimePeriod(TimePeriod.ALL);
                            while (paginator.hasNext()) {
                                mods.addAll(paginator.next());
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            final ArrayList<String> names = new ArrayList<>();
                            for (UserRecord rec : mods) {
                                names.add(rec.getFullName());
                            }
                            d.dismiss();
                            new MaterialDialog.Builder(SubredditView.this).title(
                                    getString(R.string.sidebar_submods, subreddit))
                                    .items(names)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog,
                                                View itemView, int which, CharSequence text) {
                                            Intent i =
                                                    new Intent(SubredditView.this, Profile.class);
                                            i.putExtra(Profile.EXTRA_PROFILE, names.get(which));
                                            startActivity(i);
                                        }
                                    })
                                    .positiveText(R.string.btn_message)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                            Intent i = new Intent(SubredditView.this,
                                                    SendMessage.class);
                                            i.putExtra(SendMessage.EXTRA_NAME, "/r/" + subOverride);
                                            startActivity(i);
                                        }
                                    })
                                    .show();
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            dialoglayout.findViewById(R.id.flair).setVisibility(View.GONE);
            if (Authentication.didOnline && Authentication.isLoggedIn) {
                new AsyncTask<View, Void, View>() {
                    List<FlairTemplate> flairs;
                    ArrayList<String> flairText;
                    String current;
                    AccountManager m;

                    @Override
                    protected View doInBackground(View... params) {
                        try {
                            m = new AccountManager(Authentication.reddit);
                            JsonNode node = m.getFlairChoicesRootNode(subOverride, null);
                            flairs = m.getFlairChoices(subOverride, node);

                            FlairTemplate currentF = m.getCurrentFlair(subOverride, node);
                            if (currentF != null) {
                                if (currentF.getText().isEmpty()) {
                                    current = ("[" + currentF.getCssClass() + "]");
                                } else {
                                    current = (currentF.getText());
                                }
                            }
                            flairText = new ArrayList<>();
                            for (FlairTemplate temp : flairs) {
                                if (temp.getText().isEmpty()) {
                                    flairText.add("[" + temp.getCssClass() + "]");
                                } else {
                                    flairText.add(temp.getText());
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(View flair) {
                        if (flairs != null
                                && !flairs.isEmpty()
                                && flairText != null
                                && !flairText.isEmpty()) {
                            flair.setVisibility(View.VISIBLE);
                            if (current != null) {
                                ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText(
                                        getString(R.string.sidebar_flair, current));
                            }
                            flair.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(SubredditView.this).items(flairText)
                                            .title(R.string.sidebar_select_flair)
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog,
                                                        View itemView, int which,
                                                        CharSequence text) {
                                                    final FlairTemplate t = flairs.get(which);
                                                    if (t.isTextEditable()) {
                                                        new MaterialDialog.Builder(
                                                                SubredditView.this).title(
                                                                R.string.sidebar_select_flair_text)
                                                                .input(getString(
                                                                        R.string.mod_flair_hint),
                                                                        t.getText(), true,
                                                                        new MaterialDialog.InputCallback() {
                                                                            @Override
                                                                            public void onInput(
                                                                                    MaterialDialog dialog,
                                                                                    CharSequence input) {

                                                                            }
                                                                        })
                                                                .positiveText(R.string.btn_set)
                                                                .onPositive(
                                                                        new MaterialDialog.SingleButtonCallback() {
                                                                            @Override
                                                                            public void onClick(
                                                                                    MaterialDialog dialog,
                                                                                    DialogAction which) {
                                                                                final String flair =
                                                                                        dialog.getInputEditText()
                                                                                                .getText()
                                                                                                .toString();
                                                                                new AsyncTask<Void, Void, Boolean>() {
                                                                                    @Override
                                                                                    protected Boolean doInBackground(
                                                                                            Void... params) {
                                                                                        try {
                                                                                            new ModerationManager(
                                                                                                    Authentication.reddit)
                                                                                                    .setFlair(
                                                                                                            subOverride,
                                                                                                            t,
                                                                                                            flair,
                                                                                                            Authentication.name);
                                                                                            FlairTemplate
                                                                                                    currentF =
                                                                                                    m.getCurrentFlair(
                                                                                                            subOverride);
                                                                                            if (currentF
                                                                                                    .getText()
                                                                                                    .isEmpty()) {
                                                                                                current =
                                                                                                        ("["
                                                                                                                + currentF
                                                                                                                .getCssClass()
                                                                                                                + "]");
                                                                                            } else {
                                                                                                current =
                                                                                                        (currentF
                                                                                                                .getText());
                                                                                            }
                                                                                            return true;
                                                                                        } catch (Exception e) {
                                                                                            e.printStackTrace();
                                                                                            return false;
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    protected void onPostExecute(
                                                                                            Boolean done) {
                                                                                        Snackbar s;
                                                                                        if (done) {
                                                                                            if (current
                                                                                                    != null) {
                                                                                                ((TextView) dialoglayout
                                                                                                        .findViewById(
                                                                                                                R.id.flair_text))
                                                                                                        .setText(
                                                                                                                getString(
                                                                                                                        R.string.sidebar_flair,
                                                                                                                        current));
                                                                                            }
                                                                                            s =
                                                                                                    Snackbar.make(
                                                                                                            mToolbar,
                                                                                                            R.string.snackbar_flair_success,
                                                                                                            Snackbar.LENGTH_SHORT);
                                                                                        } else {
                                                                                            s =
                                                                                                    Snackbar.make(
                                                                                                            mToolbar,
                                                                                                            R.string.snackbar_flair_error,
                                                                                                            Snackbar.LENGTH_SHORT);
                                                                                        }
                                                                                        if (s
                                                                                                != null) {
                                                                                            View
                                                                                                    view =
                                                                                                    s.getView();
                                                                                            TextView
                                                                                                    tv =
                                                                                                    view
                                                                                                            .findViewById(
                                                                                                                    android.support.design.R.id.snackbar_text);
                                                                                            tv.setTextColor(
                                                                                                    Color.WHITE);
                                                                                            s.show();
                                                                                        }
                                                                                    }
                                                                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                                            }
                                                                        })
                                                                .negativeText(R.string.btn_cancel)
                                                                .show();
                                                    } else {
                                                        new AsyncTask<Void, Void, Boolean>() {
                                                            @Override
                                                            protected Boolean doInBackground(
                                                                    Void... params) {
                                                                try {
                                                                    new ModerationManager(
                                                                            Authentication.reddit).setFlair(
                                                                            subOverride, t, null,
                                                                            Authentication.name);
                                                                    FlairTemplate currentF =
                                                                            m.getCurrentFlair(
                                                                                    subOverride);
                                                                    if (currentF.getText()
                                                                            .isEmpty()) {
                                                                        current = ("["
                                                                                + currentF.getCssClass()
                                                                                + "]");
                                                                    } else {
                                                                        current =
                                                                                (currentF.getText());
                                                                    }
                                                                    return true;
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                    return false;
                                                                }
                                                            }

                                                            @Override
                                                            protected void onPostExecute(
                                                                    Boolean done) {
                                                                Snackbar s;
                                                                if (done) {
                                                                    if (current != null) {
                                                                        ((TextView) dialoglayout.findViewById(
                                                                                R.id.flair_text)).setText(
                                                                                getString(
                                                                                        R.string.sidebar_flair,
                                                                                        current));
                                                                    }
                                                                    s = Snackbar.make(mToolbar,
                                                                            R.string.snackbar_flair_success,
                                                                            Snackbar.LENGTH_SHORT);
                                                                } else {
                                                                    s = Snackbar.make(mToolbar,
                                                                            R.string.snackbar_flair_error,
                                                                            Snackbar.LENGTH_SHORT);
                                                                }
                                                                if (s != null) {
                                                                    View view = s.getView();
                                                                    TextView tv = view.findViewById(
                                                                            android.support.design.R.id.snackbar_text);
                                                                    tv.setTextColor(Color.WHITE);
                                                                    s.show();
                                                                }
                                                            }
                                                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    }
                                                }
                                            })
                                            .show();
                                }
                            });
                        }
                    }
                }.execute((View) dialoglayout.findViewById(R.id.flair));
            }
        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        GravityCompat.END);
            }
        }
    }

    public void doSubSidebarNoLoad(final String subOverride) {
        findViewById(R.id.loader).setVisibility(View.GONE);

        invalidateOptionsMenu();

        if (!subOverride.equalsIgnoreCase("all") && !subOverride.equalsIgnoreCase("frontpage") &&
                !subOverride.equalsIgnoreCase("friends") && !subOverride.equalsIgnoreCase("mod") &&
                !subOverride.contains("+") && !subOverride.contains(".") && !subOverride.contains(
                "/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);
            findViewById(R.id.active_users).setVisibility(View.GONE);

            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subOverride));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subOverride);

            //Sidebar buttons should use subOverride's accent color
            int subColor = new ColorPreferences(this).getColor(subOverride);
            ((TextView) findViewById(R.id.theme_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.wiki_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.post_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.mods_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.flair_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.sorting).findViewById(R.id.sort)).setTextColor(subColor);
            ((TextView) findViewById(R.id.sync)).setTextColor(subColor);

        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        GravityCompat.END);
            }
        }
    }

    public void executeAsyncSubreddit(String sub) {
        new AsyncGetSubreddit().execute(sub);
    }

    public void filterContent(final String subreddit) {
        final boolean[] chosen = new boolean[]{
                PostMatch.isImage(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isAlbums(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isGif(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isVideo(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isUrls(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isSelftext(subreddit.toLowerCase(Locale.ENGLISH)),
                PostMatch.isNsfw(subreddit.toLowerCase(Locale.ENGLISH))
        };

        final String FILTER_TITLE =
                (subreddit.equals("frontpage")) ? (getString(R.string.content_to_hide, "frontpage"))
                        : (getString(R.string.content_to_hide, "/r/" + subreddit));

        new AlertDialogWrapper.Builder(this).setTitle(FILTER_TITLE)
                .alwaysCallMultiChoiceCallback()
                .setMultiChoiceItems(new String[]{
                        getString(R.string.image_downloads), getString(R.string.type_albums),
                        getString(R.string.type_gifs), getString(R.string.type_videos),
                        getString(R.string.type_links), getString(R.string.type_selftext),
                        getString(R.string.type_nsfw_content)
                }, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PostMatch.setChosen(chosen, subreddit);
                        reloadSubs();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();


    }

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager
                && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position =
                    ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition() - 1;
        } else if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems =
                    ((CatchStaggeredGridLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv
                            .getLayoutManager()).findFirstCompletelyVisibleItemPositions(
                            firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position =
                    ((PreCachingLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager())
                            .findFirstCompletelyVisibleItemPosition() - 1;
        }
        return position;
    }

    TimePeriod time = TimePeriod.DAY;
    Sorting sorts;

    private void askTimePeriod(final Sorting sort, final String sub, final View dialoglayout) {
        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        time = TimePeriod.HOUR;
                        break;
                    case 1:
                        time = TimePeriod.DAY;
                        break;
                    case 2:
                        time = TimePeriod.WEEK;
                        break;
                    case 3:
                        time = TimePeriod.MONTH;
                        break;
                    case 4:
                        time = TimePeriod.YEAR;
                        break;
                    case 5:
                        time = TimePeriod.ALL;
                        break;
                }
                SettingValues.setSubSorting(sort, time, sub);
                SortingUtil.setSorting(sub, sort);
                SortingUtil.setTime(sub, time);
                final TextView sort = dialoglayout.findViewById(R.id.sort);
                Sorting sortingis = SettingValues.getBaseSubmissionSort("Default sorting: " + subreddit);
                sort.setText(sortingis.name()
                        + ((sortingis == Sorting.CONTROVERSIAL || sortingis == Sorting.TOP)?" of "
                        + SettingValues.getBaseTimePeriod(subreddit).name():""));
                reloadSubs();
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(SortingUtil.getSortingTimesStrings(),
                SortingUtil.getSortingTimeId(""), l2);
        builder.show();
    }
    public void openPopup() {
        PopupMenu popup =
                new PopupMenu(SubredditView.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final Spannable[] base = SortingUtil.getSortingSpannables(subreddit);
        for (Spannable s : base) {
            // Do not add option for "Best" in any subreddit except for the frontpage.
            if (!subreddit.toLowerCase().equals("frontpage") && s.toString().equals(getString(R.string.sorting_best))) {
                continue;
            }
            MenuItem m = popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (Spannable s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        SortingUtil.setSorting(subreddit, Sorting.HOT);
                        reloadSubs();
                        break;
                    case 1:
                        SortingUtil.setSorting(subreddit, Sorting.NEW);
                        reloadSubs();
                        break;
                    case 2:
                        SortingUtil.setSorting(subreddit, Sorting.RISING);
                        reloadSubs();
                        break;
                    case 3:
                        SortingUtil.setSorting(subreddit, Sorting.TOP);
                        openPopupTime();
                        break;
                    case 4:
                        SortingUtil.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        openPopupTime();
                        break;
                    case 5:
                        SortingUtil.setSorting(subreddit, Sorting.BEST);
                        reloadSubs();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    public void openPopupTime() {
        PopupMenu popup =
                new PopupMenu(SubredditView.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final Spannable[] base = SortingUtil.getSortingTimesSpannables(subreddit);
        for (Spannable s : base) {
            MenuItem m = popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (Spannable s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        SortingUtil.setTime(subreddit, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 1:
                        SortingUtil.setTime(subreddit, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 2:
                        SortingUtil.setTime(subreddit, TimePeriod.WEEK);
                        reloadSubs();
                        break;
                    case 3:
                        SortingUtil.setTime(subreddit, TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 4:
                        SortingUtil.setTime(subreddit, TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 5:
                        SortingUtil.setTime(subreddit, TimePeriod.ALL);
                        reloadSubs();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    public static boolean restarting;

    public void restartTheme() {
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        finish();
        restarting = true;
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void changeSubscription(Subreddit subreddit, boolean isChecked) {
        if (isChecked) {
            UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                    SubredditView.this);
        } else {
            UserSubscriptions.removeSubreddit(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                    SubredditView.this);
            pager.setCurrentItem(pager.getCurrentItem() - 1);
            restartTheme();
        }
        Snackbar s = Snackbar.make(mToolbar, isChecked ? getString(R.string.misc_subscribed)
                : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
        View view = s.getView();
        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        s.show();
    }

    private void doSubOnlyStuff(final Subreddit subreddit) {
        if (!isFinishing()) {
            findViewById(R.id.loader).setVisibility(View.GONE);
            if (subreddit.getDataNode().has("subreddit_type") && !subreddit.getDataNode()
                    .get("subreddit_type")
                    .isNull()) {
                canSubmit = !subreddit.getDataNode()
                        .get("subreddit_type")
                        .asText()
                        .toUpperCase()
                        .equals("RESTRICTED");
            }
            if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
                findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

                final String text = subreddit.getDataNode().get("description_html").asText().trim();
                final SpoilerRobotoTextView body =
                        (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
                CommentOverflow overflow = (CommentOverflow) findViewById(R.id.commentOverflow);
                setViews(text, subreddit.getDisplayName(), body, overflow);

                //get all subs that have Notifications enabled
                ArrayList<String> rawSubs = Reddit.stringToArray(
                        Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
                HashMap<String, Integer> subThresholds = new HashMap<>();
                for (String s : rawSubs) {
                    try {
                        String[] split = s.split(":");
                        subThresholds.put(split[0].toLowerCase(Locale.ENGLISH), Integer.valueOf(split[1]));
                    } catch (Exception ignored) {
                        //do nothing
                    }
                }

                //whether or not this subreddit was in the keySet
                boolean isNotified =
                        subThresholds.keySet().contains(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH));
                ((AppCompatCheckBox) findViewById(R.id.notify_posts_state)).setChecked(isNotified);
            } else {
                findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            }
            View collection = findViewById(R.id.collection);
            if (Authentication.isLoggedIn) {
                collection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Void, Void, Void>() {
                            HashMap<String, MultiReddit> multis =
                                    new HashMap<String, MultiReddit>();

                            @Override
                            protected Void doInBackground(Void... params) {
                                if (UserSubscriptions.multireddits == null) {
                                    UserSubscriptions.syncMultiReddits(SubredditView.this);
                                }
                                for (MultiReddit r : UserSubscriptions.multireddits) {
                                    multis.put(r.getDisplayName(), r);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                new MaterialDialog.Builder(SubredditView.this).title(
                                        "Add /r/" + subreddit.getDisplayName() + " to")
                                        .items(multis.keySet())
                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                            @Override
                                            public void onSelection(MaterialDialog dialog,
                                                    View itemView, final int which,
                                                    CharSequence text) {
                                                new AsyncTask<Void, Void, Void>() {
                                                    @Override
                                                    protected Void doInBackground(Void... params) {
                                                        try {
                                                            final String multiName = multis.keySet()
                                                                    .toArray(
                                                                            new String[multis.size()])[which];
                                                            List<String> subs =
                                                                    new ArrayList<String>();
                                                            for (MultiSubreddit sub : multis.get(
                                                                    multiName).getSubreddits()) {
                                                                subs.add(sub.getDisplayName());
                                                            }
                                                            subs.add(subreddit.getDisplayName());
                                                            new MultiRedditManager(
                                                                    Authentication.reddit).createOrUpdate(
                                                                    new MultiRedditUpdateRequest.Builder(
                                                                            Authentication.name,
                                                                            multiName).subreddits(
                                                                            subs).build());

                                                            UserSubscriptions.syncMultiReddits(
                                                                    SubredditView.this);

                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    drawerLayout.closeDrawers();
                                                                    Snackbar s =
                                                                            Snackbar.make(mToolbar,
                                                                                    getString(
                                                                                            R.string.multi_subreddit_added,
                                                                                            multiName),
                                                                                    Snackbar.LENGTH_LONG);
                                                                    View view = s.getView();
                                                                    TextView tv = view.findViewById(
                                                                            android.support.design.R.id.snackbar_text);
                                                                    tv.setTextColor(Color.WHITE);
                                                                    s.show();
                                                                }
                                                            });
                                                        } catch (final NetworkException | ApiException e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            Snackbar.make(mToolbar,
                                                                                    getString(
                                                                                            R.string.multi_error),
                                                                                    Snackbar.LENGTH_LONG)
                                                                                    .setAction(
                                                                                            R.string.btn_ok,
                                                                                            new View.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        View v) {

                                                                                                }
                                                                                            })
                                                                                    .show();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                            e.printStackTrace();
                                                        }
                                                        return null;
                                                    }
                                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                                            }
                                        })
                                        .show();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            } else {
                collection.setVisibility(View.GONE);
            }

            {
                final TextView subscribe = (TextView) findViewById(R.id.subscribe);

                currentlySubbed =
                        (!Authentication.isLoggedIn && UserSubscriptions.getSubscriptions(this)
                                .contains(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH))) || (
                                Authentication.isLoggedIn
                                        && subreddit.isUserSubscriber());
                doSubscribeButtonText(currentlySubbed, subscribe);

                assert subscribe != null;
                subscribe.setOnClickListener(new View.OnClickListener() {
                    private void doSubscribe() {
                        if (Authentication.isLoggedIn) {
                            new AlertDialogWrapper.Builder(SubredditView.this).setTitle(
                                    getString(R.string.subscribe_to, subreddit.getDisplayName()))
                                    .setPositiveButton(R.string.reorder_add_subscribe,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    new AsyncTask<Void, Void, Boolean>() {
                                                        @Override
                                                        public void onPostExecute(Boolean success) {
                                                            if (!success) { // If subreddit was removed from account or not

                                                                new AlertDialogWrapper.Builder(
                                                                        SubredditView.this).setTitle(
                                                                        R.string.force_change_subscription)
                                                                        .setMessage(
                                                                                R.string.force_change_subscription_desc)
                                                                        .setPositiveButton(
                                                                                R.string.btn_yes,
                                                                                new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(
                                                                                            DialogInterface dialog,
                                                                                            int which) {
                                                                                        changeSubscription(
                                                                                                subreddit,
                                                                                                true); // Force add the subscription
                                                                                        Snackbar s =
                                                                                                Snackbar.make(
                                                                                                        mToolbar,
                                                                                                        getString(
                                                                                                                R.string.misc_subscribed),
                                                                                                        Snackbar.LENGTH_SHORT);
                                                                                        View view =
                                                                                                s.getView();
                                                                                        TextView
                                                                                                tv =
                                                                                                view
                                                                                                        .findViewById(
                                                                                                                android.support.design.R.id.snackbar_text);
                                                                                        tv.setTextColor(
                                                                                                Color.WHITE);
                                                                                        s.show();
                                                                                    }
                                                                                })
                                                                        .setNegativeButton(
                                                                                R.string.btn_no,
                                                                                new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(
                                                                                            DialogInterface dialog,
                                                                                            int which) {

                                                                                    }
                                                                                })
                                                                        .setCancelable(false)
                                                                        .show();
                                                            } else {
                                                                changeSubscription(subreddit, true);
                                                            }

                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(
                                                                Void... params) {
                                                            try {
                                                                new AccountManager(
                                                                        Authentication.reddit).subscribe(
                                                                        subreddit);
                                                            } catch (NetworkException e) {
                                                                return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                                                            }
                                                            return true;
                                                        }
                                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                }
                                            })
                                    .setNegativeButton(R.string.btn_cancel, null)
                                    .setNeutralButton(R.string.btn_add_to_sublist,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    changeSubscription(subreddit,
                                                            true); // Force add the subscription
                                                    Snackbar s = Snackbar.make(mToolbar,
                                                            R.string.sub_added,
                                                            Snackbar.LENGTH_SHORT);
                                                    View view = s.getView();
                                                    TextView tv = view.findViewById(
                                                            android.support.design.R.id.snackbar_text);
                                                    tv.setTextColor(Color.WHITE);
                                                    s.show();
                                                }
                                            })
                                    .show();
                        } else {
                            changeSubscription(subreddit, true);
                        }
                    }

                    @Override
                    public void onClick(View v) {
                        if (!currentlySubbed) {
                            doSubscribe();
                            doSubscribeButtonText(currentlySubbed, subscribe);
                        } else {
                            doUnsubscribe();
                            doSubscribeButtonText(currentlySubbed, subscribe);
                        }
                    }

                    private void doUnsubscribe() {
                        if (Authentication.didOnline) {
                            new AlertDialogWrapper.Builder(SubredditView.this).setTitle(
                                    getString(R.string.unsubscribe_from,
                                            subreddit.getDisplayName()))
                                    .setPositiveButton(R.string.reorder_remove_unsubsribe,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    new AsyncTask<Void, Void, Boolean>() {
                                                        @Override
                                                        public void onPostExecute(Boolean success) {
                                                            if (!success) { // If subreddit was removed from account or not

                                                                new AlertDialogWrapper.Builder(
                                                                        SubredditView.this).setTitle(
                                                                        R.string.force_change_subscription)
                                                                        .setMessage(
                                                                                R.string.force_change_subscription_desc)
                                                                        .setPositiveButton(
                                                                                R.string.btn_yes,
                                                                                new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(
                                                                                            DialogInterface dialog,
                                                                                            int which) {
                                                                                        changeSubscription(
                                                                                                subreddit,
                                                                                                false); // Force add the subscription
                                                                                        Snackbar s =
                                                                                                Snackbar.make(
                                                                                                        mToolbar,
                                                                                                        getString(
                                                                                                                R.string.misc_unsubscribed),
                                                                                                        Snackbar.LENGTH_SHORT);
                                                                                        View view =
                                                                                                s.getView();
                                                                                        TextView
                                                                                                tv =
                                                                                                view
                                                                                                        .findViewById(
                                                                                                                android.support.design.R.id.snackbar_text);
                                                                                        tv.setTextColor(
                                                                                                Color.WHITE);
                                                                                        s.show();
                                                                                    }
                                                                                })
                                                                        .setNegativeButton(
                                                                                R.string.btn_no,
                                                                                new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(
                                                                                            DialogInterface dialog,
                                                                                            int which) {

                                                                                    }
                                                                                })
                                                                        .setCancelable(false)
                                                                        .show();
                                                            } else {
                                                                changeSubscription(subreddit,
                                                                        false);
                                                            }

                                                        }

                                                        @Override
                                                        protected Boolean doInBackground(
                                                                Void... params) {
                                                            try {
                                                                new AccountManager(
                                                                        Authentication.reddit).unsubscribe(
                                                                        subreddit);
                                                            } catch (NetworkException e) {
                                                                return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                                                            }
                                                            return true;
                                                        }
                                                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                }
                                            })
                                    .setNeutralButton(R.string.just_unsub,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    changeSubscription(subreddit,
                                                            false); // Force add the subscription
                                                    Snackbar s = Snackbar.make(mToolbar,
                                                            R.string.misc_unsubscribed,
                                                            Snackbar.LENGTH_SHORT);
                                                    View view = s.getView();
                                                    TextView tv = view.findViewById(
                                                            android.support.design.R.id.snackbar_text);
                                                    tv.setTextColor(Color.WHITE);
                                                    s.show();
                                                }
                                            })
                                    .setNegativeButton(R.string.btn_cancel, null)
                                    .show();
                        } else {
                            changeSubscription(subreddit, false);
                        }
                    }


                });
            }
            {
                final AppCompatCheckBox notifyStateCheckBox =
                        (AppCompatCheckBox) findViewById(R.id.notify_posts_state);
                assert notifyStateCheckBox != null;

                notifyStateCheckBox.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                    boolean isChecked) {
                                if (isChecked) {
                                    final String sub = subreddit.getDisplayName();

                                    if (!sub.equalsIgnoreCase("all")
                                            && !sub.equalsIgnoreCase("frontpage")
                                            &&
                                            !sub.equalsIgnoreCase("friends")
                                            && !sub.equalsIgnoreCase("mod")
                                            &&
                                            !sub.contains("+")
                                            && !sub.contains(".")
                                            && !sub.contains("/m/")) {
                                        new AlertDialogWrapper.Builder(SubredditView.this).setTitle(
                                                getString(R.string.sub_post_notifs_title, sub))
                                                .setMessage(R.string.sub_post_notifs_msg)
                                                .setPositiveButton(R.string.btn_ok,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                new MaterialDialog.Builder(
                                                                        SubredditView.this).title(
                                                                        R.string.sub_post_notifs_threshold)
                                                                        .items(new String[]{
                                                                                "1", "5", "10",
                                                                                "20", "40", "50"
                                                                        })
                                                                        .alwaysCallSingleChoiceCallback()
                                                                        .itemsCallbackSingleChoice(
                                                                                0,
                                                                                new MaterialDialog.ListCallbackSingleChoice() {
                                                                                    @Override
                                                                                    public boolean onSelection(
                                                                                            MaterialDialog dialog,
                                                                                            View itemView,
                                                                                            int which,
                                                                                            CharSequence text) {
                                                                                        ArrayList<String>
                                                                                                subs =
                                                                                                Reddit.stringToArray(
                                                                                                        Reddit.appRestart
                                                                                                                .getString(
                                                                                                                        CheckForMail.SUBS_TO_GET,
                                                                                                                        ""));
                                                                                        subs.add(sub
                                                                                                + ":"
                                                                                                + text);
                                                                                        Reddit.appRestart
                                                                                                .edit()
                                                                                                .putString(
                                                                                                        CheckForMail.SUBS_TO_GET,
                                                                                                        Reddit.arrayToString(
                                                                                                                subs))
                                                                                                .commit();
                                                                                        return true;
                                                                                    }
                                                                                })
                                                                        .cancelable(false)
                                                                        .show();
                                                            }
                                                        })
                                                .setNegativeButton(R.string.btn_cancel, null)
                                                .setNegativeButton(R.string.btn_cancel,
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(
                                                                    DialogInterface dialog,
                                                                    int which) {
                                                                notifyStateCheckBox.setChecked(
                                                                        false);
                                                            }
                                                        })
                                                .setOnCancelListener(
                                                        new DialogInterface.OnCancelListener() {
                                                            @Override
                                                            public void onCancel(
                                                                    DialogInterface dialog) {
                                                                notifyStateCheckBox.setChecked(
                                                                        false);
                                                            }
                                                        })
                                                .show();
                                    } else {
                                        notifyStateCheckBox.setChecked(false);
                                        Toast.makeText(SubredditView.this,
                                                R.string.sub_post_notifs_err, Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                } else {
                                    Intent cancelIntent =
                                            new Intent(SubredditView.this, CancelSubNotifs.class);
                                    cancelIntent.putExtra(CancelSubNotifs.EXTRA_SUB,
                                            subreddit.getDisplayName());
                                    startActivity(cancelIntent);
                                }
                            }
                        });
            }
            if (!subreddit.getPublicDescription().isEmpty()) {
                findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
                setViews(subreddit.getDataNode().get("public_description_html").asText(),
                        subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                        ((SpoilerRobotoTextView) findViewById(R.id.sub_title)),
                        (CommentOverflow) findViewById(R.id.sub_title_overflow));
            } else {
                findViewById(R.id.sub_title).setVisibility(View.GONE);
            }
            if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode()
                    .get("icon_img")
                    .asText()
                    .isEmpty()) {
                ((Reddit) getApplication()).getImageLoader()
                        .displayImage(subreddit.getDataNode().get("icon_img").asText(),
                                (ImageView) findViewById(R.id.subimage));
            } else {
                findViewById(R.id.subimage).setVisibility(View.GONE);
            }
            String bannerImage = subreddit.getBannerImage();
            if (bannerImage != null && !bannerImage.isEmpty()) {
                findViewById(R.id.sub_banner).setVisibility(View.VISIBLE);
                ((Reddit) getApplication()).getImageLoader()
                        .displayImage(bannerImage,
                                (ImageView) findViewById(R.id.sub_banner));
            } else {
                findViewById(R.id.sub_banner).setVisibility(View.GONE);
            }
            ((TextView) findViewById(R.id.subscribers)).setText(
                    getString(R.string.subreddit_subscribers_string,
                            subreddit.getLocalizedSubscriberCount()));
            findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

            ((TextView) findViewById(R.id.active_users)).setText(
                    getString(R.string.subreddit_active_users_string_new,
                            subreddit.getLocalizedAccountsActive()));
            findViewById(R.id.active_users).setVisibility(View.VISIBLE);
        }
    }

    private void doSubscribeButtonText(boolean currentlySubbed, TextView subscribe) {
        if (Authentication.didOnline) {
            if (currentlySubbed) {
                subscribe.setText(R.string.unsubscribe_caps);
            } else {
                subscribe.setText(R.string.subscribe_caps);
            }
        } else {
            if (currentlySubbed) {
                subscribe.setText(R.string.btn_remove_from_sublist);
            } else {
                subscribe.setText(R.string.btn_add_to_sublist);
            }
        }
    }

    private void reloadSubs() {
        restartTheme();
    }

    private void setViews(String rawHTML, String subreddit, SpoilerRobotoTextView firstTextView,
            CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subreddit);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subreddit);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subreddit);
            }
            SidebarLayout sidebar = (SidebarLayout) findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private SubmissionsView mCurrentFragment;
        private BlankFragment   blankPage;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {
                    if (position == 0) {
                        CoordinatorLayout.LayoutParams params =
                                (CoordinatorLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0,
                                -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                            overridePendingTransition(0, R.anim.fade_out);
                        }
                    }

                    if (position == 0) {
                        ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(
                                positionOffset);
                        pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));
                    }
                }

                @Override
                public void onPageSelected(final int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (pager.getAdapter() != null) {
                pager.setCurrentItem(1);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 1) {
                SubmissionsView f = new SubmissionsView();
                Bundle args = new Bundle();
                args.putString("id", subreddit);
                f.setArguments(args);

                return f;
            } else {
                blankPage = new BlankFragment();
                return blankPage;
            }


        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            doSetPrimary(object, position);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null
                    && getCurrentFragment() != object
                    && position != 3
                    && object instanceof SubmissionsView) {
                mCurrentFragment = ((SubmissionsView) object);
                if (mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

                }
            }
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

    }

    public class OverviewPagerAdapterComment extends OverviewPagerAdapter {
        public int size = 2;
        public Fragment storedFragment;
        BlankFragment blankPage;
        private SubmissionsView mCurrentFragment;


        public OverviewPagerAdapterComment(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {
                    if (position == 0) {
                        CoordinatorLayout.LayoutParams params =
                                (CoordinatorLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0,
                                -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                            overridePendingTransition(0, R.anim.fade_out);
                        }

                        blankPage.doOffset(positionOffset);
                        pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));

                    } else if (positionOffset == 0) {
                        if (position == 1) {
                            doPageSelectedComments(position);
                        } else {
                            //todo if (mAsyncGetSubreddit != null) {
                            //mAsyncGetSubreddit.cancel(true);
                            //}

                            if (header.getTranslationY() == 0) {
                                header.animate()
                                        .translationY(-header.getHeight())
                                        .setInterpolator(new LinearInterpolator())
                                        .setDuration(180);
                            }

                            pager.setSwipeLeftOnly(true);
                            themeSystemBars(openingComments.getSubredditName().toLowerCase(Locale.ENGLISH));
                            setRecentBar(openingComments.getSubredditName().toLowerCase(Locale.ENGLISH));

                        }
                    }

                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (pager.getAdapter() != null) {
                pager.getAdapter().notifyDataSetChanged();
                pager.setCurrentItem(1);
                pager.setCurrentItem(0);

            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void doSetPrimary(Object object, int position) {
            if (position != 2 && position != 0) {
                if (getCurrentFragment() != object) {
                    mCurrentFragment = ((SubmissionsView) object);
                    if (mCurrentFragment != null
                            && mCurrentFragment.posts == null
                            && mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }
                }
            }

        }

        @Override
        public int getItemPosition(Object object) {
            if (object != storedFragment) return POSITION_NONE;
            return POSITION_UNCHANGED;
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 0) {
                blankPage = new BlankFragment();
                return blankPage;
            } else if (openingComments == null || i != 2) {
                SubmissionsView f = new SubmissionsView();
                Bundle args = new Bundle();
                args.putString("id", subreddit);
                f.setArguments(args);
                return f;

            } else {
                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                String name = openingComments.getFullName();
                args.putString("id", name.substring(3, name.length()));
                args.putBoolean("archived", openingComments.isArchived());
                args.putBoolean("contest",
                        openingComments.getDataNode().get("contest_mode").asBoolean());
                args.putBoolean("locked", openingComments.isLocked());
                args.putInt("page", currentComment);
                args.putString("subreddit", openingComments.getSubredditName());
                args.putString("baseSubreddit", subreddit);
                f.setArguments(args);
                return f;
            }


        }

        @Override
        public int getCount() {
            return size;
        }


    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {

            if (subreddit != null) {
                setResult(RESULT_OK);
                sub = subreddit;
                try {
                    doSubSidebarNoLoad(sub.getDisplayName());
                    doSubSidebar(sub.getDisplayName());
                    doSubOnlyStuff(sub);
                } catch (NullPointerException e) { //activity has been killed
                    if (!isFinishing()) finish();
                }
                SubredditView.this.subreddit = sub.getDisplayName();

                if (subreddit.isNsfw()
                        && SettingValues.storeHistory
                        && SettingValues.storeNSFWHistory) {
                    UserSubscriptions.addSubToHistory(subreddit.getDisplayName());
                } else if (SettingValues.storeHistory && !subreddit.isNsfw()) {
                    UserSubscriptions.addSubToHistory(subreddit.getDisplayName());
                }

                // Over 18 interstitial for signed out users or those who haven't enabled NSFW content
                if (subreddit.isNsfw() && (!SettingValues.showNSFWContent)) {
                    new AlertDialogWrapper.Builder(SubredditView.this).setTitle(
                            getString(R.string.over18_title, subreddit.getDisplayName()))
                            .setMessage(getString(R.string.over18_desc) + "\n\n" + getString(
                                    Authentication.isLoggedIn ? R.string.over18_desc_loggedin
                                            : R.string.over18_desc_loggedout))
                            .setCancelable(false)
                            .setPositiveButton(R.string.misc_continue,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ((SubmissionsView) adapter.getCurrentFragment()).doAdapter(
                                                    true);
                                        }
                                    })
                            .setNeutralButton(R.string.btn_go_back,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                            overridePendingTransition(0, R.anim.fade_out);
                                        }
                                    })
                            .show();
                }
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                Subreddit result = Authentication.reddit.getSubreddit(params[0]);
                if (result.isNsfw() == null) {
                    // Sub is probably a user profile backing subreddit for a deleted/suspended user
                    throw new Exception("Sub has null values where it shouldn't");
                }
                return result;
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(SubredditView.this).setTitle(
                                    R.string.subreddit_err)
                                    .setMessage(
                                            getString(R.string.subreddit_err_msg_new, params[0]))
                                    .setPositiveButton(R.string.btn_ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    dialog.dismiss();
                                                    setResult(4);
                                                    finish();
                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            setResult(4);
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception ignored) {

                        }
                    }
                });
                e.printStackTrace();

                return null;
            }
        }

    }

}
