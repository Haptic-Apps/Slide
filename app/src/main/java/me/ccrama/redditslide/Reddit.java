package me.ccrama.redditslide;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDexApplication;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.lusfold.androidkeyvaluestore.KVStore;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.TimePeriod;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import me.ccrama.redditslide.Tumblr.TumblrUtils;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.AdBlocker;
import me.ccrama.redditslide.util.GifCache;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.UpgradeUtil;
import okhttp3.Dns;
import okhttp3.OkHttpClient;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static final String EMPTY_STRING = "NOTHING";

    public static final long   enter_animation_time_original = 600;
    public static final String PREF_LAYOUT                   = "PRESET";
    public static final String SHARED_PREF_IS_MOD            = "is_mod";

    public static IabHelper mHelper;
    public static       SubmissionSearchPaginator.SearchSort search                          =
            SubmissionSearchPaginator.SearchSort.RELEVANCE;
    public static       long                                 enter_animation_time            =
            enter_animation_time_original;
    public static final int                                  enter_animation_time_multiplier = 1;

    public static Authentication authentication;

    public static Sorting defaultSorting;

    public static TimePeriod        timePeriod;
    public static SharedPreferences colors;
    public static SharedPreferences appRestart;
    public static SharedPreferences tags;

    public static int                      dpWidth;
    public static int                      notificationTime;
    public static boolean                  videoPlugin;
    public static NotificationJobScheduler notifications;
    public static       boolean isLoading = false;
    public static final long    time      = System.currentTimeMillis();
    public static boolean            fabClear;
    public static ArrayList<Integer> lastposition;
    public static int                currentPosition;
    public static SharedPreferences  cachedData;
    public static final boolean noGapps = true; //for testing
    public static boolean            overrideLanguage;
    public static boolean            isRestarting;
    public static AutoCacheScheduler autoCache;
    public static boolean            peek;
    private final List<Listener> listeners = new ArrayList<>();
    public        boolean      active;
    private       ImageLoader  defaultImageLoader;
    public static OkHttpClient client;

    public static void forceRestart(Context context) {
        if (appRestart.contains("back")) {
            appRestart.edit().remove("back").apply();
        }

        appRestart.edit().putBoolean("isRestarting", true).apply();
        isRestarting = true;
        ProcessPhoenix.triggerRebirth(context, new Intent(context, MainActivity.class));
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
     *
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxVertical(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp to px, uses horizontal density
     *
     * @param dp to convert to px
     * @return px
     */
    public static int dpToPxHorizontal(int dp) {
        final DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void defaultShareText(String title, String url, Context c) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        /* Decode html entities */
        title = StringEscapeUtils.unescapeHtml4(title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, c.getString(R.string.title_share)));
    }

    public static void defaultShare(String url, Context c) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Uri webpage = LinkUtil.formatURL(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }

    public static boolean isPackageInstalled(final Context ctx, String s) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(s, 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }

    private static boolean isPackageInstalled(final Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("me.ccrama.slideforreddittabletuiunlock", 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }

    private static boolean isVideoPluginInstalled(final Context ctx) {
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo("ccrama.me.slideyoutubeplugin", 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
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
        subreddit = subreddit.toLowerCase();
        Sorting sort =
                sorting.containsKey(subreddit) ? sorting.get(subreddit) : Reddit.defaultSorting;

        return getSortingId(sort);
    }

    public static Integer getSortingId(Sorting sort) {
        switch (sort) {
            case HOT:
                return 0;
            case NEW:
                return 1;
            case RISING:
                return 2;
            case TOP:
                return 3;
            case CONTROVERSIAL:
                return 4;
            default:
                return 0;
        }
    }

    public static Integer getSortingIdTime(String subreddit) {
        subreddit = subreddit.toLowerCase();
        TimePeriod time = times.containsKey(subreddit) ? times.get(subreddit) : Reddit.timePeriod;

        return getSortingIdTime(time);
    }

    public static Integer getSortingIdTime(TimePeriod time) {
        switch (time) {
            case HOUR:
                return 0;
            case DAY:
                return 1;
            case WEEK:
                return 2;
            case MONTH:
                return 3;
            case YEAR:
                return 4;
            case ALL:
                return 5;
            default:
                return 0;
        }
    }

    public static Integer getSortingIdSearch() {
        return timePeriod == TimePeriod.HOUR ? 0 : timePeriod == TimePeriod.DAY ? 1
                : timePeriod == TimePeriod.WEEK ? 2 : timePeriod == TimePeriod.MONTH ? 3
                        : timePeriod == TimePeriod.YEAR ? 4 : 5;
    }

    public static Integer getSortingIdSearch(Search s) {
        return s.time == TimePeriod.HOUR ? 0 : s.time == TimePeriod.DAY ? 1
                : s.time == TimePeriod.WEEK ? 2
                        : s.time == TimePeriod.MONTH ? 3 : s.time == TimePeriod.YEAR ? 4 : 5;
    }

    public static Integer getTypeSearch() {
        return search == SubmissionSearchPaginator.SearchSort.RELEVANCE ? 0
                : search == SubmissionSearchPaginator.SearchSort.TOP ? 1
                        : search == SubmissionSearchPaginator.SearchSort.NEW ? 2 : 3;
    }

    public static String[] getSortingStrings(Context c) {
        String[] current = new String[]{
                c.getString(R.string.sorting_hot), c.getString(R.string.sorting_new),
                c.getString(R.string.sorting_rising), c.getString(R.string.sorting_top),
                c.getString(R.string.sorting_controversial),
        };
        return current;
    }

    public static Spannable[] getSortingSpannables(Context c, String currentSub) {
        return getSortingSpannables(c, getSortingId(currentSub), currentSub);

    }

    public static Spannable[] getSortingSpannables(Context c, Sorting sorting) {
        return getSortingSpannables(c, getSortingId(sorting), " ");
    }

    private static Spannable[] getSortingSpannables(Context c, int sortingId, String sub) {
        ArrayList<Spannable> spannables = new ArrayList<>();
        String[] sortingStrings = getSortingStrings(c);
        for (int i = 0; i < sortingStrings.length; i++) {
            SpannableString spanString = new SpannableString(sortingStrings[i]);
            if (i == sortingId) {
                spanString.setSpan(new ForegroundColorSpan(new ColorPreferences(c).getColor(sub)),
                        0, spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            }
            spannables.add(spanString);
        }
        return spannables.toArray(new Spannable[spannables.size()]);
    }

    public static String[] getSortingStringsTime(Context c) {
        String[] current = new String[]{
                c.getString(R.string.sorting_hour), c.getString(R.string.sorting_day),
                c.getString(R.string.sorting_week), c.getString(R.string.sorting_month),
                c.getString(R.string.sorting_year), c.getString(R.string.sorting_all),
        };
        return current;
    }

    public static Spannable[] getSortingSpannablesTime(Context c, String currentSub) {
        return getSortingSpannablesTime(c, getSortingIdTime(currentSub), currentSub);
    }

    public static Spannable[] getSortingSpannablesTime(Context c, TimePeriod time) {
        return getSortingSpannablesTime(c, getSortingIdTime(time), " ");
    }

    private static Spannable[] getSortingSpannablesTime(Context c, int sortingId, String sub) {
        ArrayList<Spannable> spannables = new ArrayList<>();
        String[] sortingStrings = getSortingStringsTime(c);
        for (int i = 0; i < sortingStrings.length; i++) {
            SpannableString spanString = new SpannableString(sortingStrings[i]);
            if (i == sortingId) {
                spanString.setSpan(new ForegroundColorSpan(new ColorPreferences(c).getColor(sub)),
                        0, spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
            }
            spannables.add(spanString);
        }
        return spannables.toArray(new Spannable[spannables.size()]);
    }

    public static String[] getSortingStringsComments(Context c) {
        return new String[]{
                c.getString(R.string.sorting_best), c.getString(R.string.sorting_top),
                c.getString(R.string.sorting_new), c.getString(R.string.sorting_controversial),
                c.getString(R.string.sorting_old), c.getString(R.string.sorting_ama),
        };
    }

    public static String[] getSearch(Context c) {
        return new String[]{
                c.getString(R.string.search_relevance), c.getString(R.string.search_top),
                c.getString(R.string.search_new), c.getString(R.string.search_comments)
        };
    }

    public static String[] getSortingStringsSearch(Context c) {
        return new String[]{
                c.getString(R.string.sorting_search_hour), c.getString(R.string.sorting_search_day),
                c.getString(R.string.sorting_search_week),
                c.getString(R.string.sorting_search_month),
                c.getString(R.string.sorting_search_year), c.getString(R.string.sorting_search_all),
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
        doLanguages(activity);
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.dns(new GfycatIpv4Dns());
            client = builder.build();
        }
        if (authentication != null
                && Authentication.didOnline
                && Authentication.authentication.getLong("expires", 0) <= Calendar.getInstance()
                .getTimeInMillis()) {
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
        final Thread.UncaughtExceptionHandler androidHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        final WeakReference<Context> cont = new WeakReference<>(base);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable t) {
                if (cont.get() != null) {
                    final Context c = cont.get();
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    t.printStackTrace(printWriter);
                    String stacktrace = writer.toString().replace(";", ",");
                    if (stacktrace.contains("UnknownHostException") || stacktrace.contains(
                            "SocketTimeoutException") || stacktrace.contains("ConnectException")) {
                        //is offline
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                                          @Override
                                          public void run() {
                                              try {
                                                  new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                                          .setMessage(R.string.err_connection_failed_msg)
                                                          .setNegativeButton(R.string.btn_close,
                                                                  new DialogInterface.OnClickListener() {
                                                                      @Override
                                                                      public void onClick(DialogInterface dialog,
                                                                              int which) {
                                                                          if (!(c instanceof MainActivity)) {
                                                                              ((Activity) c).finish();
                                                                          }
                                                                      }
                                                                  })
                                                          .setPositiveButton(R.string.btn_offline,
                                                                  new DialogInterface.OnClickListener() {
                                                                      @Override
                                                                      public void onClick(DialogInterface dialog,
                                                                              int which) {
                                                                          Reddit.appRestart.edit()
                                                                                  .putBoolean("forceoffline",
                                                                                          true)
                                                                                  .apply();
                                                                          Reddit.forceRestart(c);
                                                                      }
                                                                  })
                                                          .show();
                                              } catch (Exception ignored) {

                                              }
                                          }
                                      }

                        );
                    } else if (stacktrace.contains("403 Forbidden") || stacktrace.contains(
                            "401 Unauthorized")) {
                        //Un-authenticated
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_refused_request_msg)
                                            .setNegativeButton("No",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            if (!(c instanceof MainActivity)) {
                                                                ((Activity) c).finish();
                                                            }
                                                        }
                                                    })
                                            .setPositiveButton("Yes",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            authentication.updateToken((c));
                                                        }
                                                    })
                                            .show();
                                } catch (Exception ignored) {

                                }
                            }
                        });

                    } else if (stacktrace.contains("404 Not Found") || stacktrace.contains(
                            "400 Bad Request")) {
                        final Handler mHandler = new Handler(Looper.getMainLooper());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    new AlertDialogWrapper.Builder(c).setTitle(R.string.err_title)
                                            .setMessage(R.string.err_could_not_find_content_msg)
                                            .setNegativeButton("Close",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog,
                                                                int which) {
                                                            if (!(c instanceof MainActivity)) {
                                                                ((Activity) c).finish();
                                                            }
                                                        }

                                                    })
                                            .show();
                                } catch (Exception ignored) {

                                }
                            }
                        });
                    } else if (t instanceof NetworkException) {
                        Toast.makeText(c, "Error "
                                + ((NetworkException) t).getResponse().getStatusMessage()
                                + ": "
                                + (t).getMessage(), Toast.LENGTH_LONG).show();
                    } else if (t instanceof NullPointerException && t.getMessage()
                            .contains(
                                    "Attempt to invoke virtual method 'android.content.Context android.view.ViewGroup.getContext()' on a null object reference")) {
                        t.printStackTrace();
                    } else if (t instanceof MaterialDialog.DialogException) {
                        t.printStackTrace();
                    } else if (t instanceof IllegalArgumentException && t.getMessage()
                            .contains("pointerIndex out of range")) {
                        t.printStackTrace();
                    } else {
                        appRestart.edit()
                                .putString("startScreen", "a")
                                .apply(); //Force reload of data after crash incase state was not saved


                        try {

                            SharedPreferences prefs =
                                    c.getSharedPreferences("STACKTRACE", Context.MODE_PRIVATE);
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
        doLanguages(activity);
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
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return;
        }
        UpgradeUtil.upgrade(getApplicationContext());
        doMainStuff();
    }

    public void doMainStuff() {
        Log.v(LogUtil.getTag(), "ON CREATED AGAIN");
        if (client == null) {
            client = new OkHttpClient();
        }

        overrideLanguage =
                getSharedPreferences("SETTINGS", 0).getBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE,
                        false);
        appRestart = getSharedPreferences("appRestart", 0);
        AlbumUtils.albumRequests = getSharedPreferences("albums", 0);
        TumblrUtils.tumblrRequests = getSharedPreferences("tumblr", 0);

        cachedData = getSharedPreferences("cache", 0);

        if (!cachedData.contains("hasReset")) {
            cachedData.edit().clear().putBoolean("hasReset", true).apply();
        }

        registerActivityLifecycleCallbacks(this);
        Authentication.authentication = getSharedPreferences("AUTH", 0);
        UserSubscriptions.subscriptions = getSharedPreferences("SUBSNEW", 0);
        UserSubscriptions.multiNameToSubs = getSharedPreferences("MULTITONAME", 0);
        UserSubscriptions.pinned = getSharedPreferences("PINNED", 0);
        PostMatch.filters = getSharedPreferences("FILTERS", 0);
        ImageFlairs.flairs = getSharedPreferences("FLAIRS", 0);
        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        defaultSorting = SettingValues.defaultSorting;
        timePeriod = SettingValues.timePeriod;
        colors = getSharedPreferences("COLOR", 0);
        tags = getSharedPreferences("TAGS", 0);
        KVStore.init(this, "SEEN");
        doLanguages(this);
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

        AdBlocker.init(this);

        Authentication.mod = Authentication.authentication.getBoolean(SHARED_PREF_IS_MOD, false);

        enter_animation_time = enter_animation_time_original * enter_animation_time_multiplier;

        fabClear = colors.getBoolean(SettingValues.PREF_FAB_CLEAR, false);

        int widthDp = this.getResources().getConfiguration().screenWidthDp;
        int heightDp = this.getResources().getConfiguration().screenHeightDp;

        int fina = (widthDp > heightDp) ? widthDp : heightDp;
        fina += 99;

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

        setupNotificationChannels();
    }

    public void doLanguages(Context c) {
        if (SettingValues.overrideLanguage) {
            Locale locale = new Locale("en_US");
            Locale.setDefault(locale);
            Configuration config = c.getResources().getConfiguration();
            config.locale = locale;
            c.getResources().updateConfiguration(config, null);
        }
    }

    public static String CHANNEL_IMG = "IMG_DOWNLOADS";
    public static String CHANNEL_COMMENT_CACHE = "POST_SYNC";
    public static String CHANNEL_MAIL = "MAIL";
    public static String CHANNEL_MODMAIL = "MODMAIL";
    public static String CHANNEL_SUBCHECKING = "SUB_CHECK";

    public void setupNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            {
                String channelId = CHANNEL_IMG;
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, "Image downloads", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setShowBadge(false);

                notificationChannel.setLightColor(Palette.getColor(""));
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            {
                String channelId = CHANNEL_COMMENT_CACHE;
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, "Comment caching", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setShowBadge(false);

                notificationChannel.setLightColor(Palette.getColor(""));
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            {
                String channelId = CHANNEL_MAIL;
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, "Reddit mail", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLightColor(Palette.getColor(""));
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            {
                String channelId = CHANNEL_MODMAIL;
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, "Reddit modmail", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setShowBadge(true);
                notificationChannel.setLightColor(getResources().getColor(R.color.md_red_500));
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
            {
                String channelId = CHANNEL_SUBCHECKING;
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel =
                        new NotificationChannel(channelId, "Submission post checking",
                                importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Palette.getColor(""));
                notificationChannel.setShowBadge(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
    }

    public static void setSorting(String s, Sorting sort) {
        sorting.put(s.toLowerCase(), sort);
    }

    public static final Map<String, Sorting> sorting = new HashMap<>();

    public static Sorting getSorting(String subreddit, Sorting defaultSort) {
        subreddit = subreddit.toLowerCase();
        if (sorting.containsKey(subreddit)) {
            return sorting.get(subreddit);
        } else {
            return defaultSort;
        }
    }

    public static TimePeriod getTime(String subreddit, TimePeriod defaultTime) {
        subreddit = subreddit.toLowerCase();
        if (times.containsKey(subreddit)) {
            return times.get(subreddit);
        } else {
            return defaultTime;
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
            if (mHelper == null) {
                try {
                    mHelper = new IabHelper(Reddit.this,
                            SecretConstants.getBase64EncodedPublicKey(getBaseContext()));
                    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                        public void onIabSetupFinished(IabResult result) {
                            if (!result.isSuccess()) {
                                LogUtil.e("Problem setting up In-app Billing: " + result);
                            }
                        }
                    });
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            return null;
        }
    }


    //IPV6 workaround by /u/talklittle
    public class GfycatIpv4Dns implements Dns {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (ContentType.hostContains(hostname, "gfycat.com")) {
                InetAddress[] addresses = InetAddress.getAllByName(hostname);
                if (addresses == null || addresses.length == 0) {
                    throw new UnknownHostException("Bad host: " + hostname);
                }

                // prefer IPv4; list IPv4 first
                ArrayList<InetAddress> result = new ArrayList<>();
                for (InetAddress address : addresses) {
                    if (address instanceof Inet4Address) {
                        result.add(address);
                    }
                }
                for (InetAddress address : addresses) {
                    if (!(address instanceof Inet4Address)) {
                        result.add(address);
                    }
                }

                return result;
            } else {
                return Dns.SYSTEM.lookup(hostname);
            }
        }
    }
}
