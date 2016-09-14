package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
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
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.Slider;

import net.dean.jraw.models.CommentSort;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Constants;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.OnSingleClickListener;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityAnim
        implements FolderChooserDialogCreate.FolderCallback {

    public static boolean searchChanged; //whether or not the subreddit search method changed

    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
        final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);
        final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);

        final CheckBox sound = (CheckBox) dialoglayout.findViewById(R.id.sound);

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
                    if (context instanceof SettingsGeneral) {
                        ((TextView) context.findViewById(R.id.notifications_current)).setText(
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
                    if (context instanceof SettingsGeneral) {
                        ((TextView) context.findViewById(R.id.notifications_current)).setText(
                                R.string.settings_notifdisabled);
                    }

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
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_OVERRIDE_LANGUAGE, isChecked)
                            .apply();
                }
            });
        }
        {
            findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FolderChooserDialogCreate.Builder(SettingsGeneral.this).chooseButton(
                            R.string.btn_select)  // changes label of the choose button
                            .initialPath(Environment.getExternalStorageDirectory()
                                    .getPath())  // changes initial path, defaults to external storage directory
                            .show();
                }
            });
        }
        String loc = Reddit.appRestart.getString("imagelocation",
                getString(R.string.settings_image_location_unset));
        ((TextView) findViewById(R.id.location)).setText(loc);
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.expandedmenu);

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

        findViewById(R.id.viewtype).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(SettingsGeneral.this, SettingsViewType.class);
                startActivity(i);
            }
        });

        //FAB multi choice//
        ((TextView) findViewById(R.id.fab_current)).setText(
                SettingValues.fab ? (SettingValues.fabType == Constants.FAB_DISMISS ? getString(
                        R.string.fab_hide) : getString(R.string.fab_create))
                        : getString(R.string.fab_disabled));

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
                        }
                        ((TextView) findViewById(R.id.fab_current)).setText(
                                SettingValues.fab ? (SettingValues.fabType == Constants.FAB_DISMISS
                                        ? getString(R.string.fab_hide)
                                        : getString(R.string.fab_create))
                                        : getString(R.string.fab_disabled));

                        return true;
                    }
                });

                popup.show();
            }
        });

        //SettingValues.subredditSearchMethod == 1 for drawer, 2 for toolbar, 3 for both
        final TextView currentMethodTitle =
                (TextView) findViewById(R.id.subreddit_search_method_current);
        if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
            currentMethodTitle.setText(getString(R.string.subreddit_search_method_drawer));
        } else if (SettingValues.subredditSearchMethod
                == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
            currentMethodTitle.setText(getString(R.string.subreddit_search_method_toolbar));
        } else if (SettingValues.subredditSearchMethod == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
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
                                SettingValues.subredditSearchMethod =
                                        Constants.SUBREDDIT_SEARCH_METHOD_DRAWER;
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                Constants.SUBREDDIT_SEARCH_METHOD_DRAWER)
                                        .apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                            case R.id.subreddit_search_toolbar:
                                SettingValues.subredditSearchMethod =
                                        Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR;
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR)
                                        .apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                            case R.id.subreddit_search_both:
                                SettingValues.subredditSearchMethod =
                                        Constants.SUBREDDIT_SEARCH_METHOD_BOTH;
                                SettingValues.prefs.edit()
                                        .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                                Constants.SUBREDDIT_SEARCH_METHOD_BOTH)
                                        .apply();
                                SettingsGeneral.searchChanged = true;
                                break;
                        }

                        if (SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_DRAWER) {
                            currentMethodTitle.setText(
                                    getString(R.string.subreddit_search_method_drawer));
                        } else if (SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR) {
                            currentMethodTitle.setText(
                                    getString(R.string.subreddit_search_method_toolbar));
                        } else if (SettingValues.subredditSearchMethod
                                == Constants.SUBREDDIT_SEARCH_METHOD_BOTH) {
                            currentMethodTitle.setText(
                                    getString(R.string.subreddit_search_method_both));
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
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_EXIT, isChecked)
                            .apply();
                }
            });
        }

        if (Reddit.notificationTime > 0) {
            ((TextView) findViewById(R.id.notifications_current)).setText(
                    getString(R.string.settings_notification_short,
                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                                    getBaseContext())));
            setSubText();
        } else {
            ((TextView) findViewById(R.id.notifications_current)).setText(
                    R.string.settings_notifdisabled);
            ((TextView) findViewById(R.id.sub_notifs_current)).setText(
                    R.string.settings_enable_notifs);
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
            findViewById(R.id.sub_notifications).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSelectDialog();
                }
            });
        } else {
            findViewById(R.id.notifications).setEnabled(false);
            findViewById(R.id.notifications).setAlpha(0.25f);
            findViewById(R.id.sub_notifications).setEnabled(false);
            findViewById(R.id.sub_notifications).setAlpha(0.25f);
        }

        ((TextView) findViewById(R.id.sorting_current)).setText(
                Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId("")]);
        {
            findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final DialogInterface.OnClickListener l2 =
                            new DialogInterface.OnClickListener() {

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
                                            askTimePeriod();
                                            return;
                                        case 4:
                                            Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                            askTimePeriod();
                                            return;
                                    }
                                    SettingValues.prefs.edit()
                                            .putString("defaultSorting",
                                                    Reddit.defaultSorting.name())
                                            .apply();
                                    SettingValues.defaultSorting = Reddit.defaultSorting;

                                    ((TextView) findViewById(R.id.sorting_current)).setText(
                                            Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId("")]);
                                }
                            };
                    AlertDialogWrapper.Builder builder =
                            new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(
                            Reddit.getSortingStrings(getBaseContext()),
                            Reddit.getSortingId(""), l2);
                    builder.show();
                }
            });
        }
        {
            final int i2 = SettingValues.defaultCommentSorting == CommentSort.CONFIDENCE ? 0
                    : SettingValues.defaultCommentSorting == CommentSort.TOP ? 1
                            : SettingValues.defaultCommentSorting == CommentSort.NEW ? 2
                                    : SettingValues.defaultCommentSorting
                                            == CommentSort.CONTROVERSIAL ? 3
                                            : SettingValues.defaultCommentSorting == CommentSort.OLD
                                                    ? 4 : SettingValues.defaultCommentSorting
                                                    == CommentSort.QA ? 5 : 0;
            ((TextView) findViewById(R.id.sorting_current_comment)).setText(
                    Reddit.getSortingStringsComments(getBaseContext())[i2]);

            findViewById(R.id.sorting_comment).setOnClickListener(new View.OnClickListener() {
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
                                    ((TextView) findViewById(R.id.sorting_current_comment)).setText(
                                            Reddit.getSortingStringsComments(getBaseContext())[i]);
                                }
                            };

                    AlertDialogWrapper.Builder builder =
                            new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    Resources res = getBaseContext().getResources();
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

    private void askTimePeriod() {
        final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Reddit.timePeriod = TimePeriod.HOUR;
                        break;
                    case 1:
                        Reddit.timePeriod = TimePeriod.DAY;
                        break;
                    case 2:
                        Reddit.timePeriod = TimePeriod.WEEK;
                        break;
                    case 3:
                        Reddit.timePeriod = TimePeriod.MONTH;
                        break;
                    case 4:
                        Reddit.timePeriod = TimePeriod.YEAR;
                        break;
                    case 5:
                        Reddit.timePeriod = TimePeriod.ALL;
                        break;
                }
                SettingValues.prefs.edit()
                        .putString("defaultSorting", Reddit.defaultSorting.name())
                        .apply();
                SettingValues.prefs.edit()
                        .putString("timePeriod", Reddit.timePeriod.name())
                        .apply();
                SettingValues.defaultSorting = Reddit.defaultSorting;
                SettingValues.timePeriod = Reddit.timePeriod;
                ((TextView) findViewById(R.id.sorting_current)).setText(
                        Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId(
                                "")] + " > " + Reddit.getSortingStringsTime(getBaseContext())[Reddit.getSortingIdTime("")]);
            }
        };
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
        builder.setTitle(R.string.sorting_choose);
        builder.setSingleChoiceItems(Reddit.getSortingStringsTime(getBaseContext()),
                Reddit.getSortingIdTime(""), l2);
        builder.show();
    }

    private void setSubText() {
        ArrayList<String> rawSubs =
                Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
        String subText = getString(R.string.sub_post_notifs_settings_none);
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
        ((TextView) findViewById(R.id.sub_notifs_current)).setText(subText);
    }

    String input;

    public void showSelectDialog() {
        ArrayList<String> rawSubs =
                Reddit.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
        HashMap<String, Integer> subThresholds = new HashMap<>();
        for (String s : rawSubs) {
            try {
                String[] split = s.split(":");
                subThresholds.put(split[0].toLowerCase(), Integer.valueOf(split[1]));
            } catch (Exception ignored) {

            }
        }

        //List of all subreddits of the multi
        List<String> sorted = new ArrayList<>();
        //Add all user subs that aren't already on the list
        for (String s : UserSubscriptions.sort(UserSubscriptions.getSubscriptions(this))) {
            sorted.add(s);
        }

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
                all[i] = s.toLowerCase();
                i++;
            }
        }

        //Remove empty entries & store which subreddits are checked
        List<String> list = new ArrayList<>();
        i = 0;
        for (String s : all) {
            if (s != null && !s.isEmpty()) {
                list.add(s);
                if (subThresholds.keySet().contains(s)) {
                    checked[i] = true;
                }
                i++;
            }
        }

        //Convert List back to Array
        all = list.toArray(new String[list.size()]);

        final ArrayList<String> toCheck = new ArrayList<>(subThresholds.keySet());
        final String[] finalAll = all;
        new AlertDialogWrapper.Builder(this).setMultiChoiceItems(finalAll, checked,
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
                .setPositiveButton(getString(R.string.btn_add).toUpperCase(),
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
                                new MaterialDialog.Builder(SettingsGeneral.this).title(
                                        R.string.reorder_add_subreddit)
                                        .inputRangeRes(2, 21, R.color.md_red_500)
                                        .alwaysCallInputCallback()
                                        .input(getString(R.string.reorder_subreddit_name), null,
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
                                            public void onClick(MaterialDialog dialog,
                                                    DialogAction which) {
                                                new AsyncGetSubreddit().execute(input);
                                            }
                                        })
                                        .negativeText(R.string.btn_cancel)
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(MaterialDialog dialog,
                                                    DialogAction which) {

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
                subs.add(s.split(":")[0].toLowerCase());
            } catch (Exception e) {

            }
        }

        final ArrayList<String> toAdd = new ArrayList<>();
        for (String s : strings) {
            if (!subs.contains(s.toLowerCase())) {
                toAdd.add(s.toLowerCase());
            }
        }
        if (!toAdd.isEmpty()) {
            new MaterialDialog.Builder(SettingsGeneral.this).title(
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
                singleSub.add(subreddit.getDisplayName().toLowerCase());
                showThresholdDialog(singleSub, true);
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(SettingsGeneral.this).setTitle(
                                    R.string.subreddit_err)
                                    .setMessage(getString(R.string.subreddit_err_msg, params[0]))
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

    @Override
    public void onFolderSelection(FolderChooserDialogCreate dialog, File folder) {
        if (folder != null) {
            Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
            Toast.makeText(this,
                    getString(R.string.settings_set_image_location, folder.getAbsolutePath()),
                    Toast.LENGTH_LONG).show();
            ((TextView) findViewById(R.id.location)).setText(folder.getAbsolutePath());
        }
    }
}