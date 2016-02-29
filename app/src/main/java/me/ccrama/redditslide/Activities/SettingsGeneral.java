package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityAnim {
    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
        final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);
        final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);


        if (Reddit.notificationTime == -1) {
            checkBox.setChecked(false);
            checkBox.setText(context.getString(R.string.settings_mail_check));
        } else {
            checkBox.setChecked(true);
            landscape.setValue(Reddit.notificationTime / 15, false);
            checkBox.setText(context.getString(R.string.settings_notification,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));

        }
        landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                if (checkBox.isChecked())
                    checkBox.setText(context.getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(i1 * 15, context.getBaseContext())));
            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    Reddit.notificationTime = -1;
                    Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                    checkBox.setText(context.getString(R.string.settings_mail_check));
                    landscape.setValue(0, true);
                    if (Reddit.notifications != null)
                        Reddit.notifications.cancel(context.getApplication());
                } else {
                    Reddit.notificationTime = 60;
                    landscape.setValue(4, true);
                    checkBox.setText(context.getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                }
            }
        });
        dialoglayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
        //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

        //todo  portrait.setBackgroundColor(Palette.getDefaultColor());


        final Dialog dialog = builder.setView(dialoglayout).create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (checkBox.isChecked()) {
                    Reddit.notificationTime = landscape.getValue() * 15;
                    Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                    }
                    Reddit.notifications.cancel(context.getApplication());
                    Reddit.notifications.start(context.getApplication());
                }
            }
        });
        dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View d) {
                if (checkBox.isChecked()) {
                    Reddit.notificationTime = landscape.getValue() * 15;
                    Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                    }
                    Reddit.notifications.cancel(context.getApplication());
                    Reddit.notifications.start(context.getApplication());
                    dialog.dismiss();
                    if (context instanceof SettingsGeneral)
                        ((TextView) context.findViewById(R.id.notifications_current)).setText(
                                context.getString(R.string.settings_notification_short,
                                        TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                } else {
                    Reddit.notificationTime = -1;
                    Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                    }
                    Reddit.notifications.cancel(context.getApplication());
                    dialog.dismiss();
                    if (context instanceof SettingsGeneral)
                        ((TextView) context.findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);

                }
            }
        });

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_general);
        setupAppBar(R.id.toolbar, R.string.settings_title_general, true, true);

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.single);

            single.setChecked(!SettingValues.single);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.single = !isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SINGLE, !isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.swipeback);

            single.setChecked(SettingValues.swipeAnywhere);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.swipeAnywhere = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SWIPE_ANYWHERE, isChecked).apply();

                }
            });
        }

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.scrollseen);

            single.setChecked(SettingValues.scrollSeen);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.scrollSeen = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.navposts);

            single.setChecked(SettingValues.postNav);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.postNav = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_POST_NAV, isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.lowq);

            single.setChecked(!SettingValues.blurCheck);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.blurCheck = !isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_BLUR, !isChecked).apply();

                }
            });
        }

        /* Might need this later
        if (Reddit.expandedSettings) {
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
                        Reddit.enter_animation_time = Reddit.enter_animation_time_original * Reddit.enter_animation_time_multiplier;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }
        }
        else {
            findViewById(R.id.animation_length_sb).setVisibility(View.GONE);
            findViewById(R.id.enter_animation).setVisibility(View.GONE);
        }*/
        final SwitchCompat animation = (SwitchCompat) findViewById(R.id.animation);
        animation.setChecked(SettingValues.animation);
        animation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.animation = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ANIMATION, isChecked).apply();
            }
        });
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.exitcheck);

            single.setChecked(SettingValues.exit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.exit = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_EXIT, isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.hidetop);

            single.setChecked(SettingValues.hideHeader);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.hideHeader = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_HEADER, isChecked).apply();

                }
            });
        }
        if (Reddit.notificationTime > 0) {
            ((TextView) findViewById(R.id.notifications_current)).setText(getString(R.string.settings_notification_short,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));

        } else {
            ((TextView) findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);
        }

        if (Authentication.isLoggedIn)
            findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                    setupNotificationSettings(dialoglayout, SettingsGeneral.this);
                }
            });
        else findViewById(R.id.notifications).setVisibility(View.GONE);


        ((TextView) findViewById(R.id.sorting_current)).setText(Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId("")]);

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
                                    Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId("")]);
                        }
                    };
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(
                            Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(""), l2);
                    builder.show();
                }
            });
        }

    }

}