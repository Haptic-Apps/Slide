package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreenSingle extends BaseActivity {
    OverviewPagerAdapter comments;
    boolean np;
    private ViewPager pager;
    private String subreddit;
    private String name;
    private String context;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);
        name = getIntent().getExtras().getString("submission", "");

        subreddit = getIntent().getExtras().getString("subreddit", "");
        np = getIntent().getExtras().getBoolean("np", false);

        if (subreddit.equals("NOTHING")) {
            new AsyncGetSubredditName().execute(name);
        } else {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
                window.setNavigationBarColor(Pallete.getDarkerColor(subreddit));
                CommentsScreenSingle.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(subreddit)));

            }
            pager = (ViewPager) findViewById(R.id.contentView);

            context = getIntent().getExtras().getString("context", "");
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        }

    }

    private class AsyncGetSubredditName extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            subreddit = s;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(subreddit));
                window.setNavigationBarColor(Pallete.getDarkerColor(subreddit));
                CommentsScreenSingle.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(subreddit)));

            }

            pager = (ViewPager) findViewById(R.id.contentView);

            context = getIntent().getExtras().getString("context", "");
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Submission s = Authentication.reddit.getSubmission(params[0]);
                HasSeen.addSeen(s.getFullName());
                return s.getSubredditName();

            } catch (Exception e){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CommentsScreenSingle.this).setTitle(R.string.submission_not_found).setMessage(R.string.submission_not_found_msg).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        }).show();
                    }
                });

                return null;
            }


        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new CommentPage();
            Bundle args = new Bundle();
            if (name.contains("t3"))
                name = name.substring(3, name.length());

            args.putString("id", name);
            args.putString("context", context);
            args.putString("subreddit", subreddit);
            args.putBoolean("single", true);
            args.putBoolean("np", np);
            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {

            return 1;

        }


    }
}
