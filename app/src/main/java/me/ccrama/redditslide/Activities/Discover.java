package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.SubredditListView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Discover extends BaseActivityAnim {

    public OverviewPagerAdapter adapter;
    private ViewPager pager;
    private TabLayout tabs;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_discover, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.search: {
                new MaterialDialog.Builder(Discover.this)
                        .alwaysCallInputCallback()
                        .inputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .inputRange(3, 100)
                        .input("Search term or topic", null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                if (input.length() >= 3) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                } else {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);

                                }
                            }
                        })
                        .positiveText(R.string.search_all)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent inte = new Intent(Discover.this, SubredditSearch.class);
                                inte.putExtra("term", dialog.getInputEditText().getText().toString());
                                Discover.this.startActivity(inte);
                            }
                        })
                        .negativeText(R.string.btn_cancel)
                        .show();
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
        setupAppBar(R.id.toolbar, "Discover", true, false);

        findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(Discover.this).getColor("no sub"));

        pager = (ViewPager) findViewById(R.id.content_view);
        pager.setAdapter(new OverviewPagerAdapter(getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);
    }


    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = new SubredditListView();
            Bundle args = new Bundle();
            args.putString("id", getPageTitle(i).toString());
            f.setArguments(args);

            return f;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "popular";
            } else {
                return "trending";
            }
        }
    }

}
