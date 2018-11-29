package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsModerationFragment {
    private Activity context;

    public SettingsModerationFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        final TextView removalReasonsCurrent = context.findViewById(R.id.settings_moderation_removal_reasons_current);

        if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.SLIDE.ordinal()) {
            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_slide));
        } else if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.TOOLBOX.ordinal()) {
            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_toolbox));
        } else {
            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_reddit));
        }

        context.findViewById(R.id.settings_moderation_removal_reasons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.removal_reason_setings, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.slide:
                                SettingValues.removalReasonType = SettingValues.RemovalReasonType.SLIDE.ordinal();
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                        SettingValues.removalReasonType).apply();
                                break;
                            case R.id.toolbox:
                                SettingValues.removalReasonType = SettingValues.RemovalReasonType.TOOLBOX.ordinal();
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                        SettingValues.removalReasonType).apply();
                                break;
                            case R.id.reddit:
                                SettingValues.removalReasonType = SettingValues.RemovalReasonType.REDDIT.ordinal();
                                SettingValues.prefs.edit().putInt(SettingValues.PREF_MOD_REMOVAL_TYPE,
                                        SettingValues.removalReasonType).apply();
                                break;
                        }

                        if (SettingValues.removalReasonType == SettingValues.RemovalReasonType.SLIDE.ordinal()) {
                            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_slide));
                        } else if (SettingValues.removalReasonType
                                == SettingValues.RemovalReasonType.TOOLBOX.ordinal()) {
                            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_toolbox));
                        } else {
                            removalReasonsCurrent.setText(context.getString(R.string.settings_mod_removal_reddit));
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        final SwitchCompat toolboxEnabled = context.findViewById(R.id.settings_moderation_toolbox_enabled);
        toolboxEnabled.setChecked(SettingValues.toolboxEnabled);
        toolboxEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.toolboxEnabled = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_MOD_TOOLBOX_ENABLED, isChecked).apply();
            }
        });
    }
}
