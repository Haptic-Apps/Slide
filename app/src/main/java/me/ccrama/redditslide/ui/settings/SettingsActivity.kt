package me.ccrama.redditslide.ui.settings

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.Activities.BaseActivity
import me.ccrama.redditslide.R
import me.ccrama.redditslide.SettingValues
import me.ccrama.redditslide.databinding.ActivitySettingsBinding
import me.ccrama.redditslide.ui.settings.fragments.*

/**
 * Created by TacoTheDank on 05/12/2021.
 */
class SettingsActivity : BaseActivity(), RestartActivity {
    val restartActivityLauncher =
        registerForActivityResult(StartActivityForResult()) { restartActivity() }
    private lateinit var _binding: ActivitySettingsBinding
    private val binding get() = _binding
    private lateinit var prefsListener: OnSharedPreferenceChangeListener

    override fun restartActivity() {
        val i = Intent(this, SettingsActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(i)
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        if (id == R.id.home) {
            if (supportFragmentManager.backStackEntryCount == 0) {
                finish()
            } else {
                supportFragmentManager.popBackStack()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyColorTheme()
        _binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupAppBar(R.id.settings_toolbar, R.string.title_settings, true, true)

        supportFragmentManager.commit {
            replace(binding.settingsContainer.id, SettingsFragment())
        }
        prefsListener = OnSharedPreferenceChangeListener { _, _ ->
            changed = true
        }
        SettingValues.prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        SettingValues.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
    }

    fun openSettingsScreen(screen: Int) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.animator.settings_fade_in,
                R.animator.settings_fade_out,
                R.animator.settings_fade_in,
                R.animator.settings_fade_out
            )
            replace(binding.settingsContainer.id, getSettingsScreen(screen))
            addToBackStack(getString(getTitleOfPage(screen)))
        }
    }

    private fun getSettingsScreen(screen: Int): PreferenceFragmentCompat {
        var prefFragment: PreferenceFragmentCompat? = null
        when (screen) {
            R.xml.preferences_multicolumn -> prefFragment = SettingsMultiColumnFragment()
            R.xml.preferences_moderation -> prefFragment = SettingsModerationFragment()
            R.xml.preferences_comments -> prefFragment = SettingsCommentsFragment()
            R.xml.preferences_history -> prefFragment = SettingsHistoryFragment()
            R.xml.preferences_datasaving -> prefFragment = SettingsDataSavingFragment()
            R.xml.preferences_redditcontent -> prefFragment = SettingsRedditContentFragment()
            R.xml.preferences_backup -> prefFragment = SettingsBackupFragment()
            R.xml.preferences_synccit -> prefFragment = SettingsSynccitFragment()
            R.xml.preferences_about -> prefFragment = SettingsAboutFragment()
        }
        return prefFragment!!
    }

    companion object {
        //whether or not a Setting was changed
        @JvmField
        var changed = false

        private fun getTitleOfPage(preferences: Int): Int {
            return when (preferences) {
                R.xml.preferences -> R.string.title_settings
                R.xml.preferences_multicolumn -> R.string.settings_title_multi_column
                R.xml.preferences_moderation -> R.string.settings_moderation
                R.xml.preferences_comments -> R.string.settings_title_comments
                R.xml.preferences_history -> R.string.settings_title_history
                R.xml.preferences_datasaving -> R.string.settings_data
                R.xml.preferences_redditcontent -> R.string.content_settings
                R.xml.preferences_backup -> R.string.settings_title_backup
                R.xml.preferences_synccit -> R.string.settings_synccit
                R.xml.preferences_about -> R.string.settings_title_about
                else -> R.string.title_settings
            }
        }
    }
}
