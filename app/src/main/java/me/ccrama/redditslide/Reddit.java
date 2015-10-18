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
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import me.ccrama.redditslide.Activities.Crash;
import me.ccrama.redditslide.Activities.LoadingData;
import me.ccrama.redditslide.Activities.Login;
import me.ccrama.redditslide.Activities.SubredditOverview;
import me.ccrama.redditslide.Activities.SubredditOverviewSingle;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Reddit extends Application implements Application.ActivityLifecycleCallbacks {
    public static IabHelper mHelper;
    public static boolean single;
    public static boolean swap;
    public static boolean album;
    public static boolean image;
    public static boolean video;
    public static boolean gif;
    public static boolean web;


    boolean closed = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
        closed = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {

        if (closed && !(activity instanceof Login) && !isRestarting) {

            new Authentication.UpdateToken().execute();
            closed = false;

        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    public static boolean tabletUI;
    public static Sorting defaultSorting;
    public static TimePeriod timePeriod;
    public static SharedPreferences colors;
    public static int themeBack;

    public static int dpWidth;
    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;

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

    public boolean isRestarting;

    public class SetupIAB extends AsyncTask<Void, Void, Void> {

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
            } catch (Exception e) {

            }
            return null;
        }
    }

    public static void forceRestart(Context context){
        Intent mStartActivity = new Intent(context, LoadingData.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
    public static void defaultShareText(String url, Context c){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, url);
        c.startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }
    public static void defaultShare(String url, Context c){
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(intent);
        }
    }
    public static int notificationTime;

    public static NotificationJobScheduler notifications;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        defaultSorting = Sorting.HOT;
        timePeriod = TimePeriod.DAY;
        Authentication.authentication = getSharedPreferences("AUTH", 0);
        SubredditStorage.subscriptions = getSharedPreferences("SUBS", 0);


        SettingValues.setAllValues(getSharedPreferences("SETTINGS", 0));
        colors = getSharedPreferences("COLOR", 0);
        seen = getSharedPreferences("SEEN", 0);
        hidden = getSharedPreferences("HIDDEN", 0);
        Hidden.hidden = getSharedPreferences("HIDDEN_POSTS", 0);

        //new SetupIAB().execute();

        //START code adapted from https://github.com/QuantumBadger/RedReader/
        final Thread.UncaughtExceptionHandler androidHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable t) {

                try {
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer);
                    t.printStackTrace(printWriter);
                    String s = writer.toString();
                    s = s.replace(";", ",");
                    s = s.replace("at", "%0Aat");
                    Log.v("Slide", "Slide crashed with " + s);
                    Intent i = new Intent(Reddit.this, Crash.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("stacktrace", s);

                    startActivity(i);
                } catch (Throwable t1) {
                }

                androidHandler.uncaughtException(thread, t);
            }
        });
        //END adaptation

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

        single = colors.getBoolean("Single", false);
        swap = colors.getBoolean("Swap", false);
        web = colors.getBoolean("web", true);
        image = colors.getBoolean("image", true);
        album = colors.getBoolean("album", true);
        gif = colors.getBoolean("gif", true);
        video = colors.getBoolean("video", true);

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
        defaultDPWidth = fina / 300;
        new Authentication(this);

        if (notificationTime != -1) {
            notifications = new NotificationJobScheduler(this);

        }
        tabletUI = isPackageInstalled(this, "me.ccrama.slideforreddittabletuiunlock");
    }

    public static int defaultDPWidth;

    public void startMain() {
        if (active) {
            Intent i = null;
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
            }
        }
    }

    public boolean active;

    public static CommentSort defaultCommentSorting;

    public static SharedPreferences seen;
    public LoadingData loader;
    public static SharedPreferences hidden;

    public static boolean isPackageInstalled(final Context ctx, final String packageName) {
        boolean result = false;
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(packageName, 0);
            if (pi != null && pi.applicationInfo.enabled)
                result = true;
        } catch (final Throwable e) {
        }

        return result;
    }

    public void restart() {
        isRestarting = true;

        Intent mStartActivity = new Intent(this, LoadingData.class);
        mStartActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        mStartActivity.putExtra("EXIT", true);
        this.startActivity(mStartActivity);
        onCreate();


    }
}
