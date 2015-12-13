
package me.ccrama.redditslide;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ccrama.redditslide.Activities.Crash;
import me.ccrama.redditslide.Activities.Internet;
import me.ccrama.redditslide.Activities.LoadingData;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static final long BACKGROUND_DELAY = 500;
    public static IabHelper mHelper;
    public static boolean single;
    public static boolean animation;
    public static boolean swap;
    public static boolean album;
    public static boolean cache;
    public static boolean expandedSettings;

    public static SubmissionSearchPaginator.SearchSort search = SubmissionSearchPaginator.SearchSort.RELEVANCE;

    public static boolean cacheDefault;

    public static boolean image;
    public static boolean video;
    public static final long enter_animation_time_original = 600;
    public static long enter_animation_time = enter_animation_time_original;
    public static int enter_animation_time_multiplier = 1;


    public static boolean fullscreen;
    boolean firstStart = false;
    public static boolean gif;
    public static boolean web;
    public static boolean exit;
    public static boolean fastscroll;
    public static boolean fab = true;
    public static int fabType = R.integer.FAB_POST;
    public static boolean click_user_name_to_profile = true;
    public static boolean hideButton;
    public static Authentication authentication;
    public static boolean tabletUI;
    public static Sorting defaultSorting;

    public static CommentSort defaultCommentSorting;
    public static TimePeriod timePeriod;

    public static SharedPreferences colors;
    public static SharedPreferences appRestart;
    public static int nighttime;
    public static int daytime;
    public static boolean autoTime;

    public static int themeBack;
    public static int dpWidth;
    public static int notificationTime;
    public static NotificationJobScheduler notifications;
    public static SharedPreferences seen;
    public static SharedPreferences hidden;
    public static boolean isLoading = false;
    private final List<Listener> listeners = new ArrayList<Listener>();
    private final Handler mBackgroundDelayHandler = new Handler();
    public boolean active;
    public LoadingData loader;
    private ImageLoader defaultImageLoader;
    private boolean closed = false;
    public boolean mInBackground = true;
    private Runnable mBackgroundTransition;
    public static long time = System.currentTimeMillis();
    private boolean isRestarting;
    public static boolean fabClear;
    public static ArrayList<Integer> lastposition;
    public static int currentPosition;

    public static void forceRestart(Context context) {
        Intent mStartActivity = new Intent(context, LoadingData.class);
        int mPendingIntentId = 654321;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void defaultShareText(String url, Context c) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        /* Decode html entities */
        url = StringEscapeUtils.unescapeHtml4(url);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, c.getString(R.string.title_share)));
    }


    public static void defaultShare(String url, Context c) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }

    private static boolean isPackageInstalled(final Context ctx) {
        boolean result = false;
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("me.ccrama.slideforreddittabletuiunlock", 0);
            if (pi != null && pi.applicationInfo.enabled)
                result = true;
        } catch (final Throwable ignored) {
        }

        return result;
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        getImageLoader().clearMemoryCache();
    }

    public ImageLoader getImageLoader() {
        if (defaultImageLoader == null || !defaultImageLoader.isInited()) {
            ImageLoaderUtils.initImageLoader(getApplicationContext());
            defaultImageLoader = ImageLoader.getInstance();
        }

        return defaultImageLoader;
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isInBackground() {
        return mInBackground;
    }

    boolean hasDone;
    boolean hasDone2;

    @Override
    public void onActivityResumed(Activity activity) {

        if (mBackgroundTransition != null) {
            mBackgroundDelayHandler.removeCallbacks(mBackgroundTransition);
            mBackgroundTransition = null;
        }

        if (mInBackground) {
            mInBackground = false;
            notifyOnBecameForeground();

            if (hasDone && hasDone2) {
                loader = null;

                authentication.updateToken(activity);
            } else if (authentication == null) {
                loader = null;
                authentication = new Authentication(this);


            } else if (hasDone) {
                hasDone2 = true;
            } else {
                hasDone = true;
            }

        }

    }

    private void notifyOnBecameForeground() {
        for (Listener listener : listeners) {
            try {
                listener.onBecameForeground();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (!mInBackground && mBackgroundTransition == null) {
            mBackgroundTransition = new Runnable() {
                @Override
                public void run() {
                    mInBackground = true;
                    mBackgroundTransition = null;
                    notifyOnBecameBackground();
                }
            };
            mBackgroundDelayHandler.postDelayed(mBackgroundTransition, BACKGROUND_DELAY);
        }
    }

    private void notifyOnBecameBackground() {
        for (Listener listener : listeners) {
            try {
                listener.onBecameBackground();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.v("Slide", "ON CREATED AGAIN");
        appRestart = getSharedPreferences("appRestart", 0);


        registerActivityLifecycleCallbacks(this);
        Authentication.authentication = getSharedPreferences("AUTH", 0);
        SubredditStorage.subscriptions = getSharedPreferences("SUBS", 0);
        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        defaultSorting = SettingValues.defaultSorting;
        timePeriod = SettingValues.timePeriod;
        defaultCommentSorting = SettingValues.defaultCommentSorting;
        colors = getSharedPreferences("COLOR", 0);
        seen = getSharedPreferences("SEEN", 0);
        hidden = getSharedPreferences("HIDDEN", 0);
        lastposition = new ArrayList<>();
        Hidden.hidden = getSharedPreferences("HIDDEN_POSTS", 0);


        //START code adapted from https://github.com/QuantumBadger/RedReader/
        final Thread.UncaughtExceptionHandler androidHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable t) {

                if (t instanceof UnknownHostException) {
                    Intent i = new Intent(Reddit.this, Internet.class);
                    startActivity(i);
                } else {
                    try {
                        Writer writer = new StringWriter();
                        PrintWriter printWriter = new PrintWriter(writer);
                        t.printStackTrace(printWriter);
                        String s = writer.toString();
                        s = s.replace(";", ",");
                        Intent i = new Intent(Reddit.this, Crash.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("stacktrace", s);

                        startActivity(i);
                    } catch (Throwable ignored) {
                    }
                }

                androidHandler.uncaughtException(thread, t);
            }
        });
        //END adaptation
        new SetupIAB().execute();


        if (!seen.contains("RESET")) {
            colors.edit().clear().apply();
            seen.edit().clear().apply();
            hidden.edit().clear().apply();
            Hidden.hidden.edit().clear().apply();

            Authentication.authentication.edit().clear().apply();
            SubredditStorage.subscriptions.edit().clear().apply();
            getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().clear().apply();
            Authentication.authentication = getSharedPreferences("AUTH", 0);
            SubredditStorage.subscriptions = getSharedPreferences("SUBS", 0);


            SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
            colors = getSharedPreferences("COLOR", 0);
            seen = getSharedPreferences("SEEN", 0);
            hidden = getSharedPreferences("HIDDEN", 0);
            seen.edit().putBoolean("RESET", true).apply();
            Hidden.hidden = getSharedPreferences("HIDDEN_POSTS", 0);


        }

        single = SettingValues.prefs.getBoolean("Single", false);
        animation = SettingValues.prefs.getBoolean("Animation", false);
        enter_animation_time_multiplier = SettingValues.prefs.getInt("AnimationLengthMultiplier", 1);
        enter_animation_time = enter_animation_time_original * enter_animation_time_multiplier;
        fullscreen = SettingValues.prefs.getBoolean("Fullscreen", false);
        fab = SettingValues.prefs.getBoolean("Fab", false);
        fabType = SettingValues.prefs.getInt("FabType", R.integer.FAB_POST);
        nighttime = SettingValues.prefs.getInt("day", 20);
        daytime = SettingValues.prefs.getInt("night", 6);
        autoTime = SettingValues.prefs.getBoolean("autotime", false);

        click_user_name_to_profile = SettingValues.prefs.getBoolean("UsernameClick", true);
        swap = SettingValues.prefs.getBoolean("Swap", false);
        web = SettingValues.prefs.getBoolean("web", true);
        image = SettingValues.prefs.getBoolean("image", true);
        cache = SettingValues.prefs.getBoolean("cache", true);
        cacheDefault = SettingValues.prefs.getBoolean("cacheDefault", false);
        expandedSettings = SettingValues.prefs.getBoolean("expandedSettings", false);

        album = SettingValues.prefs.getBoolean("album", true);
        gif = SettingValues.prefs.getBoolean("gif", true);
        video = SettingValues.prefs.getBoolean("video", true);
        exit = SettingValues.prefs.getBoolean("Exit", true);
        fastscroll = SettingValues.prefs.getBoolean("Fastscroll", false);
        fabClear = seen.getBoolean("fabClear", false);
        hideButton = SettingValues.prefs.getBoolean("Hidebutton", false);

        int height = this.getResources().getConfiguration().screenWidthDp;

        int width = this.getResources().getConfiguration().screenHeightDp;

        int fina;
        if (height > width) {
            fina = height;
        } else {
            fina = width;
        }
        fina = ((fina + 99) / 100) * 100;
        themeBack = new ColorPreferences(this).getFontStyle().getThemeType();

        if (seen.contains("tabletOVERRIDE")) {
            dpWidth = seen.getInt("tabletOVERRIDE", fina / 300);
        } else {
            dpWidth = fina / 300;
        }
        if (seen.contains("notificationOverride")) {
            notificationTime = seen.getInt("notificationOverride", 15);
        } else {
            notificationTime = 15;
        }
        int defaultDPWidth = fina / 300;
        authentication = new Authentication(this);
        if (appRestart.contains("back")) {
            SubredditStorage.subredditsForHome = stringToArray(appRestart.getString("subs", ""));
            SubredditStorage.alphabeticalSubscriptions = stringToArray(appRestart.getString("subsalph", ""));
            SubredditStorage.realSubs = stringToArray(appRestart.getString("real", ""));
            Authentication.isLoggedIn = appRestart.getBoolean("loggedin", false);
            Authentication.name = appRestart.getString("name", "");
            active = true;
            startMain();
        }

        tabletUI = isPackageInstalled(this);

    }

    public static String arrayToString(ArrayList<String> array) {
        StringBuilder b = new StringBuilder();
        for (String s : array) {
            b.append(s + ",");

        }
        String f = b.toString();
        if (f.length() > 0) {
            f = f.substring(0, f.length() - 1);
        }
        return f;
    }

    public static ArrayList<String> stringToArray(String string) {
        ArrayList<String> f = new ArrayList<>();
        Collections.addAll(f, string.split(","));
        return f;
    }

    public void startMain() {
        if (active) {
            Intent i = new Intent(this, MainActivity.class);
            Log.v("Slide", "starting new");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);

            if (loader != null) {
                loader.finish();
                loader = null;
            }
        }
    }

    public void restart() {
        isRestarting = true;

        Intent mStartActivity = new Intent(this, LoadingData.class);
        mStartActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        mStartActivity.putExtra("EXIT", true);
        this.startActivity(mStartActivity);
        onCreate();


    }

    public interface Listener {
        public void onBecameForeground();

        public void onBecameBackground();
    }

    private class SetupIAB extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mHelper = new IabHelper(Reddit.this, SecretConstants.base64EncodedPublicKey);
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        if (!result.isSuccess()) {
                            Log.d("Slide", "Problem setting up In-app Billing: " + result);
                        }

                    }
                });
            } catch (Exception ignored) {

            }
            return null;
        }
    }

    public static Integer getSortingId() {
        return defaultSorting == Sorting.HOT ? 0
                : defaultSorting == Sorting.NEW ? 1
                : defaultSorting == Sorting.RISING ? 2
                : defaultSorting == Sorting.TOP ?
                (timePeriod == TimePeriod.HOUR ? 3
                        : timePeriod == TimePeriod.DAY ? 4
                        : timePeriod == TimePeriod.WEEK ? 5
                        : timePeriod == TimePeriod.MONTH ? 6
                        : timePeriod == TimePeriod.YEAR ? 7
                        : 8)
                : defaultSorting == Sorting.CONTROVERSIAL ?
                (timePeriod == TimePeriod.HOUR ? 9
                        : timePeriod == TimePeriod.DAY ? 10
                        : timePeriod == TimePeriod.WEEK ? 11
                        : timePeriod == TimePeriod.MONTH ? 12
                        : timePeriod == TimePeriod.YEAR ? 13
                        : 14)
                : 0;
    }

    public static Integer getSortingIdSearch() {
        return
                timePeriod == TimePeriod.HOUR ? 0 :
                        timePeriod == TimePeriod.DAY ? 1 :
                        timePeriod == TimePeriod.WEEK ? 2 :
                                timePeriod == TimePeriod.MONTH ? 3 :
                                        timePeriod == TimePeriod.YEAR ? 4 :
                                                5
                ;
    }
    public static Integer getTypeSearch() {
        return
                search == SubmissionSearchPaginator.SearchSort.RELEVANCE ? 0 :
                        search == SubmissionSearchPaginator.SearchSort.TOP ? 1 :
                                search == SubmissionSearchPaginator.SearchSort.NEW ? 2 :
                                      3
                ;
    }

    public static String[] getSortingStrings(Context c) {
        return new String[]
                {c.getString(R.string.sorting_hot),
                        c.getString(R.string.sorting_new),
                        c.getString(R.string.sorting_rising),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_hour),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_day),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_week),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_month),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_year),
                        c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_all),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_hour),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_day),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_week),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_month),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_year),
                        c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_all),
                };
    }
    public static String[] getSearch(Context c) {
        return new String[]
                {
                        "Relevance",
                        "Top",
                        "New",
                        "Comments"
                };
    }

    public static String[] getSortingStringsSearch(Context c) {
        return new String[]
                {
                        WordUtils.capitalize(c.getString(R.string.sorting_hour)),
                        WordUtils.capitalize(c.getString(R.string.sorting_day)),
                        WordUtils.capitalize(c.getString(R.string.sorting_week)),
                        WordUtils.capitalize(c.getString(R.string.sorting_month)),
                        WordUtils.capitalize(c.getString(R.string.sorting_year)),
                        WordUtils.capitalize(c.getString(R.string.sorting_all)),

                };
    }


}
