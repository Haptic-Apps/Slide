package me.ccrama.redditslide.Activities;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.GetClosestColor;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 5/27/2015.
 */
public class Login extends BaseActivityAnim {
    private static final String CLIENT_ID    = "KI2Nl9A_ouG9Qw";
    private static final String REDIRECT_URL = "http://www.ccrama.me";
    Dialog                           d;
    CaseInsensitiveArrayList subNames;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstance);
        applyColorTheme("");
        try {
            setContentView(R.layout.activity_login);
        } catch(Exception e){
            finish();
            return;
        }
        setupAppBar(R.id.toolbar, R.string.title_login, true, true);

        String[] scopes = {
                "identity", "modcontributors", "modconfig", "modothers", "modwiki", "creddits",
                "livemanage", "account", "privatemessages", "modflair", "modlog", "report",
                "modposts", "modwiki", "read", "vote", "edit", "submit", "subscribe", "save",
                "wikiread", "flair", "history", "mysubreddits", "wikiedit"
        };
        if (Authentication.reddit == null) {
            new Authentication(getApplicationContext());
        }
        final OAuthHelper oAuthHelper = Authentication.reddit.getOAuthHelper();
        final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        String authorizationUrl =
                oAuthHelper.getAuthorizationUrl(credentials, true, scopes).toExternalForm();
        authorizationUrl = authorizationUrl.replace("www.", "i.");
        authorizationUrl = authorizationUrl.replace("%3A%2F%2Fi", "://www");
        Log.v(LogUtil.getTag(), "Auth URL: " + authorizationUrl);
        final WebView webView = (WebView) findViewById(R.id.web);
        webView.clearCache(true);
        webView.clearHistory();
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setMinimumFontSize(1);
        webSettings.setMinimumLogicalFontSize(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogUtil.v(url);
                if (url.contains("code=")) {
                    Log.v(LogUtil.getTag(), "WebView URL: " + url);
                    // Authentication code received, prevent HTTP call from being made.
                    webView.stopLoading();
                    new UserChallengeTask(oAuthHelper, credentials).execute(url);
                    webView.setVisibility(View.GONE);
                }
            }
        });

        webView.loadUrl(authorizationUrl);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.O)
    protected void setAutofill() {
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_AUTO);
    }

    private void doSubStrings(ArrayList<Subreddit> subs) {
        subNames = new CaseInsensitiveArrayList();
        for (Subreddit s : subs) {
            subNames.add(s.getDisplayName().toLowerCase(Locale.ENGLISH));
        }
        subNames = UserSubscriptions.sort(subNames);
        if (!subNames.contains("slideforreddit")) {
            new AlertDialogWrapper.Builder(Login.this).setTitle(
                    R.string.login_subscribe_rslideforreddit)
                    .setMessage(R.string.login_subscribe_rslideforreddit_desc)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            subNames.add(2, "slideforreddit");
                            UserSubscriptions.setSubscriptions(subNames);
                            Reddit.forceRestart(Login.this, true);
                        }
                    })
                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UserSubscriptions.setSubscriptions(subNames);
                            Reddit.forceRestart(Login.this, true);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            UserSubscriptions.setSubscriptions(subNames);
            Reddit.forceRestart(Login.this, true);
        }

    }

    public void doLastStuff(final ArrayList<Subreddit> subs) {

        d.dismiss();
        new AlertDialogWrapper.Builder(Login.this).setTitle(R.string.login_sync_colors)
                .setMessage(R.string.login_sync_colors_desc)
                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        for (Subreddit s : subs) {
                            if (s.getDataNode().has("key_color")
                                    && !s.getDataNode()
                                    .get("key_color")
                                    .asText()
                                    .isEmpty()
                                    && Palette.getColor(s.getDisplayName().toLowerCase(Locale.ENGLISH)) == Palette
                                    .getDefaultColor()) {
                                Palette.setColor(s.getDisplayName().toLowerCase(Locale.ENGLISH),
                                        GetClosestColor.getClosestColor(
                                                s.getDataNode().get("key_color").asText(),
                                                Login.this));
                            }

                        }
                        doSubStrings(subs);
                    }
                })
                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doSubStrings(subs);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        doSubStrings(subs);
                    }
                })
                .create()
                .show();
    }


    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {
        private final OAuthHelper    mOAuthHelper;
        private final Credentials    mCredentials;
        private       MaterialDialog mMaterialDialog;

        public UserChallengeTask(OAuthHelper oAuthHelper, Credentials credentials) {
            Log.v(LogUtil.getTag(), "UserChallengeTask()");
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
        }

        @Override
        protected void onPreExecute() {
            //Show a dialog to indicate progress
            MaterialDialog.Builder builder =
                    new MaterialDialog.Builder(Login.this).title(R.string.login_authenticating)
                            .progress(true, 0)
                            .content(R.string.misc_please_wait)
                            .cancelable(false);
            mMaterialDialog = builder.build();
            mMaterialDialog.show();
        }

        @Override
        protected OAuthData doInBackground(String... params) {
            try {
                OAuthData oAuthData = mOAuthHelper.onUserChallenge(params[0], mCredentials);
                if (oAuthData != null) {
                    Authentication.reddit.authenticate(oAuthData);
                    Authentication.isLoggedIn = true;
                    String refreshToken = Authentication.reddit.getOAuthData().getRefreshToken();
                    SharedPreferences.Editor editor = Authentication.authentication.edit();
                    Set<String> accounts = Authentication.authentication.getStringSet("accounts",
                            new HashSet<String>());
                    LoggedInAccount me = Authentication.reddit.me();
                    accounts.add(me.getFullName() + ":" + refreshToken);
                    Authentication.name = me.getFullName();
                    editor.putStringSet("accounts", accounts);
                    Set<String> tokens = Authentication.authentication.getStringSet("tokens",
                            new HashSet<String>());
                    tokens.add(refreshToken);
                    editor.putStringSet("tokens", tokens);
                    editor.putString("lasttoken", refreshToken);
                    editor.remove("backedCreds");
                    Reddit.appRestart.edit().remove("back").commit();
                    editor.commit();
                } else {
                    Log.e(LogUtil.getTag(), "Passed in OAuthData was null");
                }
                return oAuthData;
            } catch (IllegalStateException | NetworkException | OAuthException e) {
                // Handle me gracefully
                Log.e(LogUtil.getTag(), "OAuth failed");
                Log.e(LogUtil.getTag(), e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {
            //Dismiss old progress dialog
            mMaterialDialog.dismiss();

            if (oAuthData != null) {
                Reddit.appRestart.edit().putBoolean("firststarting", true).apply();

                UserSubscriptions.switchAccounts();
                d = new MaterialDialog.Builder(Login.this).cancelable(false)
                        .title(R.string.login_starting)
                        .progress(true, 0)
                        .content(R.string.login_starting_desc)
                        .build();
                d.show();

                UserSubscriptions.syncSubredditsGetObjectAsync(Login.this);
            } else {
                //Show a dialog if data is null
                MaterialDialog.Builder builder =
                        new MaterialDialog.Builder(Login.this).title(R.string.err_authentication)
                                .content(R.string.login_failed_err_decline)
                                .neutralText(R.string.btn_ok)
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@Nullable MaterialDialog dialog,
                                            @Nullable DialogAction which) {
                                        Reddit.forceRestart(Login.this, true);
                                        finish();

                                    }
                                });
                builder.show();
            }
        }
    }


}
