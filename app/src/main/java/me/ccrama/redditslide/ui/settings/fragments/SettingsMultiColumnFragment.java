package me.ccrama.redditslide.ui.settings.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.preference.PrefKeys;

/**
 * Created by TacoTheDank on 05/12/2021.
 */
public class SettingsMultiColumnFragment extends PreferenceFragmentCompat {

    private SeekBarPreference landscapeSeekbar;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_multicolumn);

        landscapeSeekbar = findPreference(getString(PrefKeys.PREF_LANDSCAPE_COLUMN_NUMBER));
        if (landscapeSeekbar != null) {
            landscapeSeekbar.setOnPreferenceChangeListener((preference, newValue) -> {
                SettingsActivity.changed = true;
                return true;
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final int landscapeValue = landscapeSeekbar.getValue();
        Reddit.dpWidth = landscapeValue;
        Reddit.colors.edit()
                .putInt("tabletOVERRIDE", landscapeValue)
                .apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_title_multi_column);
    }
}
