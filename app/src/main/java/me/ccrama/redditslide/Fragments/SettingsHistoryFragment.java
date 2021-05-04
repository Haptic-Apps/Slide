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
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_HISTORY, isChecked).apply();

            if (isChecked) {
                scrollSeenSwitch.setEnabled(true);
                storeNsfwHistorySwitch.setEnabled(true);
            } else {
                storeNsfwHistorySwitch.setChecked(false);
                storeNsfwHistorySwitch.setEnabled(false);
                SettingValues.storeNSFWHistory = false;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_NSFW_HISTORY, false).apply();

                scrollSeenSwitch.setChecked(false);
                scrollSeenSwitch.setEnabled(false);
                SettingValues.scrollSeen = false;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, false).apply();
            }
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        storeNsfwHistorySwitch.setChecked(SettingValues.storeNSFWHistory);
        storeNsfwHistorySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.storeNSFWHistory = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_STORE_NSFW_HISTORY, isChecked).apply();
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        scrollSeenSwitch.setChecked(SettingValues.scrollSeen);
        scrollSeenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.scrollSeen = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SCROLL_SEEN, isChecked).apply();

        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Clear history */
        clearPostsLayout.setOnClickListener(v -> {
            KVStore.getInstance().clearTable();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_history_cleared)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        clearSubsLayout.setOnClickListener(v -> {
            UserSubscriptions.subscriptions.edit().remove("subhistory").apply();
            new AlertDialog.Builder(context)
                    .setTitle(R.string.alert_history_cleared)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });
    }
}
