package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import me.ccrama.redditslide.Fragments.SettingsThemeFragment;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsViewType extends BaseActivityAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_viewtype);
        setupAppBar(R.id.toolbar, R.string.settings_view_type, true, true);



        //View type multi choice
        ((TextView) findViewById(R.id.currentViewType)).setText(SettingValues.single ? (SettingValues.commentPager ? getString(R.string.view_type_comments) : getString(R.string.view_type_none)) : getString(R.string.view_type_tabs));

        findViewById(R.id.viewtype).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsViewType.this, v);
                popup.getMenuInflater().inflate(R.menu.view_type_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tabs:
                                SettingValues.single = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SINGLE, false).apply();
                                break;
                            case R.id.notabs:
                                SettingValues.single = true;
                                SettingValues.commentPager = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SINGLE, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COMMENT_PAGER, false).apply();
                                break;
                            case R.id.comments:
                                SettingValues.single = true;
                                SettingValues.commentPager = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SINGLE, true).apply();
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COMMENT_PAGER, true).apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.currentViewType)).setText(SettingValues.single ? (SettingValues.commentPager ? getString(R.string.view_type_comments) : getString(R.string.view_type_none)) : getString(R.string.view_type_tabs));
                        SettingsThemeFragment.changed = true;
                        return true;
                    }
                });

                popup.show();
            }
        });

    }
}