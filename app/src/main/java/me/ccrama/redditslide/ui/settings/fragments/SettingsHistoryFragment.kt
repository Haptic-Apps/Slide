package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceFragmentCompat
import com.lusfold.androidkeyvaluestore.KVStore
import me.ccrama.redditslide.R
import me.ccrama.redditslide.UserSubscriptions
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.displayToast
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys

/**
 * Created by TacoTheDank on 05/12/2021.
 */
class SettingsHistoryFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_history)

        findPreference(PrefKeys.PREF_CLEAR_SUBMISSION_HISTORY)?.apply {
            onClick {
                KVStore.getInstance().clearTable()
                showHistoryClearedToast()
            }
        }

        findPreference(PrefKeys.PREF_CLEAR_SUBREDDIT_HISTORY)?.apply {
            onClick {
                UserSubscriptions.subscriptions.edit { remove("subhistory") }
                showHistoryClearedToast()
            }
        }
    }

    private fun showHistoryClearedToast() {
        context?.displayToast(R.string.alert_history_cleared)
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_title_history)
    }
}
