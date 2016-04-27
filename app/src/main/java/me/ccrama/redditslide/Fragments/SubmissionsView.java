package me.ccrama.redditslide.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.Adapters.SubmissionAdapter;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class SubmissionsView extends Fragment implements SubmissionDisplay {
    public SubredditPosts posts;
    public RecyclerView rv;
    public SubmissionAdapter adapter;
    public String id;
    public boolean main;
    public boolean forced;
    int diff;
    boolean forceLoad;
    private FloatingActionButton fab;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private int totalItemCount;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        int i = 0;
        if (rv.getAdapter() != null) {
            int[] firstVisibleItems;

            firstVisibleItems = ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(null);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                i = firstVisibleItems[0];
            }
        }

        final RecyclerView.LayoutManager mLayoutManager = createLayoutManager(newConfig.orientation);
        rv.setLayoutManager(mLayoutManager);
        rv.getLayoutManager().scrollToPosition(i);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), new ColorPreferences(inflater.getContext()).getThemeSubreddit(id));
        View v = ((LayoutInflater) contextThemeWrapper.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.fragment_verticalcontent, container, false);

        if (getActivity() instanceof MainActivity) {
            v.findViewById(R.id.back).setBackgroundResource(0);
        }
        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));

        rv.setHasFixedSize(true);

        final RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = createLayoutManager(getResources().getConfiguration().orientation);

        if (!(getActivity() instanceof SubredditView)) {
            v.findViewById(R.id.back).setBackground(null);
        }
        rv.setLayoutManager(mLayoutManager);
        rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));
        rv.getLayoutManager().scrollToPosition(0);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(Palette.getColors(id, getContext()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we just do 13% of the device screen height as a general estimate for the Tabs view type
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int headerOffset = Math.round((float) (screenHeight * 0.13));

        //if the view type is "single" (and therefore "commentPager"), we need a different offset
        if (SettingValues.single || getActivity() instanceof SubredditView) {
            headerOffset = Math.round((float) (screenHeight * 0.07));
        }

        mSwipeRefreshLayout.setProgressViewOffset(false,
                headerOffset - Reddit.pxToDp(42, getContext()),
                headerOffset + Reddit.pxToDp(42, getContext()));

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
                                            Reddit.colors.edit().putBoolean(SettingValues.PREF_FAB_CLEAR, true).apply();
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
                                            Reddit.colors.edit().putBoolean(SettingValues.PREF_FAB_CLEAR, true).apply();
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
                        Snackbar s = Snackbar.make(rv, getResources().getString(R.string.posts_hidden_forever), Snackbar.LENGTH_LONG);
                        View view = s.getView();
                        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();

                        return false;
                    }
                });
            }
        } else {
            v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);
        }
        if (fab != null)
            fab.show();

        rv.addOnScrollListener(new ToolbarScrollHideHandler(((BaseActivity) getActivity()).mToolbar, getActivity().findViewById(R.id.header)) {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!posts.loading && !posts.nomore && !posts.offline) {

                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();

                    int[] firstVisibleItems;
                    firstVisibleItems = ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(null);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        for (int firstVisibleItem : firstVisibleItems) {
                            pastVisiblesItems = firstVisibleItem;
                            if (SettingValues.scrollSeen) {
                                if (pastVisiblesItems > 0) {
                                    HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                                }
                            }
                        }
                    }

                    if ((visibleItemCount + pastVisiblesItems) + 5 >= totalItemCount) {
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

                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
                    diff += dy;
                } else {
                    diff = 0;
                }
                if (fab != null) {
                    if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {
                        if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_DRAGGING || diff < -fab.getHeight() * 2)
                            fab.show();
                    } else {
                        fab.hide();
                    }
                }

            }

            /*
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        ((Reddit)getActivity().getApplicationContext()).getImageLoader().resume();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        ((Reddit)getActivity().getApplicationContext()).getImageLoader().resume();

                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        ((Reddit)getActivity().getApplicationContext()).getImageLoader().pause();

                        break;
                }
                super.onScrollStateChanged(recyclerView, newState);

            }*/
        });


        Reddit.isLoading = false;
        if (MainActivity.shouldLoad == null || id == null || (MainActivity.shouldLoad != null && MainActivity.shouldLoad.equals(id)) || !(getActivity() instanceof MainActivity)) {
            doAdapter();
        }
        return v;
    }

    @NonNull
    private RecyclerView.LayoutManager createLayoutManager(int orientation) {
        final int numColumns;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            numColumns = Reddit.dpWidth;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            numColumns = 2;
        } else {
            numColumns = 1;
        }

        return new CatchStaggeredGridLayoutManager(numColumns, CatchStaggeredGridLayoutManager.VERTICAL);

    }

    public void doAdapter() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        posts = new SubredditPosts(id, getContext());
        adapter = new SubmissionAdapter(getActivity(), posts, rv, id, this);
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

    public void doAdapter(boolean force18) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        posts = new SubredditPosts(id, getContext(), force18);
        adapter = new SubmissionAdapter(getActivity(), posts, rv, id, this);
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

            List<Submission> originalDataSetPosts = adapter.dataSet.posts;

            OfflineSubreddit o = OfflineSubreddit.getSubreddit(id.toLowerCase(), false, getActivity());
            for (int i = adapter.dataSet.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(adapter.dataSet.posts.get(i))) {
                        if (forever) {
                            Hidden.setHidden(adapter.dataSet.posts.get(i));
                        }
                        o.clearPost(adapter.dataSet.posts.get(i));

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
            o.writeToMemory(getActivity());
            rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));
            return originalDataSetPosts;
        }

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        id = bundle.getString("id", "");
        main = bundle.getBoolean("main", false);
        forceLoad = bundle.getBoolean("load", false);

    }

    private void refresh() {
        posts.forced = true;
        forced = true;
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
            if (getActivity() instanceof MainActivity) {
                if (((MainActivity) getActivity()).runAfterLoad != null) {
                    new Handler().post(((MainActivity) getActivity()).runAfterLoad);
                }
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    if (startIndex != -1 && !forced) {
                        adapter.notifyItemRangeInserted(startIndex + 1, posts.posts.size());
                    } else {
                        forced = false;
                        adapter.notifyDataSetChanged();
                    }

                }
            });

        }
    }

    @Override
    public void updateOffline(List<Submission> submissions, final long cacheTime) {
        if (getActivity() instanceof MainActivity) {
            if (((MainActivity) getActivity()).runAfterLoad != null) {
                new Handler().post(((MainActivity) getActivity()).runAfterLoad);
            }
        }
        if (this.isAdded()) {
            if (mSwipeRefreshLayout != null) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateOfflineError() {
        if (getActivity() instanceof MainActivity) {
            if (((MainActivity) getActivity()).runAfterLoad != null) {
                new Handler().post(((MainActivity) getActivity()).runAfterLoad);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }

    @Override
    public void updateError() {
        if (getActivity() instanceof MainActivity) {
            if (((MainActivity) getActivity()).runAfterLoad != null) {
                new Handler().post(((MainActivity) getActivity()).runAfterLoad);
            }
        }
        mSwipeRefreshLayout.setRefreshing(false);
        adapter.setError(true);
    }
}