package me.ccrama.redditslide;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Activities.Shortcut;


/**
 * Created by ccrama on 5/24/2015.
 */
public final class SubredditStorage extends AsyncTask<Reddit, Void, ArrayList<String>> {


    public static SharedPreferences subscriptions;
    public static Shortcut shortcut;


    public static class GetCollections extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            try {

                List<MultiReddit> reddits = new MultiRedditManager(Authentication.reddit).mine();
                ArrayList<String> finals = new ArrayList<>();
                for (MultiReddit m : reddits) {
                    finals.add(m.getDisplayName());

                }
                return finals;
            } catch (ApiException e) {
                return new ArrayList<>();
            }
        }
    }

    public static ArrayList<String> subredditsForHome;
    public static ArrayList<String> alphabeticalSubscriptions;

    public static ArrayList<MultiReddit> multireddits;

    @Override
    protected ArrayList<String> doInBackground(final Reddit... params) {
        ArrayList<String> finished = new ArrayList<>();

        if(Authentication.isLoggedIn){
            getMultireddits();
        }


        ArrayList<String> value = getPins();

        if (Authentication.isLoggedIn && value != null) {


            ArrayList<String> res = new ArrayList<>(value);

            ArrayList<String> test = new ArrayList<>();
            test.addAll(sortNoValue(res));


            ArrayList<String> newValues = new ArrayList<>();
            if(!test.contains("frontpage"))
                test.add("frontpage");
            if(!test.contains("all"))
                test.add("all");

            for (String s : doUpdateSubsSave()) {
                if (!test.contains(s)) {
                    newValues.add(s);
                }
            }
            if(params[0].loader != null) {
                params[0].loader.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        params[0].loader.loading.setText("Finding videos of cats");

                    }
                });
            }
            test.addAll(sortNoValue(newValues));
            if(test.contains("")){
                test.remove("");
            }
            subredditsForHome = test;
            DataShare.notifs = new SubredditPaginator(Authentication.reddit, "slideforredditnotifs" ).next().get(0);
            if(Reddit.hidden.contains(DataShare.notifs.getFullName())){
                DataShare.notifs = null;
            }

            alphabeticalSubscriptions = sort(new ArrayList<>(test));
            if(params[0].loader != null) {

                params[0].startMain();
            }
            if(shortcut != null){
                shortcut.doShortcut();
            }


            return test;
        } else {
            if (Authentication.isLoggedIn) {
                finished.addAll(doUpdateSubsSave());
            } else {
                finished.addAll(Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"));
            }
            if(params[0].loader != null) {

                params[0].loader.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        params[0].loader.loading.setText("Finding videos of cats");

                    }
                });
            }
            subredditsForHome = sort(finished);
            alphabeticalSubscriptions = sort(new ArrayList<>(subredditsForHome));
            if(params[0].loader != null) {

                params[0].startMain();
            }
            if(shortcut != null){
                shortcut.doShortcut();
            }
            return subredditsForHome;
        }

    }



    public static void addPin(String name) {

        if(!subscriptions.contains("pins" + Authentication.name)) {

            String pins = name;
            subscriptions.edit().putString("pins" + Authentication.name, pins.toLowerCase()).apply();
            Log.v("Slide", "PIN ADDED FOR " + pins.toLowerCase());

        } else {
            String pins = subscriptions.getString("pins" + Authentication.name, "");

            ArrayList<String> newstrings = new ArrayList<>();
            for (String s : pins.split(",")) {
                newstrings.add(s);
            }
            newstrings.add(name.toLowerCase());
            String finals = "";
            for (String s : newstrings) {
                finals = finals + s + ",";
            }
            subscriptions.edit().putString("pins" + Authentication.name, finals).apply();
            Log.v("Slide", "PIN ADDED FOR " + name.toLowerCase());
        }
    }

    public static void removePin(String name) {
        String pins = subscriptions.getString("pins" + Authentication.name, "");

        ArrayList<String> newstrings = new ArrayList<>();
        for (String s : pins.split(",")) {
            newstrings.add(s);
        }
        if (newstrings.contains(name)) {
            newstrings.remove(name);

        }
        String finals = "";
        for (String s : newstrings) {
            finals = finals + s + ",";
        }
        subscriptions.edit().putString("pins" + Authentication.name, finals).apply();


    }
    public static ArrayList<String> getPins() {
        ArrayList<String> newstrings = null;

        if(subscriptions.contains("pins" + Authentication.name)) {

            newstrings = new ArrayList<>();
            String pins = subscriptions.getString("pins" + Authentication.name, "");

            for (String s : pins.split(",")) {
                newstrings.add(s.toLowerCase());

            }
        }
      return newstrings;

    }
    public ArrayList<String> doUpdateSubsSave() {
        ArrayList<String> finished = new ArrayList<>();

        UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
        pag.setLimit(50);


        while(pag.hasNext()){
            for (net.dean.jraw.models.Subreddit s : pag.next()) {
                finished.add(s.getDisplayName().toLowerCase());
            }
        }



        return finished;
    }
    public void getMultireddits() {

        try {
            multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());
        } catch (ApiException e) {
            e.printStackTrace();
        }


    }
    public static ArrayList<String> sortNoValue(ArrayList<String> subs) {

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
}



