package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;
import com.rey.material.widget.Slider;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import me.ccrama.redditslide.Activities.SettingsViewType;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate.FolderCallback;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SortingUtil;

/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneralFragment<ActivityType extends AppCompatActivity & FolderCallback>
            implements  FolderCallback {

    private ActivityType context;
    public static boolean searchChanged; //whether or not the subreddit search method changed

    public SettingsGeneralFragment(ActivityType context) {
        this.context = context;
    }

    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
        final Slider landscape = dialoglayout.findViewById(R.id.landscape);
        final CheckBox checkBox = dialoglayout.findViewById(R.id.load);
        final CheckBox sound = dialoglayout.findViewById(R.id.sound);

        sound.setChecked(SettingValues.notifSound);
        sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_SOUND_NOTIFS, isChecked)
                        .apply();
                SettingValues.notifSound = isChecked;
            }
        });

        if (Reddit.notificationTime == -1) {
            checkBox.setChecked(false);
            checkBox.setText(context.getString(R.string.settings_mail_check));
        } else {
            checkBox.setChecked(true);
            landscape.setValue(Reddit.notificationTime / 15, false);
            checkBox.setText(context.getString(R.string.settings_notification_newline,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                            context.getBaseContext())));
        }
        landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i,
                                          int i1) {
                if (checkBox.isChecked()) {
                    checkBox.setText(context.getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(i1 * 15, context.getBaseContext())));
                }
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
                    if (Reddit.notifications != null) {
                        Reddit.notifications.cancel(context.getApplication());
                    }
                } else {
                    Reddit.notificationTime = 60;
                    landscape.setValue(4, true);
                    checkBox.setText(context.getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                                    context.getBaseContext())));
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
                    Reddit.colors.edit()
                            .putInt("notificationOverride", landscape.getValue() * 15)
                            .apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications =
                                new NotificationJobScheduler(context.getApplication());
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
                    Reddit.colors.edit()
                            .putInt("notificationOverride", landscape.getValue() * 15)
                            .apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications =
                                new NotificationJobScheduler(context.getApplication());
                    }
                    Reddit.notifications.cancel(context.getApplication());
                    Reddit.notifications.start(context.getApplication());
                    dialog.dismiss();
                    if (context instanceof me.ccrama.redditslide.Activities.SettingsGeneral) {
                        ((TextView) context.findViewById(R.id.settings_general_notifications_current)).setText(
                                context.getString(R.string.settings_notification_short,
                                        TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                                                context.getBaseContext())));
                    }
                } else {
                    Reddit.notificationTime = -1;
                    Reddit.colors.edit().putInt("notificationOverride", -1).apply();
                    if (Reddit.notifications == null) {
                        Reddit.notifications =
                                new NotificationJobScheduler(context.getApplication());
                    }
                    Reddit.notifications.cancel(context.getApplication());
                    dialog.dismiss();
                    if (context instanceof me.ccrama.redditslide.Activities.SettingsGeneral) {
                        ((TextView) context.findViewById(R.id.settings_general_notifications_current)).setText(
                                R.string.settings_notifdisabled);
                    }

                }
            }
        });
    }

    /* Allow SettingsGeneral and Settings Activity classes to use the same XML functionality */
    public void Bind() {
        context.findViewById(R.id.settings_general_drawer_items)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DrawerItemsDialog(new MaterialDialog.Builder(context)).show();
                    }
                });

        {
            SwitchCompat single = context.findViewById(R.id.settings_general_immersivemode);

            if (single != null) {
                single.setChecked(SettingValues.immersiveMode);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingsThemeFragment.changed = true;
                        SettingValues.immersiveMode = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_IMMERSIVE_MODE, isChecked)
                                .apply();
                    }
                });
            }
        }

        {
            SwitchCompat single = context.findViewById(R.id.settings_general_forcelanguage);

            if (single != null) {
                single.setChecked(SettingValues.overrideLanguage);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingsThemeFragment.changed = true;
                        SettingValues.overrideLanguage = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE, isChecked)
                                .apply();
                    }
                });
            }
        }

        //hide fab while scrolling
        {
            SwitchCompat single = context.findViewById(R.id.settings_general_show_fab);

            if (single != null) {
                single.setChecked(SettingValues.alwaysShowFAB);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingsThemeFragment.changed = true;
                        SettingValues.alwaysShowFAB = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_ALWAYS_SHOW_FAB, isChecked)
                                .apply();
                    }
                });
            }
        }

        // Show image download button
        {
            SwitchCompat single = context.findViewById(R.id.settings_general_show_download_button);

            if (single != null) {
                single.setChecked(SettingValues.imageDownloadButton);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingValues.imageDownloadButton = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_IMAGE_DOWNLOAD_BUTTON, isChecked)
                                .apply();
                    }
                });
            }
        }

        {
            SwitchCompat single = context.findViewById(R.id.settings_general_subfolder);

            if (single != null) {
                single.setChecked(SettingValues.imageSubfolders);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingValues.imageSubfolders = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_IMAGE_SUBFOLDERS, isChecked)
                                .apply();
                    }
                });
            }
        }

        {
            if (context.findViewById(R.id.settings_general_download) != null) {
                context.findViewById(R.id.settings_general_download).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new FolderChooserDialogCreate.Builder(SettingsGeneralFragment.this.context).chooseButton(
                                R.string.btn_select)  // changes label of the choose button
                                .initialPath(Environment.getExternalStorageDirectory()
                                        .getPath())  // changes initial path, defaults to external storage directory
                                .show();
                    }
                });
            }
        }

        if (context.findViewById(R.id.settings_general_location) != null) {
            String loc = Reddit.appRestart.getString("imagelocation",
                    context.getString(R.string.settings_image_location_unset));
            ((TextView) context.findViewById(R.id.settings_general_location)).setText(loc);
        }

        {
            SwitchCompat single = context.findViewById(R.id.settings_general_expandedmenu);

            if (single != null) {
                single.setChecked(SettingValues.expandedToolbar);
                single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SettingValues.expandedToolbar = isChecked;
                        SettingValues.prefs.edit()
                                .putBoolean(SettingValues.PREF_EXPANDED_TOOLBAR, isChecked)
                                .apply();
                    }
                });
            }
        }

        if (context.findViewById(R.id.settings_general_viewtype) != null) {
            context.findViewById(R.id.settings_general_viewtype).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Intent i = new Intent(context, SettingsViewType.class);
                    context.startActivity(i);
                }
            });
        }

        //FAB multi choice//
        if (context.findViewById(R.id.settings_general_fab_current) != null && context.findViewById(R.id.settings_general_fab) != null) {
            ((TextView) context.findViewById(R.id.settings_general_fab_current)).setText(
                    SettingValues.fab ? (SettingValues.fabType == Constants.FAB_DISMISS ? context.getString(
                            R.string.fab_hide) : context.getString(R.string.fab_create))
                            : context.getString(R.string.fab_disabled));

            context.findViewById(R.id.settings_general_fab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.getMenuInflater().inflate(R.menu.fab_settings, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.disabled:
                                    SettingValues.fab = false;
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_FAB, false)
                                            .apply();
                                    break;
                                case R.id.hide:
                                    SettingValues.fab = true;
                                    SettingValues.fabType = Constants.FAB_DISMISS;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_FAB_TYPE, Constants.FAB_DISMISS)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_FAB, true)
                                            .apply();
                                    break;
                                case R.id.create:
                                    SettingValues.fab = true;
                                    SettingValues.fabType = Constants.FAB_POST;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_FAB_TYPE, Constants.FAB_POST)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_FAB, true)
                                            .apply();
                                    break;
                                case R.id.search:
                                    SettingValues.fab = true;
                                    SettingValues.fabType = Constants.FAB_SEARCH;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_FAB_TYPE, Constants.FAB_SEARCH)
                                            .apply();
                                    SettingValues.prefs.edit()
                                            .putBoolean(SettingValues.PREF_FAB, true)
                                            .apply();
                                    break;
                            }
                            final TextView fabTitle = context.findViewById(R.id.settings_general_fab_current);
                            if (SettingValues.fab) {
                                if (SettingValues.fabType == Constants.FAB_DISMISS) {
                                    fabTitle.setText(R.string.fab_hide);
                                } else if (SettingValues.fabType == Constants.FAB_POST) {
                                    fabTitle.setText(R.string.fab_create);
                                } else {
                                    fabTitle.setText(R.string.fab_search);
                                }
                            } else {
                                fabTitle.setText(R.string.fab_disabled);
                            }

                            return true;
                        }
                    });

                    popup.show();
                }
            });
        }

        //SettingValues.subredditSearchMethod == 1 for drawer, 2 for toolbar, 3 for both
        final TextView currentMethodTitle = context.findViewById(R.id.settings_general_subreddit_search_method_current);

        if (currentMethodTitle != null) {
            if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_drawer));
            } else if (SettingValues.subredditSearchMethod
                    == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_toolbar));
            } else if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_both));
            }
        }

        if (context.findViewById(R.id.settings_general_subreddit_search_method) != null) {
            context.findViewById(R.id.settings_general_subreddit_search_method).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(SettingsGeneralFragment.this.context, v);
                    popup.getMenuInflater().inflate(R.menu.subreddit_search_settings, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.subreddit_search_drawer:
                                    SettingValues.subredditSearchMethod =
                                            Constants.SUBREDDIT_SEARCH_METHOD_DRAWER;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                    Constants.SUBREDDIT_SEARCH_METHOD_DRAWER)
                                            .apply();
                                    searchChanged = true;
                                    break;
                                case R.id.subreddit_search_toolbar:
                                    SettingValues.subredditSearchMethod =
                                            Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                    Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR)
                                            .apply();
                                    searchChanged = true;
                                    break;
                                case R.id.subreddit_search_both:
                                    SettingValues.subredditSearchMethod =
                                            Constants.SUBREDDIT_SEARCH_METHOD_BOTH;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                    Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                            .apply();
                                    searchChanged = true;
                                    break;
                            }

                            if (SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                                currentMethodTitle.setText(
                                        context.getString(R.string.subreddit_search_method_drawer));
                            } else if (SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                                currentMethodTitle.setText(
                                        context.getString(R.string.subreddit_search_method_toolbar));
                            } else if (SettingValues.subredditSearchMethod
                                    == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                                currentMethodTitle.setText(
                                        context.getString(R.string.subreddit_search_method_both));
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }

        final TextView currentBackButtonTitle =
                context.findViewById(R.id.settings_general_back_button_behavior_current);
        if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.ConfirmExit.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_confirm_exit));
        } else if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.OpenDrawer.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_drawer));
        } else if (SettingValues.backButtonBehavior
                == Constants.BackButtonBehaviorOptions.GotoFirst.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_goto_first));
        } else {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_default));
        }

        context.findViewById(R.id.settings_general_back_button_behavior).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater()
                        .inflate(R.menu.back_button_behavior_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.back_button_behavior_default:
                                SettingValues.backButtonBehavior =
                                        Constants.BackButtonBehaviorOptions.Default.getValue();
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                                Constants.BackButtonBehaviorOptions.Default.getValue())
                                        .apply();
                                break;
                            case R.id.back_button_behavior_confirm_exit:
                                SettingValues.backButtonBehavior =
                                        Constants.BackButtonBehaviorOptions.ConfirmExit.getValue();
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                                Constants.BackButtonBehaviorOptions.ConfirmExit.getValue())
                                        .apply();
                                break;
                            case R.id.back_button_behavior_open_drawer:
                                SettingValues.backButtonBehavior =
                                        Constants.BackButtonBehaviorOptions.OpenDrawer.getValue();
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                                Constants.BackButtonBehaviorOptions.OpenDrawer.getValue())
                                        .apply();
                                break;
                            case R.id.back_button_behavior_goto_first:
                                SettingValues.backButtonBehavior =
                                        Constants.BackButtonBehaviorOptions.GotoFirst.getValue();
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                                Constants.BackButtonBehaviorOptions.GotoFirst.getValue())
                                        .apply();
                                break;
                        }

                        if (SettingValues.backButtonBehavior
                                == Constants.BackButtonBehaviorOptions.ConfirmExit.getValue()) {
                            currentBackButtonTitle.setText(
                                    context.getString(R.string.back_button_behavior_confirm_exit));
                        } else if (SettingValues.backButtonBehavior
                                == Constants.BackButtonBehaviorOptions.OpenDrawer.getValue()) {
                            currentBackButtonTitle.setText(
                                    context.getString(R.string.back_button_behavior_drawer));
                        } else if (SettingValues.backButtonBehavior
                                == Constants.BackButtonBehaviorOptions.GotoFirst.getValue()) {
                            currentBackButtonTitle.setText(
                                    context.getString(R.string.back_button_behavior_goto_first));
                        } else {
                            currentBackButtonTitle.setText(
                                    context.getString(R.string.back_button_behavior_default));
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        if (context.findViewById(R.id.settings_general_notifications_current) != null &&
                context.findViewById(R.id.settings_general_sub_notifs_current) != null) {
            if (Reddit.notificationTime > 0) {
                ((TextView) context.findViewById(R.id.settings_general_notifications_current)).setText(
                        context.getString(R.string.settings_notification_short,
                                TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                                        context.getBaseContext())));
                setSubText();
            } else {
                ((TextView) context.findViewById(R.id.settings_general_notifications_current)).setText(
                        R.string.settings_notifdisabled);
                ((TextView) context.findViewById(R.id.settings_general_sub_notifs_current)).setText(
                        R.string.settings_enable_notifs);
            }
        }

        if (Authentication.isLoggedIn) {
            if (context.findViewById(R.id.settings_general_notifications) != null) {
                context.findViewById(R.id.settings_general_notifications).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater inflater = context.getLayoutInflater();
                        final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                        setupNotificationSettings(dialoglayout, SettingsGeneralFragment.this.context);
                    }
                });
            }
            if (context.findViewById(R.id.settings_general_sub_notifications) != null) {
                context.findViewById(R.id.settings_general_sub_notifications).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSelectDialog();
                    }
                });
            }
        } else {
            if (context.findViewById(R.id.settings_general_notifications) != null) {
                context.findViewById(R.id.settings_general_notifications).setEnabled(false);
                context.findViewById(R.id.settings_general_notifications).setAlpha(0.25f);
            }
            if (context.findViewById(R.id.settings_general_sub_notifications) != null) {
                context.findViewById(R.id.settings_general_sub_notifications).setEnabled(false);
                context.findViewById(R.id.settings_general_sub_notifications).setAlpha(0.25f);
            }
        }

        if (context.findViewById(R.id.settings_general_sorting_current) != null) {
            ((TextView) context.findViewById(R.id.settings_general_sorting_current)).setText(
                    SortingUtil.getSortingStrings()[SortingUtil.getSortingId("")]);
        }

        {
            if (context.findViewById(R.id.settings_general_sorting) != null) {
                context.findViewById(R.id.settings_general_sorting).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final DialogInterface.OnClickListener l2 =
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        switch (i) {
                                            case 0:
                                                SortingUtil.defaultSorting = Sorting.HOT;
                                                break;
                                            case 1:
                                                SortingUtil.defaultSorting = Sorting.NEW;
                                                break;
                                            case 2:
                                                SortingUtil.defaultSorting = Sorting.RISING;
                                                break;
                                            case 3:
                                                SortingUtil.defaultSorting = Sorting.TOP;
                                                askTimePeriod();
                                                return;
                                            case 4:
                                                SortingUtil.defaultSorting = Sorting.CONTROVERSIAL;
                                                askTimePeriod();
                                                return;
                                        }
                                        SettingValues.prefs.edit()
                                                .putString("defaultSorting",
                                                        SortingUtil.defaultSorting.name())
                                                .apply();
                                        SettingValues.defaultSorting = SortingUtil.defaultSorting;

                                        if (context.findViewById(R.id.settings_general_sorting_current) != null) {
                                            ((TextView) context.findViewById(R.id.settings_general_sorting_current)).setText(
                                                    SortingUtil.getSortingStrings()[SortingUtil.getSortingId(
                                                            "")]);
                                        }
                                    }
                                };

                        AlertDialogWrapper.Builder builder =
                                new AlertDialogWrapper.Builder(SettingsGeneralFragment.this.context);
                        builder.setTitle(R.string.sorting_choose);

                        // Remove the "Best" sorting option from settings because it is only supported on the frontpage.
                        int skip = -1;
                        List<String> sortingStrings = new ArrayList<>(Arrays.asList(SortingUtil.getSortingStrings()));
                        for (int i = 0; i < sortingStrings.size(); i++) {
                            if (sortingStrings.get(i).equals(context.getString(R.string.sorting_best))) {
                                skip = i;
                                break;
                            }
                        }
                        if (skip != -1) {
                            sortingStrings.remove(skip);
                        }

                        builder.setSingleChoiceItems(sortingStrings.toArray(new String[0]), SortingUtil.getSortingId(""), l2);
                        builder.show();
                    }
                });
            }
        }
        doNotifText(context);
        {
            final int i2 = SettingValues.defaultCommentSorting == CommentSort.CONFIDENCE ? 0
                    : SettingValues.defaultCommentSorting == CommentSort.TOP ? 1
                    : SettingValues.defaultCommentSorting == CommentSort.NEW ? 2
                    : SettingValues.defaultCommentSorting
                    == CommentSort.CONTROVERSIAL ? 3
                    : SettingValues.defaultCommentSorting == CommentSort.OLD
                    ? 4 : SettingValues.defaultCommentSorting
                    == CommentSort.QA ? 5 : 0;

            if (context.findViewById(R.id.settings_general_sorting_current_comment) != null) {
                ((TextView) context.findViewById(R.id.settings_general_sorting_current_comment)).setText(
                        SortingUtil.getSortingCommentsStrings()[i2]);
            }

            if (context.findViewById(R.id.settings_general_sorting_comment) != null) {
                context.findViewById(R.id.settings_general_sorting_comment).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DialogInterface.OnClickListener l2 =
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        CommentSort commentSorting =
                                                SettingValues.defaultCommentSorting;

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
                                        SettingValues.prefs.edit()
                                                .putString("defaultCommentSortingNew",
                                                        commentSorting.name())
                                                .apply();
                                        SettingValues.defaultCommentSorting = commentSorting;
                                        if (context.findViewById(R.id.settings_general_sorting_current_comment) != null) {
                                            ((TextView) context.findViewById(R.id.settings_general_sorting_current_comment)).setText(
                                                    SortingUtil.getSortingCommentsStrings()[i]);
                                        }
                                    }
                                };

                        AlertDialogWrapper.Builder builder =
                                new AlertDialogWrapper.Builder(SettingsGeneralFragment.this.context);
                        builder.setTitle(R.string.sorting_choose);
                        Resources res = context.getBaseContext().getResources();
                        builder.setSingleChoiceItems(new String[]{
                                res.getString(R.string.sorting_best),
                                res.getString(R.string.sorting_top),
                                res.getString(R.string.sorting_new),
                                res.getString(R.string.sorting_controversial),
                                res.getString(R.string.sorting_old), res.getString(R.string.sorting_ama)
                        }, i2, l2);
                        builder.show();
                    }
                });
            }
        }
    }

    public static void doNotifText(final Activity context) {
        {
            View notifs = context.findViewById(R.id.settings_general_redditnotifs);
            if (notifs != null) {
                if (!Reddit.isPackageInstalled("com.reddit.frontpage") ||
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    notifs.setVisibility(View.GONE);
                    if (context.findViewById(R.id.settings_general_installreddit) != null) {
                        context.findViewById(R.id.settings_general_installreddit).setVisibility(View.VISIBLE);
                    }
                } else {
                    if (((Reddit) context.getApplication()).isNotificationAccessEnabled()) {
                        SwitchCompat single = context.findViewById(R.id.settings_general_piggyback);
                        if (single != null) {
                            single.setChecked(true);
                            single.setEnabled(false);
                        }
                    } else {
                        final SwitchCompat single = context.findViewById(R.id.settings_general_piggyback);
                        if (single != null) {
                            single.setChecked(false);
                            single.setEnabled(true);
                            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    single.setChecked(false);
                                    Snackbar s = Snackbar.make(single, "Give Slide notification access", Snackbar.LENGTH_LONG);
                                    s.setAction("Go to settings", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

                                        }
                                    });
                                    s.show();
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    private void askTimePeriod() {
        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        SortingUtil.timePeriod = TimePeriod.HOUR;
                        break;
                    case 1:
                        SortingUtil.timePeriod = TimePeriod.DAY;
                        break;
                    case 2:
                        SortingUtil.timePeriod = TimePeriod.WEEK;
                        break;
                    case 3:
                        SortingUtil.timePeriod = TimePeriod.MONTH;
                        break;
                    case 4:
                        SortingUtil.timePeriod = TimePeriod.YEAR;
                        break;
                    case 5:
                        SortingUtil.timePeriod = TimePeriod.ALL;
                        break;
                }
                SettingValues.prefs.edit()
                        .putString("defaultSorting", SortingUtil.defaultSorting.name())
                        .apply();
                SettingValues.prefs.edit().putString("timePeriod", SortingUtil.timePeriod.name())
                        .apply();
                SettingValues.defaultSorting = SortingUtil.defaultSorting;
                SettingValues.timePeriod = SortingUtil.timePeriod;
                ((TextView) context.findViewById(R.id.settings_general_sorting_current)).setText(
                        SortingUtil.getSortingStrings()[SortingUtil.getSortingId("")]
                                + " > "
                                + SortingUtil.getSortingTimesStrings()[SortingUtil.getSortingTimeId(
                                "")]);
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneralFragment.this.context);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(SortingUtil.getSortingTimesStrings(),
                SortingUtil.getSortingTimeId(""), l2);
        builder.show();
    }

    private void setSubText() {
        ArrayList<String> rawSubs =
                Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
        String subText = context.getString(R.string.sub_post_notifs_settings_none);
        StringBuilder subs = new StringBuilder();
        for (String s : rawSubs) {
            if (!s.isEmpty()) {
                try {
                    String[] split = s.split(":");
                    subs.append(split[0]);
                    subs.append("(+").append(split[1]).append(")");
                    subs.append(", ");
                } catch (Exception ignored) {

                }
            }
        }
        if (!subs.toString().isEmpty()) {
            subText = subs.toString().substring(0, subs.toString().length() - 2);
        }
        ((TextView) context.findViewById(R.id.settings_general_sub_notifs_current)).setText(subText);
    }

    private String input;

    private void showSelectDialog() {
        ArrayList<String> rawSubs =
                Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
        HashMap<String, Integer> subThresholds = new HashMap<>();
        for (String s : rawSubs) {
            try {
                String[] split = s.split(":");
                subThresholds.put(split[0].toLowerCase(Locale.ENGLISH), Integer.valueOf(split[1]));
            } catch (Exception ignored) {

            }
        }

        // Get list of user's subscriptions
        CaseInsensitiveArrayList subs = UserSubscriptions.getSubscriptions(context);
        // Add any subs that the user has notifications for but isn't subscribed to
        for (String s : subThresholds.keySet()) {
            if (!subs.contains(s)) {
                subs.add(s);
            }
        }

        List<String> sorted = UserSubscriptions.sort(subs);

        //Array of all subs
        String[] all = new String[sorted.size()];
        //Contains which subreddits are checked
        boolean[] checked = new boolean[all.length];


        //Remove special subreddits from list and store it in "all"
        int i = 0;
        for (String s : sorted) {
            if (!s.equals("all")
                    && !s.equals("frontpage")
                    && !s.contains("+")
                    && !s.contains(".")
                    && !s.contains("/m/")) {
                all[i] = s.toLowerCase(Locale.ENGLISH);
                i++;
            }
        }

        //Remove empty entries & store which subreddits are checked
        List<String> list = new ArrayList<>();
        i = 0;
        for (String s : all) {
            if (s != null && !s.isEmpty()) {
                list.add(s);
                if (subThresholds.containsKey(s)) {
                    checked[i] = true;
                }
                i++;
            }
        }

        //Convert List back to Array
        all = list.toArray(new String[0]);

        final ArrayList<String> toCheck = new ArrayList<>(subThresholds.keySet());
        final String[] finalAll = all;
        new AlertDialogWrapper.Builder(SettingsGeneralFragment.this.context).setMultiChoiceItems(finalAll, checked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (!isChecked) {
                            toCheck.remove(finalAll[which]);
                        } else {
                            toCheck.add(finalAll[which]);
                        }
                    }
                })
                .alwaysCallMultiChoiceCallback()
                .setTitle(R.string.sub_post_notifs_title_settings)
                .setPositiveButton(context.getString(R.string.btn_add).toUpperCase(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showThresholdDialog(toCheck, false);
                            }
                        })
                .setNegativeButton(R.string.sub_post_notifs_settings_search,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new MaterialDialog.Builder(SettingsGeneralFragment.this.context).title(
                                        R.string.reorder_add_subreddit)
                                        .inputRangeRes(2, 21, R.color.md_red_500)
                                        .alwaysCallInputCallback()
                                        .input(context.getString(R.string.reorder_subreddit_name), null,
                                                false, new MaterialDialog.InputCallback() {
                                                    @Override
                                                    public void onInput(MaterialDialog dialog,
                                                                        CharSequence raw) {
                                                        input = raw.toString()
                                                                .replaceAll("\\s",
                                                                        ""); //remove whitespace from input
                                                    }
                                                })
                                        .positiveText(R.string.btn_add)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog,
                                                                @NonNull DialogAction which) {
                                                new AsyncGetSubreddit().execute(input);
                                            }
                                        })
                                        .negativeText(R.string.btn_cancel)
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog,
                                                                @NonNull DialogAction which) {

                                            }
                                        })
                                        .show();
                            }
                        })
                .show();
    }

    private void showThresholdDialog(ArrayList<String> strings, boolean search) {
        final ArrayList<String> subsRaw =
                Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));

        if (!search) {
            //NOT a sub searched for, was instead a list of all subs
            for (String raw : new ArrayList<>(subsRaw)) {
                if (!strings.contains(raw.split(":")[0])) {
                    subsRaw.remove(raw);
                }
            }
        }

        final ArrayList<String> subs = new ArrayList<>();
        for (String s : subsRaw) {
            try {
                subs.add(s.split(":")[0].toLowerCase(Locale.ENGLISH));
            } catch (Exception e) {

            }
        }

        final ArrayList<String> toAdd = new ArrayList<>();
        for (String s : strings) {
            if (!subs.contains(s.toLowerCase(Locale.ENGLISH))) {
                toAdd.add(s.toLowerCase(Locale.ENGLISH));
            }
        }
        if (!toAdd.isEmpty()) {
            new MaterialDialog.Builder(SettingsGeneralFragment.this.context).title(
                    R.string.sub_post_notifs_threshold)
                    .items(new String[]{"1", "5", "10", "20", "40", "50"})
                    .alwaysCallSingleChoiceCallback()
                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                                                   CharSequence text) {
                            for (String s : toAdd) {
                                subsRaw.add(s + ":" + text);
                            }
                            saveAndUpdateSubs(subsRaw);
                            return true;
                        }
                    })
                    .cancelable(false)
                    .show();
        } else {
            saveAndUpdateSubs(subsRaw);
        }
    }

    private void saveAndUpdateSubs(ArrayList<String> subs) {
        Reddit.appRestart.edit()
                .putString(CheckForMail.SUBS_TO_GET, Reddit.arrayToString(subs))
                .commit();
        setSubText();
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {
        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null || input.equalsIgnoreCase("friends") || input.equalsIgnoreCase(
                    "mod")) {
                ArrayList<String> singleSub = new ArrayList<>();
                singleSub.add(subreddit.getDisplayName().toLowerCase(Locale.ENGLISH));
                showThresholdDialog(singleSub, true);
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(SettingsGeneralFragment.this.context).setTitle(
                                    R.string.subreddit_err)
                                    .setMessage(context.getString(R.string.subreddit_err_msg, params[0]))
                                    .setPositiveButton(R.string.btn_ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    dialog.dismiss();

                                                }
                                            })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialog) {

                                        }
                                    })
                                    .show();
                        } catch (Exception ignored) {

                        }
                    }
                });

                return null;
            }
        }
    }

    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder, boolean isSaveToLocation) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
            Toast.makeText(context,
                    context.getString(R.string.settings_set_image_location, folder.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            ((TextView) context.findViewById(R.id.settings_general_location)).setText(folder.getAbsolutePath());
        }
    }

}
