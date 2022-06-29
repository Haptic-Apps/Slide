package me.ccrama.redditslide.ui.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

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

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.CaseInsensitiveArrayList;
import me.ccrama.redditslide.Fragments.DrawerItemsDialog;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate.FolderCallback;
import me.ccrama.redditslide.Notifications.CheckForMail;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.DialogUtil;
import me.ccrama.redditslide.util.ImageLoaderUtils;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.SortingUtil;
import me.ccrama.redditslide.util.StringUtil;
import me.ccrama.redditslide.util.TimeUtils;

import static me.ccrama.redditslide.Constants.BackButtonBehaviorOptions;
import static me.ccrama.redditslide.Constants.FAB_DISMISS;
import static me.ccrama.redditslide.Constants.FAB_POST;
import static me.ccrama.redditslide.Constants.FAB_SEARCH;
import static me.ccrama.redditslide.Constants.SUBREDDIT_SEARCH_METHOD_BOTH;
import static me.ccrama.redditslide.Constants.SUBREDDIT_SEARCH_METHOD_DRAWER;
import static me.ccrama.redditslide.Constants.SUBREDDIT_SEARCH_METHOD_TOOLBAR;

/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneralFragment<ActivityType extends AppCompatActivity & FolderCallback>
        implements FolderCallback {

    public static boolean searchChanged; //whether or not the subreddit search method changed
    private final ActivityType context;
    private String input;

    public SettingsGeneralFragment(ActivityType context) {
        this.context = context;
    }

    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        final Slider landscape = dialoglayout.findViewById(R.id.landscape);
        final CheckBox checkBox = dialoglayout.findViewById(R.id.load);
        final CheckBox sound = dialoglayout.findViewById(R.id.sound);
        final TextView notifCurrentView = context.findViewById(R.id.settings_general_notifications_current);

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
            landscape.setValue(Reddit.notificationTime / 15.0f, false);
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
                        Reddit.notifications.cancel();
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(dialoglayout);
        final Dialog dialog = builder.create();
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
                    Reddit.notifications.cancel();
                    Reddit.notifications.start();
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
                    Reddit.notifications.cancel();
                    Reddit.notifications.start();
                    dialog.dismiss();
                    if (context instanceof SettingsGeneral) {
                        notifCurrentView.setText(context.getString(R.string.settings_notification_short,
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
                    Reddit.notifications.cancel();
                    dialog.dismiss();
                    if (context instanceof SettingsGeneral) {
                        notifCurrentView.setText(R.string.settings_notifdisabled);
                    }

                }
            }
        });
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

    /* Allow SettingsGeneral and Settings Activity classes to use the same XML functionality */
    public void Bind() {
        final RelativeLayout notifLayout = context.findViewById(R.id.settings_general_notifications);
        final TextView notifCurrentView = context.findViewById(R.id.settings_general_notifications_current);
        final RelativeLayout subNotifLayout = context.findViewById(R.id.settings_general_sub_notifications);
        final TextView defaultSortingCurrentView = context.findViewById(R.id.settings_general_sorting_current);

        context.findViewById(R.id.settings_general_drawer_items)
                .setOnClickListener(v ->
                        new DrawerItemsDialog(new MaterialDialog.Builder(context)).show());

        {
            SwitchCompat immersiveModeSwitch = context.findViewById(R.id.settings_general_immersivemode);
            if (immersiveModeSwitch != null) {
                immersiveModeSwitch.setChecked(SettingValues.immersiveMode);
                immersiveModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SettingsThemeFragment.changed = true;
                    SettingValues.immersiveMode = isChecked;
                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_IMMERSIVE_MODE, isChecked)
                            .apply();
                });
            }
        }
        {
            SwitchCompat highClrSpaceSwitch = context.findViewById(R.id.settings_general_high_colorspace);
            if (highClrSpaceSwitch != null) {
                highClrSpaceSwitch.setChecked(SettingValues.highColorspaceImages);
                highClrSpaceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SettingsThemeFragment.changed = true;
                    SettingValues.highColorspaceImages = isChecked;

                    Reddit application = (Reddit) context.getApplication();
                    ImageLoaderUtils.initImageLoader(application.getApplicationContext());
                    application.defaultImageLoader = ImageLoaderUtils.imageLoader;

                    SettingValues.prefs.edit()
                            .putBoolean(SettingValues.PREF_HIGH_COLORSPACE_IMAGES, isChecked)
                            .apply();
                });
            }
        }

        {
            SwitchCompat forceLangSwitch = context.findViewById(R.id.settings_general_forcelanguage);

            if (forceLangSwitch != null) {
                forceLangSwitch.setChecked(SettingValues.overrideLanguage);
                forceLangSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            SwitchCompat alwaysShowFabSwitch = context.findViewById(R.id.settings_general_always_show_fab);

            if (alwaysShowFabSwitch != null) {
                alwaysShowFabSwitch.setChecked(SettingValues.alwaysShowFAB);
                alwaysShowFabSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            SwitchCompat showDownloadBtnSwitch = context.findViewById(R.id.settings_general_show_download_button);

            if (showDownloadBtnSwitch != null) {
                showDownloadBtnSwitch.setChecked(SettingValues.imageDownloadButton);
                showDownloadBtnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            SwitchCompat subfolderSwitch = context.findViewById(R.id.settings_general_subfolder);

            if (subfolderSwitch != null) {
                subfolderSwitch.setChecked(SettingValues.imageSubfolders);
                subfolderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

        final RelativeLayout setSaveLocationLayout = context.findViewById(R.id.settings_general_set_save_location);
        if (setSaveLocationLayout != null) {
            setSaveLocationLayout.setOnClickListener(v ->
                    DialogUtil.showFolderChooserDialog(context));
        }


        TextView setSaveLocationView = context.findViewById(R.id.settings_general_set_save_location_view);
        if (setSaveLocationView != null) {
            String loc = Reddit.appRestart.getString("imagelocation",
                    context.getString(R.string.settings_image_location_unset));
            setSaveLocationView.setText(loc);
        }

        final SwitchCompat expandedMenuSwitch = context.findViewById(R.id.settings_general_expandedmenu);
        if (expandedMenuSwitch != null) {
            expandedMenuSwitch.setChecked(SettingValues.expandedToolbar);
            expandedMenuSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SettingValues.expandedToolbar = isChecked;
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_EXPANDED_TOOLBAR, isChecked)
                        .apply();
            });
        }

        final RelativeLayout viewTypeLayout = context.findViewById(R.id.settings_general_viewtype);
        if (viewTypeLayout != null) {
            viewTypeLayout.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Intent i = new Intent(context, SettingsViewType.class);
                    context.startActivity(i);
                }
            });
        }

        //FAB multi choice//
        final RelativeLayout fabLayout = context.findViewById(R.id.settings_general_fab);
        final TextView currentFabView = context.findViewById(R.id.settings_general_fab_current);
        if (currentFabView != null && fabLayout != null) {
            currentFabView.setText(
                    SettingValues.fab
                            ? SettingValues.fabType == FAB_DISMISS
                            ? context.getString(R.string.fab_hide)
                            : context.getString(R.string.fab_create)
                            : context.getString(R.string.fab_disabled));

            fabLayout.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.fab_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.disabled:
                            SettingValues.fab = false;
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_FAB, false)
                                    .apply();
                            break;
                        case R.id.hide:
                            SettingValues.fab = true;
                            SettingValues.fabType = FAB_DISMISS;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_FAB_TYPE, FAB_DISMISS)
                                    .apply();
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_FAB, true)
                                    .apply();
                            break;
                        case R.id.create:
                            SettingValues.fab = true;
                            SettingValues.fabType = FAB_POST;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_FAB_TYPE, FAB_POST)
                                    .apply();
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_FAB, true)
                                    .apply();
                            break;
                        case R.id.search:
                            SettingValues.fab = true;
                            SettingValues.fabType = FAB_SEARCH;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_FAB_TYPE, FAB_SEARCH)
                                    .apply();
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_FAB, true)
                                    .apply();
                            break;
                    }
                    if (SettingValues.fab) {
                        if (SettingValues.fabType == FAB_DISMISS) {
                            currentFabView.setText(R.string.fab_hide);
                        } else if (SettingValues.fabType == FAB_POST) {
                            currentFabView.setText(R.string.fab_create);
                        } else {
                            currentFabView.setText(R.string.fab_search);
                        }
                    } else {
                        currentFabView.setText(R.string.fab_disabled);
                    }

                    return true;
                });

                popup.show();
            });
        }

        //SettingValues.subredditSearchMethod == 1 for drawer, 2 for toolbar, 3 for both
        final TextView currentMethodTitle = context.findViewById(R.id.settings_general_subreddit_search_method_current);
        if (currentMethodTitle != null) {
            switch (SettingValues.subredditSearchMethod) {
                case SUBREDDIT_SEARCH_METHOD_DRAWER:
                    currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_drawer));
                    break;
                case SUBREDDIT_SEARCH_METHOD_TOOLBAR:
                    currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_toolbar));
                    break;
                case SUBREDDIT_SEARCH_METHOD_BOTH:
                    currentMethodTitle.setText(context.getString(R.string.subreddit_search_method_both));
                    break;
            }
        }

        final RelativeLayout currentMethodLayout = context.findViewById(R.id.settings_general_subreddit_search_method);
        if (currentMethodLayout != null) {
            currentMethodLayout.setOnClickListener(v -> {
                final PopupMenu popup = new PopupMenu(SettingsGeneralFragment.this.context, v);
                popup.getMenuInflater().inflate(R.menu.subreddit_search_settings, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.subreddit_search_drawer:
                            SettingValues.subredditSearchMethod =
                                    SUBREDDIT_SEARCH_METHOD_DRAWER;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                            SUBREDDIT_SEARCH_METHOD_DRAWER)
                                    .apply();
                            searchChanged = true;
                            break;
                        case R.id.subreddit_search_toolbar:
                            SettingValues.subredditSearchMethod =
                                    SUBREDDIT_SEARCH_METHOD_TOOLBAR;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                            SUBREDDIT_SEARCH_METHOD_TOOLBAR)
                                    .apply();
                            searchChanged = true;
                            break;
                        case R.id.subreddit_search_both:
                            SettingValues.subredditSearchMethod =
                                    SUBREDDIT_SEARCH_METHOD_BOTH;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_SUBREDDIT_SEARCH_METHOD,
                                            SUBREDDIT_SEARCH_METHOD_BOTH)
                                    .apply();
                            searchChanged = true;
                            break;
                    }

                    switch (SettingValues.subredditSearchMethod) {
                        case SUBREDDIT_SEARCH_METHOD_DRAWER:
                            currentMethodTitle.setText(
                                    context.getString(R.string.subreddit_search_method_drawer));
                            break;
                        case SUBREDDIT_SEARCH_METHOD_TOOLBAR:
                            currentMethodTitle.setText(
                                    context.getString(R.string.subreddit_search_method_toolbar));
                            break;
                        case SUBREDDIT_SEARCH_METHOD_BOTH:
                            currentMethodTitle.setText(
                                    context.getString(R.string.subreddit_search_method_both));
                            break;
                    }
                    return true;
                });
                popup.show();
            });
        }

        final TextView currentBackButtonTitle =
                context.findViewById(R.id.settings_general_back_button_behavior_current);
        if (SettingValues.backButtonBehavior
                == BackButtonBehaviorOptions.ConfirmExit.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_confirm_exit));
        } else if (SettingValues.backButtonBehavior
                == BackButtonBehaviorOptions.OpenDrawer.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_drawer));
        } else if (SettingValues.backButtonBehavior
                == BackButtonBehaviorOptions.GotoFirst.getValue()) {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_goto_first));
        } else {
            currentBackButtonTitle.setText(context.getString(R.string.back_button_behavior_default));
        }

        final RelativeLayout currentBackButtonLayout = context.findViewById(R.id.settings_general_back_button_behavior);
        currentBackButtonLayout.setOnClickListener(v -> {
            final PopupMenu popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.back_button_behavior_settings, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.back_button_behavior_default:
                        SettingValues.backButtonBehavior =
                                BackButtonBehaviorOptions.Default.getValue();
                        SettingValues.prefs.edit()
                                .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                        BackButtonBehaviorOptions.Default.getValue())
                                .apply();
                        break;
                    case R.id.back_button_behavior_confirm_exit:
                        SettingValues.backButtonBehavior =
                                BackButtonBehaviorOptions.ConfirmExit.getValue();
                        SettingValues.prefs.edit()
                                .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                        BackButtonBehaviorOptions.ConfirmExit.getValue())
                                .apply();
                        break;
                    case R.id.back_button_behavior_open_drawer:
                        SettingValues.backButtonBehavior =
                                BackButtonBehaviorOptions.OpenDrawer.getValue();
                        SettingValues.prefs.edit()
                                .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                        BackButtonBehaviorOptions.OpenDrawer.getValue())
                                .apply();
                        break;
                    case R.id.back_button_behavior_goto_first:
                        SettingValues.backButtonBehavior =
                                BackButtonBehaviorOptions.GotoFirst.getValue();
                        SettingValues.prefs.edit()
                                .putInt(SettingValues.PREF_BACK_BUTTON_BEHAVIOR,
                                        BackButtonBehaviorOptions.GotoFirst.getValue())
                                .apply();
                        break;
                }

                if (SettingValues.backButtonBehavior
                        == BackButtonBehaviorOptions.ConfirmExit.getValue()) {
                    currentBackButtonTitle.setText(
                            context.getString(R.string.back_button_behavior_confirm_exit));
                } else if (SettingValues.backButtonBehavior
                        == BackButtonBehaviorOptions.OpenDrawer.getValue()) {
                    currentBackButtonTitle.setText(
                            context.getString(R.string.back_button_behavior_drawer));
                } else if (SettingValues.backButtonBehavior
                        == BackButtonBehaviorOptions.GotoFirst.getValue()) {
                    currentBackButtonTitle.setText(
                            context.getString(R.string.back_button_behavior_goto_first));
                } else {
                    currentBackButtonTitle.setText(
                            context.getString(R.string.back_button_behavior_default));
                }
                return true;
            });
            popup.show();
        });

        if (notifCurrentView != null &&
                context.findViewById(R.id.settings_general_sub_notifs_current) != null) {
            if (Reddit.notificationTime > 0) {
                notifCurrentView.setText(context.getString(R.string.settings_notification_short,
                        TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime,
                                context.getBaseContext())));
                setSubText();
            } else {
                notifCurrentView.setText(R.string.settings_notifdisabled);
                ((TextView) context.findViewById(R.id.settings_general_sub_notifs_current)).setText(
                        R.string.settings_enable_notifs);
            }
        }

        if (Authentication.isLoggedIn) {
            if (notifLayout != null) {
                notifLayout.setOnClickListener(v -> {
                    final LayoutInflater inflater = context.getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                    setupNotificationSettings(dialoglayout, SettingsGeneralFragment.this.context);
                });
            }
            if (subNotifLayout != null) {
                subNotifLayout.setOnClickListener(v -> showSelectDialog());
            }
        } else {
            if (notifLayout != null) {
                notifLayout.setEnabled(false);
                notifLayout.setAlpha(0.25f);
            }
            if (subNotifLayout != null) {
                subNotifLayout.setEnabled(false);
                subNotifLayout.setAlpha(0.25f);
            }
        }

        if (defaultSortingCurrentView != null) {
            defaultSortingCurrentView.setText(
                    SortingUtil.getSortingStrings()[SortingUtil.getSortingId("")]);
        }

        {
            if (context.findViewById(R.id.settings_general_sorting) != null) {
                context.findViewById(R.id.settings_general_sorting).setOnClickListener(v -> {
                    final DialogInterface.OnClickListener l2 = (dialogInterface, i) -> {
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

                        if (defaultSortingCurrentView != null) {
                            defaultSortingCurrentView.setText(
                                    SortingUtil.getSortingStrings()[SortingUtil.getSortingId("")]);
                        }
                    };

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

                    new AlertDialog.Builder(SettingsGeneralFragment.this.context)
                            .setTitle(R.string.sorting_choose)
                            .setSingleChoiceItems(
                                    sortingStrings.toArray(new String[0]),
                                    SortingUtil.getSortingId(""),
                                    l2)
                            .show();
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

            final TextView sortingCurrentCommentView = context.findViewById(R.id.settings_general_sorting_current_comment);
            if (sortingCurrentCommentView != null) {
                sortingCurrentCommentView.setText(
                        SortingUtil.getSortingCommentsStrings()[i2]);
            }

            if (context.findViewById(R.id.settings_general_sorting_comment) != null) {
                context.findViewById(R.id.settings_general_sorting_comment).setOnClickListener(v -> {
                    final DialogInterface.OnClickListener l2 = (dialogInterface, i) -> {
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
                        if (sortingCurrentCommentView != null) {
                            sortingCurrentCommentView.setText(
                                    SortingUtil.getSortingCommentsStrings()[i]);
                        }
                    };

                    Resources res = context.getBaseContext().getResources();

                    new AlertDialog.Builder(SettingsGeneralFragment.this.context)
                            .setTitle(R.string.sorting_choose)
                            .setSingleChoiceItems(
                                    new String[]{
                                            res.getString(R.string.sorting_best),
                                            res.getString(R.string.sorting_top),
                                            res.getString(R.string.sorting_new),
                                            res.getString(R.string.sorting_controversial),
                                            res.getString(R.string.sorting_old),
                                            res.getString(R.string.sorting_ama)
                                    }, i2, l2)
                            .show();
                });
            }
        }
    }

    private void askTimePeriod() {
        final TextView defaultSortingCurrentView = context.findViewById(R.id.settings_general_sorting_current);
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
                defaultSortingCurrentView.setText(
                        SortingUtil.getSortingStrings()[SortingUtil.getSortingId("")]
                                + " > "
                                + SortingUtil.getSortingTimesStrings()[SortingUtil.getSortingTimeId(
                                "")]);
            }
        };

        new AlertDialog.Builder(SettingsGeneralFragment.this.context)
                .setTitle(R.string.sorting_choose)
                .setSingleChoiceItems(
                        SortingUtil.getSortingTimesStrings(),
                        SortingUtil.getSortingTimeId(""),
                        l2)
                .show();
    }

    private void setSubText() {
        ArrayList<String> rawSubs =
                StringUtil.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
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
            subText = subs.substring(0, subs.toString().length() - 2);
        }
        ((TextView) context.findViewById(R.id.settings_general_sub_notifs_current)).setText(subText);
    }

    private void showSelectDialog() {
        ArrayList<String> rawSubs =
                StringUtil.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));
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
        new AlertDialog.Builder(SettingsGeneralFragment.this.context)
                .setMultiChoiceItems(finalAll, checked, (dialog, which, isChecked) -> {
                    if (!isChecked) {
                        toCheck.remove(finalAll[which]);
                    } else {
                        toCheck.add(finalAll[which]);
                    }
                })
                .setTitle(R.string.sub_post_notifs_title_settings)
                .setPositiveButton(context.getString(R.string.btn_add).toUpperCase(), (dialog, which) ->
                        showThresholdDialog(toCheck, false))
                .setNegativeButton(R.string.sub_post_notifs_settings_search, (dialog, which) ->
                        new MaterialDialog.Builder(SettingsGeneralFragment.this.context)
                                .title(R.string.reorder_add_subreddit)
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
                                .show())
                .show();
    }

    private void showThresholdDialog(ArrayList<String> strings, boolean search) {
        final ArrayList<String> subsRaw =
                StringUtil.stringToArray(Reddit.appRestart.getString(CheckForMail.SUBS_TO_GET, ""));

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
                .putString(CheckForMail.SUBS_TO_GET, StringUtil.arrayToString(subs))
                .commit();
        setSubText();
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialogCreate dialog,
                                  @NonNull File folder, boolean isSaveToLocation) {
        Reddit.appRestart.edit().putString("imagelocation", folder.getAbsolutePath()).apply();
        Toast.makeText(context,
                context.getString(R.string.settings_set_image_location, folder.getAbsolutePath()),
                Toast.LENGTH_LONG).show();
        ((TextView) context.findViewById(R.id.settings_general_set_save_location_view)).setText(folder.getAbsolutePath());
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialogCreate dialog) {
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
                context.runOnUiThread(() -> {
                    try {
                        new AlertDialog.Builder(SettingsGeneralFragment.this.context)
                                .setTitle(R.string.subreddit_err)
                                .setMessage(context.getString(R.string.subreddit_err_msg, params[0]))
                                .setPositiveButton(R.string.btn_ok, (dialog, which) ->
                                        dialog.dismiss())
                                .setOnDismissListener(null)
                                .show();
                    } catch (Exception ignored) {
                    }
                });

                return null;
            }
        }
    }

}
