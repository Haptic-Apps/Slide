package me.ccrama.redditslide.util.ktx

import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import me.ccrama.redditslide.ui.settings.SettingsActivity

inline fun Preference.onClick(crossinline block: () -> Unit) {
    setOnPreferenceClickListener {
        block()
        true
    }
}

inline fun Preference.onChange(crossinline block: (Any?) -> Unit) {
    setOnPreferenceChangeListener { _, newValue ->
        block(newValue)
        true
    }
}

fun Preference.forceRestartIfChanged(block: ((Any?) -> Unit)? = null) {
    setOnPreferenceChangeListener { _, newValue ->
        SettingsActivity.changed = true
        if (block != null) {
            block(newValue)
        }
        true
    }
}

fun PreferenceFragmentCompat?.findPreference(@StringRes key: Int): Preference? {
    return this?.findPreference(getString(key))
}
