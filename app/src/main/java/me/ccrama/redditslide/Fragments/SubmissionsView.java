package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Cache;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SubtleSlideInUp;
import me.ccrama.redditslide.Visuals.Palette;

public class SubmissionsView extends Fragment implements SubmissionDisplay {
    public SubredditPosts posts;
    private RecyclerView rv;
    private FloatingActionButton fab;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private int totalItemCount;
    public SubmissionAdapter adapter;
    private String id;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        int currentOrientation = getResources().getConfiguration().orientation;


        if (rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            int i = ((LinearLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
            final StaggeredGridLayoutManager mLayoutManager;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && Reddit.tabletUI) {
                mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
            } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && Reddit.dualPortrait){
                mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            } else {
                mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

            }
            rv.setLayoutManager(mLayoutManager);
            mLayoutManager.scrollToPosition(i);


        } else {
            int i = 0;

            if (rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
                int[] firstVisibleItems = null;
                firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    i = firstVisibleItems[0];
                }
            } else {
                i = ((LinearLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            }
            final PreCachingLayoutManager mLayoutManager;
            mLayoutManager = new PreCachingLayoutManager(getActivity());
            rv.setLayoutManager(mLayoutManager);
            mLayoutManager.scrollToPosition(i);
        }
    }

    boolean down = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), new ColorPreferences(inflater.getContext()).getThemeSubreddit(id));
        View v = ((LayoutInflater) contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final StaggeredGridLayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && Reddit.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && Reddit.dualPortrait){
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        }
        rv.setLayoutManager(mLayoutManager);


        if (Reddit.animation)
            rv.setItemAnimator(new SubtleSlideInUp(getContext()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, getActivity()));

        mSwipeRefreshLayout.setRefreshing(true);


        if (Reddit.fab) {
            fab = (FloatingActionButton) v.findViewById(R.id.post_floating_action_button);

            if (Reddit.fabType == R.integer.FAB_POST) {
                fab.setImageResource(R.drawable.add);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent inte = new Intent(getActivity(), Submit.class);
                        inte.putExtra("subreddit", id);
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
                                            Reddit.seen.edit().putBoolean("fabClear", true).apply();
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
                                            Reddit.seen.edit().putBoolean("fabClear", true).apply();
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

        rv.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


                if (!posts.loading && !posts.nomore && !posts.offline) {

                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();
                    if (rv.getLayoutManager() instanceof PreCachingLayoutManager) {
                        pastVisiblesItems = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                        if (Reddit.scrollSeen) {
                            if (pastVisiblesItems > 0) {
                                HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                            }
                        }
                    } else {
                        int[] firstVisibleItems = null;
                        firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                            pastVisiblesItems = firstVisibleItems[0];
                            if (Reddit.scrollSeen) {
                                if (pastVisiblesItems > 0) {
                                    HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                                }
                            }
                        }
                    }

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        Log.v("Slide", "LOADING MORE" + totalItemCount);
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
                    if (dy <= 0 && fab.getId() != 0 && Reddit.fab) {

                        fab.show();


                    } else {
                        fab.hide();

                    }
                }
            }
        });
        Reddit.isLoading = false;

        doAdapter();
        return v;
    }

    public void doAdapter() {
        posts = new SubredditPosts(id);
        adapter = new SubmissionAdapter(getActivity(), posts, rv, posts.subreddit);
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

    private ArrayList<Submission> clearSeenPosts(boolean forever) {
        if (adapter.dataSet.posts != null) {
            ArrayList<Submission> originalDataSetPosts = adapter.dataSet.posts;

            for (int i = adapter.dataSet.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(adapter.dataSet.posts.get(i).getFullName())) {
                        if (forever) {
                            Hidden.setHidden(adapter.dataSet.posts.get(i));
                        }
                        adapter.dataSet.posts.remove(i);
                        if (adapter.dataSet.posts.size() == 0) {
                            adapter.notifyDataSetChanged();
                        } else {
                            if (Reddit.animation)

                                rv.setItemAnimator(new FadeInAnimator());

                            adapter.notifyItemRemoved(i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Let the loop reset itself
                }
            }
            if (Reddit.animation)
                rv.setItemAnimator(new SubtleSlideInUp(getContext()));
            return originalDataSetPosts;
        }
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
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
    public void update(List<Submission> submissions, boolean reset, boolean offline, String subreddit) {
        if (submissions != null && !submissions.isEmpty()) {
            int start = 0;
            if (posts != null) {
                start = posts.posts.size() + 1;
            }
            if (reset || offline || posts == null) {

                ArrayList<Submission> finalSubs = new ArrayList<>();
                for (Submission s : submissions) {

                    if (!PostMatch.doesMatch(s)) {
                        finalSubs.add(s);
                    }
                }
                posts.posts = finalSubs;
                start = -1;
            } else {
                ArrayList<Submission> finalSubs = new ArrayList<>();
                for (Submission s : submissions) {
                    if (!PostMatch.doesMatch(s)) {
                        finalSubs.add(s);
                    }
                }

                posts.posts.addAll(finalSubs);
                posts.offline = false;
            }

            final int finalStart = start;
            (adapter.sContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (finalStart != -1) {
                        adapter.notifyItemRangeInserted(finalStart, posts.posts.size());
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                }
            });
        } else if (submissions != null) {
            posts.nomore = true;
        } else if (Cache.hasSub(subreddit.toLowerCase()) && !posts.nomore && Reddit.cache) {
            Log.v("Slide", "GETTING SUB " + subreddit.toLowerCase());
            posts.offline = true;
            final OfflineSubreddit cached = Cache.getSubreddit(subreddit.toLowerCase());
            ArrayList<Submission> finalSubs = new ArrayList<>();
            for (Submission s : cached.submissions) {

                if (!PostMatch.doesMatch(s)) {
                    finalSubs.add(s);
                }
            }

            posts.posts = finalSubs;
            if (cached.submissions.size() > 0) {
                posts.stillShow = true;
            } else {
                mSwipeRefreshLayout.setRefreshing(false);

                adapter.setError(true);
            }
            (SubmissionAdapter.sContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(mSwipeRefreshLayout.getContext(), "Last updated " + TimeUtils.getTimeAgo(cached.time, mSwipeRefreshLayout.getContext()), Toast.LENGTH_SHORT).show();
                    }

                    adapter.notifyDataSetChanged();
                }
            });
        } else if (!posts.nomore) {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            adapter.setError(true);

        }
        if (submissions != null && mSwipeRefreshLayout != null)
            for (Submission s : submissions) {
                ContentType.ImageType type = ContentType.getImageType(s);

                String url = "";

                ImageLoadingListener l = new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                };

                if (!s.isNsfw() || SettingValues.NSFWPreviews) {
                    if (type == ContentType.ImageType.IMAGE) {
                        url = ContentType.getFixedUrl(s.getUrl());
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
                            ((Reddit)mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                        } else {
                            if (s.getThumbnailType() != Submission.ThumbnailType.NONE) {
                                ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(s.getThumbnail(), l);
                            }
                        }
                    } else if (s.getThumbnail() != null && (s.getThumbnailType() == Submission.ThumbnailType.URL || s.getThumbnailType() == Submission.ThumbnailType.NSFW)) {
                        if ((SettingValues.NSFWPreviews && s.getThumbnailType() == Submission.ThumbnailType.NSFW) || s.getThumbnailType() == Submission.ThumbnailType.URL) {
                            if (SettingValues.bigPicEnabled) {
                                ((Reddit)mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(url, l);
                            } else {
                                if (s.getThumbnailType() != Submission.ThumbnailType.NONE) {
                                    ((Reddit) mSwipeRefreshLayout.getContext().getApplicationContext()).getImageLoader().loadImage(s.getThumbnail(), l);
                                }
                            }

                        }
                    }


                }
            }
    }

}