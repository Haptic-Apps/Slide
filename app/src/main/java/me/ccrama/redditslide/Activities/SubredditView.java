package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.managers.AccountManager;
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
import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Views.CommentOverflow;
import me.ccrama.redditslide.Views.SidebarLayout;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;

public class SubredditView extends BaseActivityAnim {

    public static final String EXTRA_SUBREDDIT = "subreddit";

    private DrawerLayout drawerLayout;
    private String subreddit;
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
            case R.id.night: {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditView.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());


                builder.setView(dialoglayout);
                final Dialog d = builder.show();

                dialoglayout.findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = new ColorPreferences(SubredditView.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 2) {
                                Reddit.themeBack = theme.getThemeType();
                                new ColorPreferences(SubredditView.this).setFontStyle(theme);
                                d.dismiss();
                                recreate();

                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = new ColorPreferences(SubredditView.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 1) {
                                new ColorPreferences(SubredditView.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = new ColorPreferences(SubredditView.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                new ColorPreferences(SubredditView.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();
                                break;
                            }
                        }
                    }
                });
                dialoglayout.findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = new ColorPreferences(SubredditView.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 3) {
                                new ColorPreferences(SubredditView.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();
                                d.dismiss();
                                recreate();

                                break;
                            }
                        }
                    }
                });
            }

            return true;
            case R.id.submit:
                Intent i = new Intent(this, Submit.class);
                i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                startActivity(i);
                return true;
            case R.id.action_refresh:
                if (adapter != null && adapter.getCurrentFragment() != null)
                    ((SubmissionsView) adapter.getCurrentFragment()).forceRefresh();
                return true;
            case R.id.action_sort:
                if (subreddit.equalsIgnoreCase("friends")) {
                    Snackbar.make(findViewById(R.id.anchor), "Cannot sort /r/friends", Snackbar.LENGTH_SHORT).show();
                } else {
                    openPopup();
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
                if (!subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("random") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod")) {
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
            case R.id.action_info:
                drawerLayout.openDrawer(Gravity.RIGHT);
                return true;
            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((SubmissionsView) ((OverviewPagerAdapter) pager.getAdapter()).getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i2 = new Intent(this, Shadowbox.class);
                        i2.putExtra(Shadowbox.EXTRA_PAGE, 0);
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


    public String term;

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
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().getDecorView().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);

        subreddit = getIntent().getExtras().getString(EXTRA_SUBREDDIT, "");
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_singlesubreddit);
        setupSubredditAppBar(R.id.toolbar, subreddit, true, subreddit);

        UserSubscriptions.addSubToHistory(subreddit);
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
            pager.setSwipingEnabled(false);

        } else {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        }
        pager.setAdapter(adapter);
        pager.setCurrentItem(1);
        doSubSidebar(subreddit);


    }

    public boolean singleMode;
    public boolean commentPager;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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

    boolean[] chosen;

    public void filterContent(final String subreddit) {
        chosen = new boolean[]{PostMatch.isGif(subreddit), PostMatch.isAlbums(subreddit), PostMatch.isImage(subreddit), PostMatch.isNsfw(subreddit), PostMatch.isSelftext(subreddit), PostMatch.isUrls(subreddit)};

        new AlertDialogWrapper.Builder(this)
                .setTitle("Content to show in /r/" + subreddit)
                .alwaysCallMultiChoiceCallback()
                .setMultiChoiceItems(new String[]{"Gifs", "Albums", "Images", "NSFW Content", "Selftext", "Websites"}, chosen, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        chosen[which] = isChecked;
                    }
                }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtil.v(chosen[0] + " " + chosen[1] + " " + chosen[2] + " " + chosen[3] + " " + chosen[4] + " " + chosen[5]);
                PostMatch.setChosen(chosen, subreddit);
                reloadSubs();
            }
        }).setNegativeButton("Cancel", null).show();


    }

    public void openPopup() {
        PopupMenu popup = new PopupMenu(SubredditView.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final String[] base = Reddit.getSortingStrings(getBaseContext());
        for (String s : base) {
            popup.getMenu().add(s);
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

    private void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
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
            CheckBox c = ((CheckBox) findViewById(R.id.subscribed));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //reset check adapter
                }
            });
            c.setChecked(UserSubscriptions.isSubscriber(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids) {
                            if (isChecked) {
                                UserSubscriptions.addSubreddit(subreddit.getDisplayName().toLowerCase(), SubredditView.this);
                            } else {
                                UserSubscriptions.removeSubreddit(subreddit.getDisplayName().toLowerCase(), SubredditView.this);

                            }
                            Snackbar.make(mToolbar, isChecked ? getString(R.string.misc_subscribed) :
                                    getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
                        }

                        @Override
                        protected Void doInBackground(Void... params) {
                            if (isChecked) {
                                new AccountManager(Authentication.reddit).subscribe(subreddit);
                            } else {
                                new AccountManager(Authentication.reddit).unsubscribe(subreddit);

                            }
                            return null;
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

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    private void doSubSidebar(final String subreddit) {
        if (!subreddit.equalsIgnoreCase("all") && !subreddit.equalsIgnoreCase("frontpage") && !subreddit.equalsIgnoreCase("friends") && !subreddit.equalsIgnoreCase("mod") && !subreddit.contains("+")) {
            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);

            new AsyncGetSubreddit().execute(subreddit);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);


            findViewById(R.id.header_sub).setBackgroundColor(Palette.getColor(subreddit));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subreddit);
            View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox pinned = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                View submit = ( dialoglayout.findViewById(R.id.submit));
                if (!Authentication.isLoggedIn) {
                    pinned.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                    submit.setVisibility(View.GONE);
                }
                if (SettingValues.fab && SettingValues.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setVisibility(View.GONE);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(SubredditView.this, Submit.class);
                        inte.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        SubredditView.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all") || subreddit.toLowerCase().equals("friends") || subreddit.equalsIgnoreCase("mod") || subreddit.contains("+")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.mods).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SubredditView.this, Wiki.class);
                        i.putExtra(Wiki.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i);
                    }
                });
                dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SubredditView.this, Submit.class);
                        i.putExtra(Submit.EXTRA_SUBREDDIT, subreddit);
                        startActivity(i);
                    }
                });
                dialoglayout.findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int style = new ColorPreferences(SubredditView.this).getThemeSubreddit(subreddit);
                        final Context contextThemeWrapper = new ContextThemeWrapper(SubredditView.this, style);
                        LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(subreddit);
                        SettingsSubAdapter.showSubThemeEditor(arrayList, SubredditView.this, dialoglayout);
                    }
                });
                dialoglayout.findViewById(R.id.mods).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog d = new MaterialDialog.Builder(SubredditView.this).title("Finding moderators")
                                .cancelable(true)
                                .progress(true, 100)
                                .show();
                        new AsyncTask<Void, Void, Void>() {
                            ArrayList<UserRecord> mods;

                            @Override
                            protected Void doInBackground(Void... params) {
                                mods = new ArrayList<>();
                                UserRecordPaginator paginator = new UserRecordPaginator(Authentication.reddit, subreddit, "moderators");
                                paginator.setSorting(Sorting.HOT);
                                paginator.setTimePeriod(TimePeriod.ALL);
                                while (paginator.hasNext()) {
                                    mods.addAll(paginator.next());
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                final ArrayList<String> names = new ArrayList<String>();
                                for (UserRecord rec : mods) {
                                    names.add(rec.getFullName());
                                }
                                d.dismiss();
                                new MaterialDialog.Builder(SubredditView.this).title("/r/" + subreddit + " mods")
                                        .items(names)
                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                            @Override
                                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                                Intent i = new Intent(SubredditView.this, Profile.class);
                                                i.putExtra(Profile.EXTRA_PROFILE, names.get(which));
                                                startActivity(i);
                                            }
                                        }).show();
                            }
                        }.execute();
                    }
                });

                //Long press on buttons to get a "tooltip" describing the action
                dialoglayout.findViewById(R.id.wiki).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), getString(R.string.sidebar_wiki), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                dialoglayout.findViewById(R.id.submit).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), getString(R.string.editor_submit), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                dialoglayout.findViewById(R.id.theme).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), getString(R.string.subreddit_theme), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });

                dialoglayout.findViewById(R.id.mods).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(), getString(R.string.sidebar_mods), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }
        } else {
            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }


    private class ShowPopupSidebar extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            final String text = Authentication.reddit.getSubreddit(params[0]).getDataNode().get("description_html").asText();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.justtext, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubredditView.this);
                    final SpoilerRobotoTextView body = (SpoilerRobotoTextView) dialoglayout.findViewById(R.id.body);
                    final CommentOverflow overflow = (CommentOverflow) dialoglayout.findViewById(R.id.commentOverflow);
                    setViews(text, subreddit, body, overflow);

                    builder.setView(dialoglayout).show();

                }
            });
            return null;
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
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) header.getLayoutParams();
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
                if (mCurrentFragment.posts == null) {
                    if (mCurrentFragment.isAdded()) {
                        mCurrentFragment.doAdapter();
                    }

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


    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null)
                doSubOnlyStuff(subreddit);
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
                        } catch (Exception e) {

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
        pager.setSwipeLeftOnly(true);

        Reddit.currentPosition = position;
    }

    public class OverviewPagerAdapterComment extends OverviewPagerAdapter {
        private SubmissionsView mCurrentFragment;
        private CommentPage mCurrentComments;

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
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) header.getLayoutParams();
                        params.setMargins(header.getWidth() - positionOffsetPixels, 0, -((header.getWidth() - positionOffsetPixels)), 0);
                        header.setLayoutParams(params);
                        if (positionOffsetPixels == 0) {
                            finish();
                        }
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
                    if (mCurrentFragment != null) {
                        if (mCurrentFragment.posts == null) {
                            if (mCurrentFragment.isAdded()) {
                                mCurrentFragment.doAdapter();
                            }
                        }
                    }
                }
            } else if (object instanceof CommentPage) {
                mCurrentComments = (CommentPage) object;
            }

        }

        public Fragment storedFragment;

        @Override
        public int getItemPosition(Object object) {
            if (object != storedFragment)
                return POSITION_NONE;
            return POSITION_UNCHANGED;
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 0) {
                return new BlankFragment();
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