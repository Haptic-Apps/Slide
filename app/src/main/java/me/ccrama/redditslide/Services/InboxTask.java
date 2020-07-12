package me.ccrama.redditslide.Services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import net.dean.jraw.managers.InboxManager;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Activities.Inbox;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.LogUtil;

public class InboxTask {


    public static class AuthenticationVerify extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> context;

        public AuthenticationVerify(@NotNull Context context) {
            this.context = new WeakReference<>(context);

        }

        @Override
        protected Void doInBackground(Void... params) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            Context context = this.context.get();

            if (Authentication.reddit == null && context != null) {
                new Authentication(context);
            }

            if (context != null) {
                try {
                    Authentication.me = Authentication.reddit.me();
                    Authentication.mod = Authentication.me.isMod();

                    Authentication.authentication.edit()
                            .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                            .apply();

                    if (Reddit.notificationTime != -1) {
                        Reddit.notifications = new NotificationJobScheduler(context);
                        Reddit.notifications.start(context);
                    }

                    if (Reddit.cachedData.contains("toCache")) {
                        Reddit.autoCache = new AutoCacheScheduler(context);
                        Reddit.autoCache.start(context);
                    }

                    final String name = Authentication.me.getFullName();
                    Authentication.name = name;
                    LogUtil.v("AUTHENTICATED");
                    UserSubscriptions.doCachedModSubs();

                    if (Authentication.reddit.isAuthenticated()) {
                        final Set<String> accounts =
                                Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                        if (accounts.contains(name)) { //convert to new system
                            accounts.remove(name);
                            accounts.add(name + ":" + Authentication.refresh);
                            Authentication.authentication.edit()
                                    .putStringSet("accounts", accounts)
                                    .apply(); //force commit
                        }
                        Authentication.isLoggedIn = true;
                        Reddit.notFirst = true;
                    }

                } catch (Exception ignored){

                }
            }
            return null;
        }
    }

    public static class ReadStatus extends AsyncTask<Void, Void, Void> {

        private WeakReference<Inbox> inbox;
        private boolean changed = false;

        public ReadStatus(@NotNull Inbox inbox) {
            this.inbox = new WeakReference<>(inbox);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                new InboxManager(Authentication.reddit).setAllRead();
                changed = true;
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Calling get() method just one time so it wont produce NPEs
            // As subsequent access may produce NPEs
            Inbox inbox = this.inbox.get();
            if (changed && inbox != null) { //restart the fragment
                inbox.updateInboxUI();
            }
        }
    }
}
