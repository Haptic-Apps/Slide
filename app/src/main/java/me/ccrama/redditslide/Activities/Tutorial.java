package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 3/5/2015.
 */

public class Tutorial extends AppCompatActivity {
    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getCommentFontStyle().getResId(), true);
        getTheme().applyStyle(new FontPreferences(this).getPostFontStyle().getResId(), true);

        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);

        setContentView(R.layout.activity_tutorial);


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.vp);
        /*
      The pager adapter, which provides the pages to the view pager widget.
     */
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        if(getIntent().hasExtra("page")){
            mPager.setCurrentItem(1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Palette.getDarkerColor(Color.parseColor("#FF5252")));
        }

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

    public class Welcome extends Fragment {
        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.fragment_welcome, container, false);
            v.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPager.setCurrentItem(1);
                }
            });

            return v;
        }

    }
    int back;

    public class Personalize extends Fragment {
        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            back = new ColorPreferences(getContext()).getFontStyle().getThemeType();

            View v = inflater.inflate(R.layout.fragment_basicinfo, container, false);
            final View header = v.findViewById(R.id.header);

            ((ImageView)v.findViewById(R.id.tint_accent)).setColorFilter(Tutorial.this.getResources().getColor(new ColorPreferences(Tutorial.this).getFontStyle().getColor()));
            ((ImageView) v.findViewById(R.id.tint_primary)).setColorFilter(Palette.getDefaultColor());
            header.setBackgroundColor(Palette.getDefaultColor());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.setStatusBarColor(Palette.getDarkerColor(Palette.getDefaultColor()));
            }
            v.findViewById(R.id.primary).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LayoutInflater inflater = Tutorial.this.getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.choosemain, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Tutorial.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setBackgroundColor(Palette.getDefaultColor());


                    LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
                    final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

                    colorPicker.setColors(ColorPreferences.getBaseColors(Tutorial.this));
                    int currentColor = Palette.getDefaultColor();
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
                            title.setBackgroundColor(colorPicker2.getColor());
                            header.setBackgroundColor(colorPicker2.getColor());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                Window window = getWindow();
                                window.setStatusBarColor(Palette.getDarkerColor(colorPicker2.getColor()));
                            }

                        }
                    });


                    dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Reddit.colors.edit().putInt("DEFAULTCOLOR", colorPicker2.getColor()).apply();
                            Intent i = new Intent(Tutorial.this, Tutorial.class);
                            i.putExtra("page", 1);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(i);
                            overridePendingTransition(0, 0);

                            finish();
                            overridePendingTransition(0, 0);

                        }
                    });

                    builder.setView(dialoglayout);
                    builder.show();
                }
            });
            v.findViewById(R.id.secondary).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = Tutorial.this.getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.chooseaccent, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Tutorial.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setBackgroundColor(Palette.getDefaultColor());

                    final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

                    int[] arrs = new int[ColorPreferences.Theme.values().length / 7];
                    int i = 0;
                    for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                        if (type.getThemeType() == 0) {
                            arrs[i] = ContextCompat.getColor(Tutorial.this, type.getColor());

                            i++;
                        }
                    }

                    colorPicker.setColors(arrs);
                    colorPicker.setSelectedColor(new ColorPreferences(Tutorial.this).getColor(""));


                    dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int color = colorPicker.getColor();
                            ColorPreferences.Theme t = null;
                            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                if (ContextCompat.getColor(Tutorial.this, type.getColor()) == color && back == type.getThemeType()) {
                                    t = type;
                                    break;
                                }
                            }


                            new ColorPreferences(Tutorial.this).setFontStyle(t);

                            Intent i = new Intent(Tutorial.this, Tutorial.class);
                            i.putExtra("page", 1);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(i);
                            overridePendingTransition(0, 0);

                            finish();
                            overridePendingTransition(0, 0);


                        }
                    });


                    builder.setView(dialoglayout);
                    builder.show();
                }
            });
            v.findViewById(R.id.base).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = Tutorial.this.getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Tutorial.this);
                    final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                    title.setBackgroundColor(Palette.getDefaultColor());

                    dialoglayout.findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 2) {
                                    back = theme.getThemeType();
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.light).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 1) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.dark).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.blacklighter).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 4) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.sepia).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 5) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.sepia).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 6) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);

                                    break;
                                }
                            }
                        }
                    });
                    dialoglayout.findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] names = new ColorPreferences(Tutorial.this).getFontStyle().getTitle().split("_");
                            String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName) && theme.getThemeType() == 3) {
                                    new ColorPreferences(Tutorial.this).setFontStyle(theme);
                                    back = theme.getThemeType();

                                    Intent i = new Intent(Tutorial.this, Tutorial.class);
                                    i.putExtra("page", 1);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(i);
                                    overridePendingTransition(0, 0);

                                    finish();
                                    overridePendingTransition(0, 0);
                                    break;
                                }
                            }
                        }
                    });

                    builder.setView(dialoglayout);
                    builder.show();
                }
            });
            v.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Reddit.colors.edit().putString("Tutorial", "S").commit();
                    Reddit.appRestart.edit().putString("startScreen", "a").apply();
                    Reddit.forceRestart(Tutorial.this);
                }
            });
            return v;
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
            if (position == 0) {
                return new Welcome();
            } else  {
                return new Personalize();
            }

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}