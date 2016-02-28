package me.ccrama.redditslide.Activities;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import me.ccrama.redditslide.Fragments.MultiredditView;
import me.ccrama.redditslide.Fragments.SubmissionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Views.PreCachingLayoutManager;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

/**
 * Created by ccrama on 9/17/2015.
 */
public class MultiredditOverview extends BaseActivityAnim {

    public OverviewPagerAdapter adapter;
    private ViewPager pager;
    private TabLayout tabs;
    private List<MultiReddit> usedArray;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_multireddits, menu);

        //   if (mShowInfoButton) menu.findItem(R.id.action_info).setVisible(true);
        //   else menu.findItem(R.id.action_info).setVisible(false);

        return true;
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /* removed for now
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                return ((SubmissionsView) adapter.getCurrentFragment()).onKeyDown(keyCode);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return ((SubmissionsView) adapter.getCurrentFragment()).onKeyDown(keyCode);
            default:
                return super.dispatchKeyEvent(event);
        }*/
        return super.dispatchKeyEvent(event);
    }
    public int getCurrentPage() {
        int position = 0;
        int currentOrientation = getResources().getConfiguration().orientation;
        if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof LinearLayoutManager && currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            position = ((LinearLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPosition() - 1;
        } else if (((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            int[] firstVisibleItems = null;
            firstVisibleItems = ((StaggeredGridLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPositions(firstVisibleItems);
            if (firstVisibleItems != null && firstVisibleItems.length > 0) {
                position = firstVisibleItems[0] - 1;
            }
        } else {
            position = ((PreCachingLayoutManager) ((SubmissionsView) adapter.getCurrentFragment()).rv.getLayoutManager()).findFirstVisibleItemPosition() -1;
        }
        return position;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                Intent i = new Intent(MultiredditOverview.this, CreateMulti.class);
                i.putExtra(CreateMulti.EXTRA_MULTI, SubredditStorage.getMultireddits().get(pager.getCurrentItem()).getDisplayName());
                startActivity(i);
            }
                return true;
            case R.id.create:
                Intent i2 = new Intent(MultiredditOverview.this, CreateMulti.class);
                startActivity(i2);
                return true;
            case R.id.action_sort:
                openPopup();
                return true;

            case R.id.action_shadowbox:
                if (SettingValues.tabletUI) {
                    List<Submission> posts = ((MultiredditView) adapter.getCurrentFragment()).posts.posts;
                    if (posts != null && !posts.isEmpty()) {
                        Intent i = new Intent(this, Shadowbox.class);
                        i.putExtra(Shadowbox.EXTRA_PAGE, getCurrentPage());
                        i.putExtra(Shadowbox.EXTRA_MULTIREDDIT, ((MultiredditView) adapter.getCurrentFragment()).posts.getMultiReddit().getDisplayName());
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
            default:
                return false;
        }
    }
    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme("");
        setContentView(R.layout.activity_multireddits);
        setupAppBar(R.id.toolbar, R.string.title_multireddits, true, false);

        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        pager = (ViewPager) findViewById(R.id.content_view);



        new AsyncTask<Void, Void, List<MultiReddit>>() {
            @Override
            protected List<MultiReddit> doInBackground(Void... params) {
                return SubredditStorage.getMultireddits();
            }

            @Override
            protected void onPostExecute(List<MultiReddit> multiReddits) {
                setDataSet(multiReddits);
            }
        }.execute();
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
        try {
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
                builder.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
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
                pager.setOffscreenPageLimit(1);
                tabs.setupWithViewPager(pager);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = this.getWindow();
                    window.setStatusBarColor(Palette.getDarkerColor(usedArray.get(0).getDisplayName()));
                }

                findViewById(R.id.header).setBackgroundColor(Palette.getColor(usedArray.get(0).getDisplayName()));
            }
        } catch (NullPointerException e) {
            AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(this);
            builder.setTitle(R.string.err_title);
            builder.setMessage(R.string.err_loading_content);
            builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
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
            Log.e(LogUtil.getTag(), "Cannot load multis:\n" + e);
        }

    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    findViewById(R.id.header).animate()
                            .translationY(0)
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180);
                    findViewById(R.id.header).setBackgroundColor(Palette.getColor(usedArray.get(position).getDisplayName()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(Palette.getDarkerColor(usedArray.get(position).getDisplayName()));
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
            return usedArray.get(position).getFullName();
        }
    }

}
