package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 14){
            comments.notifyDataSetChanged();
        }
    }
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_CONTEXT = "context";
    public static final String EXTRA_SUBMISSION = "submission";
    public static final String EXTRA_NP = "np";
    public static final String EXTRA_LOADMORE = "loadmore";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (SettingValues.commentNav) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    return ((CommentPage) comments.getCurrentFragment()).onKeyDown(keyCode, event);
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    return ((CommentPage) comments.getCurrentFragment()).onKeyDown(keyCode, event);
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        disableSwipeBackLayout();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);
        name = getIntent().getExtras().getString(EXTRA_SUBMISSION, "");

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        np = getIntent().getExtras().getBoolean(EXTRA_NP, false);
        context = getIntent().getExtras().getString(EXTRA_CONTEXT, "");

        if (subreddit.equals(Reddit.EMPTY_STRING)) {
            new AsyncGetSubredditName().execute(name);
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            findViewById(R.id.content_view).setBackgroundColor(color);
        } else {
            setupAdapter();
        }

    }

    public int adjustAlpha( float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    private void setupAdapter() {
        themeSystemBars(subreddit);
        setRecentBar(subreddit);

        pager = (ViewPager) findViewById(R.id.content_view);
        comments = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(comments);
        pager.setBackgroundColor(Color.TRANSPARENT);
        pager.setCurrentItem(1);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == 0 && positionOffsetPixels == 0){
                    finish();
                }
                if(position == 0 && ((OverviewPagerAdapter)pager.getAdapter()).blankPage != null){
                    ((OverviewPagerAdapter)pager.getAdapter()).blankPage.doOffset(positionOffset);
                    pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    boolean locked;
    boolean archived;

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
                LastComments.setComments(s);
                locked = s.isLocked();
                archived = s.isArchived();
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
        public BlankFragment blankPage;

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
            if(i == 0){
                blankPage = new BlankFragment();
                return blankPage;
            } else {

                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                if (name.contains("t3_"))
                    name = name.substring(3, name.length());

                args.putString("id", name);
                args.putString("context", context);
                if(context != null && !context.isEmpty()){
                    LogUtil.v(context);
                    HasSeen.addSeen("t1_" + context);
                }
                args.putBoolean("archived", archived);
                args.putBoolean("locked", locked);
                args.putString("subreddit", subreddit);
                args.putBoolean("single", getIntent().getBooleanExtra(EXTRA_LOADMORE, true));
                args.putBoolean("np", np);
                f.setArguments(args);

                return f;
            }

        }


        @Override
        public int getCount() {

            return 2;

        }


    }


}
