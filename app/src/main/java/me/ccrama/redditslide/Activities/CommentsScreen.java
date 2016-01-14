package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreen extends BaseActivityAnim {
    final private static String TAG = "CommentsScreen";
    public ArrayList<Submission> posts;
    OverviewPagerAdapter comments;
    int firstPage;
    private String subreddit;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);


        firstPage = getIntent().getExtras().getInt("page", -1);
        if (firstPage == RecyclerView.NO_POSITION) {
            //IS SINGLE POST
            Log.w(TAG, "Is single post?");
        } else {
            posts = DataShare.sharedSubreddit;
        }
        if (posts == null || posts.get(firstPage) == null) {
            finish();
        } else {
            updateSubredditAndSubmission(posts.get(firstPage));

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
                    //todo load more
                    updateSubredditAndSubmission(posts.get(position));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }


    }

    private void updateSubredditAndSubmission(Submission posts) {
        subreddit = posts.getSubredditName();
        themeStatusBar(subreddit);
        setRecentBar(subreddit);
        HasSeen.addSeen(posts.getFullName());
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new CommentPage();
            Bundle args = new Bundle();

            String name = posts.get(i).getFullName();
            args.putString("id", name.substring(3, name.length()));
            Log.v("Slide", name.substring(3, name.length()));
            args.putString("subreddit", posts.get(i).getSubredditName());
            args.putBoolean("archived", posts.get(i).isArchived());
            args.putInt("page", i);
            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {
            int offset = 0;
            if (Reddit.single || Reddit.swipeAnywhere) {
                offset = 1;
            }
            if (posts == null) {
                return 1 + offset;
            } else {
                return posts.size() + offset;
            }
        }


    }

}
