package me.ccrama.redditslide.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.preference.PreferenceFragmentCompat;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.databinding.ActivitySettingsBinding;
import me.ccrama.redditslide.ui.settings.fragments.SettingsCommentsFragment;
import me.ccrama.redditslide.ui.settings.fragments.SettingsDataSavingFragment;
import me.ccrama.redditslide.ui.settings.fragments.SettingsFragment;
import me.ccrama.redditslide.ui.settings.fragments.SettingsHistoryFragment;
import me.ccrama.redditslide.ui.settings.fragments.SettingsMultiColumnFragment;

/**
 * Created by TacoTheDank on 05/12/2021.
 */
public class SettingsActivity extends BaseActivity implements RestartActivity {

    public static boolean changed; //whether or not a Setting was changed
    public final ActivityResultLauncher<Intent> restartActivityLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> restartActivity());
    private ActivitySettingsBinding binding;

    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;

    private static int getTitleOfPage(final int preferences) {
        switch (preferences) {
            case R.xml.preferences: // Main settings page
            default:
                return R.string.title_settings;

            case R.xml.preferences_multicolumn:
                return R.string.settings_title_multi_column;
            case R.xml.preferences_comments:
                return R.string.settings_title_comments;
            case R.xml.preferences_history:
                return R.string.settings_title_history;
            case R.xml.preferences_datasaving:
                return R.string.settings_data;
        }
    }

    @Override
    public void restartActivity() {
        final Intent i = new Intent(this, SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupAppBar(R.id.settings_toolbar, R.string.title_settings, true, true);

        getSupportFragmentManager().beginTransaction()
                .replace(binding.settingsContainer.getId(), new SettingsFragment())
                .commit();

        prefsListener = (sharedPreferences, key) -> SettingsActivity.changed = true;
        SettingValues.prefs.registerOnSharedPreferenceChangeListener(prefsListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingValues.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
    }

    public void openSettingsScreen(final int screen) {
        final PreferenceFragmentCompat fragment = getSettingsScreen(screen);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.settings_fade_in, R.animator.settings_fade_out,
                        R.animator.settings_fade_in, R.animator.settings_fade_out
                )
                .replace(binding.settingsContainer.getId(), fragment)
                .addToBackStack(getString(getTitleOfPage(screen)))
                .commit();
    }

    private PreferenceFragmentCompat getSettingsScreen(final int screen) {
        PreferenceFragmentCompat prefFragment = null;

        switch (screen) {
            case R.xml.preferences_multicolumn:
                prefFragment = new SettingsMultiColumnFragment();
                break;
            case R.xml.preferences_comments:
                prefFragment = new SettingsCommentsFragment();
                break;
            case R.xml.preferences_history:
                prefFragment = new SettingsHistoryFragment();
                break;
            case R.xml.preferences_datasaving:
                prefFragment = new SettingsDataSavingFragment();
                break;
        }
        return prefFragment;
    }
}
