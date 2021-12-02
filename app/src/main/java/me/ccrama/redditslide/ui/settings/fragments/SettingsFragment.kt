package me.ccrama.redditslide.ui.settings.fragments

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.Authentication
import me.ccrama.redditslide.BuildConfig
import me.ccrama.redditslide.R
import me.ccrama.redditslide.SettingValues
import me.ccrama.redditslide.ui.settings.*
import me.ccrama.redditslide.ui.settings.dragSort.ReorderSubreddits
import me.ccrama.redditslide.util.NetworkUtil
import me.ccrama.redditslide.util.ProUtil
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys

/**
 * Created by TacoTheDank on 05/12/2021.
 */
class SettingsFragment : PreferenceFragmentCompat() {
    private var activity: SettingsActivity? = null

    override fun onStart() {
        super.onStart()
        activity?.supportActionBar?.setTitle(R.string.title_settings)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        activity = getActivity() as SettingsActivity

        // General
        findPreference(PrefKeys.PREF_ROOT_GENERAL)?.apply {
            setActivityResultListener(SettingsGeneral::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_MULTICOLUMN)?.apply {
            onClick {
                if (SettingValues.isPro) {
                    activity?.openSettingsScreen(R.xml.preferences_multicolumn)
                } else {
                    ProUtil.proUpgradeMsg(activity, R.string.general_multicolumn_ispro)
                        .setNegativeButton(R.string.btn_no_thanks) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        findPreference(PrefKeys.PREF_ROOT_MANAGE_SUBREDDITS)?.apply {
            setActivityListener(ReorderSubreddits::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_MANAGE_OFFLINE_CONTENT)?.apply {
            setActivityListener(ManageOfflineContent::class.java)
        }
        if (Authentication.mod) {
            findPreference(PrefKeys.PREF_ROOT_MODERATION)?.apply {
                isVisible = true
                onClick {
                    activity?.openSettingsScreen(R.xml.preferences_moderation)
                }
            }
        }


        // Appearance
        findPreference(PrefKeys.PREF_ROOT_MAIN_THEME)?.apply {
            setActivityResultListener(SettingsTheme::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_POST_LAYOUT)?.apply {
            setActivityListener(EditCardsLayout::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_SUBREDDIT_THEMES)?.apply {
            setActivityResultListener(SettingsSubreddit::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_FONT)?.apply {
            setActivityListener(SettingsFont::class.java)
        }
        declareSettingsScreen(PrefKeys.PREF_ROOT_COMMENTS, R.xml.preferences_comments)


        // Content
        findPreference(PrefKeys.PREF_ROOT_LINK_HANDLING)?.apply {
            setActivityListener(SettingsHandling::class.java)
        }
        declareSettingsScreen(PrefKeys.PREF_ROOT_HISTORY, R.xml.preferences_history)
        declareSettingsScreen(PrefKeys.PREF_ROOT_DATA_SAVING, R.xml.preferences_datasaving)
        findPreference(PrefKeys.PREF_ROOT_FILTER_LIST)?.apply {
            setActivityListener(SettingsFilter::class.java)
        }
        findPreference(PrefKeys.PREF_ROOT_REDDIT_CONTENT)?.apply {
            if (Authentication.isLoggedIn && NetworkUtil.isConnected(context)) {
                onClick {
                    activity?.openSettingsScreen(R.xml.preferences_redditcontent)
                }
            } else {
                isEnabled = false
            }
        }


        // Other
        findPreference(PrefKeys.PREF_ROOT_BACKUP_RESTORE)?.apply {
            onClick {
                if (SettingValues.isPro) {
                    activity?.openSettingsScreen(R.xml.preferences_backup)
                } else {
                    ProUtil.proUpgradeMsg(getActivity(), R.string.general_backup_ispro)
                        .setNegativeButton(R.string.btn_no_thanks) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        declareSettingsScreen(PrefKeys.PREF_ROOT_SYNCCIT, R.xml.preferences_synccit)
        findPreference(PrefKeys.PREF_ROOT_PRO_UPGRADE)?.apply {
            if (SettingValues.isPro) {
                isVisible = false
            } else {
                onClick {
                    ProUtil.proUpgradeMsg(activity, R.string.settings_support_slide)
                        .setNegativeButton(R.string.btn_no_thanks) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        findPreference(PrefKeys.PREF_ROOT_DONATE)?.apply {
            if (BuildConfig.isFDroid) {
                setTitle(R.string.settings_donate_paypal)
                onClick {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, getString(R.string.paypal_link).toUri())
                    startActivity(browserIntent)
                }
            } else {
                setTitle(R.string.settings_title_support)
                onClick {
                    setActivityListener(DonateView::class.java)
                }
            }
        }
        declareSettingsScreen(PrefKeys.PREF_ROOT_ABOUT, R.xml.preferences_about)
    }

    private fun declareSettingsScreen(@StringRes resId: Int, @XmlRes screen: Int) {
        findPreference(resId)?.apply {
            onClick {
                activity?.openSettingsScreen(screen)
            }
        }
    }

    private fun Preference.setActivityResultListener(clazz: Class<*>) {
        onClick {
            val i = Intent(activity, clazz)
            activity?.restartActivityLauncher?.launch(i)
        }
    }

    private fun Preference.setActivityListener(clazz: Class<*>) {
        val i = Intent(activity, clazz)
        intent = i
    }
}
