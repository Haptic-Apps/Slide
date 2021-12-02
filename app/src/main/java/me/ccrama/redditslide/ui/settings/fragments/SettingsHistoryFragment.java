package me.ccrama.redditslide.ui.settings.fragments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lusfold.androidkeyvaluestore.KVStore;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.preference.PrefKeys;

/**
 * Created by TacoTheDank on 05/12/2021.
 */
public class SettingsHistoryFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_history);

        final Preference clearSubmissionHistory
                = findPreference(getString(PrefKeys.PREF_CLEAR_SUBMISSION_HISTORY));
        if (clearSubmissionHistory != null) {
            clearSubmissionHistory.setOnPreferenceClickListener(preference -> {
                KVStore.getInstance().clearTable();
                showHistoryClearedToast();
                return true;
            });
        }

        final Preference clearSubredditHistory
                = findPreference(getString(PrefKeys.PREF_CLEAR_SUBREDDIT_HISTORY));
        if (clearSubredditHistory != null) {
            clearSubredditHistory.setOnPreferenceClickListener(preference -> {
                UserSubscriptions.subscriptions.edit().remove("subhistory").apply();
                showHistoryClearedToast();
                return true;
            });
        }
    }

    private void showHistoryClearedToast() {
        Toast.makeText(getContext(), R.string.alert_history_cleared, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_title_history);
    }
}
