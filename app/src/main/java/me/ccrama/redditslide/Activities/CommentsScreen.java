package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.StyleView;
import me.ccrama.redditslide.util.LogUtil;

/**
 * This activity is responsible for the view when clicking on a post, showing
 * the post and its comments underneath with the slide left/right for the next
 * post.
 *
 * When the end of the currently loaded posts is being reached, more posts are
 * loaded asynchronously in {@link OverviewPagerAdapter}.
 *
 * Comments are displayed in the {@link CommentPage} fragment.
 *
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreen extends BaseActivityAnim implements SubmissionDisplay {
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_MULTIREDDIT = "multireddit";
    private PostLoader subredditPosts;
    int firstPage;

    OverviewPagerAdapter comments;
    private String subreddit;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);

        firstPage = getIntent().getExtras().getInt(EXTRA_PAGE, -1);
        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);
        String multireddit = getIntent().getExtras().getString(EXTRA_MULTIREDDIT);
        if (multireddit != null) {
            subredditPosts = new MultiredditPosts(multireddit);
            ((MultiredditPosts)subredditPosts).skipOne = true;

        } else {
            subredditPosts = new SubredditPosts(subreddit);
            ((SubredditPosts)subredditPosts).skipOne = true;
        }
        if (firstPage == RecyclerView.NO_POSITION) {
            //IS SINGLE POST
            Log.w(LogUtil.getTag(), "Is single post?");
        } else {
            subredditPosts.getPosts().addAll(new OfflineSubreddit(subreddit).submissions);
           // subredditPosts.loadMore(this.getApplicationContext(), this, true);
        }
        if (subredditPosts.getPosts().isEmpty() || subredditPosts.getPosts().get(firstPage) == null) {
            finish();
        } else {
            updateSubredditAndSubmission(subredditPosts.getPosts().get(firstPage));

            ViewPager pager = (ViewPager) findViewById(R.id.content_view);

            comments = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(comments);
            pager.setCurrentItem(firstPage);

            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    updateSubredditAndSubmission(subredditPosts.getPosts().get(position));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    private void updateSubredditAndSubmission(Submission post) {
        subreddit = post.getSubredditName();
        themeSystemBars(subreddit);
        setRecentBar(subreddit);
        HasSeen.addSeen(post.getFullName());
    }


    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (startIndex != -1) {
                    // TODO determine correct behaviour
                    //comments.notifyItemRangeInserted(startIndex, posts.posts.size());
                    comments.notifyDataSetChanged();
                } else {
                    comments.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                comments.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateOfflineError() {}

    @Override
    public void updateError() {}

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = new CommentPage();
            Bundle args = new Bundle();

            // TODO is there a better point load more posts (instead of the second to last element)?
            if (subredditPosts.getPosts().size() - 2 <= i && subredditPosts.hasMore()) {
                subredditPosts.loadMore(CommentsScreen.this.getApplicationContext(), CommentsScreen.this, false);
            }
            String name = subredditPosts.getPosts().get(i).getFullName();
            args.putString("id", name.substring(3, name.length()));
            Log.v(LogUtil.getTag(), name.substring(3, name.length()));
            args.putString("subreddit", subredditPosts.getPosts().get(i).getSubredditName());
            args.putBoolean("archived", subredditPosts.getPosts().get(i).isArchived());
            args.putInt("page", i);
            args.putString("subreddit", subreddit);
            f.setArguments(args);

            return f;
        }

        @Override
        public int getCount() {
            int offset = 0;
            if (SettingValues.single || SettingValues.swipeAnywhere) {
                offset = 1;
            }
            return subredditPosts.getPosts().size() + offset;
        }

    }

}
