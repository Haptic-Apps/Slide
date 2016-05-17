package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import me.ccrama.redditslide.Activities.MakeExternal;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

public class CustomTabUtil {

    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;

    private CustomTabUtil() {
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity) {

        Intent inte = new Intent(contextActivity, MakeExternal.class);
        inte.putExtra("url", url);
        PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, inte, 0);

        if (SettingValues.web && SettingValues.customtabs) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession())
                    .setToolbarColor(color)
                    .setShowTitle(true)
                    .setStartAnimations(contextActivity, R.anim.slide_up_fade_in, 0)
                    .setExitAnimations(contextActivity, 0, R.anim.slide_down_fade_out)
                    .addDefaultShareMenuItem()
                    .addMenuItem(contextActivity.getString(R.string.open_links_externally), pendingIntent)
                    .setCloseButtonIcon(drawableToBitmap(ContextCompat.getDrawable(contextActivity, R.drawable.ic_arrow_back_white_24dp)));
            try {
                String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage(packageName);
                customTabsIntent.launchUrl(contextActivity, formatURL(url));
            } catch (ActivityNotFoundException anfe) {
                Log.w(LogUtil.getTag(), "Unknown url: " + anfe);
                Reddit.defaultShare(url, contextActivity);
            }
        } else if (!SettingValues.customtabs && SettingValues.web) {
            Intent i = new Intent(contextActivity, Website.class);
            i.putExtra(Website.EXTRA_URL, url);
            i.putExtra(Website.EXTRA_COLOR, color);
            contextActivity.startActivity(i);
        } else {
            Reddit.defaultShare(url, contextActivity);
        }
    }

    /**
     * Corrects mistakes users might make when typing URLs, e.g. case sensitivity in the scheme
     * and converts to Uri
     * @param url URL to correct
     * @return corrected as a Uri
     */
    public static Uri formatURL(String url) {
        if (url.startsWith("//")) {
            url = "https:" + url;
        }
        if (url.startsWith("/")) {
            url = "https://reddit.com" + url;
        }
        if (!url.contains("://")) {
            url = "http://" + url;
        }

        Uri uri = Uri.parse(url);
        return uri.normalizeScheme();
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
