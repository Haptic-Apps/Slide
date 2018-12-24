package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Toolbox.Toolbox;
import me.ccrama.redditslide.UserSubscriptions;

public class SettingsModerationFragment {
    private Activity context;

    public SettingsModerationFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        { // Set up removal reason setting
            final TextView removalReasonsCurrent = context.findViewById(R.id.settings_moderation_removal_reasons_current);

            if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.SLIDE.ordinal()) {
                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_slide));
            } else if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.TOOLBOX.ordinal()) {
                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_toolbox));
            } else {
                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_reddit));
            }

            context.findViewById(R.id.settings_moderation_removal_reasons).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.removal_reason_setings, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.slide:
                                    SettingValues.removalReasonType = SettingValues.RemovalReasonType.SLIDE.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.removalReasonType).apply();
                                    break;
                                case R.id.toolbox:
                                    SettingValues.removalReasonType = SettingValues.RemovalReasonType.TOOLBOX.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.removalReasonType).apply();
                                    break;
                                // For implementing reddit native removal reasons:
                                /*case R.id.reddit:
                                    SettingValues.removalReasonType = SettingValues.RemovalReasonType.REDDIT.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.removalReasonType).apply();
                                    break;*/
                            }

                            if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.SLIDE.ordinal()) {
                                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_slide));
                            } else if (SettingValues.removalReasonType
                                    == SettingValues.RemovalReasonType.TOOLBOX.ordinal()) {
                                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_toolbox));
                            } else {
                                removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_reddit));
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        { // Set up toolbox enabled switch
            final SwitchCompat toolboxEnabled = context.findViewById(R.id.settings_moderation_toolbox_enabled);
            toolboxEnabled.setChecked(SettingValues.toolboxEnabled);
            toolboxEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.toolboxEnabled = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_MOD_TOOLBOX_ENABLED, isChecked).apply();
                    for (String sub : UserSubscriptions.modOf) {
                        Toolbox.ensureConfigCachedLoaded(sub);
                        Toolbox.ensureUsernotesCachedLoaded(sub);
                    }
                }
            });
        }

        { // Set up toolbox default removal type
            final TextView removalMessageCurrent =
                    context.findViewById(R.id.settings_moderation_toolbox_message_current);

            if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.COMMENT.ordinal()) {
                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_comment));
            } else if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.PM.ordinal()) {
                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_pm));
            } else if (SettingValues.toolboxMessageType == SettingValues.ToolboxRemovalMessageType.BOTH.ordinal()) {
                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_both));
            } else {
                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_none));
            }

            context.findViewById(R.id.settings_moderation_toolbox_message).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.getMenuInflater().inflate(R.menu.settings_toolbox_message, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.comment:
                                    SettingValues.toolboxMessageType =
                                            SettingValues.ToolboxRemovalMessageType.COMMENT.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.toolboxMessageType).apply();
                                    break;
                                case R.id.pm:
                                    SettingValues.toolboxMessageType =
                                            SettingValues.ToolboxRemovalMessageType.PM.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.toolboxMessageType).apply();
                                    break;
                                case R.id.both:
                                    SettingValues.toolboxMessageType =
                                            SettingValues.ToolboxRemovalMessageType.BOTH.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.toolboxMessageType).apply();
                                    break;
                                case R.id.none:
                                    SettingValues.toolboxMessageType =
                                            SettingValues.ToolboxRemovalMessageType.NONE.ordinal();
                                    SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                            SettingValues.toolboxMessageType).apply();
                                    break;
                            }

                            if (SettingValues.toolboxMessageType
                                    == SettingValues.ToolboxRemovalMessageType.COMMENT.ordinal()) {
                                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_comment));
                            } else if (SettingValues.toolboxMessageType
                                    == SettingValues.ToolboxRemovalMessageType.PM.ordinal()) {
                                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_pm));
                            } else if (SettingValues.toolboxMessageType
                                    == SettingValues.ToolboxRemovalMessageType.BOTH.ordinal()) {
                                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_both));
                            } else {
                                removalMessageCurrent.setText(context.getString(R.string.toolbox_removal_none));
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        { // Set up modmail switch
            final SwitchCompat modmail = context.findViewById(R.id.settings_moderation_toolbox_modmail);
            modmail.setChecked(SettingValues.toolboxModmail);
            modmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.toolboxModmail = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_MOD_TOOLBOX_MODMAIL, isChecked).apply();
                }
            });
        }

        { // Set up sticky switch
            final SwitchCompat stickyMessage = context.findViewById(R.id.settings_moderation_toolbox_sticky);
            stickyMessage.setChecked(SettingValues.toolboxSticky);
            stickyMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.toolboxSticky = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_MOD_TOOLBOX_STICKY, isChecked).apply();
                }
            });
        }

        { // Set up lock switch
            final SwitchCompat lock = context.findViewById(R.id.settings_moderation_toolbox_lock);
            lock.setChecked(SettingValues.toolboxLock);
            lock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.toolboxLock = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_MOD_TOOLBOX_LOCK, isChecked).apply();
                }
            });
        }

        { // Set up force refresh button
            final RelativeLayout refresh = context.findViewById(R.id.settings_moderation_toolbox_refresh);
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(context)
                            .content(R.string.settings_mod_toolbox_refreshing)
                            .progress(false, UserSubscriptions.modOf.size() * 2)
                            .showListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    new AsyncRefreshToolboxTask(dialog).execute();
                                }
                            })
                            .cancelable(false)
                            .show();
                }
            });
        }
    }

    private static class AsyncRefreshToolboxTask extends AsyncTask<Void, Void, Void> {
        final MaterialDialog dialog;

        AsyncRefreshToolboxTask(DialogInterface dialog) {
            this.dialog = (MaterialDialog) dialog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String sub : UserSubscriptions.modOf) {
                Toolbox.downloadToolboxConfig(sub);
                publishProgress();
                Toolbox.downloadUsernotes(sub);
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
