package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.util.LogUtil;


/**
 * Created by ccrama on 5/27/2015.
 */
public class Login extends BaseActivityAnim {
    private static final String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    private static final String REDIRECT_URL = "http://www.ccrama.me";

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        applyColorTheme("");
        setContentView(R.layout.activity_login);
        setupAppBar(R.id.toolbar, R.string.title_login, true, true);

        String[] scopes = {"identity", "account", "privatemessages", "modflair", "modlog", "report", "modposts", "modwiki", "read", "vote", "edit", "submit", "subscribe", "save", "wikiread", "flair", "history", "mysubreddits"};
        final OAuthHelper oAuthHelper = Authentication.reddit.getOAuthHelper();
        final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        String authorizationUrl = oAuthHelper.getAuthorizationUrl(credentials, true, scopes)
                .toExternalForm();
        authorizationUrl = authorizationUrl.replace("www.", "i.");
        authorizationUrl = authorizationUrl.replace("%3A%2F%2Fi", "://www");
        Log.v(LogUtil.getTag(), "Auth URL: " + authorizationUrl);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        final WebView webView = (WebView) findViewById(R.id.web);

        webView.loadUrl(authorizationUrl);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
//                activity.setProgress(newProgress * 1000);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    Log.v(LogUtil.getTag(), "WebView URL: " + url);
                    new UserChallengeTask(oAuthHelper, credentials).execute(url);
                    webView.setVisibility(View.GONE);
                }
            }
        });
    }

    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {
        private final OAuthHelper mOAuthHelper;
        private final Credentials mCredentials;
        private MaterialDialog mMaterialDialog;

        public UserChallengeTask(OAuthHelper oAuthHelper, Credentials credentials) {
            Log.v(LogUtil.getTag(), "UserChallengeTask()");
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
        }

        @Override
        protected void onPreExecute() {
            //Show a dialog to indicate progress
            MaterialDialog.Builder builder = new MaterialDialog.Builder(Login.this)
                    .title("Slide is authenticating")
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
                    Set<String> accounts = Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                    accounts.add(Authentication.reddit.me().getFullName());
                    editor.putStringSet("accounts", accounts);
                    Set<String> tokens = Authentication.authentication.getStringSet("tokens", new HashSet<String>());
                    tokens.add(refreshToken);
                    editor.putStringSet("tokens", tokens);
                    editor.putString("lasttoken", refreshToken);
                    Reddit.appRestart.edit().remove("back").commit();
                    editor.apply();
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
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Login.this)
                        .setTitle(R.string.login_restarting_title)
                        .setMessage(R.string.login_restart_msg)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Reddit.forceRestart(Login.this);
                            }
                        });
                builder.show();
            } else {
                //Show a dialog if data is null
                MaterialDialog.Builder builder = new MaterialDialog.Builder(Login.this)
                        .title(R.string.err_authentication)
                        .content(R.string.err_retry_later)
                        .neutralText(R.string.btn_ok)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@Nullable MaterialDialog dialog, @Nullable DialogAction which) {
                                Reddit.forceRestart(Login.this);
                            }
                        });
                builder.show();
            }
        }
    }


}
