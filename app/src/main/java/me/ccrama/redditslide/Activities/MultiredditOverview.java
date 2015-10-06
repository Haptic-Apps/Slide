package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.MultiredditView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class MultiredditOverview extends BaseActivity {
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("", true).getBaseId(), true);

        setContentView(R.layout.activity_multireddits);
        StyleView.styleActivity(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Multireddits");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDarkerColor(Pallete.getDefaultColor())));
        }
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
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MultiredditOverview.this);
        builder.setTitle("Choose a Sorting Type");
        builder.setItems(
                new String[]{"Hot", "New", "Rising", "Top This Hour", "Top Today", "Top This Week", "Top This Month", "Top This Year", "Top All Time", "Controversial This Hour", "Controversial Today"}, l2);
        builder.show();

    }

    public void reloadSubs() {
        int current = pager.getCurrentItem();
        adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setCurrentItem(current);
    }
    public OverviewPagerAdapter adapter;

    public ViewPager pager;
    public TabLayout tabs;

    public List<MultiReddit> usedArray;

    public void setDataSet(List<MultiReddit> data) {
        usedArray = data;
        if(usedArray.size() == 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Multireddits (yet)!");
            builder.setMessage("For now, you will need to create new multireddits on Reddit.com. Coming soon will be in-app multireddit creation, though!");
            builder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
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
