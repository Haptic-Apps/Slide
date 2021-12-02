package me.ccrama.redditslide.ui.settings.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.annotation.XmlRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.ui.settings.DonateView;
import me.ccrama.redditslide.ui.settings.EditCardsLayout;
import me.ccrama.redditslide.ui.settings.ManageOfflineContent;
import me.ccrama.redditslide.ui.settings.SettingsActivity;
import me.ccrama.redditslide.ui.settings.SettingsBackup;
import me.ccrama.redditslide.ui.settings.SettingsFilter;
import me.ccrama.redditslide.ui.settings.SettingsFont;
import me.ccrama.redditslide.ui.settings.SettingsGeneral;
import me.ccrama.redditslide.ui.settings.SettingsHandling;
import me.ccrama.redditslide.ui.settings.SettingsReddit;
import me.ccrama.redditslide.ui.settings.SettingsSubreddit;
import me.ccrama.redditslide.ui.settings.SettingsSynccit;
import me.ccrama.redditslide.ui.settings.SettingsTheme;
import me.ccrama.redditslide.ui.settings.dragSort.ReorderSubreddits;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.ProUtil;
import me.ccrama.redditslide.util.preference.PrefKeys;

/**
 * Created by TacoTheDank on 05/12/2021.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsActivity activity;

    @Override
    public void onStart() {
        super.onStart();
        activity.getSupportActionBar().setTitle(R.string.title_settings);
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        activity = (SettingsActivity) getActivity();

        // General
        final Preference generalPref = findPreference(getString(PrefKeys.PREF_ROOT_GENERAL));
        setActivityResultListener(generalPref, SettingsGeneral.class);

        final Preference multiColumnPref = findPreference(getString(PrefKeys.PREF_ROOT_MULTICOLUMN));
        if (multiColumnPref != null) {
            multiColumnPref.setOnPreferenceClickListener(preference -> {
                    /*final Intent i = new Intent(Overview.this, Overview.class);
                    i.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(i);*/
                if (SettingValues.isPro) {
                    activity.openSettingsScreen(R.xml.preferences_multicolumn);
                } else {
                    ProUtil.proUpgradeMsg(activity, R.string.general_multicolumn_ispro)
                            .setNegativeButton(R.string.btn_no_thanks, (dialog, whichButton) ->
                                    dialog.dismiss())
                            .show();
                }
                return true;
            });
        }

        final Preference manageSubsPref = findPreference(getString(PrefKeys.PREF_ROOT_MANAGE_SUBREDDITS));
        setActivityListener(manageSubsPref, ReorderSubreddits.class);

        final Preference offlinePref = findPreference(getString(PrefKeys.PREF_ROOT_MANAGE_OFFLINE_CONTENT));
        setActivityListener(offlinePref, ManageOfflineContent.class);

        final Preference moderationPref = findPreference(getString(PrefKeys.PREF_ROOT_MODERATION));
        if (Authentication.mod) {
            if (moderationPref != null) {
                moderationPref.setVisible(true);
                moderationPref.setOnPreferenceClickListener(preference -> {
                    activity.openSettingsScreen(R.xml.preferences_moderation);
                    return true;
                });
            }
        }


        // Appearance
        final Preference mainThemePref = findPreference(getString(PrefKeys.PREF_ROOT_MAIN_THEME));
        setActivityResultListener(mainThemePref, SettingsTheme.class);

        final Preference postLayoutPref = findPreference(getString(PrefKeys.PREF_ROOT_POST_LAYOUT));
        setActivityListener(postLayoutPref, EditCardsLayout.class);

        final Preference subThemePref = findPreference(getString(PrefKeys.PREF_ROOT_SUBREDDIT_THEMES));
        setActivityResultListener(subThemePref, SettingsSubreddit.class);

        final Preference fontPref = findPreference(getString(PrefKeys.PREF_ROOT_FONT));
        setActivityListener(fontPref, SettingsFont.class);

        declareSettingsScreen(PrefKeys.PREF_ROOT_COMMENTS, R.xml.preferences_comments);


        // Content
        final Preference handlingPref = findPreference(getString(PrefKeys.PREF_ROOT_LINK_HANDLING));
        setActivityListener(handlingPref, SettingsHandling.class);

        declareSettingsScreen(PrefKeys.PREF_ROOT_HISTORY, R.xml.preferences_history);

        declareSettingsScreen(PrefKeys.PREF_ROOT_DATA_SAVING, R.xml.preferences_datasaving);

        final Preference filterPref = findPreference(getString(PrefKeys.PREF_ROOT_FILTER_LIST));
        setActivityListener(filterPref, SettingsFilter.class);

        final Preference redditContentPref = findPreference(getString(PrefKeys.PREF_ROOT_REDDIT_CONTENT));
        if (Authentication.isLoggedIn && NetworkUtil.isConnected(getContext())) {
            setActivityListener(redditContentPref, SettingsReddit.class);
        } else {
            if (redditContentPref != null) {
                redditContentPref.setEnabled(false);
            }
        }


        // Other
        final Preference backupPref = findPreference(getString(PrefKeys.PREF_ROOT_BACKUP_RESTORE));
        setActivityListener(backupPref, SettingsBackup.class);

        final Preference synccitPref = findPreference(getString(PrefKeys.PREF_ROOT_SYNCCIT));
        setActivityListener(synccitPref, SettingsSynccit.class);

        final Preference proPref = findPreference(getString(PrefKeys.PREF_ROOT_PRO_UPGRADE));
        if (proPref != null) {
            if (SettingValues.isPro) {
                proPref.setVisible(false);
            } else {
                proPref.setOnPreferenceClickListener(preference -> {
                    ProUtil.proUpgradeMsg(activity, R.string.settings_support_slide)
                            .setNegativeButton(R.string.btn_no_thanks, (dialog, whichButton) ->
                                    dialog.dismiss())
                            .show();
                    return true;
                });
            }
        }

        final Preference donatePref = findPreference(getString(PrefKeys.PREF_ROOT_DONATE));
        if (donatePref != null) {
            if (BuildConfig.isFDroid) {
                donatePref.setTitle(R.string.settings_donate_paypal);
                donatePref.setOnPreferenceClickListener(preference -> {
                    final Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.paypal_link)));
                    startActivity(browserIntent);
                    return true;
                });
            } else {
                donatePref.setTitle(R.string.settings_title_support);
                donatePref.setOnPreferenceClickListener(preference -> {
                    setActivityListener(donatePref, DonateView.class);
                    return true;
                });
            }
        }

        declareSettingsScreen(PrefKeys.PREF_ROOT_ABOUT, R.xml.preferences_about);
    }

    private void declareSettingsScreen(@StringRes final int resId, @XmlRes final int screen) {
        final Preference pref = findPreference(getString(resId));
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                activity.openSettingsScreen(screen);
                return true;
            });
        }
    }

    private void setActivityResultListener(final Preference pref, final Class<?> clazz) {
        if (pref != null) {
            pref.setOnPreferenceClickListener(preference -> {
                final Intent i = new Intent(activity, clazz);
                activity.restartActivityLauncher.launch(i);
                return true;
            });
        }
    }

    private void setActivityListener(final Preference pref, final Class<?> clazz) {
        if (pref != null) {
            final Intent i = new Intent(activity, clazz);
            pref.setIntent(i);
        }
    }
}
