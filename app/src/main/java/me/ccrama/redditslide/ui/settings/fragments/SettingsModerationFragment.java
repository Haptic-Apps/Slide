package me.ccrama.redditslide.ui.settings.fragments;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.http.NetworkException;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Toolbox.Toolbox;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.preference.PrefKeys;

/**
 * Created by TacoTheDank on 07/30/2021.
 */
public class SettingsModerationFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_moderation);

        final ListPreference removalType = findPreference(getString(PrefKeys.PREF_REMOVAL_REASON_TYPE));
        final String reasonTypeSlide = getString(PrefKeys.PREF_REMOVAL_REASON_TYPE_SLIDE);
        final String reasonTypeToolbox = getString(PrefKeys.PREF_REMOVAL_REASON_TYPE_TOOLBOX);

        final SwitchPreferenceCompat enableToolbox = findPreference(getString(PrefKeys.PREF_ENABLE_TOOLBOX));

        if (removalType != null && enableToolbox != null) {
            removalType.setOnPreferenceChangeListener((preference, newValue) -> {
                // We want to make sure that people cannot select the Toolbox removalType
                //  if the Toolbox functionality setting isn't enabled.
                if (newValue.equals(reasonTypeToolbox) && !enableToolbox.isChecked()) {
                    Toast.makeText(getContext(), R.string.setting_toolbox_removalType_error,
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            });

            enableToolbox.setOnPreferenceChangeListener((preference, newValue) -> {
                // If the enableToolbox setting is turned off while the removalType is set
                //  to Toolbox, the removalType will be reset back to the default value.
                if (newValue.equals(false) && removalType.getValue().equals(reasonTypeToolbox)) {
                    removalType.setValue(reasonTypeSlide);
                }
                // download and cache toolbox stuff in the background unless it's already loaded
                for (String sub : UserSubscriptions.modOf) {
                    Toolbox.ensureConfigCachedLoaded(sub, false);
                    Toolbox.ensureUsernotesCachedLoaded(sub, false);
                }
                return true;
            });
        }

        final Preference refreshToolboxData = findPreference(getString(PrefKeys.PREF_REFRESH_TOOLBOX_DATA));
        if (refreshToolboxData != null) {
            refreshToolboxData.setOnPreferenceClickListener(preference -> {
                new MaterialDialog.Builder(getContext())
                        .content(R.string.settings_mod_toolbox_refreshing)
                        .progress(false, UserSubscriptions.modOf.size() * 2)
                        .showListener(dialog ->
                                new AsyncRefreshToolboxTask(dialog)
                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR))
                        .cancelable(false)
                        .show();
                return true;
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_moderation);
    }

    private static class AsyncRefreshToolboxTask extends AsyncTask<Void, Void, Void> {
        final MaterialDialog dialog;

        AsyncRefreshToolboxTask(DialogInterface dialog) {
            this.dialog = (MaterialDialog) dialog;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String sub : UserSubscriptions.modOf) {
                try {
                    Toolbox.downloadToolboxConfig(sub);
                } catch (NetworkException ignored) {
                }
                publishProgress();
                try {
                    Toolbox.downloadUsernotes(sub);
                } catch (NetworkException ignored) {
                }
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
