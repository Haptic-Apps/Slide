package me.ccrama.redditslide.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsTheme extends BaseActivityAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_theme);
        setupAppBar(R.id.toolbar, R.string.title_edit_theme, true, true);

        /*  findViewById(R.id.auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Reddit.tabletUI) {

                    Intent i = new Intent(Settings.this, SettingsAutonight.class);
                    startActivity(i);
                } else {
                    new AlertDialogWrapper.Builder(Settings.this)

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
       */
        findViewById(R.id.accent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.chooseaccent, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsTheme.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());

                final LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

                int[] arrs = new int[ColorPreferences.Theme.values().length / 4];
                int i = 0;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType() == 0) {
                        arrs[i] = ContextCompat.getColor(SettingsTheme.this, type.getColor());
                        i++;
                    }
                }

                colorPicker.setColors(arrs);
                colorPicker.setSelectedColor(ContextCompat.getColor(SettingsTheme.this, new ColorPreferences(SettingsTheme.this).getFontStyle().getColor()));


                dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int color = colorPicker.getColor();
                        ColorPreferences.Theme t = null;
                        for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                            if (ContextCompat.getColor(SettingsTheme.this, type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                t = type;
                                LogUtil.v("Setting to " + t.getTitle());
                                break;
                            }
                        }


                        new ColorPreferences(SettingsTheme.this).setFontStyle(t);

                        Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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
        /*Not needed anymore
        if (Reddit.expandedSettings) {
            //Color matching mode//
            //Everywhere, not sub//
            final TextView color = (TextView) findViewById(R.id.colormatchingwhere);
            color.setText(CreateCardView.getColorMatchingMode().toString().replace("_", " ").toLowerCase());
            findViewById(R.id.colormatchingwhere_touch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(SettingsTheme.this, v);
                    //Inflating the Popup using xml file
                    popup.getMenu().add("Always Match");
                    popup.getMenu().add("Match Externally");

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            CreateCardView.setColorMatchingMode(SettingValues.ColorMatchingMode.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())));
                            color.setText(CreateCardView.getColorMatchingMode().toString().replace("_", " ").toLowerCase());

                            return true;
                        }
                    });

                    popup.show();
                }
            });
            //Color matching type//
            //card, subreddit, or none//
            final TextView matchingtype = (TextView) findViewById(R.id.colormatching);
            matchingtype.setText(CreateCardView.getColorIndicator().toString().replace("_", " ").toLowerCase());
            findViewById(R.id.colormatching_touch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(SettingsTheme.this, v);
                    //Inflating the Popup using xml file
                    popup.getMenu().add("Card Background");
                    popup.getMenu().add("Text Color");
                    popup.getMenu().add("None");

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            CreateCardView.setColorIndicicator(SettingValues.ColorIndicator.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())));
                            matchingtype.setText(CreateCardView.getColorIndicator().toString().replace("_", " ").toLowerCase());

                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }
        else{
            findViewById(R.id.colormatching_touch).setVisibility(View.GONE);
            findViewById(R.id.colormatchingwhere_touch).setVisibility(View.GONE);
        }
*/

        findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosethemesmall, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsTheme.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());

                dialoglayout.findViewById(R.id.black).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = new ColorPreferences(SettingsTheme.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 2) {
                                Reddit.themeBack = theme.getThemeType();
                                new ColorPreferences(SettingsTheme.this).setFontStyle(theme);

                                Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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
                        String name = new ColorPreferences(SettingsTheme.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 1) {
                                new ColorPreferences(SettingsTheme.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();

                                Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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
                        String name = new ColorPreferences(SettingsTheme.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 0) {
                                new ColorPreferences(SettingsTheme.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();

                                Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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
                        String name = new ColorPreferences(SettingsTheme.this).getFontStyle().getTitle().split("_")[1];
                        final String newName = name.replace("(", "");
                        for (ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                            if (theme.toString().contains(newName) && theme.getThemeType() == 3) {
                                new ColorPreferences(SettingsTheme.this).setFontStyle(theme);
                                Reddit.themeBack = theme.getThemeType();

                                Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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

        findViewById(R.id.main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.choosemain, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsTheme.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());


                LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
                final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

                colorPicker.setColors(ColorPreferences.getBaseColors(SettingsTheme.this));
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
                        if (mToolbar != null)
                            mToolbar.setBackgroundColor(colorPicker2.getColor());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Palette.getDarkerColor(colorPicker2.getColor()));
                        }
                        setRecentBar(getString(R.string.title_theme_settings), colorPicker2.getColor());

                    }
                });


                dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.colors.edit().putInt("DEFAULTCOLOR", colorPicker2.getColor()).apply();
                        Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
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
        final SwitchCompat s2 = (SwitchCompat) findViewById(R.id.tint_everywhere);

        SwitchCompat s = (SwitchCompat) findViewById(R.id.colorback);
        s.setChecked(SettingValues.colorBack);
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.colorBack = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLOR_BACK, isChecked).apply();
                s2.setEnabled(isChecked);

            }
        });
        s2.setEnabled(s.isChecked());
        s2.setChecked(SettingValues.colorEverywhere);
        s2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.colorEverywhere = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLOR_EVERYWHERE, isChecked).apply();
            }
        });

        String selectedStyle = "";

        switch (SettingValues.navbarStyle) {
            case 1:
                selectedStyle = getString(R.string.settings_navigation_bar_stock);
                break;
            case 2:
                selectedStyle = getString(R.string.settings_navigation_bar_colored);
                break;
            case 3:
                selectedStyle = getString(R.string.settings_navigation_bar_translucent);
                break;
        }
        ((TextView) findViewById(R.id.navbar_style_current)).setText(selectedStyle);

        {
            findViewById(R.id.navbar_style).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String selectedStyle = "";
                            switch (i) {
                                case 0:
                                    SettingValues.navbarStyle = R.integer.NAVBAR_STOCK;
                                    selectedStyle = getString(R.string.settings_navigation_bar_stock);
                                    SettingValues.prefs.edit().putInt("navbarStyle", 1).apply();
                                    break;
                                case 1:
                                    SettingValues.navbarStyle = R.integer.NAVBAR_COLORED;
                                    selectedStyle = getString(R.string.settings_navigation_bar_colored);
                                    SettingValues.prefs.edit().putInt("navbarStyle", 2).apply();
                                    break;
                                case 2:
                                    SettingValues.navbarStyle = R.integer.NAVBAR_TRANSLUCENT;
                                    selectedStyle = getString(R.string.settings_navigation_bar_translucent);
                                    SettingValues.prefs.edit().putInt("navbarStyle", 3).apply();
                                    break;
                            }
                            ((TextView) findViewById(R.id.navbar_style_current)).setText(selectedStyle);
                            themeSystemBars("");
                        }
                    };
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsTheme.this);
                    builder.setTitle(R.string.settings_navigation_bar_choose);
                    String[] navbarStyles = {getString(R.string.settings_navigation_bar_stock),
                            getString(R.string.settings_navigation_bar_colored),
                            getString(R.string.settings_navigation_bar_translucent)};
                    builder.setSingleChoiceItems(navbarStyles, SettingValues.navbarStyle - 1, l2);
                    builder.show();
                }
            });
        }
    }
}