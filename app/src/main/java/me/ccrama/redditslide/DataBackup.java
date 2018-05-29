package me.ccrama.redditslide;

/**
 * Created by carlo_000 on 10/23/2015.
 */

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class DataBackup extends BackupAgentHelper {
    static final String[] PREFS_TO_BACKUP = new String[]{
            "AUTH", "SUBS", "SETTINGS", "COLOR", "SEEN", "HIDDEN", "HIDDEN_POSTS", "prefs",
            "IMAGES", "DATA"
    };

    static final String MY_PREFS_BACKUP_KEY = "myprefs";

    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper =
                new SharedPreferencesBackupHelper(this, PREFS_TO_BACKUP);
        addHelper(MY_PREFS_BACKUP_KEY, helper);
    }
}