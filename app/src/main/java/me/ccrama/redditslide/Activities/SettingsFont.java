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
public class SettingsFont extends BaseActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_font);
        setupAppBar(R.id.toolbar, "Font Settings", true, true);

        final TextView color = (TextView) findViewById(R.id.font);
        color.setText(new FontPreferences(this).getFontStyle().getTitle());
        findViewById(R.id.fontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsFont.this, v);
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsFont.this).setFontStyle(FontPreferences.FontStyle.valueOf(item.getTitle().toString()));
                        color.setText(new FontPreferences(SettingsFont.this).getFontStyle().getTitle());

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


        }
        ((RobotoRadioButton) findViewById(R.id.ccond)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Condensed);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.cslab)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Slab);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.creg)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setCommentFont(FontPreferences.FontTypeComment.Regular);
            }
        });


        switch (new FontPreferences(this).getFontTypeTitle()) {
            case Regular:
                ((RobotoRadioButton) findViewById(R.id.sreg)).setChecked(true);
                break;
            case Slab:
                ((RobotoRadioButton) findViewById(R.id.sslab)).setChecked(true);

                break;
            case Condensed:
                ((RobotoRadioButton) findViewById(R.id.scond)).setChecked(true);

                break;

        }
        ((RobotoRadioButton) findViewById(R.id.scond)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.Condensed);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sslab)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.Slab);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sreg)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.Regular);
            }
        });

    }


}