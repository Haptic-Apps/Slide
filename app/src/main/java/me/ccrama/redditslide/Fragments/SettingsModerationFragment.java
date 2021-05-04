package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.http.NetworkException;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SettingValues.RemovalReasonType;
import me.ccrama.redditslide.SettingValues.ToolboxRemovalMessageType;
import me.ccrama.redditslide.Toolbox.Toolbox;
import me.ccrama.redditslide.UserSubscriptions;

public class SettingsModerationFragment {

    private final Activity context;

    public SettingsModerationFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        final RelativeLayout removalReasonsLayout = context.findViewById(R.id.settings_moderation_removal_reasons);
        final TextView removalReasonsCurrentView = context.findViewById(R.id.settings_moderation_removal_reasons_current);

        final SwitchCompat enableToolboxSwitch = context.findViewById(R.id.settings_moderation_toolbox_enabled);
        final RelativeLayout removalMessageLayout = context.findViewById(R.id.settings_moderation_toolbox_message);
        final TextView removalMessageCurrentView = context.findViewById(R.id.settings_moderation_toolbox_message_current);
        final SwitchCompat sendMsgAsSubredditSwitch = context.findViewById(R.id.settings_moderation_toolbox_sendMsgAsSubreddit);
        final SwitchCompat stickyMessageSwitch = context.findViewById(R.id.settings_moderation_toolbox_sticky);
        final SwitchCompat lockAfterRemovalSwitch = context.findViewById(R.id.settings_moderation_toolbox_lock);
        final RelativeLayout refreshLayout = context.findViewById(R.id.settings_moderation_toolbox_refresh);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* General (Moderation) */
        // Set up removal reason setting
        if (SettingValues.removalReasonType == RemovalReasonType.SLIDE.ordinal()) {
            removalReasonsCurrentView.setText(context.getString(R.string.settings_mod_removal_slide));
        } else if (SettingValues.removalReasonType == RemovalReasonType.TOOLBOX.ordinal()) {
            removalReasonsCurrentView.setText(context.getString(R.string.settings_mod_removal_toolbox));
        } else {
            removalReasonsCurrentView.setText(context.getString(R.string.settings_mod_removal_reddit));
        }

