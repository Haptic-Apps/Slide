package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.DragSort.ListViewDraggingAnimation;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageFromContext;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Views.ToggleSwipeViewPager;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

public class MainActivity extends AppCompatActivity {
    // Instance state keys
    static final String SUBS = "subscriptions";
    static final String SUBS_ALPHA = "subscriptionsAlpha";
    static final String REAL_SUBS = "realSubscriptions";
    static final String LOGGED_IN = "loggedIn";
    static final String USERNAME = "username";

    public boolean singleMode;
    public ToggleSwipeViewPager pager;
    public List<String> usedArray;
    public DrawerLayout drawerLayout;
    public View hea;
    public EditText e;
    public View header;
    public String subToDo;
    public OverviewPagerAdapter adapter;
    private TabLayout mTabLayout;
    public int toGoto = 0;
    public boolean first = true;
    private boolean mShowInfoButton;

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
            new SubredditStorageNoContext().execute(this);
        } else if (requestCode == 4 && resultCode != 4) {
            if (e != null) {
                e.clearFocus();
                e.setText("");
                drawerLayout.closeDrawers();
            }
        }
    }
    boolean changed;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
          changed = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            changed = true;
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null   && !changed) {

            SubredditStorage.subredditsForHome = savedInstanceState.getStringArrayList(SUBS);
            SubredditStorage.alphabeticalSubscriptions =
                    savedInstanceState.getStringArrayList(SUBS_ALPHA);
            SubredditStorage.realSubs = savedInstanceState.getStringArrayList(REAL_SUBS);
            Authentication.isLoggedIn = savedInstanceState.getBoolean(LOGGED_IN);
            Authentication.name = savedInstanceState.getString(USERNAME);
        }

        if (getIntent().getBooleanExtra("EXIT", false)) finish();

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_overview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String base = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().toLowerCase();
                int number;
                if(base.contains("black") || base.contains("amoled")) {
                    number = 1;
                } else {
                    number = 2;
                }
                    String name = new ColorPreferences(MainActivity.this).getFontStyle().getTitle().split("_")[1];
                    final String newName = name.replace("(", "");
                    for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                        if (theme.toString().contains(newName) && theme.getThemeType() == number) {
                            Reddit.themeBack = theme.getThemeType();
                            new ColorPreferences(MainActivity.this).setFontStyle(theme);
                            recreate();
                            break;
                        }
                    }




            }
        });

        if (getIntent() != null && getIntent().hasExtra("pageTo"))
            toGoto = getIntent().getIntExtra("pageTo", 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(Pallete.getDefaultColor())));
        }

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        header = findViewById(R.id.header);
        pager = (ToggleSwipeViewPager) findViewById(R.id.content_view);

        singleMode = Reddit.single;
        // Inflate tabs if single mode is disabled
        if (!singleMode) mTabLayout = (TabLayout) ((ViewStub) findViewById(R.id.stub_tabs)).inflate();
        // Disable swiping if single mode is enabled
        if (singleMode) pager.setSwipingEnabled(false);

        setDataSet(SubredditStorage.subredditsForHome);
        doSidebar();
    }

    @Override
    public void onPause(){
        super.onPause();
        changed = false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList(SUBS, SubredditStorage.subredditsForHome);
        savedInstanceState.putStringArrayList(SUBS_ALPHA, SubredditStorage.alphabeticalSubscriptions);
        savedInstanceState.putStringArrayList(REAL_SUBS, SubredditStorage.realSubs);
        savedInstanceState.putBoolean(LOGGED_IN, Authentication.isLoggedIn);
        savedInstanceState.putString(USERNAME, Authentication.name);
    }

    public void doSubSidebar(final String subreddit) {
        if (!subreddit.equals("all") && !subreddit.equals("frontpage")) {
            if (drawerLayout != null) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
            }

            mShowInfoButton = true;
            invalidateOptionsMenu();

            new AsyncGetSubreddit().execute(subreddit);
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.sidebar_text).setVisibility(View.GONE);
            findViewById(R.id.sub_title).setVisibility(View.GONE);
            findViewById(R.id.subscribers).setVisibility(View.GONE);

            findViewById(R.id.header_sub).setBackgroundColor(Pallete.getColor(subreddit));
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
                if (Reddit.fab && Reddit.fabType == R.integer.FAB_POST)
                    submit.setVisibility(View.GONE);

                pinned.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        //reset check adapter
                    }
                });
                if (SubredditStorage.getPins() == null) {
                    pinned.setChecked(false);

                } else if (SubredditStorage.getPins().contains(subreddit.toLowerCase())) {
                    pinned.setChecked(true);
                } else {
                    pinned.setChecked(false);
                }
                pinned.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        if (isChecked) {
                            SubredditStorage.addPin(subreddit);
                        } else {
                            SubredditStorage.removePin(subreddit);
                        }
                        subToDo = subreddit;
                        new SubredditStorageNoContext().execute(MainActivity.this);
                    }
                });
                pinned.setHighlightColor(new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit, true).getColor());

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, Submit.class);
                        inte.putExtra("subreddit", subreddit);
                        MainActivity.this.startActivity(inte);
                    }
                });
            }


            if (subreddit.toLowerCase().equals("frontpage") || subreddit.toLowerCase().equals("all")) {
                dialoglayout.findViewById(R.id.wiki).setVisibility(View.GONE);
                dialoglayout.findViewById(R.id.sidebar_text).setVisibility(View.GONE);

            } else {
                dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(MainActivity.this, Wiki.class);
                        i.putExtra("subreddit", subreddit);
                        startActivity(i);
                    }
                });

            }
            findViewById(R.id.wiki).setBackgroundColor(Pallete.getColor(subreddit));
            dialoglayout.findViewById(R.id.submit).setBackgroundColor(Pallete.getColor(subreddit));

            findViewById(R.id.sub_theme).setBackgroundColor(Pallete.getColor(subreddit));
            findViewById(R.id.sub_theme).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String subreddit = usedArray.get(pager.getCurrentItem());

                    int style = new ColorPreferences(MainActivity.this).getThemeSubreddit(subreddit);
                    final Context contextThemeWrapper = new ContextThemeWrapper(MainActivity.this, style);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
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
                            for (int i2 : ColorPreferences.getColors(getBaseContext(), i)) {
                                if (i2 == currentColor) {
                                    colorPicker.setSelectedColor(i);
                                    colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), i));
                                    colorPicker2.setSelectedColor(i2);
                                    break;
                                }
                            }
                        }
                        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int c) {

                                colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), c));
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
                                    MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

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
                                        MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

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

                            colorPicker.setSelectedColor(new ColorPreferences(MainActivity.this).getFontStyle().getColor());

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

                                        new ColorPreferences(MainActivity.this).setFontStyle(t, subreddit);

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
                            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
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
            mShowInfoButton = false;
            invalidateOptionsMenu();

            if (drawerLayout != null)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
        }
    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    public void setDataSet(List<String> data) {

        if (data != null) {

            usedArray = data;
            if (adapter == null) {
                adapter = new OverviewPagerAdapter(getSupportFragmentManager());
            } else {
                adapter.notifyDataSetChanged();
            }
            pager.setCurrentItem(1);
            pager.setAdapter(adapter);
            pager.setOffscreenPageLimit(2);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(0)));
                MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(usedArray.get(0), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(usedArray.get(0))));

            }
                doSubSidebar(usedArray.get(0));
            findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            // hea.setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            if (!Reddit.single) {
                mTabLayout.setupWithViewPager(pager);
                mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(usedArray.get(0)));
                pager.setCurrentItem(toGoto);
            } else {
                getSupportActionBar().setTitle(usedArray.get(0));
            }

        } else if (SubredditStorage.subredditsForHome != null) {
            setDataSet(SubredditStorage.subredditsForHome);
        }

    }

    public void doSubOnlyStuff(final Subreddit subreddit) {
        findViewById(R.id.loader).setVisibility(View.GONE);
        if (subreddit.getSidebar() != null && !subreddit.getSidebar().isEmpty()) {
            findViewById(R.id.sidebar_text).setVisibility(View.VISIBLE);

            final String text = subreddit.getDataNode().get("description_html").asText();
            final ActiveTextView body = (ActiveTextView) findViewById(R.id.sidebar_text);
            new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, MainActivity.this, subreddit.getDisplayName());
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
                        public void onPostExecute(Void voids) {
                            new SubredditStorageNoContext().execute(MainActivity.this);
                            Snackbar.make(header, isChecked ?
                                    getString(R.string.misc_subscribed) : getString(R.string.misc_unsubscribed), Snackbar.LENGTH_SHORT);
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
        ((TextView) findViewById(R.id.sub_title)).setText(subreddit.getPublicDescription());
        findViewById(R.id.sub_title).setVisibility(subreddit.getPublicDescription().equals("") ? View.GONE : View.VISIBLE);

        ((TextView) findViewById(R.id.subscribers)).setText(getString(R.string.subreddit_subscribers, subreddit.getSubscriberCount()));
        findViewById(R.id.subscribers).setVisibility(View.VISIBLE);

    }

    public void openPopup() {

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
                    case 11:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.WEEK;
                        reloadSubs();
                    case 12:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.MONTH;
                        reloadSubs();
                    case 13:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.YEAR;
                        reloadSubs();
                    case 14:
                        Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                        Reddit.timePeriod = TimePeriod.ALL;
                        reloadSubs();
                }
                SettingValues.defaultSorting = Reddit.defaultSorting;
                SettingValues.timePeriod = Reddit.timePeriod;
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
        builder.show();

    }

    public void doSidebar() {
        final ListView l = (ListView) findViewById(R.id.drawerlistview);
        l.setDividerHeight(0);
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
                    Intent inte = new Intent(MainActivity.this, MultiredditOverview.class);
                    MainActivity.this.startActivity(inte);


                }
            });
            header.findViewById(R.id.reorder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, ListViewDraggingAnimation.class);
                    MainActivity.this.startActivityForResult(inte, 3);
                    subToDo = usedArray.get(pager.getCurrentItem());


                }
            });

            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Profile.class);
                    inte.putExtra("profile", Authentication.name);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog d = new MaterialDialog.Builder(MainActivity.this).title(R.string.general_sub_sync)
                            .progress(true, 100)
                            .cancelable(false).show();
                    new SubredditStorageFromContext(MainActivity.this, d).execute((Reddit) getApplication());
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
                    Intent inte = new Intent(MainActivity.this, SavedView.class);
                    inte.putExtra("where", "Saved");
                    inte.putExtra("id", Authentication.name);

                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, SavedView.class);
                    inte.putExtra("where", "Liked");
                    inte.putExtra("id", Authentication.name);

                    MainActivity.this.startActivity(inte);
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
            header.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Inbox.class);
                    MainActivity.this.startActivity(inte);
                }
            });
            if (Authentication.mod) {
                header.findViewById(R.id.mod).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, ModQueue.class);
                        MainActivity.this.startActivity(inte);
                    }
                });
            } else {
                header.findViewById(R.id.mod).setVisibility(View.GONE);
            }
            if (Reddit.fab && Reddit.fabType == R.integer.FAB_POST)
                header.findViewById(R.id.submit).setVisibility(View.GONE);
            else {
                header.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent inte = new Intent(MainActivity.this, Submit.class);
                        MainActivity.this.startActivity(inte);
                    }
                });
            }

        } else {
            header = inflater.inflate(R.layout.drawer_loggedout, l, false);
            l.addHeaderView(header, null, false);
            hea = header.findViewById(R.id.back);


            header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, Login.class);
                    MainActivity.this.startActivity(inte);
                }
            });
        }

        View support = header.findViewById(R.id.support);
        if (Reddit.tabletUI) support.setVisibility(View.GONE);
        else {
            header.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(MainActivity.this, DonateView.class);
                    MainActivity.this.startActivity(inte);
                }
            });
        }

        e = ((EditText) header.findViewById(R.id.sort));

        e.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                    Intent inte = new Intent(MainActivity.this, SubredditView.class);
                    inte.putExtra("subreddit", e.getText().toString());
                    MainActivity.this.startActivity(inte);
                    drawerLayout.closeDrawers();
                    e.setText("");
                }
                return false;
            }

        });
            header.findViewById(R.id.prof).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                    input.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                input.setText(String.valueOf(input.getText()).replace(" ", ""));
                                Editable value = input.getText();
                                if (!value.toString().matches("^[0-9a-zA-Z_-]+$")) {
                                    new AlertDialogWrapper.Builder(MainActivity.this)
                                            .setTitle(R.string.user_invalid)
                                            .setMessage(R.string.user_invalid_msg)
                                            .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                }
                                            }).show();
                                } else {
                                    Intent inte = new Intent(MainActivity.this, Profile.class);
                                    inte.putExtra("profile", value.toString());
                                    MainActivity.this.startActivity(inte);
                                }
                                return true;
                            }
                            return false;
                        }
                    });
                    new AlertDialogWrapper.Builder(MainActivity.this)
                            .setTitle(R.string.user_enter)
                            .setView(input)
                            .setPositiveButton(R.string.user_btn_goto, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable value = input.getText();
                                    if (!value.toString().matches("^[0-9a-zA-Z_-]+$")) {
                                        new AlertDialogWrapper.Builder(MainActivity.this)
                                                .setTitle(R.string.user_invalid)
                                                .setMessage(R.string.user_invalid_msg)
                                                .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                    }
                                                }).show();
                                    } else {
                                        Intent inte = new Intent(MainActivity.this, Profile.class);
                                        inte.putExtra("profile", value.toString());
                                        MainActivity.this.startActivity(inte);
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
                Intent i = new Intent(MainActivity.this, Settings.class);
                startActivityForResult(i, 1);
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    public void chooseAccounts() {
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
        new AlertDialogWrapper.Builder(MainActivity.this)
                .setTitle(R.string.general_switch_acc)
                .setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, accounts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (names.get(which).contains(":")) {
                            String token = names.get(which).split(":")[1];
                            Authentication.authentication.edit().putString("lasttoken", token).commit();
                            Reddit.appRestart.edit().remove("back").commit();

                        } else {

                            ArrayList<String> tokens = new ArrayList<>(Authentication.authentication.getStringSet("tokens", new HashSet<String>()));
                            Authentication.authentication.edit().putString("lasttoken", tokens.get(which)).commit();
                            Reddit.appRestart.edit().remove("back").commit();


                        }

                        Reddit.forceRestart(MainActivity.this);

                    }
                }).create().show();
    }

    public void resetAdapter() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usedArray = new ArrayList<>(SubredditStorage.subredditsForHome);

                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                pager.setAdapter(adapter);
                mTabLayout.setupWithViewPager(pager);

                pager.setCurrentItem(usedArray.indexOf(subToDo));

                int color = Pallete.getColor(subToDo);
                hea.setBackgroundColor(color);
                findViewById(R.id.header).setBackgroundColor(color);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.setStatusBarColor(Pallete.getDarkerColor(color));
                    MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(subToDo, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), color));
                }
            }
        });
    }

    public void restartTheme() {
        Intent intent = this.getIntent();
        intent.putExtra("pageTo", pager.getCurrentItem());
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
        finish();
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT) || drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawers();
        } else if (Reddit.exit) {
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MainActivity.this);
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

    public class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

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



    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {
        private Fragment mCurrentFragment;

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Reddit.currentPosition = position;

                        doSubSidebar(usedArray.get(position));


                    if(adapter.getCurrentFragment() != null){
                        SubredditPosts p = ((SubmissionsView) adapter.getCurrentFragment()).adapter.dataSet;
                        if(p.offline){
                                Toast.makeText(MainActivity.this, "Last updated " + TimeUtils.getTimeAgo(p.cached.time, MainActivity.this), Toast.LENGTH_LONG).show();



                        }
                    }

                    if (Reddit.single) {
                        hea.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                        header.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position)));
                            MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(usedArray.get(position), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(usedArray.get(position))));

                        }
                        getSupportActionBar().setTitle(usedArray.get(position));
                    } else {

                        if (hea != null)
                            hea.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                        header.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position)));
                            MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(usedArray.get(position), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getColor(usedArray.get(position))));

                        }
                        mTabLayout.setSelectedTabIndicatorColor(new ColorPreferences(MainActivity.this).getColor(usedArray.get(position)));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            pager.setCurrentItem(1);
            pager.setCurrentItem(0);
        }


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

        @Override
        public Fragment getItem(int i) {

            SubmissionsView f = new SubmissionsView();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_subreddit_overview, menu);

        if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shadowbox:
                if (Reddit.tabletUI) {
                    ArrayList<Submission> posts =
                            ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        DataShare.sharedSubreddit =
                                ((SubmissionsView) adapter.getCurrentFragment()).posts.posts;
                        Intent i = new Intent(this, Shadowbox.class);
                        i.putExtra("position", pager.getCurrentItem());
                        startActivity(i);
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
            case R.id.action_info:
                if (usedArray != null) {
                    String sub = usedArray.get(pager.getCurrentItem());
                    if (!sub.equals("frontpage") && !sub.equals("all")) {
                        ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer
                                (GravityCompat.END);
                    }
                }
                return true;
            case R.id.action_sort:
                openPopup();
                return true;
            default:
                return false;
        }
    }
}
