package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.google.common.base.Strings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.BuildConfig;
import me.ccrama.redditslide.DragSort.ReorderSubreddits;
import me.ccrama.redditslide.Fragments.FolderChooserDialogCreate;
import me.ccrama.redditslide.Fragments.ManageOfflineContentFragment;
import me.ccrama.redditslide.Fragments.SettingsCommentsFragment;
import me.ccrama.redditslide.Fragments.SettingsDataFragment;
import me.ccrama.redditslide.Fragments.SettingsFontFragment;
import me.ccrama.redditslide.Fragments.SettingsFragment;
import me.ccrama.redditslide.Fragments.SettingsGeneralFragment;
import me.ccrama.redditslide.Fragments.SettingsHandlingFragment;
import me.ccrama.redditslide.Fragments.SettingsHistoryFragment;
import me.ccrama.redditslide.Fragments.SettingsRedditFragment;
import me.ccrama.redditslide.Fragments.SettingsThemeFragment;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.NetworkUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;


/**
 * Created by ccrama on 3/5/2015.
 */
public class Settings extends BaseActivity
        implements FolderChooserDialogCreate.FolderCallback, SettingsFragment.RestartActivity {

    private final static int RESTART_SETTINGS_RESULT = 2;
    private       int                                                scrollY;
    private       SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private       String                                             prev_text;
    public static boolean                                            changed;  //whether or not a Setting was changed

    private SettingsGeneralFragment      mSettingsGeneralFragment      = new SettingsGeneralFragment(this);
    private ManageOfflineContentFragment mManageOfflineContentFragment = new ManageOfflineContentFragment(this);
    private SettingsThemeFragment        mSettingsThemeFragment        = new SettingsThemeFragment(this);
    private SettingsFontFragment         mSettingsFontFragment         = new SettingsFontFragment(this);
    private SettingsCommentsFragment     mSettingsCommentsFragment     = new SettingsCommentsFragment(this);
    private SettingsHandlingFragment     mSettingsHandlingFragment     = new SettingsHandlingFragment(this);
    private SettingsHistoryFragment      mSettingsHistoryFragment      = new SettingsHistoryFragment(this);
    private SettingsDataFragment         mSettingsDataFragment         = new SettingsDataFragment(this);
    private SettingsRedditFragment       mSettingsRedditFragment       = new SettingsRedditFragment(this);

    private List<Integer> settings_activities = new ArrayList<>(
            Arrays.asList(
                    R.layout.activity_settings_general_child,
                    R.layout.activity_manage_history_child,
                    R.layout.activity_settings_theme_child,
                    R.layout.activity_settings_font_child,
                    R.layout.activity_settings_comments_child,
                    R.layout.activity_settings_handling_child,
                    R.layout.activity_settings_history_child,
                    R.layout.activity_settings_datasaving_child,
                    R.layout.activity_settings_reddit_child
            )
    );

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESTART_SETTINGS_RESULT) {
            restartActivity();
        }
    }

    public void restartActivity() {
        Intent i = new Intent(Settings.this, Settings.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra("position", scrollY);
        i.putExtra("prev_text", prev_text);
        startActivity(i);
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                if(findViewById(R.id.settings_search).getVisibility() == View.VISIBLE){
                    findViewById(R.id.settings_search).setVisibility(View.GONE);
                    findViewById(R.id.search).setVisibility(View.VISIBLE);
                } else {
                    onBackPressed();
                }
                return true;
            case R.id.search: {
                findViewById(R.id.settings_search).setVisibility(View.VISIBLE);
                findViewById(R.id.search).setVisibility(View.GONE);
            }
            return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings);
        setupAppBar(R.id.toolbar, R.string.title_settings, true, true);

        if (getIntent() != null && !Strings.isNullOrEmpty(getIntent().getStringExtra("prev_text"))) {
            prev_text = getIntent().getStringExtra("prev_text");
        } else if (savedInstanceState != null) {
            prev_text = savedInstanceState.getString("prev_text");
        }

        if (!Strings.isNullOrEmpty(prev_text)) {
            ((EditText) findViewById(R.id.settings_search)).setText(prev_text);
        }

        BuildLayout(prev_text);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", scrollY);
        outState.putString("prev_text", prev_text);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            onOptionsItemSelected(mToolbar.getMenu().findItem(R.id.search));
//            (findViewById(R.id.settings_search)).requestFocus();
            MotionEvent motionEventDown = MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    0, 0, 0
            );
            MotionEvent motionEventUp = MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,
                    0, 0, 0
            );
            (findViewById(R.id.settings_search)).dispatchTouchEvent(motionEventDown);
            (findViewById(R.id.settings_search)).dispatchTouchEvent(motionEventUp);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void BuildLayout(String text) {
        LinearLayout parent = (LinearLayout) findViewById(R.id.settings_parent);

        /* Clear the settings out, then re-add the default top-level settings */
        parent.removeAllViews();
        parent.addView(getLayoutInflater().inflate(R.layout.activity_settings_child, null));
        Bind();

        /* The EditView contains text that we can use to search for matching settings */
        if (!Strings.isNullOrEmpty(text)){
            LayoutInflater inflater = getLayoutInflater();

            for (Integer activity: settings_activities) {
                parent.addView(inflater.inflate(activity, null));
            }

            mSettingsGeneralFragment.Bind();
            mManageOfflineContentFragment.Bind();
            mSettingsThemeFragment.Bind();
            mSettingsFontFragment.Bind();
            mSettingsCommentsFragment.Bind();
            mSettingsHandlingFragment.Bind();
            mSettingsHistoryFragment.Bind();
            mSettingsDataFragment.Bind();
            mSettingsRedditFragment.Bind();

            /* Go through each subview and scan it for matching text, non-matches */
            loopViews(parent, text.toLowerCase(), true, "");
        }

        /* Try to clean up the mess we've made */
        System.gc();
    }

    private void Bind() {

        SettingValues.expandedSettings = true;
        setSettingItems();

        final ScrollView mScrollView = ((ScrollView) findViewById(R.id.base));

        prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Settings.changed = true;
            }
        };

        SettingValues.prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        mScrollView.post(new Runnable() {

            @Override
            public void run() {
                ViewTreeObserver observer = mScrollView.getViewTreeObserver();
                if (getIntent().hasExtra("position")) {
                    mScrollView.scrollTo(0, getIntent().getIntExtra("position", 0));
                }
                if (getIntent().hasExtra("prev_text")) {
                    prev_text = getIntent().getStringExtra("prev_text");
                }
                observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

                    @Override
                    public void onScrollChanged() {
                        scrollY = mScrollView.getScrollY();
                    }
                });
            }
        });
    }

    private boolean loopViews(ViewGroup parent, String text, boolean isRootViewGroup, String indent) {

        boolean foundText = false;
        boolean prev_child_is_View = false;

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);
            boolean childRemoved = false;

            /* Found some text, remove labels and check for matches on non-labels */
            if (child instanceof TextView) {

                // Found text at the top-level that is probably a label, or an explicitly tagged label
                if (isRootViewGroup ||
                        (child.getTag() != null && child.getTag().toString().equals("label"))) {
                    parent.removeView(child);
                    childRemoved = true;
                    i--;
                }

                // Found matching text!
                else if (((TextView) child).getText().toString().toLowerCase().contains(text)) {
                    foundText = true;
                }

                // No match
            }

            /* This child is a View and the previous child was a View, remove duplicates */
            else if (child != null && prev_child_is_View && child.getClass() == View.class) {
                parent.removeView(child);
                childRemoved = true;
                i--;
            }

            /* Found a group, need to recursively search through it */
            else if (child instanceof ViewGroup) {
                // Look for matching TextView in the ViewGroup, remove the ViewGroup if no match is found
                if (!this.loopViews((ViewGroup) child, text, false, indent + "  ")) {
                    parent.removeView(child);
                    childRemoved = true;
                    i--;
                } else {
                    foundText = true;
                }
            }

            if (child != null && !childRemoved) {
                prev_child_is_View = child.getClass() == View.class;
            }
        }
        return foundText;
    }

    private void setSettingItems() {
        View pro = findViewById(R.id.settings_child_pro);
        if (SettingValues.isPro) {
            pro.setVisibility(View.GONE);
        } else {
            pro.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    new AlertDialogWrapper.Builder(Settings.this).setTitle(
                            R.string.settings_support_slide)
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=" + getString(
                                                                        R.string.ui_unlock_package))));
                                            } catch (ActivityNotFoundException e) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id="
                                                                        + getString(
                                                                        R.string.ui_unlock_package))));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            dialog.dismiss();
                                        }
                                    })
                            .show();
                }
            });
        }

        ((EditText) findViewById(R.id.settings_search)).addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString().trim();
                /* No idea why, but this event can fire many times when there is no change */
                if (text.equalsIgnoreCase(prev_text)) return;
                BuildLayout(text);
                prev_text = text;
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        findViewById(R.id.settings_child_general).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsGeneral.class);
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });

        findViewById(R.id.settings_child_history).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsHistory.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_about).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsAbout.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_offline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.this, ManageOfflineContent.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_datasave).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsData.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_subtheme).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsSubreddit.class);
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });

        findViewById(R.id.settings_child_filter).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Intent i = new Intent(Settings.this, SettingsFilter.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_synccit).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Intent i = new Intent(Settings.this, SettingsSynccit.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_reorder).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent inte = new Intent(Settings.this, ReorderSubreddits.class);
                Settings.this.startActivity(inte);
            }
        });

        findViewById(R.id.settings_child_maintheme).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                Intent i = new Intent(Settings.this, SettingsTheme.class);
                startActivityForResult(i, RESTART_SETTINGS_RESULT);
            }
        });

        findViewById(R.id.settings_child_handling).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsHandling.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_layout).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, EditCardsLayout.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_backup).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsBackup.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_font).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent i = new Intent(Settings.this, SettingsFont.class);
                startActivity(i);
            }
        });

        findViewById(R.id.settings_child_tablet).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                if (SettingValues.isPro) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.tabletui, null);
                    final AlertDialogWrapper.Builder builder =
                            new AlertDialogWrapper.Builder(Settings.this);
                    final Resources res = getResources();

                    dialoglayout.findViewById(R.id.title)
                            .setBackgroundColor(Palette.getDefaultColor());
                    //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);
                    final SeekBar landscape = dialoglayout.findViewById(R.id.landscape);

                    //todo  portrait.setBackgroundColor(Palette.getDefaultColor());
                    landscape.setProgress(Reddit.dpWidth - 1);

                    ((TextView) dialoglayout.findViewById(R.id.progressnumber)).setText(
                            res.getQuantityString(R.plurals.landscape_columns,
                                    landscape.getProgress() + 1, landscape.getProgress() + 1));

                    landscape.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
                            ((TextView) dialoglayout.findViewById(R.id.progressnumber)).setText(
                                    res.getQuantityString(R.plurals.landscape_columns,
                                            landscape.getProgress() + 1,
                                            landscape.getProgress() + 1));
                            Settings.changed = true;
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
                            Reddit.colors.edit()
                                    .putInt("tabletOVERRIDE", landscape.getProgress() + 1)
                                    .apply();
                        }
                    });
                    SwitchCompat s = dialog.findViewById(R.id.dualcolumns);
                    s.setChecked(SettingValues.dualPortrait);
                    s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.dualPortrait = isChecked;
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_DUAL_PORTRAIT, isChecked)
                                    .apply();
                        }
                    });
                    SwitchCompat s2 = dialog.findViewById(R.id.fullcomment);
                    s2.setChecked(SettingValues.fullCommentOverride);
                    s2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.fullCommentOverride = isChecked;
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_FULL_COMMENT_OVERRIDE, isChecked)
                                    .apply();
                        }
                    });
                    SwitchCompat s3 = dialog.findViewById(R.id.singlecolumnmultiwindow);
                    s3.setChecked(SettingValues.singleColumnMultiWindow);
                    s3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            SettingValues.singleColumnMultiWindow = isChecked;
                            SettingValues.prefs.edit()
                                    .putBoolean(SettingValues.PREF_SINGLE_COLUMN_MULTI, isChecked)
                                    .apply();
                        }
                    });
                } else {
                    new AlertDialogWrapper.Builder(Settings.this).setTitle(
                            "Mutli-Column Settings are a Pro feature")
                            .setMessage(R.string.pro_upgrade_msg)
                            .setPositiveButton(R.string.btn_yes_exclaim,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "market://details?id=" + getString(
                                                                        R.string.ui_unlock_package))));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "http://play.google.com/store/apps/details?id="
                                                                        + getString(
                                                                        R.string.ui_unlock_package))));
                                            }
                                        }
                                    })
                            .setNegativeButton(R.string.btn_no_danks,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                        }
                                    })
                            .show();
                }
            }
        });

        if(BuildConfig.isFDroid){
            ((TextView) findViewById(R.id.settings_child_donatetext)).setText("Donate via PayPal");
        }
        findViewById(R.id.settings_child_support).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(BuildConfig.isFDroid){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=56FKCCYLX7L72"));
                    startActivity(browserIntent);
                } else {
                    Intent inte = new Intent(Settings.this, DonateView.class);
                    Settings.this.startActivity(inte);
                }
            }
        });

        findViewById(R.id.settings_child_comments).setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent inte = new Intent(Settings.this, SettingsComments.class);
                Settings.this.startActivity(inte);
            }
        });

        if (Authentication.isLoggedIn && NetworkUtil.isConnected(this)) {
            findViewById(R.id.settings_child_reddit_settings).setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    Intent i = new Intent(Settings.this, SettingsReddit.class);
                    startActivity(i);
                }
            });
        } else {
            findViewById(R.id.settings_child_reddit_settings).setEnabled(false);
            findViewById(R.id.settings_child_reddit_settings).setAlpha(0.25f);
        }

        if (Authentication.mod) {
            findViewById(R.id.settings_child_moderation).setVisibility(View.VISIBLE);
            findViewById(R.id.settings_child_moderation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Settings.this, SettingsModeration.class);
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialogCreate dialog, @NonNull File folder, boolean isSaveToLocation) {
        mSettingsGeneralFragment.onFolderSelection(dialog, folder, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SettingValues.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
    }

}
