package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.ViewGroup;

import me.ccrama.redditslide.Fragments.SettingsFontFragment;
import me.ccrama.redditslide.R;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFont extends BaseActivityAnim {

    private final SettingsFontFragment fragment = new SettingsFontFragment(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_font);
        setupAppBar(R.id.toolbar, R.string.settings_title_font, true, true);

        ((ViewGroup) findViewById(R.id.settings_font)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_font_child, null));

        fragment.Bind();
    }

}