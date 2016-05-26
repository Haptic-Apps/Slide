package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.devspark.robototextview.widget.RobotoRadioButton;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.FontPreferences;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsFont extends BaseActivityAnim {
    private static String getFontName(int resource) {
        switch (resource) {
            case R.string.font_size_huge:
                return "Huge";
            case R.string.font_size_larger:
                return "Larger";
            case R.string.font_size_large:
                return "Large";
            case R.string.font_size_medium:
                return "Medium";
            case R.string.font_size_small:
                return "Small";
            case R.string.font_size_smaller:
                return "Smaller";
            case R.string.font_size_tiny:
                return "Tiny";
            default:
                return "Medium";
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_font);
        setupAppBar(R.id.toolbar, R.string.settings_title_font, true, true);

        final TextView colorComment = (TextView) findViewById(R.id.commentFont);
        colorComment.setText(new FontPreferences(this).getCommentFontStyle().getTitle());
        findViewById(R.id.commentfontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsFont.this, v);
                popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
                popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
                popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
                popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
                popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
                popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsFont.this).setCommentFontStyle(
                                FontPreferences.FontStyleComment.valueOf(getFontName(item.getItemId())));
                        colorComment.setText(new FontPreferences(SettingsFont.this).getCommentFontStyle().getTitle());
                        SettingsTheme.changed = true;
                        return true;
                    }
                });

                popup.show();
            }
        });
        final TextView colorPost = (TextView) findViewById(R.id.postFont);
        colorPost.setText(new FontPreferences(this).getPostFontStyle().getTitle());
        findViewById(R.id.postfontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsFont.this, v);
                popup.getMenu().add(0, R.string.font_size_huge, 0, R.string.font_size_huge);
                popup.getMenu().add(0, R.string.font_size_larger, 0, R.string.font_size_larger);
                popup.getMenu().add(0, R.string.font_size_large, 0, R.string.font_size_large);
                popup.getMenu().add(0, R.string.font_size_medium, 0, R.string.font_size_medium);
                popup.getMenu().add(0, R.string.font_size_small, 0, R.string.font_size_small);
                popup.getMenu().add(0, R.string.font_size_smaller, 0, R.string.font_size_smaller);
                popup.getMenu().add(0, R.string.font_size_tiny, 0, R.string.font_size_tiny);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsFont.this).setPostFontStyle(
                                FontPreferences.FontStyle.valueOf(getFontName(item.getItemId())));
                        colorPost.setText(new FontPreferences(SettingsFont.this).getPostFontStyle().getTitle());
                        SettingsTheme.changed = true;
                        return true;
                    }
                });

                popup.show();
            }
        });

        switch (new FontPreferences(this).getFontTypeComment()) {
            case Regular:
                ((RobotoRadioButton) findViewById(R.id.creg)).setChecked(true);
                break;
            case Slab:
                ((RobotoRadioButton) findViewById(R.id.cslab)).setChecked(true);
                break;
            case Condensed:
                ((RobotoRadioButton) findViewById(R.id.ccond)).setChecked(true);
                break;
            case Light:
                ((RobotoRadioButton) findViewById(R.id.clight)).setChecked(true);
                break;
            case System:
                ((RobotoRadioButton) findViewById(R.id.cnone)).setChecked(true);
                break;

        }
        ((RobotoRadioButton) findViewById(R.id.ccond)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Condensed);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.cslab)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Slab);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.creg)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Regular);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.clight)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Light);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.cnone)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.System);
                }
            }
        });
        switch (new FontPreferences(this).getFontTypeTitle()) {
            case Regular:
                ((RobotoRadioButton) findViewById(R.id.sreg)).setChecked(true);
                break;
            case Light:
                ((RobotoRadioButton) findViewById(R.id.sregl)).setChecked(true);
                break;
            case Slab:
                ((RobotoRadioButton) findViewById(R.id.sslabl)).setChecked(true);
                break;
            case SlabReg:
                ((RobotoRadioButton) findViewById(R.id.sslab)).setChecked(true);
                break;
            case CondensedReg:
                ((RobotoRadioButton) findViewById(R.id.scond)).setChecked(true);
                break;
            case CondensedBold:
                ((RobotoRadioButton) findViewById(R.id.scondb)).setChecked(true);
                break;
            case Condensed:
                ((RobotoRadioButton) findViewById(R.id.scondl)).setChecked(true);
                break;
            case Bold:
                ((RobotoRadioButton) findViewById(R.id.sbold)).setChecked(true);
                break;
            case Medium:
                ((RobotoRadioButton) findViewById(R.id.smed)).setChecked(true);
                break;
            case System:
                ((RobotoRadioButton) findViewById(R.id.snone)).setChecked(true);
                break;
        }
        ((RobotoRadioButton) findViewById(R.id.scond)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.CondensedReg);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sslab)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.SlabReg);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.scondl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Condensed);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sbold)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Bold);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.smed)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Medium);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sslabl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Slab);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sreg)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Regular);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sregl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.Light);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.snone)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.System);
                }
            }
        });
        ((RobotoRadioButton) findViewById(R.id.scondb)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingsTheme.changed = true;
                    new FontPreferences(SettingsFont.this).setTitleFont(FontPreferences.FontTypeTitle.CondensedBold);
                }
            }
        });
    }
}