package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreenSingle extends BaseActivity {
    ViewPager pager;
    OverviewPagerAdapter comments;
    String subreddit;
    public class AsyncGetSubredditName extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s){
            subreddit = s;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
                CommentsScreenSingle.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(subreddit)));

            }
            pager = (ViewPager) findViewById(R.id.contentView);

            context = getIntent().getExtras().getString("context", "");
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        }
        @Override
        protected String doInBackground(String... params) {
            return Authentication.reddit.getSubmission(params[0]).getSubredditName();
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);
        name = getIntent().getExtras().getString("submission", "");

        subreddit= getIntent().getExtras().getString("subreddit", "");

        if(subreddit.equals("NOTHING")){
            new AsyncGetSubredditName().execute(name);
        } else {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
            }
            pager = (ViewPager) findViewById(R.id.contentView);

            context = getIntent().getExtras().getString("context", "");
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
            CommentsScreenSingle.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(subreddit)));

        }

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
