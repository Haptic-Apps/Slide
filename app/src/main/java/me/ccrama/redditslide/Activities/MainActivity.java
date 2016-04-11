package me.ccrama.redditslide.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.RestResponse;
import net.dean.jraw.http.SubmissionRequest;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;
import net.dean.jraw.util.JrawUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask;
import me.ccrama.redditslide.Synccit.SynccitRead;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.AlbumUtils;
import me.ccrama.redditslide.util.GifUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.SubmissionParser;


public class MainActivity extends BaseActivity {
    public static final String EXTRA_PAGE_TO = "pageTo";
    public static final String IS_ONLINE = "online";
    // Instance state keys
    static final String SUBS = "subscriptions";
    static final String LOGGED_IN = "loggedIn";
    static final String USERNAME = "username";
    static final int TUTORIAL_RESULT = 55;
    static final int INBOX_RESULT = 66;
    static final int RESET_ADAPTER_RESULT = 3;
    static final int SETTINGS_RESULT = 2;
    public static Loader loader;
    public static boolean datasetChanged;
    public boolean singleMode;
    public ToggleSwipeViewPager pager;
    public List<String> usedArray;
    public DrawerLayout drawerLayout;
    public View hea;
    public EditText e;
    public View header;
    public String subToDo;
    public OverviewPagerAdapter adapter;
    public int toGoto = 0;
    public boolean first = true;
    public TabLayout mTabLayout;
    public ListView drawerSubList;
    boolean changed;
    String term;
    View headerMain;
    private AsyncGetSubreddit mAsyncGetSubreddit = null;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList(SUBS, (ArrayList<String>) usedArray);
        savedInstanceState.putBoolean(LOGGED_IN, Authentication.isLoggedIn);
        savedInstanceState.putBoolean(IS_ONLINE, Authentication.didOnline);
        savedInstanceState.putString(USERNAME, Authentication.name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_RESULT) {
            int current = pager.getCurrentItem();
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(current);
            }
        } else if (requestCode == 423 && resultCode == RESULT_OK) {
            ((OverviewPagerAdapterComment) adapter).mCurrentComments.doResult(data);
        } else if (requestCode == 940) {
            if (adapter != null && adapter.getCurrentFragment() != null) {
                if (resultCode == RESULT_OK) {
                    LogUtil.v("Doing hide posts");
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView(data.getIntegerArrayListExtra("seen"));
                } else {
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
                }
            }
        } else if (requestCode == RESET_ADAPTER_RESULT) {
            resetAdapter();
            setDrawerSubList();
        } else if (requestCode == 4 && resultCode != 4) { //what?
            if (e != null) {
                e.clearFocus();
                e.setText("");
                drawerLayout.closeDrawers();
            }
        } else if (requestCode == TUTORIAL_RESULT) {
            UserSubscriptions.doMainActivitySubs(this);
        } else if (requestCode == INBOX_RESULT) {
            //update notification badge
            new AsyncNotificationBadge().execute();
        } else if (requestCode == 3333) {
            this.data = data;
            if (doImage != null) {
                Handler handler = new Handler();
                handler.post(doImage);
            }
        }
      /* todo  if(resultCode == 4 && UserSubscriptions.hasChanged){
            UserSubscriptions.hasChanged = false;
            sideArrayAdapter.setSideItems(UserSubscriptions.getAllSubreddits(this));
            sideArrayAdapter.notifyDataSetChanged();
        }*/
    }

    public Runnable doImage;
    public Intent data;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            changed = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changed = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialogWrapper.Builder(MainActivity.this).setTitle(R.string.err_permission)
                                    .setMessage(R.string.err_permission_msg)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ActivityCompat.requestPermissions(MainActivity.this,
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    1);

                                        }
                                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                        }
                    });

                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean commentPager = false;

    Dialog d;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (Reddit.overrideLanguage) {
            Locale locale = new Locale("en", "US");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        boolean first = false;
        if (Reddit.colors != null && !Reddit.colors.contains("Tutorial")) {
            first = true;
            if (Reddit.appRestart == null)
                Reddit.appRestart = getSharedPreferences("appRestart", 0);

            Reddit.appRestart.edit().putBoolean("firststart52", true).apply();
            Intent i = new Intent(this, Tutorial.class);
            doForcePrefs();
            startActivity(i);
        } else {
            if (Authentication.didOnline && NetworkUtil.isConnected(MainActivity.this) && !checkedPopups) {
                runAfterLoad = new Runnable() {
                    @Override
                    public void run() {
                        runAfterLoad = null;
                        if (Authentication.isLoggedIn) new AsyncNotificationBadge().execute();
                        new AsyncTask<Void, Void, Submission>() {
                            @Override
                            protected Submission doInBackground(Void... params) {
                                if (Authentication.isLoggedIn) UserSubscriptions.doOnlineSyncing();
                                SubredditPaginator p = new SubredditPaginator(Authentication.reddit, "slideforreddit");
                                p.setLimit(2);
                                ArrayList<Submission> posts = new ArrayList<>(p.next());
                                for (Submission s : posts) {
                                    if (s.isStickied() && s.getSubmissionFlair().getText() != null && s.getSubmissionFlair().getText().equalsIgnoreCase("Announcement") && !Reddit.appRestart.contains("announcement" + s.getFullName()) && s.getTitle().contains(BuildConfig.VERSION_NAME)) {
                                        Reddit.appRestart.edit().putBoolean("announcement" + s.getFullName(), true).apply();
                                        return s;
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(final Submission s) {
                                checkedPopups = true;
                                if (s != null) {
                                    Reddit.appRestart.edit().putString("page", s.getDataNode().get("selftext_html").asText()).apply();
                                    Reddit.appRestart.edit().putString("title", s.getTitle()).apply();
                                    Reddit.appRestart.edit().putString("url", s.getUrl()).apply();

                                    Intent i = new Intent(MainActivity.this, Announcement.class);
                                    startActivity(i);
                                }
                            }
                        }.execute();
                    }
                };

            }
        }

        if (savedInstanceState != null && !changed) {
            Authentication.isLoggedIn = savedInstanceState.getBoolean(LOGGED_IN);
            Authentication.name = savedInstanceState.getString(USERNAME);
            Authentication.didOnline = savedInstanceState.getBoolean(IS_ONLINE);
        } else {
            changed = false;
        }

        if (getIntent().getBooleanExtra("EXIT", false)) finish();

        applyColorTheme();

        setContentView(R.layout.activity_overview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        setSupportActionBar(mToolbar);


        if (getIntent() != null && getIntent().hasExtra(EXTRA_PAGE_TO))
            toGoto = getIntent().getIntExtra(EXTRA_PAGE_TO, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(Palette.getDarkerColor(Palette.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);

        singleMode = SettingValues.single;
        if (singleMode)
            commentPager = SettingValues.commentPager;
        // Inflate tabs if single mode is disabled
        if (!singleMode)
            mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        // Disable swiping if single mode is enabled
        if (singleMode) pager.setSwipingEnabled(false);

        sidebarBody = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
        sidebarOverflow = (CommentOverflow) findViewById(R.id.commentOverflow);


        if (!Reddit.appRestart.getBoolean("isRestarting", false) && Reddit.colors.contains("Tutorial"))

        {
            LogUtil.v("Starting main " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "");
            UserSubscriptions.doMainActivitySubs(this);
        } else if (!first)

        {
            LogUtil.v("Starting main 2 " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "");
            Reddit.appRestart.edit().putBoolean("isRestarting", false).commit();
            Reddit.isRestarting = false;
            UserSubscriptions.doMainActivitySubs(this);
        }

        if (mTabLayout != null)

        {
            mTabLayout.setOnTabSelectedListener(
                    new TabLayout.ViewPagerOnTabSelectedListener(pager) {
                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {
                            super.onTabReselected(tab);
                            ((SubmissionsView) adapter.getCurrentFragment()).rv.smoothScrollToPosition(0);

                        }
                    });
        } else

        {
            mToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SubmissionsView) adapter.getCurrentFragment()).rv.smoothScrollToPosition(0);
                }
            });
        }

        System.gc();


    }

    public Runnable runAfterLoad;


    public void updateSubs(ArrayList<String> subs) {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (loader != null) {
            header.setVisibility(View.VISIBLE);

            setDataSet(subs);

            doDrawer();
            try {
                setDataSet(subs);
            } catch (Exception e) {

            }
            loader.finish();
            loader = null;
        } else {
            setDataSet(subs);
            doDrawer();
        }
    }


    public void doForcePrefs() {
        ArrayList<String> domains = new ArrayList<>();

        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                if (!finalS.contains("youtu"))
                    domains.add(finalS);
            }
        }

        //Make youtube and youtu.be links open externally by default, can be used with Chrome Customtabs if they remove the option in settings
        domains.add("youtube.co");
        domains.add("youtu.be");

        SharedPreferences.Editor e = SettingValues.prefs.edit();
        e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, Reddit.arrayToString(domains));
        e.apply();
        PostMatch.externalDomain = null;

        SettingValues.alwaysExternal = SettingValues.prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");
    }

    @Override
    public void onPause() {
        super.onPause();
        changed = false;
        if (!SettingValues.synccitName.isEmpty()) {
            new MySynccitUpdateTask().execute(SynccitRead.newVisited.toArray(new String[SynccitRead.newVisited.size()]));
        }
        if (Authentication.isLoggedIn && Authentication.me != null && Authentication.me.hasGold() && !SynccitRead.newVisited.isEmpty())
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        String[] returned = new String[SynccitRead.newVisited.size()];
                        int i = 0;
                        for (String s : SynccitRead.newVisited) {
                            if (!s.contains("t3_")) {
                                s = "t3_" + s;
                            }
                            returned[i] = s;
                            i++;
                        }
                        new AccountManager(Authentication.reddit).storeVisits(returned);
                        SynccitRead.newVisited = new ArrayList<>();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
    }

    public void doSubSidebar(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }
        if (!subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("frontpage") &&
                !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod") &&
                !subreddit.contains("+")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }
            invalidateOptionsMenu();

            mAsyncGetSubreddit = new AsyncGetSubreddit();
            mAsyncGetSubreddit.execute(subreddit);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);

            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subreddit));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subreddit);
            View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox pinned = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                View submit = (dialoglayout.findViewById(R.id.submit));
                if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                    pinned.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setVisibility(View.GONE);


                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, Submit.class);
                        inte.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        MainActivity.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.equalsIgnoreCase("frontpage") || subreddit.equalsIgnoreCase("all") ||
                    subreddit.equalsIgnoreCase("mod") || subreddit.equalsIgnoreCase("friends") ||
                    subreddit.contains("+")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.mods).setVisibility(View.GONE);


            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, Wiki.class);
                        i.putExtra(Wiki.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i);
                    }
                });
                dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, Submit.class);
                        i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i);
                    }
                });
                dialoglayout.findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit);
                        final Context contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, style);
                        LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(subreddit);
                        SettingsSubAdapter.showSubThemeEditor(arrayList, MainActivity.this, dialoglayout);
                    }
                });

                dialoglayout.findViewById(R.id.mods).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog d = new MaterialDialog.Builder(MainActivity.this).title("Finding moderators")
                                .cancelable(true)
                                .content(R.string.misc_please_wait)
                                .progress(true, 100)
                                .show();
                        new AsyncTask<Void, Void, Void>() {
                            ArrayList<UserRecord> mods;

                            @Override
                            protected Void doInBackground(Void... params) {
                                mods = new ArrayList<>();
                                UserRecordPaginator paginator = new UserRecordPaginator(Authentication.reddit, subreddit, "moderators");
                                paginator.setSorting(Sorting.HOT);
                                paginator.setTimePeriod(TimePeriod.ALL);
                                while (paginator.hasNext()) {
                                    mods.addAll(paginator.next());
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                final ArrayList<String> names = new ArrayList<String>();
                                for (UserRecord rec : mods) {
                                    names.add(rec.getFullName());
                                }
                                d.dismiss();
                                new MaterialDialog.Builder(MainActivity.this).title("/r/" + subreddit + " mods")
                                        .items(names)
                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                            @Override
                                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                Intent i = new Intent(MainActivity.this, Profile.class);
                                                i.putExtra(Profile.EXTRA_PROFILE, names.get(which));
                                                startActivity(i);
                                            }
                                        }).show();
                            }
                        }.execute();
                    }
                });
            }
        } else {
            //Hide info button on frontpage and all
            invalidateOptionsMenu();

            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        if (adapter instanceof OverviewPagerAdapterComment) {
            adapter = new OverviewPagerAdapterComment(getSupportFragmentManager());
            pager.setAdapter(adapter);
        } else {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
        }

        shouldLoad = usedArray.get(current);
        pager.setCurrentItem(current);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(pager);
            scrollToTabAfterLayout(current);
        }

        if (SettingValues.single)
            getSupportActionBar().setTitle(shouldLoad);

    }

    private void scrollToTabAfterLayout(final int tabIndex) {
        //from http://stackoverflow.com/a/34780589/3697225
        if (mTabLayout != null) {
            final ViewTreeObserver observer = mTabLayout.getViewTreeObserver();

            if (observer.isAlive()) {
                observer.dispatchOnGlobalLayout(); // In case a previous call is waiting when this call is made
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mTabLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        mTabLayout.getTabAt(tabIndex).select();
                    }
                });
            }
        }
    }

    public void updateColor(int color, String subreddit) {
        hea.setBackgroundColor(color);
        header.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(color));
        }
        setRecentBar(subreddit, color);
        findViewById(R.id.header_sub).setBackgroundColor(color);

    }

    public void setDataSet(List<String> data) {
        if (data != null) {
            usedArray = data;
            if (adapter == null) {
                if (commentPager && singleMode) {
                    adapter = new OverviewPagerAdapterComment(getSupportFragmentManager());
                } else {
                    adapter = new OverviewPagerAdapter(getSupportFragmentManager());
                }
            } else {
                adapter.notifyDataSetChanged();
            }
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(1);
            if (toGoto == -1) toGoto = 0;
            shouldLoad = usedArray.get(toGoto);
            selectedSub = (usedArray.get(toGoto));
            themeSystemBars(usedArray.get(toGoto));

            header.setBackgroundColor(Palette.getColor(usedArray.get(0)));
            if (hea != null)
                hea.setBackgroundColor(Palette.getColor(usedArray.get(0)));
            if (!SettingValues.single) {
                mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(usedArray.get(0)));
                shouldLoad = usedArray.get(toGoto);
                pager.setCurrentItem(toGoto);
                mTabLayout.setupWithViewPager(pager);
                if (mTabLayout != null) {
                    mTabLayout.setupWithViewPager(pager);
                    scrollToTabAfterLayout(toGoto);
                }
            } else {
                getSupportActionBar().setTitle(usedArray.get(toGoto));
                shouldLoad = usedArray.get(toGoto);
                pager.setCurrentItem(toGoto);
            }

            setRecentBar(usedArray.get(toGoto));
            doSubSidebar(usedArray.get(toGoto));


        } else {
            UserSubscriptions.doMainActivitySubs(this);
        }

    }

    SpoilerRobotoTextView sidebarBody;
    CommentOverflow sidebarOverflow;


    private void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subredditName);
            firstTextView.setLinkTextColor(new ColorPreferences(this).getColor(subredditName));
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subredditName);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
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

    public void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            setViews(text, subreddit.getDisplayName(), sidebarBody, sidebarOverflow);
        } else {
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
        }
        {
            CheckBox c = ((CheckBox) findViewById(R.id.subscribed));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //reset check adapter
                }
            });
            c.setChecked(usedArray.contains(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids) {
                            if (isChecked) {
                                UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(), MainActivity.this);
                            } else {
                                UserSubscriptions.removeSubreddit(subreddit.getDisplayName().toLowerCase(), MainActivity.this);
                                pager.setCurrentItem(pager.getCurrentItem() - 1);
                                restartTheme();
                            }
                            Snackbar s = Snackbar.make(mToolbar, isChecked ?
                                    getString(R.string.misc_subscribed) : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
                            View view = s.getView();
                            TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            s.show();
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            if (isChecked) {
                                new AccountManager(Authentication.reddit).subscribe(subreddit);
                            } else {
                                new AccountManager(Authentication.reddit).unsubscribe(subreddit);

                            }
                            return null;
                        }
                    }.execute();

                }
            });
        }
        if (!subreddit.getPublicDescription().isEmpty()) {
            findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
            setViews(subreddit.getDataNode().get("public_description_html").asText(), subreddit.getDisplayName().toLowerCase(), ((SpoilerRobotoTextView) findViewById(R.id.sub_title)), (CommentOverflow) findViewById(R.id.sub_title_overflow));
        } else {
            findViewById(R.id.sub_title).setVisibility(View.GONE);
        }
        ((ImageView) findViewById(R.id.subimage)).setImageResource(0);
        if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode().get("icon_img").asText().isEmpty()) {
            findViewById(R.id.subimage).setVisibility(View.VISIBLE);
            ((Reddit) getApplication()).getImageLoader().displayImage(subreddit.getDataNode().get("icon_img").asText(), (ImageView) findViewById(R.id.subimage));
        } else {
            findViewById(R.id.subimage).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }


    public void openPopup() {
        PopupMenu popup = new PopupMenu(MainActivity.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final String[] base = Reddit.getSortingStrings(getBaseContext());
        for (String s : base) {
            popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (String s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.HOT);
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.NEW);
                        reloadSubs();
                        break;
                    case 2:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.RISING);
                        reloadSubs();
                        break;
                    case 3:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 4:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 5:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.WEEK);
                        reloadSubs();
                        break;
                    case 6:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 7:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 8:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.TOP);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.ALL);
                        reloadSubs();
                        break;
                    case 9:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 10:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 11:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.WEEK);
                        reloadSubs();
                    case 12:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.MONTH);
                        reloadSubs();
                    case 13:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.YEAR);
                        reloadSubs();
                    case 14:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.ALL);
                        reloadSubs();


                }
                return true;
            }
        });
        popup.show();


    }

    private void expand(LinearLayout v) {
        //set Visible
        v.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, v.getMeasuredHeight(), v);
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end, final View v) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    private void collapse(final LinearLayout v) {
        int finalHeight = v.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private static ValueAnimator flipAnimator(boolean isFlipped, final View v) {
        ValueAnimator animator = ValueAnimator.ofFloat(isFlipped ? -1f : 1f, isFlipped ? 1f : -1f);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                v.setScaleY((Float) valueAnimator.getAnimatedValue());
            }
        });
        return animator;
    }

    public void doDrawer() {
        drawerSubList = (ListView) findViewById(R.id.drawerlistview);
        drawerSubList.setDividerHeight(0);
        final LayoutInflater inflater = getLayoutInflater();
        final View header;

        if (Authentication.isLoggedIn && Authentication.didOnline) {

            header = inflater.inflate(R.layout.drawer_loggedin, drawerSubList, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

            drawerSubList.addHeaderView(header, null, false);
            ((TextView) header.findViewById(R.id.name)).setText(Authentication.name);
            header.findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                    MainActivity.this.startActivity(inte);
                }
            });

            header.findViewById(R.id.discover).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Discover.class);
                    MainActivity.this.startActivity(inte);
                }
            });

            header.findViewById(R.id.prof_click).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.saved).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_SAVED, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.commented).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_COMMENT, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.submitted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_SUBMIT, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_UPVOTE, true);
                    MainActivity.this.startActivity(inte);
                }
            });

            header.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Submit.class);
                    inte.putExtra(Submit.EXTRA_SUBREDDIT, selectedSub);
                    MainActivity.this.startActivity(inte);
                }
            });
            //update notification badge

            final LinearLayout profStuff = (LinearLayout) header.findViewById(R.id.accountsarea);
            profStuff.setVisibility(View.GONE);
            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profStuff.getVisibility() == View.GONE) {
                        expand(profStuff);
                        flipAnimator(false, header.findViewById(R.id.headerflip)).start();
                    } else {
                        collapse(profStuff);
                        flipAnimator(true, header.findViewById(R.id.headerflip)).start();
                    }

                }
            });
            final HashMap<String, String> accounts = new HashMap<>();

            for (String s : Authentication.authentication.getStringSet("accounts", new HashSet<String>())) {
                if (s.contains(":")) {
                    accounts.put(s.split(":")[0], s.split(":")[1]);
                } else {

                    accounts.put(s, "");
                }
            }
            final ArrayList<String> keys = new ArrayList<>(accounts.keySet());

            final LinearLayout accountList = (LinearLayout) header.findViewById(R.id.accountsarea);
            for (final String accName : keys) {
                LogUtil.v(accName);
                final View t = getLayoutInflater().inflate(R.layout.account_textview, accountList, false);

                ((TextView) t.findViewById(R.id.name)).setText(accName);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialogWrapper.Builder(MainActivity.this)
                                .setTitle(R.string.profile_remove)
                                .setMessage(R.string.profile_remove_account)
                                .setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog2, int which2) {
                                        Set<String> accounts2 = Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                                        Set<String> done = new HashSet<>();
                                        for (String s : accounts2) {
                                            if (!s.contains(accName)) {
                                                done.add(s);
                                            }
                                        }
                                        Authentication.authentication.edit().putStringSet("accounts", done).commit();
                                        dialog2.dismiss();
                                        accountList.removeView(t);
                                        if (accName.equalsIgnoreCase(Authentication.name)) {

                                            boolean d = false;
                                            for (String s : keys) {

                                                if (!s.equalsIgnoreCase(accName)) {
                                                    d = true;
                                                    LogUtil.v("Switching to " + s);
                                                    if (!accounts.get(s).isEmpty()) {
                                                        Authentication.authentication.edit().putString("lasttoken", accounts.get(s)).remove("backedCreds").commit();

                                                    } else {
                                                        ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                                        Authentication.authentication.edit().putString("lasttoken", tokens.get(keys.indexOf(s))).remove("backedCreds").commit();
                                                    }
                                                    Authentication.name = s;
                                                    UserSubscriptions.switchAccounts();
                                                    Reddit.forceRestart(MainActivity.this, true);
                                                }

                                            }
                                            if (!d) {
                                                Authentication.name = "";
                                                Authentication.isLoggedIn = false;
                                                Authentication.authentication.edit().remove("lasttoken").remove("backedCreds").commit();
                                                UserSubscriptions.switchAccounts();
                                                Reddit.forceRestart(MainActivity.this, true);
                                            }

                                        } else {
                                            accounts.remove(accName);
                                            keys.remove(accName);
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.btn_cancel, null).show();


                    }
                });
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!accName.equalsIgnoreCase(Authentication.name)) {
                            LogUtil.v("Switching to " + accName);
                            if (!accounts.get(accName).isEmpty()) {
                                Authentication.authentication.edit().putString("lasttoken", accounts.get(accName)).remove("backedCreds").commit();
                            } else {
                                ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                Authentication.authentication.edit().putString("lasttoken", tokens.get(keys.indexOf(accName))).remove("backedCreds").commit();
                            }

                            Authentication.name = accName;
                            UserSubscriptions.switchAccounts();

                            Reddit.forceRestart(MainActivity.this, true);
                        }
                    }
                });
                accountList.addView(t);
            }


            header.findViewById(R.id.godown).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout body = (LinearLayout) header.findViewById(R.id.expand_profile);
                    if (body.getVisibility() == View.GONE) {
                        expand(body);
                        flipAnimator(false, view).start();
                    } else {
                        collapse(body);
                        flipAnimator(true, view).start();
                    }
                }
            });
            header.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Inbox.class);
                    MainActivity.this.startActivityForResult(inte, INBOX_RESULT);
                }
            });
            if (Authentication.mod) {
                header.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, ModQueue.class);
                        MainActivity.this.startActivity(inte);
                    }
                });
            } else {
                header.findViewById(R.id.mod).setVisibility(View.GONE);
            }
            headerMain = header;

            if (runAfterLoad == null) {
                new AsyncNotificationBadge().execute();
            }

        } else if (Authentication.didOnline) {
            header = inflater.inflate(R.layout.drawer_loggedout, drawerSubList, false);
            drawerSubList.addHeaderView(header, null, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
        } else {
            header = inflater.inflate(R.layout.drawer_offline, drawerSubList, false);
            headerMain = header;
            drawerSubList.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);

            header.findViewById(R.id.online).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((Reddit) getApplication()).forceRestart(MainActivity.this);
                }
            });
        }

        if (Authentication.didOnline) {

            View support = header.findViewById(R.id.support);
            if (SettingValues.tabletUI) support.setVisibility(View.GONE);
            else {
                header.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                        }
                    }
                });
            }
            header.findViewById(R.id.prof).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .inputRange(3, 20)
                            .alwaysCallInputCallback()
                            .inputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                            .input(getString(R.string.user_enter), null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                    if (input.toString().matches("^[a-zA-Z0-9_-]*$")) {
                                        if (input.length() >= 3 && input.length() <= 20)
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                        dialog.setContent("");
                                    } else {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        dialog.setContent(R.string.user_invalid_msg);
                                    }
                                }
                            })
                            .positiveText(R.string.user_btn_goto)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent inte = new Intent(MainActivity.this, Profile.class);
                                    //noinspection ConstantConditions
                                    inte.putExtra(Profile.EXTRA_PROFILE, dialog.getInputEditText().getText().toString());
                                    MainActivity.this.startActivity(inte);
                                }
                            })
                            .negativeText(R.string.btn_cancel)
                            .show();
                }
            });
        }

        header.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivity(i);
                // Cancel sub loading because exiting the settings will reload it anyway
                if (mAsyncGetSubreddit != null)
                    mAsyncGetSubreddit.cancel(true);
                drawerLayout.closeDrawers();
            }
        });

      /*  footer.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte = new Intent(Overview.this, Setting.class);
                Overview.this.startActivityForResult(inte, 3);
            }
        });*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        final ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                drawerLayout,
                toolbar,
                R.string.hello_world,
                R.string.hello_world
        ) {
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerLayout.getWindowToken(), 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // this disables the animation
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Palette.getColor("alsdkfjasld"));

        setDrawerSubList();
    }

    SideArrayAdapter sideArrayAdapter;

    public void setDrawerSubList() {
        ArrayList<String> copy = new ArrayList<>(usedArray);

        e = ((EditText) headerMain.findViewById(R.id.sort));

        headerMain.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                e.setText("");
            }
        });

        sideArrayAdapter = new SideArrayAdapter(this, copy, UserSubscriptions.getAllSubreddits(this));
        drawerSubList.setAdapter(sideArrayAdapter);

        e.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                drawerSubList.smoothScrollToPositionFromTop(1, e.getHeight());

            }
        });
        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    //If it the input text doesn't match a subreddit from the list exactly, openInSubView is true
                    if (sideArrayAdapter.fitems == null || sideArrayAdapter.openInSubView || !usedArray.contains(e.getText().toString().toLowerCase())) {
                        Intent inte = new Intent(MainActivity.this, SubredditView.class);
                        inte.putExtra(SubredditView.EXTRA_SUBREDDIT, e.getText().toString());
                        MainActivity.this.startActivity(inte);
                    } else {
                        if (usedArray.contains(e.getText().toString())) {
                            pager.setCurrentItem(usedArray.indexOf(e.getText().toString()));
                        } else {
                            pager.setCurrentItem(usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                        }
                    }

                    View view = MainActivity.this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    drawerLayout.closeDrawers();
                    e.setText("");
                }
                return false;
            }
        });

        final View close = findViewById(R.id.close);
        close.setVisibility(View.GONE);

        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = e.getText().toString().replaceAll(" ", "");
                if (result.isEmpty()) {
                    close.setVisibility(View.GONE);
                } else {
                    close.setVisibility(View.VISIBLE);
                }
                sideArrayAdapter.getFilter().filter(result);

            }
        });
    }


    public void chooseAccounts(boolean cancelable) {

        final HashMap<String, String> accounts = new HashMap<>();

        for (String s : Authentication.authentication.getStringSet("accounts", new HashSet<String>())) {
            if (s.contains(":")) {
                accounts.put(s.split(":")[0], s.split(":")[1]);
            } else {

                accounts.put(s, "");
            }
        }
        final ArrayList<String> keys = new ArrayList<>(accounts.keySet());
        if (keys.size() == 0) {
            Authentication.authentication.edit().remove("lasttoken").remove("backedCreds").commit();

            Reddit.forceRestart(this, true);
        } else {
            new AlertDialogWrapper.Builder(MainActivity.this)
                    .setTitle(R.string.profile_manage_accounts)
                    .setCancelable(cancelable)
                    .setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, keys), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            new AlertDialogWrapper.Builder(MainActivity.this)
                                    .setTitle(R.string.profile_remove)
                                    .setMessage(R.string.profile_remove_account)
                                    .setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog2, int which2) {
                                            Set<String> accounts2 = Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                                            Set<String> done = new HashSet<>();
                                            for (String s : accounts2) {
                                                if (!s.contains(keys.get(which))) {
                                                    done.add(s);
                                                }
                                            }
                                            Authentication.authentication.edit().putStringSet("accounts", done).commit();
                                            dialog.dismiss();
                                            dialog2.dismiss();


                                            chooseAccounts(false);

                                        }
                                    })
                                    .setPositiveButton(R.string.btn_cancel, null).show();


                        }
                    }).show();
        }


    }

    public void resetAdapter() {
        if (UserSubscriptions.hasSubs())

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usedArray = new ArrayList<>(UserSubscriptions.getSubscriptions(MainActivity.this));
                    adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                    pager.setAdapter(adapter);
                    if (mTabLayout != null) {
                        mTabLayout.setupWithViewPager(pager);
                        scrollToTabAfterLayout(usedArray.indexOf(subToDo));
                    }

                    pager.setCurrentItem(usedArray.indexOf(subToDo));

                    int color = Palette.getColor(subToDo);
                    hea.setBackgroundColor(color);
                    header.setBackgroundColor(color);
                    themeSystemBars(subToDo);
                    setRecentBar(subToDo);

                }
            });
    }

    public void restartTheme() {
        Intent intent = this.getIntent();
        int page = pager.getCurrentItem();
        if (currentComment == page) page -= 1;
        intent.putExtra(EXTRA_PAGE_TO, page);
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (commentPager && pager.getCurrentItem() == toOpenComments) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        } else if (SettingValues.exit) {
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
            builder.setTitle(R.string.general_confirm_exit);
            builder.setMessage(R.string.general_confirm_exit_msg);
            builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        mToolbar.getMenu().findItem(R.id.theme).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final String subreddit = usedArray.get(pager.getCurrentItem());

                int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit);
                final Context contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, style);
                LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(subreddit);
                SettingsSubAdapter.showSubThemeEditor(arrayList, MainActivity.this, dialoglayout);
                /*
                boolean old = SettingValues.isPicsEnabled(selectedSub);
                SettingValues.setPicsEnabled(selectedSub, !item.isChecked());
                item.setChecked(!item.isChecked());
                reloadSubs();
                invalidateOptionsMenu();*/
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        if (SettingValues.expandedToolbar) {
            inflater.inflate(R.menu.menu_subreddit_overview_expanded, menu);
        } else {
            inflater.inflate(R.menu.menu_subreddit_overview, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String subreddit = ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit;

        switch (item.getItemId()) {

            case R.id.filter:
                filterContent(shouldLoad);

                return true;
            case R.id.sidebar:
                if (!subreddit.equals("all") && !subreddit.equals("frontpage") && !subreddit.contains(".") && !subreddit.contains("+")) {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                } else {
                    Toast.makeText(this, "No sidebar found", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.night: {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());


                builder.setView(dialoglayout);
                final Dialog d = builder.show();

                dialoglayout.findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 2) {
                                Reddit.themeBack = theme.getThemeType();
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                d.dismiss();
                                recreate();

                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.blacklighter).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 4) {
                                Reddit.themeBack = theme.getThemeType();
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                d.dismiss();
                                recreate();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 1) {
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 3) {
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();

                                break;
                            }
                        }
                    }
                });
            }

            return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null)
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                return true;
            case R.id.action_sort:
                if (subreddit.equalsIgnoreCase("friends")) {
                    Snackbar s = Snackbar.make(findViewById(R.id.anchor), "Cannot sort /r/friends", Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    openPopup();
                }
                return true;
            case R.id.search:
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this).title(R.string.search_title)
                        .alwaysCallInputCallback()
                        .input(getString(R.string.search_msg), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                term = charSequence.toString();
                            }
                        });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("random")) {
                    builder.positiveText(getString(R.string.search_subreddit, subreddit))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(), "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                    builder.neutralText(R.string.search_all)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    startActivity(i);
                                }
                            });
                } else {
                    builder.positiveText(R.string.search_all)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();
                return true;
            case R.id.save:
                saveOffline(((SubmissionsView) adapter.getCurrentFragment()).posts.posts, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                return true;
            case R.id.submit: {
                Intent i = new Intent(this, Submit.class);
                i.putExtra(Submit.EXTRA_SUBREDDIT, selectedSub);
                startActivity(i);
            }
            return true;
            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this)
                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            default:
                return false;
        }
    }

    boolean[] chosen;

    public void filterContent(final String subreddit) {
        chosen = new boolean[]{PostMatch.isGif(subreddit), PostMatch.isAlbums(subreddit), PostMatch.isImage(subreddit), PostMatch.isNsfw(subreddit), PostMatch.isSelftext(subreddit), PostMatch.isUrls(subreddit)};

        new AlertDialogWrapper.Builder(this)
                .setTitle("Content to hide in /r/" + subreddit)
                .alwaysCallMultiChoiceCallback()
                .setMultiChoiceItems(new String[]{"Gifs", "Albums", "Images", "NSFW Content", "Selftext", "Websites"}, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.v(chosen[0] + " " + chosen[1] + " " + chosen[2] + " " + chosen[3] + " " + chosen[4] + " " + chosen[5]);
                PostMatch.setChosen(chosen, subreddit);
                reloadSubs();
            }
        }).setNegativeButton("Cancel", null).show();
    }

    AsyncTask caching;

    public void saveOffline(final List<Submission> submissions, final String subreddit) {
        final boolean[] chosen = new boolean[2];
        new AlertDialogWrapper.Builder(this)
                .setTitle("Save submissions for offline viewing")
                .setMultiChoiceItems(new String[]{"Gifs", "Albums"}, new boolean[]{false, false}, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final MaterialDialog d = new MaterialDialog.Builder(MainActivity.this).title(R.string.offline_caching)
                        .progress(false, submissions.size())
                        .cancelable(true)
                        .negativeText(R.string.btn_cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                caching.cancel(true);
                            }
                        })
                        .show();
                caching = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {

                        String newSubmissions = System.currentTimeMillis() + "<SEPARATOR>";

                        int count = 0;
                        for (final Submission s : submissions) {

                            JsonNode s2 = getSubmission(new SubmissionRequest.Builder(s.getId()).sort(CommentSort.CONFIDENCE).build());
                            if (s2 != null) {
                                newSubmissions = newSubmissions + (s2.toString() + "<SEPARATOR>");
                                switch (ContentType.getImageType(s)) {
                                    case GFY:
                                    case GIF:
                                    case NONE_GIF:
                                    case NSFW_GIF:
                                    case NONE_GFY:
                                    case NSFW_GFY:
                                        if (chosen[0])
                                            GifUtils.saveGifToCache(MainActivity.this, s.getUrl());
                                        break;
                                    case ALBUM:
                                        if (chosen[1])

                                            AlbumUtils.saveAlbumToCache(MainActivity.this, s.getUrl());
                                        break;
                                }
                            } else {
                                d.setMaxProgress((d.getMaxProgress() - 1));
                            }
                            count++;
                            d.setProgress(count);
                            if (d.getCurrentProgress() == d.getMaxProgress()) {
                                d.cancel();

                                OfflineSubreddit.getSubreddit(subreddit).overwriteSubmissions(newSubmissions);

                            }
                        }
                        return null;
                    }
                }.execute();
            }

        }).show();


    }

    public JsonNode getSubmission(SubmissionRequest request) throws NetworkException {
        Map<String, String> args = new HashMap<>();
        if (request.getDepth() != null)
            args.put("depth", Integer.toString(request.getDepth()));
        if (request.getContext() != null)
            args.put("context", Integer.toString(request.getContext()));
        if (request.getLimit() != null)
            args.put("limit", Integer.toString(request.getLimit()));
        if (request.getFocus() != null && !JrawUtils.isFullname(request.getFocus()))
            args.put("comment", request.getFocus());

        CommentSort sort = request.getSort();
        if (sort == null)
            // Reddit sorts by confidence by default
            sort = CommentSort.CONFIDENCE;
        args.put("sort", sort.name().toLowerCase());

        try {

            RestResponse response = Authentication.reddit.execute(Authentication.reddit.request()
                    .path(String.format("/comments/%s", request.getId()))
                    .query(args)
                    .build());
            return response.getJson();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkedPopups;

    @Override
    public void onResume() {
        super.onResume();
        if (Authentication.isLoggedIn && Authentication.didOnline && NetworkUtil.isConnected(MainActivity.this) && headerMain != null && runAfterLoad == null) {
            new AsyncNotificationBadge().execute();
        }

        Reddit.setDefaultErrorHandler(this);
        if (datasetChanged && UserSubscriptions.hasSubs() && !usedArray.isEmpty()) {
            usedArray = new ArrayList<>(UserSubscriptions.getSubscriptions(this));
            adapter.notifyDataSetChanged();
            sideArrayAdapter.notifyDataSetChanged();
            datasetChanged = false;
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(pager.getCurrentItem());
            }
        }
        //Only refresh the view if a Setting was altered
        if (Settings.changed || SettingsTheme.changed || (usedArray != null && usedArray.size() != UserSubscriptions.getSubscriptions(this).size())) {
            int current = pager.getCurrentItem();
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(current);
            }
            reloadSubs();
            //If the user changed a Setting regarding the app's theme, restartTheme()
            if (SettingsTheme.changed || (usedArray != null && usedArray.size() != UserSubscriptions.getSubscriptions(this).size())) {
                restartTheme();
            }
            SettingsTheme.changed = false;
            Settings.changed = false;
        }
    }

    public static boolean dontAnimate;

    public class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null)
                doSubOnlyStuff(subreddit);
        }

        @Override
        protected Subreddit doInBackground(String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                return null;
            }

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (pager != null && SettingValues.commentPager && pager.getCurrentItem() == toOpenComments && SettingValues.commentNav && pager.getAdapter() instanceof OverviewPagerAdapterComment) {
            int keyCode = event.getKeyCode();
            if (SettingValues.commentNav) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        return ((OverviewPagerAdapterComment) pager.getAdapter()).mCurrentComments.onKeyDown(keyCode, event);
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        return ((OverviewPagerAdapterComment) pager.getAdapter()).mCurrentComments.onKeyDown(keyCode, event);
                    default:
                        return super.dispatchKeyEvent(event);
                }
            } else {
                return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);

    }


    public static String shouldLoad;

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private SubmissionsView mCurrentFragment;

        @Override
        public Parcelable saveState() {
            return null;
        }

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(final int position) {
                    header.animate()
                            .translationY(0)
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);

                    Reddit.currentPosition = position;
                    doSubSidebar(usedArray.get(position));

                    SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
                    if (page != null && page.adapter != null) {
                        SubredditPosts p = page.adapter.dataSet;
                        if (p.offline && p.cached != null) {
                            Toast.makeText(MainActivity.this, getString(R.string.offline_last_update, TimeUtils.getTimeAgo(p.cached.time, MainActivity.this)), Toast.LENGTH_LONG).show();
                        }
                    }

                    if (hea != null)
                        hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                    header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                    themeSystemBars(usedArray.get(position));
                    setRecentBar(usedArray.get(position));

                    if (SettingValues.single || mTabLayout == null)
                        getSupportActionBar().setTitle(usedArray.get(position));
                    else mTabLayout.setSelectedTabIndicatorColor(
                            new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));

                    selectedSub = usedArray.get(position);


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


        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null && getCurrentFragment() != object && position != toOpenComments && object instanceof SubmissionsView) {
                shouldLoad = usedArray.get(position);
                mCurrentFragment = ((SubmissionsView) object);
                if (mCurrentFragment.posts == null) {
                    if (mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }

                }
            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (usedArray.size() >= position)
                doSetPrimary(object, position);
        }

        @Override
        public Fragment getItem(int i) {

            SubmissionsView f = new SubmissionsView();
            Bundle args = new Bundle();
            args.putString("id", usedArray.get(i));
            f.setArguments(args);

            return f;


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

            if (usedArray != null) {
                return StringUtils.abbreviate(usedArray.get(position), 25);
            } else {
                return "";
            }


        }
    }

    public int currentComment;
    public Submission openingComments;
    public int toOpenComments = -1;

    public void doPageSelectedComments(int position) {

        pager.setSwipeLeftOnly(false);

        header.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);


        Reddit.currentPosition = position;
        if (position + 1 != currentComment) {
            doSubSidebar(usedArray.get(position));
        }
        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
        if (page != null && page.adapter != null) {
            SubredditPosts p = page.adapter.dataSet;
            if (p.offline && p.cached != null) {
                Toast.makeText(MainActivity.this, getString(R.string.offline_last_update, TimeUtils.getTimeAgo(p.cached.time, MainActivity.this)), Toast.LENGTH_LONG).show();
            }
        }

        if (hea != null)
            hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
        header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
        themeSystemBars(usedArray.get(position));
        setRecentBar(usedArray.get(position));

        if (SettingValues.single)
            getSupportActionBar().setTitle(usedArray.get(position));
        else mTabLayout.setSelectedTabIndicatorColor(
                new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));

        selectedSub = usedArray.get(position);
    }

    public class OverviewPagerAdapterComment extends OverviewPagerAdapter {
        private SubmissionsView mCurrentFragment;
        private CommentPage mCurrentComments;

        public int size = usedArray.size();

        @Override
        public Parcelable saveState() {
            return null;
        }

        public OverviewPagerAdapterComment(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        if (position != toOpenComments) {
                            doPageSelectedComments(position);
                            if (position == toOpenComments - 1 && adapter != null && adapter.getCurrentFragment() != null) {
                                ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
                            }
                        } else {
                            if (mAsyncGetSubreddit != null) {
                                mAsyncGetSubreddit.cancel(true);
                            }

                            if (header.getTranslationY() == 0)
                                header.animate()
                                        .translationY(-header.getHeight())
                                        .setInterpolator(new LinearInterpolator())
                                        .setDuration(180);
                            pager.setSwipeLeftOnly(true);
                            themeSystemBars(openingComments.getSubredditName().toLowerCase());
                            setRecentBar(openingComments.getSubredditName().toLowerCase());

                        }
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
                pager.getAdapter().notifyDataSetChanged();
                pager.setCurrentItem(1);
                pager.setCurrentItem(0);

            }
        }


        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }


        @Override
        public void doSetPrimary(Object object, int position) {
            if (position != toOpenComments) {
                shouldLoad = usedArray.get(position);
                if (getCurrentFragment() != object) {
                    mCurrentFragment = ((SubmissionsView) object);
                    if (mCurrentFragment != null) {
                        if (mCurrentFragment.posts == null) {
                            if (mCurrentFragment.isAdded()) {
                                mCurrentFragment.doAdapter();
                            }
                        }
                    }
                }
            } else if (object instanceof CommentPage) {
                mCurrentComments = (CommentPage) object;
            }

        }

        public Fragment storedFragment;

        @Override
        public int getItemPosition(Object object) {
            if (object != storedFragment)
                return POSITION_NONE;
            return POSITION_UNCHANGED;
        }

        @Override
        public Fragment getItem(int i) {

            if (openingComments == null || i != toOpenComments) {
                SubmissionsView f = new SubmissionsView();
                Bundle args = new Bundle();
                if (usedArray.size() > i) args.putString("id", usedArray.get(i));
                f.setArguments(args);
                return f;

            } else {
                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                String name = openingComments.getFullName();
                args.putString("id", name.substring(3, name.length()));
                args.putBoolean("archived", openingComments.isArchived());
                args.putBoolean("locked", openingComments.isLocked());
                args.putInt("page", currentComment);
                args.putString("subreddit", openingComments.getSubredditName());
                args.putString("baseSubreddit", subToDo);
                f.setArguments(args);
                return f;
            }


        }


        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return size;
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {

            if (usedArray != null && position != toOpenComments) {
                return StringUtils.abbreviate(usedArray.get(position), 25);
            } else {
                return "";
            }


        }
    }

    public String selectedSub;

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position = ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPosition() - 1;
        } else if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((CatchStaggeredGridLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position = ((PreCachingLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPosition() - 1;
        }
        return position;
    }

    public class AsyncNotificationBadge extends AsyncTask<Void, Void, Void> {

        int count;
        int modCount;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LoggedInAccount me;
                if (Authentication.me == null) {
                    Authentication.me = Authentication.reddit.me();
                    me = Authentication.me;
                    Authentication.mod = me.isMod();
                    Reddit.over18 = me.isOver18();

                    Authentication.authentication.edit().putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod).apply();
                    Authentication.authentication.edit().putBoolean(Reddit.SHARED_PREF_IS_OVER_18, Reddit.over18).apply();

                    if (Reddit.notificationTime != -1) {
                        Reddit.notifications = new NotificationJobScheduler(MainActivity.this);
                        Reddit.notifications.start(getApplicationContext());
                    }
                    final String name = me.getFullName();
                    Authentication.name = name;
                    LogUtil.v("AUTHENTICATED");
                    if (Authentication.reddit.isAuthenticated()) {
                        final Set<String> accounts = Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                        if (accounts.contains(name)) { //convert to new system
                            accounts.remove(name);
                            accounts.add(name + ":" + Authentication.refresh);
                            Authentication.authentication.edit().putStringSet("accounts", accounts).commit(); //force commit
                        }
                        Authentication.isLoggedIn = true;
                        Reddit.notFirst = true;
                    }
                } else {
                    me = Authentication.reddit.me();
                }
                count = me.getInboxCount(); //Force reload of the LoggedInAccount object

            } catch (Exception e) {
                Log.w(LogUtil.getTag(), "Cannot fetch inbox count");
                count = -1;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {


            if (Authentication.mod && headerMain.findViewById(R.id.mod).getVisibility() == View.GONE) {
                headerMain.findViewById(R.id.mod).setVisibility(View.VISIBLE);
                headerMain.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, ModQueue.class);
                        MainActivity.this.startActivity(inte);
                    }
                });
            }

            int oldCount = Reddit.appRestart.getInt("inbox", 0);
            if (count > oldCount) {
                final Snackbar s = Snackbar.make(mToolbar, getResources().getQuantityString(R.plurals.new_messages, count - oldCount, count - oldCount), Snackbar.LENGTH_LONG).setAction(R.string.btn_view, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, Inbox.class);
                        startActivity(i);
                    }
                });

                View view = s.getView();
                TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                tv.setTextColor(Color.WHITE);
                s.show();
            }
            Reddit.appRestart.edit().putInt("inbox", count).apply();

            View badge = headerMain.findViewById(R.id.count);
            if (count == 0) {
                if (badge != null) badge.setVisibility(View.GONE);
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            } else if (count != -1) {
                if (badge != null) badge.setVisibility(View.VISIBLE);
                ((TextView) headerMain.findViewById(R.id.count)).setText(count + "");
            }

            /* Todo possibly
            View modBadge = headerMain.findViewById(R.id.count_mod);

            if (modCount == 0) {
                if (modBadge != null) modBadge.setVisibility(View.GONE);
            } else if (modCount != -1) {
                if (modBadge != null) modBadge.setVisibility(View.VISIBLE);
                ((TextView) headerMain.findViewById(R.id.count)).setText(count + "");
            }*/
        }

    }
}