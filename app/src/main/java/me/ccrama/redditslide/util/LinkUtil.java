package me.ccrama.redditslide.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsCallback;
import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;
import androidx.core.content.ContextCompat;

import net.dean.jraw.models.Submission;

import org.apache.commons.text.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import me.ccrama.redditslide.Activities.Crosspost;
import me.ccrama.redditslide.Activities.MakeExternal;
import me.ccrama.redditslide.Activities.ReaderMode;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;

import static me.ccrama.redditslide.Fragments.SettingsHandlingFragment.LinkHandlingMode;

public class LinkUtil {

    private static CustomTabsSession           mCustomTabsSession;
    private static CustomTabsClient            mClient;
    private static CustomTabsServiceConnection mConnection;

    public static final String EXTRA_URL        = "url";
    public static final String EXTRA_COLOR      = "color";
    public static final String ADAPTER_POSITION = "adapter_position";

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
        intent.putExtra(LinkUtil.EXTRA_URL, url);
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
            openExternally(url);
        }
    }

    public static void openUrl(@NonNull String url, int color, @NonNull Activity contextActivity,
            @Nullable Integer adapterPosition, @Nullable Submission submission) {
        if (!(contextActivity instanceof ReaderMode) && ((SettingValues.readerMode
                && !SettingValues.readerNight)
                || SettingValues.readerMode
                && SettingValues.readerNight
                && SettingValues.isNight())) {
            Intent i = new Intent(contextActivity, ReaderMode.class);
            openIntentThemed(i, url, color, contextActivity, adapterPosition, submission);
        } else if (SettingValues.linkHandlingMode == LinkHandlingMode.EXTERNAL.getValue()) {
            openExternally(url);
        } else {
            String packageName = CustomTabsHelper.getPackageNameToUse(contextActivity);
            if (SettingValues.linkHandlingMode == LinkHandlingMode.CUSTOM_TABS.getValue()
                    && packageName != null) {
                openCustomTab(url, color, contextActivity, packageName);
            } else {
                Intent i = new Intent(contextActivity, Website.class);
                openIntentThemed(i, url, color, contextActivity, adapterPosition, submission);
            }
        }
    }

    private static void openIntentThemed(@NonNull Intent intent, @NonNull String url, int color,
            @NonNull Activity contextActivity, @Nullable Integer adapterPosition,
            @Nullable Submission submission) {
        intent.putExtra(EXTRA_URL, url);
        if (adapterPosition != null && submission != null) {
            PopulateSubmissionViewHolder.addAdaptorPosition(intent, submission, adapterPosition);
        }
        intent.putExtra(EXTRA_COLOR, color);
        contextActivity.startActivity(intent);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return uri.normalizeScheme();
        } else {
            return uri;
        }
    }

    public static boolean tryOpenWithVideoPlugin(@NonNull String url) {
        if (Reddit.videoPlugin) {
            try {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setClassName(
                        Reddit.getAppContext().getString(R.string.youtube_plugin_package),
                        Reddit.getAppContext().getString(R.string.youtube_plugin_class));
                sharingIntent.putExtra("url", removeUnusedParameters(url));
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Reddit.getAppContext().startActivity(sharingIntent);
                return true;

            } catch (Exception ignored) {
                return false;
            }
        } else {
            return false;
        }
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
        openUrl(url, color, contextActivity, null, null);
    }

    /**
     * Opens the {@code uri} externally or shows an application chooser if it is set to open in this
     * application
     *
     * @param url     URL to open
     */
    public static void openExternally(String url) {
        url = StringEscapeUtils.unescapeHtml4(Html.fromHtml(url).toString());
        Uri uri = formatURL(url);

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        overridePackage(intent);
        Reddit.getAppContext().startActivity(intent);
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

    public static void overridePackage(Intent intent) {
        String packageName = Reddit.getAppContext()
                .getPackageManager()
                .resolveActivity(intent, 0).activityInfo.packageName;

        // Gets the default app from a URL that is most likely never link handled by another app, hopefully guaranteeing a browser
        String browserPackageName = Reddit.getAppContext()
                .getPackageManager()
                .resolveActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ccrama.me/")),
                        0).activityInfo.packageName;

        String packageToSet = packageName;

        if (packageName.equals(Reddit.getAppContext().getPackageName())) {
            packageToSet = browserPackageName;
        }

        if (packageToSet.equals(browserPackageName) && (SettingValues.selectedBrowser != null
                && !SettingValues.selectedBrowser.isEmpty())) {
            try {
                Reddit.getAppContext()
                        .getPackageManager()
                        .getPackageInfo(SettingValues.selectedBrowser,
                                PackageManager.GET_ACTIVITIES);
                packageToSet = SettingValues.selectedBrowser;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        if (!packageToSet.equals(packageName)) {
            intent.setPackage(packageToSet);
        }
    }

    public static String removeUnusedParameters(String url) {
        String returnUrl = url;
        try {
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String[] paramArray = urlParts[1].split("&");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(urlParts[0]);
                for (int i = 0; i < paramArray.length; i++) {
                    String[] paramPairArray = paramArray[i].split("=");
                    if (paramPairArray.length > 1) {
                        if (i == 0) {
                            stringBuilder.append("?");
                        } else {
                            stringBuilder.append("&");
                        }
                        stringBuilder.append(URLDecoder.decode(paramPairArray[0], "UTF-8"));
                        stringBuilder.append("=");
                        stringBuilder.append(URLDecoder.decode(paramPairArray[1], "UTF-8"));
                    }
                }
                returnUrl = stringBuilder.toString();
            }
            return returnUrl;
        } catch (UnsupportedEncodingException ignored) {
            return returnUrl;
        }
    }
}
