package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.R
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onChange
import me.ccrama.redditslide.util.preference.PrefKeys
import me.ccrama.redditslide.util.preference.PreferenceHelper

/**
 * Created by TacoTheDank on 07/29/2021.
 */
class SettingsDataSavingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_datasaving)

        val imageQuality = findPreference(PrefKeys.PREF_IMAGE_QUALITY)
        val lowQualityVideos = findPreference(PrefKeys.PREF_LOW_QUALITY_VIDEOS)

        val dataSavingOn = !PreferenceHelper.isDataSavingNever()
        imageQuality?.isEnabled = dataSavingOn
        lowQualityVideos?.isEnabled = dataSavingOn

        findPreference(PrefKeys.PREF_ENABLE_DATA_SAVING)?.apply {
            onChange { newValue ->
                // Disable these settings if the Data Savings setting is set to "Never."
                val dataSavingNever = getString(PrefKeys.PREF_ENABLE_DATA_SAVING_NEVER)
                val isNotNever = newValue != dataSavingNever
                imageQuality?.isEnabled = isNotNever
                lowQualityVideos?.isEnabled = isNotNever
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_data)
    }
}
