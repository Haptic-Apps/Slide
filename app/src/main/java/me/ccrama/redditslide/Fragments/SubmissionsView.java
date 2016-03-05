package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import net.dean.jraw.models.Submission;

import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class SubmissionsView extends Fragment implements SubmissionDisplay {
    public SubredditPosts posts;
    public RecyclerView rv;
    private FloatingActionButton fab;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private int totalItemCount;
    public SubmissionAdapter adapter;
    public String id;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public boolean onKeyDown(int keyCode) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            goDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            goUp();
            return true;
        }
        return false;
    }

    private void goUp() {
        if (adapter.dataSet.posts != null) {

            int position = 0;
            int currentOrientation = getResources().getConfiguration().orientation;
            RecyclerView.SmoothScroller smoothScroller = null;
            if (rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                position = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), rv.getLayoutManager());

            } else if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                int[] firstVisibleItems = null;
                firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    position = firstVisibleItems[0];
                    (rv.getLayoutManager()).smoothScrollToPosition(rv, new RecyclerView.State(), position - 1);
                    return;
                }
            } else {
                position = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), rv.getLayoutManager());

            }

            if (smoothScroller != null) {
                smoothScroller.setTargetPosition(position - 1);
                (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
            }

        }
    }

    private void goDown() {
        if (adapter.dataSet.posts != null) {

            int position = 0;
            int currentOrientation = getResources().getConfiguration().orientation;
            RecyclerView.SmoothScroller smoothScroller = null;
            if (rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                position = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), rv.getLayoutManager());

            } else if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                int[] firstVisibleItems = null;
                firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    position = firstVisibleItems[0];
                    (rv.getLayoutManager()).smoothScrollToPosition(rv, new RecyclerView.State(), position + 1);
                    return;
                }
            } else {
                position = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                smoothScroller = new TopSnappedSmoothScroller(rv.getContext(), rv.getLayoutManager());

            }

            if (smoothScroller != null) {
                smoothScroller.setTargetPosition(position + 1);
                (rv.getLayoutManager()).startSmoothScroll(smoothScroller);
            }

        }
    }

    public static class TopSnappedSmoothScroller extends LinearSmoothScroller {
        final RecyclerView.LayoutManager lm;

        public TopSnappedSmoothScroller(Context context, RecyclerView.LayoutManager lm) {
            super(context);
            this.lm = lm;

        }

        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            if (lm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) lm).computeScrollVectorForPosition(targetPosition);

            } else if (lm instanceof PreCachingLayoutManager) {
                return ((PreCachingLayoutManager) lm).computeScrollVectorForPosition(targetPosition);

            }
            return null;
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        int currentOrientation = newConfig.orientation;

        int i = 0;
        if (rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (rv.getAdapter() != null) {
                i = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
            }
            final RecyclerView.LayoutManager mLayoutManager;
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
                mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            } else {
                mLayoutManager = new PreCachingLayoutManager(getContext());
            }

            rv.setLayoutManager(mLayoutManager);


        } else {
            final RecyclerView.LayoutManager mLayoutManager;

            if (rv.getAdapter() != null) {
                if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                    int[] firstVisibleItems = null;
                    firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        i = firstVisibleItems[0];
                    }
                } else {
                    i = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                }
            }
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
                mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            } else {
                mLayoutManager = new PreCachingLayoutManager(getContext());
            }
            rv.setLayoutManager(mLayoutManager);

        }
        rv.getLayoutManager().scrollToPosition(i);

    }


    boolean down = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), new ColorPreferences(inflater.getContext()).getThemeSubreddit(id));
        View v = ((LayoutInflater) contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final RecyclerView.LayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        } else {
            mLayoutManager = new PreCachingLayoutManager(getActivity());

        }

        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, getContext()));

        mSwipeRefreshLayout.setProgressViewOffset(false, Reddit.pxToDp(104, getContext()), Reddit.pxToDp(140, getContext()));


        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (posts == null || (posts != null && !posts.offline))
                    mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        if (SettingValues.fab) {
            fab = (FloatingActionButton) v.findViewById(R.id.post_floating_action_button);

            if (SettingValues.fabType == R.integer.FAB_POST) {
                fab.setImageResource(R.drawable.add);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent inte = new Intent(getActivity(), Submit.class);
                        inte.putExtra(Submit.EXTRA_SUBREDDIT, id);
                        getActivity().startActivity(inte);
                    }
                });
            } else {
                fab.setImageResource(R.drawable.hide);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!Reddit.fabClear) {
                            new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.settings_fabclear)
                                    .setMessage(R.string.settings_fabclear_msg)
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Reddit.seen.edit().putBoolean(SettingValues.PREF_FAB_CLEAR, true).apply();
                                            Reddit.fabClear = true;
                                            clearSeenPosts(false);

                                        }
                                    }).show();
                        } else {
                            clearSeenPosts(false);
                        }
                    }
                });
                fab.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!Reddit.fabClear) {
                            new AlertDialogWrapper.Builder(getActivity()).setTitle(R.string.settings_fabclear)
                                    .setMessage(R.string.settings_fabclear_msg)
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Reddit.seen.edit().putBoolean(SettingValues.PREF_FAB_CLEAR, true).apply();
                                            Reddit.fabClear = true;
                                            clearSeenPosts(true);

                                        }
                                    }).show();
                        } else {
                            clearSeenPosts(true);

                        }
                        /*
                        ToDo Make a sncakbar with an undo option of the clear all
                        View.OnClickListener undoAction = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                adapter.dataSet.posts = original;
                                for(Submission post : adapter.dataSet.posts){
                                    if(HasSeen.getSeen(post.getFullName()))
                                        Hidden.undoHidden(post);
                                }
                            }
                        };*/
                        Snackbar.make(rv, getResources().getString(R.string.posts_hidden_forever), Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                });
            }
        } else {
            v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);
        }

        rv.addOnScrollListener(new ToolbarScrollHideHandler(((MainActivity) getActivity()).mToolbar, getActivity().findViewById(R.id.header)) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!posts.loading && !posts.nomore && !posts.offline) {

                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();
                    if (rv.getLayoutManager() instanceof PreCachingLayoutManager) {
                        pastVisiblesItems = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                        if (SettingValues.scrollSeen) {
                            if (pastVisiblesItems > 0) {
                                HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                            }
                        }
                    } else {
                        int[] firstVisibleItems = null;
                        firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                            pastVisiblesItems = firstVisibleItems[0];
                            if (SettingValues.scrollSeen) {
                                if (pastVisiblesItems > 0) {
                                    HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                                }
                            }
                        }
                    }

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        posts.loading = true;
                        posts.loadMore(mSwipeRefreshLayout.getContext(), SubmissionsView.this, false, posts.subreddit);

                    }
                }

                /*
                if(dy <= 0 && !down){
                    (getActivity()).findViewById(R.id.header).animate().translationY(((BaseActivity)getActivity()).mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
                    down = true;
                } else if(down){
                    (getActivity()).findViewById(R.id.header).animate().translationY(((BaseActivity)getActivity()).mToolbar.getTop()).setInterpolator(new AccelerateInterpolator()).start();
                    down = false;
                }*///todo For future implementation instead of scrollFlags

                if (fab != null) {
                    if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {

                        fab.show();


                    } else {
                        fab.hide();

                    }
                }
            }
        });

        Reddit.isLoading = false;
        if (MainActivity.shouldLoad.equals(id)) {
            doAdapter();
        }
        return v;
    }

    public boolean main;

    public void doAdapter() {
        posts = new SubredditPosts(id);
        adapter = new SubmissionAdapter(getActivity(), posts, rv, id);
        rv.setAdapter(adapter);
        posts.loadMore(mSwipeRefreshLayout.getContext(), this, true);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );
    }

    private List<Submission> clearSeenPosts(boolean forever) {
        if (adapter.dataSet.posts != null) {
            OfflineSubreddit.getSubreddit(id.toLowerCase()).clearSeenPosts(false);

            List<Submission> originalDataSetPosts = adapter.dataSet.posts;

            for (int i = adapter.dataSet.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(adapter.dataSet.posts.get(i))) {
                        if (forever) {
                            Hidden.setHidden(adapter.dataSet.posts.get(i));
                        }
                        adapter.dataSet.posts.remove(i);
                        if (adapter.dataSet.posts.size() == 0) {
                            adapter.notifyDataSetChanged();
                        } else {
                            rv.setItemAnimator(new FadeInAnimator());
                            adapter.notifyItemRemoved(i + 1);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Let the loop reset itself
                }
            }
            rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));
            return originalDataSetPosts;
        }

        return null;
    }

    boolean forceLoad;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
        main = bundle.getBoolean("main", false);
        forceLoad = bundle.getBoolean("load",false);

    }

    private void refresh() {
        posts.forced = true;
        posts.loadMore(mSwipeRefreshLayout.getContext(), this, true, id);
    }

    public void forceRefresh() {
        rv.scrollToPosition(0);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                refresh();
            }
        });
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void updateSuccess(final List<Submission> submissions, final int startIndex) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (startIndex != -1) {
                        adapter.notifyItemRangeInserted(startIndex + 1, posts.posts.size());
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                }
            });

            loadImages(submissions);
        }
    }

    private void loadImages(List<Submission> submissions) {
        for (Submission s : submissions) {
            ContentType.ImageType type = ContentType.getImageType(s);

            String url = "";

            ImageLoadingListener l = new SimpleImageLoadingListener();

            if (!s.isNsfw() || SettingValues.NSFWPreviews) {
                if (type == ContentType.ImageType.IMAGE) {
                    url = s.getUrl();
                    if (SettingValues.bigPicEnabled) {
                        ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                    } else {
                        if (s.getThumbnailType() != Submission.ThumbnailType.NONE) {
                            ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(s.getThumbnail(), l);
                        }
                    }
                } else if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height")) {
                    url = s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                    if (SettingValues.bigPicEnabled) {
                        ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                    } else if (s.getThumbnailType() != Submission.ThumbnailType.NONE) {
                        ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(s.getThumbnail(), l);
                    }
                } else if (s.getThumbnail() != null && (s.getThumbnailType() == Submission.ThumbnailType.URL || s.getThumbnailType() == Submission.ThumbnailType.NSFW)) {
                    if ((SettingValues.NSFWPreviews && s.getThumbnailType() == Submission.ThumbnailType.NSFW) || s.getThumbnailType() == Submission.ThumbnailType.URL) {
                        if (SettingValues.bigPicEnabled) {
                            ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                        } else if (s.getThumbnailType() != Submission.ThumbnailType.NONE) {
                            ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(s.getThumbnail(), l);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(mSwipeRefreshLayout.getContext(), getString(R.string.offline_last_update, TimeUtils.getTimeAgo(cacheTime, mSwipeRefreshLayout.getContext())), Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();


    }

    @Override
    public void updateOfflineError() {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }

    @Override
    public void updateError() {
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }
}