package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.io.File;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.OnSingleClickListener;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityAnim implements FolderChooserDialogCreate.FolderCallback {

    public static boolean searchChanged; //whether or not the subreddit search method changed

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
                    Reddit.colors.edit().putInt("notificationOverride", -1).apply();
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
                    Reddit.colors.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
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
                    Reddit.colors.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
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
                    Reddit.colors.edit().putInt("notificationOverride", -1).apply();
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
            SwitchCompat single = (SwitchCompat) findViewById(R.id.forcelanguage);

            single.setChecked(SettingValues.overrideLanguage);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingsTheme.changed = true;
                    SettingValues.overrideLanguage = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE, isChecked).apply();
                }
            });
        }
        {
            findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FolderChooserDialogCreate.Builder(SettingsGeneral.this)
                            .chooseButton(R.string.btn_select)  // changes label of the choose button
                            .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                            .show();
                }
            });
        }
        String loc = Reddit.appRestart.getString("imagelocation", getString(R.string.settings_image_location_unset));
        ((TextView) findViewById(R.id.location)).setText(loc);
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.expandedmenu);

            single.setChecked(SettingValues.expandedToolbar);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.expandedToolbar = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_EXPANDED_TOOLBAR, isChecked).apply();
                }
            });
        }

        findViewById(R.id.viewtype).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(SettingsGeneral.this, SettingsViewType.class);
                startActivity(i);
            }
        });

        //FAB multi choice//
        ((TextView) findViewById(R.id.fab_current)).setText(SettingValues.fab ? (SettingValues.fabType == R.integer.FAB_DISMISS ? getString(R.string.fab_hide) : getString(R.string.fab_create)) : getString(R.string.fab_disabled));

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenuInflater().inflate(R.menu.fab_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.disabled:
                                SettingValues.fab = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_FAB, false).apply();
                                break;
                            case R.id.hide:
                                SettingValues.fab = true;
                                SettingValues.fabType = R.integer.FAB_DISMISS;
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_FAB_TYPE, R.integer.FAB_DISMISS).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_FAB, true).apply();
                                break;
                            case R.id.create:
                                SettingValues.fab = true;
                                SettingValues.fabType = R.integer.FAB_POST;
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_FAB_TYPE, R.integer.FAB_POST).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_FAB, true).apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.fab_current)).setText(SettingValues.fab ? (SettingValues.fabType == R.integer.FAB_DISMISS ? getString(R.string.fab_hide) : getString(R.string.fab_create)) : getString(R.string.fab_disabled));

                        return true;
                    }
                });

                popup.show();
            }
        });

        //SettingValues.subredditSearchMethod == 1 for drawer, 2 for toolbar, 3 for both
        final TextView currentMethodTitle = (TextView) findViewById(R.id.subreddit_search_method_current);
        if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER) {
            currentMethodTitle.setText(getString(R.string.subreddit_search_method_drawer));
        } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
            currentMethodTitle.setText(getString(R.string.subreddit_search_method_toolbar));
        } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
            currentMethodTitle.setText(getString(R.string.subreddit_search_method_both));
        }

        findViewById(R.id.subreddit_search_method).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenuInflater().inflate(R.menu.subreddit_search_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.subreddit_search_drawer:
                                SettingValues.subredditSearchMethod = R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER;
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD, R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER).apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                            case R.id.subreddit_search_toolbar:
                                SettingValues.subredditSearchMethod = R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR;
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD, R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR).apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                            case R.id.subreddit_search_both:
                                SettingValues.subredditSearchMethod = R.integer.SUBREDDIT_SEARCH_METHOD_BOTH;
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD, R.integer.SUBREDDIT_SEARCH_METHOD_BOTH).apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                        }

                        if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                            currentMethodTitle.setText(getString(R.string.subreddit_search_method_drawer));
                        } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                            currentMethodTitle.setText(getString(R.string.subreddit_search_method_toolbar));
                        } else if (SettingValues.subredditSearchMethod == R.integer.SUBREDDIT_SEARCH_METHOD_BOTH) {
                            currentMethodTitle.setText(getString(R.string.subreddit_search_method_both));
                        }
                        return true;
                    }
                });
                popup.show();
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

        if (Reddit.notificationTime > 0) {
            ((TextView) findViewById(R.id.notifications_current)).setText(getString(R.string.settings_notification_short,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));
        } else {
            ((TextView) findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);
        }

        if (Authentication.isLoggedIn) {
            findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                    setupNotificationSettings(dialoglayout, SettingsGeneral.this);
                }
            });
        } else {
            findViewById(R.id.notifications).setEnabled(false);
            findViewById(R.id.notifications).setAlpha(0.25f);
        }

        ((TextView) findViewById(R.id.sorting_current)).setText(Reddit.getSortingStrings(getBaseContext(), "", false)[Reddit.getSortingId("")]);
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
                                case 11:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.WEEK;
                                    break;
                                case 12:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.MONTH;
                                    break;
                                case 13:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.YEAR;
                                    break;
                                case 14:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.ALL;
                                    break;
                            }
                            SettingValues.prefs.edit().putString("defaultSorting", Reddit.defaultSorting.name()).apply();
                            SettingValues.prefs.edit().putString("timePeriod", Reddit.timePeriod.name()).apply();
                            SettingValues.defaultSorting = Reddit.defaultSorting;
                            SettingValues.timePeriod = Reddit.timePeriod;
                            ((TextView) findViewById(R.id.sorting_current)).setText(
                                    Reddit.getSortingStrings(getBaseContext(), "", false)[Reddit.getSortingId("")]);
                        }
                    };
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(
                            Reddit.getSortingStrings(getBaseContext(), "",false), Reddit.getSortingId(""), l2);
                    builder.show();
                }
            });
        }
        {
            final int i2 = SettingValues.defaultCommentSorting == CommentSort.CONFIDENCE ? 0
                    : SettingValues.defaultCommentSorting == CommentSort.TOP ? 1
                    : SettingValues.defaultCommentSorting == CommentSort.NEW ? 2
                    : SettingValues.defaultCommentSorting == CommentSort.CONTROVERSIAL ? 3
                    : SettingValues.defaultCommentSorting == CommentSort.OLD ? 4
                    : SettingValues.defaultCommentSorting == CommentSort.QA ? 5
                    : 0;
            ((TextView) findViewById(R.id.sorting_current_comment))
                    .setText(Reddit.getSortingStringsComments(getBaseContext())[i2]);

            findViewById(R.id.sorting_comment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CommentSort commentSorting = SettingValues.defaultCommentSorting;

                            switch (i) {
                                case 0:
                                    commentSorting = CommentSort.CONFIDENCE;
                                    break;
                                case 1:
                                    commentSorting = CommentSort.TOP;
                                    break;
                                case 2:
                                    commentSorting = CommentSort.NEW;
                                    break;
                                case 3:
                                    commentSorting = CommentSort.CONTROVERSIAL;
                                    break;
                                case 4:
                                    commentSorting = CommentSort.OLD;
                                    break;
                                case 5:
                                    commentSorting = CommentSort.QA;
                                    break;
                            }
                            SettingValues.prefs.edit().putString("defaultCommentSortingNew", commentSorting.name()).apply();
                            SettingValues.defaultCommentSorting = commentSorting;
                            ((TextView) findViewById(R.id.sorting_current_comment))
                                    .setText(Reddit.getSortingStringsComments(getBaseContext())[i]);
                        }
                    };

                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    Resources res = getBaseContext().getResources();
                    builder.setSingleChoiceItems(
                            new String[]{
                                    res.getString(R.string.sorting_best),
                                    res.getString(R.string.sorting_top),
                                    res.getString(R.string.sorting_new),
                                    res.getString(R.string.sorting_controversial),
                                    res.getString(R.string.sorting_old),
                                    res.getString(R.string.sorting_ama)},
                            i2, l2);
                    builder.show();
                }
            });
        }
    }

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath().toString()).apply();
            Toast.makeText(this, getString(R.string.settings_set_image_location, folder.getAbsolutePath()), Toast.LENGTH_LONG).show();
            ((TextView) findViewById(R.id.location)).setText(folder.getAbsolutePath());
        }
    }
}