package me.ccrama.redditslide;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;
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
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.ccrama.redditslide.Activities.Internet;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.util.AlbumUtils;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static final String EMPTY_STRING = "NOTHING";
    public static final long BACKGROUND_DELAY = 500;

    public static final long enter_animation_time_original = 600;
    public static final String PREF_LAYOUT = "PRESET";

    public static IabHelper mHelper;
    public static SubmissionSearchPaginator.SearchSort search = SubmissionSearchPaginator.SearchSort.RELEVANCE;
    public static long enter_animation_time = enter_animation_time_original;
    public static int enter_animation_time_multiplier = 1;

    public static Authentication authentication;

    public static Sorting defaultSorting;

    public static CommentSort defaultCommentSorting;
    public static TimePeriod timePeriod;
    public static SharedPreferences colors;
    public static SharedPreferences appRestart;

    public static int dpWidth;
    public static int notificationTime;
    public static NotificationJobScheduler notifications;
    public static SharedPreferences seen;
    public static SharedPreferences hidden;
    public static boolean isLoading = false;
    public static long time = System.currentTimeMillis();
    public static boolean fabClear;
    public static ArrayList<Integer> lastposition;
    public static int currentPosition;
    public static int themeBack;
    public static SharedPreferences cachedData;
    private final List<Listener> listeners = new ArrayList<>();
    private final Handler mBackgroundDelayHandler = new Handler();
    public boolean active;
    public boolean mInBackground = true;
    boolean firstStart = false;
    boolean hasDone;
    boolean hasDone2;
    private ImageLoader defaultImageLoader;
    private boolean closed = false;
    private Runnable mBackgroundTransition;
    public static boolean isRestarting;

    public static void forceRestart(Context context) {
        if (appRestart.contains("back")) {
            appRestart.edit().remove("back").commit();
        }

        isRestarting = true;

        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public static void forceRestart(Context c, boolean forceLoadScreen) {
        appRestart.edit().putString("startScreen", "").commit();
        forceRestart(c);

    }

    public static int pxToDp(int dp, Context c) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
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
    public static boolean isPackageInstalled(final Context ctx, String s) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(s, 0);
            if (pi != null && pi.applicationInfo.enabled)
                return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }
    private static boolean isPackageInstalled(final Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("me.ccrama.slideforreddittabletuiunlock", 0);
            if (pi != null && pi.applicationInfo.enabled)
                return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }


    public static String arrayToString(ArrayList<String> array) {
        if (array != null) {
            StringBuilder b = new StringBuilder();
            for (String s : array) {
                b.append(s).append(",");


            }
            String f = b.toString();
            if (f.length() > 0) {
                f = f.substring(0, f.length() - 1);
            }

            return f;
        } else {
            return "";
        }
    }

    public static ArrayList<String> stringToArray(String string) {
        ArrayList<String> f = new ArrayList<>();
        Collections.addAll(f, string.split(","));
        return f;
    }

    public static Integer getSortingId(String subreddit) {
        Sorting sort = sorting.containsKey(subreddit) ? sorting.get(subreddit) : Reddit.defaultSorting;
        TimePeriod time = sorting.containsKey(subreddit) ? times.get(subreddit) : Reddit.timePeriod;

        return sort == Sorting.HOT ? 0
                : sort == Sorting.NEW ? 1
                : sort == Sorting.RISING ? 2
                : sort == Sorting.TOP ?
                (time == TimePeriod.HOUR ? 3
                        : time == TimePeriod.DAY ? 4
                        : time == TimePeriod.WEEK ? 5
                        : time == TimePeriod.MONTH ? 6
                        : time == TimePeriod.YEAR ? 7
                        : 8)
                : sort == Sorting.CONTROVERSIAL ?
                (time == TimePeriod.HOUR ? 9
                        : time == TimePeriod.DAY ? 10
                        : time == TimePeriod.WEEK ? 11
                        : time == TimePeriod.MONTH ? 12
                        : time == TimePeriod.YEAR ? 13
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

    public static Integer getSortingIdSearch(Search s) {
        return
                s.time == TimePeriod.HOUR ? 0 :
                        s.time == TimePeriod.DAY ? 1 :
                                s.time == TimePeriod.WEEK ? 2 :
                                        s.time == TimePeriod.MONTH ? 3 :
                                                s.time == TimePeriod.YEAR ? 4 :
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        getImageLoader().clearMemoryCache();
    }

    public ImageLoader getImageLoader() {
        if (defaultImageLoader == null || !defaultImageLoader.isInited()) {
            ImageLoaderUtils.initImageLoader(getApplicationContext());
            defaultImageLoader = ImageLoaderUtils.imageLoader;
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

                authentication.updateToken(activity);
            } else if (authentication == null) {
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

    public static void setDefaultErrorHandler(Context base) {
        //START code adapted from https://github.com/QuantumBadger/RedReader/
        final Thread.UncaughtExceptionHandler androidHandler = Thread.getDefaultUncaughtExceptionHandler();
        final WeakReference<Context> cont = new WeakReference<>(base);


        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable t) {
                if (cont.get() != null) {
                    final Context c = cont.get();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    t.printStackTrace(printWriter);
                    String stacktrace = writer.toString().replace(";", ",");
                    if (stacktrace.contains("UnknownHostException") || stacktrace.contains("SocketTimeoutException") || stacktrace.contains("ConnectException")) {
                        //is offline
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(c).setTitle("Uh oh, an error occured")
                                        .setMessage("The connection to Reddit failed. Please check your internet connection and try again, or enter offline mode.")
                                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!(c instanceof MainActivity)) {
                                                    ((Activity) c).finish();
                                                }
                                            }
                                        }).setPositiveButton("Enter offline mode", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        forceRestart(c);
                                    }
                                }).show();
                            }
                        });
                    } else if (stacktrace.contains("403 Forbidden") || stacktrace.contains("401 Unauthorized")) {
                        //Un-authenticated
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(c).setTitle("Uh oh, an error occured")
                                        .setMessage("Reddit refused a request. Would you like to attempt to re-connect to Reddit?")
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!(c instanceof MainActivity)) {
                                                    ((Activity) c).finish();
                                                }
                                            }
                                        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        authentication.updateToken((c));
                                    }
                                }).show();
                            }
                        });

                    } else if (stacktrace.contains("404 Not Found") || stacktrace.contains("400 Bad Request")) {
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialogWrapper.Builder(c).setTitle("Uh oh, an error occured")
                                        .setMessage("Reddit could not find the requested content.")
                                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!(c instanceof MainActivity)) {
                                                    ((Activity) c).finish();
                                                }
                                            }

                                        }).show();
                            }
                        });
                    } else {
                        appRestart.edit().putString("startScreen", "a").apply(); //Force reload of data after crash incase state was not saved

                        if (t instanceof UnknownHostException) {
                            Intent i = new Intent(c, Internet.class);
                            c.startActivity(i);
                        } else {
                            try {

                                SharedPreferences prefs = c.getSharedPreferences(
                                        "STACKTRACE", Context.MODE_PRIVATE);
                                prefs.edit().putString("stacktrace", stacktrace).apply();

                            } catch (Throwable ignored) {
                            }
                        }

                        androidHandler.uncaughtException(thread, t);
                    }
                } else {
                    androidHandler.uncaughtException(thread, t);
                }
            }
        });
        //END adaptation

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
        //  LeakCanary.install(this);

        doMainStuff();
    }

    public void doMainStuff() {
        Log.v(LogUtil.getTag(), "ON CREATED AGAIN");
        appRestart = getSharedPreferences("appRestart", 0);
        AlbumUtils.albumRequests = getSharedPreferences("albums", 0);

        cachedData = getSharedPreferences("cache", 0);

        registerActivityLifecycleCallbacks(this);
        Authentication.authentication = getSharedPreferences("AUTH", 0);
        SubredditStorage.subscriptions = getSharedPreferences("SUBSNEW", 0);
        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        defaultSorting = SettingValues.defaultSorting;
        timePeriod = SettingValues.timePeriod;
        defaultCommentSorting = SettingValues.defaultCommentSorting;
        colors = getSharedPreferences("COLOR", 0);
        seen = getSharedPreferences("SEEN", 0);
        hidden = getSharedPreferences("HIDDEN", 0);
        lastposition = new ArrayList<>();
        Hidden.hidden = getSharedPreferences("HIDDEN_POSTS", 0);


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
            SubredditStorage.subscriptions = getSharedPreferences("SUBSNEW", 0);


            SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
            colors = getSharedPreferences("COLOR", 0);
            seen = getSharedPreferences("SEEN", 0);
            hidden = getSharedPreferences("HIDDEN", 0);
            seen.edit().putBoolean("RESET", true).apply();
            Hidden.hidden = getSharedPreferences("HIDDEN_POSTS", 0);


        }
        enter_animation_time = enter_animation_time_original * enter_animation_time_multiplier;

        fabClear = seen.getBoolean(SettingValues.PREF_FAB_CLEAR, false);

        int widthDp = this.getResources().getConfiguration().screenWidthDp;
        int heightDp = this.getResources().getConfiguration().screenHeightDp;

        int fina;
        if (widthDp > heightDp) {
            fina = widthDp;
        } else {
            fina = heightDp;
        }
        fina = ((fina + 99) / 100) * 100;

        themeBack = new ColorPreferences(this).getFontStyle().getThemeType();

        if (seen.contains("tabletOVERRIDE")) {
            dpWidth = seen.getInt("tabletOVERRIDE", fina / 300);
        } else {
            dpWidth = fina / 300;
        }
        if (seen.contains("notificationOverride")) {
            notificationTime = seen.getInt("notificationOverride", 360);
        } else {
            notificationTime = 360;
        }
        int defaultDPWidth = fina / 300;
        authentication = new Authentication(this);

        if (!appRestart.contains("startScreen")) {
            SubredditStorage.subredditsForHome = stringToArray(appRestart.getString("subs", ""));
            Authentication.isLoggedIn = appRestart.getBoolean("loggedin", false);
            Authentication.name = appRestart.getString("name", "");
            active = true;
        } else {
            appRestart.edit().remove("startScreen").apply();
        }

        SettingValues.tabletUI = isPackageInstalled(this);


    }

    public static void setSorting(String s, Sorting sort) {
        sorting.put(s, sort);
    }

    public static HashMap<String, Sorting> sorting = new HashMap<>();

    public static Sorting getSorting(String subreddit) {
        if (sorting.containsKey(subreddit)) {
            return sorting.get(subreddit);
        } else {
            return defaultSorting;
        }
    }


    public static void setTime(String s, TimePeriod sort) {
        times.put(s, sort);
    }

    public static HashMap<String, TimePeriod> times = new HashMap<>();

    public static TimePeriod getTime(String subreddit) {
        if (times.containsKey(subreddit)) {
            return times.get(subreddit);
        } else {
            return Reddit.timePeriod;
        }
    }

    public interface Listener {
        void onBecameForeground();

        void onBecameBackground();
    }

    private class SetupIAB extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mHelper = new IabHelper(Reddit.this, SecretConstants.getBase64EncodedPublicKey(getBaseContext()));
                mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    public void onIabSetupFinished(IabResult result) {
                        if (!result.isSuccess()) {
                            Log.d(LogUtil.getTag(), "Problem setting up In-app Billing: " + result);
                        }

                    }
                });
            } catch (Exception ignored) {

            }
            return null;
        }
    }


}
