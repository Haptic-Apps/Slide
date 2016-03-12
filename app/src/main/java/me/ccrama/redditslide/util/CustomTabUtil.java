package me.ccrama.redditslide.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

public class CustomTabUtil {

    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;

    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity) {
        if (SettingValues.web && SettingValues.customtabs) {
            Resources res = contextActivity.getResources();
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession())
                    .setToolbarColor(color)
                    .setShowTitle(true)
                    .setStartAnimations(contextActivity, R.anim.slide_up_fade_in, 0)
                    .setExitAnimations(contextActivity, 0, R.anim.slide_down_fade_out)
                    .addDefaultShareMenuItem()
                    .setCloseButtonIcon(BitmapFactory.decodeResource(res, R.drawable.ic_arrow_back_white_24dp));
            try {
                String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage(packageName);
                customTabsIntent.launchUrl(contextActivity, Uri.parse(url));
            } catch (ActivityNotFoundException anfe) {
                Log.w(LogUtil.getTag(), "Unknown url: " + anfe);
                Reddit.defaultShare(url, contextActivity);
            }
        } else if(!SettingValues.customtabs && SettingValues.web) {
            Intent i = new Intent(contextActivity, Website.class);
            i.putExtra(Website.EXTRA_URL, url);
            i.putExtra(Website.EXTRA_COLOR, color);
            contextActivity.startActivity(i);
        } else {
            Reddit.defaultShare(url, contextActivity);
        }
    }

    public static CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                    Log.w(LogUtil.getTag(), "onNavigationEvent: Code = " + navigationEvent);
                }
            });
        }
        return mCustomTabsSession;
    }
}
