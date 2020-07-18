package me.ccrama.redditslide.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
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
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.lusfold.androidkeyvaluestore.KVStore;
import com.lusfold.androidkeyvaluestore.core.KVManger;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.CommentCacheAsync;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.DrawerItemsDialog;
import me.ccrama.redditslide.Fragments.SettingsGeneralFragment;
import me.ccrama.redditslide.Fragments.SettingsThemeFragment;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.ImageFlairs;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.OpenRedditLink;
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
import me.ccrama.redditslide.util.NetworkStateReceiver;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SortingUtil;
import me.ccrama.redditslide.util.SubmissionParser;

import static me.ccrama.redditslide.UserSubscriptions.modOf;


public class MainActivity extends BaseActivity
        implements NetworkStateReceiver.NetworkStateReceiverListener {
    public static final String EXTRA_PAGE_TO        = "pageTo";
    public static final String IS_ONLINE            = "online";
    // Instance state keys
    static final        String SUBS                 = "subscriptions";
    static final        String LOGGED_IN            = "loggedIn";
    static final        String USERNAME             = "username";
    static final        int    TUTORIAL_RESULT      = 55;
    static final        int    INBOX_RESULT         = 66;
    static final        int    RESET_ADAPTER_RESULT = 3;
    static final        int    SETTINGS_RESULT      = 2;
    public static Loader  loader;
    public static boolean datasetChanged;
    public static Map<String, String> multiNameToSubsMap = new HashMap<>();
    public static boolean checkedPopups;
    public static String  shouldLoad;
    public static boolean isRestart;
    public static int     restartPage;
    public final  long ANIMATE_DURATION        = 250; //duration of animations
    private final long ANIMATE_DURATION_OFFSET = 45; //offset for smoothing out the exit animations
    public boolean                  singleMode;
    public ToggleSwipeViewPager     pager;
    public CaseInsensitiveArrayList usedArray;
    public DrawerLayout             drawerLayout;
    public View                     hea;
    public EditText                 drawerSearch;
    public View                     header;
    public String                   subToDo;
    public OverviewPagerAdapter     adapter;
    public int     toGoto = 0;
    public boolean first  = true;
    public TabLayout mTabLayout;
    public ListView  drawerSubList;
    public String    selectedSub; //currently selected subreddit
    public Runnable  doImage;
    public Intent    data;
    public boolean commentPager = false;
    public Runnable   runAfterLoad;
    public boolean    canSubmit;
    //if the view mode is set to Subreddit Tabs, save the title ("Slide" or "Slide (debug)")
    public String     tabViewModeTitle;
    public int        currentComment;
    public Submission openingComments;
    public int toOpenComments = -1;
    public boolean inNightMode;
    boolean                     changed;
    String                      term;
    View                        headerMain;
    MaterialDialog              d;
    AsyncTask<View, Void, View> currentFlair;
    SpoilerRobotoTextView       sidebarBody;
    CommentOverflow             sidebarOverflow;
    View                        accountsArea;
    SideArrayAdapter            sideArrayAdapter;
    Menu                        menu;
    AsyncTask                   caching;
    boolean                     currentlySubbed;
    int                         back;
    private AsyncGetSubreddit mAsyncGetSubreddit = null;
    private int headerHeight; //height of the header
    public int reloadItemNumber = -2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_RESULT) {
            int current = pager.getCurrentItem();
            if (commentPager && current == currentComment) {
                current = current - 1;
            }
            if (current < 0) current = 0;
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(current);
            }
            setToolbarClick();
        } else if ((requestCode == 2001 || requestCode == 2002) && resultCode == RESULT_OK) {
            if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER
                    || SettingValues.subredditSearchMethod
                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                drawerLayout.closeDrawers();
                drawerSearch.setText("");
            }

            //clear the text from the toolbar search field
            if (findViewById(R.id.toolbar_search) != null) {
                ((AutoCompleteTextView) findViewById(R.id.toolbar_search)).setText("");
            }

            View view = MainActivity.this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else if (requestCode == 2002 && resultCode != RESULT_OK) {
            mToolbar.performLongClick(); //search was init from the toolbar, so return focus to the toolbar
        } else if (requestCode == 423 && resultCode == RESULT_OK) {
            ((OverviewPagerAdapterComment) adapter).mCurrentComments.doResult(data);
        } else if (requestCode == 940) {
            if (adapter != null && adapter.getCurrentFragment() != null) {
                if (resultCode == RESULT_OK) {
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
        } else if (requestCode == RESET_ADAPTER_RESULT) {
            resetAdapter();
            setDrawerSubList();
        } else if (requestCode == TUTORIAL_RESULT) {
            UserSubscriptions.doMainActivitySubs(this);
        } else if (requestCode == INBOX_RESULT) {
            //update notification badge
            new AsyncNotificationBadge().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if (requestCode == 3333) {
            this.data = data;
            if (doImage != null) {
                Handler handler = new Handler();
                handler.post(doImage);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
      /* todo  if(resultCode == 4 && UserSubscriptions.hasChanged){
            UserSubscriptions.hasChanged = false;
            sideArrayAdapter.setSideItems(UserSubscriptions.getAllSubreddits(this));
            sideArrayAdapter.notifyDataSetChanged();
        }*/
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)
                || drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (commentPager && pager.getCurrentItem() == toOpenComments) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        } else if ((SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                && findViewById(R.id.toolbar_search).getVisibility() == View.VISIBLE) {
            findViewById(R.id.close_search_toolbar).performClick(); //close GO_TO_SUB_FIELD
        } else if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.OpenDrawer.getValue()) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.GotoFirst.getValue()) {
            pager.setCurrentItem(0);
        } else if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.ConfirmExit.getValue()) {
            final AlertDialogWrapper.Builder builder =
                    new AlertDialogWrapper.Builder(MainActivity.this);
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
    public void onPause() {
        super.onPause();
        changed = false;
        if (!SettingValues.synccitName.isEmpty()) {
            new MySynccitUpdateTask().execute(
                    SynccitRead.newVisited.toArray(new String[0]));
        }
        if (Authentication.isLoggedIn
                && Authentication.me != null
                //This is causing a crash, might not be important since the storeVisits will just not do anything without gold && Authentication.me.hasGold()
                && !SynccitRead.newVisited.isEmpty()) {
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
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //Upon leaving MainActivity--hide the toolbar search if it is visible
        if (findViewById(R.id.toolbar_search).getVisibility() == View.VISIBLE) {
            findViewById(R.id.close_search_toolbar).performClick();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
            int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(
                                R.string.err_permission)
                                .setMessage(R.string.err_permission_msg)
                                .setPositiveButton(R.string.btn_yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                        int which) {
                                                ActivityCompat.requestPermissions(
                                                        MainActivity.this, new String[]{
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                        }, 1);

                                            }
                                        })
                                .setNegativeButton(R.string.btn_no,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                        int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                .show();
                    }
                });

            }

            // other 'case' lines to check for other
            // permissions this app might request
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList(SUBS, usedArray);
        savedInstanceState.putBoolean(LOGGED_IN, Authentication.isLoggedIn);
        savedInstanceState.putBoolean(IS_ONLINE, Authentication.didOnline);
        savedInstanceState.putString(USERNAME, Authentication.name);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (pager != null
                && SettingValues.commentPager
                && pager.getCurrentItem() == toOpenComments
                && SettingValues.commentVolumeNav
                && pager.getAdapter() instanceof OverviewPagerAdapterComment) {
            if (SettingValues.commentVolumeNav) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        return ((OverviewPagerAdapterComment) pager.getAdapter()).mCurrentComments.onKeyDown(
                                keyCode, event);
                    default:
                        return super.dispatchKeyEvent(event);
                }
            } else {
                return super.dispatchKeyEvent(event);
            }
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event);
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return onKeyDown(keyCode, event);
        }
        return super.dispatchKeyEvent(event);
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
            // Hide the "Share Slide" menu if the user has Pro installed
            if (SettingValues.isPro) {
                menu.findItem(R.id.share).setVisible(false);
            }
            if (SettingValues.fab && SettingValues.fabType == Constants.FAB_DISMISS) {
                menu.findItem(R.id.hide_posts).setVisible(false);
            }
        } else {
            inflater.inflate(R.menu.menu_subreddit_overview_offline, menu);
        }
        return true;
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

            if (subreddit.contains("/m/")
                    || subreddit.contains(".")
                    || subreddit.contains("+")
                    || subreddit.equals("frontpage")
                    || subreddit.equals("all")) {
                if (menu.findItem(R.id.submit) != null) {
                    menu.findItem(R.id.submit).setVisible(false);
                }
                if (menu.findItem(R.id.sidebar) != null) {
                    menu.findItem(R.id.sidebar).setVisible(false);
                }
            } else {
                if (menu.findItem(R.id.submit) != null) {
                    menu.findItem(R.id.submit).setVisible(true);
                }
                if (menu.findItem(R.id.sidebar) != null) {
                    menu.findItem(R.id.sidebar).setVisible(true);
                }
            }

            menu.findItem(R.id.theme)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(
                                    subreddit);
                            final Context contextThemeWrapper =
                                    new ContextThemeWrapper(MainActivity.this, style);
                            LayoutInflater localInflater =
                                    getLayoutInflater().cloneInContext(contextThemeWrapper);
                            final View dialoglayout =
                                    localInflater.inflate(R.layout.colorsub, null);
                            ArrayList<String> arrayList = new ArrayList<>();
                            arrayList.add(subreddit);
                            SettingsSubAdapter.showSubThemeEditor(arrayList, MainActivity.this,
                                    dialoglayout);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        final String subreddit = usedArray.get(Reddit.currentPosition);

        switch (item.getItemId()) {
            case R.id.filter:
                filterContent(shouldLoad);
                return true;
            case R.id.sidebar:
                if (!subreddit.equals("all")
                        && !subreddit.equals("frontpage")
                        && !subreddit.contains(".")
                        && !subreddit.contains("+")
                        && !subreddit.contains(".")
                        && !subreddit.contains("/m/")) {
                    drawerLayout.openDrawer(GravityCompat.END);
                } else {
                    Toast.makeText(this, R.string.sidebar_notfound, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.night: {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                AlertDialogWrapper.Builder builder =
                        new AlertDialogWrapper.Builder(MainActivity.this);
                final TextView title = dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());

                builder.setView(dialoglayout);
                final Dialog d = builder.show();
                back = new ColorPreferences(MainActivity.this).getFontStyle().getThemeType();
                if (SettingValues.isNight()) {
                    dialoglayout.findViewById(R.id.nightmsg).setVisibility(View.VISIBLE);
                }

                for (final Pair<Integer, Integer> pair : ColorPreferences.themePairList) {
                    dialoglayout.findViewById(pair.first)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String[] names =
                                            new ColorPreferences(MainActivity.this).getFontStyle()
                                                    .getTitle()
                                                    .split("_");
                                    String name = names[names.length - 1];
                                    final String newName = name.replace("(", "");
                                    for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                        if (theme.toString().contains(newName)
                                                && theme.getThemeType() == pair.second) {
                                            back = theme.getThemeType();
                                            new ColorPreferences(MainActivity.this).setFontStyle(
                                                    theme);
                                            d.dismiss();
                                            restartTheme();
                                            break;
                                        }
                                    }
                                }
                            });
                }
            }
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
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    openPopup();
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
                                        });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage")
                        && !subreddit.equalsIgnoreCase("all")
                        && !subreddit.contains(".")
                        && !subreddit.contains("/m/")
                        && !subreddit.equalsIgnoreCase("friends")
                        && !subreddit.equalsIgnoreCase("random")
                        && !subreddit.equalsIgnoreCase("popular")
                        && !subreddit.equalsIgnoreCase("myrandom")
                        && !subreddit.equalsIgnoreCase("randnsfw")) {
                    builder.positiveText(getString(R.string.search_subreddit, subreddit))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(),
                                            "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                    builder.neutralText(R.string.search_all)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    startActivity(i);
                                }
                            });
                } else {
                    builder.positiveText(R.string.search_all)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(MainActivity.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();
                return true;
            case R.id.save:
                saveOffline(((SubmissionsView) adapter.getCurrentFragment()).posts.posts,
                        ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                return true;
            case R.id.hide_posts:
                ((SubmissionsView) adapter.getCurrentFragment()).clearSeenPosts(false);
                return true;
            case R.id.share:
                Reddit.defaultShareText("Slide for Reddit",
                        "https://play.google.com/store/apps/details?id=me.ccrama.redditslide",
                        MainActivity.this);
                return true;
            case R.id.submit: {
                Intent i = new Intent(MainActivity.this, Submit.class);
                i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                startActivity(i);
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
                                                ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                                        if (posts != null && !posts.isEmpty()) {
                                            Intent i2 =
                                                    new Intent(MainActivity.this, Gallery.class);
                                            i2.putExtra("offline",
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                                            != null
                                                            ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                                            : 0L);
                                            i2.putExtra(Gallery.EXTRA_SUBREDDIT,
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                                            startActivity(i2);
                                        }
                                    }
                                });
                    }
                    b.show();
                }
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.isPro) {
                    List<Submission> posts =
                            ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra("offline",
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                        != null
                                        ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                        : 0L);
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
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
                                    });
                    if (SettingValues.previews > 0) {
                        b.setNeutralButton("Preview (" + SettingValues.previews + ")",
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
                                                ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                                        if (posts != null && !posts.isEmpty()) {
                                            Intent i2 =
                                                    new Intent(MainActivity.this, Shadowbox.class);
                                            i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                                            i2.putExtra("offline",
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.cached
                                                            != null
                                                            ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time
                                                            : 0L);
                                            i2.putExtra(Shadowbox.EXTRA_SUBREDDIT,
                                                    ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
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

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        inNightMode = SettingValues.isNight();
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created
            finish();
            return;
        }
        if (!Slide.hasStarted) {
            Slide.hasStarted = true;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        boolean first = false;
        if (Reddit.colors != null && !Reddit.colors.contains("firstStart53")) {
            new AlertDialogWrapper.Builder(this).setTitle("Content settings have moved!")
                    .setMessage("NSFW content is now disabled by default. If you are over the age of 18, to re-enable NSFW content, visit Settings > Content settings")
                    .setPositiveButton(R.string.btn_ok, null).setCancelable(false).show();
            Reddit.colors.edit().putBoolean("firstStart53", true).apply();
        }
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
            if (Authentication.didOnline
                    && NetworkUtil.isConnected(MainActivity.this)
                    && !checkedPopups) {
                runAfterLoad = new Runnable() {
                    @Override
                    public void run() {
                        runAfterLoad = null;
                        if (Authentication.isLoggedIn) {
                            new AsyncNotificationBadge().executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        if (!Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, "").isEmpty()) {
                            new CheckForMail.AsyncGetSubs(MainActivity.this).executeOnExecutor(
                                    AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        new AsyncTask<Void, Void, Submission>() {
                            @Override
                            protected Submission doInBackground(Void... params) {
                                if (Authentication.isLoggedIn) UserSubscriptions.doOnlineSyncing();
                                try {
                                    SubredditPaginator p =
                                            new SubredditPaginator(Authentication.reddit,
                                                    "slideforreddit");
                                    p.setLimit(2);
                                    ArrayList<Submission> posts = new ArrayList<>(p.next());
                                    for (Submission s : posts) {
                                        String version = BuildConfig.VERSION_NAME;
                                        if (version.length() > 5) {
                                            version =
                                                    version.substring(0, version.lastIndexOf("."));
                                        }
                                        if (s.isStickied()
                                                && s.getSubmissionFlair().getText() != null
                                                && s.getSubmissionFlair()
                                                .getText()
                                                .equalsIgnoreCase("Announcement")
                                                && !Reddit.appRestart.contains(
                                                "announcement" + s.getFullName())
                                                && s.getTitle().contains(version)) {
                                            Reddit.appRestart.edit()
                                                    .putBoolean("announcement" + s.getFullName(),
                                                            true)
                                                    .apply();
                                            return s;
                                        } else if (BuildConfig.VERSION_NAME.contains("alpha")
                                                && s.isStickied()
                                                && s.getSubmissionFlair().getText() != null
                                                && s.getSubmissionFlair()
                                                .getText()
                                                .equalsIgnoreCase("Alpha")
                                                && !Reddit.appRestart.contains(
                                                "announcement" + s.getFullName())
                                                && s.getTitle()
                                                .contains(BuildConfig.VERSION_NAME)) {
                                            Reddit.appRestart.edit()
                                                    .putBoolean("announcement" + s.getFullName(),
                                                            true)
                                                    .apply();
                                            return s;
                                        } else if (s.isStickied()
                                                && s.getSubmissionFlair()
                                                .getText()
                                                .equalsIgnoreCase("PRO")
                                                && !SettingValues.isPro
                                                && !Reddit.appRestart.contains(
                                                "announcement" + s.getFullName())) {
                                            Reddit.appRestart.edit()
                                                    .putBoolean("announcement" + s.getFullName(),
                                                            true)
                                                    .apply();
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
                                    Reddit.appRestart.edit()
                                            .putString("page",
                                                    s.getDataNode().get("selftext_html").asText())
                                            .apply();
                                    Reddit.appRestart.edit()
                                            .putString("title", s.getTitle())
                                            .apply();
                                    Reddit.appRestart.edit().putString("url", s.getUrl()).apply();

                                    String title;
                                    if (s.getTitle().toLowerCase(Locale.ENGLISH).contains("release")) {
                                        title = getString(R.string.btn_changelog);
                                    } else {
                                        title = getString(R.string.btn_view);
                                    }
                                    Snackbar snack = Snackbar.make(pager, s.getTitle(),
                                            Snackbar.LENGTH_INDEFINITE)
                                            .setAction(title, new OnSingleClickListener() {
                                                @Override
                                                public void onSingleClick(View v) {
                                                    Intent i = new Intent(MainActivity.this,
                                                            Announcement.class);
                                                    startActivity(i);
                                                }
                                            });
                                    View view = snack.getView();
                                    TextView tv = view.findViewById(
                                            com.google.android.material.R.id.snackbar_text);
                                    tv.setTextColor(Color.WHITE);
                                    snack.show();
                                }
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        //todo this  new AsyncStartNotifSocket().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        if (getIntent() != null && getIntent().hasExtra(EXTRA_PAGE_TO)) {
            toGoto = getIntent().getIntExtra(EXTRA_PAGE_TO, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(
                    Palette.getDarkerColor(Palette.getDarkerColor(Palette.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);

        //Gets the height of the header
        if (header != null) {
            header.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            headerHeight = header.getHeight();
                            header.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }

        pager = (ToggleSwipeViewPager)

                findViewById(R.id.content_view);

        singleMode = SettingValues.single;
        if (singleMode)

        {
            commentPager = SettingValues.commentPager;
        }
        // Inflate tabs if single mode is disabled
        if (!singleMode)

        {
            mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        }
        // Disable swiping if single mode is enabled
        if (singleMode)

        {
            pager.setSwipingEnabled(false);
        }

        sidebarBody = (SpoilerRobotoTextView)

                findViewById(R.id.sidebar_text);

        sidebarOverflow = (CommentOverflow)

                findViewById(R.id.commentOverflow);

        if (!Reddit.appRestart.getBoolean("isRestarting", false) && Reddit.colors.contains(
                "Tutorial"))

        {
            LogUtil.v("Starting main " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "LOGGEDOUT");
            UserSubscriptions.doMainActivitySubs(this);
        } else if (!first)

        {
            LogUtil.v("Starting main 2 " + Authentication.name);
            Authentication.isLoggedIn = Reddit.appRestart.getBoolean("loggedin", false);
            Authentication.name = Reddit.appRestart.getString("name", "LOGGEDOUT");
            Reddit.appRestart.edit().putBoolean("isRestarting", false).commit();
            Reddit.isRestarting = false;
            UserSubscriptions.doMainActivitySubs(this);
        }


        final SharedPreferences seen = getSharedPreferences("SEEN", 0);
        if (!seen.contains("isCleared") && !seen.getAll().

                isEmpty()

                || !Reddit.appRestart.contains("hasCleared"))

        {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    KVManger m = KVStore.getInstance();
                    Map<String, ?> values = seen.getAll();
                    for (Map.Entry<String, ?> entry : values.entrySet()) {
                        if (entry.getKey().length() == 6 && entry.getValue() instanceof Boolean) {
                            m.insert(entry.getKey(), "true");
                        } else if (entry.getValue() instanceof Long) {
                            m.insert(entry.getKey(),
                                    String.valueOf(seen.getLong(entry.getKey(), 0)));
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
                            if (entry.getValue() instanceof String
                                    && ((String) entry.getValue()).length() > 300) {
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
                    d = new MaterialDialog.Builder(MainActivity.this).title(
                            R.string.misc_setting_up)
                            .content(R.string.misc_setting_up_message)
                            .progress(true, 100)
                            .cancelable(false)
                            .build();
                    d.show();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }


        if (!BuildConfig.isFDroid && Authentication.isLoggedIn && NetworkUtil.isConnected(MainActivity.this))

        {
            // Display an snackbar that asks the user to rate the app after this
            // activity was created 6 times, never again when once clicked or with a maximum of
            // two times.
            SnackEngage.from(MainActivity.this)
                    .withSnack(new RateSnack().withConditions(new NeverAgainWhenClickedOnce(),
                            new AfterNumberOfOpportunities(10), new WithLimitedNumberOfTimes(2))
                            .overrideActionText(getString(R.string.misc_rate_msg))
                            .overrideTitleText(getString(R.string.misc_rate_title))
                            .withDuration(BaseSnack.DURATION_LONG))
                    /*.withSnack(new CustomSnack(new Intent(MainActivity.this, SettingsReddit.class), "Thumbnails are disabled", "Change", "THUMBNAIL_INFO")
                            .withConditions(new AfterNumberOfOpportunities(2),
                                    new WithLimitedNumberOfTimes(2), new NeverAgainWhenClickedOnce())
                            .withDuration(BaseSnack.DURATION_LONG))*/
                    .build()
                    .engageWhenAppropriate();
        }

        if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)

        {
            setupSubredditSearchToolbar();
        }

        /**
         * int for the current base theme selected.
         * 0 = Dark, 1 = Light, 2 = AMOLED, 3 = Dark blue, 4 = AMOLED with contrast, 5 = Sepia
         */
        SettingValues.currentTheme = new ColorPreferences(this).getFontStyle().getThemeType();
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        try {
            this.registerReceiver(networkStateReceiver,
                    new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {

        }

        LogUtil.v("Installed browsers");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://ccrama.me/"));
                List<ResolveInfo> allApps = getPackageManager().queryIntentActivities (intent,
                        PackageManager.GET_DISABLED_COMPONENTS);
        for(ResolveInfo i : allApps){
            if(i.activityInfo.isEnabled())
                LogUtil.v(i.activityInfo.packageName);
        }
    }

    @Override
    public void networkAvailable() {
        if (runAfterLoad == null && Reddit.authentication != null) {
            Authentication.resetAdapter();
        }
    }

    NetworkStateReceiver networkStateReceiver;

    @Override
    public void networkUnavailable() {
    }

    public void checkClipboard(){
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard.hasPrimaryClip()) {
                ClipData data = clipboard.getPrimaryClip();
                final String s = (String) data.getItemAt(0).getText();
                if (!s.isEmpty()) {
                    if (ContentType.getContentType(s) == ContentType.Type.REDDIT && !HasSeen.getSeen(s)) {
                        Snackbar snack =
                                Snackbar.make(mToolbar, "Reddit link found in your clipboard",
                                        Snackbar.LENGTH_LONG);
                        snack.setAction("OPEN", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                OpenRedditLink.openUrl(MainActivity.this, s, false);
                            }
                        });
                        snack.show();
                    }
                }
            }
        } catch (Exception e){
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Authentication.isLoggedIn && Authentication.didOnline && NetworkUtil.isConnected(
                MainActivity.this) && headerMain != null && runAfterLoad == null) {
            new AsyncNotificationBadge().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else if(Authentication.isLoggedIn && Authentication.name.equalsIgnoreCase("loggedout")) {
            restartTheme(); //force a restart because we should not be here
        }

        if ((!inNightMode && SettingValues.isNight()) || (inNightMode && !SettingValues.isNight())) {
            ((SwitchCompat) drawerLayout.findViewById(R.id.toggle_night_mode)).setChecked(SettingValues.isNight());
            restartTheme();
        }
        checkClipboard();

        if (pager != null && commentPager) {
            if (pager.getCurrentItem() != toOpenComments && shouldLoad != null) {
                if (usedArray != null
                        && !shouldLoad.contains("+")
                        && usedArray.indexOf(shouldLoad) != pager.getCurrentItem()) {
                    pager.setCurrentItem(toOpenComments - 1);
                }
            }
        }

        Reddit.setDefaultErrorHandler(this);

        if (sideArrayAdapter != null) {
            sideArrayAdapter.updateHistory(UserSubscriptions.getHistory());
        }

     /* remove   if (datasetChanged && UserSubscriptions.hasSubs() && !usedArray.isEmpty()) {
            usedArray = new ArrayList<>(UserSubscriptions.getSubscriptions(this));
            adapter.notifyDataSetChanged();
            sideArrayAdapter.notifyDataSetChanged();
            datasetChanged = false;
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                scrollToTabAfterLayout(pager.getCurrentItem());
            }
            setToolbarClick();
        }*/
        //Only refresh the view if a Setting was altered
        if (Settings.changed || SettingsThemeFragment.changed) {

            reloadSubs();
            //If the user changed a Setting regarding the app's theme, restartTheme()
            if (SettingsThemeFragment.changed /* todo maybe later || (usedArray != null && usedArray.size() != UserSubscriptions.getSubscriptions(this).size())*/) {
                restartTheme();
            }

            //Need to change the subreddit search method
            if (SettingsGeneralFragment.searchChanged) {
                setDrawerSubList();

                if (SettingValues.subredditSearchMethod
                        == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                    mToolbar.setOnLongClickListener(
                            null); //remove the long click listener from the toolbar
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                } else if (SettingValues.subredditSearchMethod
                        == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                    setupSubredditSearchToolbar();
                } else if (SettingValues.subredditSearchMethod
                        == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                    findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                    setupSubredditSearchToolbar();
                    setDrawerSubList();
                }
                SettingsGeneralFragment.searchChanged = false;
            }
            SettingsThemeFragment.changed = false;
            Settings.changed = false;
            setToolbarClick();
        }
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(networkStateReceiver);
        } catch (Exception ignored) {

        }
        dismissProgressDialog();
        Slide.hasStarted = false;
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return onOptionsItemSelected(menu.findItem(R.id.search));
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String abbreviate(final String str, final int maxWidth) {
        if (str.length() <= maxWidth) {
            return str;
        }

        final String abrevMarker = "...";
        return str.substring(0, maxWidth - 3) + abrevMarker;
    }

    /**
     * Set the drawer edge (i.e. how sensitive the drawer is) Based on a given screen width
     * percentage.
     *
     * @param displayWidthPercentage larger the value, the more sensitive the drawer swipe is;
     *                               percentage of screen width
     * @param drawerLayout           drawerLayout to adjust the swipe edge
     */
    public static void setDrawerEdge(Activity activity, final float displayWidthPercentage,
            DrawerLayout drawerLayout) {
        try {
            Field mDragger =
                    drawerLayout.getClass().getSuperclass().getDeclaredField("mLeftDragger");
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

    public HashMap<String, String> accounts = new HashMap<>();

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
            header.findViewById(R.id.multi).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    if (runAfterLoad == null) {
                        Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                        MainActivity.this.startActivity(inte);
                    }
                }
            });
            header.findViewById(R.id.multi).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new MaterialDialog.Builder(MainActivity.this).inputRange(3, 20)
                            .alwaysCallInputCallback()
                            .input(getString(R.string.user_enter), null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    final EditText editText = dialog.getInputEditText();
                                    EditTextValidator.validateUsername(editText);
                                    if (input.length() >= 3 && input.length() <= 20) {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    }
                                }
                            })
                            .positiveText(R.string.user_btn_gotomultis)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (runAfterLoad == null) {
                                        Intent inte = new Intent(MainActivity.this,
                                                MultiredditOverview.class);
                                        inte.putExtra(Profile.EXTRA_PROFILE,
                                                dialog.getInputEditText().getText().toString());
                                        MainActivity.this.startActivity(inte);
                                    }
                                }
                            })
                            .negativeText(R.string.btn_cancel)
                            .show();
                    return true;
                }
            });

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
            header.findViewById(R.id.later).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Intent inte = new Intent(MainActivity.this, PostReadLater.class);
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

            /**
             * If the user is a known mod, show the "Moderation" drawer item quickly to
             * stop the UI from jumping
             */
            if (modOf != null && !modOf.isEmpty() && Authentication.mod) {
                header.findViewById(R.id.mod).setVisibility(View.VISIBLE);
            }
//update notification badge

            final LinearLayout profStuff = header.findViewById(R.id.accountsarea);
            profStuff.setVisibility(View.GONE);
            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profStuff.getVisibility() == View.GONE) {
                        expand(profStuff);
                        header.setContentDescription(getResources().getString(R.string.btn_collapse));
                        flipAnimator(false, header.findViewById(R.id.headerflip)).start();
                    } else {
                        collapse(profStuff);
                        header.setContentDescription(getResources().getString(R.string.btn_expand));
                        flipAnimator(true, header.findViewById(R.id.headerflip)).start();
                    }

                }
            });

            for (String s : Authentication.authentication.getStringSet("accounts", new HashSet<String>())) {
                if (s.contains(":")) {
                    accounts.put(s.split(":")[0], s.split(":")[1]);
                } else {
                    accounts.put(s, "");
                }
            }
            final ArrayList<String> keys = new ArrayList<>(accounts.keySet());

            final LinearLayout accountList = header.findViewById(R.id.accountsarea);
            for (final String accName : keys) {
                LogUtil.v(accName);
                final View t =
                        getLayoutInflater().inflate(R.layout.account_textview_white, accountList,
                                false);
                ((TextView) t.findViewById(R.id.name)).setText(accName);
                LogUtil.v("Adding click to " + ((TextView) t.findViewById(R.id.name)).getText());
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(R.string.profile_remove)
                                .setMessage(R.string.profile_remove_account)
                                .setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog2, int which2) {
                                        Set<String> accounts2 = Authentication.authentication.getStringSet(
                                                "accounts", new HashSet<String>());
                                        Set<String> done = new HashSet<>();
                                        for (String s : accounts2) {
                                            if (!s.contains(accName)) {
                                                done.add(s);
                                            }
                                        }
                                        Authentication.authentication.edit()
                                                .putStringSet("accounts", done)
                                                .commit();
                                        dialog2.dismiss();
                                        accountList.removeView(t);
                                        if (accName.equalsIgnoreCase(Authentication.name)) {

                                            boolean d = false;
                                            for (String s : keys) {

                                                if (!s.equalsIgnoreCase(accName)) {
                                                    d = true;
                                                    LogUtil.v("Switching to " + s);
                                                    for(Map.Entry<String, String> e : accounts.entrySet()){
                                                        LogUtil.v(e.getKey() + ":" + e.getValue());
                                                    }
                                                    if (accounts.containsKey(s) && !accounts.get(s)
                                                            .isEmpty()) {
                                                        Authentication.authentication.edit()
                                                                .putString("lasttoken", accounts.get(s))
                                                                .remove("backedCreds")
                                                                .commit();
                                                    } else {
                                                        ArrayList<String> tokens = new ArrayList<>(
                                                                Authentication.authentication.getStringSet(
                                                                        "tokens", new HashSet<String>()));
                                                        int index = keys.indexOf(s);
                                                        if (keys.indexOf(s) > tokens.size()) {
                                                            index -= 1;
                                                        }
                                                        Authentication.authentication.edit()
                                                                .putString("lasttoken",
                                                                        tokens.get(index))
                                                                .remove("backedCreds")
                                                                .commit();
                                                    }
                                                    Authentication.name = s;
                                                    UserSubscriptions.switchAccounts();
                                                    Reddit.forceRestart(MainActivity.this, true);
                                                    break;
                                                }

                                            }
                                            if (!d) {
                                                Authentication.name = "LOGGEDOUT";
                                                Authentication.isLoggedIn = false;
                                                Authentication.authentication.edit()
                                                        .remove("lasttoken")
                                                        .remove("backedCreds")
                                                        .commit();
                                                UserSubscriptions.switchAccounts();
                                                Reddit.forceRestart(MainActivity.this, true);
                                            }

                                        } else {
                                            accounts.remove(accName);
                                            keys.remove(accName);
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.btn_cancel, null)
                                .show();


                    }
                });
                t.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String accName = ((TextView) t.findViewById(R.id.name)).getText().toString();
                        LogUtil.v("Found name is " + accName);
                        if (!accName.equalsIgnoreCase(Authentication.name)) {
                            LogUtil.v("Switching to " + accName);
                            if (!accounts.get(accName).isEmpty()) {
                                LogUtil.v("Using token " + accounts.get(accName));
                                Authentication.authentication.edit()
                                        .putString("lasttoken", accounts.get(accName))
                                        .remove("backedCreds")
                                        .apply();
                            } else {
                                ArrayList<String> tokens = new ArrayList<>(
                                        Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                Authentication.authentication.edit()
                                        .putString("lasttoken", tokens.get(keys.indexOf(accName)))
                                        .remove("backedCreds")
                                        .apply();
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
                    LinearLayout body = header.findViewById(R.id.expand_profile);
                    if (body.getVisibility() == View.GONE) {
                        expand(body);
                        flipAnimator(false, view).start();
                        view.findViewById(R.id.godown).setContentDescription(getResources().getString(R.string.btn_collapse));
                    } else {
                        collapse(body);
                        flipAnimator(true, view).start();
                        view.findViewById(R.id.godown).setContentDescription(getResources().getString(R.string.btn_expand));
                    }
                }
            });

            header.findViewById(R.id.guest_mode).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Authentication.name = "LOGGEDOUT";
                    Authentication.isLoggedIn = false;
                    Authentication.authentication.edit()
                            .remove("lasttoken")
                            .remove("backedCreds")
                            .apply();
                    UserSubscriptions.switchAccounts();
                    Reddit.forceRestart(MainActivity.this, true);
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
                    Reddit.forceRestart(MainActivity.this, false);
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
                new AsyncNotificationBadge().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        } else if (Authentication.didOnline) {
            header = inflater.inflate(R.layout.drawer_loggedout, drawerSubList, false);
            drawerSubList.addHeaderView(header, null, false);
            headerMain = header;
            hea = header.findViewById(R.id.back);

            final LinearLayout profStuff = header.findViewById(R.id.accountsarea);
            profStuff.setVisibility(View.GONE);
            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profStuff.getVisibility() == View.GONE) {
                        expand(profStuff);
                        flipAnimator(false, header.findViewById(R.id.headerflip)).start();
                        header.findViewById(R.id.headerflip).setContentDescription(getResources().getString(R.string.btn_collapse));
                    } else {
                        collapse(profStuff);
                        flipAnimator(true, header.findViewById(R.id.headerflip)).start();
                        header.findViewById(R.id.headerflip).setContentDescription(getResources().getString(R.string.btn_expand));
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

            final LinearLayout accountList = header.findViewById(R.id.accountsarea);
            for (final String accName : keys) {
                LogUtil.v(accName);
                final View t =
                        getLayoutInflater().inflate(R.layout.account_textview_white, accountList,
                                false);
                ((TextView) t.findViewById(R.id.name)).setText(accName);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(R.string.profile_remove)
                                .setMessage(R.string.profile_remove_account)
                                .setNegativeButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog2, int which2) {
                                        Set<String> accounts2 = Authentication.authentication.getStringSet(
                                                "accounts", new HashSet<String>());
                                        Set<String> done = new HashSet<>();
                                        for (String s : accounts2) {
                                            if (!s.contains(accName)) {
                                                done.add(s);
                                            }
                                        }
                                        Authentication.authentication.edit()
                                                .putStringSet("accounts", done)
                                                .commit();
                                        dialog2.dismiss();
                                        accountList.removeView(t);

                                        if (accName.equalsIgnoreCase(Authentication.name)) {
                                            boolean d = false;
                                            for (String s : keys) {
                                                if (!s.equalsIgnoreCase(accName)) {
                                                    d = true;
                                                    LogUtil.v("Switching to " + s);
                                                    if (!accounts.get(s).isEmpty()) {
                                                        Authentication.authentication.edit()
                                                                .putString("lasttoken", accounts.get(s))
                                                                .remove("backedCreds")
                                                                .commit();

                                                    } else {
                                                        ArrayList<String> tokens = new ArrayList<>(
                                                                Authentication.authentication.getStringSet(
                                                                        "tokens", new HashSet<String>()));
                                                        Authentication.authentication.edit()
                                                                .putString("lasttoken", tokens.get(
                                                                        keys.indexOf(s)))
                                                                .remove("backedCreds")
                                                                .commit();
                                                    }
                                                    Authentication.name = s;
                                                    UserSubscriptions.switchAccounts();
                                                    Reddit.forceRestart(MainActivity.this, true);
                                                }
                                            }
                                            if (!d) {
                                                Authentication.name = "LOGGEDOUT";
                                                Authentication.isLoggedIn = false;
                                                Authentication.authentication.edit()
                                                        .remove("lasttoken")
                                                        .remove("backedCreds")
                                                        .commit();
                                                UserSubscriptions.switchAccounts();
                                                Reddit.forceRestart(MainActivity.this, true);
                                            }
                                        } else {
                                            accounts.remove(accName);
                                            keys.remove(accName);
                                        }
                                    }
                                })
                                .setPositiveButton(R.string.btn_cancel, null)
                                .show();
                    }
                });
                t.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (!accName.equalsIgnoreCase(Authentication.name)) {
                            if (!accounts.get(accName).isEmpty()) {
                                Authentication.authentication.edit()
                                        .putString("lasttoken", accounts.get(accName))
                                        .remove("backedCreds")
                                        .commit();
                            } else {
                                ArrayList<String> tokens = new ArrayList<>(
                                        Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                                Authentication.authentication.edit()
                                        .putString("lasttoken", tokens.get(keys.indexOf(accName)))
                                        .remove("backedCreds")
                                        .commit();
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
                    Reddit.forceRestart(MainActivity.this, false);
                }
            });
            headerMain = header;

            header.findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(MainActivity.this).inputRange(3, 20)
                            .alwaysCallInputCallback()
                            .input(getString(R.string.user_enter), null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    final EditText editText = dialog.getInputEditText();
                                    EditTextValidator.validateUsername(editText);
                                    if (input.length() >= 3 && input.length() <= 20) {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    }
                                }
                            })
                            .positiveText(R.string.user_btn_gotomultis)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    if (runAfterLoad == null) {
                                        Intent inte = new Intent(MainActivity.this,
                                                MultiredditOverview.class);
                                        inte.putExtra(Profile.EXTRA_PROFILE,
                                                dialog.getInputEditText().getText().toString());
                                        MainActivity.this.startActivity(inte);
                                    }
                                }
                            })
                            .negativeText(R.string.btn_cancel)
                            .show();
                }
            });

        } else {
            header = inflater.inflate(R.layout.drawer_offline, drawerSubList, false);
            headerMain = header;
            drawerSubList.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);

            header.findViewById(R.id.online).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View view) {
                    Reddit.appRestart.edit().remove("forceoffline").commit();
                    Reddit.forceRestart(MainActivity.this, false);
                }
            });

        }

        final LinearLayout expandSettings = header.findViewById(R.id.expand_settings);
        header.findViewById(R.id.godown_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandSettings.getVisibility() == View.GONE) {
                    expand(expandSettings);
                    header.findViewById(R.id.godown_settings).setContentDescription(getResources().getString(R.string.btn_collapse));
                    flipAnimator(false, v).start();
                } else {
                    collapse(expandSettings);
                    header.findViewById(R.id.godown_settings).setContentDescription(getResources().getString(R.string.btn_expand));
                    flipAnimator(true, v).start();
                }
            }
        });

        {   // Set up quick setting toggles
            final SwitchCompat toggleNightMode = expandSettings.findViewById(R.id.toggle_night_mode);
            if (SettingValues.isPro) {
                toggleNightMode.setVisibility(View.VISIBLE);
                toggleNightMode.setChecked(inNightMode);
                toggleNightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingValues.forcedNightModeState = isChecked
                                ? SettingValues.ForcedState.FORCED_ON
                                : SettingValues.ForcedState.FORCED_OFF;
                        restartTheme();
                    }
                });
            }

            final SwitchCompat toggleImmersiveMode = expandSettings.findViewById(R.id.toggle_immersive_mode);
            toggleImmersiveMode.setChecked(SettingValues.immersiveMode);
            toggleImmersiveMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.immersiveMode = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMMERSIVE_MODE, isChecked).apply();
                    if (isChecked) {
                        hideDecor();
                    } else {
                        showDecor();
                    }
                }
            });

            final SwitchCompat toggleNSFW = expandSettings.findViewById(R.id.toggle_nsfw);
            toggleNSFW.setChecked(SettingValues.showNSFWContent);
            toggleNSFW.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.showNSFWContent = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SHOW_NSFW_CONTENT, isChecked).apply();
                    reloadSubs();
                }
            });

            final SwitchCompat toggleRightThumbnails = expandSettings.findViewById(R.id.toggle_right_thumbnails);
            toggleRightThumbnails.setChecked(SettingValues.switchThumb);
            toggleRightThumbnails.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.switchThumb = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SWITCH_THUMB, isChecked).apply();
                    reloadSubs();
                }
            });

            final SwitchCompat toggleReaderMode = expandSettings.findViewById(R.id.toggle_reader_mode);
            toggleReaderMode.setChecked(SettingValues.readerMode);
            toggleReaderMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.readerMode = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_READER_MODE, isChecked).apply();
                }
            });
        }

        header.findViewById(R.id.manage).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent i = new Intent(MainActivity.this, ManageOfflineContent.class);
                startActivity(i);
            }
        });
        if (Authentication.didOnline) {
            View support = header.findViewById(R.id.support);

            if (SettingValues.isPro) {
                support.setVisibility(View.GONE);
            } else {
                header.findViewById(R.id.support).setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(R.string.settings_support_slide)
                                .setMessage(R.string.pro_upgrade_msg)
                                .setPositiveButton(R.string.btn_yes_exclaim, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("market://details?id=" + getString(R.string.ui_unlock_package))));
                                        } catch (ActivityNotFoundException e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                                    "http://play.google.com/store/apps/details?id="
                                                            + getString(R.string.ui_unlock_package))));
                                        }
                                    }
                                })
                                .setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                });
            }
            header.findViewById(R.id.prof).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MaterialDialog.Builder(MainActivity.this).inputRange(3, 20)
                            .alwaysCallInputCallback()
                            .input(getString(R.string.user_enter), null, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    final EditText editText = dialog.getInputEditText();
                                    EditTextValidator.validateUsername(editText);
                                    if (input.length() >= 3 && input.length() <= 20) {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    }
                                }
                            })
                            .positiveText(R.string.user_btn_goto)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent inte = new Intent(MainActivity.this, Profile.class);
                                    //noinspection ConstantConditions
                                    inte.putExtra(Profile.EXTRA_PROFILE,
                                            dialog.getInputEditText().getText().toString());
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
                if (mAsyncGetSubreddit != null) mAsyncGetSubreddit.cancel(true);
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

        final ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.btn_open, R.string.btn_close) {
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
                            if (compare.equals("random") || compare.equals("myrandom") || compare.equals("randnsfw")) {
                                if (adapter != null
                                        && adapter.getCurrentFragment() != null
                                        && ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet.subredditRandom
                                        != null) {
                                    String sub = ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet.subredditRandom;
                                    doSubSidebarNoLoad(sub);
                                    doSubSidebar(sub);
                                }
                            } else {
                                doSubSidebar(usedArray.get(current));
                            }
                        }
                    }

                    @Override
                    public void onDrawerClosed(View view) {
                        super.onDrawerClosed(view);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(drawerLayout.getWindowToken(), 0);
                    }
                };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Palette.getColor("alsdkfjasld"));
        accountsArea = header.findViewById(R.id.accountsarea);
        if (accountsArea != null) {
            accountsArea.setBackgroundColor(Palette.getDarkerColor("alsdkfjasld"));
        }


        setDrawerSubList();
        hideDrawerItems();
    }

    public void hideDrawerItems() {
        for (DrawerItemsDialog.SettingsDrawerEnum settingDrawerItem : DrawerItemsDialog.SettingsDrawerEnum
                .values()) {
            View drawerItem = drawerSubList.findViewById(settingDrawerItem.drawerId);
            if (drawerItem != null
                    && drawerItem.getVisibility() == View.VISIBLE
                    && (SettingValues.selectedDrawerItems & settingDrawerItem.value) == 0) {
                drawerItem.setVisibility(View.GONE);
            }
        }
    }

    public void doForcePrefs() {
        HashSet<String> domains = new HashSet<>();
        for (String s : SettingValues.alwaysExternal) {
            if (!s.isEmpty()) {
                s = s.trim();
                final String finalS = s;
                if (!finalS.contains("youtu")) domains.add(finalS);
            }
        }

        // Make some domains open externally by default, can be used with Chrome Customtabs if they remove the option in settings
        domains.add("youtube.com");
        domains.add("youtu.be");
        domains.add("play.google.com");

        SettingValues.prefs.edit()
                .putStringSet(SettingValues.PREF_ALWAYS_EXTERNAL, domains)
                .apply();

        SettingValues.alwaysExternal = domains;
    }

    public void doFriends(final List<String> friends) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (friends != null
                        && !friends.isEmpty()
                        && headerMain.findViewById(R.id.friends) != null) {
                    headerMain.findViewById(R.id.friends).setVisibility(View.VISIBLE);
                    headerMain.findViewById(R.id.friends)
                            .setOnClickListener(new OnSingleClickListener() {
                                @Override
                                public void onSingleClick(View view) {
                                    new MaterialDialog.Builder(MainActivity.this).title("Friends")
                                            .items(friends)
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog,
                                                        View itemView, int which,
                                                        CharSequence text) {
                                                    Intent i = new Intent(MainActivity.this,
                                                            Profile.class);
                                                    i.putExtra(Profile.EXTRA_PROFILE,
                                                            friends.get(which));
                                                    startActivity(i);
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            });
                } else if (Authentication.isLoggedIn
                        && headerMain.findViewById(R.id.friends) != null) {
                    headerMain.findViewById(R.id.friends).setVisibility(View.GONE);
                }
            }
        });
    }

    public void doPageSelectedComments(int position) {

        pager.setSwipeLeftOnly(false);

        header.animate().translationY(0).setInterpolator(new LinearInterpolator()).setDuration(180);


        Reddit.currentPosition = position;
        if (position + 1 != currentComment) {
            doSubSidebarNoLoad(usedArray.get(position));
        }
        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
        if (page != null && page.adapter != null) {
            SubredditPosts p = page.adapter.dataSet;
            if (p.offline && p.cached != null) {
                Toast.makeText(MainActivity.this, getString(R.string.offline_last_update,
                        TimeUtils.getTimeAgo(p.cached.time, MainActivity.this)), Toast.LENGTH_LONG)
                        .show();
            }
        }

        if (hea != null) {
            hea.setBackgroundColor(Palette.getColor(usedArray.get(position)));
            if (accountsArea != null) {
                accountsArea.setBackgroundColor(Palette.getDarkerColor(usedArray.get(position)));
            }
        }
        header.setBackgroundColor(Palette.getColor(usedArray.get(position)));

        themeSystemBars(usedArray.get(position));
        setRecentBar(usedArray.get(position));

        if (SettingValues.single) {
            getSupportActionBar().setTitle(usedArray.get(position));
        } else {
            if (mTabLayout != null) {
                mTabLayout.setSelectedTabIndicatorColor(
                        new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));
            }
        }

        selectedSub = usedArray.get(position);
    }

    public void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSubredditType() != null) {
            canSubmit = !subreddit.getSubredditType().equals("RESTRICTED");
        } else {
            canSubmit = true;
        }
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText().trim();
            setViews(text, subreddit.getDisplayName(), sidebarBody, sidebarOverflow);

            //get all subs that have Notifications enabled
            ArrayList<String> rawSubs =
                    Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
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
                    subThresholds.containsKey(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH));
            ((AppCompatCheckBox) findViewById(R.id.notify_posts_state)).setChecked(isNotified);
        } else {
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
        }
        {
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
                                    UserSubscriptions.syncMultiReddits(MainActivity.this);
                                }
                                for (MultiReddit r : UserSubscriptions.multireddits) {
                                    multis.put(r.getDisplayName(), r);
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                new MaterialDialog.Builder(MainActivity.this).title(
                                        getString(R.string.multi_add_to,
                                                subreddit.getDisplayName()))
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
                                                                            new String[0])[which];
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
                                                                    MainActivity.this);

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
                                                                            com.google.android.material.R.id.snackbar_text);
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
        }
        {
            final AppCompatCheckBox notifyStateCheckBox =
                    (AppCompatCheckBox) findViewById(R.id.notify_posts_state);
            assert notifyStateCheckBox != null;

            notifyStateCheckBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                final String sub = subreddit.getDisplayName();

                                if (!sub.equalsIgnoreCase("all")
                                        && !sub.equalsIgnoreCase("frontpage")
                                        && !sub.equalsIgnoreCase("friends")
                                        && !sub.equalsIgnoreCase("mod")
                                        && !sub.contains("+")
                                        && !sub.contains(".")
                                        && !sub.contains("/m/")) {
                                    new AlertDialogWrapper.Builder(MainActivity.this).setTitle(
                                            getString(R.string.sub_post_notifs_title, sub))
                                            .setMessage(R.string.sub_post_notifs_msg)
                                            .setPositiveButton(R.string.btn_ok,
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            new MaterialDialog.Builder(
                                                                    MainActivity.this).title(
                                                                    R.string.sub_post_notifs_threshold)
                                                                    .items(new String[]{
                                                                            "1", "5", "10", "20",
                                                                            "40", "50"
                                                                    })
                                                                    .alwaysCallSingleChoiceCallback()
                                                                    .itemsCallbackSingleChoice(0,
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
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            notifyStateCheckBox.setChecked(false);
                                                        }
                                                    })
                                            .setOnCancelListener(
                                                    new DialogInterface.OnCancelListener() {
                                                        @Override
                                                        public void onCancel(
                                                                DialogInterface dialog) {
                                                            notifyStateCheckBox.setChecked(false);
                                                        }
                                                    })
                                            .show();
                                } else {
                                    notifyStateCheckBox.setChecked(false);
                                    Toast.makeText(MainActivity.this, R.string.sub_post_notifs_err,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Intent cancelIntent =
                                        new Intent(MainActivity.this, CancelSubNotifs.class);
                                cancelIntent.putExtra(CancelSubNotifs.EXTRA_SUB,
                                        subreddit.getDisplayName());
                                startActivity(cancelIntent);
                            }
                        }
                    });
        }
        {
            final TextView subscribe = (TextView) findViewById(R.id.subscribe);
            currentlySubbed = (!Authentication.isLoggedIn && usedArray.contains(
                    subreddit.getDisplayName().toLowerCase(Locale.ENGLISH))) || subreddit.isUserSubscriber();
            doSubscribeButtonText(currentlySubbed, subscribe);

            assert subscribe != null;
            subscribe.setOnClickListener(new View.OnClickListener() {
                private void doSubscribe() {
                    if (Authentication.isLoggedIn) {
                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(
                                getString(R.string.subscribe_to, subreddit.getDisplayName()))
                                .setPositiveButton(R.string.reorder_add_subscribe,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AsyncTask<Void, Void, Boolean>() {
                                                    @Override
                                                    public void onPostExecute(Boolean success) {
                                                        if (!success) { // If subreddit was removed from account or not

                                                            new AlertDialogWrapper.Builder(
                                                                    MainActivity.this).setTitle(
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
                                                                                                    Snackbar.LENGTH_LONG);
                                                                                    View view =
                                                                                            s.getView();
                                                                                    TextView tv =
                                                                                            view
                                                                                                    .findViewById(
                                                                                                            com.google.android.material.R.id.snackbar_text);
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

                                .setNeutralButton(R.string.btn_add_to_sublist,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                changeSubscription(subreddit,
                                                        true); // Force add the subscription
                                                Snackbar s =
                                                        Snackbar.make(mToolbar, R.string.sub_added,
                                                                Snackbar.LENGTH_LONG);
                                                View view = s.getView();
                                                TextView tv = view.findViewById(
                                                        com.google.android.material.R.id.snackbar_text);
                                                tv.setTextColor(Color.WHITE);
                                                s.show();
                                            }
                                        })
                                .setNegativeButton(R.string.btn_cancel, null)
                                .show();
                    } else {
                        changeSubscription(subreddit, true);
                    }
                }

                private void doUnsubscribe() {
                    if (Authentication.didOnline) {
                        new AlertDialogWrapper.Builder(MainActivity.this).setTitle(
                                getString(R.string.unsubscribe_from, subreddit.getDisplayName()))
                                .setPositiveButton(R.string.reorder_remove_unsubsribe,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AsyncTask<Void, Void, Boolean>() {
                                                    @Override
                                                    public void onPostExecute(Boolean success) {
                                                        if (!success) { // If subreddit was removed from account or not

                                                            new AlertDialogWrapper.Builder(
                                                                    MainActivity.this).setTitle(
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
                                                                                                    Snackbar.LENGTH_LONG);
                                                                                    View view =
                                                                                            s.getView();
                                                                                    TextView tv =
                                                                                            view
                                                                                                    .findViewById(
                                                                                                            com.google.android.material.R.id.snackbar_text);
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
                                                            changeSubscription(subreddit, false);
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
                                            public void onClick(DialogInterface dialog, int which) {
                                                changeSubscription(subreddit,
                                                        false); // Force add the subscription
                                                Snackbar s = Snackbar.make(mToolbar,
                                                        R.string.misc_unsubscribed,
                                                        Snackbar.LENGTH_LONG);
                                                View view = s.getView();
                                                TextView tv = view.findViewById(
                                                        com.google.android.material.R.id.snackbar_text);
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

                @Override
                public void onClick(View v) {
                    if (!currentlySubbed) {
                        doSubscribe();
                    } else {
                        doUnsubscribe();
                    }
                    doSubscribeButtonText(currentlySubbed, subscribe);
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
        ((ImageView) findViewById(R.id.subimage)).setImageResource(0);
        if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode()
                .get("icon_img")
                .asText()
                .isEmpty()) {
            findViewById(R.id.subimage).setVisibility(View.VISIBLE);
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

    Sorting sorts;

    public void doSubSidebar(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }
        findViewById(R.id.loader).setVisibility(View.VISIBLE);

        invalidateOptionsMenu();

        if (!subreddit.equalsIgnoreCase("all")
                && !subreddit.equalsIgnoreCase("frontpage")
                && !subreddit.equalsIgnoreCase("friends")
                && !subreddit.equalsIgnoreCase("mod")
                && !subreddit.contains("+")
                && !subreddit.contains(".")
                && !subreddit.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            mAsyncGetSubreddit = new AsyncGetSubreddit();
            mAsyncGetSubreddit.execute(subreddit);

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
            dialoglayout.findViewById(R.id.syncflair)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ImageFlairs.syncFlairs(MainActivity.this, subreddit);
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
                            new AlertDialogWrapper.Builder(MainActivity.this);
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
                            new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit);

                    final Context contextThemeWrapper =
                            new ContextThemeWrapper(MainActivity.this, style);
                    LayoutInflater localInflater =
                            getLayoutInflater().cloneInContext(contextThemeWrapper);

                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);

                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(subreddit);
                    SettingsSubAdapter.showSubThemeEditor(arrayList, MainActivity.this,
                            dialoglayout);
                }
            });
            dialoglayout.findViewById(R.id.mods).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog d = new MaterialDialog.Builder(MainActivity.this).title(
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
                                    new UserRecordPaginator(Authentication.reddit, subreddit,
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
                            new MaterialDialog.Builder(MainActivity.this).title(
                                    getString(R.string.sidebar_submods, subreddit))
                                    .items(names)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog,
                                                View itemView, int which, CharSequence text) {
                                            Intent i = new Intent(MainActivity.this, Profile.class);
                                            i.putExtra(Profile.EXTRA_PROFILE, names.get(which));
                                            startActivity(i);
                                        }
                                    })
                                    .positiveText(R.string.btn_message)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                            Intent i = new Intent(MainActivity.this,
                                                    SendMessage.class);
                                            i.putExtra(SendMessage.EXTRA_NAME, "/r/" + subreddit);
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
                if (currentFlair != null) currentFlair.cancel(true);
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
                                    new MaterialDialog.Builder(MainActivity.this).items(flairText)
                                            .title(R.string.sidebar_select_flair)
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog,
                                                        View itemView, int which,
                                                        CharSequence text) {
                                                    final FlairTemplate t = flairs.get(which);
                                                    if (t.isTextEditable()) {
                                                        new MaterialDialog.Builder(
                                                                MainActivity.this).title(
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
                                                                                                            subreddit,
                                                                                                            t,
                                                                                                            flair,
                                                                                                            Authentication.name);
                                                                                            FlairTemplate
                                                                                                    currentF =
                                                                                                    m.getCurrentFlair(
                                                                                                            subreddit);
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
                                                                                                                    com.google.android.material.R.id.snackbar_text);
                                                                                            tv.setTextColor(
                                                                                                    Color.WHITE);
                                                                                            s.show();
                                                                                        }
                                                                                    }
                                                                                }.executeOnExecutor(
                                                                                        AsyncTask.THREAD_POOL_EXECUTOR);
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
                                                                            subreddit, t, null,
                                                                            Authentication.name);
                                                                    FlairTemplate currentF =
                                                                            m.getCurrentFlair(
                                                                                    subreddit);
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
                                                                            com.google.android.material.R.id.snackbar_text);
                                                                    tv.setTextColor(Color.WHITE);
                                                                    s.show();
                                                                }
                                                            }
                                                        }.executeOnExecutor(
                                                                AsyncTask.THREAD_POOL_EXECUTOR);
                                                    }
                                                }
                                            })
                                            .show();
                                }
                            });
                        }
                    }
                };
                currentFlair.execute((View) dialoglayout.findViewById(R.id.flair));
            }
        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        GravityCompat.END);
            }
        }
    }

    TimePeriod time = TimePeriod.DAY;

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
                if(SettingValues.hasSort(sub)) {
                    Sorting sortingis = SettingValues.getBaseSubmissionSort(sub);
                    sort.setText(sortingis.name()
                            + ((sortingis == Sorting.CONTROVERSIAL || sortingis == Sorting.TOP)?" of "
                            + SettingValues.getBaseTimePeriod(sub).name():""));
                } else {
                    sort.setText("Set default sorting");
                }
                reloadSubs();
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(SortingUtil.getSortingTimesStrings(),
                SortingUtil.getSortingTimeId(""), l2);
        builder.show();
    }

    public void doSubSidebarNoLoad(final String subreddit) {
        if (mAsyncGetSubreddit != null) {
            mAsyncGetSubreddit.cancel(true);
        }

        findViewById(R.id.loader).setVisibility(View.GONE);

        invalidateOptionsMenu();

        if (!subreddit.equalsIgnoreCase("all")
                && !subreddit.equalsIgnoreCase("frontpage")
                && !subreddit.equalsIgnoreCase("friends")
                && !subreddit.equalsIgnoreCase("mod")
                && !subreddit.contains("+")
                && !subreddit.contains(".")
                && !subreddit.contains("/m/")) {
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
            ((TextView) drawerLayout.findViewById(R.id.sorting).findViewById(R.id.sort)).setTextColor(subColor);
            ((TextView) findViewById(R.id.sync)).setTextColor(subColor);

        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        GravityCompat.END);
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
                                                final CardView SUGGESTIONS_BACKGROUND, final AutoCompleteTextView GO_TO_SUB_FIELD,
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
            final CardView SUGGESTIONS_BACKGROUND, final AutoCompleteTextView GO_TO_SUB_FIELD,
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

                if (SettingValues.single) {
                    getSupportActionBar().setTitle(selectedSub);
                } else {
                    getSupportActionBar().setTitle(tabViewModeTitle);
                }
            }
        }, ANIMATION_DURATION + ANIMATE_DURATION_OFFSET);
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

        new AlertDialogWrapper.Builder(this).setTitle(filterTitle)
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
        if (adapter.getCurrentFragment() == null) {
            return 0;
        }
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

    public void openPopup() {
        PopupMenu popup =
                new PopupMenu(MainActivity.this, findViewById(R.id.anchor), Gravity.RIGHT);
        String id =
                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id;
        final Spannable[] base = SortingUtil.getSortingSpannables(id);
        for (Spannable s : base) {
            // Do not add option for "Best" in any subreddit except for the frontpage.
            if (!id.equals("frontpage") && s.toString().equals(getString(R.string.sorting_best))) {
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
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.HOT);
                        reloadSubs();
                        break;
                    case 1:
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.NEW);
                        reloadSubs();
                        break;
                    case 2:
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.RISING);
                        reloadSubs();
                        break;
                    case 3:
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.TOP);
                        openPopupTime();
                        break;
                    case 4:
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.CONTROVERSIAL);
                        openPopupTime();
                        break;
                    case 5:
                        SortingUtil.setSorting(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                Sorting.BEST);
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
                new PopupMenu(MainActivity.this, findViewById(R.id.anchor), Gravity.RIGHT);
        String id =
                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id;
        final Spannable[] base = SortingUtil.getSortingTimesSpannables(id);
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
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 1:
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 2:
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.WEEK);
                        reloadSubs();
                        break;
                    case 3:
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 4:
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 5:
                        SortingUtil.setTime(
                                ((SubmissionsView) (((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment())).id,
                                TimePeriod.ALL);
                        reloadSubs();
                        break;
                }
                return true;
            }
        });
        popup.show();


    }

    public static String randomoverride;

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        if (commentPager && current == currentComment) {
            current = current - 1;
        }
        if (current < 0) {
            current = 0;
        }
        reloadItemNumber = current;
        if (adapter instanceof OverviewPagerAdapterComment) {
            pager.setAdapter(null);
            adapter = new OverviewPagerAdapterComment(getSupportFragmentManager());
        } else {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        }
        pager.setAdapter(adapter);

        reloadItemNumber = -2;
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

        if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                || SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
            setupSubredditSearchToolbar();
        }
    }

    public void resetAdapter() {
        if (UserSubscriptions.hasSubs()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    usedArray = new CaseInsensitiveArrayList(
                            UserSubscriptions.getSubscriptions(MainActivity.this));
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
        isRestart = true;
        restartPage = getCurrentPage();
        Intent intent = this.getIntent();
        int page = pager.getCurrentItem();
        if (currentComment == page) page -= 1;
        intent.putExtra(EXTRA_PAGE_TO, page);
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    public void saveOffline(final List<Submission> submissions, final String subreddit) {
        final boolean[] chosen = new boolean[2];
        new AlertDialogWrapper.Builder(this).setTitle(R.string.save_for_offline_viewing)
                .setMultiChoiceItems(new String[]{getString(R.string.type_gifs)},
                        new boolean[]{false}, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                    boolean isChecked) {
                                chosen[which] = isChecked;
                            }
                        })
                .setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        caching = new CommentCacheAsync(submissions, MainActivity.this, subreddit,
                                chosen).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                })
                .setPositiveButton(R.string.btn_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        new CommentCacheAsync(submissions, MainActivity.this, subreddit,
                                chosen).executeOnExecutor(service);
                    }
                })
                .show();

    }

    public void scrollToTop() {
        int[] firstVisibleItems;
        int pastVisiblesItems = 0;

        if (((adapter.getCurrentFragment()) == null)) return;
        firstVisibleItems =
                ((CatchStaggeredGridLayoutManager) (((SubmissionsView) adapter.getCurrentFragment()).rv
                        .getLayoutManager())).findFirstVisibleItemPositions(null);
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

    public void setDataSet(List<String> data) {
        if (data != null && !data.isEmpty()) {
            usedArray = new CaseInsensitiveArrayList(data);
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
                mTabLayout.setSelectedTabIndicatorColor(
                        new ColorPreferences(MainActivity.this).getColor(USEDARRAY_0));
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

    public void setDrawerSubList() {

        ArrayList<String> copy;

        if (NetworkUtil.isConnected(this)) {
            copy = new ArrayList<>(usedArray);
        } else {
            copy = UserSubscriptions.getAllUserSubreddits(this);
        }

        copy.removeAll(Arrays.asList("", null));

        sideArrayAdapter =
                new SideArrayAdapter(this, copy, UserSubscriptions.getAllSubreddits(this),
                        drawerSubList);
        drawerSubList.setAdapter(sideArrayAdapter);

        if ((SettingValues.subredditSearchMethod
                != Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR)) {
            drawerSearch = headerMain.findViewById(R.id.sort);
            drawerSearch.setVisibility(View.VISIBLE);

            drawerSubList.setFocusable(false);

            headerMain.findViewById(R.id.close_search_drawer)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            drawerSearch.setText("");
                        }
                    });

            drawerSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                        drawerSubList.smoothScrollToPositionFromTop(1, drawerSearch.getHeight(),
                                100);
                    } else {
                        getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    }
                }
            });
            drawerSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                    if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                        //If it the input text doesn't match a subreddit from the list exactly, openInSubView is true
                        if (sideArrayAdapter.fitems == null
                                || sideArrayAdapter.openInSubView
                                || !usedArray.contains(
                                drawerSearch.getText().toString().toLowerCase(Locale.ENGLISH))) {
                            Intent inte = new Intent(MainActivity.this, SubredditView.class);
                            inte.putExtra(SubredditView.EXTRA_SUBREDDIT,
                                    drawerSearch.getText().toString());
                            MainActivity.this.startActivityForResult(inte, 2001);
                        } else {
                            if (commentPager && adapter instanceof OverviewPagerAdapterComment) {
                                openingComments = null;
                                toOpenComments = -1;
                                ((MainActivity.OverviewPagerAdapterComment) adapter).size =
                                        (usedArray.size() + 1);
                                adapter.notifyDataSetChanged();
                                if (usedArray.contains(
                                        drawerSearch.getText().toString().toLowerCase(Locale.ENGLISH))) {
                                    doPageSelectedComments(usedArray.indexOf(
                                            drawerSearch.getText().toString().toLowerCase(Locale.ENGLISH)));
                                } else {
                                    doPageSelectedComments(
                                            usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                                }
                            }
                            if (usedArray.contains(
                                    drawerSearch.getText().toString().toLowerCase(Locale.ENGLISH))) {
                                pager.setCurrentItem(usedArray.indexOf(
                                        drawerSearch.getText().toString().toLowerCase(Locale.ENGLISH)));
                            } else {
                                pager.setCurrentItem(
                                        usedArray.indexOf(sideArrayAdapter.fitems.get(0)));
                            }
                            drawerLayout.closeDrawers();
                            drawerSearch.setText("");
                            View view = MainActivity.this.getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                        }
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
                drawerSearch.setOnClickListener(
                        null); //remove the touch listener on the drawer search field
                drawerSearch.setVisibility(View.GONE);
            }
        }
    }

    public void setToolbarClick() {
        if (mTabLayout != null) {
            mTabLayout.addOnTabSelectedListener(
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

    public void updateColor(int color, String subreddit) {
        hea.setBackgroundColor(color);
        header.setBackgroundColor(color);
        if (accountsArea != null) {
            accountsArea.setBackgroundColor(Palette.getDarkerColor(color));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(color));
        }
        setRecentBar(subreddit, color);
        findViewById(R.id.header_sub).setBackgroundColor(color);

    }

    public void updateMultiNameToSubs(Map<String, String> subs) {
        multiNameToSubsMap = subs;
    }

    public void updateSubs(ArrayList<String> subs) {
        if (subs.isEmpty() && !NetworkUtil.isConnected(this)) {
            findViewById(R.id.toolbar).setVisibility(View.GONE);
            d = new MaterialDialog.Builder(MainActivity.this).title(
                    R.string.offline_no_content_found)
                    .positiveText(R.string.offline_enter_online)
                    .negativeText(R.string.btn_close)
                    .cancelable(false)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                @NonNull DialogAction which) {
                            Reddit.appRestart.edit().remove("forceoffline").commit();
                            Reddit.forceRestart(MainActivity.this, false);
                        }
                    })
                    .show();
        } else {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (!getResources().getBoolean(R.bool.isTablet)) {
                setDrawerEdge(this, Constants.DRAWER_SWIPE_EDGE, drawerLayout);
            } else {
                setDrawerEdge(this, Constants.DRAWER_SWIPE_EDGE_TABLET, drawerLayout);
            }

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

        if (NetworkUtil.isConnected(MainActivity.this)) {
            if (Authentication.isLoggedIn
                    && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
                shortcuts.add(new ShortcutInfo.Builder(this, "inbox").setShortLabel("Inbox")
                        .setLongLabel("Open your Inbox")
                        .setIcon(getIcon("inbox", R.drawable.sidebar_inbox))
                        .setIntent(new Intent(Intent.ACTION_VIEW, new Uri.Builder().build(), this,
                                Inbox.class))
                        .build());

                shortcuts.add(new ShortcutInfo.Builder(this, "submit").setShortLabel("Submit")
                        .setLongLabel("Create new Submission")
                        .setIcon(getIcon("submit", R.drawable.edit))
                        .setIntent(new Intent(Intent.ACTION_VIEW, new Uri.Builder().build(), this,
                                Submit.class))
                        .build());

                int count = 0;

                for (String s : subs) {
                    if (count == 2 || count == subs.size()) {
                        break;
                    }
                    if (!s.contains("/m/")) {
                        Intent sub = new Intent(Intent.ACTION_VIEW, new Uri.Builder().build(), this,
                                SubredditView.class);
                        sub.putExtra(SubredditView.EXTRA_SUBREDDIT, s);
                        String frontpage = (s.equalsIgnoreCase("frontpage") ? "" : "/r/") + s;
                        shortcuts.add(new ShortcutInfo.Builder(this, "sub" + s).setShortLabel(
                                frontpage)
                                .setLongLabel(frontpage)
                                .setIcon(getIcon(s, R.drawable.sub))
                                .setIntent(sub)
                                .build());
                        count++;
                    }
                }

                Collections.reverse(shortcuts);

                shortcutManager.setDynamicShortcuts(shortcuts);
            } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
                int count = 0;
                for (String s : subs) {
                    if (count == 4 || count == subs.size()) {
                        break;
                    }
                    if (!s.contains("/m/")) {

                        Intent sub = new Intent(Intent.ACTION_VIEW, new Uri.Builder().build(), this,
                                SubredditView.class);
                        sub.putExtra(SubredditView.EXTRA_SUBREDDIT, s);
                        String frontpage = (s.equalsIgnoreCase("frontpage") ? "" : "/r/") + s;
                        new ShortcutInfo.Builder(this, "sub" + s).setShortLabel(
                                frontpage)
                                .setLongLabel(frontpage)
                                .setIcon(getIcon(s, R.drawable.sub))
                                .setIntent(sub)
                                .build();
                        count++;
                    }
                }

                Collections.reverse(shortcuts);

                shortcutManager.setDynamicShortcuts(shortcuts);
            }
        }
    }

    private Icon getIcon(String subreddit, @DrawableRes int overlay) {
        Bitmap color = Bitmap.createBitmap(toDp(this, 148), toDp(this, 148), Bitmap.Config.RGB_565);
        color.eraseColor(Palette.getColor(subreddit));
        color = clipToCircle(color);

        Bitmap over = drawableToBitmap(ResourcesCompat.getDrawable(getResources(), overlay, null));

        Canvas canvas = new Canvas(color);
        canvas.drawBitmap(over, color.getWidth() / 2 - (over.getWidth() / 2),
                color.getHeight() / 2 - (over.getHeight() / 2), null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Icon.createWithBitmap(color);
        }
        return null;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap =
                Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int toDp(Context context, int px) {
        return convert(context, px, TypedValue.COMPLEX_UNIT_PX);
    }

    private static int convert(Context context, int amount, int conversionUnit) {
        if (amount < 0) {
            throw new IllegalArgumentException("px should not be less than zero");
        }

        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(conversionUnit, amount, r.getDisplayMetrics());
    }

    public static Bitmap clipToCircle(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        final Path path = new Path();
        path.addCircle((float) (width / 2), (float) (height / 2),
                (float) Math.min(width, (height / 2)), Path.Direction.CCW);

        final Canvas canvas = new Canvas(outputBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
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

    private void changeSubscription(Subreddit subreddit, boolean isChecked) {
        currentlySubbed = isChecked;
        if (isChecked) {
            UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                    MainActivity.this);
        } else {
            UserSubscriptions.removeSubreddit(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH),
                    MainActivity.this);
            pager.setCurrentItem(pager.getCurrentItem() - 1);
            restartTheme();
        }
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

    private void dismissProgressDialog() {
        if (d != null && d.isShowing()) {
            d.dismiss();
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

    private void expand(LinearLayout v) {
        //set Visible
        v.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, v.getMeasuredHeight(), v);
        mAnimator.start();
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
                        mTabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mTabLayout.getTabAt(tabIndex).select();
                    }
                });
            }
        }
    }

    private void setViews(String rawHTML, String subredditName, SpoilerRobotoTextView firstTextView,
            CommentOverflow commentOverflow) {
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

    /**
     * If the user has the Subreddit Search method set to "long press on toolbar title", an
     * OnLongClickListener needs to be set for the toolbar as well as handling all of the relevant
     * onClicks for the views of the search bar.
     */
    private void setupSubredditSearchToolbar() {
        if (!NetworkUtil.isConnected(this)) {
            if (findViewById(R.id.drawer_divider) != null) {
                findViewById(R.id.drawer_divider).setVisibility(View.GONE);
            }
        } else {
            if ((SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                    || SettingValues.subredditSearchMethod
                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                    && usedArray != null
                    && !usedArray.isEmpty()) {
                if (findViewById(R.id.drawer_divider) != null) {
                    if (SettingValues.subredditSearchMethod
                            == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                        findViewById(R.id.drawer_divider).setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.drawer_divider).setVisibility(View.VISIBLE);
                    }
                }
                final ListView TOOLBAR_SEARCH_SUGGEST_LIST =
                        (ListView) findViewById(R.id.toolbar_search_suggestions_list);
                final ArrayList<String> subs_copy = new ArrayList<>(usedArray);
                final SideArrayAdapter TOOLBAR_SEARCH_SUGGEST_ADAPTER =
                        new SideArrayAdapter(this, subs_copy,
                                UserSubscriptions.getAllSubreddits(this),
                                TOOLBAR_SEARCH_SUGGEST_LIST);

                if (TOOLBAR_SEARCH_SUGGEST_LIST != null) {
                    TOOLBAR_SEARCH_SUGGEST_LIST.setAdapter(TOOLBAR_SEARCH_SUGGEST_ADAPTER);
                }

                if (mToolbar != null) {
                    mToolbar.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            final AutoCompleteTextView GO_TO_SUB_FIELD =
                                    (AutoCompleteTextView) findViewById(R.id.toolbar_search);
                            final ImageView CLOSE_BUTTON =
                                    (ImageView) findViewById(R.id.close_search_toolbar);
                            final CardView SUGGESTIONS_BACKGROUND =
                                    (CardView) findViewById(R.id.toolbar_search_suggestions);

                            //if the view mode is set to Subreddit Tabs, save the title ("Slide" or "Slide (debug)")
                            tabViewModeTitle =
                                    (!SettingValues.single) ? getSupportActionBar().getTitle()
                                            .toString() : null;

                            getSupportActionBar().setTitle(
                                    ""); //clear title to make room for search field

                            if (GO_TO_SUB_FIELD != null
                                    && CLOSE_BUTTON != null
                                    && SUGGESTIONS_BACKGROUND != null) {
                                GO_TO_SUB_FIELD.setVisibility(View.VISIBLE);
                                CLOSE_BUTTON.setVisibility(View.VISIBLE);
                                SUGGESTIONS_BACKGROUND.setVisibility(View.VISIBLE);

                                //run enter animations
                                enterAnimationsForToolbarSearch(ANIMATE_DURATION,
                                        SUGGESTIONS_BACKGROUND, GO_TO_SUB_FIELD, CLOSE_BUTTON);

                                //Get focus of the search field and show the keyboard
                                GO_TO_SUB_FIELD.requestFocus();
                                InputMethodManager imm = (InputMethodManager) getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                        InputMethodManager.HIDE_IMPLICIT_ONLY);


                                //Close the search UI and keyboard when clicking the close button
                                CLOSE_BUTTON.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final View view = MainActivity.this.getCurrentFocus();
                                        if (view != null) {
                                            //Hide the keyboard
                                            InputMethodManager imm =
                                                    (InputMethodManager) getSystemService(
                                                            Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }

                                        //run the exit animations
                                        exitAnimationsForToolbarSearch(ANIMATE_DURATION,
                                                SUGGESTIONS_BACKGROUND, GO_TO_SUB_FIELD,
                                                CLOSE_BUTTON);

                                        //clear sub text when close button is clicked
                                        GO_TO_SUB_FIELD.setText("");
                                    }
                                });

                                GO_TO_SUB_FIELD.setOnEditorActionListener(
                                        new TextView.OnEditorActionListener() {
                                            @Override
                                            public boolean onEditorAction(TextView arg0, int arg1,
                                                    KeyEvent arg2) {
                                                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                                                    //If it the input text doesn't match a subreddit from the list exactly, openInSubView is true
                                                    if (sideArrayAdapter.fitems == null
                                                            || sideArrayAdapter.openInSubView
                                                            || !usedArray.contains(
                                                            GO_TO_SUB_FIELD.getText()
                                                                    .toString()
                                                                    .toLowerCase(Locale.ENGLISH))) {
                                                        Intent intent =
                                                                new Intent(MainActivity.this,
                                                                        SubredditView.class);
                                                        intent.putExtra(
                                                                SubredditView.EXTRA_SUBREDDIT,
                                                                GO_TO_SUB_FIELD.getText()
                                                                        .toString());
                                                        MainActivity.this.startActivityForResult(
                                                                intent, 2002);
                                                    } else {
                                                        if (commentPager
                                                                && adapter instanceof OverviewPagerAdapterComment) {
                                                            openingComments = null;
                                                            toOpenComments = -1;
                                                            ((OverviewPagerAdapterComment) adapter).size =
                                                                    (usedArray.size() + 1);
                                                            adapter.notifyDataSetChanged();

                                                            if (usedArray.contains(
                                                                    GO_TO_SUB_FIELD.getText()
                                                                            .toString()
                                                                            .toLowerCase(Locale.ENGLISH))) {
                                                                doPageSelectedComments(
                                                                        usedArray.indexOf(
                                                                                GO_TO_SUB_FIELD.getText()
                                                                                        .toString()
                                                                                        .toLowerCase(Locale.ENGLISH)));
                                                            } else {
                                                                doPageSelectedComments(
                                                                        usedArray.indexOf(
                                                                                sideArrayAdapter.fitems
                                                                                        .get(0)));
                                                            }
                                                        }
                                                        if (usedArray.contains(
                                                                GO_TO_SUB_FIELD.getText()
                                                                        .toString()
                                                                        .toLowerCase(Locale.ENGLISH))) {
                                                            pager.setCurrentItem(usedArray.indexOf(
                                                                    GO_TO_SUB_FIELD.getText()
                                                                            .toString()
                                                                            .toLowerCase(Locale.ENGLISH)));
                                                        } else {
                                                            pager.setCurrentItem(usedArray.indexOf(
                                                                    sideArrayAdapter.fitems.get(
                                                                            0)));
                                                        }
                                                    }

                                                    View view = MainActivity.this.getCurrentFocus();
                                                    if (view != null) {
                                                        //Hide the keyboard
                                                        InputMethodManager imm =
                                                                (InputMethodManager) getSystemService(
                                                                        Context.INPUT_METHOD_SERVICE);
                                                        imm.hideSoftInputFromWindow(
                                                                view.getWindowToken(), 0);
                                                    }

                                                    SUGGESTIONS_BACKGROUND.setVisibility(View.GONE);
                                                    GO_TO_SUB_FIELD.setVisibility(View.GONE);
                                                    CLOSE_BUTTON.setVisibility(View.GONE);

                                                    if (SettingValues.single) {
                                                        getSupportActionBar().setTitle(selectedSub);
                                                    } else {
                                                        //Set the title back to "Slide" or "Slide (debug)"
                                                        getSupportActionBar().setTitle(
                                                                tabViewModeTitle);
                                                    }
                                                }
                                                return false;
                                            }
                                        });

                                GO_TO_SUB_FIELD.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence charSequence, int i,
                                            int i2, int i3) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence charSequence, int i,
                                            int i2, int i3) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable editable) {
                                        final String RESULT = GO_TO_SUB_FIELD.getText()
                                                .toString()
                                                .replaceAll(" ", "");
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
  /*Todo once API allows for getting the websocket URL  public class AsyncStartNotifSocket extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            try {
                String access = Authentication.authentication.getString("websocket_url", "");

                LogUtil.v(access);
                com.neovisionaries.ws.client.WebSocket ws = new WebSocketFactory().createSocket(access);
                ws.addListener(new WebSocketListener() {
                    @Override
                    public void onStateChanged(com.neovisionaries.ws.client.WebSocket websocket, WebSocketState newState) throws Exception {

                    }

                    @Override
                    public void onConnected(com.neovisionaries.ws.client.WebSocket websocket, Map<String, List<String>> headers) throws Exception {

                    }

                    @Override
                    public void onConnectError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                    }

                    @Override
                    public void onDisconnected(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

                    }

                    @Override
                    public void onFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onContinuationFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onTextFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onBinaryFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onCloseFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onPingFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onPongFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onTextMessage(com.neovisionaries.ws.client.WebSocket websocket, String s) throws Exception {
                        LogUtil.v("Recieved" + s);

                    }

                    @Override
                    public void onBinaryMessage(com.neovisionaries.ws.client.WebSocket websocket, byte[] binary) throws Exception {

                    }

                    @Override
                    public void onSendingFrame(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onFrameSent(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onFrameUnsent(com.neovisionaries.ws.client.WebSocket websocket, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                    }

                    @Override
                    public void onFrameError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onMessageError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

                    }

                    @Override
                    public void onMessageDecompressionError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

                    }

                    @Override
                    public void onTextMessageError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

                    }

                    @Override
                    public void onSendError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

                    }

                    @Override
                    public void onUnexpectedError(com.neovisionaries.ws.client.WebSocket websocket, WebSocketException cause) throws Exception {

                    }

                    @Override
                    public void handleCallbackError(com.neovisionaries.ws.client.WebSocket websocket, Throwable cause) throws Exception {

                    }

                    @Override
                    public void onSendingHandshake(com.neovisionaries.ws.client.WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

                    }
                });
                ws.connect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
            return null;
        }
    }*/

    public class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null) doSubOnlyStuff(subreddit);
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

    public class AsyncNotificationBadge extends AsyncTask<Void, Void, Void> {
        int count;

        boolean restart;
        int     modCount;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LoggedInAccount me;
                if (Authentication.me == null) {
                    Authentication.me = Authentication.reddit.me();
                    me = Authentication.me;
                    if (Authentication.name.equalsIgnoreCase("loggedout")) {
                        Authentication.name = me.getFullName();
                        Reddit.appRestart.edit().putString("name", Authentication.name).apply();
                        restart = true;
                        return null;
                    }
                    Authentication.mod = me.isMod();

                    Authentication.authentication.edit()
                            .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                            .apply();

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
                        final Set<String> accounts =
                                Authentication.authentication.getStringSet("accounts",
                                        new HashSet<String>());
                        if (accounts.contains(name)) { //convert to new system
                            accounts.remove(name);
                            accounts.add(name + ":" + Authentication.refresh);
                            Authentication.authentication.edit()
                                    .putStringSet("accounts", accounts)
                                    .commit(); //force commit
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
            if (restart) {
                restartTheme();
                return;
            }
            if (Authentication.mod && Authentication.didOnline) {
                RelativeLayout mod = headerMain.findViewById(R.id.mod);
                mod.setVisibility(View.VISIBLE);

                mod.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        if (modOf != null && !modOf.isEmpty()) {
                            Intent inte = new Intent(MainActivity.this, ModQueue.class);
                            MainActivity.this.startActivity(inte);
                        }
                    }
                });
            }
            if (count != -1) {
                int oldCount = Reddit.appRestart.getInt("inbox", 0);
                if (count > oldCount) {
                    final Snackbar s = Snackbar.make(mToolbar,
                            getResources().getQuantityString(R.plurals.new_messages,
                                    count - oldCount, count - oldCount), Snackbar.LENGTH_LONG)
                            .setAction(R.string.btn_view, new OnSingleClickListener() {
                                @Override
                                public void onSingleClick(View v) {
                                    Intent i = new Intent(MainActivity.this, Inbox.class);
                                    i.putExtra(Inbox.EXTRA_UNREAD, true);
                                    startActivity(i);
                                }
                            });

                    View view = s.getView();
                    TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
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
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(0);
            } else if (count != -1) {
                if (badge != null) {
                    badge.setVisibility(View.VISIBLE);
                }
                ((TextView) headerMain.findViewById(R.id.count)).setText(
                        String.format(Locale.getDefault(), "%d", count));
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

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        protected SubmissionsView mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        header.animate()
                                .translationY(0)
                                .setInterpolator(new LinearInterpolator())
                                .setDuration(180);
                        doSubSidebarNoLoad(usedArray.get(position));
                    }
                }

                @Override
                public void onPageSelected(final int position) {
                    Reddit.currentPosition = position;
                    selectedSub = usedArray.get(position);
                    SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();

                    if (hea != null) {
                        hea.setBackgroundColor(Palette.getColor(selectedSub));
                        if (accountsArea != null) {
                            accountsArea.setBackgroundColor(Palette.getDarkerColor(selectedSub));
                        }
                    }

                    int colorFrom = ((ColorDrawable) header.getBackground()).getColor();
                    int colorTo = Palette.getColor(selectedSub);

                    ValueAnimator colorAnimation =
                            ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);

                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            int color = (int) animator.getAnimatedValue();

                            header.setBackgroundColor(color);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                getWindow().setStatusBarColor(Palette.getDarkerColor(color));
                                if (SettingValues.colorNavBar) {
                                    getWindow().setNavigationBarColor(
                                            Palette.getDarkerColor(color));
                                }
                            }
                        }

                    });
                    colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    colorAnimation.setDuration(200);
                    colorAnimation.start();

                    setRecentBar(selectedSub);

                    if (SettingValues.single || mTabLayout == null) {
                        //Smooth out the fading animation for the toolbar subreddit search UI
                        if ((SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR
                                || SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                && findViewById(R.id.toolbar_search).getVisibility()
                                == View.VISIBLE) {
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
                                new ColorPreferences(MainActivity.this).getColor(selectedSub));
                    }
                    if (page != null && page.adapter != null) {
                        SubredditPosts p = page.adapter.dataSet;
                        if (p.offline && !isRestart) {
                            p.doMainActivityOffline(MainActivity.this, p.displayer);
                        }
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
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.size();
            }
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
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (reloadItemNumber == position || reloadItemNumber < 0) {
                super.setPrimaryItem(container, position, object);
                if (usedArray.size() >= position) doSetPrimary(object, position);
            } else {
                shouldLoad = usedArray.get(reloadItemNumber);
                if (multiNameToSubsMap.containsKey(usedArray.get(reloadItemNumber))) {
                    shouldLoad = multiNameToSubsMap.get(usedArray.get(reloadItemNumber));
                } else {
                    shouldLoad = usedArray.get(reloadItemNumber);
                }
            }
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null
                    && getCurrentFragment() != object
                    && position != toOpenComments
                    && object instanceof SubmissionsView) {
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

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
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
        public  Fragment    storedFragment;
        private CommentPage mCurrentComments;

        public OverviewPagerAdapterComment(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        if (position != toOpenComments) {
                            pager.setSwipeLeftOnly(true);
                            header.setBackgroundColor(Palette.getColor(usedArray.get(position)));
                            doPageSelectedComments(position);
                            if (position == toOpenComments - 1
                                    && adapter != null
                                    && adapter.getCurrentFragment() != null) {
                                SubmissionsView page =
                                        (SubmissionsView) adapter.getCurrentFragment();
                                if (page != null && page.adapter != null) {
                                    page.adapter.refreshView();
                                }
                            }
                        } else {
                            if (mAsyncGetSubreddit != null) {
                                mAsyncGetSubreddit.cancel(true);
                            }

                            if (header.getTranslationY() == 0) {
                                header.animate()
                                        .translationY(-header.getHeight() * 1.5f)
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
                public void onPageSelected(final int position) {
                    if (position == toOpenComments - 1
                            && adapter != null
                            && adapter.getCurrentFragment() != null) {
                        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
                        if (page != null && page.adapter != null) {
                            page.adapter.refreshView();
                            SubredditPosts p = page.adapter.dataSet;
                            if (p.offline && !isRestart) {
                                p.doMainActivityOffline(MainActivity.this, p.displayer);
                            }
                        }
                    } else {
                        SubmissionsView page = (SubmissionsView) adapter.getCurrentFragment();
                        if (page != null && page.adapter != null) {
                            SubredditPosts p = page.adapter.dataSet;
                            if (p.offline && !isRestart) {
                                p.doMainActivityOffline(MainActivity.this, p.displayer);
                            }
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            notifyDataSetChanged();
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
                args.putBoolean("contest",
                        openingComments.getDataNode().get("contest_mode").asBoolean());
                args.putBoolean("locked", openingComments.isLocked());
                args.putInt("page", currentComment);
                args.putString("subreddit", openingComments.getSubredditName());
                args.putString("baseSubreddit", subToDo);
                f.setArguments(args);
                return f;
            }


        }

        @Override
        public Parcelable saveState() {
            return null;
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
                    if (mCurrentFragment != null
                            && mCurrentFragment.posts == null
                            && mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }
                }
            } else if (object instanceof CommentPage) {
                mCurrentComments = (CommentPage) object;
            }

        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object != storedFragment) return POSITION_NONE;
            return POSITION_UNCHANGED;
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
}
