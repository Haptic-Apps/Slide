package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;

import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.Shortcut;
import me.ccrama.redditslide.util.NetworkUtil;

/**
 * Created by carlo_000 on 1/16/2016.
 */
public class SubredditStorage {
    public static SharedPreferences subscriptions;
    public static ArrayList<String> modOf;
    public static ArrayList<MultiReddit> multireddits;
    public static ArrayList<String> subredditsForHome;
    public static ArrayList<String> alphabeticalSubreddits;
    public static Shortcut shortcut;

    public static void saveState(){
        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", Reddit.arrayToString(subredditsForHome));
        editor.putString("subsalph", Reddit.arrayToString(alphabeticalSubreddits));

        editor.putBoolean("loggedin", Authentication.isLoggedIn);

        editor.putString("name", Authentication.name);

        editor.commit();

    }

    public static MultiReddit getMultiredditByDisplayName(String displayName) {
        for (MultiReddit multiReddit : SubredditStorage.multireddits) {
            if (multiReddit.getDisplayName().equals(displayName)) {
                return multiReddit;
            }
        }
        return null;
    }

    public static void getSubredditsForHome(Reddit a) {
        String s = subscriptions.getString(Authentication.name, "");
        final boolean online = NetworkUtil.isConnected(a);
        if (s.isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<String> subs = syncSubreddits(false, online);
                    subredditsForHome =sort(new ArrayList<>(subs));
                    alphabeticalSubreddits = sort(new ArrayList<>(subs));
                    saveSubredditsForHome(subredditsForHome);
                    return null;
                }
            }.execute();

        } else {
            subredditsForHome = new ArrayList<>();
            for (String s2 : s.split(",")) {
                subredditsForHome.add(s2.toLowerCase());
            }
            alphabeticalSubreddits = sort(new ArrayList<>(subredditsForHome));
            if (online) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        if (Authentication.mod) {
                            doModOf();
                        }
                        if (Authentication.isLoggedIn && Authentication.didOnline) {
                            getMultireddits();
                        }
                        return null;
                    }
                }.execute();

            }
        }
        saveState();
    }


    public static void addSubscription(String name){
        subredditsForHome.add(name);
        alphabeticalSubreddits.add(name);
        alphabeticalSubreddits = sort(alphabeticalSubreddits);
        saveSubredditsForHome(subredditsForHome);
    }
    public static void removeSubscription(String name){
        if(subredditsForHome.contains(name)) {
            subredditsForHome.remove(name);
            alphabeticalSubreddits.remove(name);
            alphabeticalSubreddits = sort(alphabeticalSubreddits);
            saveSubredditsForHome(subredditsForHome);
        }
    }
    public static void saveSubredditsForHome(ArrayList<String> subs) {
        StringBuilder b = new StringBuilder();
        for (String s : subs) {
            b.append(s);
            b.append(",");
        }
        String finalS = b.toString();
        finalS = finalS.substring(0, finalS.length() - 1);
        subscriptions.edit().putString(Authentication.name, finalS).commit();
        subredditsForHome = new ArrayList<>(subs);
        alphabeticalSubreddits = sort(new ArrayList<>(subs));
        saveState();
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


    private static void getMultireddits() {

        try {
            multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());

        } catch (ApiException e) {
            multireddits = new ArrayList<>();
            e.printStackTrace();
        }


    }

    private static ArrayList<String> sortNoValue(ArrayList<String> subs) {

        java.util.Collections.sort(subs);
        ArrayList<String> finals = new ArrayList<>();


        finals.addAll(subs);
        return finals;

    }

    public static ArrayList<String> sort(ArrayList<String> subs) {
        if (subs.contains("all")) {
            subs.remove("all");
        }
        if (subs.contains("frontpage")) {
            subs.remove("frontpage");
        }

        java.util.Collections.sort(subs);
        ArrayList<String> finals = new ArrayList<>();

        finals.add("frontpage");
        finals.add("all");
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
