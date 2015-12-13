package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.Palette;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsTheme extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_theme);
        setupAppBar(R.id.toolbar, R.string.title_edit_theme, true);

        findViewById(R.id.accent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.chooseaccent, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsTheme.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setBackgroundColor(Palette.getDefaultColor());

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
                colorPicker.setSelectedColor(new ColorPreferences(SettingsTheme.this).getColor(""));


                dialoglayout.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
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
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        int color = colorPicker.getColor();
                        ColorPreferences.Theme t = null;
                        for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                            if (getResources().getColor(type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                t = type;
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
                builder.show();

            }

        });
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
                        mToolbar.setBackgroundColor(colorPicker2.getColor());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Palette.getDarkerColor(colorPicker2.getColor()));
                            SettingsTheme.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_theme_settings), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                        }

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

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Reddit.colors.edit().putInt("DEFAULTCOLOR", colorPicker2.getColor()).apply();
                        Intent i = new Intent(SettingsTheme.this, SettingsTheme.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(i);
                        overridePendingTransition(0, 0);

                        finish();
                        overridePendingTransition(0, 0);

                    }
                });
                builder.show();

            }

        });

    }

}