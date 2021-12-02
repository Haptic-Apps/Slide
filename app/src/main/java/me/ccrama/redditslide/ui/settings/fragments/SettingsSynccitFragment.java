package me.ccrama.redditslide.ui.settings.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Collections;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask;
import me.ccrama.redditslide.Synccit.SynccitRead;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.preference.PrefKeys;
import me.ccrama.redditslide.util.preference.PreferenceHelper;

/**
 * Created by TacoTheDank on 08/04/2021.
 * <p>
 * The Synccit integration in this preference screen works in this manner:
 * <p>
 * - The username and authcode inputs must both be valid (if not, an alert dialog error will
 * be shown during the next step).
 * - The "Save" button must be clicked to connect to the Synccit service.
 * - The service is now effectively "turned on."
 * - At this point the "Disconnect" button will be enabled. Pressing this will both clear
 * the username and authcode inputs and disconnect the Synccit service.
 * <p>
 * - If, at any point, either the username or authcode are changed, the service will be
 * disconnected regardless of its current state. The "Disconnect" button will now be disabled to
 * reflect this. This will hint to the user that the service has in fact been disconnected.
 */
public class SettingsSynccitFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    private EditTextPreference usernamePref;
    private EditTextPreference authcodePref;
    private Preference disconnectPref;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_synccit);

        usernamePref = findPreference(getString(PrefKeys.PREF_SYNCCIT_USERNAME));
        authcodePref = findPreference(getString(PrefKeys.PREF_SYNCCIT_AUTHCODE));
        final Preference savePref = findPreference(getString(PrefKeys.PREF_SYNCCIT_SAVE));
        disconnectPref = findPreference(getString(PrefKeys.PREF_SYNCCIT_DISCONNECT));

        //
        // Set initial preference states
        //
        updateSummaries();
        disconnectPref.setEnabled(SynccitRead.visitedIds.contains("16noez"));

        //
        // Set listeners
        //
        usernamePref.setOnPreferenceChangeListener(this);
        authcodePref.setOnPreferenceChangeListener(this);

        savePref.setOnPreferenceClickListener(preference -> {
            removeAllVisitedIds();
            new MySynccitUpdateTask().execute("16noez");
            try {
                new MySynccitReadTask().execute("16noez").get();
                if (SynccitRead.visitedIds.contains("16noez")) {
                    disconnectPref.setEnabled(true);

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.settings_synccit_connected)
                            .setMessage(R.string.settings_synccit_active)
                            .setPositiveButton(android.R.string.ok, (dialog, which) ->
                                    dialog.dismiss())
                            .show();
                } else {
                    showFailedDialog();
                }
            } catch (final Exception e) {
                showFailedDialog();
            }
            return true;
        });

        disconnectPref.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.settings_synccit_delete)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        PreferenceHelper.disconnectSynccit();
                        updateSummaries();
                        disconnectPref.setEnabled(false);
                        removeAllVisitedIds();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        });
    }

    // Display the keys' values as their summaries
    private void updateSummaries() {
        usernamePref.setSummary(PreferenceHelper.synccitUsername());
        authcodePref.setSummary(PreferenceHelper.synccitAuthcode());
    }

    private void showFailedDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.settings_synccit_failed)
                .setMessage(R.string.settings_synccit_failed_msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void removeAllVisitedIds() {
        SynccitRead.visitedIds.removeAll(Collections.singleton("16noez"));
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (preference == usernamePref) {
            usernamePref.setSummary(newValue.toString());
        } else if (preference == authcodePref) {
            authcodePref.setSummary(newValue.toString());
        }
        removeAllVisitedIds();
        disconnectPref.setEnabled(false);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_synccit);
    }
}
