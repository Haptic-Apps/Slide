package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.ImportantUserPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ccrama.redditslide.Activities.Login;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.DragSort.ReorderSubreddits;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class UserSubscriptions {
    public static final String       SUB_NAME_TO_PROPERTIES = "multiNameToSubs";
    public static final List<String> defaultSubs            =
            Arrays.asList("frontpage", "all", "announcements", "Art", "AskReddit", "askscience",
                    "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries",
                    "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology",
                    "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA",
                    "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis",
                    "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion",
                    "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics",
                    "science", "Showerthoughts", "space", "sports", "television", "tifu",
                    "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews",
                    "WritingPrompts");
    public static final List<String> specialSubreddits      =
            Arrays.asList("frontpage", "all", "random", "randnsfw", "myrandom", "friends", "mod");
    public static SharedPreferences subscriptions;
    public static SharedPreferences multiNameToSubs;

    public static void setSubNameToProperties(String name, String descrption) {
        multiNameToSubs.edit().putString(name, descrption).apply();
    }

    public static Map<String, String> getMultiNameToSubs(boolean all) {
        Map<String, String> multiNameToSubsMapBase = new HashMap<>();

        Map<String, ?> multiNameToSubsObject = multiNameToSubs.getAll();

        for (Map.Entry<String, ?> entry : multiNameToSubsObject.entrySet()) {
            multiNameToSubsMapBase.put(entry.getKey(), entry.getValue().toString());
        }
        if (all) multiNameToSubsMapBase.putAll(getSubsNameToMulti());

        Map<String, String> multiNameToSubsMap = new HashMap<>();

        for (Map.Entry<String, String> entries : multiNameToSubsMapBase.entrySet()) {
            multiNameToSubsMap.put(entries.getKey().toLowerCase(), entries.getValue());
        }

        return multiNameToSubsMap;
    }

    private static Map<String, String> getSubsNameToMulti() {
        Map<String, String> multiNameToSubsMap = new HashMap<>();

        Map<String, ?> multiNameToSubsObject = multiNameToSubs.getAll();

        for (Map.Entry<String, ?> entry : multiNameToSubsObject.entrySet()) {
            multiNameToSubsMap.put(entry.getValue().toString(), entry.getKey());
        }

        return multiNameToSubsMap;
    }

    public static void doMainActivitySubs(MainActivity c) {
        if (NetworkUtil.isConnected(c)) {
            String s = subscriptions.getString(Authentication.name, "");
            if (s.isEmpty()) {
                //get online subs
                c.updateSubs(syncSubscriptionsOverwrite(c));
            } else {
                ArrayList<String> subredditsForHome = new ArrayList<>();
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase());
                }
                c.updateSubs(subredditsForHome);
            }
            c.updateMultiNameToSubs(getMultiNameToSubs(false));

        } else {
            String s = subscriptions.getString(Authentication.name, "");
            List<String> subredditsForHome = new ArrayList<>();
            if (!s.isEmpty()) {
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase());
                }
            }
            ArrayList<String> finals = new ArrayList<>();
            List<String> offline = OfflineSubreddit.getAllFormatted();
            for (String subs : subredditsForHome) {
                if (offline.contains(subs)) {
                    finals.add(subs);
                }
            }
            for (String subs : offline) {
                if (!finals.contains(subs)) {
                    finals.add(subs);
                }
            }
            c.updateSubs(finals);
            c.updateMultiNameToSubs(getMultiNameToSubs(false));
        }
    }

    public static void doCachedModSubs() {
        if(modOf == null || modOf.isEmpty()) {
            String s = subscriptions.getString(Authentication.name + "mod", "");
            if (!s.isEmpty()) {
                modOf = new ArrayList<>();
                for (String s2 : s.split(",")) {
                    modOf.add(s2.toLowerCase());
                }
            }
        }
    }

    public static void cacheModOf(){
        subscriptions.edit().putString(Authentication.name + "mod", Reddit.arrayToString(modOf)).apply();
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
            syncMultiReddits(c);
            return null;
        }
    }

    public static ArrayList<String> getSubscriptions(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return syncSubscriptionsOverwrite(c);
        } else {
            ArrayList<String> subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                subredditsForHome.add(s2);
            }
            return subredditsForHome;
        }
    }

    public static ArrayList<String> getSubscriptionsForShortcut(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return syncSubscriptionsOverwrite(c);
        } else {
            ArrayList<String> subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                if (!s2.contains("/m/")) subredditsForHome.add(s2.toLowerCase());
            }
            return subredditsForHome;
        }
    }

    public static boolean hasSubs() {
        String s = subscriptions.getString(Authentication.name, "");
        return s.isEmpty();
    }

    public static ArrayList<String>      modOf;
    public static ArrayList<MultiReddit> multireddits;
    public static HashMap<String, List<MultiReddit>> public_multireddits =
            new HashMap<String, List<MultiReddit>>();

    public static void doOnlineSyncing() {
        if (Authentication.mod) {
            doModOf();
        }
        doFriendsOf();
        loadMultireddits();
    }

    public static ArrayList<String> toreturn;
    public static ArrayList<String> friends = new ArrayList<>();

    public static ArrayList<String> syncSubscriptionsOverwrite(final Context c) {
        toreturn = new ArrayList<>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                toreturn = syncSubreddits(c);
                toreturn = sort(toreturn);
                setSubscriptions(toreturn);
                return null;
            }
        }.execute();

        if (toreturn.isEmpty()) {
            //failed, load defaults
            toreturn.addAll(defaultSubs);
        }

        return toreturn;
    }

    public static ArrayList<String> syncSubreddits(Context c) {
        ArrayList<String> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(c)) {
            UserSubredditsPaginator pag =
                    new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);
            try {
                while (pag.hasNext()) {
                    for (net.dean.jraw.models.Subreddit s : pag.next()) {
                        toReturn.add(s.getDisplayName().toLowerCase());
                    }
                }
                if (toReturn.isEmpty() && subscriptions.getString(Authentication.name, "")
                        .isEmpty()) {
                    toreturn.addAll(defaultSubs);
                }
            } catch (Exception e) {
                //failed;
                e.printStackTrace();
            }
            addSubsToHistory(toReturn, true);
            return toReturn;
        } else {
            toReturn.addAll(defaultSubs);
            return toReturn;
        }
    }

    public static void syncMultiReddits(Context c) {
        try {
            multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
            for (MultiReddit multiReddit : multireddits) {
                if (MainActivity.multiNameToSubsMap.containsKey(
                        ReorderSubreddits.MULTI_REDDIT + multiReddit.getDisplayName())) {
                    StringBuilder concatenatedSubs = new StringBuilder();
                    for (MultiSubreddit subreddit : multiReddit.getSubreddits()) {
                        concatenatedSubs.append(subreddit.getDisplayName());
                        concatenatedSubs.append("+");
                    }
                    MainActivity.multiNameToSubsMap.put(
                            ReorderSubreddits.MULTI_REDDIT + multiReddit.getDisplayName(),
                            concatenatedSubs.toString());
                    UserSubscriptions.setSubNameToProperties(
                            ReorderSubreddits.MULTI_REDDIT + multiReddit.getDisplayName(),
                            concatenatedSubs.toString());
                }
            }
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public static void setSubscriptions(ArrayList<String> subs) {
        subscriptions.edit().putString(Authentication.name, Reddit.arrayToString(subs)).apply();
    }

    public static void switchAccounts() {
        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", "");
        Authentication.authentication.edit().remove("backedCreds").remove("expires").apply();
        editor.putBoolean("loggedin", Authentication.isLoggedIn);
        editor.putString("name", Authentication.name);
        editor.commit();
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
                multireddits =
                        new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
            } catch (Exception e) {
                multireddits = null;
                e.printStackTrace();
            }
        }
    }

    /**
     * @return list of multireddits if they are available, null if could not fetch multireddits
     */
    public static List<MultiReddit> getPublicMultireddits(final String profile) {
        if (profile.isEmpty()) {
            return getMultireddits();
        }

        if (public_multireddits.get(profile) == null) {
            // It appears your own multis are pre-loaded at some point
            // but some other user's multis obviously can't be so
            // don't return until we've loaded them.
            loadPublicMultireddits(profile);
        }
        return public_multireddits.get(profile);
    }

    private static void loadPublicMultireddits(String profile) {
        try {
            public_multireddits.put(profile, new ArrayList<>(
                    new MultiRedditManager(Authentication.reddit).getPublicMultis(profile)));
        } catch (Exception e) {
            public_multireddits.put(profile, null);
            e.printStackTrace();
        }
    }

    private static ArrayList<String> doModOf() {
        ArrayList<String> finished = new ArrayList<>();

        UserSubredditsPaginator pag =
                new UserSubredditsPaginator(Authentication.reddit, "moderator");
        pag.setLimit(100);
        try {
            while (pag.hasNext()) {
                for (net.dean.jraw.models.Subreddit s : pag.next()) {
                    finished.add(s.getDisplayName().toLowerCase());
                }
            }
            modOf = (finished);
            cacheModOf();
        } catch (Exception e) {
            //failed;
            e.printStackTrace();
        }

        return finished;
    }

    public static void doFriendsOfMain(MainActivity main) {
        main.doFriends(doFriendsOf());
    }

    private static List<String> doFriendsOf() {
        if (friends == null || friends.isEmpty()) {
            friends = new ArrayList<>();
            ArrayList<String> finished = new ArrayList<>();

            ImportantUserPaginator pag =
                    new ImportantUserPaginator(Authentication.reddit, "friends");
            pag.setLimit(100);
            try {
                while (pag.hasNext()) {
                    for (UserRecord s : pag.next()) {
                        finished.add(s.getFullName());
                    }
                }
                friends = (finished);
                return friends;

            } catch (Exception e) {
                //failed;
                e.printStackTrace();
            }
        }
        return friends;
    }

    public static MultiReddit getMultiredditByDisplayName(String displayName) {
        if (multireddits != null) {
            for (MultiReddit multiReddit : multireddits) {
                if (multiReddit.getDisplayName().equals(displayName)) {
                    return multiReddit;
                }
            }
        }
        return null;
    }

    public static MultiReddit getPublicMultiredditByDisplayName(String profile,
            String displayName) {
        if (profile.isEmpty()) {
            return getMultiredditByDisplayName(displayName);
        }

        if (public_multireddits.get(profile) != null) {
            for (MultiReddit multiReddit : public_multireddits.get(profile)) {
                if (multiReddit.getDisplayName().equals(displayName)) {
                    return multiReddit;
                }
            }
        }
        return null;
    }

    //Gets user subscriptions + top 500 subs + subs in history
    public static ArrayList<String> getAllSubreddits(Context c) {
        ArrayList<String> finalReturn = new ArrayList<>();
        List<String> history = getHistory();
        List<String> defaults = getDefaults(c);
        finalReturn.addAll(getSubscriptions(c));
        for (String s : finalReturn) {
            if (history.contains(s)) {
                history.remove(s);
            }
            if (defaults.contains(s)) {
                defaults.remove(s);
            }
        }
        for (String s : history) {
            if (defaults.contains(s)) {
                defaults.remove(s);
            }
        }
        finalReturn.addAll(history);
        finalReturn.addAll(defaults);
        return finalReturn;
    }

    //Gets user subscriptions + top 500 subs + subs in history
    public static ArrayList<String> getAllUserSubreddits(Context c) {
        ArrayList<String> finalReturn = new ArrayList<>();
        finalReturn.addAll(getSubscriptions(c));
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

    public static void addSubreddit(String s, Context c) {
        ArrayList<String> subs = getSubscriptions(c);
        subs.add(s);
        setSubscriptions(subs);
    }

    public static void removeSubreddit(String s, Context c) {
        ArrayList<String> subs = getSubscriptions(c);
        subs.remove(s);
        setSubscriptions(subs);
    }

    //Sets sub as "searched for", will apply to all accounts
    public static void addSubToHistory(String s) {
        String history = subscriptions.getString("subhistory", "");
        if (!history.contains(s.toLowerCase())) {
            history += "," + s.toLowerCase();
            subscriptions.edit().putString("subhistory", history).apply();
        }
    }

    //Sets a list of subreddits as "searched for", will apply to all accounts
    public static void addSubsToHistory(ArrayList<Subreddit> s2) {
        String history = subscriptions.getString("subhistory", "").toLowerCase();
        for (Subreddit s : s2) {
            if (!history.contains(s.getDisplayName().toLowerCase())) {
                history += "," + s.getDisplayName().toLowerCase();
            }
        }
        subscriptions.edit().putString("subhistory", history).apply();
    }

    public static void addSubsToHistory(ArrayList<String> s2, boolean b) {
        String history = subscriptions.getString("subhistory", "").toLowerCase();
        for (String s : s2) {
            if (!history.contains(s.toLowerCase())) {
                history += "," + s.toLowerCase();
            }
        }
        subscriptions.edit().putString("subhistory", history).apply();
    }

    public static ArrayList<Subreddit> syncSubredditsGetObject() {
        ArrayList<Subreddit> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn) {
            UserSubredditsPaginator pag =
                    new UserSubredditsPaginator(Authentication.reddit, "subscriber");
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

            addSubsToHistory(toReturn);
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
                    UserSubredditsPaginator pag =
                            new UserSubredditsPaginator(Authentication.reddit, "subscriber");
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
     * Sorts the subreddit ArrayList, keeping special subreddits at the top of the list (e.g.
     * frontpage, all, the random subreddits). Always adds frontpage and all
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
     * Sorts the subreddit ArrayList, keeping special subreddits at the top of the list (e.g.
     * frontpage, all, the random subreddits)
     *
     * @param unsorted the ArrayList to sort
     * @return the sorted ArrayList
     * @see #sort(ArrayList)
     */
    public static ArrayList<String> sortNoExtras(ArrayList<String> unsorted) {
        List<String> subs = new ArrayList<>(unsorted);
        ArrayList<String> finals = new ArrayList<>();

        for (String subreddit : specialSubreddits) {
            if (subs.contains(subreddit)) {
                subs.remove(subreddit);
                finals.add(subreddit);
            }
        }

        java.util.Collections.sort(subs, String.CASE_INSENSITIVE_ORDER);
        finals.addAll(subs);
        return finals;
    }

    public static boolean isSubscriber(String s, Context c) {
        return getSubscriptions(c).contains(s.toLowerCase());
    }

    public static class SubscribeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddits) {
            final AccountManager m = new AccountManager(Authentication.reddit);
            for (String subreddit : subreddits) {
                m.subscribe(Authentication.reddit.getSubreddit(subreddit));
            }
            return null;
        }
    }

    public static class UnsubscribeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddits) {
            final AccountManager m = new AccountManager(Authentication.reddit);
            for (String subreddit : subreddits) {
                m.unsubscribe(Authentication.reddit.getSubreddit(subreddit));
            }
            return null;
        }
    }
}
