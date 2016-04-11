package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.Gif;
import me.ccrama.redditslide.Fragments.ImageFull;
import me.ccrama.redditslide.Fragments.MediaFragment;
import me.ccrama.redditslide.Fragments.SelftextFull;
import me.ccrama.redditslide.Fragments.TitleFull;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Shadowbox extends FullScreenActivity implements SubmissionDisplay {
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_MULTIREDDIT = "multireddit";
    public OfflineSubreddit submissions;
    private PostLoader subredditPosts;
    private String subreddit;
    int firstPage;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);

        firstPage = getIntent().getExtras().getInt(EXTRA_PAGE, 0);
        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);
        String multireddit = getIntent().getExtras().getString(EXTRA_MULTIREDDIT);
        if (multireddit != null) {
            subredditPosts = new MultiredditPosts(multireddit);
        } else {
            subredditPosts = new SubredditPosts(subreddit, Shadowbox.this);
        }
        subreddit = multireddit == null ? subreddit : ("multi" + multireddit);

        submissions = OfflineSubreddit.getSubreddit(subreddit);
        subredditPosts.getPosts().addAll(submissions.submissions);
        ViewPager pager = (ViewPager) findViewById(R.id.content_view);
        submissionsPager = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(submissionsPager);
        pager.setCurrentItem(firstPage);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                HasSeen.addSeen(subredditPosts.getPosts().get(position).getFullName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    OverviewPagerAdapter submissionsPager;

    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (startIndex != -1) {
                    // TODO determine correct behaviour
                    //comments.notifyItemRangeInserted(startIndex, posts.posts.size());
                    submissionsPager.notifyDataSetChanged();
                } else {
                    submissionsPager.notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                submissionsPager.notifyDataSetChanged();
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

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f;
            ContentType.ImageType t = ContentType.getImageType(subredditPosts.getPosts().get(i));

            if (subredditPosts.getPosts().size() - 2 <= i && subredditPosts.hasMore()) {
                subredditPosts.loadMore(Shadowbox.this.getApplicationContext(), Shadowbox.this, false);
            }
            switch (t) {
                case GIF:
                case IMAGE:
                case REDDIT:
                case EMBEDDED:
                case LINK:
                case VIDEO:
                {
                    f = new MediaFragment();
                    Bundle args = new Bundle();
                    Submission submission = submissions.submissions.get(i);
                    String previewUrl = "";
                    if (submission.getDataNode().has("preview") && submission.getDataNode().get("preview").get("images").get(0).get("source").has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                        previewUrl = submission.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                    }
                    args.putString("contentUrl", submission.getUrl());
                    args.putString("firstUrl", previewUrl);
                    args.putInt("page", i);
                    args.putString("sub", subreddit);
                    f.setArguments(args);
                }
                break;
                case SELF: {
                    if (subredditPosts.getPosts().get(i).getSelftext().isEmpty()) {
                        f = new TitleFull();
                        Bundle args = new Bundle();
                        args.putInt("page", i);
                        args.putString("sub", subreddit);

                        f.setArguments(args);
                    } else {
                        f = new SelftextFull();
                        Bundle args = new Bundle();
                        args.putInt("page", i);
                        args.putString("sub", subreddit);

                        f.setArguments(args);
                    }
                }
                break;
                case ALBUM: {
                    f = new AlbumFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    args.putString("sub", subreddit);

                    f.setArguments(args);
                }
                break;
                case VID_ME:
                case STREAMABLE:{
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    args.putString("sub", subreddit);

                    f.setArguments(args);
                }
                break;
                case NONE: {
                    if (subredditPosts.getPosts().get(i).getSelftext().isEmpty()) {
                        f = new TitleFull();
                        Bundle args = new Bundle();
                        args.putInt("page", i);
                        args.putString("sub", subreddit);

                        f.setArguments(args);
                    } else {
                        f = new SelftextFull();
                        Bundle args = new Bundle();
                        args.putInt("page", i);
                        args.putString("sub", subreddit);

                        f.setArguments(args);
                    }
                }
                break;

                default: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    args.putString("sub", subreddit);

                    f.setArguments(args);
                }
                break;
            }


            return f;


        }


        @Override
        public int getCount() {
            int offset = 0;

            return subredditPosts.getPosts().size() + offset;
        }


    }

}
