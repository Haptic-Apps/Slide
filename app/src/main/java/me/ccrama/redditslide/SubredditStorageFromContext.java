package me.ccrama.redditslide;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import me.ccrama.redditslide.Activities.SubredditOverview;
import me.ccrama.redditslide.Activities.SubredditOverviewSingle;


/**
 * Created by ccrama on 5/24/2015.
 */
public final class SubredditStorageFromContext extends AsyncTask<Reddit, Void, ArrayList<String>> {

    private Context mContext;
    public SubredditStorageFromContext(Context context, Dialog dialog) {
        mContext = context;
        this.d = dialog;
    }

    private Dialog d;

    @Override
    public void onPostExecute(ArrayList<String> s){
        if(Authentication.isLoggedIn){AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //doUpdateSubsSaveBackground();
                d.dismiss();
                if(mContext instanceof SubredditOverview)
                    ((SubredditOverview)mContext).restartTheme();
                if(mContext instanceof SubredditOverviewSingle)
                    ((SubredditOverviewSingle)mContext).restartTheme();

            }
        });
        }
    }
    @Override
    protected ArrayList<String> doInBackground(final Reddit... params) {

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

        return null;
    }

    public static ArrayList<String> realSubs;



    public static void addPin(String name) {

        if(!SubredditStorage.subscriptions.contains("pins" + Authentication.name)) {

            SubredditStorage.subscriptions.edit().putString("pins" + Authentication.name, name.toLowerCase()).apply();
            Log.v("Slide", "PIN ADDED FOR " + name.toLowerCase());

        } else {
            String pins = SubredditStorage.subscriptions.getString("pins" + Authentication.name, "");

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
            SubredditStorage.subscriptions.edit().putString("pins" + Authentication.name, finals).apply();
            Log.v("Slide", "PIN ADDED FOR " + name.toLowerCase());
        }
    }

    public static void removePin(String name) {
        String pins = SubredditStorage.subscriptions.getString("pins" + Authentication.name, "");

        ArrayList<String> newstrings = new ArrayList<>();
        Collections.addAll(newstrings, pins.split(","));
        if (newstrings.contains(name)) {
            newstrings.remove(name);

        }
        String finals = "";
        for (String s : newstrings) {
            finals = finals + s + ",";
        }
        SubredditStorage.subscriptions.edit().putString("pins" + Authentication.name, finals).apply();


    }
    public static ArrayList<String> getPins() {
        ArrayList<String> newstrings = null;

        if(SubredditStorage.subscriptions.contains("pins" + Authentication.name) && !SubredditStorage.subscriptions.getString("pins" + Authentication.name,"").isEmpty()) {

            newstrings = new ArrayList<>();
            String pins = SubredditStorage.subscriptions.getString("pins" + Authentication.name, "");

            for (String s : pins.split(",")) {
                newstrings.add(s.toLowerCase());

            }
        }
      return newstrings;

    }
    public static ArrayList<String> getSubreddits() {
        ArrayList<String> newstrings = null;

        if(SubredditStorage.subscriptions.contains("subs" + Authentication.name)) {

            newstrings = new ArrayList<>();
            String pins = SubredditStorage.subscriptions.getString("subs" + Authentication.name, "");

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


    public static void setSubreddits (ArrayList<String> pinns) {
        String finals = "";
        for (String s : pinns) {
            finals = finals + s + ",";
        }
        SubredditStorage.subscriptions.edit().putString("subs" + Authentication.name, finals).apply();
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
            SubredditStorage.multireddits = new ArrayList<>(new MultiRedditManager(Authentication.reddit).mine());

        } catch (ApiException e) {
            e.printStackTrace();
        }


    }
    private static ArrayList<String> sortNoValue(ArrayList<String> subs) {

        Collections.sort(subs);
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

        Collections.sort(subs);
        ArrayList<String> finals = new ArrayList<>();

        finals.add("frontpage");
        finals.add("all");
        finals.addAll(subs);
        return finals;

    }


}



