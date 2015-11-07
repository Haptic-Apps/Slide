package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.DragSort.ListViewDraggingAnimation;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditInputFilter;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.ToastHelpCreation;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditOverview extends OverviewBase {


    private int toGoto = 0;

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

    private String subToDo;

    public void resetAdapter() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usedArray = new ArrayList<>(SubredditStorage.subredditsForHome);

                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                pager.setAdapter(adapter);
                tabs.setupWithViewPager(pager);

                pager.setCurrentItem(usedArray.indexOf(subToDo));

                int color = Pallete.getColor(subToDo);
                hea.setBackgroundColor(color);
                findViewById(R.id.header).setBackgroundColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.setStatusBarColor(Pallete.getDarkerColor(color));
                    SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(subToDo, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), color));

                }

            }
        });


    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawers();
        } else if (Reddit.exit) {
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
            builder.setTitle(R.string.general_confirm_exit);
            builder.setMessage(R.string.general_confirm_exit_msg);
            builder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            super.onBackPressed();
        }
    }

    private void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    private void chooseAccounts() {
        final ArrayList<String> accounts = new ArrayList<>();
        final ArrayList<String> names = new ArrayList<>();

        for (String s : Authentication.authentication.getStringSet("accounts", new HashSet<String>())) {
            if (s.contains(":")) {
                accounts.add(s.split(":")[0]);
            } else {
                accounts.add(s);
            }
            names.add(s);
        }
        new AlertDialogWrapper.Builder(SubredditOverview.this)
                .setTitle(R.string.general_switch_acc)
                .setAdapter(new ArrayAdapter<>(SubredditOverview.this, android.R.layout.simple_expandable_list_item_1, accounts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (names.get(which).contains(":")) {
                            String token = names.get(which).split(":")[1];
                            Authentication.authentication.edit().putString("lasttoken", token).commit();
                        } else {

                            ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                            Authentication.authentication.edit().putString("lasttoken", tokens.get(which)).commit();

                        }

                        Reddit.forceRestart(SubredditOverview.this);

                    }
                }).create().show();
    }

    private View header;

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

    private void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final ActiveTextView body = (ActiveTextView) findViewById(R.id.sidebar_text);
            new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditOverview.this, "slideforreddit");
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
            c.setChecked(SubredditStorage.realSubs.contains(subreddit.getDisplayName().toLowerCase()));
            c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public void onPostExecute(Void voids){
                            new SubredditStorageNoContext().execute(SubredditOverview.this);
                             Snackbar.make(header, isChecked?"Subscribed":"Unsubscribed", Snackbar.LENGTH_SHORT);
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
        ( findViewById(R.id.sub_title)).setVisibility(View.GONE);
        findViewById(R.id.sub_title).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null)
                doSubOnlyStuff(subreddit);
        }

        @Override
        protected Subreddit doInBackground(String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                return null;
            }

        }
    }

    private void doSubSidebar(final String subreddit) {
        if (!subreddit.equals("all") && !subreddit.equals("frontpage")) {
            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
            findViewById(R.id.info).setVisibility(View.VISIBLE);

            new AsyncGetSubreddit().execute(subreddit);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);


            findViewById(R.id.header_sub).setBackgroundColor(Pallete.getColor(subreddit));
            ((TextView) findViewById(R.id.sub_infotitle)).setText(subreddit);
            View dialoglayout = findViewById(R.id.sidebarsub);
            {
                CheckBox c = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                if (!Authentication.isLoggedIn) {
                    c.setVisibility(View.GONE);
                    findViewById(R.id.subscribed).setVisibility(View.GONE);
                }
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //reset check adapter
                    }
                });
                if (SubredditStorage.getPins() == null) {
                    c.setChecked(false);

                } else if (SubredditStorage.getPins().contains(subreddit.toLowerCase())) {
                    c.setChecked(true);
                } else {
                    c.setChecked(false);
                }
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            SubredditStorage.addPin(subreddit);
                        } else {
                            SubredditStorage.removePin(subreddit);
                        }
                        subToDo = subreddit;
                        new SubredditStorageNoContext().execute(SubredditOverview.this);
                    }
                });
                c.setHighlightColor(new ColorPreferences(SubredditOverview.this).getThemeSubreddit(subreddit, true).getColor());
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(SubredditOverview.this, Wiki.class);
                        i.putExtra("subreddit", subreddit);
                        startActivity(i);
                    }
                });

            }
            findViewById(R.id.sub_theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String subreddit = usedArray.get(pager.getCurrentItem());

                    int style = new ColorPreferences(SubredditOverview.this).getThemeSubreddit(subreddit);
                    final Context contextThemeWrapper = new ContextThemeWrapper(SubredditOverview.this, style);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText("/r/" + subreddit);
                    title.setBackgroundColor(Pallete.getColor(subreddit));

                    {
                        final View body = dialoglayout.findViewById(R.id.body2);
                        body.setVisibility(View.INVISIBLE);
                        final View center = dialoglayout.findViewById(R.id.colorExpandFrom);
                        dialoglayout.findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int cx = center.getWidth() / 2;
                                int cy = center.getHeight() / 2;

                                int finalRadius = Math.max(body.getWidth(), body.getHeight());

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Animator anim =
                                            ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                                    body.setVisibility(View.VISIBLE);
                                    anim.start();
                                } else {
                                    body.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                        LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
                        final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

                        colorPicker.setColors(new int[]{
                                getResources().getColor(R.color.md_red_500),
                                getResources().getColor(R.color.md_pink_500),
                                getResources().getColor(R.color.md_purple_500),
                                getResources().getColor(R.color.md_deep_purple_500),
                                getResources().getColor(R.color.md_indigo_500),
                                getResources().getColor(R.color.md_blue_500),
                                getResources().getColor(R.color.md_light_blue_500),
                                getResources().getColor(R.color.md_cyan_500),
                                getResources().getColor(R.color.md_teal_500),
                                getResources().getColor(R.color.md_green_500),
                                getResources().getColor(R.color.md_light_green_500),
                                getResources().getColor(R.color.md_lime_500),
                                getResources().getColor(R.color.md_yellow_500),
                                getResources().getColor(R.color.md_amber_500),
                                getResources().getColor(R.color.md_orange_500),
                                getResources().getColor(R.color.md_deep_orange_500),
                                getResources().getColor(R.color.md_brown_500),
                                getResources().getColor(R.color.md_grey_500),
                                getResources().getColor(R.color.md_blue_grey_500),

                        });
                        int currentColor = Pallete.getColor(subreddit);
                        for (int i : colorPicker.getColors()) {
                            for (int i2 : getColors(i)) {
                                if (i2 == currentColor) {
                                    colorPicker.setSelectedColor(i);
                                    colorPicker2.setColors(getColors(i));
                                    colorPicker2.setSelectedColor(i2);
                                    break;
                                }
                            }
                        }
                        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int c) {

                                colorPicker2.setColors(getColors(c));
                                colorPicker2.setSelectedColor(c);


                            }
                        });
                        colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int i) {
                                hea.setBackgroundColor(colorPicker2.getColor());
                                findViewById(R.id.header).setBackgroundColor(colorPicker2.getColor());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    Window window = getWindow();
                                    window.setStatusBarColor(Pallete.getDarkerColor(colorPicker2.getColor()));
                                    SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                }
                                title.setBackgroundColor(colorPicker2.getColor());
                            }
                        });


                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.reset);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Pallete.removeColor(subreddit);
                                    hea.setBackgroundColor(Pallete.getDefaultColor());
                                    findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
                                        SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                    }
                                    title.setBackgroundColor(Pallete.getDefaultColor());


                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int initialRadius = body.getWidth();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                        anim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                body.setVisibility(View.GONE);
                                            }
                                        });
                                        anim.start();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }

                                }
                            });


                        }
                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Pallete.setColor(subreddit, colorPicker2.getColor());

                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int initialRadius = body.getWidth();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                        anim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                body.setVisibility(View.GONE);
                                            }
                                        });
                                        anim.start();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }

                                }
                            });


                        }
                    }
                    {
                        {

                            final View body = dialoglayout.findViewById(R.id.body3);
                            body.setVisibility(View.INVISIBLE);
                            final View center = dialoglayout.findViewById(R.id.colorExpandFrom2);
                            dialoglayout.findViewById(R.id.color2).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int finalRadius = Math.max(body.getWidth(), body.getHeight());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                                        body.setVisibility(View.VISIBLE);
                                        anim.start();
                                    } else {
                                        body.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                            final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

                            int[] arrs = new int[ColorPreferences.Theme.values().length / 3];
                            int i = 0;
                            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                if (type.getThemeType() == 0) {
                                    arrs[i] = getResources().getColor(type.getColor());

                                    i++;
                                }
                            }

                            colorPicker.setColors(arrs);

                            colorPicker.setSelectedColor(new ColorPreferences(SubredditOverview.this).getFontStyle().getColor());

                            {
                                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok2);

                                // if button is clicked, close the custom dialog
                                dialogButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int color = colorPicker.getColor();
                                        ColorPreferences.Theme t = null;
                                        for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                            if (getResources().getColor(type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                                t = type;
                                                break;
                                            }
                                        }

                                        new ColorPreferences(SubredditOverview.this).setFontStyle(t, subreddit);

                                        int cx = center.getWidth() / 2;
                                        int cy = center.getHeight() / 2;

                                        int initialRadius = body.getWidth();
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                            Animator anim =
                                                    ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                            anim.addListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    super.onAnimationEnd(animation);
                                                    body.setVisibility(View.GONE);
                                                }
                                            });
                                            anim.start();
                                        } else {
                                            body.setVisibility(View.GONE);
                                        }
                                        int current = pager.getCurrentItem();
                                        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
                                        pager.setAdapter(adapter);
                                        pager.setCurrentItem(current);


                                    }
                                });


                            }
                        }
                    }

                    dialoglayout.findViewById(R.id.card).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 1:
                                            SettingValues.prefs.edit().putBoolean("PRESET" + subreddit, true).apply();
                                            reloadSubs();
                                            break;
                                        case 0:
                                            SettingValues.prefs.edit().remove("PRESET" + subreddit).apply();
                                            reloadSubs();
                                            break;

                                    }
                                }
                            };
                            int i = (SettingValues.prefs.contains("PRESET" + subreddit) ? 1 : 0);
                            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
                            builder.setTitle(R.string.settings_layout_chooser);
                            builder.setSingleChoiceItems(
                                    new String[]{getString(R.string.settings_layout_default),
                                            getString(R.string.settings_title_alternative_layout)}, i, l2);
                            builder.show();

                        }
                    });
                    builder.setView(dialoglayout);
                    builder.show();

                }
            });
        } else {
            //Hide info button on frontpage and all
            findViewById(R.id.info).setVisibility(View.GONE);

            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }

    private int[] getColors(int c) {
        if (c == getResources().getColor(R.color.md_red_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_red_100),
                    getResources().getColor(R.color.md_red_200),
                    getResources().getColor(R.color.md_red_300),
                    getResources().getColor(R.color.md_red_400),
                    getResources().getColor(R.color.md_red_500),
                    getResources().getColor(R.color.md_red_600),
                    getResources().getColor(R.color.md_red_700),
                    getResources().getColor(R.color.md_red_800),
                    getResources().getColor(R.color.md_red_900)
            };
        } else if (c == getResources().getColor(R.color.md_pink_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_pink_100),
                    getResources().getColor(R.color.md_pink_200),
                    getResources().getColor(R.color.md_pink_300),
                    getResources().getColor(R.color.md_pink_400),
                    getResources().getColor(R.color.md_pink_500),
                    getResources().getColor(R.color.md_pink_600),
                    getResources().getColor(R.color.md_pink_700),
                    getResources().getColor(R.color.md_pink_800),
                    getResources().getColor(R.color.md_pink_900)
            };
        } else if (c == getResources().getColor(R.color.md_purple_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_purple_100),
                    getResources().getColor(R.color.md_purple_200),
                    getResources().getColor(R.color.md_purple_300),
                    getResources().getColor(R.color.md_purple_400),
                    getResources().getColor(R.color.md_purple_500),
                    getResources().getColor(R.color.md_purple_600),
                    getResources().getColor(R.color.md_purple_700),
                    getResources().getColor(R.color.md_purple_800),
                    getResources().getColor(R.color.md_purple_900)
            };
        } else if (c == getResources().getColor(R.color.md_deep_purple_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_deep_purple_100),
                    getResources().getColor(R.color.md_deep_purple_200),
                    getResources().getColor(R.color.md_deep_purple_300),
                    getResources().getColor(R.color.md_deep_purple_400),
                    getResources().getColor(R.color.md_deep_purple_500),
                    getResources().getColor(R.color.md_deep_purple_600),
                    getResources().getColor(R.color.md_deep_purple_700),
                    getResources().getColor(R.color.md_deep_purple_800),
                    getResources().getColor(R.color.md_deep_purple_900)
            };
        } else if (c == getResources().getColor(R.color.md_indigo_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_indigo_100),
                    getResources().getColor(R.color.md_indigo_200),
                    getResources().getColor(R.color.md_indigo_300),
                    getResources().getColor(R.color.md_indigo_400),
                    getResources().getColor(R.color.md_indigo_500),
                    getResources().getColor(R.color.md_indigo_600),
                    getResources().getColor(R.color.md_indigo_700),
                    getResources().getColor(R.color.md_indigo_800),
                    getResources().getColor(R.color.md_indigo_900)
            };
        } else if (c == getResources().getColor(R.color.md_blue_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_blue_100),
                    getResources().getColor(R.color.md_blue_200),
                    getResources().getColor(R.color.md_blue_300),
                    getResources().getColor(R.color.md_blue_400),
                    getResources().getColor(R.color.md_blue_500),
                    getResources().getColor(R.color.md_blue_600),
                    getResources().getColor(R.color.md_blue_700),
                    getResources().getColor(R.color.md_blue_800),
                    getResources().getColor(R.color.md_blue_900)
            };
        } else if (c == getResources().getColor(R.color.md_light_blue_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_light_blue_100),
                    getResources().getColor(R.color.md_light_blue_200),
                    getResources().getColor(R.color.md_light_blue_300),
                    getResources().getColor(R.color.md_light_blue_400),
                    getResources().getColor(R.color.md_light_blue_500),
                    getResources().getColor(R.color.md_light_blue_600),
                    getResources().getColor(R.color.md_light_blue_700),
                    getResources().getColor(R.color.md_light_blue_800),
                    getResources().getColor(R.color.md_light_blue_900)
            };
        } else if (c == getResources().getColor(R.color.md_cyan_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_cyan_100),
                    getResources().getColor(R.color.md_cyan_200),
                    getResources().getColor(R.color.md_cyan_300),
                    getResources().getColor(R.color.md_cyan_400),
                    getResources().getColor(R.color.md_cyan_500),
                    getResources().getColor(R.color.md_cyan_600),
                    getResources().getColor(R.color.md_cyan_700),
                    getResources().getColor(R.color.md_cyan_800),
                    getResources().getColor(R.color.md_cyan_900)
            };
        } else if (c == getResources().getColor(R.color.md_teal_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_teal_100),
                    getResources().getColor(R.color.md_teal_200),
                    getResources().getColor(R.color.md_teal_300),
                    getResources().getColor(R.color.md_teal_400),
                    getResources().getColor(R.color.md_teal_500),
                    getResources().getColor(R.color.md_teal_600),
                    getResources().getColor(R.color.md_teal_700),
                    getResources().getColor(R.color.md_teal_800),
                    getResources().getColor(R.color.md_teal_900)
            };
        } else if (c == getResources().getColor(R.color.md_green_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_green_100),
                    getResources().getColor(R.color.md_green_200),
                    getResources().getColor(R.color.md_green_300),
                    getResources().getColor(R.color.md_green_400),
                    getResources().getColor(R.color.md_green_500),
                    getResources().getColor(R.color.md_green_600),
                    getResources().getColor(R.color.md_green_700),
                    getResources().getColor(R.color.md_green_800),
                    getResources().getColor(R.color.md_green_900)
            };
        } else if (c == getResources().getColor(R.color.md_light_green_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_light_green_100),
                    getResources().getColor(R.color.md_light_green_200),
                    getResources().getColor(R.color.md_light_green_300),
                    getResources().getColor(R.color.md_light_green_400),
                    getResources().getColor(R.color.md_light_green_500),
                    getResources().getColor(R.color.md_light_green_600),
                    getResources().getColor(R.color.md_light_green_700),
                    getResources().getColor(R.color.md_light_green_800),
                    getResources().getColor(R.color.md_light_green_900)
            };
        } else if (c == getResources().getColor(R.color.md_lime_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_lime_100),
                    getResources().getColor(R.color.md_lime_200),
                    getResources().getColor(R.color.md_lime_300),
                    getResources().getColor(R.color.md_lime_400),
                    getResources().getColor(R.color.md_lime_500),
                    getResources().getColor(R.color.md_lime_600),
                    getResources().getColor(R.color.md_lime_700),
                    getResources().getColor(R.color.md_lime_800),
                    getResources().getColor(R.color.md_lime_900)
            };
        } else if (c == getResources().getColor(R.color.md_yellow_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_yellow_100),
                    getResources().getColor(R.color.md_yellow_200),
                    getResources().getColor(R.color.md_yellow_300),
                    getResources().getColor(R.color.md_yellow_400),
                    getResources().getColor(R.color.md_yellow_500),
                    getResources().getColor(R.color.md_yellow_600),
                    getResources().getColor(R.color.md_yellow_700),
                    getResources().getColor(R.color.md_yellow_800),
                    getResources().getColor(R.color.md_yellow_900)
            };
        } else if (c == getResources().getColor(R.color.md_amber_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_amber_100),
                    getResources().getColor(R.color.md_amber_200),
                    getResources().getColor(R.color.md_amber_300),
                    getResources().getColor(R.color.md_amber_400),
                    getResources().getColor(R.color.md_amber_500),
                    getResources().getColor(R.color.md_amber_600),
                    getResources().getColor(R.color.md_amber_700),
                    getResources().getColor(R.color.md_amber_800),
                    getResources().getColor(R.color.md_amber_900)
            };
        } else if (c == getResources().getColor(R.color.md_orange_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_orange_100),
                    getResources().getColor(R.color.md_orange_200),
                    getResources().getColor(R.color.md_orange_300),
                    getResources().getColor(R.color.md_orange_400),
                    getResources().getColor(R.color.md_orange_500),
                    getResources().getColor(R.color.md_orange_600),
                    getResources().getColor(R.color.md_orange_700),
                    getResources().getColor(R.color.md_orange_800),
                    getResources().getColor(R.color.md_orange_900)
            };
        } else if (c == getResources().getColor(R.color.md_deep_orange_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_deep_orange_100),
                    getResources().getColor(R.color.md_deep_orange_200),
                    getResources().getColor(R.color.md_deep_orange_300),
                    getResources().getColor(R.color.md_deep_orange_400),
                    getResources().getColor(R.color.md_deep_orange_500),
                    getResources().getColor(R.color.md_deep_orange_600),
                    getResources().getColor(R.color.md_deep_orange_700),
                    getResources().getColor(R.color.md_deep_orange_800),
                    getResources().getColor(R.color.md_deep_orange_900)
            };
        } else if (c == getResources().getColor(R.color.md_brown_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_brown_100),
                    getResources().getColor(R.color.md_brown_200),
                    getResources().getColor(R.color.md_brown_300),
                    getResources().getColor(R.color.md_brown_400),
                    getResources().getColor(R.color.md_brown_500),
                    getResources().getColor(R.color.md_brown_600),
                    getResources().getColor(R.color.md_brown_700),
                    getResources().getColor(R.color.md_brown_800),
                    getResources().getColor(R.color.md_brown_900)
            };
        } else if (c == getResources().getColor(R.color.md_grey_500)) {
            return new int[]{
                    getResources().getColor(R.color.md_grey_100),
                    getResources().getColor(R.color.md_grey_200),
                    getResources().getColor(R.color.md_grey_300),
                    getResources().getColor(R.color.md_grey_400),
                    getResources().getColor(R.color.md_grey_500),
                    getResources().getColor(R.color.md_grey_600),
                    getResources().getColor(R.color.md_grey_700),
                    getResources().getColor(R.color.md_grey_800),
                    getResources().getColor(R.color.md_grey_900)
            };
        } else {
            return new int[]{
                    getResources().getColor(R.color.md_blue_grey_100),
                    getResources().getColor(R.color.md_blue_grey_200),
                    getResources().getColor(R.color.md_blue_grey_300),
                    getResources().getColor(R.color.md_blue_grey_400),
                    getResources().getColor(R.color.md_blue_grey_500),
                    getResources().getColor(R.color.md_blue_grey_600),
                    getResources().getColor(R.color.md_blue_grey_700),
                    getResources().getColor(R.color.md_blue_grey_800),
                    getResources().getColor(R.color.md_blue_grey_900)
            };

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
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
                    final ActiveTextView body = (ActiveTextView) dialoglayout.findViewById(R.id.body);
                    new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditOverview.this, "slideforreddit");

                    builder.setView(dialoglayout).show();

                }
            });
            return null;
        }
    }

    public int[] getMainColors() {
        return new int[]{
                getResources().getColor(R.color.md_red_500),
                getResources().getColor(R.color.md_pink_500),
                getResources().getColor(R.color.md_purple_500),
                getResources().getColor(R.color.md_deep_purple_500),
                getResources().getColor(R.color.md_indigo_500),
                getResources().getColor(R.color.md_blue_500),
                getResources().getColor(R.color.md_light_blue_500),
                getResources().getColor(R.color.md_cyan_500),
                getResources().getColor(R.color.md_teal_500),
                getResources().getColor(R.color.md_green_500),
                getResources().getColor(R.color.md_light_green_500),
                getResources().getColor(R.color.md_lime_500),
                getResources().getColor(R.color.md_yellow_500),
                getResources().getColor(R.color.md_amber_500),
                getResources().getColor(R.color.md_orange_500),
                getResources().getColor(R.color.md_deep_orange_500),
                getResources().getColor(R.color.md_brown_500),
                getResources().getColor(R.color.md_grey_500),
                getResources().getColor(R.color.md_blue_grey_500)};
    }

    public int[][] getSecondaryColors() {
        return new int[][]{
                new int[]{
                        getResources().getColor(R.color.md_red_100),
                        getResources().getColor(R.color.md_red_200),
                        getResources().getColor(R.color.md_red_300),
                        getResources().getColor(R.color.md_red_400),
                        getResources().getColor(R.color.md_red_500),
                        getResources().getColor(R.color.md_red_600),
                        getResources().getColor(R.color.md_red_700),
                        getResources().getColor(R.color.md_red_800),
                        getResources().getColor(R.color.md_red_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_pink_100),
                        getResources().getColor(R.color.md_pink_200),
                        getResources().getColor(R.color.md_pink_300),
                        getResources().getColor(R.color.md_pink_400),
                        getResources().getColor(R.color.md_pink_500),
                        getResources().getColor(R.color.md_pink_600),
                        getResources().getColor(R.color.md_pink_700),
                        getResources().getColor(R.color.md_pink_800),
                        getResources().getColor(R.color.md_pink_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_purple_100),
                        getResources().getColor(R.color.md_purple_200),
                        getResources().getColor(R.color.md_purple_300),
                        getResources().getColor(R.color.md_purple_400),
                        getResources().getColor(R.color.md_purple_500),
                        getResources().getColor(R.color.md_purple_600),
                        getResources().getColor(R.color.md_purple_700),
                        getResources().getColor(R.color.md_purple_800),
                        getResources().getColor(R.color.md_purple_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_deep_purple_100),
                        getResources().getColor(R.color.md_deep_purple_200),
                        getResources().getColor(R.color.md_deep_purple_300),
                        getResources().getColor(R.color.md_deep_purple_400),
                        getResources().getColor(R.color.md_deep_purple_500),
                        getResources().getColor(R.color.md_deep_purple_600),
                        getResources().getColor(R.color.md_deep_purple_700),
                        getResources().getColor(R.color.md_deep_purple_800),
                        getResources().getColor(R.color.md_deep_purple_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_indigo_100),
                        getResources().getColor(R.color.md_indigo_200),
                        getResources().getColor(R.color.md_indigo_300),
                        getResources().getColor(R.color.md_indigo_400),
                        getResources().getColor(R.color.md_indigo_500),
                        getResources().getColor(R.color.md_indigo_600),
                        getResources().getColor(R.color.md_indigo_700),
                        getResources().getColor(R.color.md_indigo_800),
                        getResources().getColor(R.color.md_indigo_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_blue_100),
                        getResources().getColor(R.color.md_blue_200),
                        getResources().getColor(R.color.md_blue_300),
                        getResources().getColor(R.color.md_blue_400),
                        getResources().getColor(R.color.md_blue_500),
                        getResources().getColor(R.color.md_blue_600),
                        getResources().getColor(R.color.md_blue_700),
                        getResources().getColor(R.color.md_blue_800),
                        getResources().getColor(R.color.md_blue_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_light_blue_100),
                        getResources().getColor(R.color.md_light_blue_200),
                        getResources().getColor(R.color.md_light_blue_300),
                        getResources().getColor(R.color.md_light_blue_400),
                        getResources().getColor(R.color.md_light_blue_500),
                        getResources().getColor(R.color.md_light_blue_600),
                        getResources().getColor(R.color.md_light_blue_700),
                        getResources().getColor(R.color.md_light_blue_800),
                        getResources().getColor(R.color.md_light_blue_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_cyan_100),
                        getResources().getColor(R.color.md_cyan_200),
                        getResources().getColor(R.color.md_cyan_300),
                        getResources().getColor(R.color.md_cyan_400),
                        getResources().getColor(R.color.md_cyan_500),
                        getResources().getColor(R.color.md_cyan_600),
                        getResources().getColor(R.color.md_cyan_700),
                        getResources().getColor(R.color.md_cyan_800),
                        getResources().getColor(R.color.md_cyan_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_teal_100),
                        getResources().getColor(R.color.md_teal_200),
                        getResources().getColor(R.color.md_teal_300),
                        getResources().getColor(R.color.md_teal_400),
                        getResources().getColor(R.color.md_teal_500),
                        getResources().getColor(R.color.md_teal_600),
                        getResources().getColor(R.color.md_teal_700),
                        getResources().getColor(R.color.md_teal_800),
                        getResources().getColor(R.color.md_teal_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_green_100),
                        getResources().getColor(R.color.md_green_200),
                        getResources().getColor(R.color.md_green_300),
                        getResources().getColor(R.color.md_green_400),
                        getResources().getColor(R.color.md_green_500),
                        getResources().getColor(R.color.md_green_600),
                        getResources().getColor(R.color.md_green_700),
                        getResources().getColor(R.color.md_green_800),
                        getResources().getColor(R.color.md_green_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_light_green_100),
                        getResources().getColor(R.color.md_light_green_200),
                        getResources().getColor(R.color.md_light_green_300),
                        getResources().getColor(R.color.md_light_green_400),
                        getResources().getColor(R.color.md_light_green_500),
                        getResources().getColor(R.color.md_light_green_600),
                        getResources().getColor(R.color.md_light_green_700),
                        getResources().getColor(R.color.md_light_green_800),
                        getResources().getColor(R.color.md_light_green_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_lime_100),
                        getResources().getColor(R.color.md_lime_200),
                        getResources().getColor(R.color.md_lime_300),
                        getResources().getColor(R.color.md_lime_400),
                        getResources().getColor(R.color.md_lime_500),
                        getResources().getColor(R.color.md_lime_600),
                        getResources().getColor(R.color.md_lime_700),
                        getResources().getColor(R.color.md_lime_800),
                        getResources().getColor(R.color.md_lime_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_yellow_100),
                        getResources().getColor(R.color.md_yellow_200),
                        getResources().getColor(R.color.md_yellow_300),
                        getResources().getColor(R.color.md_yellow_400),
                        getResources().getColor(R.color.md_yellow_500),
                        getResources().getColor(R.color.md_yellow_600),
                        getResources().getColor(R.color.md_yellow_700),
                        getResources().getColor(R.color.md_yellow_800),
                        getResources().getColor(R.color.md_yellow_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_amber_100),
                        getResources().getColor(R.color.md_amber_200),
                        getResources().getColor(R.color.md_amber_300),
                        getResources().getColor(R.color.md_amber_400),
                        getResources().getColor(R.color.md_amber_500),
                        getResources().getColor(R.color.md_amber_600),
                        getResources().getColor(R.color.md_amber_700),
                        getResources().getColor(R.color.md_amber_800),
                        getResources().getColor(R.color.md_amber_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_orange_100),
                        getResources().getColor(R.color.md_orange_200),
                        getResources().getColor(R.color.md_orange_300),
                        getResources().getColor(R.color.md_orange_400),
                        getResources().getColor(R.color.md_orange_500),
                        getResources().getColor(R.color.md_orange_600),
                        getResources().getColor(R.color.md_orange_700),
                        getResources().getColor(R.color.md_orange_800),
                        getResources().getColor(R.color.md_orange_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_deep_orange_100),
                        getResources().getColor(R.color.md_deep_orange_200),
                        getResources().getColor(R.color.md_deep_orange_300),
                        getResources().getColor(R.color.md_deep_orange_400),
                        getResources().getColor(R.color.md_deep_orange_500),
                        getResources().getColor(R.color.md_deep_orange_600),
                        getResources().getColor(R.color.md_deep_orange_700),
                        getResources().getColor(R.color.md_deep_orange_800),
                        getResources().getColor(R.color.md_deep_orange_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_brown_100),
                        getResources().getColor(R.color.md_brown_200),
                        getResources().getColor(R.color.md_brown_300),
                        getResources().getColor(R.color.md_brown_400),
                        getResources().getColor(R.color.md_brown_500),
                        getResources().getColor(R.color.md_brown_600),
                        getResources().getColor(R.color.md_brown_700),
                        getResources().getColor(R.color.md_brown_800),
                        getResources().getColor(R.color.md_brown_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_grey_100),
                        getResources().getColor(R.color.md_grey_200),
                        getResources().getColor(R.color.md_grey_300),
                        getResources().getColor(R.color.md_grey_400),
                        getResources().getColor(R.color.md_grey_500),
                        getResources().getColor(R.color.md_grey_600),
                        getResources().getColor(R.color.md_grey_700),
                        getResources().getColor(R.color.md_grey_800),
                        getResources().getColor(R.color.md_grey_900)
                },
                new int[]{
                        getResources().getColor(R.color.md_blue_grey_100),
                        getResources().getColor(R.color.md_blue_grey_200),
                        getResources().getColor(R.color.md_blue_grey_300),
                        getResources().getColor(R.color.md_blue_grey_400),
                        getResources().getColor(R.color.md_blue_grey_500),
                        getResources().getColor(R.color.md_blue_grey_600),
                        getResources().getColor(R.color.md_blue_grey_700),
                        getResources().getColor(R.color.md_blue_grey_800),
                        getResources().getColor(R.color.md_blue_grey_900)
                }
        };

    }

    private OverviewPagerAdapter adapter;

    private TabLayout tabs;


    private void setDataSet(List<String> data) {
        if (data != null) {
            usedArray = data;
            if (adapter == null) {
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            } else {
                adapter.notifyDataSetChanged();
            }
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(2);
            tabs.setupWithViewPager(pager);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(0)));
                SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(usedArray.get(0), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(usedArray.get(0))));

            }

            doSubSidebar(usedArray.get(0));
            findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            // hea.setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            tabs.setSelectedTabIndicatorColor(new ColorPreferences(SubredditOverview.this).getColor(usedArray.get(0)));

        }
        pager.setCurrentItem(toGoto);

    }

    private void openPopup(View view) {

        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.defaultSorting = Sorting.HOT;
                        reloadSubs();
                        break;
                    case 1:
                        Reddit.defaultSorting = Sorting.NEW;


                        reloadSubs();
                        break;
                    case 2:
                        Reddit.defaultSorting = Sorting.RISING;


                        reloadSubs();
                        break;
                    case 3:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.HOUR;


                        reloadSubs();
                        break;
                    case 4:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.DAY;


                        reloadSubs();
                        break;
                    case 5:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.WEEK;
                        reloadSubs();
                        break;
                    case 6:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.MONTH;

                        reloadSubs();
                        break;
                    case 7:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.YEAR;


                        reloadSubs();
                        break;
                    case 8:
                        Reddit.defaultSorting = Sorting.TOP;
                        Reddit.timePeriod = TimePeriod.ALL;


                        reloadSubs();
                        break;
                    case 9:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.HOUR;


                        reloadSubs();
                        break;
                    case 10:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.DAY;


                        reloadSubs();
                        break;
                }

                SettingValues.defaultSorting = Reddit.defaultSorting;
                SettingValues.timePeriod = Reddit.timePeriod;
            }
        };
        int i = Reddit.defaultSorting == Sorting.HOT ? 0
                : Reddit.defaultSorting == Sorting.NEW ? 1
                : Reddit.defaultSorting == Sorting.RISING ? 2
                : Reddit.defaultSorting == Sorting.TOP ?
                (Reddit.timePeriod == TimePeriod.HOUR ? 3
                        : Reddit.timePeriod == TimePeriod.DAY ? 4
                        : Reddit.timePeriod == TimePeriod.WEEK ? 5
                        : Reddit.timePeriod == TimePeriod.MONTH ? 6
                        : Reddit.timePeriod == TimePeriod.YEAR ? 7
                        : 8)
                : Reddit.defaultSorting == Sorting.CONTROVERSIAL ?
                (Reddit.timePeriod == TimePeriod.HOUR ? 9
                        : 10)
                : 0;
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(
                new String[]{
                        getString(R.string.sorting_hot),
                        getString(R.string.sorting_new),
                        getString(R.string.sorting_rising),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_hour),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_day),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_week),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_month),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_year),
                        getString(R.string.sorting_top) + " " + getString(R.string.sorting_all),
                        getString(R.string.sorting_controversial) + " " + getString(R.string.sorting_hour),
                        getString(R.string.sorting_controversial) + " " + getString(R.string.sorting_day),
                }, i, l2);
        builder.show();

    }

    private void restartTheme() {
        if (Reddit.single) {
            ((Reddit) getApplication()).startMain();

            finish();
        } else {
            Intent intent = this.getIntent();
            intent.putExtra("pageTo", pager.getCurrentItem());

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
            finish();
        }

    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment mCurrentFragment;

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    doSubSidebar(usedArray.get(position));
                    if (hea != null)
                        hea.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    header.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position)));
                        SubredditOverview.this.setTaskDescription(new ActivityManager.TaskDescription(usedArray.get(position), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(usedArray.get(position))));

                    }
                    tabs.setSelectedTabIndicatorColor(new ColorPreferences(SubredditOverview.this).getColor(usedArray.get(position)));

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new SubmissionsView();
            Bundle args = new Bundle();

            args.putString("id", usedArray.get(i));

            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.size();
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {

            if (usedArray != null) {
                return usedArray.get(position);
            } else {
                return "";
            }


        }
    }

    boolean restart;

    EditText e;

    private View hea;


    private void doSidebar() {
        final ListView l = (ListView) findViewById(R.id.drawerlistview);
        LayoutInflater inflater = getLayoutInflater();
        final View header;

        if (Authentication.isLoggedIn) {

            header = inflater.inflate(R.layout.drawer_loggedin, l, false);
            hea = header.findViewById(R.id.back);
            l.addHeaderView(header, null, false);
            ((TextView) header.findViewById(R.id.name)).setText(Authentication.name);
            header.findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, MultiredditOverview.class);
                    SubredditOverview.this.startActivity(inte);


                }
            });
            header.findViewById(R.id.reorder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, ListViewDraggingAnimation.class);
                    SubredditOverview.this.startActivityForResult(inte, 3);
                    subToDo = usedArray.get(pager.getCurrentItem());


                }
            });

            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Profile.class);
                    inte.putExtra("profile", Authentication.name);
                    SubredditOverview.this.startActivity(inte);
                }
            });

            header.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    chooseAccounts();
                }
            });
            header.findViewById(R.id.saved).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, SavedView.class);
                    inte.putExtra("where", "Saved");
                    inte.putExtra("id", Authentication.name);

                    SubredditOverview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, SavedView.class);
                    inte.putExtra("where", "Liked");
                    inte.putExtra("id", Authentication.name);

                    SubredditOverview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.prof_click).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View body = header.findViewById(R.id.expand_profile);
                    if (body.getVisibility() == View.GONE) {
                        body.setVisibility(View.VISIBLE);
                    } else {
                        body.setVisibility(View.GONE);
                    }
                }
            });
            header.findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Inbox.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
            Log.v("Slide", "2 MOD IS " + Authentication.mod);
            if (Authentication.mod) {
                header.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(SubredditOverview.this, ModQueue.class);
                        SubredditOverview.this.startActivity(inte);
                    }
                });

            } else {
                header.findViewById(R.id.mod).setVisibility(View.GONE);
            }
            header.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Submit.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Login.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
        } else {
            header = inflater.inflate(R.layout.drawer_loggedout, l, false);
            l.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);


            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Login.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
        }

        header.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte = new Intent(SubredditOverview.this, DonateView.class);

                SubredditOverview.this.startActivity(inte);
            }
        });
        e = ((EditText) header.findViewById(R.id.sort));


        e.setFilters(new InputFilter[]{new SubredditInputFilter()});

        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    Intent inte = new Intent(SubredditOverview.this, SubredditView.class);
                    inte.putExtra("subreddit", e.getText().toString());
                    SubredditOverview.this.startActivity(inte);
                }
                return false;
            }

        });
        header.findViewById(R.id.prof).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText input = new EditText(SubredditOverview.this);

                new AlertDialogWrapper.Builder(SubredditOverview.this)
                        .setTitle(R.string.user_enter)
                        .setView(input)
                        .setPositiveButton(R.string.user_btn_goto, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Editable value = input.getText();
                                if (!value.toString().matches("^[0-9a-zA-Z_-]+$")) {
                                    new AlertDialogWrapper.Builder(SubredditOverview.this)
                                            .setTitle(R.string.user_invalid)
                                            .setMessage(R.string.user_invalid_msg)
                                            .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                }
                                            }).show();
                                } else {
                                    Intent inte = new Intent(SubredditOverview.this, Profile.class);
                                    inte.putExtra("profile", value.toString());
                                    SubredditOverview.this.startActivity(inte);
                                }
                            }
                        }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

            }
        });
        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    Intent i = new Intent(SubredditOverview.this, Settings.class);
                    startActivityForResult(i, 1);
                }
            }
        });
        header.findViewById(R.id.tablet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                if (Reddit.tabletUI) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.tabletui, null);
                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SubredditOverview.this);

                    dialoglayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                    //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);
                    final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                    //todo  portrait.setBackgroundColor(Pallete.getDefaultColor());
                    landscape.setValue(Reddit.dpWidth, false);


                    final Dialog dialog = builder.setView(dialoglayout).create();
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Reddit.dpWidth = landscape.getValue();
                            Reddit.seen.edit().putInt("tabletOVERRIDE", landscape.getValue()).apply();

                            restartTheme();

                        }
                    });


                } else {
                    new AlertDialogWrapper.Builder(SubredditOverview.this)
                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
                }
            }
        });

      /*  footer.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte = new Intent(Overview.this, Setting.class);
                Overview.this.startActivityForResult(inte, 3);
            }
        });*/
        ArrayList<String> copy = new ArrayList<>();
        if (SubredditStorage.alphabeticalSubscriptions != null)
            for (String s : SubredditStorage.alphabeticalSubscriptions) {
                copy.add(s);
            }

        final SideArrayAdapter adapter = new SideArrayAdapter(this, copy);
        l.setAdapter(adapter);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.hello_world,
                R.string.hello_world
        )

        {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
                syncState();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };

        actionBarDrawerToggle.syncState();
        header.findViewById(R.id.back).setBackgroundColor(Pallete.getColor("alsdkfjasld"));
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = e.getText().toString().replaceAll(" ", "");
                adapter.getFilter().filter(result);

            }
        });


    }


}
