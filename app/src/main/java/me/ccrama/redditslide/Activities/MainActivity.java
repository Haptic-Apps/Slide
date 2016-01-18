package me.ccrama.redditslide.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.util.JrawUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.DragSort.ListViewDraggingAnimation;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;

public class MainActivity extends BaseActivity {
    // Instance state keys
    static final String SUBS = "subscriptions";
    static final String SUBS_ALPHA = "subscriptionsAlpha";
    static final String REAL_SUBS = "realSubscriptions";
    static final String LOGGED_IN = "loggedIn";
    static final String IS_MOD = "ismod";
    static final String USERNAME = "username";

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
    boolean changed;
    String term;
    View headerMain;
    private AsyncGetSubreddit mAsyncGetSubreddit = null;
    private TabLayout mTabLayout;
    private boolean mShowInfoButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            int current = pager.getCurrentItem();
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
        } else if (requestCode == 1) {
            restartTheme();
        } else if (requestCode == 3) {
            resetAdapter();
        } else if (requestCode == 4 && resultCode != 4) { //what?
            if (e != null) {
                e.clearFocus();
                e.setText("");
                drawerLayout.closeDrawers();
            }
        } else if (requestCode == 55) {
            doDrawer();
            setDataSet(SubredditStorage.subredditsForHome);
        } else if (requestCode == 66) {
            new AsyncTask<Void, Void, Void>() {
                int count;

                @Override
                protected Void doInBackground(Void... params) {
                    count = Authentication.reddit.me().getInboxCount();

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (count == 0) {
                        headerMain.findViewById(R.id.count).setVisibility(View.GONE);
                    } else {
                        ((TextView) headerMain.findViewById(R.id.count)).setText(count + "");
                    }
                }
            }.execute();
        }
    }

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

                    new AlertDialogWrapper.Builder(this).setTitle(R.string.err_permission)
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
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
        if (!Reddit.colors.contains("Tutorial")) {
            first = true;
            Intent i = new Intent(this, Tutorial.class);
            startActivityForResult(i, 55);
        } else if (!Reddit.colors.contains("v4.5update")) {
            new MaterialDialog.Builder(this)
                    .title("Slide v4.5")
                    .content("I’m proud to announce Slide v4.5. RAM use has been greatly reduced, stability increased, and lots of new features added! \n" +
                            "\t•Offline mode and auto data caching\n" +
                            "\t•Startup time now close to instant\n" +
                            "\t•Swipe back from anywhere (enable in General Settings)\n" +
                            "\t•Improved toolbar and subreddit sidebar\n" +
                            "\t•Search reddit\n" +
                            "\t•Center image card mode\n" +
                            "\t•Improved theme dialog\n" +
                            "\t•Edit and delete comments\n" +
                            "\t•Redesigned settings\n" +
                            "\t•Reduced memory use by ¾\n" +
                            "\t•TONS of stability improvements and bugfixes\n" +
                            "\t•Improved backup and restore settings\n" +
                            "\t•Enter animations\n" +
                            "\t•Fixed opening from background randomly\n" +
                            "\t•Cache gifs for faster retrieval and less data use\n" +
                            "\t•Mutliselect edit subreddit theme\n" +
                            "\t•Much more!\n"
                            + "Make sure to report all bugs to the G+ group!")
                    .positiveText("Will do!")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Reddit.colors.edit().putBoolean("v4.5update", true).apply();

                        }
                    })
                    .dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Reddit.colors.edit().putBoolean("v4.5update", true).apply();

                        }
                    })
                    .show();
        }

        if (savedInstanceState != null && !changed) {

            SubredditStorage.subredditsForHome = savedInstanceState.getStringArrayList(SUBS);
            SubredditStorage.alphabeticalSubreddits =
                    savedInstanceState.getStringArrayList(SUBS_ALPHA);
            Authentication.isLoggedIn = savedInstanceState.getBoolean(LOGGED_IN);
            Authentication.name = savedInstanceState.getString(USERNAME);
            Authentication.didOnline = savedInstanceState.getBoolean("ONLINE");

            Authentication.mod = savedInstanceState.getBoolean(IS_MOD);
        } else {
            changed = false;
        }

        if (getIntent().getBooleanExtra("EXIT", false)) finish();

        if (Reddit.autoTime) {
            int hour = Calendar.getInstance().getTime().getHours();

            String base = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().toLowerCase();
            int number;
            if (hour >= Reddit.nighttime && base.contains("light")) {
                number = 0;
            } else if (hour >= Reddit.daytime && (base.contains("dark") || base.contains("amoled"))) {
                number = 1;
            } else {
                number = 3;
            }
            if (number != 3) {
                String name = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_")[1];
                final String newName = name.replace("(", "");
                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                    if (theme.toString().contains(newName) && theme.getThemeType() == number) {
                        getTheme().applyStyle(theme.getBaseId(), true);
                        Reddit.themeBack = theme.getThemeType();
                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                        break;
                    }
                }
            } else {
                getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

            }

        }
        applyColorTheme();

        setContentView(R.layout.activity_overview);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        if (getIntent() != null && getIntent().hasExtra("pageTo"))
            toGoto = getIntent().getIntExtra("pageTo", 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(Palette.getDarkerColor(Palette.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);

        singleMode = Reddit.single;
        // Inflate tabs if single mode is disabled
        if (!singleMode)
            mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        // Disable swiping if single mode is enabled
        if (singleMode) pager.setSwipingEnabled(false);


        if (SubredditStorage.subredditsForHome != null) {
            if (!first)
                doDrawer();

            setDataSet(SubredditStorage.subredditsForHome);

        } else if (!first) {
            ((Reddit) getApplication()).doMainStuff();


            final Dialog d = new MaterialDialog.Builder(this)
                    .title("Loading Data")
                    .cancelable(false)
                    .progress(true, 100)
                    .show();

            findViewById(R.id.header).setVisibility(View.GONE);

            //Hopefully will allow Authentication time to authenticate and for SubredditStorage to get subs list
            mToolbar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (SubredditStorage.subredditsForHome != null) {
                                findViewById(R.id.header).setVisibility(View.VISIBLE);

                                doDrawer();

                                setDataSet(SubredditStorage.subredditsForHome);
                                d.dismiss();
                            } else {
                                mToolbar.postDelayed(this, 2000);
                            }
                        }
                    });

                }
            }, 2000);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        changed = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList(SUBS, SubredditStorage.subredditsForHome);
        savedInstanceState.putStringArrayList(SUBS_ALPHA, SubredditStorage.alphabeticalSubreddits);
        savedInstanceState.putBoolean(LOGGED_IN, Authentication.isLoggedIn);
        savedInstanceState.putBoolean("ONLINE", Authentication.didOnline);

        savedInstanceState.putBoolean(IS_MOD, Authentication.mod);
        savedInstanceState.putString(USERNAME, Authentication.name);
    }

    public void doSubSidebar(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }
        if (!subreddit.equals("all") && !subreddit.equals("frontpage")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }
            mShowInfoButton = true;
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
                if (Reddit.fab && Reddit.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setVisibility(View.GONE);


                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, Submit.class);
                        inte.putExtra("subreddit", subreddit);
                        MainActivity.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, Wiki.class);
                        i.putExtra("subreddit", subreddit);
                        startActivity(i);
                    }
                });

            }


            findViewById(R.id.sub_theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String subreddit = usedArray.get(pager.getCurrentItem());

                    int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit);
                    final Context contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, style);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(subreddit);
                    SettingsSubAdapter.showSubThemeEditor(arrayList, MainActivity.this, dialoglayout);
                }
            });
        } else {
            //Hide info button on frontpage and all
            mShowInfoButton = false;
            invalidateOptionsMenu();

            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
        }
    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    public void updateColor(int color, String subreddit) {
        hea.setBackgroundColor(color);
        findViewById(R.id.header).setBackgroundColor(color);
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
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            } else {
                adapter.notifyDataSetChanged();
            }
            pager.setCurrentItem(1);
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(1);

            themeStatusBar(usedArray.get(0));
            setRecentBar(usedArray.get(0));
            doSubSidebar(usedArray.get(0));

            findViewById(R.id.header).setBackgroundColor(Palette.getColor(usedArray.get(0)));
            if (hea != null)
                hea.setBackgroundColor(Palette.getColor(usedArray.get(0)));
            if (!Reddit.single) {
                mTabLayout.setupWithViewPager(pager);
                mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(usedArray.get(0)));
                pager.setCurrentItem(toGoto);
            } else {
                getSupportActionBar().setTitle(usedArray.get(0));
            }

        } else if (SubredditStorage.subredditsForHome != null) {
            setDataSet(SubredditStorage.subredditsForHome);
        }

    }

    public void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final SpoilerRobotoTextView body = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
            new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, MainActivity.this, subreddit.getDisplayName());
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
            c.setChecked(SubredditStorage.subredditsForHome.contains(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids) {
                            if (isChecked) {
                                SubredditStorage.addSubscription(subreddit.getDisplayName().toLowerCase());
                            } else {
                                SubredditStorage.removeSubscription(subreddit.getDisplayName().toLowerCase());

                            }
                            Snackbar.make(header, isChecked ?
                                    getString(R.string.misc_subscribed) : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
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
        ((TextView) findViewById(R.id.sub_title)).setText(subreddit.getPublicDescription());
        findViewById(R.id.sub_title).setVisibility(subreddit.getPublicDescription().equals("") ? View.GONE : View.VISIBLE);

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    public void openPopup() {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.defaultSorting = Sorting.HOT;
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.defaultSorting = Sorting.NEW;
                        reloadSubs();
                        break;
                    case 2:
                        Reddit.defaultSorting = Sorting.RISING;
                        reloadSubs();
                        break;
                    case 3:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.HOUR;
                        reloadSubs();
                        break;
                    case 4:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.DAY;
                        reloadSubs();
                        break;
                    case 5:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.WEEK;
                        reloadSubs();
                        break;
                    case 6:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.MONTH;
                        reloadSubs();
                        break;
                    case 7:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.YEAR;
                        reloadSubs();
                        break;
                    case 8:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.ALL;
                        reloadSubs();
                        break;
                    case 9:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.HOUR;
                        reloadSubs();
                        break;
                    case 10:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.DAY;
                        reloadSubs();
                        break;
                    case 11:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.WEEK;
                        reloadSubs();
                    case 12:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.MONTH;
                        reloadSubs();
                    case 13:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.YEAR;
                        reloadSubs();
                    case 14:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.ALL;
                        reloadSubs();
                }
                SettingValues.defaultSorting = Reddit.defaultSorting;
                SettingValues.timePeriod = Reddit.timePeriod;
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
        builder.show();

    }

    public void doDrawer() {
        final ListView l = (ListView) findViewById(R.id.drawerlistview);
        l.setDividerHeight(0);
        final LayoutInflater inflater = getLayoutInflater();
        final View header;

        if (Authentication.isLoggedIn && Authentication.didOnline) {

            header = inflater.inflate(R.layout.drawer_loggedin, l, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

            l.addHeaderView(header, null, false);
            ((TextView) header.findViewById(R.id.name)).setText(Authentication.name);
            header.findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                    MainActivity.this.startActivity(inte);


                }
            });
            header.findViewById(R.id.reorder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, ListViewDraggingAnimation.class);
                    MainActivity.this.startActivityForResult(inte, 3);
                    subToDo = usedArray.get(pager.getCurrentItem());


                }
            });

            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra("profile", Authentication.name);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.sync).setVisibility(View.GONE);
            header.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseAccounts();
                }
            });
            new AsyncTask<Void, Void, Void>() {
                int count;

                @Override
                protected Void doInBackground(Void... params) {
                    count = Authentication.reddit.me().getInboxCount();

                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (count == 0) {
                        header.findViewById(R.id.count).setVisibility(View.GONE);
                    } else {
                        ((TextView) header.findViewById(R.id.count)).setText(count + "");
                    }
                }
            }.execute();

            header.findViewById(R.id.prof_click).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View body = header.findViewById(R.id.expand_profile);
                    if (body.getVisibility() == View.GONE) {
                        body.setVisibility(View.VISIBLE);
                    } else {
                        body.setVisibility(View.GONE);
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
                    MainActivity.this.startActivityForResult(inte, 66);
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

        } else {
            header = inflater.inflate(R.layout.drawer_loggedout, l, false);
            l.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);

            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
        }
        if (Reddit.hideHeader) {
            header.findViewById(R.id.back).setVisibility(View.GONE);
        }

        View support = header.findViewById(R.id.support);
        if (Reddit.tabletUI) support.setVisibility(View.GONE);
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
                                inte.putExtra("profile", dialog.getInputEditText().getText().toString());
                                MainActivity.this.startActivity(inte);
                            }
                        })
                        .negativeText(R.string.btn_cancel)
                        .show();
            }
        });

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(i, 1);
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
        ArrayList<String> copy = new ArrayList<>();
        if ((Reddit.alphabetical_home && SubredditStorage.alphabeticalSubreddits != null) || (!Reddit.alphabetical_home && SubredditStorage.subredditsForHome != null))
            for (String s : Reddit.alphabetical_home ? SubredditStorage.alphabeticalSubreddits : SubredditStorage.subredditsForHome) {
                copy.add(s);
            }
        e = ((EditText) header.findViewById(R.id.sort));

        final SideArrayAdapter adapter = new SideArrayAdapter(this, copy);
        l.setAdapter(adapter);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.hello_world,
                R.string.hello_world
        )

        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Palette.getColor("alsdkfjasld"));


        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {

                    if (adapter.fitems == null || adapter.fitems.get(0).startsWith(getString(R.string.search_goto))) {
                        Intent inte = new Intent(MainActivity.this, SubredditView.class);
                        inte.putExtra("subreddit", e.getText().toString());
                        MainActivity.this.startActivity(inte);
                    } else
                        pager.setCurrentItem(usedArray.indexOf(adapter.fitems.get(0)));

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
                adapter.getFilter().filter(result);

            }
        });
    }


    public void chooseAccounts() {

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
            Authentication.authentication.edit().remove("lasttoken").commit();

            Reddit.forceRestart(this);
        } else {
            new AlertDialogWrapper.Builder(MainActivity.this)
                    .setTitle(R.string.general_switch_acc)
                    .setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, keys), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            new AlertDialogWrapper.Builder(MainActivity.this)
                                    .setTitle("Switch or Delete")
                                    .setMessage("Would you like to switch to this account or remove this account?")
                                    .setPositiveButton("Switch", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which2) {
                                            if (!accounts.get(keys.get(which)).isEmpty()) {
                                                Authentication.authentication.edit().putString("lasttoken", accounts.get(keys.get(which))).commit();
                                            } else {
                                                ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                                Authentication.authentication.edit().putString("lasttoken", tokens.get(which)).commit();
                                            }

                                            Reddit.forceRestart(MainActivity.this);
                                        }
                                    }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
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


                                    chooseAccounts();

                                }
                            }).show();


                        }
                    }).show();
        }


    }

    public void resetAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usedArray = new ArrayList<>(SubredditStorage.subredditsForHome);

                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                pager.setAdapter(adapter);
                mTabLayout.setupWithViewPager(pager);

                pager.setCurrentItem(usedArray.indexOf(subToDo));

                int color = Palette.getColor(subToDo);
                hea.setBackgroundColor(color);
                findViewById(R.id.header).setBackgroundColor(color);
                themeStatusBar(subToDo);
                setRecentBar(subToDo);
            }
        });
    }

    public void restartTheme() {
        Intent intent = this.getIntent();
        intent.putExtra("pageTo", pager.getCurrentItem());
        finish();

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (Reddit.exit) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_subreddit_overview, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night:
                String base = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().toLowerCase();
                int number;
                if (base.contains("dark") || base.contains("amoled")) {
                    number = 1;
                } else {
                    number = 0;
                }
                String name = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_")[1];
                final String newName = name.replace("(", "");
                for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                    if (theme.toString().contains(newName) && theme.getThemeType() == number) {
                        Reddit.themeBack = theme.getThemeType();
                        new ColorPreferences(MainActivity.this).setFontStyle(theme);
                        changed = true;


                        recreate();

                        break;
                    }
                }
                return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null)
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                return true;
            case R.id.action_sort:
                openPopup();
                return true;
            case R.id.search:
                new MaterialDialog.Builder(this).title(R.string.search_title)
                        .alwaysCallInputCallback()
                        .input(getString(R.string.search_msg), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                term = charSequence.toString();
                            }
                        })
                        .positiveText(R.string.search_all)
                        .negativeText(getString(R.string.search_subreddit, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                Intent i = new Intent(MainActivity.this, Search.class);
                                i.putExtra("term", term);
                                startActivity(i);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                Intent i = new Intent(MainActivity.this, Search.class);
                                i.putExtra("term", term);
                                i.putExtra("subreddit", ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                                Log.v("Slide", "INTENT SHOWS " + term + " AND " + ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                                startActivity(i);
                            }
                        }).show();
                return true;
            case R.id.save:
                saveOffline(((SubmissionsView) adapter.getCurrentFragment()).posts.posts, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                return true;
          /*  case R.id.action_info:
                if (usedArray != null) {
                    String sub = usedArray.get(pager.getCurrentItem());
                    if (!sub.equals("frontpage") && !sub.equals("all")) {
                        ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer
                                (GravityCompat.END);
                    }
                }
                return true;*/
            case R.id.action_shadowbox:
                if (Reddit.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        DataShare.sharedSubreddit =
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                        Intent i = new Intent(this, Shadowbox.class);
                        i.putExtra("page", 0);
                        i.putExtra("subreddit", ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i);
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

    public void saveOffline(List<Submission> submissions, final String subreddit) {
        final MaterialDialog d = new MaterialDialog.Builder(this).title(R.string.offline_caching)
                .progress(false, submissions.size())
                .cancelable(false)
                .show();
        final ArrayList<JsonNode> newSubmissions = new ArrayList<>();
        for (final Submission s : submissions) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    JsonNode s2 = getSubmission(new SubmissionRequest.Builder(s.getId()).sort(CommentSort.CONFIDENCE).build());
                    newSubmissions.add(s2);
                    d.setProgress(newSubmissions.size());
                    if (d.getCurrentProgress() == d.getMaxProgress()) {
                        d.cancel();
                        Cache.writeSubredditJson(newSubmissions, subreddit);

                    }
                    return null;
                }
            }.execute();
        }
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


        RestResponse response = Authentication.reddit.execute(Authentication.reddit.request()
                .path(String.format("/comments/%s", request.getId()))
                .query(args)
                .build());
        return response.getJson();
    }

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

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Reddit.currentPosition = position;

                    doSubSidebar(usedArray.get(position));

                    // ((SubmissionsView) getCurrentFragment()).doAdapter();
                    if (adapter.getCurrentFragment() != null) {
                        SubredditPosts p = ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet;
                        if (p.offline && p.cached != null) {
                            Toast.makeText(MainActivity.this, "Last updated " + TimeUtils.getTimeAgo(p.cached.time, MainActivity.this), Toast.LENGTH_LONG).show();


                        }
                    }

                    if (Reddit.single) {
                        hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                        header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                        themeStatusBar(usedArray.get(position));
                        getSupportActionBar().setTitle(usedArray.get(position));
                    } else {

                        if (hea != null)
                            hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                        header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                        themeStatusBar(usedArray.get(position));
                        mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));
                    }
                    setRecentBar(usedArray.get(position));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            pager.setCurrentItem(1);
            pager.setCurrentItem(0);
        }


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
                return usedArray.get(position);
            } else {
                return "";
            }


        }
    }


}