package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.LastComments;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LogUtil;

/**
 * This activity is responsible for the view when clicking on a post, showing
 * the post and its comments underneath with the slide left/right for the next
 * post.
 * <p/>
 * When the end of the currently loaded posts is being reached, more posts are
 * loaded asynchronously in {@link OverviewPagerAdapter}.
 * <p/>
 * Comments are displayed in the {@link CommentPage} fragment.
 * <p/>
 * Created by ccrama on 9/17/2015.
 */
public class CommentsScreen extends BaseActivityAnim implements SubmissionDisplay {
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_MULTIREDDIT = "multireddit";

    public PostLoader subredditPosts;
    int firstPage;

    OverviewPagerAdapter comments;
    private String subreddit;
    private String baseSubreddit;

    String multireddit;

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
        } else {
            return super.dispatchKeyEvent(event);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!Reddit.appRestart.contains("tutorialSwipeComment")) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipeComment", true).apply();
        } else if (!Reddit.appRestart.contains("tutorial_comm")) {
            Reddit.appRestart.edit().putBoolean("tutorial_comm", true).apply();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 14) {
            comments.notifyDataSetChanged();
            //todo make this work
        }
        if (requestCode == 333) {
            Reddit.appRestart.edit().putBoolean("tutorialSwipeComments", true).apply();

        }
    }

    public int currentPage;
    public ArrayList<Integer> seen;

    public int adjustAlpha(float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    public boolean popup;

    @Override
    public void onCreate(Bundle savedInstance) {


        popup = SettingValues.tabletUI && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !SettingValues.fullCommentOverride;
        seen = new ArrayList<>();
        if (popup) {
            disableSwipeBackLayout();
            applyColorTheme();
            setTheme(R.style.popup);
            supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            super.onCreate(savedInstance);
            setContentView(R.layout.activity_slide_popup);
        } else {
            overrideSwipeFromAnywhere();
            applyColorTheme();
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().getDecorView().setBackgroundDrawable(null);
            super.onCreate(savedInstance);
            setContentView(R.layout.activity_slide);
        }

        Reddit.setDefaultErrorHandler(this);

        firstPage = getIntent().getExtras().getInt(EXTRA_PAGE, -1);
        baseSubreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);
        subreddit = baseSubreddit;
        multireddit = getIntent().getExtras().getString(EXTRA_MULTIREDDIT);
        if (multireddit != null) {
            subredditPosts = new MultiredditPosts(multireddit);
            ((MultiredditPosts) subredditPosts).skipOne = true;

        } else {
            baseSubreddit = subreddit.toLowerCase();
            subredditPosts = new SubredditPosts(baseSubreddit, CommentsScreen.this);
        }
        if (firstPage == RecyclerView.NO_POSITION) {
            firstPage = 0;
            //IS SINGLE POST
            Log.w(LogUtil.getTag(), "Is single post?");
        } else {

            OfflineSubreddit o = OfflineSubreddit.getSubreddit(multireddit == null ? baseSubreddit : "multi" + multireddit, OfflineSubreddit.currentid);
            subredditPosts.getPosts().addAll(o.submissions);

        }

        if (subredditPosts.getPosts().isEmpty() || subredditPosts.getPosts().get(firstPage) == null) {
            finish();
        } else {
            updateSubredditAndSubmission(subredditPosts.getPosts().get(firstPage));

            final ViewPager pager = (ViewPager) findViewById(R.id.content_view);

            comments = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(comments);
            currentPage = firstPage;

            pager.setCurrentItem(firstPage + 1);

            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                                  if (position == firstPage && positionOffsetPixels == 0) {
                                                      finish();
                                                  }
                                                  if (position == firstPage && !popup) {
                                                      if (((OverviewPagerAdapter) pager.getAdapter()).blankPage != null)
                                                          ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(positionOffset);
                                                      pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));

                                                  }
                                              }

                                              @Override
                                              public void onPageSelected(int position) {
                                                  if (position != firstPage) {
                                                      position = position - 1;
                                                      updateSubredditAndSubmission(subredditPosts.getPosts().get(position));

                                                      if (subredditPosts.getPosts().size() - 2 <= position && subredditPosts.hasMore()) {
                                                          subredditPosts.loadMore(CommentsScreen.this.getApplicationContext(), CommentsScreen.this, false);
                                                      }

                                                      currentPage = position;
                                                      seen.add(position);

                                                      Bundle conData = new Bundle();
                                                      conData.putIntegerArrayList("seen", seen);
                                                      Intent intent = new Intent();
                                                      intent.putExtras(conData);
                                                      setResult(RESULT_OK, intent);
                                                  }
                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int state) {

                                              }
                                          }

            );
        }
        if (!Reddit.appRestart.contains("tutorialSwipeComments")) {
            Intent i = new Intent(this, SwipeTutorial.class);
            i.putExtra("subtitle", "Swipe from the left edge to exit comments.\n\nYou can swipe in the middle to get to the previous/next submission, this can be disabled in Settings > General > Swipe from Anywhere");
            startActivityForResult(i, 333);
        }
    }


    private void updateSubredditAndSubmission(Submission post) {
        subreddit = post.getSubredditName();
        themeSystemBars(subreddit);
        setRecentBar(subreddit);

        if (SettingValues.storeHistory) {
            if (post.isNsfw() && !SettingValues.storeNSFWHistory) {
            } else HasSeen.addSeen(post.getFullName());
            LastComments.setComments(post);

        }
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
    public void updateOfflineError() {
    }

    @Override
    public void updateError() {
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private CommentPage mCurrentFragment;
        public BlankFragment blankPage;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (getCurrentFragment() != object && object != null && object instanceof CommentPage) {
                mCurrentFragment = ((CommentPage) object);
                if (!mCurrentFragment.loaded) {
                    if (mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }

                }
            }
        }

        @Override
        public Fragment getItem(int i) {
            if (i == firstPage || i == 0) {
                blankPage = new BlankFragment();
                return blankPage;
            } else {
                i = i - 1;
                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                String name = subredditPosts.getPosts().get(i).getFullName();
                args.putString("id", name.substring(3, name.length()));
                args.putBoolean("archived", subredditPosts.getPosts().get(i).isArchived());
                args.putBoolean("locked", subredditPosts.getPosts().get(i).isLocked());
                args.putInt("page", i);
                args.putString("subreddit", subredditPosts.getPosts().get(i).getSubredditName());
                args.putString("baseSubreddit", multireddit == null ? baseSubreddit : "multi" + multireddit);

                f.setArguments(args);
                return f;

            }
        }

        @Override
        public int getCount() {

            return subredditPosts.getPosts().size();
        }

    }

}
