package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;

import net.dean.jraw.models.Submission;

import java.util.List;

import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.Gif;
import me.ccrama.redditslide.Fragments.ImageFull;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Shadowbox extends FullScreenActivity implements SubmissionDisplay {
    private PostLoader subredditPosts;
    private String subreddit;
    int firstPage;
    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);
        applyColorTheme();
        setContentView(R.layout.activity_slide);

        firstPage = getIntent().getExtras().getInt("page", -1);
        subreddit = getIntent().getExtras().getString("subreddit");
        String multireddit = getIntent().getExtras().getString("multireddit");
        if (multireddit != null) {
            subredditPosts = new MultiredditPosts(multireddit);
        } else {
            subredditPosts = new SubredditPosts(subreddit);
        }
        if (firstPage == RecyclerView.NO_POSITION) {
            //IS SINGLE POST
        } else {
            subredditPosts.getPosts().addAll(DataShare.sharedSubreddit);
            subredditPosts.loadMore(this.getApplicationContext(), this, true);
        }        ViewPager pager = (ViewPager) findViewById(R.id.content_view);

        submissionsPager = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(submissionsPager);
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
    public void updateOfflineError() {}

    @Override
    public void updateError() {}
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

                case NSFW_IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case REDDIT: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case EMBEDDED: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case IMAGE_LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NSFW_LINK: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case SELF: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case ALBUM: {
                    f = new AlbumFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_GFY: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_GIF: {
                    f = new Gif();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_IMAGE: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case VIDEO: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                case NONE_URL: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
                    f.setArguments(args);
                }
                break;
                default: {
                    f = new ImageFull();
                    Bundle args = new Bundle();
                    args.putInt("page", i);
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
