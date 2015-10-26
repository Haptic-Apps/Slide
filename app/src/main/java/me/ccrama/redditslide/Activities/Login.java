package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.AlertDialogWrapper;

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


/**
 * Created by ccrama on 5/27/2015.
 */
public class Login extends BaseActivity {
    private Toolbar toolbar;
    private static final String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    private static final String REDIRECT_URL = "http://www.ccrama.me";
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_login);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String[] scopes = {"identity", "account", "privatemessages", "modflair", "modlog", "report", "modposts", "modwiki", "read", "vote", "edit", "submit", "subscribe", "save", "wikiread", "flair", "history", "mysubreddits"};
        final OAuthHelper oAuthHelper = Authentication.reddit.getOAuthHelper();
        final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
        String authorizationUrl = oAuthHelper.getAuthorizationUrl(credentials, true, scopes)
                .toExternalForm();
        authorizationUrl = authorizationUrl.replace("www.", "i.");
        authorizationUrl = authorizationUrl.replace("%3A%2F%2Fi", "://www");
        Log.v("Slide", "Auth URL: " + authorizationUrl);
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
                    Log.v("Slide", "WebView URL: " + url);
                    new UserChallengeTask(oAuthHelper, credentials).execute(url);
                    webView.setVisibility(View.GONE);
                }
            }
        });
    }

    private final class UserChallengeTask extends AsyncTask<String, Void, OAuthData> {
        private final OAuthHelper mOAuthHelper;
        private final Credentials mCredentials;

        public UserChallengeTask(OAuthHelper oAuthHelper, Credentials credentials) {
            Log.v("Slide", "UserChallengeTask()");
            mOAuthHelper = oAuthHelper;
            mCredentials = credentials;
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
                    editor.putStringSet("accounts",accounts );
                    Set<String> tokens = Authentication.authentication.getStringSet("tokens", new HashSet<String>());
                    tokens.add(refreshToken);
                    editor.putStringSet("tokens", tokens);
                    editor.putString("lasttoken", refreshToken);
                    editor.apply();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Login.this);
                            builder.setTitle(R.string.login_restarting_title);
                            builder.setMessage(R.string.login_restart_msg);
                            builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                   Reddit.forceRestart(Login.this);
                                }
                            });
                            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Reddit.forceRestart(Login.this);

                                }
                            });

                            builder.show();
                        }
                    });



                } else {
                    Log.e("RedditSlide", "Passed in OAuthData was null");
                }
                return oAuthData;
            } catch (IllegalStateException | NetworkException | OAuthException e) {
                // Handle me gracefully
                Log.e("Slide", "OAuth failed");
                Log.e("Slide", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(OAuthData oAuthData) {



        }
    }


}
