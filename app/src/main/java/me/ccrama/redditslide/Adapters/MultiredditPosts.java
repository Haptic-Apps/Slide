package me.ccrama.redditslide.Adapters;

import android.content.Context;
import android.os.AsyncTask;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.MultiRedditPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;

/**
 * This class is reponsible for loading multireddit specific submissions
 * {@Link loadMore(Context, SubmissionDisplay, boolean, String)} is implemented
 * asynchronously.
 *
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditPosts implements PostLoader {
    public ArrayList<Submission> posts;
    public boolean loading;
    private MultiRedditPaginator paginator;
    private MultiReddit multiReddit;

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
            if (submissions != null) {
                loading = false;
                displayer.updateSuccess(submissions, -1);
            } else {
                displayer.updateError();
            }
        }

        @Override
        protected ArrayList<Submission> doInBackground(MultiReddit... subredditPaginators) {
            try {
                if (reset || paginator == null) {
                    paginator = new MultiRedditPaginator(Authentication.reddit, subredditPaginators[0]);
                    paginator.setSorting(Reddit.defaultSorting);
                    paginator.setTimePeriod(Reddit.timePeriod);
                }

                if (reset) {
                    posts = new ArrayList<>();
                }

                for (Submission s : paginator.next()) {
                    if (SettingValues.NSFWPosts || !s.isNsfw()) {
                        posts.add(s);
                    }
                }

                DataShare.sharedSubreddit = posts; // set this since it gets out of sync at CommentPage

                return posts;
            } catch (Exception e) {
                return null;
            }

        }
    }
}
