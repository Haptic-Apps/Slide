package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class EditCardsLayout extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstance) {
        overrideRedditSwipeAnywhere();
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        applyColorTheme();
        setContentView(R.layout.activity_settings_theme_card);

        setupAppBar(R.id.toolbar, R.string.settings_layout_default, true, true);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout, false, ""));

        //View type//
        //Cards or List//
        ((TextView) findViewById(R.id.view_current)).setText(CreateCardView.isCard(false) ? (CreateCardView.isMiddle(false) ? getString(R.string.mode_centered) : getString(R.string.mode_card)) : getString(R.string.mode_list));

        findViewById(R.id.view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.card_mode_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.center:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setMiddleCard(true, layout, false, ""));
                                break;
                            case R.id.card:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, false, ""));
                                break;
                            case R.id.list:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout, false, ""));
                                break;
                        }
                        ((TextView) findViewById(R.id.view_current)).setText(CreateCardView.isCard(false) ? (CreateCardView.isMiddle(false) ? getString(R.string.mode_centered) : getString(R.string.mode_card)) : getString(R.string.mode_list));
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Pic modes//

        ((TextView) findViewById(R.id.picture_current)).setText(SettingValues.bigPicEnabled ? (SettingValues.bigPicCropped ? getString(R.string.mode_cropped) : getString(R.string.mode_bigpic)) : getString(R.string.mode_thumbnail));

        findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.pic_mode_settings, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.bigpic:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setBigPicEnabled(true, layout));
                            {
                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                for (Map.Entry<String, ?> map : SettingValues.prefs.getAll().entrySet()) {
                                    if (map.getKey().startsWith("picsenabled")) {
                                        e.remove(map.getKey()); //reset all overridden values
                                    }
                                }
                                e.apply();
                            }
                            break;
                            case R.id.cropped:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setBigPicCropped(true, layout));
                                break;
                            case R.id.thumbnail:
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setBigPicEnabled(false, layout));
                            {
                                SharedPreferences.Editor e = SettingValues.prefs.edit();
                                for (Map.Entry<String, ?> map : SettingValues.prefs.getAll().entrySet()) {
                                    if (map.getKey().startsWith("picsenabled")) {
                                        e.remove(map.getKey()); //reset all overridden values
                                    }
                                }
                                e.apply();
                            }
                            break;
                        }
                        ((TextView) findViewById(R.id.picture_current)).setText(SettingValues.bigPicEnabled ? (SettingValues.bigPicCropped ? getString(R.string.mode_cropped) : getString(R.string.mode_bigpic)) : getString(R.string.mode_thumbnail));
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Actionbar//


        ((TextView) findViewById(R.id.actionbar_current)).setText(!SettingValues.actionbarVisible ? (SettingValues.actionbarTap ? getString(R.string.tap_actionbar) : getString(R.string.press_actionbar)) : getString(R.string.always_actionbar));

        findViewById(R.id.actionbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                popup.getMenuInflater().inflate(R.menu.actionbar_mode, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.always:
                                SettingValues.actionbarTap = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, false).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(true, layout));
                                break;
                            case R.id.tap:
                                SettingValues.actionbarTap = true;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, true).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(false, layout));
                                break;
                            case R.id.button:
                                SettingValues.actionbarTap = false;
                                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ACTIONBAR_TAP, false).apply();
                                layout.removeAllViews();
                                layout.addView(CreateCardView.setActionbarVisible(false, layout));
                                break;
                        }
                        ((TextView) findViewById(R.id.actionbar_current)).setText(!SettingValues.actionbarVisible ? (SettingValues.actionbarTap ? getString(R.string.tap_actionbar) : getString(R.string.press_actionbar)) : getString(R.string.always_actionbar));
                        return true;
                    }
                });

                popup.show();
            }
        });


        //Other buttons//
        final AppCompatCheckBox hidebutton = (AppCompatCheckBox) findViewById(R.id.hidebutton);
        layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);

        hidebutton.setChecked(SettingValues.hideButton);
        hidebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.hideButton = isChecked;
                layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDEBUTTON, isChecked).apply();

            }
        });
        final AppCompatCheckBox savebutton = (AppCompatCheckBox) findViewById(R.id.savebutton);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);

        savebutton.setChecked(SettingValues.saveButton);
        savebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.saveButton = isChecked;
                layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton && SettingValues.actionbarVisible ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SAVE_BUTTON, isChecked).apply();

            }
        });

        //Actionbar//
        //Enable, collapse//
        final SwitchCompat switchThumb = (SwitchCompat) findViewById(R.id.action);
        switchThumb.setChecked(SettingValues.switchThumb);
        switchThumb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setSwitchThumb(isChecked, layout));
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = SettingValues.prefs.edit();
                edit.remove("largeThumbnailsNew");
                edit.remove("defaultCardViewNew");
                edit.remove("infoBarTypeNew");
                edit.apply();
                layout.removeAllViews();
                layout.addView(CreateCardView.CreateView(layout, false, ""));

            }
        });




       /*todo findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardView.setCardViewType(SettingValues.defaultCardView, layout, subreddit, subreddit);
                CreateCardView.setActionBarVisible(SettingValues.actionBarVisible, layout, subreddit, subreddit);
                CreateCardView.setInfoBarVisible(SettingValues.infoBar, layout, subreddit, subreddit);
                layout.removeAllViews();
                layout.addView(CreateCardView.setLargeThumbnails(SettingValues.largeThumbnails, layout, subreddit, subreddit));
                largeimage.setChecked(CreateCardView.isLarge(subreddit));
                actionbar.setChecked(CreateCardView.isActionBar(subreddit));
                infobar.setChecked(CreateCardView.isInfoBar(subreddit));
                int chosen = list.indexOf(CreateCardView.getCardView(subreddit).getDisplayName());
                sp.setSelection(chosen);
            }
        });*/


    }


}
