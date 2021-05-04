package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import me.ccrama.redditslide.Activities.Settings;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;

public class SettingsRedditFragment {

    private final Activity context;

    public SettingsRedditFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        final SwitchCompat wantToSeeNsfwSwitch = context.findViewById(R.id.settings_reddit_wantToSeeNsfwContent);
        final SwitchCompat hideAllNsfwSwitch = context.findViewById(R.id.settings_reddit_hideAllNsfw);
        final TextView hideAllNsfwText = context.findViewById(R.id.settings_reddit_hideAllNsfw_text);
        final SwitchCompat hideNsfwPrevCollectionsSwitch = context.findViewById(R.id.settings_reddit_hideNsfwPreviewCollections);
        final TextView hideNsfwPrevCollectionsText = context.findViewById(R.id.settings_reddit_hideNsfwPreviewCollections_text);
        final SwitchCompat ignoreSubMediaPrefsSwitch = context.findViewById(R.id.settings_reddit_ignoreSubMediaPrefs);

        final RelativeLayout viewRedditPrefsLayout = context.findViewById(R.id.settings_reddit_viewRedditPrefs);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* NSFW Content */
        wantToSeeNsfwSwitch.setChecked(SettingValues.showNSFWContent);
        wantToSeeNsfwSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.showNSFWContent = isChecked;
            Settings.changed = true;

            if (isChecked) {
                hideAllNsfwSwitch.setEnabled(true);
                hideAllNsfwText.setAlpha(1f);
                hideAllNsfwSwitch.setChecked(SettingValues.getIsNSFWEnabled());

                hideNsfwPrevCollectionsSwitch.setEnabled(true);
                hideNsfwPrevCollectionsText.setAlpha(1f);
                hideNsfwPrevCollectionsSwitch.setChecked(SettingValues.hideNSFWCollection);

            } else {
                hideAllNsfwSwitch.setChecked(false);
                hideAllNsfwSwitch.setEnabled(false);
                hideAllNsfwText.setAlpha(0.25f);

                hideNsfwPrevCollectionsSwitch.setChecked(false);
                hideNsfwPrevCollectionsSwitch.setEnabled(false);
                hideNsfwPrevCollectionsText.setAlpha(0.25f);
            }
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SHOW_NSFW_CONTENT, isChecked).apply();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (!wantToSeeNsfwSwitch.isChecked()) {
            hideAllNsfwSwitch.setChecked(true);
            hideAllNsfwSwitch.setEnabled(false);
            hideAllNsfwText.setAlpha(0.25f);
        } else {
            hideAllNsfwSwitch.setChecked(SettingValues.getIsNSFWEnabled());
        }
        hideAllNsfwSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.changed = true;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_NSFW_PREVIEW + Authentication.name, isChecked).apply();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (!wantToSeeNsfwSwitch.isChecked()) {
            hideNsfwPrevCollectionsSwitch.setChecked(true);
            hideNsfwPrevCollectionsSwitch.setEnabled(false);
            hideNsfwPrevCollectionsText.setAlpha(0.25f);
        } else {
            hideNsfwPrevCollectionsSwitch.setChecked(SettingValues.hideNSFWCollection);
        }
        hideNsfwPrevCollectionsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.changed = true;
            SettingValues.hideNSFWCollection = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDE_NSFW_COLLECTION, isChecked).apply();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ignoreSubMediaPrefsSwitch.setChecked(SettingValues.ignoreSubSetting);
        ignoreSubMediaPrefsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Settings.changed = true;
            SettingValues.ignoreSubSetting = isChecked;
            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IGNORE_SUB_SETTINGS, isChecked).apply();
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Open Reddit settings in browser */
        viewRedditPrefsLayout.setOnClickListener(v ->
                LinkUtil.openUrl(
                        "https://www.reddit.com/prefs/", Palette.getDefaultColor(), context));
    }
}
