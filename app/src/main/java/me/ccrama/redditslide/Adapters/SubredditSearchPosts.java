package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubmissionSearchPaginator;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearchPosts extends GeneralPosts {
    private  String term;
    private  String subreddit = "";
    public boolean loading;
    private SubmissionSearchPaginator paginator;
    private SwipeRefreshLayout refreshLayout;
    private ContributionAdapter adapter;

    public SubredditSearchPosts(String subreddit, String term) {
        if(subreddit != null) {
            this.subreddit = subreddit;
        }
        this.term = term;
    }

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, term);
    }

    public void loadMore(ContributionAdapter a, String subreddit, String where) {


        this.adapter = a;
        this.subreddit = subreddit;
        this.term = where;


            new LoadData(true).execute();


    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> subs) {
            if (subs != null) {

                loading = false;
                if (refreshLayout != null)
                    ( adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                            adapter.dataSet = posts;

                            adapter.notifyDataSetChanged();

                        }
                    });
            } else {
                adapter.setError(true);
                refreshLayout.setRefreshing(false);

            }
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            Log.v("Slide", "DOING SEARCH OF " + term + " in " + subreddit);
            try {
                if (reset || paginator == null) {
                    paginator = new SubmissionSearchPaginator(Authentication.reddit, term);
                    if(!subreddit.isEmpty())
                    paginator.setSubreddit(subreddit);

                    paginator.setSearchSorting(SubmissionSearchPaginator.SearchSort.RELEVANCE);
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
                if(posts == null){
                    posts = new ArrayList<>();
                }
                Log.v("Slide", "RETURNED " + paginator.hasNext() );
                if (reset) {
                    posts = new ArrayList<>();
                    for (Submission s : paginator.next()) {
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);

                            }
                        }

                } else {
                    for (Submission s : paginator.next()) {
                        if (SettingValues.NSFWPosts && s.isNsfw()) {
                            posts.add(s);
                        } else if (!s.isNsfw()) {
                            posts.add(s);

                        }
                    }
                }
                return posts;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
