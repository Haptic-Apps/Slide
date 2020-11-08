package me.ccrama.redditslide;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.multidex.MultiDexApplication;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.lusfold.androidkeyvaluestore.KVStore;
import com.nostra13.universalimageloader.core.ImageLoader;

import net.dean.jraw.http.NetworkException;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
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

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.ImgurAlbum.AlbumUtils;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.Notifications.NotificationPiggyback;
import me.ccrama.redditslide.Tumblr.TumblrUtils;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.AdBlocker;
import me.ccrama.redditslide.util.GifCache;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.SortingUtil;
import me.ccrama.redditslide.util.UpgradeUtil;
import me.ccrama.redditslide.util.billing.IabHelper;
import me.ccrama.redditslide.util.billing.IabResult;
import okhttp3.Dns;
import okhttp3.OkHttpClient;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    private static Application mApplication;

    public static final String EMPTY_STRING = "NOTHING";

    public static final long   enter_animation_time_original = 600;
    public static final String PREF_LAYOUT                   = "PRESET";
    public static final String SHARED_PREF_IS_MOD            = "is_mod";
    public static Cache videoCache;

    public static IabHelper mHelper;
    public static       long enter_animation_time            = enter_animation_time_original;
    public static final int  enter_animation_time_multiplier = 1;

    public static Authentication authentication;

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
    public static ArrayList<Integer> lastPosition;
    public static int                currentPosition;
    public static SharedPreferences  cachedData;
    public static final boolean noGapps = true; //for testing
    public static boolean            overrideLanguage;
    public static boolean            isRestarting;
    public static AutoCacheScheduler autoCache;
    public static boolean            peek;
    public        boolean      active;
    public        ImageLoader  defaultImageLoader;
    public static OkHttpClient client;

    public static boolean canUseNightModeAuto = false;

    public static void forceRestart(Context context, boolean forceLoadScreen) {
        if (forceLoadScreen) {
            appRestart.edit().putString("startScreen", "").apply();
            appRestart.edit().putBoolean("isRestarting", true).apply();
        }
        if (appRestart.contains("back")) {
            appRestart.edit().remove("back").apply();
        }

        appRestart.edit().putBoolean("isRestarting", true).apply();
        isRestarting = true;
        ProcessPhoenix.triggerRebirth(context, new Intent(context, MainActivity.class));
    }

    private static int dpToPx(int dp, float xy) {
        return Math.round(dp * xy / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int dpToPxVertical(int dp) {
        return dpToPx(dp, Resources.getSystem().getDisplayMetrics().ydpi);
    }

    public static int dpToPxHorizontal(int dp) {
        return dpToPx(dp, Resources.getSystem().getDisplayMetrics().xdpi);
    }

    public static void defaultShareText(String title, String url, Context c) {
        url = StringEscapeUtils.unescapeHtml4(HtmlCompat.fromHtml(url, HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        /* Decode html entities */
        title = StringEscapeUtils.unescapeHtml4(title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, c.getString(R.string.title_share)));
    }

    public static boolean isPackageInstalled(String s) {
        try {
            final PackageInfo pi = getAppContext().getPackageManager().getPackageInfo(s, 0);
            if (pi != null && pi.applicationInfo.enabled) return true;
        } catch (final Throwable ignored) {
        }
        return false;
    }

    private static boolean isProPackageInstalled() {
        return isPackageInstalled(getAppContext().getString(R.string.ui_unlock_package));
    }

    private static boolean isVideoPluginInstalled() {
        return isPackageInstalled(getAppContext().getString(R.string.youtube_plugin_package));
    }

    public static HashMap<String, String> getInstalledBrowsers() {
        int packageMatcher =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PackageManager.MATCH_ALL : PackageManager.GET_DISABLED_COMPONENTS;

        HashMap<String, String> browserMap = new HashMap<>();

        final List<ResolveInfo> resolveInfoList = getAppContext().getPackageManager()
                .queryIntentActivities(
                        new Intent(Intent.ACTION_VIEW, Uri.parse("http://ccrama.me")),
                        packageMatcher);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (resolveInfo.activityInfo.enabled) {
                browserMap.put(resolveInfo.activityInfo.applicationInfo.packageName,
                        Reddit.getAppContext()
                                .getPackageManager()
                                .getApplicationLabel(resolveInfo.activityInfo.applicationInfo)
                                .toString());
            }
        }

        return browserMap;
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
        doLanguages();
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
                                                                          Reddit.forceRestart(c,
                                                                                  false);
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
        doLanguages();
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
        mApplication = this;
        //  LeakCanary.install(this);
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return;
        }

        final File dir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getExternalCacheDir() != null) {
            dir = new File(getExternalCacheDir() + File.separator + "video-cache");
        } else {
            dir = new File(getCacheDir() + File.separator + "video-cache");
        }
        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(256 * 1024 * 1024);
        DatabaseProvider databaseProvider = new ExoDatabaseProvider(getAppContext());
        videoCache = new SimpleCache(dir, evictor, databaseProvider); // 256MB

        UpgradeUtil.upgrade(getApplicationContext());
        doMainStuff();
    }

    public void doMainStuff() {
        Log.v(LogUtil.getTag(), "ON CREATED AGAIN");
        if (client == null) {
            client = new OkHttpClient();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setCanUseNightModeAuto();
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
        UserSubscriptions.newsNameToSubs = getSharedPreferences("NEWSMULTITONAME", 0);
        UserSubscriptions.news = getSharedPreferences("NEWS", 0);

        UserSubscriptions.newsNameToSubs.edit()
                .putString("android", "android+androidapps+googlepixel")
                .putString("news", "worldnews+news+politics")
                .apply();

        UserSubscriptions.pinned = getSharedPreferences("PINNED", 0);
        PostMatch.filters = getSharedPreferences("FILTERS", 0);
        ImageFlairs.flairs = getSharedPreferences("FLAIRS", 0);
        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        SortingUtil.defaultSorting = SettingValues.defaultSorting;
        SortingUtil.timePeriod = SettingValues.timePeriod;
        colors = getSharedPreferences("COLOR", 0);
        tags = getSharedPreferences("TAGS", 0);
        KVStore.init(this, "SEEN");
        doLanguages();
        lastPosition = new ArrayList<>();

        if (BuildConfig.FLAVOR == "withGPlay") {
            new SetupIAB().execute();
        }

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

        int fina = Math.max(widthDp, heightDp);
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

        SettingValues.isPro = isProPackageInstalled() || BuildConfig.isFDroid;
        videoPlugin = isVideoPluginInstalled();

        GifCache.init(this);

        setupNotificationChannels();
    }

    public void doLanguages() {
        if (SettingValues.overrideLanguage) {
            Locale locale = new Locale("en_US");
            Locale.setDefault(locale);
            Configuration config = getResources().getConfiguration();
            config.locale = locale;
            getResources().updateConfiguration(config, null);
        }
    }

    public boolean isNotificationAccessEnabled() {
        ActivityManager manager = ContextCompat.getSystemService(this, ActivityManager.class);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                    Integer.MAX_VALUE)) {
                if (NotificationPiggyback.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }


    public static final String CHANNEL_IMG           = "IMG_DOWNLOADS";
    public static final String CHANNEL_COMMENT_CACHE = "POST_SYNC";
    public static final String CHANNEL_MAIL          = "MAIL_NOTIFY";
    public static final String CHANNEL_MODMAIL       = "MODMAIL_NOTIFY";
    public static final String CHANNEL_SUBCHECKING   = "SUB_CHECK_NOTIFY";

    public void setupNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Each triple contains the channel ID, name, and importance level
            List<Triple<String, String, Integer>> notificationTripleList =
                    new ArrayList<Triple<String, String, Integer>>() {{
                        add(Triple.of(CHANNEL_IMG, "Image downloads",
                                NotificationManager.IMPORTANCE_LOW));
                        add(Triple.of(CHANNEL_COMMENT_CACHE, "Comment caching",
                                NotificationManager.IMPORTANCE_LOW));
                        add(Triple.of(CHANNEL_MAIL, "Reddit mail",
                                NotificationManager.IMPORTANCE_HIGH));
                        add(Triple.of(CHANNEL_MODMAIL, "Reddit modmail",
                                NotificationManager.IMPORTANCE_HIGH));
                        add(Triple.of(CHANNEL_SUBCHECKING, "Submission post checking",
                                NotificationManager.IMPORTANCE_LOW));
                    }};

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            for (Triple<String, String, Integer> notificationTriple : notificationTripleList) {
                NotificationChannel notificationChannel =
                        new NotificationChannel(notificationTriple.getLeft(),
                                notificationTriple.getMiddle(), notificationTriple.getRight());
                notificationChannel.enableLights(true);
                notificationChannel.setShowBadge(
                        notificationTriple.getRight() == NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setLightColor(
                        notificationTriple.getLeft().contains("MODMAIL") ? getResources().getColor(
                                R.color.md_red_500, null) : Palette.getColor(""));
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
    }

    private static class SetupIAB extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (mHelper == null) {
                try {
                    mHelper = new IabHelper(getAppContext(),
                            SecretConstants.getBase64EncodedPublicKey(getAppContext()));
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
    public static class GfycatIpv4Dns implements Dns {
        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (ContentType.hostContains(hostname, "gfycat.com", "redgifs.com")) {
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

    public static Context getAppContext() {
        return mApplication.getApplicationContext();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void setCanUseNightModeAuto() {
        UiModeManager uiModeManager = getAppContext().getSystemService(UiModeManager.class);
        if (uiModeManager != null) {
            uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_AUTO);
            canUseNightModeAuto = true;
        } else {
            canUseNightModeAuto = false;
        }
    }
}
