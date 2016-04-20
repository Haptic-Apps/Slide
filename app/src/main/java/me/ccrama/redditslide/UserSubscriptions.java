package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

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

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class UserSubscriptions {
    public static SharedPreferences subscriptions;

    public static void doMainActivitySubs(MainActivity c) {
        if(NetworkUtil.isConnected(c)) {
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
        } else {
            String s = subscriptions.getString(Authentication.name, "");
            ArrayList<String> subredditsForHome = new ArrayList<>();
            if (!s.isEmpty()) {
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase());
                }
            }
            ArrayList<String> finals = new ArrayList<>();
            ArrayList<String> offline = OfflineSubreddit.getAllFormatted();
            for(String subs : subredditsForHome){
                if(offline.contains(subs)){
                    finals.add(subs);
                }
            }
            for(String subs : offline){
                if(!finals.contains(subs)){
                    finals.add(subs);
                }
            }
            c.updateSubs(finals);
        }
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

    public static ArrayList<String> getSubscriptions(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return syncSubscriptionsOverwrite(c);
        } else {
            ArrayList<String> subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                subredditsForHome.add(s2.toLowerCase());
            }
            return subredditsForHome;
        }
    }

    public static boolean hasSubs() {
        String s = subscriptions.getString(Authentication.name, "");
        return s.isEmpty();
    }

    public static ArrayList<String> modOf;
    private static ArrayList<MultiReddit> multireddits;


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
            for (String s : Arrays.asList("frontpage", "all", "announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")) {
                toreturn.add(s);
            }
        }

        return toreturn;
    }

    public static ArrayList<String> syncSubreddits(Context c) {
        ArrayList<String> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(c)) {
            UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);
            try {
                while (pag.hasNext()) {
                    for (net.dean.jraw.models.Subreddit s : pag.next()) {
                        toReturn.add(s.getDisplayName().toLowerCase());
                    }
                }
                if (toReturn.size() == 0) {
                    for (String s : Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")) {
                        toReturn.add(s);

                    }
                }
            } catch (Exception e) {
                //failed;
                e.printStackTrace();
            }
            addSubsToHistory(toReturn, true);
            return toReturn;
        } else {
            toReturn.addAll(Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"));
            return toReturn;
        }
    }

    public static void setSubscriptions(ArrayList<String> subs) {
        subscriptions.edit().putString(Authentication.name, Reddit.arrayToString(subs)).commit();
    }

    public static void switchAccounts() {
        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", "");
        Authentication.authentication.edit().remove("backedCreds").remove("expires").apply();
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
        ArrayList<String> history = getHistory();
        ArrayList<String> defaults = getDefaults(c);
        finalReturn.addAll(getSubscriptions(c));
        for(String s : finalReturn){
            if(history.contains(s)){
                history.remove(s);
            }
            if(defaults.contains(s)){
                defaults.remove(s);
            }
        }
        for(String s : history){
            if(defaults.contains(s)){
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
}
