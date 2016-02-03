package me.ccrama.redditslide.Activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import me.ccrama.redditslide.Fragments.Agreement;
import me.ccrama.redditslide.Fragments.TutorialFragment;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */

public class Tutorial extends FragmentActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 9;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.vp);
        /*
      The pager adapter, which provides the pages to the view pager widget.
     */
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        findViewById(R.id.agreement).setVisibility(View.GONE);

        findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Reddit.colors.edit().putBoolean("Tutorial", true).apply();
               Reddit.forceRestart(Tutorial.this, true);
            }
        });
        findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(9);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Palette.getDarkerColor(Color.parseColor("#FF5252")));
        }
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        mPager.setCurrentItem(0);
                        break;

                    case R.id.radioButton2:
                        mPager.setCurrentItem(1);
                        break;

                    case R.id.radioButton3:
                        mPager.setCurrentItem(2);
                        break;

                    case R.id.radioButton4:
                        mPager.setCurrentItem(3);
                        break;

                    case R.id.radioButton5:
                        mPager.setCurrentItem(4);
                        break;

                    case R.id.radioButton6:
                        mPager.setCurrentItem(5);
                        break;

                    case R.id.radioButton7:
                        mPager.setCurrentItem(6);
                        break;

                    case R.id.radioButton8:
                        mPager.setCurrentItem(7);
                        break;
                }
            }
        });
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position < 10) {
                    findViewById(R.id.agreement).setVisibility(View.GONE);
                    findViewById(R.id.menu).setVisibility(View.VISIBLE);
                }
                switch (position) {
                    case 0:
                        radioGroup.check(R.id.radioButton);
                        break;
                    case 1:
                        radioGroup.check(R.id.radioButton2);
                        break;
                    case 2:
                        radioGroup.check(R.id.radioButton3);
                        break;
                    case 3:
                        radioGroup.check(R.id.radioButton4);
                        break;
                    case 4:
                        radioGroup.check(R.id.radioButton5);
                        break;
                    case 5:
                        radioGroup.check(R.id.radioButton6);
                        break;
                    case 6:
                        radioGroup.check(R.id.radioButton7);
                        break;
                    case 7:
                        radioGroup.check(R.id.radioButton8);
                        break;


                    case 8:
                        findViewById(R.id.agreement).setVisibility(View.VISIBLE);
                        findViewById(R.id.menu).setVisibility(View.GONE);


                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position < 8) {
                return TutorialFragment.newInstance(position);
            } else {
                return new Agreement();
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}