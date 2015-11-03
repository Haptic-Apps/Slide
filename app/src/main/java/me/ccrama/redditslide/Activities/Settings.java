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

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
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
            Settings.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_settings), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
        version.setText("Slide v" + BuildConfig.VERSION_NAME);

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
        findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                Reddit.defaultSorting = Sorting.HOT;
                                break;
                            case 1:
                                Reddit.defaultSorting = Sorting.NEW;
                                break;
                            case 2:
                                Reddit.defaultSorting = Sorting.RISING;
                                break;
                            case 3:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.HOUR;
                                break;
                            case 4:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.DAY;
                                break;
                            case 5:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.WEEK;
                                break;
                            case 6:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.MONTH;
                                break;
                            case 7:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.YEAR;
                                break;
                            case 8:
                                Reddit.defaultSorting = Sorting.TOP;
                                Reddit.timePeriod = TimePeriod.ALL;
                                break;
                            case 9:
                                Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                Reddit.timePeriod = TimePeriod.HOUR;
                                break;
                            case 10:
                                Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                Reddit.timePeriod = TimePeriod.DAY;
                                break;
                        }
                        SettingValues.prefs.edit().putString("defaultSorting", Reddit.defaultSorting.name()).apply();
                        SettingValues.prefs.edit().putString("timePeriod", Reddit.timePeriod.name()).apply();
                        SettingValues.defaultSorting = Reddit.defaultSorting;
                        SettingValues.timePeriod = Reddit.timePeriod;
                    }
                };
                int i = Reddit.defaultSorting == Sorting.HOT ? 0
                        : Reddit.defaultSorting == Sorting.NEW ? 1
                        : Reddit.defaultSorting == Sorting.RISING ? 2
                        : Reddit.defaultSorting == Sorting.TOP ?
                        (Reddit.timePeriod == TimePeriod.HOUR ? 3
                                : Reddit.timePeriod == TimePeriod.DAY ? 4
                                : Reddit.timePeriod == TimePeriod.WEEK ? 5
                                : Reddit.timePeriod == TimePeriod.MONTH ? 6
                                : Reddit.timePeriod == TimePeriod.YEAR ? 7
                                : 8)
                        : Reddit.defaultSorting == Sorting.CONTROVERSIAL ?
                        (Reddit.timePeriod == TimePeriod.HOUR ? 9
                                : 10)
                        : 0;
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Settings.this);
                builder.setTitle(R.string.sorting_choose);
                builder.setSingleChoiceItems(
                        new String[]{
                                getString(R.string.sorting_hot),
                                getString(R.string.sorting_new),
                                getString(R.string.sorting_rising),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_hour),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_day),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_week),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_month),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_year),
                                getString(R.string.sorting_top) + " " + getString(R.string.sorting_all),
                                getString(R.string.sorting_controversial) + " " + getString(R.string.sorting_hour),
                                getString(R.string.sorting_controversial) + " " + getString(R.string.sorting_day),
                        }, i, l2);
                builder.show();
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
                    checkBox.setText(getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));

                }
                landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                    @Override
                    public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                        checkBox.setText(getString(R.string.settings_notification,
                                TimeUtils.getTimeInHoursAndMins(i1 * 15, getBaseContext())));
                    }
                });
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!isChecked) {
                            Reddit.notificationTime = -1;
                            Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                            if(Reddit.notifications != null)
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


                final Dialog dialog = builder.setView(dialoglayout).create();
                dialog.show();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue() * 15;
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            Reddit.notifications.cancel(getApplication());
                            Reddit.notifications.start(getApplication());
                        }
                    }
                });
                dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View d) {
                        if (checkBox.isChecked()) {
                            Reddit.notificationTime = landscape.getValue() * 15;
                            Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                            Reddit.notifications.cancel(getApplication());
                            Reddit.notifications.start(getApplication());
                            dialog.dismiss();

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