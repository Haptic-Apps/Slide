package me.ccrama.redditslide.ui.settings.fragments

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.R
import me.ccrama.redditslide.Synccit.MySynccitReadTask
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask
import me.ccrama.redditslide.Synccit.SynccitRead
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onChange
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys
import me.ccrama.redditslide.util.preference.PreferenceHelper

/**
 * Created by TacoTheDank on 08/04/2021.
 *
 * The Synccit integration in this preference screen works in this manner:
 *
 * - The username and authcode inputs must both be valid (if not, an alert dialog error will
 * be shown during the next step).
 * - The "Save" button must be clicked to connect to the Synccit service.
 * - The service is now effectively "turned on."
 * - At this point the "Disconnect" button will be enabled. Pressing this will both clear
 * the username and authcode inputs and disconnect the Synccit service.
 *
 * - If, at any point, either the username or authcode are changed, the service will be
 * disconnected regardless of its current state. The "Disconnect" button will now be disabled to
 * reflect this. This will hint to the user that the service has in fact been disconnected.
 */
class SettingsSynccitFragment : PreferenceFragmentCompat() {
    private lateinit var usernamePref: EditTextPreference
    private lateinit var authcodePref: EditTextPreference
    private lateinit var disconnectPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_synccit)
        usernamePref = findPreference(PrefKeys.PREF_SYNCCIT_USERNAME) as EditTextPreference
        authcodePref = findPreference(PrefKeys.PREF_SYNCCIT_AUTHCODE) as EditTextPreference
        disconnectPref = findPreference(PrefKeys.PREF_SYNCCIT_DISCONNECT)!!

        usernamePref.apply {
            summary = PreferenceHelper.synccitUsername()
            disconnectSynccitWhenChanged()
        }
        authcodePref.apply {
            summary = PreferenceHelper.synccitAuthcode()
            disconnectSynccitWhenChanged()
        }
        findPreference(PrefKeys.PREF_SYNCCIT_SAVE)?.apply {
            onClick {
                removeAllVisitedIds()
                saveSynccitData()
            }
        }
        disconnectPref.apply {
            isEnabled = SynccitRead.visitedIds.contains("16noez")
            onClick {
                disconnectSynccitService()
            }
        }
    }

    private fun Preference.disconnectSynccitWhenChanged() {
        onChange { newValue ->
            this.summary = "$newValue"
            removeAllVisitedIds()
            disconnectPref.isEnabled = false
        }
    }

    private fun showFailedDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.settings_synccit_failed)
            .setMessage(R.string.settings_synccit_failed_msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun saveSynccitData() {
        MySynccitUpdateTask().execute("16noez")
        try {
            MySynccitReadTask().execute("16noez").get()
            if (SynccitRead.visitedIds.contains("16noez")) {
                disconnectPref.isEnabled = true
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.settings_synccit_connected)
                    .setMessage(R.string.settings_synccit_active)
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                showFailedDialog()
            }
        } catch (e: Exception) {
            showFailedDialog()
        }
    }

    private fun disconnectSynccitService() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.settings_synccit_delete)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                PreferenceHelper.disconnectSynccit()
                usernamePref.summary = PreferenceHelper.synccitUsername()
                authcodePref.summary = PreferenceHelper.synccitAuthcode()
                removeAllVisitedIds()
                disconnectPref.isEnabled = false
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun removeAllVisitedIds() {
        SynccitRead.visitedIds.removeAll(setOf("16noez"))
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_synccit)
    }
}
