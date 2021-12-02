package me.ccrama.redditslide.ui.settings.fragments

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import me.ccrama.redditslide.R
import me.ccrama.redditslide.Toolbox.Toolbox
import me.ccrama.redditslide.UserSubscriptions
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.*
import me.ccrama.redditslide.util.preference.PrefKeys
import net.dean.jraw.http.NetworkException

/**
 * Created by TacoTheDank on 07/30/2021.
 */
class SettingsModerationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_moderation)

        val removalType = findPreference(PrefKeys.PREF_REMOVAL_REASON_TYPE) as ListPreference
        val reasonTypeSlide = getString(PrefKeys.PREF_REMOVAL_REASON_TYPE_SLIDE)
        val reasonTypeToolbox = getString(PrefKeys.PREF_REMOVAL_REASON_TYPE_TOOLBOX)
        val enableToolbox = findPreference(PrefKeys.PREF_ENABLE_TOOLBOX) as SwitchPreferenceCompat

        removalType.apply {
            setOnPreferenceChangeListener { _, newValue ->
                // We want to make sure that people cannot select the Toolbox removalType
                //  if the Toolbox functionality setting isn't enabled.
                if (newValue == reasonTypeToolbox && !enableToolbox.isChecked) {
                    context?.displayToast(R.string.setting_toolbox_removalType_error)
                    return@setOnPreferenceChangeListener false
                }
                true
            }
        }
        enableToolbox.apply {
            onChange { newValue ->
                // If the enableToolbox setting is turned off while the removalType is set
                //  to Toolbox, the removalType will be reset back to the default value.
                if (newValue == false && removalType.value == reasonTypeToolbox) {
                    removalType.value = reasonTypeSlide
                }
                // download and cache toolbox stuff in the background unless it's already loaded
                for (sub in UserSubscriptions.modOf) {
                    Toolbox.ensureConfigCachedLoaded(sub, false)
                    Toolbox.ensureUsernotesCachedLoaded(sub, false)
                }
            }
        }

        findPreference(PrefKeys.PREF_REFRESH_TOOLBOX_DATA)?.apply {
            onClick {
                MaterialDialog.Builder(requireActivity())
                    .content(R.string.settings_mod_toolbox_refreshing)
                    .progress(false, UserSubscriptions.modOf.size * 2)
                    .showListener {
                        AsyncRefreshToolboxTask(it)
                            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                    }
                    .cancelable(false)
                    .show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_moderation)
    }

    private class AsyncRefreshToolboxTask(dialog: DialogInterface) :
        AsyncTask<Void?, Void?, Void?>() {
        val dialog: MaterialDialog = dialog as MaterialDialog
        public override fun doInBackground(vararg voids: Void?): Void? {
            for (sub in UserSubscriptions.modOf) {
                try {
                    Toolbox.downloadToolboxConfig(sub)
                } catch (ignored: NetworkException) {
                }
                publishProgress()
                try {
                    Toolbox.downloadUsernotes(sub)
                } catch (ignored: NetworkException) {
                }
                publishProgress()
            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            dialog.dismiss()
        }

        override fun onProgressUpdate(vararg voids: Void?) {
            dialog.incrementProgress(1)
        }
    }
}
