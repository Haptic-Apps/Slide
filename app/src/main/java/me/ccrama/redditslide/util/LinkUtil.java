package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
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
import android.widget.Toast;

import net.dean.jraw.models.Submission;

import org.apache.commons.lang3.StringEscapeUtils;

import me.ccrama.redditslide.Activities.Crosspost;
import me.ccrama.redditslide.Activities.MakeExternal;
import me.ccrama.redditslide.Activities.ReaderMode;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;

public class LinkUtil {

    private static CustomTabsSession           mCustomTabsSession;
    private static CustomTabsClient            mClient;
    private static CustomTabsServiceConnection mConnection;

    private LinkUtil() {
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap =
                Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Attempts to open the {@code url} in a custom tab. If no custom tab activity can be found,
     * falls back to opening externally
     *
     * @param url             URL to open
     * @param color           Color to provide to the browser UI if applicable
     * @param contextActivity The current activity
     * @param packageName     The package name recommended to use for connecting to custom tabs
     *                        related components.
     */
    public static void openCustomTab(@NonNull String url, int color,
            @NonNull Activity contextActivity, @NonNull String packageName) {
        Intent intent = new Intent(contextActivity, MakeExternal.class);
        intent.putExtra(Website.EXTRA_URL, url);
        PendingIntent pendingIntent = PendingIntent.getActivity(contextActivity, 0, intent, 0);

        CustomTabsIntent.Builder builder =
                new CustomTabsIntent.Builder(getSession()).setToolbarColor(color)
                        .setShowTitle(true)
                        .setStartAnimations(contextActivity, R.anim.slide_up_fade_in, 0)
                        .setExitAnimations(contextActivity, 0, R.anim.slide_down_fade_out)
                        .addDefaultShareMenuItem()
                        .addMenuItem(contextActivity.getString(R.string.open_links_externally),
                                pendingIntent)
                        .setCloseButtonIcon(drawableToBitmap(
                                ContextCompat.getDrawable(contextActivity,
                                        R.drawable.ic_arrow_back_white_24dp)));
        try {
            CustomTabsIntent customTabsIntent = builder.build();

            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(contextActivity,
                    formatURL(StringEscapeUtils.unescapeHtml4(url)));
        } catch (ActivityNotFoundException anfe) {
            Log.w(LogUtil.getTag(), "Unknown url: " + anfe);
            openExternally(url, contextActivity);
        }
    }

    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity,
            int adapterPosition, Submission submission) {
        if (!SettingValues.web) {
            // External browser
            openExternally(url, contextActivity);
            return;
        }

        if (SettingValues.firefox) {
            url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
            Uri uri = formatURL(url);

            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));

            contextActivity.startActivity(intent);
        } else {
            String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);

            if (packageName != null) {
                openCustomTab(url, color, contextActivity, packageName);
            } else {
                if (SettingValues.reader && (!SettingValues.readerNight
                        || SettingValues.isNight())) {
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
    }


    /**
     * Corrects mistakes users might make when typing URLs, e.g. case sensitivity in the scheme
     * and converts to Uri
     *
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
        if (!url.contains("://") && !url.startsWith("mailto:")) {
            url = "http://" + url;
        }

        Uri uri = Uri.parse(url);
        Uri toReturn;
        try {
            toReturn = uri.normalizeScheme();
        } catch (NoSuchMethodError e) {
            toReturn = uri;
        }
        return toReturn;
    }

    /**
     * Opens the {@code url} using the method the user has set in their preferences (custom tabs,
     * internal, external) falling back as needed
     *
     * @param url             URL to open
     * @param color           Color to provide to the browser UI if applicable
     * @param contextActivity The current activity
     */
    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity) {
        if (!SettingValues.web) {
            // External browser
            openExternally(url, contextActivity);
            return;
        }

        if (SettingValues.firefox) {
            url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
            Uri uri = formatURL(url);

            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setComponent(new ComponentName("org.mozilla.firefox", "org.mozilla.firefox.App"));

            contextActivity.startActivity(intent);
        } else {
            String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);

            if (packageName != null) {
                openCustomTab(url, color, contextActivity, packageName);
            } else {
                if (SettingValues.reader && (!SettingValues.readerNight
                        || SettingValues.isNight())) {
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
    }

    /**
     * Opens the {@code uri} externally or shows an application chooser if it is set to open in this
     * application
     *
     * @param uri     URI to open
     * @param context Current context
     */
    public static void openExternally(String url, Context context) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Uri uri = formatURL(url);

        final String id = BuildConfig.APPLICATION_ID;
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final PackageManager packageManager = context.getPackageManager();
        String resolvedName;
        try {
            resolvedName = intent.resolveActivity(packageManager).getPackageName();
        } catch (Exception e) {
            resolvedName = context.getPackageName();
        }
        if (resolvedName == null) return;

        if (resolvedName.matches(id)) {
            context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.misc_link_chooser)));
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

    public static void copyUrl(String url, Context context) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Link", url);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, R.string.submission_link_copied, Toast.LENGTH_SHORT).show();
    }

    public static void crosspost(Submission submission, Activity mContext) {
        Crosspost.toCrosspost = submission;
        mContext.startActivity(new Intent(mContext, Crosspost.class));
    }

    public static String getPackage(Intent intent) {
        String packageName = Reddit.getAppContext()
                .getPackageManager()
                .resolveActivity(intent,
                        PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        if (packageName.equals(Reddit.getAppContext().getPackageName())) {
            // Gets the default app from a URL that is most likely never link handled by another app, hopefully guaranteeing a browser
            return Reddit.getAppContext()
                    .getPackageManager()
                    .resolveActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blank.org")),
                            PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        }
        return packageName;
    }
}
