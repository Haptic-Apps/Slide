package me.ccrama.redditslide;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.lusfold.androidkeyvaluestore.KVStore;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.GifCache;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.UpgradeUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static final String EMPTY_STRING = "NOTHING";

    public static final long enter_animation_time_original = 600;
    public static final String PREF_LAYOUT = "PRESET";
    public static final String SHARED_PREF_IS_MOD = "is_mod";
    public static final String SHARED_PREF_IS_OVER_18 = "is_over_18";

    public static IabHelper mHelper;
    public static SubmissionSearchPaginator.SearchSort search = SubmissionSearchPaginator.SearchSort.RELEVANCE;
    public static long enter_animation_time = enter_animation_time_original;
    public static final int enter_animation_time_multiplier = 1;

    public static Authentication authentication;

    public static Sorting defaultSorting;

    public static TimePeriod timePeriod;
    public static SharedPreferences colors;
    public static SharedPreferences appRestart;
    public static SharedPreferences tags;

    public static int dpWidth;
    public static int notificationTime;
    public static boolean videoPlugin;
    public static NotificationJobScheduler notifications;
    public static boolean isLoading = false;
    public static final long time = System.currentTimeMillis();
    public static boolean fabClear;
    public static ArrayList<Integer> lastposition;
    public static int currentPosition;
    public static int themeBack;
    public static SharedPreferences cachedData;
    public static final boolean noGapps = true; //for testing
    public static boolean over18 = true;
    public static boolean overrideLanguage;
    public static boolean isRestarting;
    public static AutoCacheScheduler autoCache;
    private final List<Listener> listeners = new ArrayList<>();
    public boolean active;
    private ImageLoader defaultImageLoader;

    public static void forceRestart(Context context) {
        if (appRestart.contains("back")) {
            appRestart.edit().remove("back").apply();
        }

        appRestart.edit().putBoolean("isRestarting", true).apply();
        isRestarting = true;
        ProcessPhoenix.triggerRebirth(context.getApplicationContext());

    }

    public static void forceRestart(Context c, boolean forceLoadScreen) {
        appRestart.edit().putString("startScreen", "").apply();
        appRestart.edit().putBoolean("isRestarting", true).apply();
        forceRestart(c);
    }

    /**
     * Converts px to dp
     *
     * @param px to convert to dp
     * @param c  context of view
     * @return dp
     */
    public static int pxToDp(int px, Context c) {
        final DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp to px, uses vertical density
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxVertical(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp to px, uses horizontal density
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxHorizontal(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void defaultShareText(String title, String url, Context c) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        /* Decode html entities */
        title = StringEscapeUtils.unescapeHtml4(title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, c.getString(R.string.title_share)));
    }

    public static void defaultShare(String url, Context c) {
        Uri webpage = CustomTabUtil.formatURL(url);
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

    private static boolean isVideoPluginInstalled(final Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("ccrama.me.slideyoutubeplugin", 0);
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

    public static String arrayToString(ArrayList<String> array, String separator) {
        if (array != null) {
            StringBuilder b = new StringBuilder();
            for (String s : array) {
                b.append(s).append(separator);
            }
            String f = b.toString();
            if (f.length() > 0) {
                f = f.substring(0, f.length() - separator.length());
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
        return timePeriod == TimePeriod.HOUR ? 0 :
                timePeriod == TimePeriod.DAY ? 1 :
                        timePeriod == TimePeriod.WEEK ? 2 :
                                timePeriod == TimePeriod.MONTH ? 3 :
                                        timePeriod == TimePeriod.YEAR ? 4 :
                                                5;
    }

    public static Integer getSortingIdSearch(Search s) {
        return s.time == TimePeriod.HOUR ? 0 :
                s.time == TimePeriod.DAY ? 1 :
                        s.time == TimePeriod.WEEK ? 2 :
                                s.time == TimePeriod.MONTH ? 3 :
                                        s.time == TimePeriod.YEAR ? 4 :
                                                5;
    }

    public static Integer getTypeSearch() {
        return search == SubmissionSearchPaginator.SearchSort.RELEVANCE ? 0 :
                search == SubmissionSearchPaginator.SearchSort.TOP ? 1 :
                        search == SubmissionSearchPaginator.SearchSort.NEW ? 2 :
                                3;
    }

    public static String[] getSortingStrings(Context c, String currentSub, boolean arrows) {
        return getSortingStrings(c, getSorting(currentSub), getTime(currentSub), arrows);
    }

    public static String[] getSortingStrings(Context c, Sorting currentSort, TimePeriod currentTime, boolean arrows) {
        String[] current = new String[]{
                c.getString(R.string.sorting_hot),
                c.getString(R.string.sorting_new),
                c.getString(R.string.sorting_rising),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_hour).toLowerCase(),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_day).toLowerCase(),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_week).toLowerCase(),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_month).toLowerCase(),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_year).toLowerCase(),
                c.getString(R.string.sorting_top) + " " + c.getString(R.string.sorting_all).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_hour).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_day).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_week).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_month).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_year).toLowerCase(),
                c.getString(R.string.sorting_controversial) + " " + c.getString(R.string.sorting_all).toLowerCase(),
        };
        int pos = 0;
        switch (currentSort) {

            case HOT:
                pos = 0;
                break;
            case NEW:
                pos = 1;
                break;
            case RISING:
                pos = 2;
                break;
            case CONTROVERSIAL:
                pos = 9;
                break;
            case TOP:
                pos = 3;
                break;
        }
        if (pos > 2) {
            switch (currentTime) {
                case HOUR:
                    break;
                case DAY:
                    pos += 1;
                    break;
                case WEEK:
                    pos += 2;
                    break;
                case MONTH:
                    pos += 3;
                    break;
                case YEAR:
                    pos += 4;
                    break;
                case ALL:
                    pos += 5;
                    break;
            }
        }
        current[pos] = (arrows ? "» " : "") + current[pos] + "";
        return current;
    }

    public static String[] getSortingStringsComments(Context c) {
        return new String[]{
                c.getString(R.string.sorting_best),
                c.getString(R.string.sorting_top),
                c.getString(R.string.sorting_new),
                c.getString(R.string.sorting_controversial),
                c.getString(R.string.sorting_old),
                c.getString(R.string.sorting_ama),
        };
    }

    public static String[] getSearch(Context c) {
        return new String[]{
                c.getString(R.string.search_relevance),
                c.getString(R.string.search_top),
                c.getString(R.string.search_new),
                c.getString(R.string.search_comments)
        };
    }

    public static String[] getSortingStringsSearch(Context c) {
        return new String[]{
                c.getString(R.string.sorting_search_hour),
                c.getString(R.string.sorting_search_day),
                c.getString(R.string.sorting_search_week),
                c.getString(R.string.sorting_search_month),
                c.getString(R.string.sorting_search_year),
                c.getString(R.string.sorting_search_all),
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

    public static boolean notFirst = false;

    @Override
    public void onActivityResumed(Activity activity) {
        if (authentication != null && Authentication.didOnline && Authentication.authentication.getLong("expires", 0) <= Calendar.getInstance().getTimeInMillis()) {
            authentication.updateToken(activity);
        } else if (NetworkUtil.isConnected(activity) && authentication == null) {
            authentication = new Authentication(this);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
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
                                              try {
                                                  new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                                          .setMessage(R.string.err_connection_failed_msg)
                                                          .setNegativeButton(R.string.btn_close, new DialogInterface.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialog, int which) {
                                                                  if (!(c instanceof MainActivity)) {
                                                                      ((Activity) c).finish();
                                                                  }
                                                              }
                                                          }).setPositiveButton(R.string.btn_offline, new DialogInterface.OnClickListener() {
                                                      @Override
                                                      public void onClick(DialogInterface dialog, int which) {
                                                          Reddit.appRestart.edit().putBoolean("forceoffline", true).apply();
                                                          Reddit.forceRestart(c);
                                                      }
                                                  }).show();
                                              } catch (Exception ignored) {

                                              }
                                          }
                                      }

                        );
                    } else if (stacktrace.contains("403 Forbidden") || stacktrace.contains("401 Unauthorized")) {
                        //Un-authenticated
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_refused_request_msg)
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
                                } catch (Exception ignored) {

                                }
                            }
                        });

                    } else if (stacktrace.contains("404 Not Found") || stacktrace.contains("400 Bad Request")) {
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_could_not_find_content_msg)
                                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (!(c instanceof MainActivity)) {
                                                        ((Activity) c).finish();
                                                    }
                                                }

                                            }).show();
                                } catch (Exception ignored) {

                                }
                            }
                        });
                    } else {
                        appRestart.edit().putString("startScreen", "a").apply(); //Force reload of data after crash incase state was not saved


                            try {

                                SharedPreferences prefs = c.getSharedPreferences(
                                        "STACKTRACE", Context.MODE_PRIVATE);
                                prefs.edit().putString("stacktrace", stacktrace).apply();

                            } catch (Throwable ignored) {
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
        UpgradeUtil.upgrade(getApplicationContext());
        doMainStuff();
    }

    public void doMainStuff() {
        Log.v(LogUtil.getTag(), "ON CREATED AGAIN");
        overrideLanguage = getSharedPreferences("SETTINGS", 0).getBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE, false);
        appRestart = getSharedPreferences("appRestart", 0);
        AlbumUtils.albumRequests = getSharedPreferences("albums", 0);

        cachedData = getSharedPreferences("cache", 0);

        if (!cachedData.contains("hasReset")) {
            cachedData.edit().clear().putBoolean("hasReset", true).apply();
        }

        registerActivityLifecycleCallbacks(this);
        Authentication.authentication = getSharedPreferences("AUTH", 0);
        UserSubscriptions.subscriptions = getSharedPreferences("SUBSNEW", 0);
        UserSubscriptions.multiNameToSubs = getSharedPreferences("MULTITONAME", 0);
        PostMatch.filters = getSharedPreferences("FILTERS", 0);
        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        defaultSorting = SettingValues.defaultSorting;
        timePeriod = SettingValues.timePeriod;
        colors = getSharedPreferences("COLOR", 0);
        tags = getSharedPreferences("TAGS", 0);
        KVStore.init(this, "SEEN");
        if (SettingValues.overrideLanguage) {
            Locale locale = new Locale("en_US");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, null);
        }
        lastposition = new ArrayList<>();

        new SetupIAB().execute();

        if (!appRestart.contains("startScreen")) {
            Authentication.isLoggedIn = appRestart.getBoolean("loggedin", false);
            Authentication.name = appRestart.getString("name", "LOGGEDOUT");
            active = true;
        } else {
            appRestart.edit().remove("startScreen").apply();
        }

        authentication = new Authentication(this);

        Authentication.mod = Authentication.authentication.getBoolean(SHARED_PREF_IS_MOD, false);

        enter_animation_time = enter_animation_time_original * enter_animation_time_multiplier;

        fabClear = colors.getBoolean(SettingValues.PREF_FAB_CLEAR, false);

        int widthDp = this.getResources().getConfiguration().screenWidthDp;
        int heightDp = this.getResources().getConfiguration().screenHeightDp;

        int fina = (widthDp > heightDp) ? widthDp : heightDp;
        fina += 99;

        themeBack = new ColorPreferences(this).getFontStyle().getThemeType();

        if (colors.contains("tabletOVERRIDE")) {
            dpWidth = colors.getInt("tabletOVERRIDE", fina / 300);
        } else {
            dpWidth = fina / 300;
        }

        if (colors.contains("notificationOverride")) {
            notificationTime = colors.getInt("notificationOverride", 360);
        } else {
            notificationTime = 360;
        }

        SettingValues.tabletUI = isPackageInstalled(this) || FDroid.isFDroid;
        videoPlugin = isVideoPluginInstalled(this);

        GifCache.init(this);
    }

    public static void setSorting(String s, Sorting sort) {
        sorting.put(s.toLowerCase(), sort);
    }

    public static final Map<String, Sorting> sorting = new HashMap<>();

    public static Sorting getSorting(String subreddit) {
        subreddit = subreddit.toLowerCase();
        if (sorting.containsKey(subreddit)) {
            return sorting.get(subreddit);
        } else {
            return defaultSorting;
        }
    }

    public static void setTime(String s, TimePeriod sort) {
        times.put(s.toLowerCase(), sort);
    }

    public static final Map<String, TimePeriod> times = new HashMap<>();

    public static TimePeriod getTime(String subreddit) {
        subreddit = subreddit.toLowerCase();
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
