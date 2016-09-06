package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsComments extends BaseActivityAnim {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applyColorTheme();
        setContentView(R.layout.activity_settings_comments);
        setupAppBar(R.id.toolbar, R.string.settings_title_comments, true, true);
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.commentnav);
            single.setChecked(SettingValues.fastscroll);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.fastscroll = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_FASTSCROLL, isChecked).apply();

                    //Disable autohidenav if commentNav isn't checked
                    if (!isChecked) {
                        findViewById(R.id.autohidenav).setEnabled(false);
                        ((SwitchCompat) findViewById(R.id.autohidenav)).setChecked(SettingValues.commentAutoHide);
                        findViewById(R.id.auto_hide_the_comment_nav_bar_text).setAlpha(0.25f);
                    } else {
                        findViewById(R.id.autohidenav).setEnabled(true);
                        findViewById(R.id.auto_hide_the_comment_nav_bar_text).setAlpha(1f);
                    }
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.fab);
            single.setChecked(SettingValues.fabComments);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.fabComments = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COMMENT_FAB, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.rightHandedComments);
            single.setChecked(SettingValues.rightHandedCommentMenu);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.rightHandedCommentMenu = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_RIGHT_HANDED_COMMENT_MENU, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.color);
            single.setChecked(SettingValues.colorCommentDepth);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.colorCommentDepth = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLOR_COMMENT_DEPTH, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.gestures);
            single.setChecked(SettingValues.voteGestures);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.voteGestures = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_VOTE_GESTURES, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.percentvote);
            single.setChecked(SettingValues.upvotePercentage);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.upvotePercentage = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_UPVOTE_PERCENTAGE, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.autohidenav);
            single.setChecked(SettingValues.commentAutoHide);

            if (!((SwitchCompat) findViewById(R.id.commentnav)).isChecked()) {
                single.setEnabled(false);
                findViewById(R.id.auto_hide_the_comment_nav_bar_text).setAlpha(0.25f);
            }

            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.commentAutoHide = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_AUTOHIDE_COMMENTS, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.collapse);
            single.setChecked(SettingValues.collapseComments);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.collapseComments = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLLAPSE_COMMENTS, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.collapse_comments_default);
            single.setChecked(SettingValues.collapseCommentsDefault);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.collapseCommentsDefault = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLLAPSE_COMMENTS_DEFAULT, isChecked).apply();
                }
            });
        }
        SwitchCompat check = (SwitchCompat) findViewById(R.id.swapGesture);
        check.setChecked(SettingValues.swap);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.swap = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SWAP, isChecked).apply();
            }
        });
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.volumenavcomments);
            single.setChecked(SettingValues.commentVolumeNav);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.commentVolumeNav = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COMMENT_NAV, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.cropimage);
            single.setChecked(SettingValues.cropImage);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.cropImage = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_CROP_IMAGE, isChecked).apply();
                }
            });
        }
    }
}