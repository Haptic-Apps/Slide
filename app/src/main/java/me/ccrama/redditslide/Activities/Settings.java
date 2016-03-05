package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.DragSort.ReorderSubreddits;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends BaseActivity {
    private final static int RESTART_SETTINGS_RESULT = 2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESTART_SETTINGS_RESULT) {
            Intent i = new Intent(Settings.this, Settings.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            overridePendingTransition(0, 0);

            finish();
            overridePendingTransition(0, 0);


        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings);
        setupAppBar(R.id.toolbar, R.string.title_settings, true, true);

        SettingValues.expandedSettings = true;
        setSettingItems();

    }

    private void setSettingItems() {
        View pro = findViewById(R.id.pro);
        if (SettingValues.tabletUI) pro.setVisibility(View.GONE);
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
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });
        findViewById(R.id.about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsAbout.class);
                startActivity(i);
            }
        });


        findViewById(R.id.subtheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsSubreddit.class);
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });



        findViewById(R.id.filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsFilter.class);
                startActivity(i);
            }
        });
        findViewById(R.id.synccit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsSynccit.class);
                startActivity(i);
            }
        });
        if(Authentication.isLoggedIn) {
            findViewById(R.id.reorder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Settings.this, ReorderSubreddits.class);
                    Settings.this.startActivity(inte);
                }
            });
        } else {
            findViewById(R.id.reorder).setVisibility(View.GONE);
        }

        findViewById(R.id.theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Settings.this, SettingsTheme.class);
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });
        findViewById(R.id.handling).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsHandling.class);
                startActivity(i);
            }
        });
        findViewById(R.id.layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                startActivity(i);
            }
        });
        findViewById(R.id.backup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsBackup.class);
                startActivity(i);
            }
        });

        findViewById(R.id.font).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.this, SettingsFont.class);
                startActivity(i);
            }
        });
        findViewById(R.id.tablet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                if (SettingValues.tabletUI) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.tabletui, null);
                    final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Settings.this);

                    dialoglayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
                    //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);
                    final SeekBar landscape = (SeekBar) dialoglayout.findViewById(R.id.landscape);

                    //todo  portrait.setBackgroundColor(Palette.getDefaultColor());
                    landscape.setProgress(Reddit.dpWidth - 1);

                    ((TextView)dialoglayout.findViewById(R.id.progressnumber)).setText(landscape.getProgress() + 1 + " columns in landscape");

                    landscape.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            ((TextView) dialoglayout.findViewById(R.id.progressnumber)).setText(landscape.getProgress() + 1 + " columns in landscape");

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    final Dialog dialog = builder.setView(dialoglayout).create();
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Reddit.dpWidth = landscape.getProgress() + 1;
                            Reddit.seen.edit().putInt("tabletOVERRIDE", landscape.getProgress() + 1).apply();
                        }
                    });
                    SwitchCompat s = (SwitchCompat) dialog.findViewById(R.id.dualcolumns);
                    s.setChecked(SettingValues.dualPortrait);
                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.dualPortrait = isChecked;
                            SettingValues.prefs.edit().putBoolean(SettingValues.PREF_DUAL_PORTRAIT, isChecked).apply();
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
        findViewById(R.id.comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(Settings.this, SettingsComments.class);
                Settings.this.startActivity(inte);
            }
        });

        if(Authentication.isLoggedIn) {
            findViewById(R.id.reddit_settings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Settings.this, SettingsReddit.class);
                    startActivity(i);
                }
            });
        } else {
            findViewById(R.id.reddit_settings).setVisibility(View.GONE);
        }

    }

}