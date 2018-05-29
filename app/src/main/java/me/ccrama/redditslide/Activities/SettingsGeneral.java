package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.io.File;

import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.SettingsGeneralFragment;
import me.ccrama.redditslide.R;

/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityAnim
        implements FolderChooserDialogCreate.FolderCallback {

    private final SettingsGeneralFragment fragment = new SettingsGeneralFragment(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_general);
        setupAppBar(R.id.toolbar, R.string.settings_title_general, true, true);

        ((ViewGroup) findViewById(R.id.settings_general)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_general_child, null));

        fragment.Bind();
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialogCreate dialog, @NonNull File folder) {
        fragment.onFolderSelection(dialog, folder);
    }

    @Override
    public void onResume() {
        super.onResume();
        SettingsGeneralFragment.doNotifText(this);
    }
}
