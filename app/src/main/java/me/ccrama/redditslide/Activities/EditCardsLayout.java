package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import me.ccrama.redditslide.R;
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
        overrideRedditSwipeAnywhere();
        overrideSwipeFromAnywhere();

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

        setupAppBar(R.id.toolbar, R.string.settings_layout_default, true, true);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout, (!subreddit.isEmpty()), subreddit));

        //View type//
        //Cards or List//

        final SwitchCompat cardmode = (SwitchCompat) findViewById(R.id.cardmode);
        cardmode.setChecked(CreateCardView.isCard(!subreddit.isEmpty()));

        final SwitchCompat bigpic = (SwitchCompat) findViewById(R.id.bigpicsqitch);
        final SwitchCompat cropped = (SwitchCompat) findViewById(R.id.bigpiccropped);
        final SwitchCompat middle = (SwitchCompat) findViewById(R.id.middlechk);

        //Big pic enabled//

        bigpic.setChecked(SettingValues.bigPicEnabled);
        bigpic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setBigPicEnabled(isChecked, layout));
                cropped.setEnabled(isChecked);
                middle.setEnabled(isChecked && cardmode.isChecked());

            }
        });
        //Big pic cropped//

        cropped.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setBigPicCropped(isChecked, layout));
            }
        });

        middle.setEnabled(cardmode.isChecked() && bigpic.isChecked());
        middle.setChecked(CreateCardView.isMiddle(!subreddit.isEmpty()));



        cropped.setEnabled(bigpic.isChecked());
        cropped.setChecked(SettingValues.bigPicCropped);

        cardmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout, !subreddit.isEmpty(), subreddit));

                } else {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, !subreddit.isEmpty(), subreddit));

                    middle.setChecked(CreateCardView.isMiddle(!subreddit.isEmpty()));
                }

                middle.setEnabled(isChecked);
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


        final SwitchCompat hidebutton = (SwitchCompat) findViewById(R.id.hidebutton);
        layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton ? View.VISIBLE : View.GONE);

        hidebutton.setChecked(SettingValues.hideButton);
        hidebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.hideButton = isChecked;
                layout.findViewById(R.id.hide).setVisibility(SettingValues.hideButton ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_HIDEBUTTON, isChecked).apply();

            }
        });
        final SwitchCompat savebutton = (SwitchCompat) findViewById(R.id.savebutton);
        layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton ? View.VISIBLE : View.GONE);

        savebutton.setChecked(SettingValues.saveButton);
        savebutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.saveButton = isChecked;
                layout.findViewById(R.id.save).setVisibility(SettingValues.saveButton ? View.VISIBLE : View.GONE);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_SAVE_BUTTON, isChecked).apply();

            }
        });

        //Actionbar//
        //Enable, collapse//
        final SwitchCompat actionbar = (SwitchCompat) findViewById(R.id.action);
        actionbar.setChecked(SettingValues.switchThumb);
        actionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                edit.remove(subreddit + "largeThumbnailsNew");
                edit.remove(subreddit + "defaultCardViewNew");
                edit.remove(subreddit + "infoBarTypeNew");
                edit.apply();
                layout.removeAllViews();
                layout.addView(CreateCardView.CreateView(layout, !subreddit.isEmpty(), subreddit));

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
