package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Autocache.AutoCacheScheduler;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 * <p/>
 * This activity takes parameters for a submission id (through intent or direct link), retrieves the
 * Submission object, and then displays the submission with its comments.
 */
public class CommentsScreenSingle extends BaseActivityAnim {
    OverviewPagerAdapter comments;
    boolean              np;
    private ViewPager pager;
    private String    subreddit;
    private String    name;
    private String    context;
    private int       contextNumber;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 14 && comments != null) {
            comments.notifyDataSetChanged();
        }
    }

    public static final String EXTRA_SUBREDDIT  = "subreddit";
    public static final String EXTRA_CONTEXT    = "context";
    public static final String EXTRA_CONTEXT_NUMBER    = "contextNumber";
    public static final String EXTRA_SUBMISSION = "submission";
    public static final String EXTRA_NP         = "np";
    public static final String EXTRA_LOADMORE   = "loadmore";

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (SettingValues.commentVolumeNav) {

            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
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
        getWindow().getDecorView().setBackground(null);
        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);
        name = getIntent().getExtras().getString(EXTRA_SUBMISSION, "");

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        np = getIntent().getExtras().getBoolean(EXTRA_NP, false);
        context = getIntent().getExtras().getString(EXTRA_CONTEXT, "");

        contextNumber = getIntent().getExtras().getInt(EXTRA_CONTEXT_NUMBER, 5);

        if (subreddit.equals(Reddit.EMPTY_STRING)) {
            new AsyncGetSubredditName().execute(name);
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            findViewById(R.id.content_view).setBackgroundColor(color);
        } else {
            setupAdapter();
        }
        if (Authentication.isLoggedIn && Authentication.me == null) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    if (Authentication.reddit == null) {
                        new Authentication(getApplicationContext());
                    } else {
                        try {
                            Authentication.me = Authentication.reddit.me();
                            Authentication.mod = Authentication.me.isMod();

                            Authentication.authentication.edit()
                                    .putBoolean(Reddit.SHARED_PREF_IS_MOD, Authentication.mod)
                                    .apply();

                            if (Reddit.notificationTime != -1) {
                                Reddit.notifications = new NotificationJobScheduler(CommentsScreenSingle.this);
                                Reddit.notifications.start(getApplicationContext());
                            }

                            if (Reddit.cachedData.contains("toCache")) {
                                Reddit.autoCache = new AutoCacheScheduler(CommentsScreenSingle.this);
                                Reddit.autoCache.start(getApplicationContext());
                            }

                            final String name = Authentication.me.getFullName();
                            Authentication.name = name;
                            LogUtil.v("AUTHENTICATED");
                            UserSubscriptions.doCachedModSubs();

                            if (Authentication.reddit.isAuthenticated()) {
                                final Set<String> accounts =
                                        Authentication.authentication.getStringSet("accounts", new HashSet<String>());
                                if (accounts.contains(name)) { //convert to new system
                                    accounts.remove(name);
                                    accounts.add(name + ":" + Authentication.refresh);
                                    Authentication.authentication.edit()
                                            .putStringSet("accounts", accounts)
                                            .apply(); //force commit
                                }
                                Authentication.isLoggedIn = true;
                                Reddit.notFirst = true;
                            }
                        } catch (Exception e){
                            new Authentication(getApplicationContext());
                        }
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public int adjustAlpha(float factor) {
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
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
                if (position == 0 && positionOffsetPixels == 0) {
                    finish();
                }
                if (position == 0
                        && ((OverviewPagerAdapter) pager.getAdapter()).blankPage != null) {
                    ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(positionOffset);
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
    boolean contest;

    private class AsyncGetSubredditName extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            subreddit = s;
            setupAdapter();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                final Submission s = Authentication.reddit.getSubmission(params[0]);
                if (SettingValues.storeHistory) {
                    if (SettingValues.storeNSFWHistory && s.isNsfw() || !s.isNsfw()) {
                        HasSeen.addSeen(s.getFullName());
                    }
                    LastComments.setComments(s);
                }
                HasSeen.setHasSeenSubmission(new ArrayList<Submission>() {{
                    this.add(s);
                }});
                locked = s.isLocked();
                archived = s.isArchived();
                contest = s.getDataNode().get("contest_mode").asBoolean();
                if(s.getSubredditName() == null){
                    subreddit = "Promoted";
                } else {
                    subreddit = s.getSubredditName();
                }
                return subreddit;

            } catch (Exception e) {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialogWrapper.Builder(CommentsScreenSingle.this).setTitle(
                                    R.string.submission_not_found)
                                    .setMessage(R.string.submission_not_found_msg)
                                    .setPositiveButton(R.string.btn_ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    finish();
                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });
                } catch (Exception ignored) {

                }
                return null;
            }


        }
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        private Fragment      mCurrentFragment;
        public  BlankFragment blankPage;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = (Fragment) object;
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                blankPage = new BlankFragment();
                return blankPage;
            } else {

                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                if (name.contains("t3_")) name = name.substring(3, name.length());

                args.putString("id", name);
                args.putString("context", context);
                if (SettingValues.storeHistory) {
                    if (context != null && !context.isEmpty() && !context.equals(
                            Reddit.EMPTY_STRING)) {
                        HasSeen.addSeen("t1_" + context);
                    } else {
                        HasSeen.addSeen(name);
                    }
                }

                args.putBoolean("archived", archived);
                args.putBoolean("locked", locked);
                args.putBoolean("contest", contest);
                args.putInt("contextNumber", contextNumber);
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
