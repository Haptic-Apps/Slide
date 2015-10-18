package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
 * Created by ccrama on 3/30/2015.
 */
public class Authentication {
    public static boolean isLoggedIn;
    public static RedditClient reddit;
    private static boolean isNetworkAvailable(Context ac) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ac.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String name;
    public static String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    public static SharedPreferences authentication;
    public static String REDIRECT_URL = "http://www.ccrama.me";
    public static int inboxC;
    public Reddit a;

    public Authentication(Context a) {
        if(isNetworkAvailable(a)) {
            isLoggedIn = false;
            this.a = (Reddit) a;
            reddit = new RedditClient(UserAgent.of("android:me.ccrama.RedditSlide:v4.0 (by /u/ccrama)"));
            reddit.setLoggingMode(LoggingMode.ALWAYS);
                new VerifyCredentials().execute();

        }

    }
    public static class UpdateToken extends  AsyncTask<Void, Void, Void>{


        @Override
        protected Void doInBackground(Void... params) {
            Log.v("Slide", "REAUTH");
            if(isLoggedIn) {

                final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                Log.v("Slide", "REAUTH LOGGED IN");

                OAuthHelper oAuthHelper = reddit.getOAuthHelper();
                oAuthHelper.setRefreshToken(refresh);
                try {
                    OAuthData finalData = oAuthHelper.refreshToken(credentials);

                    refresh = oAuthHelper.getRefreshToken();
                    reddit.authenticate(finalData);
                    if (reddit.isAuthenticated()) {
                        final String name = reddit.me().getFullName();
                        Authentication.name = name;
                        Authentication.isLoggedIn = true;


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v("Slide", "RESTARTING CREDS");
                }
            } else {
                final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                OAuthData authData = null;
                if(reddit != null) {
                    try {

                        authData = reddit.getOAuthHelper().easyAuth(fcreds);
                        Authentication.name = "LOGGEDOUT";

                    } catch (OAuthException e) {
                        //TODO fail
                    }
                    reddit.authenticate(authData);
                }


            }
            return null;
        }
    }
    public static String refresh;

    public class VerifyCredentials extends AsyncTask<String, Void, Void> {



        @Override
        public void onPostExecute(Void voids){
            if(a.loader != null){
                a.loader.loading.setText("Updating your subreddits");
            }

                new SubredditStorage().execute(a);


        }

        @Override
        protected Void doInBackground(String... subs) {
            try {
                String token = authentication.getString("lasttoken", "");
                Log.v("Slide", "TOKEN IS " + token);
                if (!token.isEmpty()) {

                    final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                    OAuthHelper oAuthHelper = reddit.getOAuthHelper();
                    oAuthHelper.setRefreshToken(token);
                    try {
                        OAuthData finalData = oAuthHelper.refreshToken(credentials);

                         refresh = oAuthHelper.getRefreshToken();
                        reddit.authenticate(finalData);
                        if (reddit.isAuthenticated()) {
                            final String name = reddit.me().getFullName();
                            Log.v("Slide", "LOGGED IN AS " + name);
                            Authentication.name = name;
                            Authentication.isLoggedIn = true;


                        }
                    } catch (OAuthException e) {
                        e.printStackTrace();
                        Log.v("Slide", "RESTARTING CREDS");
                    }
                } else {
                    Log.v("Slide", "NOT LOGGED IN");

                    final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                    OAuthData authData = null;
                    try {
                        authData = reddit.getOAuthHelper().easyAuth(fcreds);
                        Authentication.name = "LOGGEDOUT";

                    } catch (OAuthException e) {
                      //TODO fail
                        e.printStackTrace();
                    }
                    reddit.authenticate(authData);


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
