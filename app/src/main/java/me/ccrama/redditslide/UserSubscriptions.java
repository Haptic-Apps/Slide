package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.ImportantUserPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.ccrama.redditslide.Activities.Login;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.util.NetworkUtil;

import static me.ccrama.redditslide.UserSubscriptions.SubscriptionType.HIDDEN;
import static me.ccrama.redditslide.UserSubscriptions.SubscriptionType.LOCAL;
import static me.ccrama.redditslide.UserSubscriptions.SubscriptionType.NORMAL;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class UserSubscriptions {
    public static SharedPreferences subscriptions;
    public static ArrayList<String> modOf;
    public static ArrayList<Subscription> toreturn;
    public static ArrayList<String> friends = new ArrayList<>();
    private static ArrayList<MultiReddit> multireddits;

    public static void doMainActivitySubs(MainActivity c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            c.updateSubs(getNamesFromSubscriptions(loadSubscriptionsOverwrite(c), false));
        } else {
            ArrayList<String> subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                subredditsForHome.add(s2.toLowerCase());
            }
            c.updateSubs(subredditsForHome);
        }
    }

    /**
     * Gets subscriptions from sharedPrefs or syncs them if there are no subs stored
     *
     * @param c context
     * @return ArrayList of all subscriptions, including multis
     */
    public static ArrayList<Subscription> getSubscriptions(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return loadSubscriptionsOverwrite(c);
        } else {
            Gson gson = new Gson();

            return gson.fromJson(s, new TypeToken<List<Subscription>>() {
            }.getType());
        }
    }

    public static boolean hasSubs() {
        String s = subscriptions.getString(Authentication.name, "");
        return s.isEmpty();
    }

    public static void doOnlineSyncing() {
        if (Authentication.mod) {
            doModOf();
        }
        doFriendsOf();
        loadMultireddits();
    }

    public static ArrayList<Subscription> loadSubscriptionsOverwrite(final Context c) {
        toreturn = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                toreturn = loadSubreddits(c);
                toreturn = sort(toreturn);
                setSubscriptions(toreturn);
                return null;
            }
        }.execute();

        if (toreturn.isEmpty()) {
            //failed, load defaults
            for (String s : Arrays.asList(c.getString(R.string.default_subs_csv))) {
                toreturn.add(new Subscription(s, false));
            }
        }

        return toreturn;
    }

    /**
     * Loads subreddits
     *
     * @param c Context
     * @return Subreddits the user is subscribed to, default subs on error
     */
    public static ArrayList<Subscription> loadSubreddits(Context c) {
        ArrayList<Subscription> subs = new ArrayList<>();
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(c)) {
            UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);
            try {
                while (pag.hasNext()) {
                    for (net.dean.jraw.models.Subreddit s : pag.next()) {
                        subs.add(new Subscription(s, NORMAL));
                    }
                }
                if (subs.size() != 0) {
                    addSubsToHistory(subs);
                    return subs;
                }
            } catch (Exception e) {
                //failed;
                e.printStackTrace();
            }

        }
        //Return default subs on error / when sub size is empty / when logged out
        for (String s : Arrays.asList(c.getString(R.string.default_subs_csv))) {
            subs.add(new Subscription(s, false));
        }
        return subs;

    }

    /**
     * @param subscriptions ArrayList of subscriptions
     * @param excludeMultis If multireddits should be excluded
     * @return ArrayList of subscription names
     */
    public static ArrayList<String> getNamesFromSubscriptions(ArrayList<Subscription> subscriptions, boolean excludeMultis) {
        ArrayList<String> toReturn = new ArrayList<>();
        for (Subscription s : subscriptions) {
            //Don't add multis if onlySubs is true
            if (!excludeMultis || !s.isMulti()) {
                toReturn.add(s.getName());
            }
        }
        return toReturn;
    }

    /**
     * Stores the the list in a sharedPref
     *
     * @param subs Subs to store
     */
    public static void setSubscriptions(ArrayList<Subscription> subs) {
        String list = new Gson().toJson(subs);
        subscriptions.edit().putString(Authentication.name, list).commit();
    }

    public static void switchAccounts() {
        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", "");
        editor.putBoolean("loggedin", Authentication.isLoggedIn);
        editor.putString("name", Authentication.name);
        editor.apply();
    }

    /**
     * @return list of multireddits if they are available, null if could not fetch multireddits
     */
    public static List<MultiReddit> getMultireddits() {
        if (multireddits == null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    loadMultireddits();
                    return null;
                }
            }.execute();
        }
        return multireddits;
    }

    private static void loadMultireddits() {
        if (Authentication.isLoggedIn && Authentication.didOnline) {
            try {
                multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
            } catch (Exception e) {
                multireddits = null;
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<String> doModOf() {
        ArrayList<String> finished = new ArrayList<>();

        UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "moderator");
        pag.setLimit(100);
        try {
            while (pag.hasNext()) {
                for (net.dean.jraw.models.Subreddit s : pag.next()) {
                    finished.add(s.getDisplayName().toLowerCase());
                }
            }
            modOf = (finished);
        } catch (Exception e) {
            //failed;
            e.printStackTrace();
        }

        return finished;
    }

    private static void doFriendsOf() {
        friends = new ArrayList<>();
        ArrayList<String> finished = new ArrayList<>();

        ImportantUserPaginator pag = new ImportantUserPaginator(Authentication.reddit, "friends");
        pag.setLimit(100);
        try {
            while (pag.hasNext()) {
                for (UserRecord s : pag.next()) {
                    finished.add(s.getFullName());
                }
            }
            friends = (finished);
        } catch (Exception e) {
            //failed;
            e.printStackTrace();
        }
    }

    public static MultiReddit getMultiredditByDisplayName(String displayName) {
        if (multireddits != null)
            for (MultiReddit multiReddit : multireddits) {
                if (multiReddit.getDisplayName().equals(displayName)) {
                    return multiReddit;
                }
            }
        return null;
    }

    //Gets user subscriptions + top 500 subs + subs in history
    public static ArrayList<String> getAllSubreddits(Context c) {
        ArrayList<String> finalReturn = new ArrayList<>();
        finalReturn.addAll(getNamesFromSubscriptions(getSubscriptions(c), true));
        finalReturn.removeAll(getHistory());
        finalReturn.addAll(getHistory());
        finalReturn.removeAll(getDefaults(c));
        finalReturn.addAll(getDefaults(c));
        return finalReturn;
    }

    //Gets user subscriptions + top 500 subs + subs in history
    public static ArrayList<String> getAllUserSubreddits(Context c) {
        ArrayList<String> finalReturn = new ArrayList<>();
        finalReturn.addAll(getNamesFromSubscriptions(getSubscriptions(c), true));
        finalReturn.removeAll(getHistory());
        finalReturn.addAll(getHistory());
        return finalReturn;
    }

    public static ArrayList<String> getHistory() {
        String[] hist = subscriptions.getString("subhistory", "").toLowerCase().split(",");
        ArrayList<String> history = new ArrayList<>();
        Collections.addAll(history, hist);
        return history;
    }

    public static ArrayList<String> getDefaults(Context c) {
        ArrayList<String> history = new ArrayList<>();
        Collections.addAll(history, c.getString(R.string.top_500_csv).split(","));
        return history;
    }

    /**
     * Add a new subreddit subscription
     *
     * @param s Subreddit to add
     * @param c context
     */
    public static void addSubreddit(Subreddit s, Context c) {
        ArrayList<Subscription> subs = getSubscriptions(c);
        subs.add(new Subscription(s, NORMAL));
        setSubscriptions(subs);
    }

    /**
     * Hides subreddit from drawer if subscribed too, otherwise remove it completely
     *
     * @param s Subreddit name
     * @param c context
     */
    public static void removeSubreddit(String s, Context c) {
        ArrayList<Subscription> subs = getSubscriptions(c);
        ArrayList<String> subNames = getNamesFromSubscriptions(subs, false);
        int i = 0;
        for (String name : subNames) {
            if (name.equals(s) && !subs.get(i).isMulti()) {
                Subscription sub = subs.get(i);
                if (sub.isSubscribed()) {
                    sub.setType(HIDDEN);
                    subs.set(i, sub);
                } else subs.remove(i);
                break;
            }
            i++;
        }
        setSubscriptions(subs);
    }

    /**
     * Sets sub as "searched for", will apply to all accounts
     *
     * @param s Subreddit name
     */
    public static void addSubToHistory(String s) {
        String history = subscriptions.getString("subhistory", "");
        if (!history.contains(s.toLowerCase())) {
            history += "," + s.toLowerCase();
            subscriptions.edit().putString("subhistory", history).apply();
        }
    }

    /**
     * Sets a list of subreddits as "searched for", will apply to all accounts
     *
     * @param subscriptions ArrayList of subscriptions
     */
    public static void addSubsToHistory(ArrayList<Subscription> subscriptions) {
        String history = UserSubscriptions.subscriptions.getString("subhistory", "").toLowerCase();
        for (Subscription s : subscriptions) {
            if (!history.contains(s.getName()) && !s.isMulti()) {
                history += "," + s.getName();
            }
        }
        UserSubscriptions.subscriptions.edit().putString("subhistory", history).apply();
    }

    /**
     * Sets a list of subreddits as "searched for", will apply to all accounts
     *
     * @param subreddits ArrayList of subreddit names
     */
    public static void addSubStringsToHistory(ArrayList<Subreddit> subreddits) {
        String history = subscriptions.getString("subhistory", "").toLowerCase();
        for (Subreddit s : subreddits) {
            if (!history.contains(s.getDisplayName().toLowerCase())) {
                history += "," + s.getDisplayName().toLowerCase();
            }
        }
        subscriptions.edit().putString("subhistory", history).apply();
    }

    public static ArrayList<Subreddit> syncSubredditsGetObject() {
        ArrayList<Subreddit> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn) {
            UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);


            try {
                while (pag.hasNext()) {
                    for (net.dean.jraw.models.Subreddit s : pag.next()) {
                        toReturn.add(s);
                    }
                }


            } catch (Exception e) {
                //failed;
                e.printStackTrace();
            }

            addSubStringsToHistory(toReturn);
            return toReturn;
        }
        return toReturn;
    }

    public static void syncSubredditsGetObjectAsync(final Login mainActivity) {
        final ArrayList<Subreddit> toReturn = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (Authentication.isLoggedIn) {
                    UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
                    pag.setLimit(100);


                    try {
                        while (pag.hasNext()) {
                            for (net.dean.jraw.models.Subreddit s : pag.next()) {
                                toReturn.add(s);
                            }
                        }


                    } catch (Exception e) {
                        //failed;
                        e.printStackTrace();
                    }
                }
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mainActivity.doLastStuff(toReturn);
            }
        }.execute();
    }

    /**
     * Sorts the subreddit ArrayList, keeping special subreddits at the top of the list
     * (e.g. frontpage, all, the random subreddits). Always adds frontpage and all
     *
     * @param unsorted the ArrayList to sort
     * @return the sorted ArrayList
     * @see #sortNoExtras(ArrayList)
     */
    public static ArrayList<String> sort(ArrayList<String> unsorted) {
        ArrayList<String> subs = new ArrayList<>(unsorted);

        if (!subs.contains("frontpage")) {
            subs.add("frontpage");
        }

        if (!subs.contains("all")) {
            subs.add("all");
        }

        return sortNoExtras(subs);
    }

    /**
     * Sorts the subreddit ArrayList, keeping special subreddits at the top of the list
     * (e.g. frontpage, all, the random subreddits)
     *
     * @param unsorted the ArrayList to sort
     * @return the sorted ArrayList
     * @see #sort(ArrayList)
     */
    public static ArrayList<String> sortNoExtras(ArrayList<String> unsorted) {
        ArrayList<String> subs = new ArrayList<>(unsorted);
        ArrayList<String> finals = new ArrayList<>();
        final List<String> specialSubreddits = Arrays.asList(
                "frontpage", "all", "random", "randnsfw", "myrandom", "friends", "mod"
        );

        for (String subreddit : specialSubreddits) {
            if (subs.contains(subreddit)) {
                subs.remove(subreddit);
                finals.add(subreddit);
            }
        }

        java.util.Collections.sort(subs);
        finals.addAll(subs);
        return finals;

    }

    public static boolean isSubscriber(String s) {
        return subscriptions.getString(Authentication.name, "").toLowerCase().contains(s.toLowerCase());
    }

    public enum SubscriptionType {
        NORMAL, //Normal subscription
        HIDDEN, //Subscribed, but hidden in sub list
        LOCAL //Local only subscription (does not show on frontpage)
    }

    public static class SyncMultireddits extends AsyncTask<Void, Void, Boolean> {

        Context c;

        public SyncMultireddits(Context c) {
            this.c = c;
        }

        @Override
        public void onPostExecute(Boolean b) {
            Intent i = new Intent(c, MultiredditOverview.class);
            c.startActivity(i);
            ((Activity) c).finish();
        }

        @Override
        public Boolean doInBackground(Void... params) {
            try {
                multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
                return null;
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * This class stores subreddits and multireddits. You can set whether it's
     * a normal subscription,
     * a local only subscription (for multireddits it's called collection)
     * or a hidden subscription (doesn't show up in drawer)
     */
    public static class Subscription {
        String mName;
        SubscriptionType mType;
        boolean mIsMulti;
        @Nullable
        Subreddit mSubreddit;
        @Nullable
        MultiReddit mMultiReddit;

        /**
         * Add Subscription (Multi or Subreddit) from it's name
         * It is LOCAL ONLY!
         *
         * @param name Name of the subscription
         */
        public Subscription(String name, boolean isMulti) {
            this.mName = name.toLowerCase();
            this.mType = LOCAL;
            this.mIsMulti = isMulti;
        }

        /**
         * Add subreddit from Subreddit object
         *
         * @param subreddit Subreddit object
         * @param type      Type of the subreddit
         */
        public Subscription(Subreddit subreddit, SubscriptionType type) {
            this.mName = subreddit.getDisplayName().toLowerCase();
            this.mType = type;
            this.mIsMulti = false;
        }

        /**
         * Add multireddit from Multireddit object
         *
         * @param multiReddit Multireddit object
         * @param type        Type of the multireddit
         */
        public Subscription(MultiReddit multiReddit, SubscriptionType type) {
            this.mName = multiReddit.getDisplayName().toLowerCase();
            this.mType = type;
            this.mIsMulti = true;
        }

        /**
         * @return Display name of the multireddit / subreddit
         */
        public String getName() {
            return this.mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        @Nullable
        public Subreddit getSubreddit() {
            if (!isMulti()) return mSubreddit;
            else return null;
        }

        @Nullable
        public MultiReddit getMultiReddit() {
            if (isMulti()) return mMultiReddit;
            else return null;
        }

        /**
         * @return Type of the subscription
         */
        public SubscriptionType getType() {
            return this.mType;
        }

        /**
         * Set type of the subscription. This can be used to hide subscriptions
         *
         * @param type New Type
         */
        public void setType(SubscriptionType type) {
            this.mType = type;
        }

        public boolean isMulti() {
            return this.mIsMulti;
        }

        /**
         * @return If user is subscribed to the subreddit / multireddit on reddit.com
         */
        public boolean isSubscribed() {
            return (mType == HIDDEN || mType == NORMAL);
        }

        /**
         * @return If the user is subscribed, but if it's hidden in drawer
         */
        public boolean isHidden() {
            return mType == HIDDEN;
        }

        /**
         * @return If subreddit is only stored local (for multireddits it's called collection)
         */
        public boolean isLocalOnly() {
            return mType == LOCAL;
        }


    }
}

