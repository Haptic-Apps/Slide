package me.ccrama.redditslide.ui.settings.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import com.jakewharton.processphoenix.ProcessPhoenix
import me.ccrama.redditslide.R
import me.ccrama.redditslide.ui.settings.SettingsActivity
import me.ccrama.redditslide.util.FileUtil
import me.ccrama.redditslide.util.LogUtil
import me.ccrama.redditslide.util.ktx.findPreference
import me.ccrama.redditslide.util.ktx.onClick
import me.ccrama.redditslide.util.preference.PrefKeys
import org.apache.commons.lang3.StringUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ccrama on 3/5/2015.
 *
 *
 * Rewritten into AndroidX Preference by TacoTheDank on 08/03/2021.
 */
class SettingsBackupFragment : PreferenceFragmentCompat() {
    private val restoreFromFileLauncher =
        registerForActivityResult(StartActivityForResult(), this::restoreFromFileResult)
    private var file: File? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_backup)

        findPreference(PrefKeys.PREF_BACKUP_TO_FILE)?.apply {
            onClick {
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.settings_backup_include_personal_title)
                    .setMessage(R.string.settings_backup_include_personal_text)
                    .setPositiveButton(R.string.btn_yes) { _, _ ->
                        backupToDir(false)
                    }
                    .setNegativeButton(R.string.btn_no) { _, _ ->
                        backupToDir(true)
                    }
                    .setNeutralButton(R.string.btn_cancel, null)
                    .setCancelable(false)
                    .show()
            }
        }
        findPreference(PrefKeys.PREF_RESTORE_FROM_FILE)?.apply {
            onClick {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "file/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain"))
                }
                restoreFromFileLauncher.launch(intent)
            }
        }
    }

    private fun restoreFromFileResult(result: ActivityResult) {
        if (result.data != null) {
            val fileUri = result.data?.data
            Log.v(LogUtil.getTag(), "WORKED! $fileUri")
            val fw = StringWriter()
            try {
                val inputStream = activity?.contentResolver?.openInputStream(fileUri)
                val reader = InputStreamReader(inputStream).buffered()
                var c = reader.read()
                while (c != -1) {
                    fw.write(c)
                    c = reader.read()
                }
                val read = "$fw"
                writeToSharedPrefs(read)
            } catch (e: Exception) {
                e.printStackTrace()
                showFileNotFoundDialog()
            }
        } else {
            showFileNotFoundDialog()
        }
    }

    private fun showFileNotFoundDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.err_file_not_found)
            .setMessage(R.string.err_file_not_found_msg)
            .setPositiveButton(R.string.btn_ok, null)
            .setCancelable(false)
            .show()
    }

    private fun writeToSharedPrefs(read: String) {
        if (read.contains("Slide_backupEND>")) {
            val files = read.split("END>\\s*".toRegex()).toTypedArray()
            for (i in 1 until files.size) {
                var innerFile = files[i]
                val t = innerFile.substring(6, innerFile.indexOf(">"))
                innerFile = innerFile.substring(innerFile.indexOf(">") + 1)
                val newF = File(
                    activity?.applicationInfo?.dataDir
                            + File.separator
                            + "shared_prefs"
                            + File.separator
                            + t
                )
                Log.v(LogUtil.getTag(), "WRITING TO " + newF.absolutePath)
                try {
                    val newfw = FileWriter(newF)
                    val bw = BufferedWriter(newfw)
                    bw.write(innerFile)
                    bw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.backup_restore_settings)
                .setMessage(R.string.backup_restarting)
                .setOnDismissListener {
                    ProcessPhoenix.triggerRebirth(activity)
                }
                .setPositiveButton(R.string.btn_ok) { _, _ ->
                    ProcessPhoenix.triggerRebirth(activity)
                }
                .setCancelable(false)
                .show()
        } else {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.err_not_valid_backup)
                .setMessage(R.string.err_not_valid_backup_msg)
                .setPositiveButton(R.string.btn_ok, null)
                .setCancelable(false)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as SettingsActivity?)?.supportActionBar?.setTitle(R.string.settings_title_backup)
    }

    private fun close(stream: Closeable?) {
        try {
            stream?.close()
        } catch (ignored: IOException) {
        }
    }

    private fun backupToDir(personal: Boolean) {
        val prefsdir = File(activity?.applicationInfo?.dataDir, "shared_prefs")
        if (prefsdir.exists() && prefsdir.isDirectory) {
            val list = prefsdir.list()
            val getExtDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            getExtDir.mkdirs()
            val backedup = File(
                "$getExtDir" + File.separator
                        + "Slide"
                        + SimpleDateFormat("-yyyy-MM-dd-HH-mm-ss")
                    .format(Calendar.getInstance().time)
                        + (if (!personal) "-personal" else "")
                        + ".txt"
            )
            file = backedup
            var fw: FileWriter? = null
            try {
                backedup.createNewFile()
                fw = FileWriter(backedup)
                fw.write("Slide_backupEND>")
                for (s in list) {
                    val other = !StringUtils.containsAny(
                        s, "cache", "ion-cookies",
                        "albums", "com.google", "STACKTRACE"
                    )
                    val personal1 = !StringUtils.containsAny(
                        s, "SUBSNEW", "appRestart", "STACKTRACE", "AUTH",
                        "TAGS", "SEEN", "HIDDEN", "HIDDEN_POSTS"
                    )
                    if (other && (!personal || personal1)) {
                        var fr: FileReader? = null
                        try {
                            fr = FileReader("$prefsdir" + File.separator + s)
                            var c = fr.read()
                            fw.write("<START" + File(s).name + ">")
                            while (c != -1) {
                                fw.write(c)
                                c = fr.read()
                            }
                            fw.write("END>")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            close(fr)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                close(fw)
            }
        }
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.backup_complete)
            .setMessage(R.string.backup_saved_downloads)
            .setPositiveButton(R.string.btn_view) { _, _ ->
                val intent = FileUtil.getFileIntent(file, Intent(Intent.ACTION_VIEW), activity)
                if (intent.resolveActivityInfo(activity?.packageManager, 0) != null) {
                    startActivity(
                        Intent.createChooser(
                            intent,
                            getString(R.string.settings_backup_view)
                        )
                    )
                } else {
                    Toast.makeText(
                        activity,
                        getString(
                            R.string.settings_backup_err_no_explorer,
                            file!!.absolutePath + file
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(R.string.btn_close, null)
            .setCancelable(false)
            .show()
    }
}
