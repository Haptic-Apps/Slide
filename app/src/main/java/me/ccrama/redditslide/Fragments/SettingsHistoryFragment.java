package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import com.lusfold.androidkeyvaluestore.KVStore;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.UserSubscriptions;

public class SettingsHistoryFragment {

    private final Activity context;

    public SettingsHistoryFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        final SwitchCompat storeHistorySwitch = context.findViewById(R.id.settings_history_storehistory);
        final SwitchCompat storeNsfwHistorySwitch = context.findViewById(R.id.settings_history_storensfw);
        final SwitchCompat scrollSeenSwitch = context.findViewById(R.id.settings_history_scrollseen);

        final RelativeLayout clearPostsLayout = context.findViewById(R.id.settings_history_clearposts);
        final RelativeLayout clearSubsLayout = context.findViewById(R.id.settings_history_clearsubs);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Save history */
        storeHistorySwitch.setChecked(SettingValues.storeHistory);
        storeHistorySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.storeHistory = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_STORE_HISTORY, isChecked);

            if (isChecked) {
                scrollSeenSwitch.setEnabled(true);
                storeNsfwHistorySwitch.setEnabled(true);
            } else {
                storeNsfwHistorySwitch.setChecked(false);
                storeNsfwHistorySwitch.setEnabled(false);
                SettingValues.storeNSFWHistory = false;
                editSharedBooleanPreference(SettingValues.PREF_STORE_NSFW_HISTORY, false);

                scrollSeenSwitch.setChecked(false);
                scrollSeenSwitch.setEnabled(false);
                SettingValues.scrollSeen = false;
                editSharedBooleanPreference(SettingValues.PREF_SCROLL_SEEN, false);
            }
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        storeNsfwHistorySwitch.setChecked(SettingValues.storeNSFWHistory);
        storeNsfwHistorySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.storeNSFWHistory = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_STORE_NSFW_HISTORY, isChecked);
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        scrollSeenSwitch.setChecked(SettingValues.scrollSeen);
        scrollSeenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.scrollSeen = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_SCROLL_SEEN, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Clear history */
        clearPostsLayout.setOnClickListener(v -> {
            KVStore.getInstance().clearTable();
            showHistoryClearedDialog();
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        clearSubsLayout.setOnClickListener(v -> {
            UserSubscriptions.subscriptions.edit().remove("subhistory").apply();
            showHistoryClearedDialog();
        });
    }

    private void showHistoryClearedDialog() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.alert_history_cleared)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void editSharedBooleanPreference(final String settingValueString, final boolean isChecked) {
        SettingValues.prefs.edit().putBoolean(settingValueString, isChecked).apply();
    }
}
