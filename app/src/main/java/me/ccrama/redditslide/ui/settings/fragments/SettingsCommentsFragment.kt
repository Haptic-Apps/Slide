package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.R
import me.ccrama.redditslide.SettingValues
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.preference.PrefKeys

/**
 * Created by TacoTheDank on 05/12/2021.
 */
class SettingsCommentsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_comments)

        findPreference(PrefKeys.PREF_COLORED_TIME_BUBBLE)?.apply {
            isEnabled = SettingValues.commentLastVisit
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_title_comments)
    }
}
