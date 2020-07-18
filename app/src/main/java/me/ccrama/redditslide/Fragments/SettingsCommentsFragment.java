package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;

public class SettingsCommentsFragment {

    private Activity context;

    public SettingsCommentsFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_commentnav);
            single.setChecked(SettingValues.fastscroll);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.fastscroll = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_FASTSCROLL, isChecked).apply();

                    //Disable autohidenav and showcollapseexpand if commentNav isn't checked
                    if (!isChecked) {
                        context.findViewById(R.id.settings_comments_autohidenav).setEnabled(false);
                        ((SwitchCompat)context.findViewById(R.id.settings_comments_autohidenav)).setChecked(SettingValues.commentAutoHide);
                        context.findViewById(R.id.settings_comments_auto_hide_the_comment_nav_bar_text).setAlpha(0.25f);
                        context.findViewById(R.id.settings_comments_show_collapse_expand).setEnabled(false);
                        ((SwitchCompat)context.findViewById(R.id.settings_comments_show_collapse_expand)).setChecked(SettingValues.commentAutoHide);
                        context.findViewById(R.id.show_collapse_expand_nav_bar).setAlpha(0.25f);
                    } else {
                        context.findViewById(R.id.settings_comments_autohidenav).setEnabled(true);
                        context.findViewById(R.id.settings_comments_auto_hide_the_comment_nav_bar_text).setAlpha(1f);
                        context.findViewById(R.id.settings_comments_show_collapse_expand).setEnabled(true);
                        context.findViewById(R.id.show_collapse_expand_nav_bar).setAlpha(1f);
                    }
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_fab);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_show_collapse_expand);
            single.setChecked(SettingValues.showCollapseExpand);

            if (!((SwitchCompat) context.findViewById(R.id.settings_comments_commentnav)).isChecked()) {
                single.setEnabled(false);
                context.findViewById(R.id.show_collapse_expand_nav_bar).setAlpha(0.25f);
            }

            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.showCollapseExpand = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SHOW_COLLAPSE_EXPAND, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_rightHandedComments);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_color);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_colored_time);
            single.setChecked(SettingValues.highlightTime);
            single.setEnabled(SettingValues.commentLastVisit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.highlightTime = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIGHLIGHT_TIME, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_gestures);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_percentvote);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_autohidenav);
            single.setChecked(SettingValues.commentAutoHide);

            if (!((SwitchCompat) context.findViewById(R.id.settings_comments_commentnav)).isChecked()) {
                single.setEnabled(false);
                context.findViewById(R.id.settings_comments_auto_hide_the_comment_nav_bar_text).setAlpha(0.25f);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_opColor);
            single.setChecked(SettingValues.highlightCommentOP);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.highlightCommentOP = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIGHLIGHT_COMMENT_OP, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_collapse);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_collapse_comments_default);
            single.setChecked(SettingValues.collapseCommentsDefault);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.collapseCommentsDefault = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_COLLAPSE_COMMENTS_DEFAULT, isChecked).apply();
                }
            });
        }
        SwitchCompat check = context.findViewById(R.id.settings_comments_swapGesture);
        check.setChecked(SettingValues.swap);
        check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.swap = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SWAP, isChecked).apply();
            }
        });
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_volumenavcomments);
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
            SwitchCompat single = context.findViewById(R.id.settings_comments_wide);
            single.setChecked(SettingValues.largeDepth);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.largeDepth = isChecked;
                    SettingValues.prefs.edit().putBoolean(SettingValues.PREF_LARGE_DEPTH, isChecked).apply();
                }
            });
        }
        {
            SwitchCompat single = context.findViewById(R.id.settings_comments_cropimage);
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
