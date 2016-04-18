package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Adapters.MultiredditAdapter;
import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

;

public class MultiredditView extends Fragment implements SubmissionDisplay {

    private MultiredditAdapter adapter;
    public MultiredditPosts posts;
    private SwipeRefreshLayout refreshLayout;
    private int id;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    public RecyclerView rv;
    public FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        rv = ((RecyclerView) v.findViewById(R.id.vertical_content));
        final RecyclerView.LayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
            mLayoutManager = new CatchStaggeredGridLayoutManager(Reddit.dpWidth, CatchStaggeredGridLayoutManager.VERTICAL);
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            mLayoutManager = new CatchStaggeredGridLayoutManager(2, CatchStaggeredGridLayoutManager.VERTICAL);
        } else {
            mLayoutManager = new PreCachingLayoutManager(getActivity());
        }
        rv.setLayoutManager(mLayoutManager);
        if (SettingValues.fab) {
            fab = (FloatingActionButton) v.findViewById(R.id.post_floating_action_button);

            if (SettingValues.fabType == R.integer.FAB_POST) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ArrayList<String> subs = new ArrayList<>();
                        for (MultiSubreddit s : posts.multiReddit.getSubreddits()) {
                            subs.add(s.getDisplayName());
                        }
                        new MaterialDialog.Builder(getActivity())
                                .title("Which sub would you like to submit to?")
                                .items(subs)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        Intent i = new Intent(getActivity(), Submit.class);
                                        i.putExtra(Submit.EXTRA_SUBREDDIT, subs.get(which));
                                        startActivity(i);
                                    }
                                }).show();
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
        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.activity_main_swipe_refresh_layout);

        if (UserSubscriptions.getMultireddits() != null && !UserSubscriptions.getMultireddits().isEmpty())
            refreshLayout.setColorSchemeColors(Palette.getColors(UserSubscriptions.getMultireddits().get(id).getDisplayName(), getActivity()));

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we just do 13% of the device screen height as a general estimate for the Tabs view type
        int screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        int headerOffset = Math.round((float) (screenHeight * 0.13));

        refreshLayout.setProgressViewOffset(false,
                headerOffset - Reddit.pxToDp(42, getContext()),
                headerOffset + Reddit.pxToDp(42, getContext()));

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });
        if (UserSubscriptions.getMultireddits() != null && !UserSubscriptions.getMultireddits().isEmpty()) {
            posts = new MultiredditPosts(UserSubscriptions.getMultireddits().get(id).getDisplayName());

            adapter = new MultiredditAdapter(getActivity(), posts, rv, refreshLayout, this);
            rv.setAdapter(adapter);
            rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));
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

            rv.addOnScrollListener(new ToolbarScrollHideHandler((Toolbar) (getActivity()).findViewById(R.id.toolbar), getActivity().findViewById(R.id.header)) {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

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
                        firstVisibleItems = ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                            for (int i = 0; i < firstVisibleItems.length; i++) {
                                pastVisiblesItems = firstVisibleItems[i];
                                if (SettingValues.scrollSeen) {
                                    if (pastVisiblesItems > 0) {
                                        HasSeen.addSeen(posts.posts.get(pastVisiblesItems - 1).getFullName());
                                    }
                                }
                            }
                        }
                    }

                    if (!posts.loading) {
                        if ((visibleItemCount + pastVisiblesItems) + 5 >= totalItemCount && !posts.nomore) {
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
        }
        return v;
    }

    private List<Submission> clearSeenPosts(boolean forever) {
        if (posts.posts != null) {

            List<Submission> originalDataSetPosts = posts.posts;

            OfflineSubreddit o = OfflineSubreddit.getSubreddit("multi" + posts.multiReddit.getDisplayName().toLowerCase(), false);
            for (int i = posts.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(posts.posts.get(i))) {
                        if (forever) {
                            Hidden.setHidden(posts.posts.get(i));
                        }
                        o.clearPost(posts.posts.get(i));

                        posts.posts.remove(i);
                        if (posts.posts.size() == 0) {
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
            o.writeToMemory();
            rv.setItemAnimator(new SlideInUpAnimator(new AccelerateDecelerateInterpolator()));
            return originalDataSetPosts;
        }

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getInt("id", 0);
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
                mLayoutManager = new CatchStaggeredGridLayoutManager(Reddit.dpWidth, CatchStaggeredGridLayoutManager.VERTICAL);
            } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new CatchStaggeredGridLayoutManager(2, CatchStaggeredGridLayoutManager.VERTICAL);
            } else {
                mLayoutManager = new PreCachingLayoutManager(getContext());
            }

            rv.setLayoutManager(mLayoutManager);


        } else {
            final RecyclerView.LayoutManager mLayoutManager;

            if (rv.getAdapter() != null) {
                if (rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
                    int[] firstVisibleItems = null;
                    firstVisibleItems = ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        i = firstVisibleItems[0];
                    }
                } else {
                    i = ((PreCachingLayoutManager) rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                }
            }
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.tabletUI) {
                mLayoutManager = new CatchStaggeredGridLayoutManager(Reddit.dpWidth, CatchStaggeredGridLayoutManager.VERTICAL);
            } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
                mLayoutManager = new CatchStaggeredGridLayoutManager(2, CatchStaggeredGridLayoutManager.VERTICAL);
            } else {
                mLayoutManager = new PreCachingLayoutManager(getContext());
            }
            rv.setLayoutManager(mLayoutManager);

        }
        rv.getLayoutManager().scrollToPosition(i);

    }

    @Override
    public void updateSuccess(List<Submission> submissions, final int startIndex) {
        adapter.context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);

                if (startIndex != -1) {
                    adapter.notifyItemRangeInserted(startIndex + 1, posts.posts.size());
                } else {
                    adapter.notifyDataSetChanged();
                }
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