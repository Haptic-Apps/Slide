package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.text.Html;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.Set;

import me.ccrama.redditslide.Activities.MakeExternal;
import me.ccrama.redditslide.Activities.ReaderMode;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;

public class LinkUtil {

    private static CustomTabsSession mCustomTabsSession;
    private static CustomTabsClient mClient;
    private static CustomTabsServiceConnection mConnection;

    private LinkUtil() {
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

    /**
     * Opens the {@code url} using the method the user has set in their preferences (custom tabs,
     * internal, external) falling back as needed
     * @param url URL to open
     * @param color Color to provide to the browser UI if applicable
     * @param contextActivity The current activity
     */
    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity) {
        if (!SettingValues.web) {
            // External browser
            Reddit.defaultShare(url, contextActivity);
            return;
        }

        String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);

        if (packageName != null) {
            Intent intent = new Intent(contextActivity, MakeExternal.class);
            intent.putExtra(Website.EXTRA_URL, url);
            PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, intent, 0);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession())
                    .setToolbarColor(color)
                    .setShowTitle(true)
                    .setStartAnimations(contextActivity, R.anim.slide_up_fade_in, 0)
                    .setExitAnimations(contextActivity, 0, R.anim.slide_down_fade_out)
                    .addDefaultShareMenuItem()
                    .addMenuItem(contextActivity.getString(R.string.open_links_externally), pendingIntent)
                    .setCloseButtonIcon(drawableToBitmap(ContextCompat.getDrawable(contextActivity, R.drawable.ic_arrow_back_white_24dp)));
            try {
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage(packageName);
                customTabsIntent.launchUrl(contextActivity, formatURL(url));
            } catch (ActivityNotFoundException anfe) {
                Log.w(LogUtil.getTag(), "Unknown url: " + anfe);
                Reddit.defaultShare(url, contextActivity);
            }
        } else {
            if(SettingValues.reader && (!SettingValues.readerNight || SettingValues.isNight())){
                //Reader mode
                Intent i = new Intent(contextActivity, ReaderMode.class);
                i.putExtra(ReaderMode.EXTRA_URL, url);
                i.putExtra(ReaderMode.EXTRA_COLOR, color);
                contextActivity.startActivity(i);
            } else {
                // Internal browser
                Intent i = new Intent(contextActivity, Website.class);
                i.putExtra(Website.EXTRA_URL, url);
                i.putExtra(Website.EXTRA_COLOR, color);
                contextActivity.startActivity(i);
            }
        }
    }

    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity, int adapterPosition, Submission submission) {
        if (!SettingValues.web) {
            // External browser
            Reddit.defaultShare(url, contextActivity);
            return;
        }

        String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);

        if (packageName != null) {
            Intent intent = new Intent(contextActivity, MakeExternal.class);
            intent.putExtra(Website.EXTRA_URL, url);
            PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, intent, 0);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession())
                    .setToolbarColor(color)
                    .setShowTitle(true)
                    .setStartAnimations(contextActivity, R.anim.slide_up_fade_in, 0)
                    .setExitAnimations(contextActivity, 0, R.anim.slide_down_fade_out)
                    .addDefaultShareMenuItem()
                    .addMenuItem(contextActivity.getString(R.string.open_links_externally), pendingIntent)
                    .setCloseButtonIcon(drawableToBitmap(ContextCompat.getDrawable(contextActivity, R.drawable.ic_arrow_back_white_24dp)));
            try {
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage(packageName);
                customTabsIntent.launchUrl(contextActivity, formatURL(url));
            } catch (ActivityNotFoundException anfe) {
                Log.w(LogUtil.getTag(), "Unknown url: " + anfe);
                Reddit.defaultShare(url, contextActivity);
            }
        } else {
            if(SettingValues.reader && (!SettingValues.readerNight || SettingValues.isNight())){
                //Reader mode
                Intent i = new Intent(contextActivity, ReaderMode.class);
                i.putExtra(ReaderMode.EXTRA_URL, url);
                PopulateSubmissionViewHolder.addAdaptorPosition(i, submission, adapterPosition);
                i.putExtra(ReaderMode.EXTRA_COLOR, color);
                contextActivity.startActivity(i);

            } else {
                // Internal browser
                Intent i = new Intent(contextActivity, Website.class);
                i.putExtra(Website.EXTRA_URL, url);
                PopulateSubmissionViewHolder.addAdaptorPosition(i, submission, adapterPosition);
                i.putExtra(Website.EXTRA_COLOR, color);
                contextActivity.startActivity(i);
            }
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
        Uri toReturn;
        try {
            toReturn = uri.normalizeScheme();
        } catch(NoSuchMethodError e){
            toReturn = uri;
        }
        return toReturn;
    }

    /**
     * Opens the {@code url} externally or shows an application chooser if it is set to open in this
     * application
     * @param url URL to open
     * @param context Current context
     * @param encoded If the URL is HTML encoded (e.g. includes {@code &amp;amp;})
     */
    public static void openExternally(String url, Context context, Boolean encoded) {
        if (encoded) url = Html.fromHtml(url).toString();
        Uri uri = formatURL(url);
        openExternally(uri, context);
    }

    /**
     * Opens the {@code uri} externally or shows an application chooser if it is set to open in this
     * application
     * @param uri URI to open
     * @param context Current context
     */
    public static void openExternally(Uri uri, Context context) {
        final String id = BuildConfig.APPLICATION_ID;
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final PackageManager packageManager = context.getPackageManager();
        String resolvedName;
        try {
            resolvedName = intent.resolveActivity(packageManager).getPackageName();
        } catch(Exception e){
            resolvedName = context.getPackageName();
        }
        if (resolvedName == null)
            return;

        if (resolvedName.matches(id)) {
            context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.misc_link_chooser))
            );
            return;
        }

        context.startActivity(intent);
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
