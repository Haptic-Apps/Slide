package me.ccrama.redditslide.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class DrawerItemsDialog extends MaterialDialog {
    public DrawerItemsDialog(final Builder builder) {
        super(builder.customView(R.layout.dialog_drawer_items, false)
                .title(R.string.settings_general_title_drawer_items)
                .positiveText(android.R.string.ok)
                .canceledOnTouchOutside(false)
                .onPositive(new SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        if (SettingsThemeFragment.changed) {
                            SettingValues.prefs.edit()
                                    .putLong(SettingValues.PREF_SELECTED_DRAWER_ITEMS,
                                            SettingValues.selectedDrawerItems)
                                    .apply();
                        }
                    }
                }));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SettingValues.selectedDrawerItems == -1) {
            SettingValues.selectedDrawerItems = 0;
            for (final SettingsDrawerEnum settingDrawerItem : SettingsDrawerEnum.values()) {
                SettingValues.selectedDrawerItems += settingDrawerItem.value;
            }
            SettingValues.prefs.edit()
                    .putLong(SettingValues.PREF_SELECTED_DRAWER_ITEMS,
                            SettingValues.selectedDrawerItems)
                    .apply();
        }

        setupViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        SettingValues.prefs.edit()
                .putLong(SettingValues.PREF_SELECTED_DRAWER_ITEMS,
                        SettingValues.selectedDrawerItems)
                .apply();
    }

    private void setupViews() {
        for (final SettingsDrawerEnum settingDrawerItem : SettingsDrawerEnum.values()) {
            findViewById(settingDrawerItem.layoutId).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) findViewById(settingDrawerItem.checkboxId);
                    if (checkBox.isChecked()) {
                        SettingValues.selectedDrawerItems -= settingDrawerItem.value;
                    } else {
                        SettingValues.selectedDrawerItems += settingDrawerItem.value;
                    }
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });
            SettingsThemeFragment.changed = true;
            ((CheckBox) findViewById(settingDrawerItem.checkboxId)).setChecked(
                    (SettingValues.selectedDrawerItems & settingDrawerItem.value) != 0);
        }
    }

    public enum SettingsDrawerEnum {
        PROFILE(1, R.id.settings_drawer_profile, R.id.settings_drawer_profile_checkbox,
                R.id.prof_click),
        INBOX(1 << 1, R.id.settings_drawer_inbox, R.id.settings_drawer_inbox_checkbox, R.id.inbox),
        MULTIREDDITS(1 << 2, R.id.settings_drawer_multireddits,
                R.id.settings_drawer_multireddits_checkbox, R.id.multi),
        GOTO_PROFILE(1 << 3, R.id.settings_drawer_goto_profile,
                R.id.settings_drawer_goto_profile_checkbox, R.id.prof),
        DISCOVER(1 << 4, R.id.settings_drawer_discover, R.id.settings_drawer_discover_checkbox,
                R.id.discover);

        public long value;
        @IdRes
        public int  layoutId;
        @IdRes
        public int  checkboxId;
        @IdRes
        public int  drawerId;

        SettingsDrawerEnum(long value, @IdRes int layoutId, @IdRes int checkboxId,
                @IdRes int drawerId) {
            this.value = value;
            this.layoutId = layoutId;
            this.checkboxId = checkboxId;
            this.drawerId = drawerId;
        }
    }
}
