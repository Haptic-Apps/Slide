package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.util.UUID;

/**
 * Created by carlo_000 on 3/30/2015.
 */
public class Authentication {
    public static boolean isLoggedIn;
    public static RedditClient reddit;
    public static String name;
    public static String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    public static SharedPreferences authentication;
    public static String REDIRECT_URL = "http://www.ccrama.me";
    public static int inboxC;
    public Reddit a;
    public Authentication(Context a) {
        isLoggedIn = false;
        this.a = (Reddit) a;
        reddit = new RedditClient(UserAgent.of("android:me.ccrama.RedditSlide:v4.0 (by /u/ccrama)"));
        reddit.setLoggingMode(LoggingMode.ALWAYS);
        new VerifyCredentials().execute();

    }
    public static String refresh;

    public class VerifyCredentials extends AsyncTask<String, Void, Void> {



        @Override
        public void onPostExecute(Void voids){
            new SubredditStorage().execute(a);

        }

        @Override
        protected Void doInBackground(String... subs) {
            try {
                String token = authentication.getString("lasttoken", "");
                Log.v("Slide", token);
                if (!token.isEmpty()) {

                    final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                    OAuthHelper oAuthHelper = Authentication.reddit.getOAuthHelper();
                    oAuthHelper.setRefreshToken(token);
                    try {
                        OAuthData finalData = oAuthHelper.refreshToken(credentials);

                         refresh = oAuthHelper.getRefreshToken();
                        Authentication.reddit.authenticate(finalData);
                        if (Authentication.reddit.isAuthenticated()) {
                            final String name = Authentication.reddit.me().getFullName();
                            Authentication.name = name;
                            Authentication.isLoggedIn = true;


                        }
                    } catch (OAuthException e) {
                        e.printStackTrace();
                        Log.v("Slide", "RESTARTING CREDS");
                    }
                } else {
                    final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                    OAuthData authData = null;
                    try {
                        authData = Authentication.reddit.getOAuthHelper().easyAuth(fcreds);
                        Authentication.name = "LOGGEDOUT";

                    } catch (OAuthException e) {
                      //TODO fail
                    }
                    Authentication.reddit.authenticate(authData);


                }
                if (isLoggedIn)
                    inboxC = reddit.me().getInboxCount();
            } catch(Exception e){
              //TODO fail

            }
            return null;
        }

    }

}
