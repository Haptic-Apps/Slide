package me.ccrama.redditslide.Activities;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.GalleryView;
import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.Fragments.AlbumFull;
import me.ccrama.redditslide.Fragments.MediaFragment;
import me.ccrama.redditslide.Fragments.SelftextFull;
import me.ccrama.redditslide.Fragments.TitleFull;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostLoader;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Gallery extends FullScreenActivity implements SubmissionDisplay {
    public static final String EXTRA_PROFILE     = "profile";
    public static final String EXTRA_PAGE        = "page";
    public static final String EXTRA_SUBREDDIT   = "subreddit";
    public static final String EXTRA_MULTIREDDIT = "multireddit";
    public PostLoader subredditPosts;
    public String     subreddit;

    public ArrayList<Submission> baseSubs;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();
        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT);
        String multireddit = getIntent().getExtras().getString(EXTRA_MULTIREDDIT);
        String profile = getIntent().getExtras().getString(EXTRA_PROFILE, "");
        if (multireddit != null) {
            subredditPosts = new MultiredditPosts(multireddit, profile);
        } else {
            subredditPosts = new SubredditPosts(subreddit, Gallery.this);
        }
        subreddit = multireddit == null ? subreddit : ("multi" + multireddit);

        if (multireddit == null) {
            setShareUrl("https://reddit.com/r/" + subreddit);
        }

        applyDarkColorTheme(subreddit);
        super.onCreate(savedInstance);
        setContentView(R.layout.gallery);
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        long offline = getIntent().getLongExtra("offline", 0L);

        final OfflineSubreddit submissions =
                OfflineSubreddit.getSubreddit(subreddit, offline, !Authentication.didOnline, this);

        baseSubs = new ArrayList<>();

        for (Submission s : submissions.submissions) {
            if (s.getThumbnails() != null && s.getThumbnails().getSource() != null) {
                baseSubs.add(s);
            } else if (ContentType.getContentType(s) == ContentType.Type.IMAGE) {
                baseSubs.add(s);
            }
            subredditPosts.getPosts().add(s);
        }

        rv = (RecyclerView) findViewById(R.id.content_view);
        recyclerAdapter = new GalleryView(this, baseSubs, subreddit);
        RecyclerView.LayoutManager layoutManager =
                createLayoutManager(getNumColumns(getResources().getConfiguration().orientation));
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(recyclerAdapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int[] firstVisibleItems =
                        ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(
                                null);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    for (int firstVisibleItem : firstVisibleItems) {
                        pastVisiblesItems = firstVisibleItem;
                    }
                }

                if ((visibleItemCount + pastVisiblesItems) + 5 >= totalItemCount) {
                    if (subredditPosts instanceof SubredditPosts) {
                        if (!((SubredditPosts) subredditPosts).loading) {
                            ((SubredditPosts) subredditPosts).loading = true;
                            ((SubredditPosts) subredditPosts).loadMore(Gallery.this, Gallery.this,
                                    false, subreddit);

                        }
                    } else if (subredditPosts instanceof MultiredditPosts) {
                        if (!((MultiredditPosts) subredditPosts).loading) {
                            ((MultiredditPosts) subredditPosts).loading = true;
                            (subredditPosts).loadMore(Gallery.this, Gallery.this, false);

                        }
                    }
                }
            }

        });

    }

    GalleryView recyclerAdapter;
    public int pastVisiblesItems;
    public int visibleItemCount;
    public int totalItemCount;
    RecyclerView rv;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final int currentOrientation = newConfig.orientation;

        final CatchStaggeredGridLayoutManager mLayoutManager =
                (CatchStaggeredGridLayoutManager) rv.getLayoutManager();

        mLayoutManager.setSpanCount(getNumColumns(currentOrientation));
    }

    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int startSize = baseSubs.size();
                for (Submission s : submissions) {
                    if (!baseSubs.contains(s)
                            && s.getThumbnails() != null
                            && s.getThumbnails().getSource() != null) {
                        baseSubs.add(s);
                    }
                }
                recyclerAdapter.notifyItemRangeInserted(startSize, baseSubs.size() - startSize);
            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateOfflineError() {
    }

    @Override
    public void updateError() {
    }

    @Override
    public void updateViews() {
        recyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAdapterUpdated() {
        recyclerAdapter.notifyDataSetChanged();
    }

    @NonNull
    private RecyclerView.LayoutManager createLayoutManager(final int numColumns) {
        return new CatchStaggeredGridLayoutManager(numColumns,
                CatchStaggeredGridLayoutManager.VERTICAL);
    }

    private int getNumColumns(final int orientation) {
        final int numColumns;
        boolean singleColumnMultiWindow = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            singleColumnMultiWindow = Gallery.this.isInMultiWindowMode() && SettingValues.singleColumnMultiWindow;
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.isPro && !singleColumnMultiWindow) {
            numColumns = Reddit.dpWidth;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT
                && SettingValues.dualPortrait) {
            numColumns = 2;
        } else {
            numColumns = 1;
        }
        return numColumns;
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = null;
            ContentType.Type t = ContentType.getContentType(baseSubs.get(i));

            if (baseSubs.size() - 2 <= i && subredditPosts.hasMore()) {
                subredditPosts.loadMore(Gallery.this.getApplicationContext(), Gallery.this, false);
            }
            switch (t) {
                case GIF:
                case IMAGE:
                case IMGUR:
                case REDDIT:
                case EXTERNAL:
                case SPOILER:
                case XKCD:
                case DEVIANTART:
                case EMBEDDED:
                case LINK:
                case STREAMABLE:
                case VIDEO: {
                    f = new MediaFragment();
                    Bundle args = new Bundle();
                    Submission submission = baseSubs.get(i);
                    String previewUrl = "";
                    if (t != ContentType.Type.XKCD
                            && submission.getDataNode().has("preview")
                            && submission.getDataNode()
                            .get("preview")
                            .get("images")
                            .get(0)
                            .get("source")
                            .has("height")) { //Load the preview image which has probably already been cached in memory instead of the direct link
                        previewUrl = submission.getDataNode()
                                .get("preview")
                                .get("images")
                                .get(0)
                                .get("source")
                                .get("url")
                                .asText();
                    }
                    args.putString("contentUrl", submission.getUrl());
                    args.putString("firstUrl", previewUrl);
                    args.putInt("page", i);
                    args.putString("sub", subreddit);
                    f.setArguments(args);
                }
                break;
                case SELF:
                case NONE: {
                    if (baseSubs.get(i).getSelftext().isEmpty()) {
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
            }


            return f;


        }


        @Override
        public int getCount() {
            return baseSubs.size();
        }


    }

}
