package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends BaseActivity {

    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        if (Authentication.isLoggedIn) {
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
            final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);
            final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);


            if (Reddit.notificationTime == -1) {
                checkBox.setChecked(false);
                checkBox.setText(context.getString(R.string.settings_mail_check));
            } else {
                checkBox.setChecked(true);
                landscape.setValue(Reddit.notificationTime / 15, false);
                checkBox.setText(context.getString(R.string.settings_notification,
                        TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));

            }
            landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                    if (checkBox.isChecked())
                    checkBox.setText(context.getString(R.string.settings_notification,
                            TimeUtils.getTimeInHoursAndMins(i1 * 15, context.getBaseContext())));
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        Reddit.notificationTime = -1;
                        Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                        checkBox.setText(context.getString(R.string.settings_mail_check));
                        landscape.setValue(0, true);
                        if (Reddit.notifications != null)
                            Reddit.notifications.cancel(context.getApplication());
                    } else {
                        Reddit.notificationTime = 60;
                        landscape.setValue(4, true);
                        checkBox.setText(context.getString(R.string.settings_notification,
                                TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                    }
                }
            });
            dialoglayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
            //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

            //todo  portrait.setBackgroundColor(Palette.getDefaultColor());


            final Dialog dialog = builder.setView(dialoglayout).create();
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (checkBox.isChecked()) {
                        Reddit.notificationTime = landscape.getValue() * 15;
                        Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        Reddit.notifications.start(context.getApplication());
                    }
                }
            });
            dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View d) {
                    if (checkBox.isChecked()) {
                        Reddit.notificationTime = landscape.getValue() * 15;
                        Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        Reddit.notifications.start(context.getApplication());
                        dialog.dismiss();
                        if (context instanceof Settings)
                            ((TextView) context.findViewById(R.id.notifications_current)).setText(
                                    context.getString(R.string.settings_notification_short,
                                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                    } else {
                        Reddit.notificationTime = -1;
                        Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        dialog.dismiss();
                        if (context instanceof Settings)
                            ((TextView) context.findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);


                    }
                }
            });

        } else {
            new AlertDialogWrapper.Builder(context)

                    .setTitle(R.string.general_login)
                    .setMessage(R.string.err_login)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent inte = new Intent(context, Login.class);
                            context.startActivity(inte);
                        }
                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    context.finish();
                }
            }).show();
        }
    }

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
        switch (id) {
            case R.id.action_expand_settings:
                Reddit.expandedSettings = !Reddit.expandedSettings;
                setSettingItems();
                item.setTitle(Reddit.expandedSettings ? "Show less" : "Show more");
                SettingValues.prefs.edit().putBoolean("expandedSettings", Reddit.expandedSettings).apply();
                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        menu.findItem(R.id.action_expand_settings).setTitle(Reddit.expandedSettings ? "Show less" : "Show more");
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings);
        setupAppBar(R.id.toolbar, R.string.title_settings, true);

        setSettingItems();

    }

    private void setSettingItems() {
        View pro = findViewById(R.id.pro);
        if (Reddit.tabletUI) pro.setVisibility(View.GONE);
        else {
            pro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                    }
                }
            });
        }

        findViewById(R.id.general).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsGeneral.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsAbout.class);
                startActivityForResult(i, 2);
            }
        });

        //Copy the latest stacktrace with a long click on the version number
        findViewById(R.id.about).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                SharedPreferences prefs = getSharedPreferences(
                        "STACKTRACE", Context.MODE_PRIVATE);
                String stacktrace = prefs.getString("stacktrace", null);
                if (stacktrace != null) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Stacktrace", stacktrace);
                    clipboard.setPrimaryClip(clip);
                }
                prefs.edit().clear().apply();
                return false;
            }
        });
        
        if (Reddit.expandedSettings) {
            findViewById(R.id.cache).setVisibility(View.VISIBLE);
            findViewById(R.id.cache).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Settings.this, SettingsCache.class);
                    startActivity(i);
                }
            });
        } else findViewById(R.id.cache).setVisibility(View.GONE);


        findViewById(R.id.subtheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsSubreddit.class);
                startActivityForResult(i, 2);
            }
        });

        findViewById(R.id.auto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Reddit.tabletUI) {

                    Intent i = new Intent(Settings.this, SettingsAutonight.class);
                    startActivity(i);
                } else {
                    new AlertDialogWrapper.Builder(Settings.this)

                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
                }
            }
        });
        findViewById(R.id.comments_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsComments.class);
                startActivity(i);
            }
        });
        if (Reddit.notificationTime > 0) {
            ((TextView) findViewById(R.id.notifications_current)).setText(getString(R.string.settings_notification_short,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));

        } else {
            ((TextView) findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);
        }

        findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                setupNotificationSettings(dialoglayout, Settings.this);

            }
        });
        findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsTheme.class);
                startActivityForResult(i, 2);
            }
        });
        findViewById(R.id.handling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsHandling.class);
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
        if (Reddit.expandedSettings) {
            findViewById(R.id.preset).setVisibility(View.VISIBLE);
            findViewById(R.id.preset).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Settings.this, EditCardsLayout.class);
                    i.putExtra("secondary", "yes");
                    startActivityForResult(i, 2);
                }
            });
        } else findViewById(R.id.preset).setVisibility(View.GONE);

        findViewById(R.id.tablet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                if (Reddit.tabletUI) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.tabletui, null);
                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Settings.this);

                    dialoglayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
                    //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);
                    final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);

                    //todo  portrait.setBackgroundColor(Palette.getDefaultColor());
                    landscape.setValue(Reddit.dpWidth, false);

                    final Dialog dialog = builder.setView(dialoglayout).create();
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Reddit.dpWidth = landscape.getValue();
                            Reddit.seen.edit().putInt("tabletOVERRIDE", landscape.getValue()).apply();
                        }
                    });
                } else {
                    new AlertDialogWrapper.Builder(Settings.this)

                            .setTitle(R.string.general_pro)
                            .setMessage(R.string.general_pro_msg)
                            .setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no_danks, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();
                }
            }
        });

        findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(Settings.this, DonateView.class);
                Settings.this.startActivity(inte);
            }
        });
        findViewById(R.id.reddit_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.reddit.com/prefs/"));
                startActivity(browserIntent);
            }
        });
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(Settings.this, SettingsFab.class);
                Settings.this.startActivity(inte);
            }
        });
    }

}