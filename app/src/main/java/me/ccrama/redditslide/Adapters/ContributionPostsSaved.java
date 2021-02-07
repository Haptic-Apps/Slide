package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.UserSavedPaginator;

import java.util.ArrayList;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.SettingValues;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContributionPostsSaved extends ContributionPosts {
    private final String category;

    public ContributionPostsSaved(String subreddit, String where, String category) {
        super(subreddit, where);
        this.category = category;
    }

    UserSavedPaginator paginator;

    @Override
    public void loadMore(ContributionAdapter adapter, String subreddit, boolean reset) {
        new LoadData(reset).execute(subreddit);
    }

    public class LoadData extends ContributionPosts.LoadData {

        public LoadData(boolean reset) {
            super(reset);
        }

        @Override
        public void onPostExecute(ArrayList<Contribution> submissions) {
            super.onPostExecute(submissions);
        }

        @Override
        protected ArrayList<Contribution> doInBackground(String... subredditPaginators) {
            ArrayList<Contribution> newData = new ArrayList<>();
            try {
                if (reset || paginator == null) {
                    paginator = new UserSavedPaginator(Authentication.reddit, where, subreddit);
                    paginator.setSorting(SettingValues.getSubmissionSort(subreddit));
                    paginator.setTimePeriod(SettingValues.getSubmissionTimePeriod(subreddit));
                    if(category != null)
                        paginator.setCategory(category);
                }

                if (!paginator.hasNext()) {
                    nomore = true;
                    return new ArrayList<>();
                }
                for (Contribution c : paginator.next()) {
                    if (c instanceof Submission) {
                        Submission s = (Submission) c;
                        if (!PostMatch.doesMatch(s)) {
                            newData.add(s);
                        }
                    } else {
                        newData.add(c);
                    }
                }

                HasSeen.setHasSeenContrib(newData);

                return newData;
            } catch (Exception e) {
                return null;
            }
        }

    }

}
