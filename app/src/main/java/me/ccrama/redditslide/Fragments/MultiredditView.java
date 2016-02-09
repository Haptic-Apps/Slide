package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.Submission;

import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import me.ccrama.redditslide.Adapters.MultiredditAdapter;
import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SubtleSlideInUp;
import me.ccrama.redditslide.Visuals.Palette;

public class MultiredditView extends Fragment implements SubmissionDisplay {

    private MultiredditAdapter adapter;
    private MultiredditPosts posts;
    private SwipeRefreshLayout refreshLayout;
    private int id;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    RecyclerView rv;
    public FloatingActionButton fab;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final RecyclerView.LayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            mLayoutManager = new StaggeredGridLayoutManager(Reddit.dpWidth, StaggeredGridLayoutManager.VERTICAL);
        } else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait){
            mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new PreCachingLayoutManager(getActivity());

        }
        rv.setLayoutManager(mLayoutManager);
        rv.setItemViewCacheSize(2);

        if (SettingValues.fab) {
            fab = (FloatingActionButton) v.findViewById(R.id.post_floating_action_button);

            if (SettingValues.fabType == R.integer.FAB_POST) {
              fab.setVisibility(View.GONE);
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
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);
        TypedValue typed_value = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        refreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));

        refreshLayout.setColorSchemeColors(Palette.getColors(SubredditStorage.multireddits.get(id).getDisplayName(), getActivity()));

        refreshLayout.setRefreshing(true);
        posts = new MultiredditPosts(SubredditStorage.multireddits.get(id).getDisplayName());
        adapter = new MultiredditAdapter(getActivity(), posts, rv, refreshLayout);
        rv.setAdapter(adapter);
        if(SettingValues.animation)
            rv.setItemAnimator(new SubtleSlideInUp(getContext()));
        posts.loadMore(getActivity(), this, true, adapter);

        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        posts.loadMore(getActivity(), MultiredditView.this, true, adapter);

                        //TODO catch errors
                    }
                }
        );

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = rv.getLayoutManager().getChildCount();
                totalItemCount = rv.getLayoutManager().getItemCount();
                if (rv.getLayoutManager() instanceof PreCachingLayoutManager) {
                    pastVisiblesItems = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPosition();
                } else {
                    int[] firstVisibleItems = null;
                    firstVisibleItems = ((StaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        pastVisiblesItems = firstVisibleItems[0];
                    }
                }

                if (!posts.loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount && !posts.nomore) {
                        posts.loading = true;
                        posts.loadMore(getActivity(), MultiredditView.this, false, adapter);
                    }
                }
                if (fab != null) {
                    if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {

                        fab.show();


                    } else {
                        fab.hide();

                    }
                }
            }
        });
        return v;
    }
    private List<Submission> clearSeenPosts(boolean forever) {
        if (posts.posts != null) {
            List<Submission> originalDataSetPosts = posts.posts;

            for (int i = posts.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(posts.posts.get(i).getFullName())) {
                        if (forever) {
                            Hidden.setHidden(posts.posts.get(i));
                        }
                        posts.posts.remove(i);
                        if (posts.posts.size() == 0) {
                            adapter.notifyDataSetChanged();
                        } else {
                            if (SettingValues.animation)

                                rv.setItemAnimator(new FadeInAnimator());

                            adapter.notifyItemRemoved(i);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Let the loop reset itself
                }
            }
            if (SettingValues.animation)
                rv.setItemAnimator(new SubtleSlideInUp(getContext()));
            return originalDataSetPosts;
        }

        new OfflineSubreddit("multi"  + posts.getMultiReddit().getDisplayName()).clearSeenPosts(false);
        return null;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getInt("id", 0);
    }


    @Override
    public void updateSuccess(List<Submission> submissions, int startIndex) {
        adapter.mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateOffline(List<Submission> submissions, long cacheTime) {
        adapter.setError(true);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void updateOfflineError() {

    }

    @Override
    public void updateError() {

    }
}