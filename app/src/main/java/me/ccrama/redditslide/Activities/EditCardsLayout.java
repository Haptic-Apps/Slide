package me.ccrama.redditslide.Activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 9/17/2015.
 */
public class EditCardsLayout extends BaseActivity {

    ViewPager pager;
    String subreddit;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        if (getIntent() != null && getIntent().hasExtra("subreddit")) {
            subreddit = getIntent().getExtras().getString("subreddit", "");
        } else {
            subreddit = "";
        }
        subreddit = subreddit.toLowerCase();
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit, true).getBaseId(), true);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        setContentView(R.layout.activity_settings_theme_card);

        findViewById(R.id.toolbar).setBackgroundColor(Pallete.getColor(subreddit));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getColor(subreddit)));
        }


        final LinearLayout layout = (LinearLayout) findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout, subreddit, subreddit));

        //View type//
        //Cards or List//

        final CheckBox cardmode = (CheckBox) findViewById(R.id.cardmode);
        cardmode.setChecked(CreateCardView.isCard(subreddit));


        cardmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout, subreddit, subreddit));
                } else {
                    layout.removeAllViews();
                    layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, subreddit, subreddit));

                }
            }
        });



        //Link preview//
        //Big, Infobar, thumb only//
        final TextView infobar = (TextView) findViewById(R.id.infobar);
        infobar.setText(CreateCardView.getInfoBar(subreddit).toString().replace("_", " ").toLowerCase());
        findViewById(R.id.infobar_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                //Inflating the Popup using xml file
                popup.getMenu().add("Big picture");
                popup.getMenu().add("Info bar");
                popup.getMenu().add("Thumbnail");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        layout.removeAllViews();
                        layout.addView(CreateCardView.setInfoBarVisible(SettingValues.InfoBar.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())), layout, subreddit, subreddit));
                        infobar.setText(CreateCardView.getInfoBar(subreddit).toString().replace("_", " ").toLowerCase());

                        return true;
                    }
                });

                popup.show();
            }
        });

        //Too tall//
        //Crop, show full//


        final CheckBox crop = (CheckBox) findViewById(R.id.cropped);
        crop.setChecked(CreateCardView.getCroppedImage(subreddit));


        crop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setCropped(isChecked, layout, subreddit, subreddit));

            }
        });

        //Actionbar//
        //Enable, collapse//
        final CheckBox actionbar = (CheckBox) findViewById(R.id.action);
        actionbar.setChecked(CreateCardView.isActionBar(subreddit));
        actionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setActionBarVisible(isChecked, layout, subreddit, subreddit));

            }
        });

        //Color matching mode//
        //Everywhere, not sub//
        final TextView color = (TextView) findViewById(R.id.colormatchingwhere);
        color.setText(CreateCardView.getColorMatchingMode(subreddit).toString().replace("_", " ").toLowerCase());
        findViewById(R.id.colormatchingwhere_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                //Inflating the Popup using xml file
                popup.getMenu().add("Always Match");
                popup.getMenu().add("Match Externally");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        layout.removeAllViews();
                        layout.addView(CreateCardView.setColorMatchingMode(SettingValues.ColorMatchingMode.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())), layout, subreddit, subreddit));
                        color.setText(CreateCardView.getColorMatchingMode(subreddit).toString().replace("_", " ").toLowerCase());

                        return true;
                    }
                });

                popup.show();
            }
        });
        //Color matching type//
        //card, subreddit, or none//
        final TextView matchingtype = (TextView) findViewById(R.id.colormatching);
        matchingtype.setText(CreateCardView.getColorIndicator(subreddit).toString().replace("_", " ").toLowerCase());
        findViewById(R.id.colormatching_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(EditCardsLayout.this, v);
                //Inflating the Popup using xml file
                popup.getMenu().add("Card Background");
                popup.getMenu().add("Text Color");
                popup.getMenu().add("None");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        layout.removeAllViews();
                        layout.addView(CreateCardView.setColorIndicicator(SettingValues.ColorIndicator.valueOf((item.getTitle().toString().replace(" ", "_").toUpperCase())), layout, subreddit, subreddit));
                        matchingtype.setText(CreateCardView.getColorIndicator(subreddit).toString().replace("_", " ").toLowerCase());

                        return true;
                    }
                });

                popup.show();
            }
        });

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = SettingValues.prefs.edit();
                edit.remove(subreddit + "actionBarVisible");
                edit.remove(subreddit + "largeThumbnails");
                edit.remove(subreddit + "croppedImage");
                edit.remove(subreddit + "defaultCardView");
                edit.remove(subreddit + "NSFWPreviews");
                edit.remove(subreddit + "ccolorMatchingMode");
                edit.remove(subreddit + "colorIndicator");
                edit.remove(subreddit + "infoBarType");
                edit.apply();
                layout.removeAllViews();
                layout.addView(CreateCardView.CreateView(layout, subreddit, subreddit));
                color.setText(CreateCardView.getColorMatchingMode(subreddit).toString().replace("_", " ").toLowerCase());
                actionbar.setChecked(CreateCardView.isActionBar(subreddit));
                crop.setChecked(CreateCardView.getCroppedImage(subreddit));
                infobar.setText(CreateCardView.getInfoBar(subreddit).toString().replace("_", " ").toLowerCase());

                matchingtype.setText(CreateCardView.getColorIndicator(subreddit).toString().replace("_", " ").toLowerCase());

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
