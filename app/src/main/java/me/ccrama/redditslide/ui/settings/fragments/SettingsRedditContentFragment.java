package me.ccrama.redditslide.ui.settings.fragments;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.preference.PrefKeys;
import me.ccrama.redditslide.util.preference.PreferenceHelper;

/**
 * Created by TacoTheDank on 08/07/2021.
 */
public class SettingsRedditContentFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_redditcontent);

        final SwitchPreferenceCompat seeNsfwContent = findPreference(getString(PrefKeys.PREF_SEE_NSFW_CONTENT));
        seeNsfwContent.setOnPreferenceChangeListener((preference, newValue) -> {
            SettingsActivity.changed = true;
            return true;
        });

        final SwitchPreferenceCompat hideAllNsfw = findPreference(getString(PrefKeys.PREF_HIDE_ALL_NSFW));
        hideAllNsfw.setOnPreferenceChangeListener((preference, newValue) -> {
            SettingsActivity.changed = true;
            PreferenceHelper.setHideNsfwPreviews((Boolean) newValue);
            return true;
        });

        final SwitchPreferenceCompat hideNsfwPreviews = findPreference(getString(PrefKeys.PREF_HIDE_NSFW_PREVIEWS));
        hideNsfwPreviews.setOnPreferenceChangeListener((preference, newValue) -> {
            SettingsActivity.changed = true;
            return true;
        });

        final SwitchPreferenceCompat ignoreSubMediaPrefs = findPreference(getString(PrefKeys.PREF_IGNORE_SUB_MEDIA_PREFS));
        ignoreSubMediaPrefs.setOnPreferenceChangeListener((preference, newValue) -> {
            SettingsActivity.changed = true;
            return true;
        });

        final Preference viewAllRedditPrefs = findPreference(getString(PrefKeys.PREF_VIEW_ALL_REDDIT_PREFS));
        viewAllRedditPrefs.setOnPreferenceClickListener(preference -> {
            LinkUtil.openUrl("https://www.reddit.com/prefs/",
                    Palette.getDefaultColor(), getActivity());
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.content_settings);
    }
}
