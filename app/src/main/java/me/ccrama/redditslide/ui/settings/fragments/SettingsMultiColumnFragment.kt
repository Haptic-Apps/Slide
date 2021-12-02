package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import me.ccrama.redditslide.R
import me.ccrama.redditslide.Reddit
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.forceRestartIfChanged
import me.ccrama.redditslide.util.preference.PrefKeys

/**
 * Created by TacoTheDank on 05/12/2021.
 */
class SettingsMultiColumnFragment : PreferenceFragmentCompat() {

    private lateinit var landscapeSeekbar: SeekBarPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_multicolumn)

        landscapeSeekbar = findPreference(PrefKeys.PREF_LANDSCAPE_COLUMN_NUMBER)?.apply {
            forceRestartIfChanged()
        } as SeekBarPreference
    }

    override fun onDestroy() {
        super.onDestroy()
        val landscapeValue = landscapeSeekbar.value
        Reddit.dpWidth = landscapeValue
        Reddit.colors.edit {
            putInt("tabletOVERRIDE", landscapeValue)
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_title_multi_column)
    }
}
