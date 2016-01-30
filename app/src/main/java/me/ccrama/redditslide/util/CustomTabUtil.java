package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
                    .setStartAnimations(contextActivity, R.anim.slideright, R.anim.fading_out_real)
                    .setExitAnimations(contextActivity, R.anim.fade_out, R.anim.fade_in_real)

                    .setActionButton(BitmapFactory.decodeResource(res, R.drawable.share),
                            contextActivity.getString(R.string.submission_link_share),
                            createPendingShareIntent(contextActivity.getApplicationContext(), url))
                    .setCloseButtonIcon(BitmapFactory.decodeResource(res, R.drawable.ic_arrow_back_white_24dp));

            try {
                builder.build().launchUrl(contextActivity, Uri.parse(url));
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

    private static PendingIntent createPendingShareIntent(Context context, String url) {
        Intent actionIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, url);
        return PendingIntent.getActivity(context, 0, actionIntent, 0);
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
