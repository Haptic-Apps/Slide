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
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.Shortcut;


/**
 * Created by ccrama on 5/24/2015.
 */
public final class SubredditStorage extends AsyncTask<Reddit, Void, ArrayList<String>> {

    private Context mContext;
    // Context is only needed to get the startup strings, so it's safe to set it null for all other cases
    public SubredditStorage() {
        mContext = null;
    }
    public SubredditStorage(Context context) {
        mContext = context;
    }


    public static SharedPreferences subscriptions;
    public static Shortcut shortcut;

    public static void setPins(ArrayList<String> pinns) {
        String finals = "";
        for (String s : pinns) {
            finals = finals + s + ",";
        }
        subscriptions.edit().putString("pins" + Authentication.name, finals).apply();
    }
    public static void setSubreddits (ArrayList<String> pinns) {
        String finals = "";
        for (String s : pinns) {
            finals = finals + s + ",";
        }
        subscriptions.edit().putString("subs" + Authentication.name, finals).apply();
    }

    public static ArrayList<String> subredditsForHome;
    public static ArrayList<String> alphabeticalSubscriptions;

    public static ArrayList<MultiReddit> multireddits;

    @Override
    public void onPostExecute(ArrayList<String> s){
        if(Authentication.isLoggedIn){AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //doUpdateSubsSaveBackground();
            }
        });
        }
    }
    @Override
    protected ArrayList<String> doInBackground(final Reddit... params) {


        if(Authentication.mod){
            doModOf();
        }

        realSubs = new ArrayList<>();

        ArrayList<String> value = getPins();

        if (Authentication.isLoggedIn && value != null) {


            ArrayList<String> res = new ArrayList<>(value);

            ArrayList<String> test = new ArrayList<>();
            test.addAll(res);
            if(test.contains("")){
                test.remove("");
            }

            ArrayList<String> newValues = new ArrayList<>();
            if(!test.contains("frontpage"))
                test.add("frontpage");
            if(!test.contains("all"))
                test.add("all");

            if(getSubreddits() != null) {
                for (String s : getSubreddits()) {
                    if (!test.contains(s)) {
                        newValues.add(s);
                    }
                    realSubs.add(s.toLowerCase());
                }
            } else {
                for (String s : doUpdateSubsSave()) {
                    if (!test.contains(s)) {
                        newValues.add(s);
                    }
                    realSubs.add(s.toLowerCase());

                }
            }
            if(params[0].loader != null) {
                params[0].loader.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] strings = StartupStrings.startupStrings(mContext);
                        params[0].loader.loading.setText(strings[new Random().nextInt(strings.length)]);
                    }
                });
            }
            test.addAll(sortNoValue(newValues));
            if(test.contains("")){
                test.remove("");
            }
            if(Authentication.isLoggedIn){
                getMultireddits();
            }
            subredditsForHome = test;
          //  DataShare.notifs = new SubredditPaginator(Authentication.reddit, "slideforredditnotifs" ).next().get(0);
           // if(Reddit.hidden.contains(DataShare.notifs.getFullName())){
           //     DataShare.notifs = null;
          //  }

            alphabeticalSubscriptions = sort(new ArrayList<>(test));
            if(params[0].loader != null) {

                params[0].startMain();
            }
            if(shortcut != null){
                shortcut.doShortcut();
            }


            return test;
        } else {
            ArrayList<String> test = new ArrayList<>();

            ArrayList<String> newValues = new ArrayList<>();
            if(!test.contains("frontpage"))
                test.add("frontpage");
            if(!test.contains("all"))
                test.add("all");
            if(test.contains("")){
                test.remove("");
            }
            if (Authentication.isLoggedIn) {
                if(getSubreddits() != null) {
                    for (String s : getSubreddits()) {
                        if (!test.contains(s)) {
                            newValues.add(s);
                        }
                        realSubs.add(s.toLowerCase());

                    }

                } else {
                    for (String s : doUpdateSubsSave()) {
                        if (!test.contains(s)) {
                            newValues.add(s);
                        }
                        realSubs.add(s.toLowerCase());

                    }
                }
            } else {
                for (String s : Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")){
                    if (!test.contains(s)) {
                        newValues.add(s);
                    }

                }
            }

            Log.v("Slide", "SIZE IS " +  newValues.size());


            test.addAll(sortNoValue(newValues));
            if(test.contains("")){
                test.remove("");
            }
            subredditsForHome = test;
           /* DataShare.notifs = new SubredditPaginator(Authentication.reddit, "slideforredditnotifs" ).next().get(0);
            if(Reddit.hidden.contains(DataShare.notifs.getFullName())){
                DataShare.notifs = null;
            }*/

            alphabeticalSubscriptions = sort(new ArrayList<>(test));
            if(params[0].loader != null) {

                params[0].startMain();
            }
            if(shortcut != null){
                shortcut.doShortcut();
            }



            return test;
        }

    }

    public static ArrayList<String> realSubs;



    public static void addPin(String name) {

        if(!subscriptions.contains("pins" + Authentication.name)) {

            subscriptions.edit().putString("pins" + Authentication.name, name.toLowerCase()).apply();
            Log.v("Slide", "PIN ADDED FOR " + name.toLowerCase());

        } else {
            String pins = subscriptions.getString("pins" + Authentication.name, "");

            ArrayList<String> newstrings = new ArrayList<>();
            for (String s : pins.split(",")) {
                if(!newstrings.contains(s))
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
        Collections.addAll(newstrings, pins.split(","));
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

        if(subscriptions.contains("pins" + Authentication.name) && !subscriptions.getString("pins" + Authentication.name,"").isEmpty()) {

            newstrings = new ArrayList<>();
            String pins = subscriptions.getString("pins" + Authentication.name, "");

            for (String s : pins.split(",")) {
                newstrings.add(s.toLowerCase());

            }
        }
      return newstrings;

    }
    public static ArrayList<String> getSubreddits() {
        ArrayList<String> newstrings = null;

        if(subscriptions.contains("subs" + Authentication.name)) {

            newstrings = new ArrayList<>();
            String pins = subscriptions.getString("subs" + Authentication.name, "");

            for (String s : pins.split(",")) {
                newstrings.add(s.toLowerCase());

            }
        }
        return newstrings;

    }
    public static ArrayList<String> modOf;
    private ArrayList<String> doModOf() {
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
        } catch (Exception e){
            //failed;
            e.printStackTrace();
        }




        return finished;
    }

    private ArrayList<String> doUpdateSubsSaveBackground() {
        ArrayList<String> finished = new ArrayList<>();

        UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
        pag.setLimit(100);


        try {
            while (pag.hasNext()) {
                Log.v("Slide", "ADDING NEW SUBS");
                for (net.dean.jraw.models.Subreddit s : pag.next()) {
                    Log.v("Slide", s.getDisplayName());
                    finished.add(s.getDisplayName().toLowerCase());
                }
            }
            if (finished.size() == 0) {
                for (String s : Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")) {
                    finished.add(s);

                }
            }

            setSubreddits(finished);
        } catch (Exception e){
            //failed;
            e.printStackTrace();
        }




        return finished;
    }

    private ArrayList<String> doUpdateSubsSave() {
        ArrayList<String> finished = new ArrayList<>();

        UserSubredditsPaginator pag = new UserSubredditsPaginator(Authentication.reddit, "subscriber");
        pag.setLimit(100);


        while(pag.hasNext()){
            Log.v("Slide", "ADDING NEW SUBS");
            for (net.dean.jraw.models.Subreddit s : pag.next()) {
                Log.v("Slide", s.getDisplayName());
                finished.add(s.getDisplayName().toLowerCase());
            }
        }
        if(finished.size() == 0){
            for (String s : Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts")){
                    finished.add(s);

            }
        }

        setSubreddits(finished);



        return finished;
    }
    private void getMultireddits() {

        try {
            multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());

        } catch (ApiException e) {
            e.printStackTrace();
        }


    }
    private static ArrayList<String> sortNoValue(ArrayList<String> subs) {

        java.util.Collections.sort(subs);
        ArrayList<String> finals = new ArrayList<>();


        finals.addAll(subs);
        return finals;

    }
    private static ArrayList<String> sort(ArrayList<String> subs) {
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
        public SyncMultireddits(Context c){
            this.c = c;
        }
       @Override
       public void onPostExecute(Boolean b){
           Intent i = new Intent(c, MultiredditOverview.class);
           c.startActivity(i);
           ((Activity)c).finish();
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



