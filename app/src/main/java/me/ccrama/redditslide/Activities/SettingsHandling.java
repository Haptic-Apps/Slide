package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;
import java.util.Map;

import me.ccrama.redditslide.PostMatch;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsHandling extends BaseActivityAnim
        implements CompoundButton.OnCheckedChangeListener {

    public ArrayList<String> domains = new ArrayList<>();
    EditText domain;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.web:
                SettingValues.web = isChecked;
                (((SwitchCompat) findViewById(R.id.chrome))).setEnabled(isChecked);
                SettingValues.prefs.edit().putBoolean(SettingValues.PREFS_WEB, isChecked).apply();
                break;
            case R.id.image:
                SettingValues.image = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_IMAGE, isChecked).apply();
                break;
            case R.id.gif:
                SettingValues.gif = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_GIF, isChecked).apply();
                break;
            case R.id.album:
                SettingValues.album = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_ALBUM, isChecked).apply();
                break;
            case R.id.shortlink:
                SettingValues.shareLongLink = !isChecked;
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_LONG_LINK, !isChecked)
                        .apply();
                break;
            case R.id.peek:
                SettingValues.peek = isChecked;
                SettingValues.prefs.edit().putBoolean(SettingValues.PREF_PEEK, isChecked).apply();
                break;
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_handling);
        setupAppBar(R.id.toolbar, R.string.settings_link_handling, true, true);

        TextView web = (TextView) findViewById(R.id.browser);

        //todo web stuff
        SwitchCompat image = (SwitchCompat) findViewById(R.id.image);
        SwitchCompat gif = (SwitchCompat) findViewById(R.id.gif);
        SwitchCompat album = (SwitchCompat) findViewById(R.id.album);
        SwitchCompat peek = (SwitchCompat) findViewById(R.id.peek);
        SwitchCompat shortlink = (SwitchCompat) findViewById(R.id.shortlink);

        image.setChecked(SettingValues.image);
        gif.setChecked(SettingValues.gif);
        album.setChecked(SettingValues.album);
        peek.setChecked(SettingValues.peek);
        shortlink.setChecked(!SettingValues.shareLongLink);

        image.setOnCheckedChangeListener(this);
        gif.setOnCheckedChangeListener(this);
        album.setOnCheckedChangeListener(this);
        peek.setOnCheckedChangeListener(this);
        shortlink.setOnCheckedChangeListener(this);

        if (!Reddit.videoPlugin) {
            findViewById(R.id.video).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=ccrama.me.slideyoutubeplugin")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                                "http://play.google.com/store/apps/details?id=ccrama.me.slideyoutubeplugin")));
                    }
                }
            });
        } else {
            findViewById(R.id.video).setVisibility(View.GONE);
        }
        ((TextView) findViewById(R.id.browser)).setText(
                SettingValues.web ? SettingValues.reader ? getString(
                        R.string.handling_reader_mode)
                        : (SettingValues.customtabs ? getString(
                                R.string.settings_link_chrome)
                                : getString(R.string.handling_internal_browser))
                        : getString(R.string.handling_external_browser));


        final SwitchCompat readernight = (SwitchCompat) findViewById(R.id.readernight);
        readernight.setEnabled(
                SettingValues.nightMode && SettingValues.web && SettingValues.reader);
        readernight.setChecked(SettingValues.readerNight);
        readernight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingValues.readerNight = isChecked;
                SettingValues.prefs.edit()
                        .putBoolean(SettingValues.PREF_READER_NIGHT, isChecked)
                        .apply();
            }
        });
        findViewById(R.id.select_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsHandling.this, v);

                popup.getMenuInflater().inflate(R.menu.browser_type, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.chrome:
                                SettingValues.customtabs = true;
                                SettingValues.web = true;
                                SettingValues.reader = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, true)
                                        .apply();
                                break;
                            case R.id.internal:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.reader = false;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                break;
                            case R.id.reader:
                                SettingValues.customtabs = false;
                                SettingValues.web = true;
                                SettingValues.reader = true;
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, true)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_CUSTOMTABS, false)
                                        .apply();
                                break;
                            case R.id.external:
                                final Map<String, String> installedBrowsers =
                                        Reddit.getInstalledBrowsers();
                                if (installedBrowsers.size() > 1) {
                                    AlertDialogWrapper.Builder builder =
                                            new AlertDialogWrapper.Builder(SettingsHandling.this);
                                    builder.setTitle(R.string.browser_choose);
                                    builder.setSingleChoiceItems(installedBrowsers.values()
                                                    .toArray(new String[installedBrowsers.size()]), -1,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                        int which) {
                                                    SettingValues.selectedBrowser =
                                                            installedBrowsers.keySet()
                                                                    .toArray(
                                                                            new String[installedBrowsers
                                                                                    .size()])[which];
                                                    SettingValues.prefs.edit()
                                                            .putString(
                                                                    SettingValues.PREF_SELECTED_BROWSER,
                                                                    SettingValues.selectedBrowser)
                                                            .apply();
                                                }
                                            });
                                    builder.show();
                                }
                                SettingValues.selectedBrowser = "";
                                SettingValues.web = false;
                                SettingValues.reader = false;
                                SettingValues.prefs.edit()
                                        .putString(SettingValues.PREF_SELECTED_BROWSER,
                                                SettingValues.selectedBrowser)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREFS_WEB, false)
                                        .apply();
                                SettingValues.prefs.edit()
                                        .putBoolean(SettingValues.PREF_READER, false)
                                        .apply();
                                break;
                        }
                        ((TextView) findViewById(R.id.browser)).setText(
                                SettingValues.web ? SettingValues.reader ? getString(
                                        R.string.handling_reader_mode)
                                        : (SettingValues.customtabs ? getString(
                                                R.string.settings_link_chrome)
                                                : getString(R.string.handling_internal_browser))
                                        : getString(R.string.handling_external_browser));
                        readernight.setEnabled(SettingValues.nightMode
                                && SettingValues.web
                                && SettingValues.reader);

                        return true;
                    }
                });
                popup.show();
            }
        });

        domain = (EditText) findViewById(R.id.domain);
        domain.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.alwaysExternal =
                            SettingValues.alwaysExternal + ", " + domain.getText().toString();
                    domain.setText("");
                    updateFilters();
                }
                return false;
            }
        });
        updateFilters();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor e = SettingValues.prefs.edit();

        e.putString(SettingValues.PREF_ALWAYS_EXTERNAL, Reddit.arrayToString(domains));
        e.apply();

        PostMatch.externalDomain = null;

        SettingValues.alwaysExternal =
                SettingValues.prefs.getString(SettingValues.PREF_ALWAYS_EXTERNAL, "");
    }

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (String s : SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+")) {
            if (!s.isEmpty() && (!Reddit.videoPlugin || (!s.contains("youtube.co") && !s.contains(
                    "youtu.be")))) {
                s = s.trim();
                final String finalS = s;
                domains.add(finalS);
                final View t = getLayoutInflater().inflate(R.layout.account_textview,
                        ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(s);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(finalS);
                        SettingValues.alwaysExternal = Reddit.arrayToString(domains);
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }
    }

}