package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;

import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreenPopup extends BaseActivity {
    OverviewPagerAdapter comments;
    private ArrayList<Submission> posts;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_slide_popup);


        int firstPage = getIntent().getExtras().getInt("page", -1);
        if (firstPage == -1) {
            //IS SNIGLE POST
        } else {
            posts = DataShare.sharedSubreddit;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(posts.get(firstPage).getSubredditName()));
        }
        ViewPager pager = (ViewPager) findViewById(R.id.contentView);

        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(firstPage);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //todo load more
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.setStatusBarColor(Pallete.getDarkerColor(posts.get(position).getSubredditName()));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
            args.putInt("page", i);
            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {
            if (posts == null) {
                return 1;
            } else {
                return posts.size();
            }
        }


    }

}
