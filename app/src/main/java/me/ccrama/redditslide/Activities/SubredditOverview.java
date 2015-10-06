package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.ActiveTextView;
import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.DataShare;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.SubredditStorageNoContext;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.MakeTextviewClickable;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * Created by ccrama on 9/17/2015.
 */
public class SubredditOverview extends ActionBarActivity {
    Toolbar toolbar;

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
        }
    }

    String subToDo;

    public void resetAdapter() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usedArray = new ArrayList<>(SubredditStorage.subredditsForHome);

                adapter = new OverviewPagerAdapter(getSupportFragmentManager());

                pager.setAdapter(adapter);
                tabs.setupWithViewPager(pager);

                pager.setCurrentItem(usedArray.indexOf(subToDo));
            }
        });


    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    View header;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_overview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Slide");
        if(DataShare.notifs != null){

            final Submission s = DataShare.notifs;
            LayoutInflater inflater = getLayoutInflater();
            final View dialoglayout = inflater.inflate(R.layout.popupsubmission, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(SubredditOverview.this);
            new MakeTextviewClickable().ParseTextWithLinksTextView(s.getDataNode().get("selftext_html").asText(), (ActiveTextView) dialoglayout.findViewById(R.id.body), this, s.getSubredditName());

            ((TextView) dialoglayout.findViewById(R.id.title)).setText(s.getTitle());

            ImageView title = (ImageView) dialoglayout.findViewById(R.id.image);
            if (s.getDataNode().has("preview") && s.getDataNode().get("preview").get("images").get(0).get("source").has("height") && s.getDataNode().get("preview").get("images").get(0).get("source").get("height").asInt() > 200) {

                String url = s.getDataNode().get("preview").get("images").get(0).get("source").get("url").asText();
                Ion.with(title).load(url);
            } else {
                title.setVisibility(View.GONE);
            }
            ((TextView)dialoglayout.findViewById(R.id.info)).setText(TimeUtils.getTimeAgo(s.getCreatedUtc().getTime()));



           final AlertDialog dialog = builder.setView(dialoglayout).create();
            dialog.show();
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reddit.hidden.edit().putBoolean(s.getFullName(), true).apply();
                    dialog.dismiss();
                }
            });
        }
        if (!Reddit.colors.getBoolean("Tutorial", false)) {
            Intent i = new Intent(SubredditOverview.this, Tutorial.class);
            startActivity(i);
        }
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(Pallete.getDefaultColor())));
        }
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        header = findViewById(R.id.header);
        pager = (ViewPager) findViewById(R.id.contentView);

        setDataSet(SubredditStorage.subredditsForHome);
        doSidebar();

        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.choosetheme, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubredditOverview.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    final String subreddit = usedArray.get(pager.getCurrentItem());
                    title.setBackgroundColor(Pallete.getDefaultColor());

                    dialoglayout.findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = new ColorPreferences(SubredditOverview.this).getFontStyle().getTitle().split("_")[1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 2) {
                                    Reddit.themeBack = theme.getThemeType();
                                    new ColorPreferences(SubredditOverview.this).setFontStyle(theme);

                                    restartTheme();
                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = new ColorPreferences(SubredditOverview.this).getFontStyle().getTitle().split("_")[1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 1) {
                                    new ColorPreferences(SubredditOverview.this).setFontStyle(theme);
                                    Reddit.themeBack = theme.getThemeType();

                                    restartTheme();
                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = new ColorPreferences(SubredditOverview.this).getFontStyle().getTitle().split("_")[1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                    new ColorPreferences(SubredditOverview.this).setFontStyle(theme);
                                    Reddit.themeBack = theme.getThemeType();

                                    restartTheme();
                                    break;
                                }
                            }
                        }
                    });
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
                                title.setBackgroundColor(colorPicker2.getColor());


                                if (Pallete.getColor(subreddit) == Pallete.getDefaultColor()) {//is default
                                    hea.setBackgroundColor(colorPicker2.getColor());

                                    findViewById(R.id.header).setBackgroundColor(colorPicker2.getColor());

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Pallete.getDarkerColor(colorPicker2.getColor()));
                                    }
                                }
                            }
                        });


                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Reddit.colors.edit().putInt("DEFAULTCOLOR", colorPicker2.getColor()).apply();

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
                                        restartTheme();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }


                                }
                            });


                        }
                    }
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
                        for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                            if(type.getThemeType() == 0) {
                                arrs[i] = getResources().getColor(type.getColor());

                                i++;
                            }
                        }

                        colorPicker.setColors(arrs);
                        colorPicker.setSelectedColor(new ColorPreferences(SubredditOverview.this).getColor(subreddit));


                        {
                            TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok2);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int color = colorPicker.getColor();
                                    ColorPreferences.Theme t = null;
                                    for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                                        if(getResources().getColor(type.getColor()) == color  && Reddit.themeBack == type.getThemeType()){
                                            t = type;
                                            break;
                                        }
                                    }


                                    new ColorPreferences(SubredditOverview.this).setFontStyle(t);
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

                    dialoglayout.findViewById(R.id.editcards).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(SubredditOverview.this, EditCardsLayout.class);
                            startActivityForResult(i, 2);
                        }
                    });

                    builder.setView(dialoglayout);
                    builder.show();
                }
            }
        });
        findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });

        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    final String subreddit = usedArray.get(pager.getCurrentItem());

                    int style = new ColorPreferences(SubredditOverview.this).getThemeSubreddit(subreddit);
                    final Context contextThemeWrapper = new ContextThemeWrapper(SubredditOverview.this, style);
                    Log.v("Slide", "STYLE: " + style + " DEFAULT: " + R.style.deeporange_dark);
                    LayoutInflater localInflater = getLayoutInflater().cloneInContext(contextThemeWrapper);
                    final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubredditOverview.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setText("/r/" + subreddit);
                    title.setBackgroundColor(Pallete.getColor(subreddit));
                    CheckBox c = ((CheckBox) dialoglayout.findViewById(R.id.pinned));
                    c.setHighlightColor(new ColorPreferences(SubredditOverview.this).getThemeSubreddit(subreddit, true).getColor());
                    if (SubredditStorage.getPins() == null) {
                        c.setChecked(false);

                    } else if (SubredditStorage.getPins().contains(subreddit.toLowerCase())) {
                        c.setChecked(true);
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
                            for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                                if(type.getThemeType() == 0) {
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
                                        for(ColorPreferences.Theme type : ColorPreferences.Theme.values()){
                                            if(getResources().getColor(type.getColor()) == color  && Reddit.themeBack == type.getThemeType()){
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
                                        tabs.setSelectedTabIndicatorColor(new ColorPreferences(SubredditOverview.this).getColor(usedArray.get(0)));


                                    }
                                });


                            }
                        }
                    }
                    dialoglayout.findViewById(R.id.wiki).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(SubredditOverview.this, Wiki.class);
                            i.putExtra("subreddit", subreddit);
                            startActivity(i);
                        }
                    });
                    dialoglayout.findViewById(R.id.sidebar).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ShowPopupSidebar().execute(subreddit);
                        }
                    });
                    dialoglayout.findViewById(R.id.card).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(SubredditOverview.this, EditCardsLayout.class);
                            i.putExtra("subreddit", subreddit);
                            startActivityForResult(i, 1);
                        }
                    });
                    builder.setView(dialoglayout);
                    builder.show();
                }
            }
        });

    }

    public class ShowPopupSidebar extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            final String text = Authentication.reddit.getSubreddit(params[0]).getDataNode().get("description_html").asText();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.justtext, null);
                    AlertDialog.Builder builder = new AlertDialog.Builder(SubredditOverview.this);
                    final ActiveTextView body = (ActiveTextView) dialoglayout.findViewById(R.id.body);
                    new MakeTextviewClickable().ParseTextWithLinksTextView(text, body, SubredditOverview.this, "slideforreddit");

                    builder.setView(dialoglayout).show();

                }
            });
            return null;
        }
    }

    public int[] getColors(int c) {
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

    public OverviewPagerAdapter adapter;

    public ViewPager pager;
    public TabLayout tabs;

    public List<String> usedArray;

    public void setDataSet(List<String> data) {
        if(data != null) {
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
            }

            findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            // hea.setBackgroundColor(Pallete.getColor(usedArray.get(0)));
            tabs.setSelectedTabIndicatorColor(new ColorPreferences(SubredditOverview.this).getColor(usedArray.get(0)));

        }
    }

    public void openPopup(View view) {

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
                        //TODO WEEK


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
            }
        };
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SubredditOverview.this);
        builder.setTitle("Choose a Sorting Type");
        builder.setItems(
                new String[]{"Hot", "New", "Rising", "Top This Hour", "Top Today", "Top This Week", "Top This Month", "Top This Year", "Top All Time", "Controversial This Hour", "Controversial Today"}, l2);
        builder.show();

    }

    public void restartTheme() {
        Intent intent = this.getIntent();
        intent.putExtra("pageTo", pager.getCurrentItem());

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in_real, R.anim.fading_out_real);
        finish();

    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    hea.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    header.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position)));
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

            if(usedArray != null) {
                return usedArray.get(position);
            } else {
                return "";
            }


        }
    }


    public View hea;

    public DrawerLayout drawerLayout;

    public void doSidebar() {
        ListView l = (ListView) findViewById(R.id.drawerlistview);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.drawerbottom, l, false);
        l.addFooterView(footer, null, false);
        View header;

        if (Authentication.isLoggedIn) {

            header = (ViewGroup) inflater.inflate(R.layout.drawer_loggedin, l, false);
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
                    Authentication.authentication.edit().remove("lasttoken").apply();
                    ((Reddit) SubredditOverview.this.getApplicationContext()).restart();
                }
            });
            /*header.findViewById(R.id.saved).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, SavedView.class);
                    inte.putExtra("type", "Saved");
                    Overview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, SavedView.class);
                    inte.putExtra("type", "Liked");

                    Overview.this.startActivity(inte);
                }
            });*/
            header.findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Inbox.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(SubredditOverview.this, Submit.class);
                    SubredditOverview.this.startActivity(inte);
                }
            });
           /* footer.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, DonateView.class);

                    Overview.this.startActivity(inte);
                }
            });*/
        } else {
            header = (ViewGroup) inflater.inflate(R.layout.drawer_loggedout, l, false);
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
        final EditText e = ((EditText) header.findViewById(R.id.sort));
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

                new android.support.v7.app.AlertDialog.Builder(SubredditOverview.this)
                        .setTitle("Enter Username")
                        .setView(input)
                        .setPositiveButton("Go to user", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Editable value = input.getText();
                                Intent inte = new Intent(SubredditOverview.this, Profile.class);
                                inte.putExtra("profile", value.toString());
                                SubredditOverview.this.startActivity(inte);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

            }
        });
        footer.findViewById(R.id.tablet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                if (Reddit.tabletUI) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.tabletui, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SubredditOverview.this);

                    dialoglayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                   final CheckBox cb = (CheckBox) dialoglayout.findViewById(R.id.default_click);
                   final View elseView = dialoglayout.findViewById(R.id.override);

                    final EditText editOverride = (EditText) dialoglayout.findViewById(R.id.overrideText);
                    if(Reddit.dpWidth == Reddit.defaultDPWidth){
                        cb.setChecked(true);
                        elseView.setAlpha(0.25f);
                        editOverride.setInputType(InputType.TYPE_NULL);
                    } else {
                        cb.setChecked(false);
                        elseView.setAlpha(1f);
                    }


                    editOverride.setText(String.valueOf(Reddit.dpWidth));

                    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                elseView.setAlpha(0.25f);
                                editOverride.setInputType(InputType.TYPE_NULL);
                                editOverride.setText(String.valueOf(Reddit.defaultDPWidth));

                            } else {
                                editOverride.setInputType(InputType.TYPE_CLASS_NUMBER);
                                elseView.setAlpha(1f);
                                editOverride.requestFocus();


                            }
                        }
                    });



                    final AlertDialog dialog = builder.setView(dialoglayout).create();
                    dialog.show();
                    dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cb.isChecked() || editOverride.getText().toString().isEmpty()) {
                                Reddit.dpWidth = Reddit.defaultDPWidth;
                                Reddit.seen.edit().putInt("tabletOVERRIDE", Reddit.dpWidth).apply();
                                dialog.dismiss();
                                restartTheme();
                            } else {
                                Integer in = Integer.valueOf(editOverride.getText().toString() );
                                if(in > 0 && in < 8) {
                                    Reddit.seen.edit().putInt("tabletOVERRIDE", Integer.valueOf(editOverride.getText().toString())).apply();
                                    dialog.dismiss();
                                    Reddit.dpWidth = in;

                                    restartTheme();
                                } else {
                                    new AlertDialog.Builder(SubredditOverview.this).setTitle("Invalid Amount of Columns").setMessage("You must enter a number between 1 and 7!").create().show();
                                }

                            }

                        }
                    });


                } else {
                    new android.support.v7.app.AlertDialog.Builder(SubredditOverview.this)
                            .setTitle("Unlock Grid Layout")
                            .setMessage("I have opted to make Multi-Column a paid feature of Slide for Reddit. I am a student developer, and can't keep up the pace of development if I have to get a supplementary job to support myself. This Multi-Column is in lieu of ads or locking already unlocked content, and the app will function normally without purchasing it!\n\nWould you like to unlock Multi-Column?")
                            .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton("No thank you", new DialogInterface.OnClickListener() {
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
        ArrayList<String> copy = new ArrayList<String>();
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
