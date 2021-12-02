package me.ccrama.redditslide.ui.settings.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.preference.PrefKeys;
import me.ccrama.redditslide.util.preference.PreferenceHelper;

/**
 * Created by TacoTheDank on 07/29/2021.
 */
public class SettingsDataSavingFragment extends PreferenceFragmentCompat {

    private ListPreference imageQuality;
    private SwitchPreferenceCompat lowQualityVideos;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_datasaving);

        final String enableDataSavingKey = getString(PrefKeys.PREF_ENABLE_DATA_SAVING);
        final ListPreference enableDataSaving = findPreference(enableDataSavingKey);
        imageQuality = findPreference(getString(PrefKeys.PREF_IMAGE_QUALITY));
        lowQualityVideos = findPreference(getString(PrefKeys.PREF_LOW_QUALITY_VIDEOS));

        final boolean isDataSavingNever = !PreferenceHelper.isDataSavingNever();
        imageQuality.setEnabled(isDataSavingNever);
        lowQualityVideos.setEnabled(isDataSavingNever);

        if (enableDataSaving != null) {
            enableDataSaving.setOnPreferenceChangeListener((preference, newValue) -> {
                // Disable these settings if the Data Savings setting is set to "Never."
                setDependencyStates(newValue);
                return true;
            });
        }
    }

    private void setDependencyStates(final Object obj) {
        final String dataSavingNever = getString(PrefKeys.PREF_ENABLE_DATA_SAVING_NEVER);
        if (obj.equals(dataSavingNever)) {
            imageQuality.setEnabled(false);
            lowQualityVideos.setEnabled(false);
        } else {
            imageQuality.setEnabled(true);
            lowQualityVideos.setEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_data);
    }
}
