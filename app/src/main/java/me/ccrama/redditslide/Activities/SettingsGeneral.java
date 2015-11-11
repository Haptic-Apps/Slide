package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivityNoAnim {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings_general);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.title_settings_general);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            SettingsGeneral.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_settings_general), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
        {
            CheckBox single = (CheckBox) findViewById(R.id.single);

            single.setChecked(Reddit.single);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.single = isChecked;
                    SettingValues.prefs.edit().putBoolean("Single", isChecked).apply();

                }
            });
        }
        {
            CheckBox single = (CheckBox) findViewById(R.id.fastscroll);

            single.setChecked(Reddit.fastscroll);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.fastscroll = isChecked;
                    SettingValues.prefs.edit().putBoolean("Fastscroll", isChecked).apply();

                }
            });
        }
        {
            CheckBox single = (CheckBox) findViewById(R.id.hidebutton);

            single.setChecked(Reddit.hideButton);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.hideButton = isChecked;
                    SettingValues.prefs.edit().putBoolean("Hidebutton", isChecked).apply();

                }
            });
        }
        {
            CheckBox single = (CheckBox) findViewById(R.id.exitcheck);

            single.setChecked(Reddit.exit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.exit = isChecked;
                    SettingValues.prefs.edit().putBoolean("Exit", isChecked).apply();

                }
            });
        }

        {
            CheckBox single = (CheckBox) findViewById(R.id.nsfw);

            single.setChecked(!SettingValues.NSFWPosts);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.prefs.edit().putBoolean("NSFWPostsNew", !isChecked).apply();

                    SettingValues.NSFWPosts = !isChecked;
                }
            });
        }


        {
            CheckBox single = (CheckBox) findViewById(R.id.nsfwrpev);

            single.setChecked(!SettingValues.NSFWPreviews);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SettingValues.prefs.edit().putBoolean("NSFWPreviewsNew", !isChecked).apply();
                    SettingValues.NSFWPreviews = !isChecked;

                }
            });
        }
        final TextView color = (TextView) findViewById(R.id.font);
        color.setText(new FontPreferences(this).getFontStyle().getTitle());
        findViewById(R.id.fontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsGeneral.this).setFontStyle(FontPreferences.FontStyle.valueOf(item.getTitle().toString()));
                        color.setText(new FontPreferences(SettingsGeneral.this).getFontStyle().getTitle());

                        return true;
                    }
                });

                popup.show();
            }
        });
        {
            CheckBox check = (CheckBox) findViewById(R.id.swapGesture);

            check.setChecked(Reddit.swap);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.swap = isChecked;
                    SettingValues.prefs.edit().putBoolean("Swap", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.web);

            check.setChecked(Reddit.web);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.web = isChecked;
                    SettingValues.prefs.edit().putBoolean("web", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.image);

            check.setChecked(Reddit.image);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.image = isChecked;
                    SettingValues.prefs.edit().putBoolean("image", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.gif);

            check.setChecked(Reddit.gif);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.gif = isChecked;
                    SettingValues.prefs.edit().putBoolean("gif", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.album);

            check.setChecked(Reddit.album);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.album = isChecked;
                    SettingValues.prefs.edit().putBoolean("album", isChecked).apply();

                }
            });
        }
        {
            CheckBox check = (CheckBox) findViewById(R.id.video);

            check.setChecked(Reddit.video);
            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.video = isChecked;
                    SettingValues.prefs.edit().putBoolean("video", isChecked).apply();

                }
            });
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}