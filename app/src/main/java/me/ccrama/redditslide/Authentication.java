package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by ccrama on 3/30/2015.
 */
public class Authentication {
    private static final String CLIENT_ID = "KI2Nl9A_ouG9Qw";
    private static final String REDIRECT_URL = "http://www.ccrama.me";
    public static boolean isLoggedIn;
    public static RedditClient reddit;
    public static LoggedInAccount me;
    public static boolean mod;
    public static String name;
    public static SharedPreferences authentication;
    public static ArrayList<String> modSubs;
    private static String refresh;
    private Reddit a;

    public boolean hasDone;
    public static boolean didOnline;

    public Authentication(Context context) {
        this.a = (Reddit) context;
        Reddit.setDefaultErrorHandler(context);

        if (NetworkUtil.isConnected(context)) {

            hasDone = true;
            isLoggedIn = false;
            reddit = new RedditClient(UserAgent.of("android:me.ccrama.RedditSlide:v4.5"));
            didOnline = true;

            new VerifyCredentials(context).execute();
        } else {
            for (String s : Authentication.authentication.getStringSet("accounts", new HashSet<String>())) {
                if (s.contains(authentication.getString("lasttoken", ""))) {
                    name = (s.split(":")[0]);
                    break;
                }
            }
            isLoggedIn = true;
            SubredditStorage.getSubredditsForHome(a);

        }


    }


    public void updateToken(Context c) {
        new UpdateToken(c).execute();
    }

    public class UpdateToken extends AsyncTask<Void, Void, Void> {

        Context context;

        public UpdateToken(Context c) {
            this.context = c;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (NetworkUtil.isConnected(context)) {
                didOnline = true;
                if (name != null && !name.isEmpty()) {
                    Log.v(LogUtil.getTag(), "REAUTH");
                    if (isLoggedIn) {
                        try {

                            final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                            Log.v(LogUtil.getTag(), "REAUTH LOGGED IN");

                            OAuthHelper oAuthHelper = reddit.getOAuthHelper();

                            oAuthHelper.setRefreshToken(refresh);
                            OAuthData finalData = oAuthHelper.refreshToken(credentials);


                            reddit.authenticate(finalData);
                            refresh = reddit.getOAuthHelper().getRefreshToken();

                            if (reddit.isAuthenticated()) {
                                if (me == null) {
                                    me = reddit.me();

                                }
                                Authentication.isLoggedIn = true;

                            }
                            Log.v(LogUtil.getTag(), "AUTHENTICATED");
                        } catch (Exception e) {
                            try {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialogWrapper.Builder(context).setTitle(R.string.err_general)
                                                .setMessage(R.string.err_no_connection)
                                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        new UpdateToken(context).execute();
                                                    }
                                                }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Reddit.forceRestart(context);
                                            }
                                        }).show();

                                    }
                                });
                            } catch (Exception ignored) {

                            }
                            e.printStackTrace();
                        }

                    } else {
                        final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                        OAuthData authData = null;
                        try {

                            authData = reddit.getOAuthHelper().easyAuth(fcreds);
                            Authentication.name = "LOGGEDOUT";
                            mod = false;


                            reddit.authenticate(authData);
                            Log.v(LogUtil.getTag(), "REAUTH LOGGED IN");

                        } catch (Exception e) {
                            try {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialogWrapper.Builder(context).setTitle(R.string.err_general)
                                                .setMessage(R.string.err_no_connection)
                                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        new UpdateToken(context).execute();
                                                    }
                                                }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Reddit.forceRestart(context);

                                            }
                                        }).show();
                                    }
                                });
                            } catch (Exception ignored) {

                            }
                            //TODO fail
                        }
                    }


                }


            }
            return null;

        }

    }

    public class VerifyCredentials extends AsyncTask<String, Void, Void> {
        Context mContext;

        public VerifyCredentials(Context context) {
            mContext = context;
        }


        @Override
        public void onPostExecute(Void voids) {

            didOnline = true;
            Log.v(LogUtil.getTag(), "LOADING SUBS");

            SubredditStorage.getSubredditsForHome(a);


        }

        @Override
        protected Void doInBackground(String... subs) {
            try {
                String token = authentication.getString("lasttoken", "");
                Log.v(LogUtil.getTag(), "TOKEN IS " + token);
                if (!token.isEmpty()) {

                    final Credentials credentials = Credentials.installedApp(CLIENT_ID, REDIRECT_URL);
                    OAuthHelper oAuthHelper = reddit.getOAuthHelper();
                    oAuthHelper.setRefreshToken(token);
                    try {
                        OAuthData finalData = oAuthHelper.refreshToken(credentials);

                        reddit.authenticate(finalData);
                        refresh = oAuthHelper.getRefreshToken();

                        Authentication.isLoggedIn = true;
                        me = reddit.me();
                        if (Reddit.notificationTime != -1) {
                            Reddit.notifications = new NotificationJobScheduler(a);
                            Reddit.notifications.start(mContext);

                        }
                        final String name = me.getFullName();
                        Authentication.name = name;
                        Log.v(LogUtil.getTag(), "AUTHENTICATED");

                        if (reddit.isAuthenticated()) {
                            final Set<String> accounts = authentication.getStringSet("accounts", new HashSet<String>());
                            if (accounts.contains(name)) { //convert to new system
                                accounts.remove(name);
                                accounts.add(name + ":" + token);
                                Authentication.authentication.edit().putStringSet("accounts", accounts).commit(); //force commit

                            }
                            mod = me.isMod();

                            Authentication.isLoggedIn = true;
                            if (Reddit.notificationTime != -1) {
                                Reddit.notifications = new NotificationJobScheduler(a);
                                Reddit.notifications.start(mContext);

                            }
                            return null;

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.v(LogUtil.getTag(), "NOT LOGGED IN");

                    final Credentials fcreds = Credentials.userlessApp(CLIENT_ID, UUID.randomUUID());
                    OAuthData authData = null;
                    try {
                        authData = reddit.getOAuthHelper().easyAuth(fcreds);
                        reddit.authenticate(authData);
                        Authentication.name = "LOGGEDOUT";
                        return null;

                    } catch (Exception e) {
                        try {
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialogWrapper.Builder(mContext).setTitle(R.string.err_general)
                                            .setMessage(R.string.err_no_connection)
                                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new UpdateToken(mContext).execute();
                                                }
                                            }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Reddit.forceRestart(mContext);

                                        }
                                    }).show();
                                }
                            });
                        } catch (Exception ignored) {

                        }
                        //TODO fail
                    }


                }

            } catch (Exception e) {
                //TODO fail


            }
            return null;
        }

    }

}
