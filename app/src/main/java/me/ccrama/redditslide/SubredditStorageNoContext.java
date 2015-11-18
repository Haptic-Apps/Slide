package me.ccrama.redditslide;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import me.ccrama.redditslide.Activities.SubredditOverview;


/**
 * Created by ccrama on 5/24/2015.
 */
public final class SubredditStorageNoContext extends AsyncTask<Activity, Void, ArrayList<String>> {


    private static ArrayList<String> getPins() {
        ArrayList<String> newstrings = null;

        if (SubredditStorage.subscriptions.contains("pins" + Authentication.name)) {

            newstrings = new ArrayList<>();
            String pins = SubredditStorage.subscriptions.getString("pins" + Authentication.name, "");

            for (String s : pins.split(",")) {
                newstrings.add(s.toLowerCase());
                Log.v("Slide", "PIN FOUND " + s.toLowerCase());

            }
        }
        return newstrings;

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

    @Override
    protected ArrayList<String> doInBackground(Activity... params) {
        ArrayList<String> finished = new ArrayList<>();


        if (!Authentication.isLoggedIn && getPins() != null) {
            SubredditStorage.subscriptions.edit().remove("pins").apply();
        }

        ArrayList<String> value = getPins();

        if (value != null && !value.isEmpty()) {


            ArrayList<String> res = new ArrayList<>(value);

            ArrayList<String> test = new ArrayList<>();
            test.addAll(res);

            ArrayList<String> newValues = new ArrayList<>();
            if (!test.contains("frontpage"))
                test.add("frontpage");
            if (!test.contains("all"))
                test.add("all");

            for (String s : SubredditStorage.alphabeticalSubscriptions) {
                if (!test.contains(s)) {
                    newValues.add(s);
                }
            }
            test.addAll(sortNoValue(newValues));
            if (test.contains("")) {
                test.remove("");
            }
            SubredditStorage.subredditsForHome = test;

            if (params[0] instanceof SubredditOverview)
                ((SubredditOverview) params[0]).resetAdapter();
            SubredditStorage.saveState();

            return test;
        } else {
            if (Authentication.isLoggedIn) {
                finished.addAll(SubredditStorage.alphabeticalSubscriptions);
            } else {
                finished.addAll(Arrays.asList("announcements", "Art", "AskReddit", "askscience", "aww", "blog", "books", "creepy", "dataisbeautiful", "DIY", "Documentaries", "EarthPorn", "explainlikeimfive", "Fitness", "food", "funny", "Futurology", "gadgets", "gaming", "GetMotivated", "gifs", "history", "IAmA", "InternetIsBeautiful", "Jokes", "LifeProTips", "listentothis", "mildlyinteresting", "movies", "Music", "news", "nosleep", "nottheonion", "OldSchoolCool", "personalfinance", "philosophy", "photoshopbattles", "pics", "science", "Showerthoughts", "space", "sports", "television", "tifu", "todayilearned", "TwoXChromosomes", "UpliftingNews", "videos", "worldnews", "WritingPrompts"));
            }
            SubredditStorage.subredditsForHome = sort(finished);


            if (params[0] instanceof SubredditOverview)
                ((SubredditOverview) params[0]).resetAdapter();
            SubredditStorage.saveState();

            return SubredditStorage.subredditsForHome;
        }

    }
}



