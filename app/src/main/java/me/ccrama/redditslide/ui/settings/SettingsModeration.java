package me.ccrama.redditslide.ui.settings;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.R;

public class SettingsModeration extends BaseActivityAnim {
    private SettingsModerationFragment fragment = new SettingsModerationFragment(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_moderation);
        setupAppBar(R.id.toolbar, R.string.settings_moderation, true, true);

        ((ViewGroup) findViewById(R.id.settings_moderation)).addView(getLayoutInflater().inflate(
                R.layout.activity_settings_moderation_child, null));

        fragment.Bind();
    }
}
