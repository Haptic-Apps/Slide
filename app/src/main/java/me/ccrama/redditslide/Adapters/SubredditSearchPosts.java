package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Paginator;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.SubmissionSearchPaginatorMultireddit;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.MultiredditOverview;
import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.Reddit;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditSearchPosts extends GeneralPosts {
    private String term;
    private String subreddit = "";
    public boolean loading;
    private Paginator<Submission> paginator;
    public SwipeRefreshLayout refreshLayout;
    private ContributionAdapter adapter;

    public Search parent;

    public SubredditSearchPosts(String subreddit, String term, Search parent) {
        if (subreddit != null) {
            this.subreddit = subreddit;
        }
        this.parent = parent;
        this.term = term;
    }

    public void bindAdapter(ContributionAdapter a, SwipeRefreshLayout layout) {
        this.adapter = a;
        this.refreshLayout = layout;
        loadMore(a, subreddit, term, true);
    }

    public void loadMore(ContributionAdapter a, String subreddit, String where, boolean reset) {


        this.adapter = a;
        this.subreddit = subreddit;
        this.term = where;


        new LoadData(reset).execute();


    }

    public void reset() {
        new LoadData(true).execute();
    }

    public class LoadData extends AsyncTask<String, Void, ArrayList<Contribution>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> submissions) {
            loading = false;

            if (submissions != null && !submissions.isEmpty()) {
                // new submissions found

                int start = 0;
                if (posts != null) {
                    start = posts.size() + 1;
                }

                ArrayList<Contribution> filteredSubmissions = new ArrayList<>();
                for (Contribution c : submissions) {
                    if (c instanceof Submission) {
                        if (!PostMatch.doesMatch((Submission) c)) {
                            filteredSubmissions.add(c);
                        }
                    } else {
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
                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                if (finalStart != -1) {
                    adapter.notifyItemRangeInserted(finalStart + 1, posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }

            } else if (!nomore) {
                // error
                adapter.setError(true);
            } else {
                if(isNew) {
                    posts = new ArrayList<>();
                    adapter.notifyDataSetChanged();
                }
            }
            refreshLayout.setRefreshing(false);
        }

        boolean isNew;

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            ArrayList<Contribution> newSubmissions = new ArrayList<>();
            try {
                if (reset || paginator == null) {
                    isNew = true;
                    if (parent.multireddit) {
                        paginator = new SubmissionSearchPaginatorMultireddit(Authentication.reddit, term);
                        ((SubmissionSearchPaginatorMultireddit) paginator).setMultiReddit(MultiredditOverview.searchMulti);
                        ((SubmissionSearchPaginatorMultireddit) paginator).setSearchSorting(SubmissionSearchPaginatorMultireddit.SearchSort.valueOf(Reddit.search.toString()));
                        ((SubmissionSearchPaginatorMultireddit) paginator).setSyntax(SubmissionSearchPaginatorMultireddit.SearchSyntax.LUCENE);

                    } else {
                        paginator = new SubmissionSearchPaginator(Authentication.reddit, term);
                        if (!subreddit.isEmpty())
                            ((SubmissionSearchPaginator) paginator).setSubreddit(subreddit);
                        ((SubmissionSearchPaginator) paginator).setSearchSorting(Reddit.search);
                        ((SubmissionSearchPaginator) paginator).setSyntax(SubmissionSearchPaginator.SearchSyntax.LUCENE);

                    }
                    paginator.setTimePeriod((parent.time));
                }

                if (!paginator.hasNext()) {
                    nomore = true;
                    return new ArrayList<>();
                }
                if (reset) {
                    nomore = false;
                    for (Submission s : paginator.next()) {

                        newSubmissions.add(s);


                    }
                    if (newSubmissions.isEmpty()) {
                        nomore = true;
                    }

                } else if (!nomore) {
                    for (Submission s : paginator.next()) {
                        newSubmissions.add(s);
                    }
                    if (newSubmissions.isEmpty()) {
                        nomore = true;
                    }
                } else {
                    adapter.notifyDataSetChanged();
                }
                return newSubmissions;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

}
