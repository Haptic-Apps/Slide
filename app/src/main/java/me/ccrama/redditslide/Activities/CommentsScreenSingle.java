package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class CommentsScreenSingle extends BaseActivity {
    ViewPager pager;
    OverviewPagerAdapter comments;
    String subreddit;
    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("ASDF"), true);

        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);
        subreddit= getIntent().getExtras().getString("subreddit", "");





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
        }
        pager = (ViewPager) findViewById(R.id.contentView);

        name = getIntent().getExtras().getString("submission", "");
        context = getIntent().getExtras().getString("context","");
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

    }
    public String name;
    public String context;

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new CommentPage();
            Bundle args = new Bundle();
            if(name.contains("t3"))
            name = name.substring(3, name.length());

            args.putString("id", name);
            args.putString("context", context);
            args.putString("subreddit", subreddit);
            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {

                return 1;

        }



    }

}
