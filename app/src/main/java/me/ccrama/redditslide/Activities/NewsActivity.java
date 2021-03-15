package me.ccrama.redditslide.Activities;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import net.dean.jraw.managers.AccountManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Adapters.SubredditPostsRealm;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.Fragments.NewsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask;
import me.ccrama.redditslide.Synccit.SynccitRead;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LayoutUtils;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkStateReceiver;
import me.ccrama.redditslide.util.NetworkUtil;

public class NewsActivity extends BaseActivity
        implements NetworkStateReceiver.NetworkStateReceiverListener {
    public static final  String IS_ONLINE     = "online";
    // Instance state keys
    static final         String SUBS          = "news";
    private static final String EXTRA_PAGE_TO = "PAGE_TO";
    public View header;

    public static Loader loader;
    public static Map<String, String> newsSubToMap            = new HashMap<>();
    public final  long                ANIMATE_DURATION        = 250; //duration of animations
    private final long                ANIMATE_DURATION_OFFSET = 45;
    //offset for smoothing out the exit animations
    public ToggleSwipeViewPager     pager;
    public CaseInsensitiveArrayList usedArray;
    public NewsPagerAdapter adapter;
    public TabLayout                mTabLayout;
    public String                   selectedSub; //currently selected subreddit
    public boolean                  inNightMode;
    boolean changed;
    boolean currentlySubbed;
    int     back;
    private int headerHeight; //height of the header
    public int reloadItemNumber = -2;

    @Override
    public void onBackPressed() {
        finish();
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
                && Authentication.me.hasGold()
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
                        new AlertDialog.Builder(NewsActivity.this).setTitle(
                                R.string.err_permission)
                                .setMessage(R.string.err_permission_msg)
                                .setPositiveButton(R.string.btn_yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                        int which) {
                                                ActivityCompat.requestPermissions(
                                                        NewsActivity.this, new String[]{
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
    protected void onCreate(final Bundle savedInstanceState) {
        inNightMode = SettingValues.isNight();
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);

        applyColorTheme();

        setContentView(R.layout.activity_news);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(
                    Palette.getDarkerColor(Palette.getDarkerColor(Palette.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);

        pager = (ToggleSwipeViewPager)

                findViewById(R.id.content_view);

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        UserSubscriptions.doNewsSubs(NewsActivity.this);
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
    }

    @Override
    public void networkAvailable() {
    }

    NetworkStateReceiver networkStateReceiver;

    @Override
    public void networkUnavailable() {
    }

    @Override
    public void onResume() {
        super.onResume();

        if (inNightMode != SettingValues.isNight()) {
            restartTheme();
        }

        Reddit.setDefaultErrorHandler(this);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(networkStateReceiver);
        } catch (Exception ignored) {

        }
        Slide.hasStarted = false;
        super.onDestroy();
    }

    public static String abbreviate(final String str, final int maxWidth) {
        if (str.length() <= maxWidth) {
            return str;
        }

        final String abrevMarker = "...";
        return str.substring(0, maxWidth - 3) + abrevMarker;
    }

    String shouldLoad;


    public void restartTheme() {
        Intent intent = this.getIntent();
        int page = pager.getCurrentItem();
        intent.putExtra(EXTRA_PAGE_TO, page);
        finish();
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
    }

    public void scrollToTop() {
        int pastVisiblesItems = 0;

        if (((adapter.getCurrentFragment()) == null)) return;
        int[] firstVisibleItems =
                ((CatchStaggeredGridLayoutManager) (((NewsView) adapter.getCurrentFragment()).rv.getLayoutManager()))
                        .findFirstVisibleItemPositions(null);
        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
            for (int firstVisibleItem : firstVisibleItems) {
                pastVisiblesItems = firstVisibleItem;
            }
        }
        if (pastVisiblesItems > 8) {
            ((NewsView) adapter.getCurrentFragment()).rv.scrollToPosition(0);
            header.animate()
                    .translationY(header.getHeight())
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(0);
        } else {
            ((NewsView) adapter.getCurrentFragment()).rv.smoothScrollToPosition(0);
        }
        ((NewsView) adapter.getCurrentFragment()).resetScroll();
    }

    int toGoto;

    public void setDataSet(List<String> data) {
        if (data != null && !data.isEmpty()) {
            usedArray = new CaseInsensitiveArrayList(data);
            if (adapter == null) {
                adapter = new NewsPagerAdapter(getSupportFragmentManager());
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

            mTabLayout.setSelectedTabIndicatorColor(
                    new ColorPreferences(NewsActivity.this).getColor(USEDARRAY_0));
            pager.setCurrentItem(toGoto);
            mTabLayout.setupWithViewPager(pager);
            if (mTabLayout != null) {
                mTabLayout.setupWithViewPager(pager);
                LayoutUtils.scrollToTabAfterLayout(mTabLayout, toGoto);
            }
            setToolbarClick();
        } else if (NetworkUtil.isConnected(this)) {
            UserSubscriptions.doNewsSubs(this);
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


    public void updateMultiNameToSubs(Map<String, String> subs) {
        newsSubToMap = subs;
    }

    public void updateSubs(ArrayList<String> subs) {
        if (subs.isEmpty() && !NetworkUtil.isConnected(this)) {
            //todo this
        } else {

            if (loader != null) {
                header.setVisibility(View.VISIBLE);

                setDataSet(subs);
                try {
                    setDataSet(subs);
                } catch (Exception ignored) {

                }
                loader.finish();
                loader = null;
            } else {
                setDataSet(subs);
            }
        }
    }

    private class NewsPagerAdapter extends FragmentStatePagerAdapter {
        protected NewsView mCurrentFragment;

        NewsPagerAdapter(FragmentManager fm) {
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
                    }
                }

                @Override
                public void onPageSelected(final int position) {
                    Reddit.currentPosition = position;
                    selectedSub = usedArray.get(position);
                    NewsView page = (NewsView) adapter.getCurrentFragment();

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

                    mTabLayout.setSelectedTabIndicatorColor(
                            new ColorPreferences(NewsActivity.this).getColor(selectedSub));
                    if (page != null && page.adapter != null) {
                        SubredditPostsRealm p = page.adapter.dataSet;
                        if (p.offline) {
                            p.doNewsActivityOffline(NewsActivity.this, p.displayer);
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

        @NonNull
        @Override
        public Fragment getItem(int i) {
            NewsView f = new NewsView();
            Bundle args = new Bundle();
            String name;
            if (newsSubToMap.containsKey(usedArray.get(i))) {
                name = newsSubToMap.get(usedArray.get(i));
            } else {
                name = usedArray.get(i);
            }
            args.putString("id", name);
            f.setArguments(args);

            return f;
        }

        @Override
        public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            if (reloadItemNumber == position || reloadItemNumber < 0) {
                super.setPrimaryItem(container, position, object);
                if (usedArray.size() >= position) doSetPrimary(object, position);
            } else {
                shouldLoad = usedArray.get(reloadItemNumber);
                if (newsSubToMap.containsKey(usedArray.get(reloadItemNumber))) {
                    shouldLoad = newsSubToMap.get(usedArray.get(reloadItemNumber));
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
            if (object != null && getCurrentFragment() != object && object instanceof NewsView) {
                shouldLoad = usedArray.get(position);
                if (newsSubToMap.containsKey(usedArray.get(position))) {
                    shouldLoad = newsSubToMap.get(usedArray.get(position));
                } else {
                    shouldLoad = usedArray.get(position);
                }

                mCurrentFragment = ((NewsView) object);
                if (mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

                }
            }
        }

        Fragment getCurrentFragment() {
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
}
