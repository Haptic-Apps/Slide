package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;

/**
 * Created by ccrama on 9/17/2015.
 */
public class EditCardsLayout extends BaseActivity {
    ViewPager pager;
    private String subreddit;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        boolean isAlternate;
        if (getIntent() != null && getIntent().hasExtra("secondary")) {
            subreddit = getIntent().getExtras().getString("secondary", "test");
            isAlternate = true;
        } else {
            subreddit = "";
            isAlternate = false;
        }
        subreddit = subreddit.toLowerCase();
        applyColorTheme(subreddit);
        setContentView(R.layout.activity_settings_theme_card);
        int title = isAlternate ?
                R.string.settings_title_alternative_layout : R.string.settings_layout_default;
        setupAppBar(R.id.toolbar, title, true, true);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout, (!subreddit.isEmpty()), subreddit));

        //View type//
        //Cards or List//

        final SwitchCompat cardmode = (SwitchCompat) findViewById(R.id.cardmode);
        cardmode.setChecked(CreateCardView.isCard(!subreddit.isEmpty()));

        final SwitchCompat middle = (SwitchCompat) findViewById(R.id.middlechk);

        if(cardmode.isChecked()){
            middle.setAlpha(1f);
            middle.setChecked(CreateCardView.isMiddle(!subreddit.isEmpty()));

            middle.setClickable(true);
        } else {
            middle.setAlpha(0.5f);
            middle.setChecked(false);
            middle.setClickable(false);
        }
        cardmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout, !subreddit.isEmpty(), subreddit));
                } else {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, !subreddit.isEmpty(), subreddit));

                }
                if (cardmode.isChecked()) {
                    middle.setAlpha(1f);
                    middle.setChecked(CreateCardView.isMiddle(!subreddit.isEmpty()));

                    middle.setClickable(true);
                } else {
                    middle.setAlpha(0.5f);
                    middle.setChecked(false);
                    middle.setClickable(false);
                }
            }
        });
        middle.setChecked(CreateCardView.isMiddle(!subreddit.isEmpty()));


        middle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setMiddleCard(isChecked, layout, !subreddit.isEmpty(), subreddit));

            }
        });


        SwitchCompat hidebutton = (SwitchCompat) findViewById(R.id.hidebutton);
        layout.findViewById(R.id.hide).setVisibility(Reddit.hideButton ? View.VISIBLE : View.GONE);

        hidebutton.setChecked(Reddit.hideButton);
        hidebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.hideButton = isChecked;
                layout.findViewById(R.id.hide).setVisibility(Reddit.hideButton ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean("Hidebutton", isChecked).apply();

            }
        });

        //Link preview//
        //Big, Infobar, thumb only//
        final TextView infobar = (TextView) findViewById(R.id.infobar);
        infobar.setText(CreateCardView.getInfoBar(!subreddit.isEmpty()).toString().replace("_", " ").toLowerCase());
        findViewById(R.id.infobar_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                //Inflating the Popup using xml file
                popup.getMenu().add("Big Picture");
                popup.getMenu().add("Big Picture Cropped");
                popup.getMenu().add("Info Bar");
                popup.getMenu().add("Thumbnail");
                popup.getMenu().add("None");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        layout.removeAllViews();
                        layout.addView(CreateCardView.setInfoBarVisible(SettingValues.InfoBar.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())), layout, !subreddit.isEmpty(), subreddit));
                        infobar.setText(CreateCardView.getInfoBar(!subreddit.isEmpty()).toString().replace("_", " ").toLowerCase());

                        return true;
                    }
                });

                popup.show();
            }
        });


        //Actionbar//
        //Enable, collapse//
        final SwitchCompat actionbar = (SwitchCompat) findViewById(R.id.action);
        actionbar.setChecked(CreateCardView.isActionBar(!subreddit.isEmpty()));
        actionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setActionBarVisible(isChecked, layout, !subreddit.isEmpty(), subreddit));

            }
        });


        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = SettingValues.prefs.edit();
                edit.remove(subreddit + "actionBarVisible");
                edit.remove(subreddit + "largeThumbnails");
                edit.remove(subreddit + "defaultCardView");
                edit.remove(subreddit + "NSFWPreviews");
                edit.remove(subreddit + "infoBarType");
                edit.apply();
                layout.removeAllViews();
                layout.addView(CreateCardView.CreateView(layout, !subreddit.isEmpty(), subreddit));
                actionbar.setChecked(CreateCardView.isActionBar(!subreddit.isEmpty()));
                infobar.setText(CreateCardView.getInfoBar(!subreddit.isEmpty()).toString().replace("_", " ").toLowerCase());

            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
