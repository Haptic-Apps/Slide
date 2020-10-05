package me.ccrama.redditslide.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mikepenz.itemanimators.AlphaInAnimator;
import com.mikepenz.itemanimators.SlideUpAlphaAnimator;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Submission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.Search;
import me.ccrama.redditslide.Activities.Submit;
import me.ccrama.redditslide.Adapters.MultiredditAdapter;
import me.ccrama.redditslide.Adapters.MultiredditPosts;
import me.ccrama.redditslide.Adapters.SubmissionDisplay;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.HasSeen;
import me.ccrama.redditslide.Hidden;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.handler.ToolbarScrollHideHandler;

public class MultiredditView extends Fragment implements SubmissionDisplay {

    private static final String EXTRA_PROFILE = "profile";

    public MultiredditAdapter adapter;
    public MultiredditPosts posts;
    public RecyclerView rv;
    public FloatingActionButton fab;
    public int diff;
    private SwipeRefreshLayout refreshLayout;
    private int id;
    private int totalItemCount;
    private int visibleItemCount;
    private int pastVisiblesItems;
    private String profile;

    @NonNull
    private RecyclerView.LayoutManager createLayoutManager(final int numColumns) {
        return new CatchStaggeredGridLayoutManager(numColumns, CatchStaggeredGridLayoutManager.VERTICAL);
    }

