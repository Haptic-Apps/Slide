package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;

import net.dean.jraw.models.Submission;

import java.util.List;

import it.sephiroth.android.library.tooltip.Tooltip;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 *
 * This activity is the same as CommentScreen, but allows for it to show up in a dialog-like
 * layout for Tablet mode.
 *
 */
public class CommentsScreenPopup extends BaseActivityAnim {
    public static final String EXTRA_PAGE = "page";
    OverviewPagerAdapter comments;
    private List<Submission> posts;
    String subreddit;
    String multireddit;
    public OfflineSubreddit o;
    boolean tip;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return ((CommentPage) comments.getCurrentFragment()).onKeyDown(keyCode);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return ((CommentPage) comments.getCurrentFragment()).onKeyDown(keyCode);
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        applyColorTheme();
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_slide_popup);

        subreddit = getIntent().getExtras().getString(CommentsScreen.EXTRA_SUBREDDIT);

        int firstPage = getIntent().getExtras().getInt(EXTRA_PAGE, -1);
        if (firstPage == -1) {
            //IS SNIGLE POST
        } else {
            posts = OfflineSubreddit.getSubreddit(subreddit).submissions;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Palette.getDarkerColor(posts.get(firstPage).getSubredditName()));
        }
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        HasSeen.addSeen(posts.get(firstPage).getFullName());

        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(firstPage);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //todo load more
                themeSystemBars(posts.get(position).getSubredditName());
                HasSeen.addSeen(posts.get(position).getFullName());

                if (tip) {
                    Tooltip.removeAll(CommentsScreenPopup.this);
                    Reddit.appRestart.edit().putString("tutorial_6", "t").apply();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if(!Reddit.appRestart.contains("tutorial_comm")){
            tip = true;
            Tooltip.make(CommentsScreenPopup.this,
                    new Tooltip.Builder(106)
                            .anchor(findViewById(R.id.content_view), Tooltip.Gravity.CENTER)
                            .text("Swipe left and right to go between submissions. You can disable this in General Settings")
                            .maxWidth(600)
                            .activateDelay(800)
                            .showDelay(300)
                            .withArrow(true)
                            .withOverlay(true)
                            .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                            .build()
            ).show();
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

            String name = posts.get(i).getFullName();
            args.putString("id", name.substring(3, name.length()));
            Log.v(LogUtil.getTag(), name.substring(3, name.length()));
            args.putBoolean("archived", posts.get(i).isArchived());
            args.putInt("page", i);
            args.putString("subreddit", multireddit==null?subreddit:"multi"+multireddit);

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
