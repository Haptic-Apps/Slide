package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.Views.ToastHelpCreation;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditOverview extends OverviewBase {


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            int current = pager.getCurrentItem();
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            pager.setAdapter(adapter);
            pager.setCurrentItem(current);
        } else if (requestCode == 1) {
            restartTheme();
        } else if (requestCode == 3) {
            new SubredditStorageNoContext().execute(SubredditOverview.this);

        } else if (requestCode == 4 && resultCode != 4) {
            if (e != null) {
                e.clearFocus();
                e.setText("");
                drawerLayout.closeDrawers();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Slide");
        if (getIntent() != null && getIntent().hasExtra("pageTo"))
            toGoto = getIntent().getIntExtra("pageTo", 0);

        /*if (DataShare.notifs != null) {

            final Submission s = DataShare.notifs;
            LayoutInflater inflater = getLayoutInflater();
            final View dialoglayout = inflater.inflate(R.layout.popupsubmission, null);
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
            new MakeTextviewClickable().ParseTextWithLinksTextView(s.getDataNode().get("selftext_html").asText(), (ActiveTextView) dialoglayout.findViewById(R.id.body), this, s.getSubredditName());

            ((TextView) dialoglayout.findViewById(R.id.title)).setText(s.getTitle());

            ImageView title = (ImageView) dialoglayout.findViewById(R.id.image);
            if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height") && s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

                String url = s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                Ion.with(title).load(url);
            } else {
                title.setVisibility(View.GONE);
            }
            ((TextView) dialoglayout.findViewById(R.id.info)).setText(TimeUtils.getTimeAgo(s.getCreatedUtc().getTime(), getBaseContext()));


            final Dialog dialog = builder.setView(dialoglayout).create();
            dialog.show();
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reddit.hidden.edit().putBoolean(s.getFullName(), true).apply();
                    dialog.dismiss();
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Reddit.hidden.edit().putBoolean(s.getFullName(), true).apply();
                    dialog.dismiss();
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Reddit.hidden.edit().putBoolean(s.getFullName(), true).apply();
                    dialog.dismiss();
                }
            });
        }*/
        if (!Reddit.colors.getBoolean("Tutorial", false)) {
            Intent i = new Intent(SubredditOverview.this, Tutorial.class);
            startActivity(i);
        }
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(Pallete.getDefaultColor())));
            SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_default), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor("")));

        }
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        header = findViewById(R.id.header);
        pager = (ViewPager) findViewById(R.id.contentView);


        setDataSet(SubredditStorage.subredditsForHome);
        doSidebar();


        findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });
        findViewById(R.id.sorting).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastHelpCreation.makeToast(v, getString(R.string.sorting_change_sorting), SubredditOverview.this);
                return false;
            }
        });
        findViewById(R.id.grid).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastHelpCreation.makeToast(v, getString(R.string.general_enable_shadowbox), SubredditOverview.this);
                return false;
            }
        });
        findViewById(R.id.grid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {

                    if (Reddit.tabletUI) {

                        if (((SubmissionsView) adapter.getCurrentFragment()).posts.posts != null) {
                            DataShare.sharedSubreddit = ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                            Intent i = new Intent(SubredditOverview.this, Shadowbox.class);
                            i.putExtra("position", pager.getCurrentItem());
                            startActivity(i);
                        }
                    } else {
                        new AlertDialogWrapper.Builder(SubredditOverview.this)

                                .setTitle(R.string.general_pro)
                                .setMessage(R.string.general_pro_msg)
                                .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        } catch (ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        }
                                    }
                                }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).show();
                    }
                }
            }
        });

        findViewById(R.id.info).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastHelpCreation.makeToast(v, getString(R.string.general_open_settings), SubredditOverview.this);
                return false;
            }
        });
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                {
                    if (usedArray != null) {
                        String sub = usedArray.get(pager.getCurrentItem());
                        if (!sub.equals("frontpage") && !sub.equals("all")) {
                            ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.RIGHT);
                        }
                    }
                }
            }
        });
        pager.setCurrentItem(1);
        pager.setCurrentItem(0); //force redraw and sidebar


    }


}