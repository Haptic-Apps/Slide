package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fasterxml.jackson.databind.JsonNode;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.FlairTemplate;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.UserRecord;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;
import net.dean.jraw.paginators.UserRecordPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.SettingsSubAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.BlankFragment;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CatchStaggeredGridLayoutManager;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SubmissionParser;

public class SubredditView extends BaseActivityAnim {

    public static final String EXTRA_SUBREDDIT = "subreddit";
    public boolean canSubmit = true;
    private DrawerLayout drawerLayout;
    public String subreddit;
    public Submission openingComments;
    public int currentComment;

    public void restartTheme() {
        Intent intent = this.getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public OverviewPagerAdapter adapter;

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START) || drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers();
        } else if (commentPager && pager.getCurrentItem() == 2) {
            pager.setCurrentItem(pager.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (SettingValues.expandedToolbar) {
            inflater.inflate(R.menu.menu_single_subreddit_expanded, menu);
        } else {
            inflater.inflate(R.menu.menu_single_subreddit, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.filter:
                filterContent(subreddit);
                return true;
            case R.id.submit:
                Intent i = new Intent(this, Submit.class);
                if (canSubmit)
                    i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                startActivity(i);
                return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null)
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                return true;
            case R.id.action_sort:
                if (subreddit.equalsIgnoreCase("friends")) {
                    Snackbar s = Snackbar.make(findViewById(R.id.anchor), "Cannot sort /r/friends", Snackbar.LENGTH_SHORT);
                    View view = s.getView();
                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                    tv.setTextColor(Color.WHITE);
                    s.show();
                } else {
                    openPopup();
                }
                return true;
            case R.id.gallery:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Gallery.class);
                        i2.putExtra("offline", ((SubmissionsView) adapter.getCurrentFragment()).posts.cached != null ? ((SubmissionsView) adapter.getCurrentFragment()).posts.cached.time : 0L);
                        i2.putExtra(Gallery.EXTRA_SUBREDDIT, ((SubmissionsView) adapter.getCurrentFragment()).posts.subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this)
                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            case R.id.search:
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this).title(R.string.search_title)
                        .alwaysCallInputCallback()
                        .input(getString(R.string.search_msg), "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                term = charSequence.toString();
                            }
                        })
                        .neutralText(R.string.search_all)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                Intent i = new Intent(SubredditView.this, Search.class);
                                i.putExtra(Search.EXTRA_TERM, term);
                                startActivity(i);
                            }
                        });

                //Add "search current sub" if it is not frontpage/all/random
                if (!subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("random") && !subreddit.equals("myrandom") && !subreddit.equals("nsfwrandom") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod")) {
                    builder.positiveText(getString(R.string.search_subreddit, subreddit))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    Intent i = new Intent(SubredditView.this, Search.class);
                                    i.putExtra(Search.EXTRA_TERM, term);
                                    i.putExtra(Search.EXTRA_SUBREDDIT, subreddit);
                                    Log.v(LogUtil.getTag(), "INTENT SHOWS " + term + " AND " + subreddit);
                                    startActivity(i);
                                }
                            });
                }
                builder.show();
                return true;
            case R.id.sidebar:
                drawerLayout.openDrawer(Gravity.RIGHT);
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) ((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i2.putExtra(Shadowbox.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i2);
                    }
                } else {
                    new AlertDialogWrapper.Builder(this)
                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (ActivityNotFoundException e) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    }).show();
                }
                return true;
            default:
                return false;
        }
    }

    private void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getDataNode().has("subreddit_type") && !subreddit.getDataNode().get("subreddit_type").isNull())
            canSubmit = !subreddit.getDataNode().get("subreddit_type").asText().toUpperCase().equals("RESTRICTED");
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final SpoilerRobotoTextView body = (SpoilerRobotoTextView) findViewById(R.id.sidebar_text);
            CommentOverflow overflow = (CommentOverflow) findViewById(R.id.commentOverflow);
            setViews(text, subreddit.getDisplayName(), body, overflow);
        } else {
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
        }
        {
            final CheckBox c = ((CheckBox) findViewById(R.id.subscribed));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //reset check adapter
                }
            });
            c.setChecked(UserSubscriptions.isSubscriber(subreddit.getDisplayName().toLowerCase(), this));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        public void onPostExecute(Boolean success) {
                            if (!success) { // If subreddit was removed from account or not

                                new AlertDialogWrapper.Builder(SubredditView.this).setTitle(R.string.force_change_subscription)
                                        .setMessage(R.string.force_change_subscription_desc)
                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                changeSubscription(subreddit, isChecked); // Force remove the subscription
                                            }
                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).setCancelable(false)
                                        .show();
                            } else {
                                changeSubscription(subreddit, isChecked);
                            }

                        }


                        @Override
                        protected Boolean doInBackground(Void... params) {
                            try {
                                if (isChecked) {
                                    new AccountManager(Authentication.reddit).subscribe(subreddit);
                                } else {
                                    new AccountManager(Authentication.reddit).unsubscribe(subreddit);
                                }

                            } catch (NetworkException e) {
                                return false; // Either network crashed or trying to unsubscribe to a subreddit that the account isn't subscribed to
                            }
                            return true;

                        }
                    }.execute();

                }
            });
        }
        if (!subreddit.getPublicDescription().isEmpty()) {
            findViewById(R.id.sub_title).setVisibility(View.VISIBLE);
            setViews(subreddit.getDataNode().get("public_description_html").asText(), subreddit.getDisplayName().toLowerCase(), ((SpoilerRobotoTextView) findViewById(R.id.sub_title)), (CommentOverflow) findViewById(R.id.sub_title_overflow));
        } else {
            findViewById(R.id.sub_title).setVisibility(View.GONE);
        }
        if (subreddit.getDataNode().has("icon_img") && !subreddit.getDataNode().get("icon_img").asText().isEmpty()) {
            ((Reddit) getApplication()).getImageLoader().displayImage(subreddit.getDataNode().get("icon_img").asText(), (ImageView) findViewById(R.id.subimage));
        } else {
            findViewById(R.id.subimage).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers_string, subreddit.getLocalizedSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.active_users)).setText(getString(R.string.subreddit_active_users_string, subreddit.getAccountsActive()));
        findViewById(R.id.active_users).setVisibility(View.VISIBLE);
    }

    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position = ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition() - 1;
        } else if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof CatchStaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((CatchStaggeredGridLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position = ((PreCachingLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstCompletelyVisibleItemPosition() - 1;
        }
        return position;
    }

    public String term;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sub != null) {
            if (sub.isNsfw() && (!SettingValues.storeHistory || !SettingValues.storeNSFWHistory)) {
                SharedPreferences.Editor e = Reddit.cachedData.edit();
                for (String s : OfflineSubreddit.getAll(sub.getDisplayName())) {
                    e.remove(s);
                }
                e.apply();
            } else if (!SettingValues.storeHistory) {
                SharedPreferences.Editor e = Reddit.cachedData.edit();
                for (String s : OfflineSubreddit.getAll(sub.getDisplayName())) {
                    e.remove(s);
                }
                e.apply();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        } else if (requestCode == 1) {
            restartTheme();
        }
    }

    public ToggleSwipeViewPager pager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        if (SettingValues.commentPager && SettingValues.single) {
            disableSwipeBackLayout();
        }
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        header = findViewById(R.id.header);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setResult(3);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);
        singleMode = SettingValues.single;
        commentPager = false;
        if (singleMode)
            commentPager = SettingValues.commentPager;
        if (commentPager) {
            adapter = new OverviewPagerAdapterComment(getSupportFragmentManager());
            pager.setSwipeLeftOnly(false);
            pager.setSwipingEnabled(true);
        } else {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        }
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] firstVisibleItems;
                int pastVisiblesItems = 0;
                firstVisibleItems = ((CatchStaggeredGridLayoutManager) ((SubmissionsView) (adapter.getCurrentFragment())).rv.getLayoutManager()).findFirstVisibleItemPositions(null);
                if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                    for (int firstVisibleItem : firstVisibleItems) {
                        pastVisiblesItems = firstVisibleItem;
                    }
                }
                if (pastVisiblesItems > 8) {
                    ((SubmissionsView) (adapter.getCurrentFragment())).rv.scrollToPosition(0);
                    header.animate()
                            .translationY(header.getHeight())
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);
                } else {
                    ((SubmissionsView) (adapter.getCurrentFragment())).rv.smoothScrollToPosition(0);
                }
                ((SubmissionsView) (adapter.getCurrentFragment())).resetScroll();
            }
        });
        if (!subreddit.equals("random") && !subreddit.equals("all")
                && !subreddit.equals("frontpage") && !subreddit.equals("friends")
                && !subreddit.equals("mod") && !subreddit.equals("myrandom")
                && !subreddit.equals("nsfwrandom") && !subreddit.contains("+"))
            executeAsyncSubreddit(subreddit);
    }

    public boolean singleMode;
    public boolean commentPager;
    public boolean loaded;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //Hide the "Submit" menu item if the currently viewed sub is the frontpage or /r/all.
        if (subreddit.equals("frontpage") || subreddit.equals("all")) {
            menu.findItem(R.id.submit).setVisible(false);
            menu.findItem(R.id.sidebar).setVisible(false);
        }

        mToolbar.getMenu().findItem(R.id.theme).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int style = new ColorPreferences(SubredditView.this).getThemeSubreddit(subreddit);
                final Context contextThemeWrapper = new ContextThemeWrapper(SubredditView.this, style);
                LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(subreddit);
                SettingsSubAdapter.showSubThemeEditor(arrayList, SubredditView.this, dialoglayout);
                return false;
            }
        });
        return true;
    }


    public void filterContent(final String subreddit) {
        final boolean[] chosen = new boolean[]{
                PostMatch.isImage(subreddit.toLowerCase()),
                PostMatch.isAlbums(subreddit.toLowerCase()),
                PostMatch.isGif(subreddit.toLowerCase()),
                PostMatch.isVideo(subreddit.toLowerCase()),
                PostMatch.isUrls(subreddit.toLowerCase()),
                PostMatch.isSelftext(subreddit.toLowerCase()),
                PostMatch.isNsfw(subreddit.toLowerCase())
        };

        final String FILTER_TITLE = (subreddit.equals("frontpage")) ? (getString(R.string.content_to_hide, "frontpage"))
                : (getString(R.string.content_to_hide, "/r/" + subreddit));

        new AlertDialogWrapper.Builder(this)
                .setTitle(FILTER_TITLE)
                .alwaysCallMultiChoiceCallback()
                .setMultiChoiceItems(new String[]{
                        getString(R.string.image_downloads),
                        getString(R.string.type_albums),
                        getString(R.string.type_gifs),
                        getString(R.string.type_videos),
                        getString(R.string.type_links),
                        getString(R.string.type_selftext),
                        getString(R.string.type_nsfw_content)}, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PostMatch.setChosen(chosen, subreddit);
                reloadSubs();
            }
        }).setNegativeButton("Cancel", null).show();


    }

    public void openPopup() {
        PopupMenu popup = new PopupMenu(SubredditView.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final String[] base = Reddit.getSortingStrings(getBaseContext(), subreddit, true);
        for (String s : base) {
            MenuItem m = popup.getMenu().add(s);
            if (s.startsWith("» ")) {
                SpannableString spanString = new SpannableString(s.replace("» ", ""));
                spanString.setSpan(new ForegroundColorSpan(new ColorPreferences(SubredditView.this).getColor(subreddit)), 0, spanString.length(), 0);
                spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                m.setTitle(spanString);
            }
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (String s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        Reddit.setSorting(subreddit, Sorting.HOT);
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.setSorting(subreddit, Sorting.NEW);
                        reloadSubs();
                        break;
                    case 2:
                        Reddit.setSorting(subreddit, Sorting.RISING);
                        reloadSubs();
                        break;
                    case 3:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 4:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 5:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.WEEK);
                        reloadSubs();
                        break;
                    case 6:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.MONTH);
                        reloadSubs();
                        break;
                    case 7:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.YEAR);
                        reloadSubs();
                        break;
                    case 8:
                        Reddit.setSorting(subreddit, Sorting.TOP);
                        Reddit.setTime(subreddit, TimePeriod.ALL);
                        reloadSubs();
                        break;
                    case 9:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.HOUR);
                        reloadSubs();
                        break;
                    case 10:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.DAY);
                        reloadSubs();
                        break;
                    case 11:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.WEEK);
                        reloadSubs();
                    case 12:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.MONTH);
                        reloadSubs();
                    case 13:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.YEAR);
                        reloadSubs();
                    case 14:
                        Reddit.setSorting(subreddit, Sorting.CONTROVERSIAL);
                        Reddit.setTime(subreddit, TimePeriod.ALL);
                        reloadSubs();

                }
                return true;
            }
        });
        popup.show();
    }

    private void reloadSubs() {
        restartTheme();
    }

    private void setViews(String rawHTML, String subreddit, SpoilerRobotoTextView firstTextView, CommentOverflow commentOverflow) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            firstTextView.setVisibility(View.VISIBLE);
            firstTextView.setTextHtml(blocks.get(0), subreddit);
            startIndex = 1;
        } else {
            firstTextView.setText("");
            firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                commentOverflow.setViews(blocks, subreddit);
            } else {
                commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subreddit);
            }
            SidebarLayout sidebar = (SidebarLayout) findViewById(R.id.drawer_layout);
            for (int i = 0; i < commentOverflow.getChildCount(); i++) {
                View maybeScrollable = commentOverflow.getChildAt(i);
                if (maybeScrollable instanceof HorizontalScrollView) {
                    sidebar.addScrollable(maybeScrollable);
                }
            }
        } else {
            commentOverflow.removeAllViews();
        }
    }

    private void changeSubscription(Subreddit subreddit, boolean isChecked) {
        if (isChecked) {
            UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(), SubredditView.this);
        } else {
            UserSubscriptions.removeSubreddit(subreddit.getDisplayName().toLowerCase(), SubredditView.this);
            pager.setCurrentItem(pager.getCurrentItem() - 1);
            restartTheme();
        }
        Snackbar s = Snackbar.make(mToolbar, isChecked ?
                getString(R.string.misc_subscribed) : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
        View view = s.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        s.show();
    }


    public void doSubSidebarNoLoad(final String subOverride) {
        findViewById(R.id.loader).setVisibility(View.GONE);

        invalidateOptionsMenu();

        if (!subOverride.equalsIgnoreCase("all") && !subOverride.equalsIgnoreCase("frontpage") &&
                !subOverride.equalsIgnoreCase("friends") && !subOverride.equalsIgnoreCase("mod") &&
                !subOverride.contains("+") && !subOverride.contains(".") && !subOverride.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);
            findViewById(R.id.active_users).setVisibility(View.GONE);

            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subOverride));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subOverride);

            //Sidebar buttons should use subOverride's accent color
            int subColor = new ColorPreferences(this).getColor(subOverride);
            ((TextView) findViewById(R.id.theme_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.wiki_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.post_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.mods_text)).setTextColor(subColor);
            ((TextView) findViewById(R.id.flair_text)).setTextColor(subColor);

        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        }
    }

    public void doSubSidebar(final String subOverride) {
        findViewById(R.id.loader).setVisibility(View.VISIBLE);

        invalidateOptionsMenu();

        if (!subOverride.equalsIgnoreCase("all") && !subOverride.equalsIgnoreCase("frontpage") && !subOverride.equalsIgnoreCase("random") && !subOverride.equalsIgnoreCase("myrandom") && !subOverride.equalsIgnoreCase("nsfwrandom") &&
                !subOverride.equalsIgnoreCase("friends") && !subOverride.equalsIgnoreCase("mod") &&
                !subOverride.contains("+") && !subOverride.contains(".") && !subOverride.contains("/m/")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }
            loaded = true;

            final View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox pinned = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                View submit = (dialoglayout.findViewById(R.id.submit));

                if (!Authentication.isLoggedIn || !Authentication.didOnline) {
                    pinned.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_POST) {
                    submit.setVisibility(View.GONE);
                }

                pinned.setVisibility(View.GONE);

                submit.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View view) {
                        Intent inte = new Intent(SubredditView.this, Submit.class);
                        if (!subOverride.contains("/m/") && canSubmit) {
                            inte.putExtra(Submit.EXTRA_SUBREDDIT, subOverride);
                        }
                        SubredditView.this.startActivity(inte);
                    }
                });
            }

            dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(SubredditView.this, Wiki.class);
                    i.putExtra(Wiki.EXTRA_SUBREDDIT, subOverride);
                    startActivity(i);
                }
            });
            dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(SubredditView.this, Submit.class);
                    if ((!subOverride.contains("/m/") || !subOverride.contains(".")) && canSubmit) {
                        i.putExtra(Submit.EXTRA_SUBREDDIT, subOverride);
                    }
                    startActivity(i);
                }
            });
            dialoglayout.findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int style = new ColorPreferences(SubredditView.this).getThemeSubreddit(subOverride);

                    final Context contextThemeWrapper = new ContextThemeWrapper(SubredditView.this, style);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);

                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);

                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(subOverride);
                    SettingsSubAdapter.showSubThemeEditor(arrayList, SubredditView.this, dialoglayout);
                }
            });
            dialoglayout.findViewById(R.id.mods).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog d = new MaterialDialog.Builder(SubredditView.this).title("Finding moderators")
                            .cancelable(true)
                            .content(R.string.misc_please_wait)
                            .progress(true, 100)
                            .show();
                    new AsyncTask<Void, Void, Void>() {
                        ArrayList<UserRecord> mods;

                        @Override
                        protected Void doInBackground(Void... params) {
                            mods = new ArrayList<>();
                            UserRecordPaginator paginator = new UserRecordPaginator(Authentication.reddit, subOverride, "moderators");
                            paginator.setSorting(Sorting.HOT);
                            paginator.setTimePeriod(TimePeriod.ALL);
                            while (paginator.hasNext()) {
                                mods.addAll(paginator.next());
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            final ArrayList<String> names = new ArrayList<>();
                            for (UserRecord rec : mods) {
                                names.add(rec.getFullName());
                            }
                            d.dismiss();
                            new MaterialDialog.Builder(SubredditView.this).title("/r/" + subOverride + " mods")
                                    .items(names)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                            Intent i = new Intent(SubredditView.this, Profile.class);
                                            i.putExtra(Profile.EXTRA_PROFILE, names.get(which));
                                            startActivity(i);
                                        }
                                    })
                                    .positiveText(R.string.btn_message)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Intent i = new Intent(SubredditView.this, Sendmessage.class);
                                            i.putExtra(Sendmessage.EXTRA_NAME, "/r/" + subOverride);
                                            startActivity(i);
                                        }
                                    }).show();
                        }
                    }.execute();
                }
            });
            dialoglayout.findViewById(R.id.flair).setVisibility(View.GONE);
            if (Authentication.didOnline && Authentication.isLoggedIn) {
                new AsyncTask<View, Void, View>() {
                    List<FlairTemplate> flairs;
                    ArrayList<String> flairText;
                    String current;
                    AccountManager m;

                    @Override
                    protected View doInBackground(View... params) {
                        try {
                            m = new AccountManager(Authentication.reddit);
                            JsonNode node = m.getFlairChoicesRootNode(subOverride, null);
                            flairs = m.getFlairChoices(subOverride, node);

                            FlairTemplate currentF = m.getCurrentFlair(subOverride, node);
                            if (currentF != null) {
                                if (currentF.getText().isEmpty()) {
                                    current = ("[" + currentF.getCssClass() + "]");
                                } else {
                                    current = (currentF.getText());
                                }
                            }
                            flairText = new ArrayList<>();
                            for (FlairTemplate temp : flairs) {
                                if (temp.getText().isEmpty()) {
                                    flairText.add("[" + temp.getCssClass() + "]");
                                } else {
                                    flairText.add(temp.getText());
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        return params[0];
                    }

                    @Override
                    protected void onPostExecute(View flair) {
                        if (flairs != null && !flairs.isEmpty() && flairText != null && !flairText.isEmpty()) {
                            flair.setVisibility(View.VISIBLE);
                            if (current != null) {
                                ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                            }
                            flair.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new MaterialDialog.Builder(SubredditView.this).items(flairText)
                                            .title("Select flair")
                                            .itemsCallback(new MaterialDialog.ListCallback() {
                                                @Override
                                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                    final FlairTemplate t = flairs.get(which);
                                                    if (t.isTextEditable()) {
                                                        new MaterialDialog.Builder(SubredditView.this).title("Set flair text")
                                                                .input("Flair text", t.getText(), true, new MaterialDialog.InputCallback() {
                                                                    @Override
                                                                    public void onInput(MaterialDialog dialog, CharSequence input) {

                                                                    }
                                                                }).positiveText("Set")
                                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                                                        final String flair = dialog.getInputEditText().getText().toString();
                                                                        new AsyncTask<Void, Void, Boolean>() {
                                                                            @Override
                                                                            protected Boolean doInBackground(Void... params) {
                                                                                try {
                                                                                    new ModerationManager(Authentication.reddit).setFlair(subOverride, t, flair, Authentication.name);
                                                                                    FlairTemplate currentF = m.getCurrentFlair(subOverride);
                                                                                    if (currentF.getText().isEmpty()) {
                                                                                        current = ("[" + currentF.getCssClass() + "]");
                                                                                    } else {
                                                                                        current = (currentF.getText());
                                                                                    }
                                                                                    return true;
                                                                                } catch (Exception e) {
                                                                                    e.printStackTrace();
                                                                                    return false;
                                                                                }
                                                                            }

                                                                            @Override
                                                                            protected void onPostExecute(Boolean done) {
                                                                                Snackbar s;
                                                                                if (done) {
                                                                                    if (current != null) {
                                                                                        ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                                                                                    }
                                                                                    s = Snackbar.make(mToolbar, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                                } else {
                                                                                    s = Snackbar.make(mToolbar, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                                }
                                                                                if (s != null) {
                                                                                    View view = s.getView();
                                                                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                                    tv.setTextColor(Color.WHITE);
                                                                                    s.show();
                                                                                }
                                                                            }
                                                                        }.execute();
                                                                    }
                                                                }).negativeText(R.string.btn_cancel)
                                                                .show();
                                                    } else {
                                                        new AsyncTask<Void, Void, Boolean>() {
                                                            @Override
                                                            protected Boolean doInBackground(Void... params) {
                                                                try {
                                                                    new ModerationManager(Authentication.reddit).setFlair(subOverride, t, null, Authentication.name);
                                                                    FlairTemplate currentF = m.getCurrentFlair(subOverride);
                                                                    if (currentF.getText().isEmpty()) {
                                                                        current = ("[" + currentF.getCssClass() + "]");
                                                                    } else {
                                                                        current = (currentF.getText());
                                                                    }
                                                                    return true;
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                    return false;
                                                                }
                                                            }

                                                            @Override
                                                            protected void onPostExecute(Boolean done) {
                                                                Snackbar s;
                                                                if (done) {
                                                                    if (current != null) {
                                                                        ((TextView) dialoglayout.findViewById(R.id.flair_text)).setText("Flair: " + current);
                                                                    }
                                                                    s = Snackbar.make(mToolbar, "Flair set successfully", Snackbar.LENGTH_SHORT);
                                                                } else {
                                                                    s = Snackbar.make(mToolbar, "Error setting flair, try again soon", Snackbar.LENGTH_SHORT);
                                                                }
                                                                if (s != null) {
                                                                    View view = s.getView();
                                                                    TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                                                                    tv.setTextColor(Color.WHITE);
                                                                    s.show();
                                                                }
                                                            }
                                                        }.execute();
                                                    }
                                                }
                                            }).show();
                                }
                            });
                        }
                    }
                }.execute(dialoglayout.findViewById(R.id.flair));
            }
        } else {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        }
    }


    View header;

    public int adjustAlpha(float factor) {
        int alpha = Math.round(Color.alpha(Color.BLACK) * factor);
        int red = Color.red(Color.BLACK);
        int green = Color.green(Color.BLACK);
        int blue = Color.blue(Color.BLACK);
        return Color.argb(alpha, red, green, blue);
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private SubmissionsView mCurrentFragment;
        private BlankFragment blankPage;

        @Override
        public Parcelable saveState() {
            return null;
        }

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == 0) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0, -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                        }
                    }

                    if (position == 0) {
                        ((OverviewPagerAdapter) pager.getAdapter()).blankPage.doOffset(positionOffset);
                        pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));
                    }
                }

                @Override
                public void onPageSelected(final int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (pager.getAdapter() != null) {
                pager.setCurrentItem(1);
            }
        }


        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        public void doSetPrimary(Object object, int position) {
            if (object != null && getCurrentFragment() != object && position != 3 && object instanceof SubmissionsView) {
                mCurrentFragment = ((SubmissionsView) object);
                if (mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                    mCurrentFragment.doAdapter();

                }
            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            doSetPrimary(object, position);
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 1) {
                SubmissionsView f = new SubmissionsView();
                Bundle args = new Bundle();
                args.putString("id", subreddit);
                f.setArguments(args);

                return f;
            } else {
                blankPage = new BlankFragment();
                return blankPage;
            }


        }


        @Override
        public int getCount() {
            return 2;
        }

    }


    Subreddit sub;

    public void executeAsyncSubreddit(String sub) {
        new AsyncGetSubreddit().execute(sub);
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {

            if (subreddit != null) {
                sub = subreddit;
                doSubSidebarNoLoad(sub.getDisplayName());
                doSubSidebar(sub.getDisplayName());
                doSubOnlyStuff(sub);
                SubredditView.this.subreddit = sub.getDisplayName();

                if (subreddit.isNsfw() && SettingValues.storeHistory && SettingValues.storeNSFWHistory)
                    UserSubscriptions.addSubToHistory(subreddit.getDisplayName());
                else if (SettingValues.storeHistory && !subreddit.isNsfw())
                    UserSubscriptions.addSubToHistory(subreddit.getDisplayName());

                // Over 18 interstitial for signed out users or those who haven't enabled NSFW content
                if (subreddit.isNsfw() && !Reddit.over18) {
                    new AlertDialogWrapper.Builder(SubredditView.this)
                            .setTitle(getString(R.string.over18_title, subreddit.getDisplayName()))
                            .setMessage(getString(R.string.over18_desc) + "\n\n"
                                    + getString(Authentication.isLoggedIn ? R.string.over18_desc_loggedin : R.string.over18_desc_loggedout))
                            .setCancelable(false)
                            .setPositiveButton(R.string.misc_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ((SubmissionsView) adapter.getCurrentFragment()).doAdapter(true);
                                }
                            }).setNeutralButton(R.string.btn_go_back, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
                }
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(SubredditView.this)
                                    .setTitle(R.string.subreddit_err)
                                    .setMessage(getString(R.string.subreddit_err_msg_new, params[0]))
                                    .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            setResult(4);
                                            finish();
                                        }
                                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    setResult(4);
                                    finish();
                                }
                            }).show();
                        } catch (Exception ignored) {

                        }
                    }
                });
                e.printStackTrace();

                return null;
            }
        }

    }

    public void doPageSelectedComments(int position) {
        header.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(180);
        pager.setSwipeLeftOnly(false);
        Reddit.currentPosition = position;
    }

    public class OverviewPagerAdapterComment extends OverviewPagerAdapter {
        private SubmissionsView mCurrentFragment;
        public int size = 2;

        @Override
        public Parcelable saveState() {
            return null;
        }

        public OverviewPagerAdapterComment(FragmentManager fm) {
            super(fm);
            pager.clearOnPageChangeListeners();
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == 0) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0, -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                        }

                        blankPage.doOffset(positionOffset);
                        pager.setBackgroundColor(adjustAlpha(positionOffset * 0.7f));

                    } else if (positionOffset == 0) {
                        if (position == 1) {
                            doPageSelectedComments(position);
                            if (position == 2 && adapter != null && adapter.getCurrentFragment() != null) {
                                ((SubmissionsView) adapter.getCurrentFragment()).adapter.refreshView();
                            }
                        } else {
                            //todo if (mAsyncGetSubreddit != null) {
                            //mAsyncGetSubreddit.cancel(true);
                            //}

                            if (header.getTranslationY() == 0)
                                header.animate()
                                        .translationY(-header.getHeight())
                                        .setInterpolator(new LinearInterpolator())
                                        .setDuration(180);

                            pager.setSwipeLeftOnly(true);
                            themeSystemBars(openingComments.getSubredditName().toLowerCase());
                            setRecentBar(openingComments.getSubredditName().toLowerCase());

                        }
                    }

                }

                @Override
                public void onPageSelected(final int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (pager.getAdapter() != null) {
                pager.getAdapter().notifyDataSetChanged();
                pager.setCurrentItem(1);
                pager.setCurrentItem(0);

            }
        }


        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }


        @Override
        public void doSetPrimary(Object object, int position) {
            if (position != 2 && position != 0) {
                if (getCurrentFragment() != object) {
                    mCurrentFragment = ((SubmissionsView) object);
                    if (mCurrentFragment != null && mCurrentFragment.posts == null && mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }
                }
            }

        }

        public Fragment storedFragment;

        @Override
        public int getItemPosition(Object object) {
            if (object != storedFragment)
                return POSITION_NONE;
            return POSITION_UNCHANGED;
        }

        BlankFragment blankPage;

        @Override
        public Fragment getItem(int i) {

            if (i == 0) {
                blankPage = new BlankFragment();
                return blankPage;
            } else if (openingComments == null || i != 2) {
                SubmissionsView f = new SubmissionsView();
                Bundle args = new Bundle();
                args.putString("id", subreddit);
                f.setArguments(args);
                return f;

            } else {
                Fragment f = new CommentPage();
                Bundle args = new Bundle();
                String name = openingComments.getFullName();
                args.putString("id", name.substring(3, name.length()));
                args.putBoolean("archived", openingComments.isArchived());
                args.putBoolean("locked", openingComments.isLocked());
                args.putInt("page", currentComment);
                args.putString("subreddit", openingComments.getSubredditName());
                args.putString("baseSubreddit", subreddit);
                f.setArguments(args);
                return f;
            }


        }


        @Override
        public int getCount() {
            return size;
        }


    }

}