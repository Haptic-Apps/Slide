package me.ccrama.redditslide.Adapters;

import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.ccrama.redditslide.Activities.CommentsScreen;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditPosts {
    public ArrayList<Submission> posts;
    private SubredditPaginator paginator;
    private SwipeRefreshLayout refreshLayout;

    public boolean loading;
    public SubredditPosts(ArrayList<Submission> firstData, SubredditPaginator paginator) {
        posts = firstData;
        this.paginator = paginator;
    }

    public ArrayList<Submission> getPosts(){
        try {
            return new LoadData(true).execute(subreddit).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public String subreddit;

    public SubredditPosts(String subreddit) {
        this.subreddit = subreddit;
    }

    private SubmissionAdapter adapter;

    public void bindAdapter(SubmissionAdapter a, SwipeRefreshLayout layout) throws ExecutionException, InterruptedException {
        this.adapter = a;
        this.refreshLayout=layout;

        loadMore(a, true, subreddit);
    }

    public void loadMore(SubmissionAdapter adapter, boolean reset, String subreddit) {
        new LoadData(reset).execute(subreddit);


    }
    public void loadMore(CommentsScreen adapter, boolean reset) {
        this.pagerad = adapter;
        new LoadData(reset).execute(subreddit);

    }

    CommentsScreen pagerad;
    public class LoadData extends AsyncTask<String, Void, ArrayList<Submission>> {
        final boolean reset;

        public LoadData(boolean reset) {
            this.reset = reset;
        }

        @Override
        public void onPostExecute(ArrayList<Submission> subs) {

            if(subs != null) {

                loading = false;

                if (refreshLayout != null)
                    (adapter.mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);

                                adapter.dataSet = posts;


                            adapter.notifyDataSetChanged();

                        }
                    });
            } else {
                adapter.setError(true);

            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(String... subredditPaginators) {
            if (reset || paginator == null) {
                if (subredditPaginators[0].toLowerCase().equals("frontpage")) {
                    paginator = new SubredditPaginator(Authentication.reddit);
                } else {
                    paginator = new SubredditPaginator(Authentication.reddit, subredditPaginators[0]);

                }
                paginator.setSorting(Reddit.defaultSorting);
                paginator.setTimePeriod(Reddit.timePeriod);
            }
            if (paginator != null && paginator.hasNext()) {
                if (reset) {
                    posts = new ArrayList<>();
                    try {
                        for (Submission c : paginator.next()) {
                            Submission s = c;
                            if (Hidden.isHidden(s)) {
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);
                                }
                            }

                        }
                    } catch(Exception ignored){
                        //gets caught above
                    }
                } else {
                    if(posts == null)
                        posts = new ArrayList<>();

                    try{
                    for (Submission c : paginator.next()) {
                            Submission s =  c;
                            if (Hidden.isHidden(s)) {
                                if (SettingValues.NSFWPosts && s.isNsfw()) {
                                    posts.add(s);
                                } else if (!s.isNsfw()) {
                                    posts.add(s);
                                }
                            }

                    }
                    } catch(Exception ignored){
                        //gets caught above
                    }
                }
            }

            return posts;
        }
    }

    public void addData(List<Submission> data) {
        posts.addAll(data);
    }
}