    private int getNumColumns(final int orientation) {
        final int numColumns;
        boolean singleColumnMultiWindow = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            singleColumnMultiWindow = getActivity().isInMultiWindowMode() && SettingValues.singleColumnMultiWindow;
        }
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && SettingValues.isPro && !singleColumnMultiWindow) {
            numColumns = Reddit.dpWidth;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT && SettingValues.dualPortrait) {
            numColumns = 2;
        } else {
            numColumns = 1;
        }
        return numColumns;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_verticalcontent, container, false);

        rv = v.findViewById(R.id.vertical_content);
        final RecyclerView.LayoutManager mLayoutManager =
                createLayoutManager(getNumColumns(getResources().getConfiguration().orientation));

        rv.setLayoutManager(mLayoutManager);
        if (SettingValues.fab) {
            fab = v.findViewById(R.id.post_floating_action_button);

            if (SettingValues.fabType == Constants.FAB_POST) {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final ArrayList<String> subs = new ArrayList<>();
                        for (MultiSubreddit s : posts.multiReddit.getSubreddits()) {
                            subs.add(s.getDisplayName());
                        }
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.multi_submit_which_sub)
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
            } else if (SettingValues.fabType == Constants.FAB_SEARCH) {
                fab.setImageResource(R.drawable.search);
                fab.setOnClickListener(new View.OnClickListener() {
                    String term;
                    @Override
                    public void onClick(View v) {
                        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                                .title(R.string.search_title)
                                .alwaysCallInputCallback()
                                .input(getString(R.string.search_msg), "",
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(
                                                    MaterialDialog materialDialog,
                                                    CharSequence charSequence) {
                                                term = charSequence.toString();
                                            }
                                        });

                        builder.positiveText(getString(R.string.search_subreddit,
                                "/m/" + posts.multiReddit.getDisplayName()))
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog materialDialog,
                                                        @NonNull DialogAction dialogAction) {
                                        Intent i = new Intent(getActivity(), Search.class);
                                        i.putExtra(Search.EXTRA_TERM, term);
                                        i.putExtra(Search.EXTRA_MULTIREDDIT, posts.multiReddit.getDisplayName());
                                        startActivity(i);
                                    }
                                });

                        builder.show();
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
                        TextView tv = view.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        s.show();

                        return false;
                    }
                });
            }
        } else {
            v.findViewById(R.id.post_floating_action_button).setVisibility(View.GONE);
        }
        refreshLayout = v.findViewById(R.id.activity_main_swipe_refresh_layout);

        /**
         * If using List view mode, we need to remove the start margin from the SwipeRefreshLayout.
         * The scrollbar style of "outsideInset" creates a 4dp padding around it. To counter this,
         * change the scrollbar style to "insideOverlay" when list view is enabled.
         * To recap: this removes the margins from the start/end so list view is full-width.
         */
        if (SettingValues.defaultCardView == CreateCardView.CardEnum.LIST) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            MarginLayoutParamsCompat.setMarginStart(params, 0);
            rv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            refreshLayout.setLayoutParams(params);
        }

        List<MultiReddit> multireddits;
        if (profile.isEmpty()) {
            multireddits = UserSubscriptions.multireddits;
        } else {
            multireddits = UserSubscriptions.public_multireddits.get(profile);
        }

        if ((multireddits != null) && !multireddits.isEmpty()) {
            refreshLayout.setColorSchemeColors(Palette.getColors(multireddits.get(id).getDisplayName(), getActivity()));
        }

        //If we use 'findViewById(R.id.header).getMeasuredHeight()', 0 is always returned.
        //So, we estimate the height of the header in dp
        refreshLayout.setProgressViewOffset(false,
                Constants.TAB_HEADER_VIEW_OFFSET - Constants.PTR_OFFSET_TOP,
                Constants.TAB_HEADER_VIEW_OFFSET + Constants.PTR_OFFSET_BOTTOM);

        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(true);
            }
        });

        if ((multireddits != null) && !multireddits.isEmpty()) {
            posts = new MultiredditPosts(multireddits.get(id).getDisplayName(), profile);

            adapter = new MultiredditAdapter(getActivity(), posts, rv, refreshLayout, this);
            rv.setAdapter(adapter);
            rv.setItemAnimator(new SlideUpAlphaAnimator().withInterpolator(new LinearOutSlowInInterpolator()));
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

            if (fab != null) {
                fab.show();
            }

            rv.addOnScrollListener(new ToolbarScrollHideHandler((getActivity()).findViewById(R.id.toolbar), getActivity().findViewById(R.id.header)) {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    visibleItemCount = rv.getLayoutManager().getChildCount();
                    totalItemCount = rv.getLayoutManager().getItemCount();

                    int[] firstVisibleItems = ((CatchStaggeredGridLayoutManager) rv.getLayoutManager()).findFirstVisibleItemPositions(null);
                    if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                        for (int firstVisibleItem : firstVisibleItems) {
                            pastVisiblesItems = firstVisibleItem;
                            if (SettingValues.scrollSeen && pastVisiblesItems > 0 && SettingValues.storeHistory) {
                                HasSeen.addSeenScrolling(posts.posts.get(pastVisiblesItems - 1).getFullName());
                            }
                        }
                    }


                    if (!posts.loading) {
                        if ((visibleItemCount + pastVisiblesItems) + 5 >= totalItemCount && !posts.nomore) {
                            posts.loading = true;
                            posts.loadMore(getActivity(), MultiredditView.this, false, adapter);
                        }
                    }
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
            });
        }
        return v;
    }

    private List<Submission> clearSeenPosts(boolean forever) {
        if (posts.posts != null) {

            List<Submission> originalDataSetPosts = posts.posts;

            OfflineSubreddit o = OfflineSubreddit.getSubreddit("multi" + posts.multiReddit.getDisplayName().toLowerCase(
                    Locale.ENGLISH), false, getActivity());
            for (int i = posts.posts.size(); i > -1; i--) {
                try {
                    if (HasSeen.getSeen(posts.posts.get(i))) {
                        if (forever) {
                            Hidden.setHidden(posts.posts.get(i));
                        }
                        o.clearPost(posts.posts.get(i));
                        posts.posts.remove(i);
                        if (posts.posts.isEmpty()) {
                            adapter.notifyDataSetChanged();
                        } else {
                            rv.setItemAnimator(new AlphaInAnimator());
                            adapter.notifyItemRemoved(i + 1);
                        }
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Let the loop reset itself
                }
            }
            o.writeToMemoryNoStorage();
            rv.setItemAnimator(new SlideUpAlphaAnimator().withInterpolator(new LinearOutSlowInInterpolator()));
            return originalDataSetPosts;
        }

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        id = bundle.getInt("id", 0);
        profile = bundle.getString(EXTRA_PROFILE, "");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        final int currentOrientation = newConfig.orientation;

        final CatchStaggeredGridLayoutManager mLayoutManager =
                (CatchStaggeredGridLayoutManager) rv.getLayoutManager();

        mLayoutManager.setSpanCount(getNumColumns(currentOrientation));
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

    @Override
    public void updateViews() {
        try {
            adapter.notifyItemRangeChanged(0, adapter.dataSet.getPosts().size());
        } catch(Exception e){

        }
    }

    @Override
    public void onAdapterUpdated() {
        adapter.notifyDataSetChanged();
    }
}
