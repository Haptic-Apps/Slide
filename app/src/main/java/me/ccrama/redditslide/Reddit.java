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
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.Crash;
import me.ccrama.redditslide.Activities.Internet;
import me.ccrama.redditslide.Activities.LoadingData;
import me.ccrama.redditslide.Activities.SubredditOverview;
import me.ccrama.redditslide.Activities.SubredditOverviewSingle;
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
    public static boolean swap;
    public static boolean album;
    public static boolean image;
    public static boolean video;
    public static boolean gif;
    public static boolean web;
    public static boolean exit;
    public static boolean fastscroll;
    public static boolean fab = true;
    public static int fabType = R.integer.FAB_POST;
    public static boolean hideButton;
    public static Authentication authentication;
    public static boolean tabletUI;
    public static Sorting defaultSorting;
    public static CommentSort defaultCommentSorting;
    public static TimePeriod timePeriod;
    public static SharedPreferences colors;
    public static int themeBack;
    public static int dpWidth;
    public static int notificationTime;
    public static NotificationJobScheduler notifications;
    public static Boolean online = true;
    public static SharedPreferences seen;
    public static SharedPreferences hidden;
    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;
    public static boolean isLoading = false;
    private final List<Listener> listeners = new ArrayList<Listener>();
    private final Handler mBackgroundDelayHandler = new Handler();
    public boolean active;
    public LoadingData loader;
    private ImageLoader defaultImageLoader;
    private boolean closed = false;
    private boolean mInBackground = true;
    private Runnable mBackgroundTransition;
    public static long time = System.currentTimeMillis();
    private boolean isRestarting;

    public static CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                    Log.w("Slide", "onNavigationEvent: Code = " + navigationEvent);
                }
            });
        }
        return mCustomTabsSession;
    }

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

    @Override
    public void onActivityResumed(Activity activity) {

        if (mBackgroundTransition != null) {
            mBackgroundDelayHandler.removeCallbacks(mBackgroundTransition);
            mBackgroundTransition = null;
        }

        if (mInBackground) {
            mInBackground = false;
            notifyOnBecameForeground();

            authentication.updateToken(activity);

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
                        i.putExtra("stacktrace", "```" + s + "```");

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
        fab = SettingValues.prefs.getBoolean("Fab", false);
        fabType = SettingValues.prefs.getInt("FabType", R.integer.FAB_POST);
        swap = SettingValues.prefs.getBoolean("Swap", false);
        web = SettingValues.prefs.getBoolean("web", true);
        image = SettingValues.prefs.getBoolean("image", true);
        album = SettingValues.prefs.getBoolean("album", true);
        gif = SettingValues.prefs.getBoolean("gif", true);
        video = SettingValues.prefs.getBoolean("video", true);
        exit = SettingValues.prefs.getBoolean("Exit", true);
        fastscroll = SettingValues.prefs.getBoolean("Fastscroll", false);
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


        tabletUI = isPackageInstalled(this);

    }

    public void startMain() {
        if (active) {
            Intent i;
            if (single) {
                i = new Intent(this, SubredditOverviewSingle.class);
            } else {
                i = new Intent(this, SubredditOverview.class);
            }
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
}
