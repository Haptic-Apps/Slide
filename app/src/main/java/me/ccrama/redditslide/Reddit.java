package me.ccrama.redditslide;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.Activities.LoadingData;
import me.ccrama.redditslide.Activities.SubredditOverview;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class Reddit extends Application  implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        new Authentication.UpdateToken().execute();
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


        int height = this.getResources().getConfiguration().screenWidthDp;

        int width = this.getResources().getConfiguration().screenHeightDp;

        int fina;
        if(height > width){
            fina = height;
        } else {
            fina = width;
        }
        fina = ((fina + 99) / 100 ) * 100;

        dpWidth = fina / 300;
        new Authentication(this);
        themeBack = new ColorPreferences(this).getFontStyle().getThemeType();


        tabletUI = isPackageInstalled(this, "me.ccrama.slideforreddittabletuiunlock");
    }

    public void startMain(){
        if(active) {
            Intent i = new Intent(this, SubredditOverview.class);
            Log.v("Slide", "starting new");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(i);
            if(loader != null){
                loader.finish();
            }
        }
    }

    public boolean active;

    public static CommentSort defaultCommentSorting;

    public static SharedPreferences seen;
    public LoadingData loader;
    public static SharedPreferences hidden;

    public static boolean isPackageInstalled (final Context ctx, final String packageName) {
        boolean result = false;
        try {
            final PackageManager pm = ctx.getPackageManager();
            final PackageInfo pi = pm.getPackageInfo(packageName, 0);
            if (pi != null && pi.applicationInfo.enabled)
                result = true;
        }
        catch (final Throwable e) {
        }

        return result;
    }
    public void restart(){

        Intent iIntent = new Intent(this, SubredditOverview.class);
        iIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(iIntent);
        android.os.Process.killProcess(android.os.Process.myPid());

    }
}
