package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.Shortcut;
import me.ccrama.redditslide.Widget.Configure;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class SubredditStorage {
    public static SharedPreferences subscriptions;
    public static ArrayList<String> modOf;
    private static ArrayList<MultiReddit> multireddits;
    public static ArrayList<String> subredditsForHome;
    public static Shortcut shortcut;
    public static Configure configure;

    public static void saveState(boolean login) {

        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", login ? "" : Reddit.arrayToString(subredditsForHome));
        editor.putBoolean("loggedin", Authentication.isLoggedIn);
        editor.putString("name", Authentication.name);
        editor.apply();

    }

    public static void saveState(boolean login, boolean name) {

        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", login ? "" : Reddit.arrayToString(subredditsForHome));
        editor.putBoolean("loggedin", Authentication.isLoggedIn);
        editor.putString("name", "");
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

    public static MultiReddit getMultiredditByDisplayName(String displayName) {
        for (MultiReddit multiReddit : multireddits) {
            if (multiReddit.getDisplayName().equals(displayName)) {
                return multiReddit;
            }
        }
        return null;
    }

    public static void getSubredditsForHome(Reddit a) {
        Log.v(LogUtil.getTag(), "NAME IS " + Authentication.name);
        String s = subscriptions.getString(Authentication.name, "");
        final boolean online = NetworkUtil.isConnected(a);
        if (s.isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<String> subs = syncSubreddits(false, online);
                    subredditsForHome = sort(new ArrayList<>(subs));
                    saveSubredditsForHome(subredditsForHome);
                    return null;
                }
            }.execute();

        } else {
            subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                subredditsForHome.add(s2.toLowerCase());
            }
            if (online) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if (Authentication.mod) {
                            doModOf();
                        }
                        loadMultireddits();
                        return null;
                    }
                }.execute();

            }
        }
        saveState(false);
    }


    public static void addSubscription(String name, MainActivity context) {
        subredditsForHome.add(name);
        saveState(false);
        if (context != null) {
            context.usedArray = new ArrayList<>(subredditsForHome);
            context.adapter.notifyDataSetChanged();
            if (context.mTabLayout != null) {
                context.mTabLayout.setupWithViewPager(context.pager);
            }

        } else {
            MainActivity.datasetChanged = true;
        }
    }

    public static void removeSubscription(String name, MainActivity context) {
        if (subredditsForHome.contains(name)) {
            subredditsForHome.remove(name);
            saveState(false);
            if (context != null) {
                context.usedArray = new ArrayList<>(subredditsForHome);
                context.adapter.notifyDataSetChanged();
                if (context.mTabLayout != null) {
                    context.mTabLayout.setupWithViewPager(context.pager);
                }
            } else {
                MainActivity.datasetChanged = true;
            }

        }

    }

    public static void saveSubredditsForHome(ArrayList<String> subs) {
        subredditsForHome = new ArrayList<>(subs);
        StringBuilder b = new StringBuilder();
        for (String s : subs) {
            b.append(s);
            b.append(",");
        }
        String finalS = b.toString();
        finalS = finalS.substring(0, finalS.length() - 1);
        subscriptions.edit().putString(Authentication.name, finalS).apply();
        saveState(false);
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

    public static ArrayList<String> syncSubreddits(boolean save, boolean online) {
        ArrayList<String> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn && online) {
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
            if (save && online) {
                saveSubredditsForHome(toReturn);
            }

            return toReturn;
        } else {
            toReturn.addAll(Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"));
            if (save) {
                saveSubredditsForHome(toReturn);
            }

            return toReturn;
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

    private static void loadMultireddits() {
        if (Authentication.isLoggedIn && Authentication.didOnline) {
            try {
                multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
            } catch (ApiException e) {
                multireddits = null;
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<String> sortNoValue(ArrayList<String> subs) {

        java.util.Collections.sort(subs);
        ArrayList<String> finals = new ArrayList<>();


        finals.addAll(subs);
        return finals;

    }

    public static ArrayList<String> sort(ArrayList<String> copy) {
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
}
