package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;

import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubredditSearchPaginator;
import net.dean.jraw.paginators.SubredditStream;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Fragments.SubredditListView;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * This class is reponsible for loading a list of subreddits from an endpoint
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 * <p/>
 * Created by ccrama on 3/21/2016.
 */
public class SubredditNames {
    public List<Subreddit> posts;
    public String where;
    public boolean nomore = false;
    public boolean stillShow;
    public boolean loading;
    public SubredditListView parent;
    private Paginator<Subreddit> paginator;
    Context c;

    public SubredditNames(String where, Context c, SubredditListView parent) {
        posts = new ArrayList<>();
        this.parent = parent;
        this.where = where;
        this.c = c;
    }

    public void loadMore(Context context, boolean reset) {
        new LoadData(context, reset).execute(where);
    }

    public void loadMore(Context context, boolean reset, String where) {
        this.where = where;
        loadMore(context, reset);
    }


    public List<Subreddit> getPosts() {
        return posts;
    }


    /**
     * Asynchronous task for loading data
     */
    private class LoadData extends AsyncTask<String, Void, List<Subreddit>> {
        final boolean reset;
        Context context;

        public LoadData(Context context, boolean reset) {
            this.context = context;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(List<Subreddit> submissions) {

            loading = false;
            context = null;

            if (submissions != null && !submissions.isEmpty()) {
                ArrayList<Subreddit> toRemove = new ArrayList<>();
                for (Subreddit s : submissions) {
                    if (PostMatch.contains(s.getDisplayName().toLowerCase(Locale.ENGLISH), SettingValues.subredditFilters, true))
                        toRemove.add(s);
                }
                submissions.removeAll(toRemove);
                // new submissions found
                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }

                if (reset || posts == null) {
                    posts = new ArrayList<>(new LinkedHashSet(submissions));
                    start = -1;
                } else {
                    posts.addAll(submissions);
                    posts = new ArrayList<>(new LinkedHashSet(posts));
                }

                final int finalStart = start;

                //update online
                parent.updateSuccess(posts, finalStart);

            } else if (!nomore) {
                parent.updateError();
            }
        }

        @Override
        protected List<Subreddit> doInBackground(String... subredditPaginators) {

            List<Subreddit> things = new ArrayList<>();
            try {
            if (subredditPaginators[0].equalsIgnoreCase("trending")) {
                List<String> trending = Authentication.reddit.getTrendingSubreddits();

                for (String s : trending) {
                    things.add(Authentication.reddit.getSubreddit(s));
                }
                nomore = true;
            } else if (subredditPaginators[0].equalsIgnoreCase("popular")) {
                stillShow = true;
                if (reset || paginator == null) {
                    paginator = new SubredditStream(Authentication.reddit, subredditPaginators[0]);
                    paginator.setSorting(SettingValues.getSubmissionSort(where));
                    paginator.setTimePeriod(SettingValues.getSubmissionTimePeriod(where));
                    paginator.setLimit(Constants.PAGINATOR_POST_LIMIT);

                }


                try {
                    if (paginator != null && paginator.hasNext()) {
                        things.addAll(paginator.next());
                    } else {
                        nomore = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("Forbidden")) {
                        Reddit.authentication.updateToken(context);
                    }

                }
            } else {
                stillShow = true;
                if (reset || paginator == null) {
                    paginator = new SubredditSearchPaginator(Authentication.reddit, subredditPaginators[0]);
                    paginator.setSorting(SettingValues.getSubmissionSort(where));
                    paginator.setTimePeriod(SettingValues.getSubmissionTimePeriod(where));
                    paginator.setLimit(Constants.PAGINATOR_POST_LIMIT);
                }

                try {
                    if (paginator != null && paginator.hasNext()) {
                        things.addAll(paginator.next());

                    } else {
                        nomore = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("Forbidden")) {
                        Reddit.authentication.updateToken(context);
                    }

                }
            }
            } catch (Exception e){
                return null;
            }
            return things;
        }
    }
}
