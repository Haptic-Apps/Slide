package me.ccrama.redditslide.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 *
 * This activity takes parameters for a submission id (through intent or direct link),
 * retrieves the Submission object, and then displays the submission with its comments.
 *
 */
public class CommentsScreenSingle extends BaseActivityAnim {
    OverviewPagerAdapter comments;
    boolean np;
    private ViewPager pager;
    private String subreddit;
    private String name;
    private String context;

    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_CONTEXT = "context";
    public static final String EXTRA_SUBMISSION = "submission";
    public static final String EXTRA_NP = "np";
    public static final String EXTRA_LOADMORE = "loadmore";
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (comments.getCurrentFragment() != null && SettingValues.postNav && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); // Get audioManager
            switch (keyCode) {
                case (KeyEvent.KEYCODE_VOLUME_UP):
                {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
                    return ((SubmissionsView) comments.getCurrentFragment()).onKeyDown(keyCode);
                }
                case (KeyEvent.KEYCODE_VOLUME_DOWN):
                {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
                    return ((SubmissionsView) comments.getCurrentFragment()).onKeyDown(keyCode);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);
        StyleView.styleActivity(this);
        name = getIntent().getExtras().getString(EXTRA_SUBMISSION, "");

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        np = getIntent().getExtras().getBoolean(EXTRA_NP, false);
        context = getIntent().getExtras().getString(EXTRA_CONTEXT, "");

        if (subreddit.equals(Reddit.EMPTY_STRING)) {
            new AsyncGetSubredditName().execute(name);
        } else {
            setupAdapter();
        }

    }

    private void setupAdapter() {
        themeSystemBars(subreddit);
        setRecentBar(subreddit);

        pager = (ViewPager) findViewById(R.id.content_view);
        comments = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(comments);
    }

    private class AsyncGetSubredditName extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            subreddit = s;
            setupAdapter();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Submission s = Authentication.reddit.getSubmission(params[0]);
                HasSeen.addSeen(s.getFullName());
                return s.getSubredditName();

            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CommentsScreenSingle.this)
                                .setTitle(R.string.submission_not_found)
                                .setMessage(R.string.submission_not_found_msg)
                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
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

        private Fragment mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
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