        removalReasonsLayout.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.removal_reason_setings, popupMenu.getMenu());
            popupMenu.getMenu().findItem(R.id.toolbox).setEnabled(SettingValues.toolboxEnabled);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.slide:
                        setModRemovalReasonType(removalReasonsCurrentView,
                                RemovalReasonType.SLIDE.ordinal(), R.string.settings_mod_removal_slide);
                        break;
                    case R.id.toolbox:
                        setModRemovalReasonType(removalReasonsCurrentView,
                                RemovalReasonType.TOOLBOX.ordinal(), R.string.settings_mod_removal_toolbox);
                        break;
                    // For implementing reddit native removal reasons:
                    /*case R.id.reddit:
                        setModRemovalReasonType(removalReasonsCurrentView,
                                RemovalReasonType.REDDIT.ordinal(), R.string.settings_mod_removal_reddit);
                        break;*/
                }
                return true;
            });
            popupMenu.show();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Toolbox */
        // Set up toolbox enabled switch
        enableToolboxSwitch.setChecked(SettingValues.toolboxEnabled);
        enableToolboxSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.toolboxEnabled = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_MOD_TOOLBOX_ENABLED, isChecked);

            removalMessageLayout.setEnabled(isChecked);
            sendMsgAsSubredditSwitch.setEnabled(isChecked);
            stickyMessageSwitch.setEnabled(isChecked);
            lockAfterRemovalSwitch.setEnabled(isChecked);
            refreshLayout.setEnabled(isChecked);

            if (!isChecked) {
                setModRemovalReasonType(removalReasonsCurrentView,
                        RemovalReasonType.SLIDE.ordinal(), R.string.settings_mod_removal_slide);
            }

            // download and cache toolbox stuff in the background unless it's already loaded
            for (String sub : UserSubscriptions.modOf) {
                Toolbox.ensureConfigCachedLoaded(sub, false);
                Toolbox.ensureUsernotesCachedLoaded(sub, false);
            }
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Set up toolbox default removal type
        removalMessageLayout.setEnabled(SettingValues.toolboxEnabled);

        if (SettingValues.toolboxMessageType == ToolboxRemovalMessageType.COMMENT.ordinal()) {
            removalMessageCurrentView.setText(context.getString(R.string.toolbox_removal_comment));
        } else if (SettingValues.toolboxMessageType == ToolboxRemovalMessageType.PM.ordinal()) {
            removalMessageCurrentView.setText(context.getString(R.string.toolbox_removal_pm));
        } else if (SettingValues.toolboxMessageType == ToolboxRemovalMessageType.BOTH.ordinal()) {
            removalMessageCurrentView.setText(context.getString(R.string.toolbox_removal_both));
        } else {
            removalMessageCurrentView.setText(context.getString(R.string.toolbox_removal_none));
        }

        removalMessageLayout.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.settings_toolbox_message, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.comment:
                        setToolboxRemovalMessageType(removalMessageCurrentView,
                                ToolboxRemovalMessageType.COMMENT.ordinal(), R.string.toolbox_removal_comment);
                        break;
                    case R.id.pm:
                        setToolboxRemovalMessageType(removalMessageCurrentView,
                                ToolboxRemovalMessageType.PM.ordinal(), R.string.toolbox_removal_pm);
                        break;
                    case R.id.both:
                        setToolboxRemovalMessageType(removalMessageCurrentView,
                                ToolboxRemovalMessageType.BOTH.ordinal(), R.string.toolbox_removal_both);
                        break;
                    case R.id.none:
                        setToolboxRemovalMessageType(removalMessageCurrentView,
                                ToolboxRemovalMessageType.NONE.ordinal(), R.string.toolbox_removal_none);
                        break;
                }
                return true;
            });
            popupMenu.show();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Set up modmail switch
        sendMsgAsSubredditSwitch.setEnabled(SettingValues.toolboxEnabled);
        sendMsgAsSubredditSwitch.setChecked(SettingValues.toolboxModmail);
        sendMsgAsSubredditSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.toolboxModmail = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_MOD_TOOLBOX_MODMAIL, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Set up sticky switch
        stickyMessageSwitch.setEnabled(SettingValues.toolboxEnabled);
        stickyMessageSwitch.setChecked(SettingValues.toolboxSticky);
        stickyMessageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.toolboxSticky = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_MOD_TOOLBOX_STICKY, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Set up lock switch
        lockAfterRemovalSwitch.setEnabled(SettingValues.toolboxEnabled);
        lockAfterRemovalSwitch.setChecked(SettingValues.toolboxLock);
        lockAfterRemovalSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.toolboxLock = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_MOD_TOOLBOX_LOCK, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Set up force refresh button
        refreshLayout.setEnabled(SettingValues.toolboxEnabled);
        refreshLayout.setOnClickListener(v ->
                new MaterialDialog.Builder(context)
                        .content(R.string.settings_mod_toolbox_refreshing)
                        .progress(false, UserSubscriptions.modOf.size() * 2)
                        .showListener(dialog ->
                                new AsyncRefreshToolboxTask(dialog).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR))
                        .cancelable(false)
                        .show());
    }

    private void setToolboxRemovalMessageType(final TextView textView, final int enumOrdinal, final int string) {
        SettingValues.toolboxMessageType = enumOrdinal;
        setBaseModerationType(textView, SettingValues.toolboxMessageType, string);
    }

    private void setModRemovalReasonType(final TextView textView, final int enumOrdinal, final int string) {
        SettingValues.removalReasonType = enumOrdinal;
        setBaseModerationType(textView, SettingValues.removalReasonType, string);
    }

    private void setBaseModerationType(final TextView textView, final int moderationType, final int string) {
        editSharedIntPreference(SettingValues.PREF_MOD_REMOVAL_TYPE, moderationType);
        textView.setText(context.getString(string));
    }

    private void editSharedIntPreference(final String settingValueString, final int i) {
        SettingValues.prefs.edit().putInt(settingValueString, i).apply();
    }

    private void editSharedBooleanPreference(final String settingValueString, final boolean isChecked) {
        SettingValues.prefs.edit().putBoolean(settingValueString, isChecked).apply();
    }

    private static class AsyncRefreshToolboxTask extends AsyncTask<Void, Void, Void> {
        final MaterialDialog dialog;

        AsyncRefreshToolboxTask(DialogInterface dialog) {
            this.dialog = (MaterialDialog) dialog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String sub : UserSubscriptions.modOf) {
                try {
                    Toolbox.downloadToolboxConfig(sub);
                } catch (NetworkException ignored) {
                }
                publishProgress();
                try {
                    Toolbox.downloadUsernotes(sub);
                } catch (NetworkException ignored) {
                }
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            dialog.incrementProgress(1);
        }
    }
}
