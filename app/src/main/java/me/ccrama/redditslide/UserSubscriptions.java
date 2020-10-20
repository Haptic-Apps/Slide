package me.ccrama.redditslide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
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
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.Activities.Login;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.NewsActivity;
import me.ccrama.redditslide.DragSort.ReorderSubreddits;
import me.ccrama.redditslide.Toolbox.Toolbox;
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
            Arrays.asList("frontpage", "all", "random", "randnsfw", "myrandom", "friends", "mod",
                    "popular");
    public static SharedPreferences subscriptions;
    public static SharedPreferences multiNameToSubs;
    public static SharedPreferences newsNameToSubs;
    public static SharedPreferences news;
    public static SharedPreferences pinned;

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
            multiNameToSubsMap.put(entries.getKey().toLowerCase(Locale.ENGLISH), entries.getValue());
        }

        return multiNameToSubsMap;
    }

    public static Map<String, String> getNewsNameToSubs(boolean all) {
        Map<String, String> multiNameToSubsMapBase = new HashMap<>();

        Map<String, ?> multiNameToSubsObject = newsNameToSubs.getAll();

        for (Map.Entry<String, ?> entry : multiNameToSubsObject.entrySet()) {
            multiNameToSubsMapBase.put(entry.getKey(), entry.getValue().toString());
        }
        if (all) multiNameToSubsMapBase.putAll(getSubsNameToMulti());

        Map<String, String> multiNameToSubsMap = new HashMap<>();

        for (Map.Entry<String, String> entries : multiNameToSubsMapBase.entrySet()) {
            multiNameToSubsMap.put(entries.getKey().toLowerCase(Locale.ENGLISH), entries.getValue());
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
                CaseInsensitiveArrayList subredditsForHome = new CaseInsensitiveArrayList();
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase(Locale.ENGLISH));
                }
                c.updateSubs(subredditsForHome);
            }
            c.updateMultiNameToSubs(getMultiNameToSubs(false));

        } else {
            String s = subscriptions.getString(Authentication.name, "");
            List<String> subredditsForHome = new CaseInsensitiveArrayList();
            if (!s.isEmpty()) {
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase(Locale.ENGLISH));
                }
            }
            CaseInsensitiveArrayList finals = new CaseInsensitiveArrayList();
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

    public static void doNewsSubs(NewsActivity c) {
        if (NetworkUtil.isConnected(c)) {
            String s = news.getString("subs", "news,android");
            if (s.isEmpty()) {
                //get online subs
                c.updateSubs(syncSubscriptionsOverwrite(c));
            } else {
                CaseInsensitiveArrayList subredditsForHome = new CaseInsensitiveArrayList();
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase(Locale.ENGLISH));
                }
                c.updateSubs(subredditsForHome);
            }
            c.updateMultiNameToSubs(getNewsNameToSubs(false));

        } else {
            String s = news.getString("subs", "news,android");
            List<String> subredditsForHome = new CaseInsensitiveArrayList();
            if (!s.isEmpty()) {
                for (String s2 : s.split(",")) {
                    subredditsForHome.add(s2.toLowerCase(Locale.ENGLISH));
                }
            }
            CaseInsensitiveArrayList finals = new CaseInsensitiveArrayList();
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
        if (modOf == null || modOf.isEmpty()) {
            String s = subscriptions.getString(Authentication.name + "mod", "");
            if (!s.isEmpty()) {
                modOf = new CaseInsensitiveArrayList();
                for (String s2 : s.split(",")) {
                    modOf.add(s2.toLowerCase(Locale.ENGLISH));
                }
            }
        }
    }

    public static void cacheModOf() {
        subscriptions.edit()
                .putString(Authentication.name + "mod", Reddit.arrayToString(modOf))
                .apply();
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

    public static CaseInsensitiveArrayList getSubscriptions(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return syncSubscriptionsOverwrite(c);
        } else {
            CaseInsensitiveArrayList subredditsForHome = new CaseInsensitiveArrayList();
            for (String s2 : s.split(",")) {
                if (!subredditsForHome.contains(s2)) subredditsForHome.add(s2);
            }
            return subredditsForHome;
        }
    }

    public static CaseInsensitiveArrayList pins;

    public static CaseInsensitiveArrayList getPinned() {
        String s = pinned.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return new CaseInsensitiveArrayList();
        } else if (pins == null) {
            pins = new CaseInsensitiveArrayList();
            for (String s2 : s.split(",")) {
                if (!pins.contains(s2)) pins.add(s2);
            }
            return pins;
        } else {
            return pins;
        }
    }

    public static CaseInsensitiveArrayList getSubscriptionsForShortcut(Context c) {
        String s = subscriptions.getString(Authentication.name, "");
        if (s.isEmpty()) {
            //get online subs
            return syncSubscriptionsOverwrite(c);
        } else {
            CaseInsensitiveArrayList subredditsForHome = new CaseInsensitiveArrayList();
            for (String s2 : s.split(",")) {
                if (!s2.contains("/m/")) subredditsForHome.add(s2.toLowerCase(Locale.ENGLISH));
            }
            return subredditsForHome;
        }
    }

    public static boolean hasSubs() {
        String s = subscriptions.getString(Authentication.name, "");
        return s.isEmpty();
    }

    public static CaseInsensitiveArrayList modOf;
    public static ArrayList<MultiReddit>   multireddits;
    public static HashMap<String, List<MultiReddit>> public_multireddits =
            new HashMap<String, List<MultiReddit>>();

    public static void doOnlineSyncing() {
        if (Authentication.mod) {
            doModOf();
            if (modOf != null) {
                for (String sub : modOf) {
                    Toolbox.ensureConfigCachedLoaded(sub);
                    Toolbox.ensureUsernotesCachedLoaded(sub);
                }
            }
        }
        doFriendsOf();
        loadMultireddits();
    }

    public static CaseInsensitiveArrayList toreturn;
    public static CaseInsensitiveArrayList friends = new CaseInsensitiveArrayList();

    public static CaseInsensitiveArrayList syncSubscriptionsOverwrite(final Context c) {
        toreturn = new CaseInsensitiveArrayList();
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

    public static CaseInsensitiveArrayList syncSubreddits(Context c) {
        CaseInsensitiveArrayList toReturn = new CaseInsensitiveArrayList();
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(c)) {
            UserSubredditsPaginator pag =
                    new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);
            try {
                while (pag.hasNext()) {
                    for (net.dean.jraw.models.Subreddit s : pag.next()) {
                        toReturn.add(s.getDisplayName().toLowerCase(Locale.ENGLISH));
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
        } catch (ApiException | NetworkException e) {
            e.printStackTrace();
        }
    }

    public static void setSubscriptions(CaseInsensitiveArrayList subs) {
        subscriptions.edit().putString(Authentication.name, Reddit.arrayToString(subs)).apply();
    }

    public static void setPinned(CaseInsensitiveArrayList subs) {
        pinned.edit().putString(Authentication.name, Reddit.arrayToString(subs)).apply();
        pins = null;
    }

    public static void switchAccounts() {
        SharedPreferences.Editor editor = Reddit.appRestart.edit();
        editor.putBoolean("back", true);
        editor.putString("subs", "");
        Authentication.authentication.edit().remove("backedCreds").remove("expires").commit();
        editor.putBoolean("loggedin", Authentication.isLoggedIn);
        editor.putString("name", Authentication.name);
        editor.commit();
    }

    /**
     * @return list of multireddits if they are available, null if could not fetch multireddits
     */
    public static void getMultireddits(final MultiCallback callback) {
        new AsyncTask<Void, Void, List<MultiReddit>>() {

            @Override
            protected List<MultiReddit> doInBackground(Void... params) {
                loadMultireddits();
                return multireddits;
            }

            @Override
            protected void onPostExecute(List<MultiReddit> multiReddits) {
                callback.onComplete(multiReddits);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface MultiCallback {
        void onComplete(List<MultiReddit> multis);
    }

    public static void loadMultireddits() {
        if (Authentication.isLoggedIn && Authentication.didOnline && (multireddits == null
                || multireddits.isEmpty())) {
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
    public static void getPublicMultireddits(MultiCallback callback, final String profile) {
        if (profile.isEmpty()) {
            getMultireddits(callback);
        }

        if (public_multireddits.get(profile) == null) {
            // It appears your own multis are pre-loaded at some point
            // but some other user's multis obviously can't be so
            // don't return until we've loaded them.
            loadPublicMultireddits(callback, profile);
        } else {
            callback.onComplete(public_multireddits.get(profile));
        }
    }

    private static void loadPublicMultireddits(final MultiCallback callback, final String profile) {
        new AsyncTask<Void, Void, List<MultiReddit>>() {

            @Override
            protected List<MultiReddit> doInBackground(Void... params) {
                try {
                    public_multireddits.put(profile, new ArrayList(
                            new MultiRedditManager(Authentication.reddit).getPublicMultis(profile)));
                } catch (Exception e) {
                    public_multireddits.put(profile, null);
                    e.printStackTrace();
                }
                return public_multireddits.get(profile);
            }

            @Override
            protected void onPostExecute(List<MultiReddit> multiReddits) {
                callback.onComplete(multiReddits);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static CaseInsensitiveArrayList doModOf() {
        CaseInsensitiveArrayList finished = new CaseInsensitiveArrayList();

        UserSubredditsPaginator pag =
                new UserSubredditsPaginator(Authentication.reddit, "moderator");
        pag.setLimit(100);
        try {
            while (pag.hasNext()) {
                for (net.dean.jraw.models.Subreddit s : pag.next()) {
                    finished.add(s.getDisplayName().toLowerCase(Locale.ENGLISH));
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
            friends = new CaseInsensitiveArrayList();
            CaseInsensitiveArrayList finished = new CaseInsensitiveArrayList();

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
    public static CaseInsensitiveArrayList getAllSubreddits(Context c) {
        CaseInsensitiveArrayList finalReturn = new CaseInsensitiveArrayList();
        CaseInsensitiveArrayList history = getHistory();
        CaseInsensitiveArrayList defaults = getDefaults(c);
        finalReturn.addAll(getSubscriptions(c));
        for (String s : finalReturn) {
            history.remove(s);
            defaults.remove(s);
        }
        for (String s : history) {
            defaults.remove(s);
        }
        for (String s : history) {
            if (!finalReturn.contains(s)) {
                finalReturn.add(s);
            }
        }
        for (String s : defaults) {
            if (!finalReturn.contains(s)) {
                finalReturn.add(s);
            }
        }
        return finalReturn;
    }

    //Gets user subscriptions + top 500 subs + subs in history
    public static CaseInsensitiveArrayList getAllUserSubreddits(Context c) {
        CaseInsensitiveArrayList finalReturn = new CaseInsensitiveArrayList();
        finalReturn.addAll(getSubscriptions(c));
        finalReturn.removeAll(getHistory());
        finalReturn.addAll(getHistory());
        return finalReturn;
    }

    public static CaseInsensitiveArrayList getHistory() {
        String[] hist = subscriptions.getString("subhistory", "").toLowerCase(Locale.ENGLISH).split(",");
        CaseInsensitiveArrayList history = new CaseInsensitiveArrayList();
        Collections.addAll(history, hist);
        return history;
    }

    public static CaseInsensitiveArrayList getDefaults(Context c) {
        CaseInsensitiveArrayList history = new CaseInsensitiveArrayList();
        Collections.addAll(history, c.getString(R.string.top_500_csv).split(","));
        return history;
    }

    public static void addSubreddit(String s, Context c) {
        CaseInsensitiveArrayList subs = getSubscriptions(c);
        subs.add(s);
        if (SettingValues.alphabetizeOnSubscribe) {
            setSubscriptions(sortNoExtras(subs));
        } else {
            setSubscriptions(subs);
        }
    }

    public static void removeSubreddit(String s, Context c) {
        CaseInsensitiveArrayList subs = getSubscriptions(c);
        subs.remove(s);
        setSubscriptions(subs);
    }

    public static void addPinned(String s, Context c) {
        CaseInsensitiveArrayList subs = getPinned();
        subs.add(s);
        setPinned(subs);
    }

    public static void removePinned(String s, Context c) {
        CaseInsensitiveArrayList subs = getPinned();
        subs.remove(s);
        setPinned(subs);
    }

    //Sets sub as "searched for", will apply to all accounts
    public static void addSubToHistory(String s) {
        String history = subscriptions.getString("subhistory", "");
        if (!history.contains(s.toLowerCase(Locale.ENGLISH))) {
            history += "," + s.toLowerCase(Locale.ENGLISH);
            subscriptions.edit().putString("subhistory", history).apply();
        }
    }

    //Sets a list of subreddits as "searched for", will apply to all accounts
    public static void addSubsToHistory(ArrayList<Subreddit> s2) {
        StringBuilder history = new StringBuilder(subscriptions.getString("subhistory", "").toLowerCase(Locale.ENGLISH));
        for (Subreddit s : s2) {
            if (!history.toString().contains(s.getDisplayName().toLowerCase(Locale.ENGLISH))) {
                history.append(",").append(s.getDisplayName().toLowerCase(Locale.ENGLISH));
            }
        }
        subscriptions.edit().putString("subhistory", history.toString()).apply();
    }

    public static void addSubsToHistory(CaseInsensitiveArrayList s2, boolean b) {
        StringBuilder history = new StringBuilder(subscriptions.getString("subhistory", "").toLowerCase(Locale.ENGLISH));
        for (String s : s2) {
            if (!history.toString().contains(s.toLowerCase(Locale.ENGLISH))) {
                history.append(",").append(s.toLowerCase(Locale.ENGLISH));
            }
        }
        subscriptions.edit().putString("subhistory", history.toString()).apply();
    }

    public static ArrayList<Subreddit> syncSubredditsGetObject() {
        ArrayList<Subreddit> toReturn = new ArrayList<>();
        if (Authentication.isLoggedIn) {
            UserSubredditsPaginator pag =
                    new UserSubredditsPaginator(Authentication.reddit, "subscriber");
            pag.setLimit(100);


            try {
                while (pag.hasNext()) {
                    toReturn.addAll(pag.next());
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
                            toReturn.addAll(pag.next());
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
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Sorts the subreddit ArrayList, keeping special subreddits at the top of the list (e.g.
     * frontpage, all, the random subreddits). Always adds frontpage and all
     *
     * @param unsorted the ArrayList to sort
     * @return the sorted ArrayList
     * @see #sortNoExtras(CaseInsensitiveArrayList)
     */
    public static CaseInsensitiveArrayList sort(CaseInsensitiveArrayList unsorted) {
        CaseInsensitiveArrayList subs = new CaseInsensitiveArrayList(unsorted);

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
     * @see #sort(CaseInsensitiveArrayList)
     */
    public static CaseInsensitiveArrayList sortNoExtras(CaseInsensitiveArrayList unsorted) {
        List<String> subs = new CaseInsensitiveArrayList(unsorted);
        CaseInsensitiveArrayList finals = new CaseInsensitiveArrayList();

        for (String subreddit : getPinned()) {
            if (subs.contains(subreddit)) {
                subs.remove(subreddit);
                finals.add(subreddit);
            }
        }

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
        return getSubscriptions(c).contains(s.toLowerCase(Locale.ENGLISH));
    }

    public static class SubscribeTask extends AsyncTask<String, Void, Void> {
        Context context;
        public SubscribeTask(Context context){
            this.context = context;
        }

        @Override
        protected Void doInBackground(String... subreddits) {
            final AccountManager m = new AccountManager(Authentication.reddit);
            for (String subreddit : subreddits) {
                try {
                    m.subscribe(Authentication.reddit.getSubreddit(subreddit));
                } catch(Exception e){
                    Toast.makeText(context, "Couldn't subscribe, subreddit is private, quarantined, or invite only", Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        }
    }

    public static class UnsubscribeTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddits) {
            final AccountManager m = new AccountManager(Authentication.reddit);
            try {
                for (String subreddit : subreddits) {
                    m.unsubscribe(Authentication.reddit.getSubreddit(subreddit));
                }
            } catch(Exception e){

            }
            return null;
        }
    }
}
