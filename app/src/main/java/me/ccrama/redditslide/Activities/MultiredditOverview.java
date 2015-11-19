package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.MultiredditView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditOverview extends BaseActivity {

    public OverviewPagerAdapter adapter;
    private ViewPager pager;
    private TabLayout tabs;
    private List<MultiReddit> usedArray;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        setContentView(R.layout.activity_multireddits);
        StyleView.styleActivity(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_multireddits);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(Pallete.getDefaultColor())));
        }
        findViewById(R.id.header).setBackgroundColor(Pallete.getDefaultColor());
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        pager = (ViewPager) findViewById(R.id.contentView);
        findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    openPopup(v);
                }
            }
        });
        findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                i.putExtra("multi", SubredditStorage.multireddits.get(pager.getCurrentItem()).getDisplayName());
                startActivity(i);
            }
        });
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                startActivity(i);
            }
        });

        setDataSet(SubredditStorage.multireddits);


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

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(MultiredditOverview.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
        builder.show();


    }

    private void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }

    private void setDataSet(List<MultiReddit> data) {
        usedArray = data;
        if (usedArray.size() == 0) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);
            builder.setTitle(R.string.multireddit_err_title);
            builder.setMessage(R.string.multireddit_err_msg);
            builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                    startActivity(i);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                }
            });
            builder.create().show();

        } else {

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
                window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(0).getDisplayName()));
            }

            findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(0).getDisplayName()));
        }

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
                    findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(position).getDisplayName()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position).getDisplayName()));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new MultiredditView();
            Bundle args = new Bundle();

            args.putInt("id", i);

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
            return usedArray.get(position).getDisplayName();
        }
    }


}
