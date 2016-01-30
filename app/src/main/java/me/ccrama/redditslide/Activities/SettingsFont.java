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
        setupAppBar(R.id.toolbar, R.string.settings_title_font, true, true);

        final TextView colorComment = (TextView) findViewById(R.id.commentFont);
        colorComment.setText(new FontPreferences(this).getCommentFontStyle().getTitle());
        findViewById(R.id.commentfontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsFont.this, v);
                popup.getMenu().add("Larger");
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");
                popup.getMenu().add("Smaller");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsFont.this).setCommentFontStyle(FontPreferences.FontStyleComment.valueOf(item.getTitle().toString()));
                        colorComment.setText(new FontPreferences(SettingsFont.this).getCommentFontStyle().getTitle());

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
                popup.getMenu().add("Larger");
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");
                popup.getMenu().add("Smaller");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsFont.this).setPostFontStyle(FontPreferences.FontStyle.valueOf(item.getTitle().toString()));
                        colorPost.setText(new FontPreferences(SettingsFont.this).getPostFontStyle().getTitle());

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
            case Light:
                ((RobotoRadioButton) findViewById(R.id.sregl)).setChecked(true);
            case Slab:
                ((RobotoRadioButton) findViewById(R.id.sslabl)).setChecked(true);

                break;
            case SlabReg:
                ((RobotoRadioButton) findViewById(R.id.sslab)).setChecked(true);

                break;
            case CondensedReg:
                ((RobotoRadioButton) findViewById(R.id.scond)).setChecked(true);

                break;

            case Condensed:
                ((RobotoRadioButton) findViewById(R.id.scondl)).setChecked(true);

                break;

        }
        ((RobotoRadioButton) findViewById(R.id.scond)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.CondensedReg);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sslab)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.SlabReg);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.scondl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.Condensed);
            }
        });
        ((RobotoRadioButton) findViewById(R.id.sslabl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        ((RobotoRadioButton) findViewById(R.id.sregl)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)

                    new FontPreferences(SettingsFont.this).setTitlFont(FontPreferences.FontTypeTitle.Light);
            }
        });

    }


}