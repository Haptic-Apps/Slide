package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityNoAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_general);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.settings_title_general);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsGeneral.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.settings_title_general), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.single);

            single.setChecked(Reddit.single);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.single = isChecked;
                    SettingValues.prefs.edit().putBoolean("Single", isChecked).apply();

                }
            });
        }
        {
            final SeekBar animationMultiplier = (SeekBar) findViewById(R.id.animation_length_sb);
            animationMultiplier.setProgress(Reddit.enter_animation_time_multiplier);
            animationMultiplier.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress <= 0) {
                        progress = 1;
                        animationMultiplier.setProgress(1);
                    }
                    SettingValues.prefs.edit().putInt("AnimationLengthMultiplier", progress).apply();
                    Reddit.enter_animation_time_multiplier = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            SwitchCompat single = (SwitchCompat) findViewById(R.id.animation);
            single.setChecked(Reddit.animation);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.animation = isChecked;
                    SettingValues.prefs.edit().putBoolean("Animation", isChecked).apply();
                    animationMultiplier.setEnabled(isChecked);
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.hidebutton);

            single.setChecked(Reddit.hideButton);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.hideButton = isChecked;
                    SettingValues.prefs.edit().putBoolean("Hidebutton", isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.exitcheck);

            single.setChecked(Reddit.exit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.exit = isChecked;
                    SettingValues.prefs.edit().putBoolean("Exit", isChecked).apply();

                }
            });
        }

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.nsfw);

            single.setChecked(!SettingValues.NSFWPosts);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.prefs.edit().putBoolean("NSFWPostsNew", !isChecked).apply();

                    SettingValues.NSFWPosts = !isChecked;
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.nsfwrpev);

            single.setChecked(!SettingValues.NSFWPreviews);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !isChecked).apply();
                    SettingValues.NSFWPreviews = !isChecked;

                }
            });
        }
        final TextView color = (TextView) findViewById(R.id.font);
        color.setText(new FontPreferences(this).getFontStyle().getTitle());
        findViewById(R.id.fontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsGeneral.this).setFontStyle(FontPreferences.FontStyle.valueOf(item.getTitle().toString()));
                        color.setText(new FontPreferences(SettingsGeneral.this).getFontStyle().getTitle());

                        return true;
                    }
                });

                popup.show();
            }
        });
        ((TextView) findViewById(R.id.sorting_current)).setText(Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId()]);

        {
            findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    Reddit.defaultSorting = Sorting.HOT;
                                    break;
                                case 1:
                                    Reddit.defaultSorting = Sorting.NEW;
                                    break;
                                case 2:
                                    Reddit.defaultSorting = Sorting.RISING;
                                    break;
                                case 3:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.HOUR;
                                    break;
                                case 4:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.DAY;
                                    break;
                                case 5:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.WEEK;
                                    break;
                                case 6:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.MONTH;
                                    break;
                                case 7:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.YEAR;
                                    break;
                                case 8:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.ALL;
                                    break;
                                case 9:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.HOUR;
                                    break;
                                case 10:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.DAY;
                                    break;
                            }
                            SettingValues.prefs.edit().putString("defaultSorting", Reddit.defaultSorting.name()).apply();
                            SettingValues.prefs.edit().putString("timePeriod", Reddit.timePeriod.name()).apply();
                            SettingValues.defaultSorting = Reddit.defaultSorting;
                            SettingValues.timePeriod = Reddit.timePeriod;
                            ((TextView) findViewById(R.id.sorting_current)).setText(
                                    Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId()]);
                        }
                    };
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(
                            Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
                    builder.show();
                }
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}