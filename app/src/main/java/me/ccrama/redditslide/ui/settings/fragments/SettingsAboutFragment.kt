package me.ccrama.redditslide.ui.settings.fragments

import android.content.Context
import android.os.Bundle
import androidx.core.content.edit
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat
import com.mikepenz.aboutlibraries.LibsBuilder
import me.ccrama.redditslide.BuildConfig
import me.ccrama.redditslide.OpenRedditLink
import me.ccrama.redditslide.R
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ClipboardUtil
import me.ccrama.redditslide.util.LinkUtil
import me.ccrama.redditslide.util.ktx.displayToast
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys

/**
 * Created by TacoTheDank on 07/29/2021.
 */
class SettingsAboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_about)

        findPreference(PrefKeys.PREF_ABOUT_REPORT_BUGS)?.apply {
            onClick {
                LinkUtil.openExternally("https://github.com/ccrama/Slide/issues")
            }
        }

        findPreference(PrefKeys.PREF_ABOUT_CHANGELOG)?.apply {
            onClick {
                LinkUtil.openExternally(
                    "https://github.com/ccrama/Slide/blob/master/CHANGELOG.md"
                )
            }
        }

        findPreference(PrefKeys.PREF_ABOUT_RATE)?.apply {
            onClick {
                LinkUtil.launchMarketUri(context, R.string.app_package)
            }
        }

        findPreference(PrefKeys.PREF_ABOUT_SUBREDDIT)?.apply {
            onClick {
                OpenRedditLink.openUrl(
                    context,
                    "https://reddit.com/r/slideforreddit",
                    true
                )
            }
        }

        findPreference(PrefKeys.PREF_ABOUT_LIBRARIES)?.apply {
            onClick {
                parentFragmentManager.commit {
                    replace(R.id.settings_container, LibsBuilder().supportFragment())
                    addToBackStack(getString(R.string.settings_about_libs))
                }
            }
        }

        findPreference(PrefKeys.PREF_ABOUT_VERSION)?.apply {
            summary = "Slide v" + BuildConfig.VERSION_NAME
            onClick {
                if (BuildConfig.DEBUG) {
                    val prefs = context?.getSharedPreferences("STACKTRACE", Context.MODE_PRIVATE)
                    val stacktrace = prefs?.getString("stacktrace", null)
                    ClipboardUtil.copyToClipboard(context, "Stacktrace", stacktrace)
                    prefs?.edit { clear() }
                } else {
                    ClipboardUtil.copyToClipboard(context, "Version", "$summary")
                    context?.displayToast(R.string.settings_about_version_copied_toast)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_title_about)
    }
}
