package me.ccrama.redditslide.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
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
import android.graphics.Point;
import android.graphics.Typeface;
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
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
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
import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;

import org.ligi.snackengage.SnackEngage;
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities;
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce;
import org.ligi.snackengage.conditions.WithLimitedNumberOfTimes;
import org.ligi.snackengage.snacks.BaseSnack;
import org.ligi.snackengage.snacks.RateSnack;

import java.lang.reflect.Field;
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
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.CommentCacheAsync;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
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
import me.ccrama.redditslide.util.EditTextValidator;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
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
    public static Map<String, String> multiNameToSubsMap = new HashMap<>();
    public static boolean checkedPopups;
    public static String shouldLoad;
    public final long ANIMATE_DURATION = 250; //duration of animations
    private final long ANIMATE_DURATION_OFFSET = 45; //offset for smoothing out the exit animations
    public boolean singleMode;
    public ToggleSwipeViewPager pager;
    public List<String> usedArray;
    public DrawerLayout drawerLayout;
    public View hea;
    public EditText drawerSearch;
    public View header;
    public String subToDo;
    public OverviewPagerAdapter adapter;
    public int toGoto = 0;
    public boolean first = true;
    public TabLayout mTabLayout;
    public ListView drawerSubList;
    public String selectedSub; //currently selected subreddit
    public Runnable doImage;
    public Intent data;
    public boolean commentPager = false;
    public Runnable runAfterLoad;
    public boolean canSubmit;
    //if the view mode is set to Subreddit Tabs, save the title ("Slide" or "Slide (debug)")
    public String tabViewModeTitle;
    public int currentComment;
    public Submission openingComments;
    public int toOpenComments = -1;
    boolean changed;
    String term;
    View headerMain;
    MaterialDialog d;
    AsyncTask<View, Void, View> currentFlair;
    SpoilerRobotoTextView sidebarBody;
    CommentOverflow sidebarOverflow;
    View accountsArea;
    SideArrayAdapter sideArrayAdapter;
    Menu menu;
    AsyncTask caching;
    private AsyncGetSubreddit mAsyncGetSubreddit = null;
    private int headerHeight; //height of the header

    public static String abbreviate(final String str, final int maxWidth) {
        if (str.length() <= maxWidth) {
            return str;
        }

        final String abrevMarker = "...";
        return str.substring(0, maxWidth - 3) + abrevMarker;
    }

    /**
     * Set the drawer edge (i.e. how sensitive the drawer is)
     * Based on a given screen width percentage.
     *
     * @param displayWidthPercentage larger the value, the more sensitive the drawer swipe is;
     *                               percentage of screen width
     * @param drawerLayout           drawerLayout to adjust the swipe edge
     */
    public static void setDrawerEdge(Activity activity, final float displayWidthPercentage,
                                     DrawerLayout drawerLayout) {
        try {
            Field mDragger = drawerLayout.getClass().getSuperclass()
                    .getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);

            ViewDragHelper leftDragger = (ViewDragHelper) mDragger.get(drawerLayout);
            Field mEdgeSize = leftDragger.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            final int currentEdgeSize = mEdgeSize.getInt(leftDragger);

            Point displaySize = new Point();
            activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
            mEdgeSize.setInt(leftDragger,
                    Math.max(currentEdgeSize, (int) (displaySize.x * displayWidthPercentage)));
        } catch (Exception e) {
            LogUtil.e(e + ": Exception thrown while changing navdrawer edge size");
        }
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
            if (commentPager && current == currentComment) {
                current = current - 1;
            }
            if (current < 0)
                current = 0;
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(current);
            }
            setToolbarClick();
        } else if (requestCode == 423 && resultCode == RESULT_OK) {
            ((OverviewPagerAdapterComment) adapter).mCurrentComments.doResult(data);
        } else if (requestCode == 940) {
            if (adapter != null && adapter.getCurrentFragment() != null) {
                if (resultCode == RESULT_OK) {
                    LogUtil.v("Doing hide posts");
                    ArrayList<Integer> posts = data.getIntegerArrayListExtra("seen");
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView(posts);
                    if (data.hasExtra("lastPage") && data.getIntExtra("lastPage", 0) != 0 && ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager)
                        ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).scrollToPositionWithOffset(data.getIntExtra("lastPage", 0) + 1, mToolbar.getHeight());
                } else {
                    ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
                }
            }
        } else if (requestCode == RESET_ADAPTER_RESULT) {
            resetAdapter();
            setDrawerSubList();
        } else if (requestCode == 4 && resultCode != 4) { //what?
            if (drawerSearch != null) {
                drawerSearch.clearFocus();
                drawerSearch.setText("");
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        applyOverrideLanguage(); // Re-apply the language override if selected
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

    /**
     * Force English locale if setting is checked
     */
    public void applyOverrideLanguage() {
        if (SettingValues.overrideLanguage) {
            Locale locale = new Locale("en", "US");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        applyOverrideLanguage();

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
            if (Reddit.appRestart == null) {
                Reddit.appRestart = getSharedPreferences("appRestart", 0);
            }

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
                                try {
                                    SubredditPaginator p = new SubredditPaginator(Authentication.reddit, "slideforreddit");
                                    p.setLimit(2);
                                    ArrayList<Submission> posts = new ArrayList<>(p.next());
                                    for (Submission s : posts) {
                                        String version = BuildConfig.VERSION_NAME;
                                        if (version.length() > 5) {
                                            version = version.substring(0, version.lastIndexOf("."));
                                        }
                                        if (s.isStickied() && s.getSubmissionFlair().getText() != null && s.getSubmissionFlair().getText().equalsIgnoreCase("Announcement") && !Reddit.appRestart.contains("announcement" + s.getFullName()) && s.getTitle().contains(version)) {
                                            Reddit.appRestart.edit().putBoolean("announcement" + s.getFullName(), true).apply();
                                            return s;
                                        } else if (BuildConfig.VERSION_NAME.contains("alpha") && s.isStickied() && s.getSubmissionFlair().getText() != null && s.getSubmissionFlair().getText().equalsIgnoreCase("Alpha") && !Reddit.appRestart.contains("announcement" + s.getFullName()) && s.getTitle().contains(BuildConfig.VERSION_NAME)) {
                                            Reddit.appRestart.edit().putBoolean("announcement" + s.getFullName(), true).apply();
                                            return s;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
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

                                    String title;
                                    if (s.getTitle().toLowerCase().contains("release")) {
                                        title = getString(R.string.btn_changelog);
                                    } else {
                                        title = getString(R.string.btn_view);
                                    }
                                    Snackbar snack = Snackbar.make(pager, s.getTitle(), Snackbar.LENGTH_INDEFINITE).setAction(title, new OnSingleClickListener() {
                                        @Override
                                        public void onSingleClick(View v) {
                                            Intent i = new Intent(MainActivity.this, Announcement.class);
                                            startActivity(i);
                                        }
                                    });
                                    View view = snack.getView();
                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snack.show();
                                }
                            }
                        }.execute();
                    }
                };

            }
        }

        if (savedInstanceState != null && !changed) {
            Authentication.isLoggedIn = savedInstanceState.getBoolean(LOGGED_IN);
            Authentication.name = savedInstanceState.getString(USERNAME, "LOGGEDOUT");
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

        //Gets the height of the header
        if (header != null) {
            header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    headerHeight = header.getHeight();
                    header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);

        singleMode = SettingValues.single;
        if (singleMode) {
            commentPager = SettingValues.commentPager;
        }
        // Inflate tabs if single mode is disabled
        if (!singleMode) {
            mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        }
        // Disable swiping if single mode is enabled
        if (singleMode) {
            pager.setSwipingEnabled(false);
        }

        sidebarBody = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
        sidebarOverflow = (CommentOverflow) findViewById(R.id.commentOverflow);

        if (!Reddit.appRestart.getBoolean("isRestarting", false) && Reddit.colors.contains("Tutorial")) {
            LogUtil.v("Starting main " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "LOGGEDOUT");
            UserSubscriptions.doMainActivitySubs(this);
        } else if (!first) {
            LogUtil.v("Starting main 2 " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "LOGGEDOUT");
            Reddit.appRestart.edit().putBoolean("isRestarting", false).commit();
            Reddit.isRestarting = false;
            UserSubscriptions.doMainActivitySubs(this);
        }


        final SharedPreferences seen = getSharedPreferences("SEEN", 0);
        if (!seen.contains("isCleared") && !seen.getAll().isEmpty() || !Reddit.appRestart.contains("hasCleared")) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    KVManger m = KVStore.getInstance();
                    Map<String, ?> values = seen.getAll();
                    for (Map.Entry<String, ?> entry : values.entrySet()) {
                        if (entry.getKey().length() == 6 && entry.getValue() instanceof Boolean) {
                            m.insert(entry.getKey(), "true");
                        } else if (entry.getValue() instanceof Long) {
                            m.insert(entry.getKey(), String.valueOf(seen.getLong(entry.getKey(), 0)));
                        }
                    }
                    seen.edit().clear().putBoolean("isCleared", true).apply();
                    if (getSharedPreferences("HIDDEN_POSTS", 0).getAll().size() != 0) {
                        getSharedPreferences("HIDDEN", 0).edit().clear().apply();
                        getSharedPreferences("HIDDEN_POSTS", 0).edit().clear().apply();
                    }
                    if (!Reddit.appRestart.contains("hasCleared")) {
                        SharedPreferences.Editor e = Reddit.appRestart.edit();
                        Map<String, ?> toClear = Reddit.appRestart.getAll();
                        for (Map.Entry<String, ?> entry : toClear.entrySet()) {
                            if (entry.getValue() instanceof String && ((String) entry.getValue()).length() > 300) {
                                e.remove(entry.getKey());
                            }
                        }
                        e.putBoolean("hasCleared", true);
                        e.apply();
                    }
                    return null;
                }


                @Override
                protected void onPostExecute(Void aVoid) {
                    dismissProgressDialog();
                }

                @Override
                protected void onPreExecute() {
                    d = new MaterialDialog.Builder(MainActivity.this).title("Setting some things up...")
                            .content("Please don't leave this screen. Shouldn't take long!")
                            .progress(true, 100)
                            .cancelable(false)
                            .build();
                    d.show();
                }
            }.execute();

        }
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(MainActivity.this)) {
            // Display an snackbar that asks the user to rate the app after this
            // activity was created 6 times, never again when once clicked or with a maximum of
            // two times.
            SnackEngage.from(MainActivity.this).withSnack(
                    new RateSnack().withConditions(new NeverAgainWhenClickedOnce(),
                            new AfterNumberOfOpportunities(10), new WithLimitedNumberOfTimes(2))
                            .overrideActionText(getString(R.string.misc_rate_msg))
                            .overrideTitleText(getString(R.string.misc_rate_title))
                            .withDuration(BaseSnack.DURATION_INDEFINITE))
                    /*.withSnack(new CustomSnack(new Intent(MainActivity.this, SettingsReddit.class), "Thumbnails are disabled", "Change", "THUMBNAIL_INFO")
                            .withConditions(new AfterNumberOfOpportunities(2),
                                    new WithLimitedNumberOfTimes(2), new NeverAgainWhenClickedOnce())
                            .withDuration(BaseSnack.DURATION_LONG))*/
                    .build().engageWhenAppropriate();
        }

        if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
            setupSubredditSearchToolbar();
        }

        /**
         * int for the current base theme selected.
         * 0 = Dark, 1 = Light, 2 = AMOLED, 3 = Dark blue, 4 = AMOLED with contrast, 5 = Sepia
         */
        SettingValues.currentTheme = new ColorPreferences(this).getFontStyle().getThemeType();
    }

    public void updateSubs(ArrayList<String> subs) {
        if (subs.isEmpty() && !NetworkUtil.isConnected(this)) {
            d = new MaterialDialog.Builder(MainActivity.this)
                    .title("No offline content found")
                    .positiveText("Enter online mode")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Reddit.appRestart.edit().remove("forceoffline").apply();
                            ((Reddit) getApplication()).forceRestart(MainActivity.this);
                        }
                    }).show();
        } else {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            setDrawerEdge(this, Constants.DRAWER_SWIPE_EDGE, drawerLayout);

            if (loader != null) {
                header.setVisibility(View.VISIBLE);

                setDataSet(subs);

                doDrawer();
                try {
                    setDataSet(subs);
                } catch (Exception ignored) {

                }
                loader.finish();
                loader = null;
            } else {
                setDataSet(subs);
                doDrawer();
            }
        }
    }

    public void updateMultiNameToSubs(Map<String, String> subs) {
        multiNameToSubsMap = subs;
    }

    public void setToolbarClick() {
        if (mTabLayout != null) {
            mTabLayout.setOnTabSelectedListener(
                    new TabLayout.ViewPagerOnTabSelectedListener(pager) {
                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {
                            super.onTabReselected(tab);
                            scrollToTop();
                        }
                    });
        } else {
            LogUtil.v("notnull");
            mToolbar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scrollToTop();
                }
            });
        }
    }

    public void scrollToTop() {
        int[] firstVisibleItems;
        int pastVisiblesItems = 0;
        firstVisibleItems = ((CatchStaggeredGridLayoutManager) (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager())).findFirstVisibleItemPositions(null);
        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
            for (int firstVisibleItem : firstVisibleItems) {
                pastVisiblesItems = firstVisibleItem;
            }
        }
        if (pastVisiblesItems > 8) {
            ((SubmissionsView) adapter.getCurrentFragment()).rv.scrollToPosition(0);
            header.animate()
                    .translationY(header.getHeight())
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(0);
        } else {
            ((SubmissionsView) adapter.getCurrentFragment()).rv.smoothScrollToPosition(0);
        }
        ((SubmissionsView) adapter.getCurrentFragment()).resetScroll();
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

        // Make some domains open externally by default, can be used with Chrome Customtabs if they remove the option in settings
        domains.add("youtube.com");
        domains.add("youtu.be");
        domains.add("play.google.com");

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
        if (Authentication.isLoggedIn && Authentication.me != null && Authentication.me.hasGold() && !SynccitRead.newVisited.isEmpty()) {
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

        //Upon leaving MainActivity--hide the toolbar search if it is visible
        if (findViewById(R.id.toolbar_search).getVisibility() == View.VISIBLE) {
            findViewById(R.id.close_search_toolbar).performClick();
        }
    }

    public void doSubSidebarNoLoad(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }

        findViewById(R.id.loader).setVisibility(View.GONE);

        invalidateOptionsMenu();

        if (!subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("frontpage") &&
                !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod") &&
                !subreddit.contains("+") && !subreddit.contains(".") && !subreddit.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);
            findViewById(R.id.active_users).setVisibility(View.GONE);

            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subreddit));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subreddit);

            //Sidebar buttons should use subreddit's accent color
            int subColor = new ColorPreferences(this).getColor(subreddit);
            ((TextView) findViewById(R.id.theme_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.wiki_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.post_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.mods_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.flair_text)).setTextColor(subColor);

        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        }
    }

    public void doSubSidebar(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }
        findViewById(R.id.loader).setVisibility(View.VISIBLE);

        invalidateOptionsMenu();

        if (!subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("frontpage") &&
                !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod") &&
                !subreddit.contains("+") && !subreddit.contains(".") && !subreddit.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            mAsyncGetSubreddit = new AsyncGetSubreddit();
            mAsyncGetSubreddit.execute(subreddit);

            final View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox pinned = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                View submit = (dialoglayout.findViewById(R.id.submit));

                if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                    pinned.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_POST) {
                    submit.setVisibility(View.GONE);
                }

                pinned.setVisibility(View.GONE);

                submit.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        Intent inte = new Intent(MainActivity.this, Submit.class);
                        if (!subreddit.contains("/m/") && canSubmit) {
                            inte.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        }
                        MainActivity.this.startActivity(inte);
                    }
                });
            }

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
                    if ((!subreddit.contains("/m/") || !subreddit.contains(".")) && canSubmit) {
                        i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                    }
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
                            final ArrayList<String> names = new ArrayList<>();
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
                                    })
                                    .positiveText(R.string.btn_message)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent i = new Intent(MainActivity.this, Sendmessage.class);
                                            i.putExtra(Sendmessage.EXTRA_NAME, "/r/" + subreddit);
                                            startActivity(i);
                                        }
                                    }).show();
                        }
                    }.execute();
                }
            });
            dialoglayout.findViewById(R.id.flair).setVisibility(View.GONE);
            if (Authentication.didOnline && Authentication.isLoggedIn) {
                if (currentFlair != null)
                    currentFlair.cancel(true);
                currentFlair = new AsyncTask<View, Void, View>() {
                    List<FlairTemplate> flairs;
                    ArrayList<String> flairText;
                    String current;
                    AccountManager m;

                    @Override
                    protected View doInBackground(View... params) {
                        try {
                            m = new AccountManager(Authentication.reddit);
                            JsonNode node = m.getFlairChoicesRootNode(subreddit, null);
                            flairs = m.getFlairChoices(subreddit, node);

                            FlairTemplate currentF = m.getCurrentFlair(subreddit, node);
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
                        if (flairs != null && !flairs.isEmpty() && flairText != null && !flairText.isEmpty()) {
                            flair.setVisibility(View.VISIBLE);
                            if (current != null) {
                                ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                            }
                            flair.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(MainActivity.this).items(flairText)
                                            .title("Select flair")
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                    final FlairTemplate t = flairs.get(which);
                                                    if (t.isTextEditable()) {
                                                        new MaterialDialog.Builder(MainActivity.this).title("Set flair text")
                                                                .input("Flair text", t.getText(), true, new MaterialDialog.InputCallback() {
                                                                    @Override
                                                                    public void onInput(MaterialDialog dialog, CharSequence input) {

                                                                    }
                                                                }).positiveText("Set")
                                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                                                        final String flair = dialog.getInputEditText().getText().toString();
                                                                        new AsyncTask<Void, Void, Boolean>() {
                                                                            @Override
                                                                            protected Boolean doInBackground(Void... params) {
                                                                                try {
                                                                                    new ModerationManager(Authentication.reddit).setFlair(subreddit, t, flair, Authentication.name);
                                                                                    FlairTemplate currentF = m.getCurrentFlair(subreddit);
                                                                                    if (currentF.getText().isEmpty()) {
                                                                                        current = ("[" + currentF.getCssClass() + "]");
                                                                                    } else {
                                                                                        current = (currentF.getText());
                                                                                    }
                                                                                    return true;
                                                                                } catch (Exception e) {
                                                                                    e.printStackTrace();
                                                                                    return false;
                                                                                }
                                                                            }

                                                                            @Override
                                                                            protected void onPostExecute(Boolean done) {
                                                                                Snackbar s;
                                                                                if (done) {
                                                                                    if (current != null) {
                                                                                        ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                                                                                    }
                                                                                    s = Snackbar.make(mToolbar, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                                } else {
                                                                                    s = Snackbar.make(mToolbar, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                                }
                                                                                if (s != null) {
                                                                                    View view = s.getView();
                                                                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                                    tv.setTextColor(Color.WHITE);
                                                                                    s.show();
                                                                                }
                                                                            }
                                                                        }.execute();
                                                                    }
                                                                }).negativeText(R.string.btn_cancel)
                                                                .show();
                                                    } else {
                                                        new AsyncTask<Void, Void, Boolean>() {
                                                            @Override
                                                            protected Boolean doInBackground(Void... params) {
                                                                try {
                                                                    new ModerationManager(Authentication.reddit).setFlair(subreddit, t, null, Authentication.name);
                                                                    FlairTemplate currentF = m.getCurrentFlair(subreddit);
                                                                    if (currentF.getText().isEmpty()) {
                                                                        current = ("[" + currentF.getCssClass() + "]");
                                                                    } else {
                                                                        current = (currentF.getText());
                                                                    }
                                                                    return true;
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                    return false;
                                                                }
                                                            }

                                                            @Override
                                                            protected void onPostExecute(Boolean done) {
                                                                Snackbar s;
                                                                if (done) {
                                                                    if (current != null) {
                                                                        ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                                                                    }
                                                                    s = Snackbar.make(mToolbar, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                } else {
                                                                    s = Snackbar.make(mToolbar, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                }
                                                                if (s != null) {
                                                                    View view = s.getView();
                                                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                    tv.setTextColor(Color.WHITE);
                                                                    s.show();
                                                                }
                                                            }
                                                        }.execute();
                                                    }
                                                }
                                            }).show();
                                }
                            });
                        }
                    }
                };
                currentFlair.execute(dialoglayout.findViewById(R.id.flair));
            }
        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        }
    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        if (commentPager && current == currentComment) {
            current = current - 1;
        }
        if (current < 0) {
            current = 0;
        }
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


        if (SettingValues.single) {
            getSupportActionBar().setTitle(shouldLoad);
        }

        setToolbarClick();

        if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
            setupSubredditSearchToolbar();
        }
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
        if (accountsArea != null)
            accountsArea.setBackgroundColor(Palette.getDarkerColor(color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(color));
        }
        setRecentBar(subreddit, color);
        findViewById(R.id.header_sub).setBackgroundColor(color);

    }

    public void setDataSet(List<String> data) {
        if (data != null && !data.isEmpty()) {
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
            if (toGoto == -1) {
                toGoto = 0;
            }
            if (toGoto >= usedArray.size()) {
                toGoto -= 1;
            }
            shouldLoad = usedArray.get(toGoto);
            selectedSub = (usedArray.get(toGoto));
            themeSystemBars(usedArray.get(toGoto));

            final String USEDARRAY_0 = usedArray.get(0);
            header.setBackgroundColor(Palette.getColor(USEDARRAY_0));

            if (hea != null) {
                hea.setBackgroundColor(Palette.getColor(USEDARRAY_0));
                if (accountsArea != null) {
                    accountsArea.setBackgroundColor(Palette.getDarkerColor(USEDARRAY_0));
                }
            }

            if (!SettingValues.single) {
                mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(USEDARRAY_0));
                pager.setCurrentItem(toGoto);
                mTabLayout.setupWithViewPager(pager);
                if (mTabLayout != null) {
                    mTabLayout.setupWithViewPager(pager);
                    scrollToTabAfterLayout(toGoto);
                }
            } else {
                getSupportActionBar().setTitle(usedArray.get(toGoto));
                pager.setCurrentItem(toGoto);
            }
            setToolbarClick();

            setRecentBar(usedArray.get(toGoto));
            doSubSidebarNoLoad(usedArray.get(toGoto));
        } else if (NetworkUtil.isConnected(this)) {
            UserSubscriptions.doMainActivitySubs(this);
        }
    }

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

    private void changeSubscription(Subreddit subreddit, boolean isChecked) {
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

    public void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSubredditType() != null)
            canSubmit = !subreddit.getSubredditType().equals("RESTRICTED");
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
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        public void onPostExecute(Boolean success) {
                            if (!success) { // If subreddit was removed from account or not

                                new AlertDialogWrapper.Builder(MainActivity.this).setTitle(R.string.force_change_subscription)
                                        .setMessage(R.string.force_change_subscription_desc)
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                changeSubscription(subreddit, isChecked); // Force remove the subscription
                                            }
                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setCancelable(false)
                                        .show();
                            } else {
                                changeSubscription(subreddit, isChecked);
                            }

                        }

                        @Override
                        protected Boolean doInBackground(Void... params) {
                            try {
                                if (isChecked) {
                                    new AccountManager(Authentication.reddit).subscribe(subreddit);
                                } else {
                                    new AccountManager(Authentication.reddit).unsubscribe(subreddit);
                                }

                            } catch (NetworkException e) {
                                return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                            }
                            return true;
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
        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers_string, subreddit.getLocalizedSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.active_users)).setText(getString(R.string.subreddit_active_users_string, subreddit.getAccountsActive()));
        findViewById(R.id.active_users).setVisibility(View.VISIBLE);
    }

    public void openPopup() {
        PopupMenu popup = new PopupMenu(MainActivity.this, findViewById(R.id.anchor), Gravity.RIGHT);
        String id = ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id;
        final String[] base = Reddit.getSortingStrings(getBaseContext(), id, true);
        for (String s : base) {
            MenuItem m = popup.getMenu().add(s);
            if (s.startsWith("» ")) {
                SpannableString spanString = new SpannableString(s.replace("» ", ""));
                spanString.setSpan(new ForegroundColorSpan(new ColorPreferences(MainActivity.this).getColor(id)), 0, spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                m.setTitle(spanString);
            }
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
                        break;
                    case 12:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 13:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 14:
                        Reddit.setSorting(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, Sorting.CONTROVERSIAL);
                        Reddit.setTime(((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id, TimePeriod.ALL);
                        reloadSubs();
                        break;
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

    public void doDrawer() {
        drawerSubList = (ListView) findViewById(R.id.drawerlistview);
        drawerSubList.setDividerHeight(0);
        drawerSubList.setDescendantFocusability(ListView.FOCUS_BEFORE_DESCENDANTS);
        final LayoutInflater inflater = getLayoutInflater();
        final View header;

        if (Authentication.isLoggedIn && Authentication.didOnline) {

            header = inflater.inflate(R.layout.drawer_loggedin, drawerSubList, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

            drawerSubList.addHeaderView(header, null, false);
            ((TextView) header.findViewById(R.id.name)).setText(Authentication.name);
            header.findViewById(R.id.multi).setOnClickListener(
                    new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View view) {
                            if (runAfterLoad == null) {
                                Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                                MainActivity.this.startActivity(inte);
                            }
                        }
                    }
            );
            header.findViewById(R.id.multi).setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .inputRange(3, 20)
                                    .alwaysCallInputCallback()
                                    .input(
                                            getString(R.string.user_enter),
                                            null,
                                            new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    final EditText editText = dialog.getInputEditText();
                                                    EditTextValidator.validateUsername(editText);
                                                    if (input.length() >= 3 && input.length() <= 20)
                                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                }
                                            }
                                    )
                                    .positiveText(R.string.user_btn_gotomultis)
                                    .onPositive(
                                            new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    if (runAfterLoad == null) {
                                                        Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                                                        inte.putExtra(Profile.EXTRA_PROFILE, dialog.getInputEditText().getText().toString());
                                                        MainActivity.this.startActivity(inte);
                                                    }
                                                }
                                            }
                                    )
                                    .negativeText(R.string.btn_cancel)
                                    .show();
                            return true;
                        }
                    }
            );

            header.findViewById(R.id.discover).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Discover.class);
                    MainActivity.this.startActivity(inte);
                }
            });

            header.findViewById(R.id.prof_click).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.saved).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_SAVED, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.history).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_HISTORY, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.commented).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_COMMENT, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.submitted).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_SUBMIT, true);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra(Profile.EXTRA_PROFILE, Authentication.name);
                    inte.putExtra(Profile.EXTRA_UPVOTE, true);
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

            final String guest = getString(R.string.guest);
            keys.add(guest);
            final LinearLayout accountList = (LinearLayout) header.findViewById(R.id.accountsarea);
            for (final String accName : keys) {
                LogUtil.v(accName);
                final View t = getLayoutInflater().inflate(R.layout.account_textview_white, accountList, false);
                ((TextView) t.findViewById(R.id.name)).setText(accName);
                if (!accName.equals(guest)) {
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
                                                        if (accounts.containsKey(s) && !accounts.get(s).isEmpty()) {
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
                                                    Authentication.name = "LOGGEDOUT";
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
                } else {
                    t.findViewById(R.id.remove).setVisibility(View.GONE);
                }
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!accName.equalsIgnoreCase(Authentication.name)) {
                            LogUtil.v("Switching to " + accName);
                            if (!accName.equals(guest)) {
                                if (!accounts.get(accName).isEmpty()) {
                                    Authentication.authentication.edit().putString("lasttoken", accounts.get(accName)).remove("backedCreds").apply();
                                } else {
                                    ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                    Authentication.authentication.edit().putString("lasttoken", tokens.get(keys.indexOf(accName))).remove("backedCreds").apply();
                                }
                                Authentication.name = accName;
                            } else {
                                Authentication.name = "LOGGEDOUT";
                                Authentication.isLoggedIn = false;
                                Authentication.authentication.edit().remove("lasttoken").remove("backedCreds").apply();
                            }

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
            header.findViewById(R.id.godownsettings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout body = (LinearLayout) header.findViewById(R.id.expand_settings);
                    if (body.getVisibility() == View.GONE) {
                        expand(body);
                        flipAnimator(false, view).start();
                    } else {
                        collapse(body);
                        flipAnimator(true, view).start();
                    }
                }
            });
            header.findViewById(R.id.add).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.offline).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Reddit.appRestart.edit().putBoolean("forceoffline", true).commit();
                    Reddit.forceRestart(MainActivity.this);
                }
            });
            header.findViewById(R.id.inbox).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Inbox.class);
                    MainActivity.this.startActivityForResult(inte, INBOX_RESULT);
                }
            });


            headerMain = header;

            if (runAfterLoad == null) {
                new AsyncNotificationBadge().execute();
            }

        } else if (Authentication.didOnline) {
            header = inflater.inflate(R.layout.drawer_loggedout, drawerSubList, false);
            drawerSubList.addHeaderView(header, null, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

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

            final String guest = getString(R.string.guest);
            keys.add(guest);
            final LinearLayout accountList = (LinearLayout) header.findViewById(R.id.accountsarea);
            for (final String accName : keys) {
                LogUtil.v(accName);
                final View t = getLayoutInflater().inflate(R.layout.account_textview_white, accountList, false);
                ((TextView) t.findViewById(R.id.name)).setText(accName);
                if (!accName.equals(guest)) {
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
                                                    Authentication.name = "LOGGEDOUT";
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
                } else {
                    t.findViewById(R.id.remove).setVisibility(View.GONE);
                }
                t.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (!accName.equalsIgnoreCase(Authentication.name) && !accName.equals(guest)) {
                            if (!accounts.get(accName).isEmpty()) {
                                Authentication.authentication.edit().putString("lasttoken", accounts.get(accName)).remove("backedCreds").commit();
                            } else {
                                ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                Authentication.authentication.edit().putString("lasttoken", tokens.get(keys.indexOf(accName))).remove("backedCreds").commit();
                            }
                            Authentication.isLoggedIn = true;
                            Authentication.name = accName;
                            UserSubscriptions.switchAccounts();
                            Reddit.forceRestart(MainActivity.this, true);
                        }
                    }
                });
                accountList.addView(t);
            }


            header.findViewById(R.id.add).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.offline).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Reddit.appRestart.edit().putBoolean("forceoffline", true).commit();
                    Reddit.forceRestart(MainActivity.this);
                }
            });
            headerMain = header;

            header.findViewById(R.id.multi).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .inputRange(3, 20)
                                    .alwaysCallInputCallback()
                                    .input(
                                            getString(R.string.user_enter),
                                            null,
                                            new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    final EditText editText = dialog.getInputEditText();
                                                    EditTextValidator.validateUsername(editText);
                                                    if (input.length() >= 3 && input.length() <= 20)
                                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                }
                                            }
                                    )
                                    .positiveText(R.string.user_btn_gotomultis)
                                    .onPositive(
                                            new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    if (runAfterLoad == null) {
                                                        Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                                                        inte.putExtra(Profile.EXTRA_PROFILE, dialog.getInputEditText().getText().toString());
                                                        MainActivity.this.startActivity(inte);
                                                    }
                                                }
                                            }
                                    )
                                    .negativeText(R.string.btn_cancel)
                                    .show();
                        }
                    }
            );

        } else {
            header = inflater.inflate(R.layout.drawer_offline, drawerSubList, false);
            headerMain = header;
            drawerSubList.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);

            header.findViewById(R.id.online).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Reddit.appRestart.edit().remove("forceoffline").apply();
                    ((Reddit) getApplication()).forceRestart(MainActivity.this);
                }
            });

        }
        header.findViewById(R.id.manage).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent i = new Intent(MainActivity.this, ManageHistory.class);
                startActivity(i);
            }
        });
        if (Authentication.didOnline) {
            View support = header.findViewById(R.id.support);

            if (SettingValues.tabletUI) {
                support.setVisibility(View.GONE);
            } else {
                header.findViewById(R.id.support).setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
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
                            .input(getString(R.string.user_enter), null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    final EditText editText = dialog.getInputEditText();
                                    EditTextValidator.validateUsername(editText);
                                    if (input.length() >= 3 && input.length() <= 20)
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
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

        header.findViewById(R.id.settings).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
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

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

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
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    int current = pager.getCurrentItem();

                    if (current == toOpenComments && toOpenComments != 0) {
                        current -= 1;
                    }
                    String compare = usedArray.get(current);
                    if (compare.equals("random") || compare.equals("myrandom") || compare.equals("nsfwrandom")) {
                        if (adapter != null && adapter.getCurrentFragment() != null && ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet.subredditRandom != null) {
                            String sub = ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet.subredditRandom;
                            doSubSidebarNoLoad(sub);
                            doSubSidebar(sub);
                        }
                    } else {
                        doSubSidebar(usedArray.get(current));
                    }
                }
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Palette.getColor("alsdkfjasld"));
        accountsArea = header.findViewById(R.id.accountsarea);
        if (accountsArea != null) {
            accountsArea.setBackgroundColor(Palette.getDarkerColor("alsdkfjasld"));
        }


        setDrawerSubList();
    }

    @Override
    public void onDestroy() {
        dismissProgressDialog();
        Slide.hasStarted = false;
        super.onDestroy();
    }

    private void dismissProgressDialog() {
        if (d != null && d.isShowing()) {
            d.dismiss();
        }
    }

    public void setDrawerSubList() {
        ArrayList<String> copy = new ArrayList<>(usedArray);

        sideArrayAdapter = new SideArrayAdapter(this, copy, UserSubscriptions.getAllSubreddits(this), drawerSubList);
        drawerSubList.setAdapter(sideArrayAdapter);

        if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER
                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
            drawerSearch = ((EditText) headerMain.findViewById(R.id.sort));
            drawerSearch.setVisibility(View.VISIBLE);

            headerMain.findViewById(R.id.close_search_drawer).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerSearch.setText("");
                }
            });

            drawerSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    drawerSubList.smoothScrollToPositionFromTop(1, drawerSearch.getHeight(), 100);
                }
            });
            drawerSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                    if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                        //If it the input text doesn't match a subreddit from the list exactly, openInSubView is true
                        if (sideArrayAdapter.fitems == null || sideArrayAdapter.openInSubView || !usedArray.contains(drawerSearch.getText().toString().toLowerCase())) {
                            Intent inte = new Intent(MainActivity.this, SubredditView.class);
                            inte.putExtra(SubredditView.EXTRA_SUBREDDIT, drawerSearch.getText().toString());
                            MainActivity.this.startActivity(inte);
                        } else {
                            if (commentPager && adapter instanceof OverviewPagerAdapterComment) {
                                openingComments = null;
                                toOpenComments = -1;
                                ((MainActivity.OverviewPagerAdapterComment) adapter).size = (usedArray.size() + 1);
                                adapter.notifyDataSetChanged();
                                if (usedArray.contains(drawerSearch.getText().toString().toLowerCase())) {
                                    doPageSelectedComments(usedArray.indexOf(drawerSearch.getText().toString().toLowerCase()));
                                } else {
                                    doPageSelectedComments(usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                                }
                            }
                            if (usedArray.contains(drawerSearch.getText().toString().toLowerCase())) {
                                pager.setCurrentItem(usedArray.indexOf(drawerSearch.getText().toString().toLowerCase()));
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
                        drawerSearch.setText("");
                    }
                    return false;
                }
            });

            final View close = findViewById(R.id.close_search_drawer);
            close.setVisibility(View.GONE);

            drawerSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    final String result = editable.toString();
                    if (result.isEmpty()) {
                        close.setVisibility(View.GONE);
                    } else {
                        close.setVisibility(View.VISIBLE);
                    }
                    sideArrayAdapter.getFilter().filter(result);
                }
            });
        } else {
            if (drawerSearch != null) {
                drawerSearch.setOnClickListener(null); //remove the touch listener on the drawer search field
                drawerSearch.setVisibility(View.GONE);
            }
        }
    }

    public void resetAdapter() {
        if (UserSubscriptions.hasSubs()) {
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

                    setToolbarClick();

                    pager.setCurrentItem(usedArray.indexOf(subToDo));

                    int color = Palette.getColor(subToDo);
                    hea.setBackgroundColor(color);
                    header.setBackgroundColor(color);
                    if (accountsArea != null) {
                        accountsArea.setBackgroundColor(Palette.getDarkerColor(color));
                    }
                    themeSystemBars(subToDo);
                    setRecentBar(subToDo);
                }
            });
        }
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
        } else if ((SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH)
                && findViewById(R.id.toolbar_search).getVisibility() == View.VISIBLE) {
            findViewById(R.id.close_search_toolbar).performClick(); //close GO_TO_SUB_FIELD
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

        this.menu = menu;
        /**
         * Hide the "Submit" and "Sidebar" menu items if the currently viewed sub is a multi,
         * domain, the frontpage, or /r/all. If the subreddit has a "." in it, we know it's a domain because
         * subreddits aren't allowed to have hard-stops in the name.
         */
        if (Authentication.didOnline && usedArray != null) {
            final String subreddit = usedArray.get(pager.getCurrentItem());

            if (subreddit.contains("/m/") || subreddit.contains(".") || subreddit.contains("+")
                    || subreddit.equals("frontpage") || subreddit.equals("all")) {
                if (menu.findItem(R.id.submit) != null)
                    menu.findItem(R.id.submit).setVisible(false);
                if (menu.findItem(R.id.sidebar) != null)
                    menu.findItem(R.id.sidebar).setVisible(false);
            } else {
                if (menu.findItem(R.id.submit) != null)
                    menu.findItem(R.id.submit).setVisible(true);
                if (menu.findItem(R.id.sidebar) != null)
                    menu.findItem(R.id.sidebar).setVisible(true);
            }

            menu.findItem(R.id.theme).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
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
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (NetworkUtil.isConnected(this)) {
            if (SettingValues.expandedToolbar) {
                inflater.inflate(R.menu.menu_subreddit_overview_expanded, menu);
            } else {
                inflater.inflate(R.menu.menu_subreddit_overview, menu);
            }
            //Only show the "Share Slide" menu item if the user doesn't have Pro installed
            if (SettingValues.tabletUI) {
                menu.findItem(R.id.share).setVisible(false);
            }
            if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_DISMISS) {
                menu.findItem(R.id.hide_posts).setVisible(false);
            }
        } else {
            inflater.inflate(R.menu.menu_subreddit_overview_offline, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String subreddit = usedArray.get(Reddit.currentPosition);

        switch (item.getItemId()) {
            case R.id.filter:
                filterContent(shouldLoad);
                return true;
            case R.id.sidebar:
                if (!subreddit.equals("all") && !subreddit.equals("frontpage") && !subreddit.contains(".") && !subreddit.contains("+") && !subreddit.contains(".") && !subreddit.contains("/m/")) {
                    drawerLayout.openDrawer(GravityCompat.END);
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
                                restartTheme();
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
                                restartTheme();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.sepia).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] names = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_");
                        String name = names[names.length - 1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 5) {
                                Reddit.themeBack = theme.getThemeType();
                                new ColorPreferences(MainActivity.this).setFontStyle(theme);
                                d.dismiss();
                                restartTheme();
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
                                restartTheme();
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
                                restartTheme();
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
                                restartTheme();
                                break;
                            }
                        }
                    }
                });
            }
            return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null) {
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                }
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
                if (!subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("all") && !subreddit.contains(".") && !subreddit.contains("/m/") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("random") && !subreddit.equalsIgnoreCase("myrandom") && !subreddit.equalsIgnoreCase("nsfwrandom")) {
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
            case R.id.hide_posts:
                ((SubmissionsView) adapter.getCurrentFragment()).clearSeenPosts(false);
                return true;
            case R.id.share:
                Reddit.defaultShareText("Slide for Reddit", "https://play.google.com/store/apps/details?id=me.ccrama.redditslide", MainActivity.this);
                return true;
            case R.id.submit: {
                Intent i = new Intent(MainActivity.this, Submit.class);
                if ((!subreddit.contains("/m/") || !subreddit.contains(".")) && canSubmit) {
                    i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                }
                startActivity(i);
            }
            return true;
            case R.id.gallery:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Gallery.class);
                        i2.putExtra("offline", ((SubmissionsView) adapter.getCurrentFragment()).posts.cached != null ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time : 0L);
                        i2.putExtra(Gallery.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this)
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
                            }).setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton("Preview (" + SettingValues.previews + ")", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingValues.prefs.edit().putInt(SettingValues.PREVIEWS_LEFT, SettingValues.previews - 1).apply();
                                SettingValues.previews = SettingValues.prefs.getInt(SettingValues.PREVIEWS_LEFT, 10);
                                List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                                if (posts != null && !posts.isEmpty()) {
                                    Intent i2 = new Intent(MainActivity.this, Gallery.class);
                                    i2.putExtra("offline", ((SubmissionsView) adapter.getCurrentFragment()).posts.cached != null ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time : 0L);
                                    i2.putExtra(Gallery.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
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
                    List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra("offline", ((SubmissionsView) adapter.getCurrentFragment()).posts.cached != null ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time : 0L);
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(this)
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
                            });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton("Preview (" + SettingValues.previews + ")", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingValues.prefs.edit().putInt(SettingValues.PREVIEWS_LEFT, SettingValues.previews - 1).apply();
                                SettingValues.previews = SettingValues.prefs.getInt(SettingValues.PREVIEWS_LEFT, 10);
                                List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                                if (posts != null && !posts.isEmpty()) {
                                    Intent i2 = new Intent(MainActivity.this, Shadowbox.class);
                                    i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                                    i2.putExtra("offline", ((SubmissionsView) adapter.getCurrentFragment()).posts.cached != null ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time : 0L);
                                    i2.putExtra(Shadowbox.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                                    startActivity(i2);
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

    public void filterContent(final String subreddit) {
        final boolean[] chosen = new boolean[]{
                PostMatch.isImage(subreddit.toLowerCase()),
                PostMatch.isAlbums(subreddit.toLowerCase()),
                PostMatch.isGif(subreddit.toLowerCase()),
                PostMatch.isVideo(subreddit.toLowerCase()),
                PostMatch.isUrls(subreddit.toLowerCase()),
                PostMatch.isSelftext(subreddit.toLowerCase()),
                PostMatch.isNsfw(subreddit.toLowerCase())
        };

        final String currentSubredditName = usedArray.get(Reddit.currentPosition);

        //Title of the filter dialog
        String filterTitle;
        if (currentSubredditName.contains("/m/")) {
            filterTitle = getString(R.string.content_to_hide, currentSubredditName);
        } else {
            if (currentSubredditName.equals("frontpage")) {
                filterTitle = getString(R.string.content_to_hide, "frontpage");
            } else {
                filterTitle = getString(R.string.content_to_hide, "/r/" + currentSubredditName);
            }
        }

        new AlertDialogWrapper.Builder(this)
                .setTitle(filterTitle)
                .alwaysCallMultiChoiceCallback()
                .setMultiChoiceItems(new String[]{
                        getString(R.string.image_downloads),
                        getString(R.string.type_albums),
                        getString(R.string.type_gifs),
                        getString(R.string.type_videos),
                        getString(R.string.type_links),
                        getString(R.string.type_selftext),
                        getString(R.string.type_nsfw_content)
                }, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                }).setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                PostMatch.setChosen(chosen, subreddit);
                reloadSubs();
            }
        }).setNegativeButton(R.string.btn_cancel, null).show();
    }

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
                caching = new CommentCacheAsync(submissions, MainActivity.this, subreddit).execute();
            }

        }).show();


    }

    @Override
    public void onResume() {
        super.onResume();
        if (Authentication.isLoggedIn && Authentication.didOnline && NetworkUtil.isConnected(MainActivity.this) && headerMain != null && runAfterLoad == null) {
            new AsyncNotificationBadge().execute();
        }

        if (pager != null && commentPager) {
            if (pager.getCurrentItem() != toOpenComments && shouldLoad != null) {
                if (usedArray != null && !shouldLoad.contains("+") && usedArray.indexOf(shouldLoad) != pager.getCurrentItem())
                    pager.setCurrentItem(toOpenComments - 1);
            }
        }

        Reddit.setDefaultErrorHandler(this);

        if (sideArrayAdapter != null) {
            sideArrayAdapter.updateHistory(UserSubscriptions.getHistory());
        }

        if (datasetChanged && UserSubscriptions.hasSubs() && !usedArray.isEmpty()) {
            usedArray = new ArrayList<>(UserSubscriptions.getSubscriptions(this));
            adapter.notifyDataSetChanged();
            sideArrayAdapter.notifyDataSetChanged();
            datasetChanged = false;
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(pager.getCurrentItem());
            }
            setToolbarClick();
        }
        //Only refresh the view if a Setting was altered
        if (Settings.changed || SettingsTheme.changed || (NetworkUtil.isConnected(this) && usedArray != null
                && usedArray.size() != UserSubscriptions.getSubscriptions(this).size())) {

            int current = pager.getCurrentItem();
            if (commentPager && current == currentComment) {
                current = current - 1;
            }
            if (current < 0) {
                current = 0;
            }
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

            //Need to change the subreddit search method
            if (SettingsGeneral.searchChanged) {
                setDrawerSubList();

                if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                    mToolbar.setOnLongClickListener(null); //remove the long click listener from the toolbar
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                    setupSubredditSearchToolbar();
                } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                    setupSubredditSearchToolbar();
                    setDrawerSubList();
                }
                SettingsGeneral.searchChanged = false;
            }
            SettingsTheme.changed = false;
            Settings.changed = false;
            setToolbarClick();
        }
    }

    public void doFriends(final List<String> friends) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (friends != null && !friends.isEmpty() && headerMain.findViewById(R.id.friends) != null) {
                    headerMain.findViewById(R.id.friends).setVisibility(View.VISIBLE);
                    headerMain.findViewById(R.id.friends).setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View view) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title("Friends")
                                    .items(friends)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                            Intent i = new Intent(MainActivity.this, Profile.class);
                                            i.putExtra(Profile.EXTRA_PROFILE, friends.get(which));
                                            startActivity(i);
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    });
                } else if (Authentication.isLoggedIn && headerMain.findViewById(R.id.friends) != null) {
                    headerMain.findViewById(R.id.friends).setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (pager != null && SettingValues.commentPager && pager.getCurrentItem() == toOpenComments && SettingValues.commentVolumeNav && pager.getAdapter() instanceof OverviewPagerAdapterComment) {
            int keyCode = event.getKeyCode();
            if (SettingValues.commentVolumeNav) {
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

    /**
     * If the user has the Subreddit Search method set to "long press on toolbar title",
     * an OnLongClickListener needs to be set for the toolbar as well as handling all of the relevant
     * onClicks for the views of the search bar.
     */
    private void setupSubredditSearchToolbar() {
        if ((SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) && usedArray != null && !usedArray.isEmpty()) {
            if (findViewById(R.id.drawer_divider) != null) {
                if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.drawer_divider).setVisibility(View.VISIBLE);
                }
            }
            final ListView TOOLBAR_SEARCH_SUGGEST_LIST = (ListView) findViewById(R.id.toolbar_search_suggestions_list);
            final ArrayList<String> subs_copy = new ArrayList<>(usedArray);
            final SideArrayAdapter TOOLBAR_SEARCH_SUGGEST_ADAPTER
                    = new SideArrayAdapter(this, subs_copy, UserSubscriptions.getAllSubreddits(this), TOOLBAR_SEARCH_SUGGEST_LIST);

            if (TOOLBAR_SEARCH_SUGGEST_LIST != null) {
                TOOLBAR_SEARCH_SUGGEST_LIST.setAdapter(TOOLBAR_SEARCH_SUGGEST_ADAPTER);
            }

            if (mToolbar != null) {
                mToolbar.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AutoCompleteTextView GO_TO_SUB_FIELD = (AutoCompleteTextView) findViewById(R.id.toolbar_search);
                        final ImageView CLOSE_BUTTON = (ImageView) findViewById(R.id.close_search_toolbar);
                        final CardView SUGGESTIONS_BACKGROUND = (CardView) findViewById(R.id.toolbar_search_suggestions);

                        //if the view mode is set to Subreddit Tabs, save the title ("Slide" or "Slide (debug)")
                        tabViewModeTitle = (!SettingValues.single) ? getSupportActionBar().getTitle().toString() : null;

                        getSupportActionBar().setTitle(""); //clear title to make room for search field

                        if (GO_TO_SUB_FIELD != null && CLOSE_BUTTON != null && SUGGESTIONS_BACKGROUND != null) {
                            GO_TO_SUB_FIELD.setVisibility(View.VISIBLE);
                            CLOSE_BUTTON.setVisibility(View.VISIBLE);
                            SUGGESTIONS_BACKGROUND.setVisibility(View.VISIBLE);

                            //run enter animations
                            enterAnimationsForToolbarSearch(ANIMATE_DURATION, SUGGESTIONS_BACKGROUND,
                                    GO_TO_SUB_FIELD, CLOSE_BUTTON);

                            //Get focus of the search field and show the keyboard
                            GO_TO_SUB_FIELD.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


                            //Close the search UI and keyboard when clicking the close button
                            CLOSE_BUTTON.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final View view = MainActivity.this.getCurrentFocus();
                                    if (view != null) {
                                        //Hide the keyboard
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    //run the exit animations
                                    exitAnimationsForToolbarSearch(ANIMATE_DURATION,
                                            SUGGESTIONS_BACKGROUND, GO_TO_SUB_FIELD, CLOSE_BUTTON);
                                }
                            });

                            GO_TO_SUB_FIELD.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                                    if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                                        //If it the input text doesn't match a subreddit from the list exactly, openInSubView is true
                                        if (sideArrayAdapter.fitems == null || sideArrayAdapter.openInSubView || !usedArray.contains(GO_TO_SUB_FIELD.getText().toString().toLowerCase())) {
                                            Intent intent = new Intent(MainActivity.this, SubredditView.class);
                                            intent.putExtra(SubredditView.EXTRA_SUBREDDIT, GO_TO_SUB_FIELD.getText().toString());
                                            MainActivity.this.startActivity(intent);
                                        } else {
                                            if (commentPager && adapter instanceof OverviewPagerAdapterComment) {
                                                openingComments = null;
                                                toOpenComments = -1;
                                                ((OverviewPagerAdapterComment) adapter).size = (usedArray.size() + 1);
                                                adapter.notifyDataSetChanged();

                                                if (usedArray.contains(GO_TO_SUB_FIELD.getText().toString().toLowerCase())) {
                                                    doPageSelectedComments(usedArray.indexOf(GO_TO_SUB_FIELD.getText().toString().toLowerCase()));
                                                } else {
                                                    doPageSelectedComments(usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                                                }
                                            }
                                            if (usedArray.contains(GO_TO_SUB_FIELD.getText().toString().toLowerCase())) {
                                                pager.setCurrentItem(usedArray.indexOf(GO_TO_SUB_FIELD.getText().toString().toLowerCase()));
                                            } else {
                                                pager.setCurrentItem(usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                                            }
                                        }

                                        View view = MainActivity.this.getCurrentFocus();
                                        if (view != null) {
                                            //Hide the keyboard
                                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }

                                        SUGGESTIONS_BACKGROUND.setVisibility(View.GONE);
                                        GO_TO_SUB_FIELD.setVisibility(View.GONE);
                                        CLOSE_BUTTON.setVisibility(View.GONE);

                                        GO_TO_SUB_FIELD.setText(""); //clear text from search field

                                        if (SettingValues.single) {
                                            getSupportActionBar().setTitle(selectedSub);
                                        } else {
                                            //Set the title back to "Slide" or "Slide (debug)"
                                            getSupportActionBar().setTitle(tabViewModeTitle);
                                        }
                                    }
                                    return false;
                                }
                            });

                            GO_TO_SUB_FIELD.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                                }

                                @Override
                                public void afterTextChanged(Editable editable) {
                                    final String RESULT = GO_TO_SUB_FIELD.getText().toString().replaceAll(" ", "");
                                    TOOLBAR_SEARCH_SUGGEST_ADAPTER.getFilter().filter(RESULT);
                                }
                            });
                        }
                        return true;
                    }
                });
            }
        }
    }

    /**
     * Starts the enter animations for various UI components of the toolbar subreddit search
     *
     * @param ANIMATION_DURATION     duration of the animation in ms
     * @param SUGGESTIONS_BACKGROUND background of subreddit suggestions list
     * @param GO_TO_SUB_FIELD        search field in toolbar
     * @param CLOSE_BUTTON           button that clears the search and closes the search UI
     */
    public void enterAnimationsForToolbarSearch(final long ANIMATION_DURATION,
                                                final CardView SUGGESTIONS_BACKGROUND,
                                                final AutoCompleteTextView GO_TO_SUB_FIELD,
                                                final ImageView CLOSE_BUTTON) {
        SUGGESTIONS_BACKGROUND.animate()
                .translationY(headerHeight)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION + ANIMATE_DURATION_OFFSET)
                .start();

        GO_TO_SUB_FIELD.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

        CLOSE_BUTTON.animate()
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();
    }

    /**
     * Starts the exit animations for various UI components of the toolbar subreddit search
     *
     * @param ANIMATION_DURATION     duration of the animation in ms
     * @param SUGGESTIONS_BACKGROUND background of subreddit suggestions list
     * @param GO_TO_SUB_FIELD        search field in toolbar
     * @param CLOSE_BUTTON           button that clears the search and closes the search UI
     */
    public void exitAnimationsForToolbarSearch(final long ANIMATION_DURATION,
                                               final CardView SUGGESTIONS_BACKGROUND,
                                               final AutoCompleteTextView GO_TO_SUB_FIELD,
                                               final ImageView CLOSE_BUTTON) {
        SUGGESTIONS_BACKGROUND.animate()
                .translationY(-SUGGESTIONS_BACKGROUND.getHeight())
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION + ANIMATE_DURATION_OFFSET)
                .start();

        GO_TO_SUB_FIELD.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

        CLOSE_BUTTON.animate()
                .alpha(0f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIMATION_DURATION)
                .start();

        //Helps smooth the transition between the toolbar title being reset and the search elements
        //fading out.
        final long OFFSET_ANIM = (ANIMATION_DURATION == 0) ? 0 : ANIMATE_DURATION_OFFSET;

        //Hide the various UI components after the animations are complete and
        //reset the toolbar title
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SUGGESTIONS_BACKGROUND.setVisibility(View.GONE);
                GO_TO_SUB_FIELD.setVisibility(View.GONE);
                CLOSE_BUTTON.setVisibility(View.GONE);

                GO_TO_SUB_FIELD.setText(""); //clear text from search field

                if (SettingValues.single) {
                    getSupportActionBar().setTitle(selectedSub);
                } else {
                    getSupportActionBar().setTitle(tabViewModeTitle);
                }
            }
        }, ANIMATION_DURATION + ANIMATE_DURATION_OFFSET);
    }

    public void doPageSelectedComments(int position) {

        pager.setSwipeLeftOnly(false);

        header.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);


        Reddit.currentPosition = position;
        if (position + 1 != currentComment) {
            doSubSidebarNoLoad(usedArray.get(position));
        }
        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
        if (page != null && page.adapter != null) {
            SubredditPosts p = page.adapter.dataSet;
            if (p.offline && p.cached != null) {
                Toast.makeText(MainActivity.this, getString(R.string.offline_last_update, TimeUtils.getTimeAgo(p.cached.time, MainActivity.this)), Toast.LENGTH_LONG).show();
            }
        }

        if (hea != null) {
            hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
            if (accountsArea != null)
                accountsArea.setBackgroundColor(Palette.getDarkerColor(usedArray.get(position)));
        }
        header.setBackgroundColor(Palette.getColor(usedArray.get(position)));

        themeSystemBars(usedArray.get(position));
        setRecentBar(usedArray.get(position));

        if (SettingValues.single) {
            getSupportActionBar().setTitle(usedArray.get(position));
        } else {
            mTabLayout.setSelectedTabIndicatorColor(
                    new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));
        }

        selectedSub = usedArray.get(position);
    }

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position = ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition() - 1;
        } else if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((CatchStaggeredGridLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position = ((PreCachingLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition() - 1;
        }
        return position;
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
        private SubmissionsView mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        header.animate()
                                .translationY(0)
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(180);
                        doSubSidebarNoLoad(usedArray.get(position));

                        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
                        if (page != null && page.adapter != null) {
                            SubredditPosts p = page.adapter.dataSet;
                            if (p.offline) {
                                p.doMainActivityOffline(p.displayer);
                            }
                        }
                    }
                }

                @Override
                public void onPageSelected(final int position) {
                    Reddit.currentPosition = position;
                    selectedSub = usedArray.get(position);

                    if (hea != null) {
                        hea.setBackgroundColor(Palette.getColor(selectedSub));
                        if (accountsArea != null)
                            accountsArea.setBackgroundColor(Palette.getDarkerColor(selectedSub));
                    }
                    header.setBackgroundColor(Palette.getColor(selectedSub));

                    themeSystemBars(selectedSub);
                    setRecentBar(selectedSub);

                    if (SettingValues.single || mTabLayout == null) {
                        //Smooth out the fading animation for the toolbar subreddit search UI
                        if ((SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                || SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH)
                                && findViewById(R.id.toolbar_search).getVisibility() == View.VISIBLE) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getSupportActionBar().setTitle(selectedSub);
                                }
                            }, ANIMATE_DURATION + ANIMATE_DURATION_OFFSET);
                        } else {
                            getSupportActionBar().setTitle(selectedSub);
                        }
                    } else {
                        mTabLayout.setSelectedTabIndicatorColor(
                                new ColorPreferences(MainActivity.this)
                                        .getColor(selectedSub));
                    }
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

        public void doSetPrimary(Object object, int position) {
            if (object != null && getCurrentFragment() != object && position != toOpenComments && object instanceof SubmissionsView) {
                shouldLoad = usedArray.get(position);
                if (multiNameToSubsMap.containsKey(usedArray.get(position))) {
                    shouldLoad = multiNameToSubsMap.get(usedArray.get(position));
                } else {
                    shouldLoad = usedArray.get(position);
                }

                mCurrentFragment = ((SubmissionsView) object);
                if (mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

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
            String name;
            if (multiNameToSubsMap.containsKey(usedArray.get(i))) {
                name = multiNameToSubsMap.get(usedArray.get(i));
            } else {
                name = usedArray.get(i);
            }
            args.putString("id", name);
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
                return abbreviate(usedArray.get(position), 25);
            } else {
                return "";
            }


        }
    }

    public class OverviewPagerAdapterComment extends OverviewPagerAdapter {
        public int size = usedArray.size();
        public Fragment storedFragment;
        private SubmissionsView mCurrentFragment;
        private CommentPage mCurrentComments;

        public OverviewPagerAdapterComment(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        if (position != toOpenComments) {
                            header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
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
                                        .translationY(-header.getHeight() * 1.5f)
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

        @Override
        public Parcelable saveState() {
            return null;
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void doSetPrimary(Object object, int position) {
            if (position != toOpenComments) {
                if (multiNameToSubsMap.containsKey(usedArray.get(position))) {
                    shouldLoad = multiNameToSubsMap.get(usedArray.get(position));
                } else {
                    shouldLoad = usedArray.get(position);
                }

                if (getCurrentFragment() != object) {
                    mCurrentFragment = ((SubmissionsView) object);
                    if (mCurrentFragment != null && mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }
                }
            } else if (object instanceof CommentPage) {
                mCurrentComments = (CommentPage) object;
            }

        }

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
                if (usedArray.size() > i) {
                    if (multiNameToSubsMap.containsKey(usedArray.get(i))) {
                        //if (usedArray.get(i).co
                        args.putString("id", multiNameToSubsMap.get(usedArray.get(i)));
                    } else {
                        args.putString("id", usedArray.get(i));
                    }
                }
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
                return abbreviate(usedArray.get(position), 25);
            } else {
                return "";
            }


        }
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
                    if (Reddit.cachedData.contains("toCache")) {
                        Reddit.autoCache = new AutoCacheScheduler(MainActivity.this);
                        Reddit.autoCache.start(getApplicationContext());
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
                UserSubscriptions.doFriendsOfMain(MainActivity.this);

            } catch (Exception e) {
                Log.w(LogUtil.getTag(), "Cannot fetch inbox count");
                count = -1;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (Authentication.mod && Authentication.didOnline) {
                headerMain.findViewById(R.id.mod).setVisibility(View.VISIBLE);
                headerMain.findViewById(R.id.mod).setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        if (UserSubscriptions.modOf != null && !UserSubscriptions.modOf.isEmpty()) {
                            Intent inte = new Intent(MainActivity.this, ModQueue.class);
                            MainActivity.this.startActivity(inte);
                        }
                    }
                });
            }
            if (count != -1) {
                int oldCount = Reddit.appRestart.getInt("inbox", 0);
                if (count > oldCount) {
                    final Snackbar s = Snackbar.make(mToolbar, getResources().getQuantityString(R.plurals.new_messages, count - oldCount, count - oldCount), Snackbar.LENGTH_LONG).setAction(R.string.btn_view, new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            Intent i = new Intent(MainActivity.this, Inbox.class);
                            i.putExtra(Inbox.EXTRA_UNREAD, true);
                            startActivity(i);
                        }
                    });

                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                }
                Reddit.appRestart.edit().putInt("inbox", count).apply();
            }
            View badge = headerMain.findViewById(R.id.count);
            if (count == 0) {
                if (badge != null) {
                    badge.setVisibility(View.GONE);
                }
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
            } else if (count != -1) {
                if (badge != null) {
                    badge.setVisibility(View.VISIBLE);
                }
                ((TextView) headerMain.findViewById(R.id.count)).setText(String.format(Locale.getDefault(), "%d", count));
            }

            /* Todo possibly
            View modBadge = headerMain.findViewById(R.id.count_mod);

            if (modCount == 0) {
                if (modBadge != null) modBadge.setVisibility(View.GONE);
            } else if (modCount != -1) {
                if (modBadge != null) modBadge.setVisibility(View.VISIBLE);
                ((TextView) headerMain.findViewById(R.id.count)).setText(String.format(Locale.getDefault(), "%d", count));
            }*/
        }
    }
}
