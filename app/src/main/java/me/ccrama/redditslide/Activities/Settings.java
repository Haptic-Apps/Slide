package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends ActionBarActivity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            Intent i = new Intent(Settings.this, Settings.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();


        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings);
        Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            Settings.this.setTaskDescription(new ActivityManager.TaskDescription("Settings", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));


        }

        findViewById(R.id.general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsGeneral.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Settings.this);
                final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);
                if (Reddit.notificationTime == -1) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                    landscape.setValue(Reddit.notificationTime / 15, false);
                    checkBox.setText("Check for new messages every " + Inbox.getTime(Reddit.notificationTime));

                }
                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                    @Override
                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                        checkBox.setText("Check for new messages every " + Inbox.getTime(i1 * 15));
                    }
                });
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            Reddit.notificationTime = -1;
                            Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                            Reddit.notifications.cancel(getApplication());
                        } else {
                            Reddit.notificationTime = 15;
                            landscape.setValue(1, true);
                        }
                    }
                });
                dialoglayout.findViewById(R.id.title).setBackgroundColor(Pallete.getDefaultColor());
                //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

                //todo  portrait.setBackgroundColor(Pallete.getDefaultColor());
                landscape.setValue(Reddit.dpWidth, false);


                final Dialog dialog = builder.setView(dialoglayout).create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue();
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            Reddit.notifications.cancel(getApplication());
                            Reddit.notifications.start(getApplication());
                        }
                    }
                });

            }
        });
        findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsTheme.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(Settings.this, DonateView.class);

                Settings.this.startActivity(inte);
            }
        });

    }

}