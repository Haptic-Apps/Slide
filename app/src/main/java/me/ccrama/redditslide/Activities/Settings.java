package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends BaseActivityNoAnim  {


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            Intent i = new Intent(Settings.this, Settings.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);


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




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_settings);
        Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        TextView version = (TextView) findViewById(R.id.settings_version);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.title_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            Settings.this.setTaskDescription(new ActivityManager.TaskDescription("Settings", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
        version.setText("Slide v" + BuildConfig.VERSION_CODE);

        findViewById(R.id.pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                }
            }
        });
        findViewById(R.id.general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsGeneral.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.subtheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsSubreddit.class);
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
                    checkBox.setText(getString(R.string.settings_notification) + " " + Inbox.getTime(Reddit.notificationTime, getBaseContext()));

                }
                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                    @Override
                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                        checkBox.setText(getString(R.string.settings_notification) + " " + Inbox.getTime(i1 * 15, getBaseContext()));
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
                            if (Reddit.notifications != null) {
                                Reddit.notifications.cancel(getApplication());
                                Reddit.notifications.start(getApplication());
                            } else {
                                Reddit.notifications = new NotificationJobScheduler(getApplication());
                            }
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
        findViewById(R.id.backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsBackup.class);
                startActivity(i);
            }
        });
        findViewById(R.id.preset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                i.putExtra("secondary", "yes");
                startActivityForResult(i, 2);
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