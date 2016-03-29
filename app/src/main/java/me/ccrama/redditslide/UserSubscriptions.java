package me.ccrama.redditslide;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.ccrama.redditslide.Activities.Login;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class UserSubscriptions {
    public static SharedPreferences subscriptions;

    public static void doMainActivitySubs(MainActivity c) {
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
        loadMultireddits();
    }

    public static ArrayList<String> sort(ArrayList<String> copy) {
        ArrayList<String> subs = new ArrayList<>(copy);
        ArrayList<String> finals = new ArrayList<>();
        finals.add("frontpage");
        finals.add("all");

        if (subs.contains("frontpage")) {
            subs.remove("frontpage");
        }

        if (subs.contains("all")) {
            subs.remove("all");
        }

        if (subs.contains("random")) {
            subs.remove("random");
            finals.add("random");
        }

        if (subs.contains("randnsfw")) {
            subs.remove("randnsfw");
            finals.add("randnsfw");
        }

        if (subs.contains("friends")) {
            subs.remove("friends");
            finals.add("friends");
        }

        if (subs.contains("mod")) {
            subs.remove("mod");
            finals.add("mod");
        }

        java.util.Collections.sort(subs);
        finals.addAll(subs);
        return finals;

    }

    public static ArrayList<String> toreturn;

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
            for (String s : Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")) {
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
            return toReturn;
        } else {
            toReturn.addAll(Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"));
            return toReturn;
        }
    }

    public static void setSubscriptions(ArrayList<String> subs) {
        subscriptions.edit().putString(Authentication.name, Reddit.arrayToString(subs)).apply();
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

    public static MultiReddit getMultiredditByDisplayName(String displayName) {
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
        finalReturn.addAll(getSubscriptions(c));
        finalReturn.addAll(getHistory());
        finalReturn.addAll(getDefaults(c));
        return finalReturn;
    }

    public static ArrayList<String> getHistory() {
        String[] hist = subscriptions.getString("history", "").split(",");
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
    public void addSubToHistory(String s) {
        String history = subscriptions.getString("history", "");
        if (!history.contains(s)) {
            history += (history.contains(",") ? "," : "" + s);
            subscriptions.edit().putString("history", history);
        }
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
    public static ArrayList<String> sortNoExtras(ArrayList<String> copy) {
        ArrayList<String> subs = new ArrayList<>(copy);
        ArrayList<String> finals = new ArrayList<>();

        if (subs.contains("frontpage")) {
            subs.remove("frontpage");
            finals.add("frontpage");
        }

        if (subs.contains("all")) {
            subs.remove("all");
            finals.add("all");
        }

        if (subs.contains("random")) {
            subs.remove("random");
            finals.add("random");
        }

        if (subs.contains("randnsfw")) {
            subs.remove("randnsfw");
            finals.add("randnsfw");
        }

        if (subs.contains("friends")) {
            subs.remove("friends");
            finals.add("friends");
        }

        if (subs.contains("mod")) {
            subs.remove("mod");
            finals.add("mod");
        }

        java.util.Collections.sort(subs);
        finals.addAll(subs);
        return finals;

    }
    public static boolean isSubscriber(String s) {
        return subscriptions.getString(Authentication.name, "").toLowerCase().contains(s.toLowerCase());
    }
}
