package me.ccrama.redditslide.ui.settings.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.OpenRedditLink;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.util.ClipboardUtil;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.preference.PrefKeys;

/**
 * Created by TacoTheDank on 07/29/2021.
 */
public class SettingsAboutFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences_about);

        final Preference reportBugs = findPreference(getString(PrefKeys.PREF_ABOUT_REPORT_BUGS));
        if (reportBugs != null) {
            reportBugs.setOnPreferenceClickListener(preference -> {
                LinkUtil.openExternally("https://github.com/ccrama/Slide/issues");
                return true;
            });
        }

        final Preference changelog = findPreference(getString(PrefKeys.PREF_ABOUT_CHANGELOG));
        if (changelog != null) {
            changelog.setOnPreferenceClickListener(preference -> {
                LinkUtil.openExternally("https://github.com/ccrama/Slide/blob/master/CHANGELOG.md");
                return true;
            });
        }

        final Preference appRate = findPreference(getString(PrefKeys.PREF_ABOUT_RATE));
        if (appRate != null) {
            appRate.setOnPreferenceClickListener(preference -> {
                LinkUtil.launchMarketUri(getActivity(), R.string.app_package);
                return true;
            });
        }

        final Preference subreddit = findPreference(getString(PrefKeys.PREF_ABOUT_SUBREDDIT));
        if (subreddit != null) {
            subreddit.setOnPreferenceClickListener(preference -> {
                OpenRedditLink.openUrl(getActivity(), "https://reddit.com/r/slideforreddit", true);
                return true;
            });
        }

        final Preference libraries = findPreference(getString(PrefKeys.PREF_ABOUT_LIBRARIES));
        if (libraries != null) {
            libraries.setOnPreferenceClickListener(preference -> {
                final LibsSupportFragment fragment = new LibsBuilder().supportFragment();
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.settings_container, fragment)
                        .addToBackStack(getString(R.string.settings_about_libs))
                        .commit();
                return true;
            });
        }

        final Preference version = findPreference(getString(PrefKeys.PREF_ABOUT_VERSION));
        if (version != null) {
            version.setSummary("Slide v" + BuildConfig.VERSION_NAME);
            version.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.DEBUG) {
                    final SharedPreferences prefs = getActivity().getSharedPreferences(
                            "STACKTRACE", Context.MODE_PRIVATE);
                    final String stacktrace = prefs.getString("stacktrace", null);
                    if (stacktrace != null) {
                        ClipboardUtil.copyToClipboard(getActivity(), "Stacktrace", stacktrace);
                    }
                    prefs.edit().clear().apply();
                } else {
                    final String versionNumber = version.getSummary().toString();
                    ClipboardUtil.copyToClipboard(getActivity(), "Version", versionNumber);
                    Toast.makeText(getActivity(),
                            R.string.settings_about_version_copied_toast, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings_title_about);
    }
}
