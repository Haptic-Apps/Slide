package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;

/**
 * This class is reponsible for loading multireddit specific submissions
 * {@link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 *
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditPosts implements PostLoader {
    public ArrayList<Submission> posts;
    public boolean loading;
    private MultiRedditPaginator paginator;
    private MultiReddit multiReddit;
    public boolean nomore = false;
    public MultiredditAdapter adapter;

    public boolean skipOne;
    /**
     *
     * @param multiRedditDisplayName the display name of the multireddit
     */
    public MultiredditPosts(String multiRedditDisplayName) {
        posts = new ArrayList<>();
        this.multiReddit = SubredditStorage.getMultiredditByDisplayName(multiRedditDisplayName);
    }

    public MultiReddit getMultiReddit() {
        return multiReddit;
    }

    @Override
    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset) {
        new LoadData(context, displayer, reset).execute(multiReddit);
    }
    public void loadMore(Context context, SubmissionDisplay displayer, boolean reset, MultiredditAdapter adapter) {
        this.adapter = adapter;
        new LoadData(context, displayer, reset).execute(multiReddit);
    }
    @Override
    public List<Submission> getPosts() {
        return posts;
    }

    @Override
    public boolean hasMore() {
        return true; // TODO when is this false
    }

    private class LoadData extends AsyncTask<MultiReddit, Void, ArrayList<Submission>> {
        final boolean reset;
        final Context context;
        final SubmissionDisplay displayer;

        public LoadData(Context context, SubmissionDisplay displayer, boolean reset) {
            this.context = context;
            this.displayer = displayer;
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> submissions) {
            loading = false;

            if (submissions != null && !submissions.isEmpty()) {
                // new submissions found

                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }

                ArrayList<Submission> filteredSubmissions = new ArrayList<>();
                for (Submission c : submissions) {
                        if (!PostMatch.doesMatch(c)) {
                            filteredSubmissions.add(c);
                        }

                }

                if (reset || posts == null) {
                    posts = filteredSubmissions;
                    start = -1;
                } else {
                    posts.addAll(filteredSubmissions);
                }

                final int finalStart = start;
                // update online


                if(adapter != null) {
                    if (finalStart != -1) {
                        adapter.notifyItemRangeInserted(finalStart, posts.size());
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    adapter.refreshLayout.setRefreshing(false);

                }

            } else if (submissions != null) {
                // end of submissions
                nomore = true;
            } else if (!nomore && adapter != null) {
                // error
                adapter.setError(true);
                adapter.refreshLayout.setRefreshing(false);
            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(MultiReddit... subredditPaginators) {
            ArrayList<Submission> newSubmissions = new ArrayList<>();

            try {
                if (reset || paginator == null) {
                    paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);
                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                    if(skipOne)
                        paginator.next();
                }

                if(!paginator.hasNext()){
                    nomore = true;
                    return newSubmissions;
                }

                for (Submission s : paginator.next()) {
                        newSubmissions.add(s);

                }
                new OfflineSubreddit("multi" + subredditPaginators[0].getFullName()).overwriteSubmissions(newSubmissions).writeToMemory();

                return newSubmissions;
            } catch (Exception e) {
                return null;
            }

        }
    }
}
