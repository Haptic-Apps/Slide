package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.R
import me.ccrama.redditslide.Visuals.Palette
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.LinkUtil
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.forceRestartIfChanged
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys
import me.ccrama.redditslide.util.preference.PreferenceHelper

/**
 * Created by TacoTheDank on 08/07/2021.
 */
class SettingsRedditContentFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_redditcontent)

        findPreference(PrefKeys.PREF_SEE_NSFW_CONTENT)?.apply {
            forceRestartIfChanged()
        }

        findPreference(PrefKeys.PREF_HIDE_ALL_NSFW)?.apply {
            forceRestartIfChanged { newValue ->
                PreferenceHelper.setHideNsfwPreviews((newValue as Boolean))
            }
        }

        findPreference(PrefKeys.PREF_HIDE_NSFW_PREVIEWS)?.apply {
            forceRestartIfChanged()
        }

        findPreference(PrefKeys.PREF_IGNORE_SUB_MEDIA_PREFS)?.apply {
            forceRestartIfChanged()
        }

        findPreference(PrefKeys.PREF_VIEW_ALL_REDDIT_PREFS)?.apply {
            onClick {
                LinkUtil.openUrl(
                    "https://www.reddit.com/prefs/",
                    Palette.getDefaultColor(), requireActivity()
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.content_settings)
    }
}
