package me.ccrama.redditslide.Fragments;

import android.app.Activity;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.util.LinkUtil;

public class SettingsHandlingFragment implements CompoundButton.OnCheckedChangeListener {

    private final Activity context;
    LinearLayout domainListLayout;

    public SettingsHandlingFragment(Activity context) {
        this.context = context;
    }

    public void Bind() {
        //todo web stuff
        SwitchCompat shortlink = context.findViewById(R.id.settings_handling_shortlink);
        SwitchCompat gif = context.findViewById(R.id.settings_handling_gif);
        SwitchCompat hqgif = context.findViewById(R.id.settings_handling_hqgif);
        SwitchCompat image = context.findViewById(R.id.settings_handling_image);
        SwitchCompat album = context.findViewById(R.id.settings_handling_album);
        SwitchCompat peek = context.findViewById(R.id.settings_handling_peek);

        shortlink.setChecked(!SettingValues.shareLongLink);
        gif.setChecked(SettingValues.gif);
        hqgif.setChecked(SettingValues.hqgif);
        image.setChecked(SettingValues.image);
        album.setChecked(SettingValues.album);
        peek.setChecked(SettingValues.peek);

        shortlink.setOnCheckedChangeListener(this);
        gif.setOnCheckedChangeListener(this);
        hqgif.setOnCheckedChangeListener(this);
        image.setOnCheckedChangeListener(this);
        album.setOnCheckedChangeListener(this);
        peek.setOnCheckedChangeListener(this);

        final SwitchCompat readerMode = context.findViewById(R.id.settings_handling_reader_mode);
        final SwitchCompat readernight = context.findViewById(R.id.settings_handling_readernight);

        final RelativeLayout handlingVideoLayout = context.findViewById(R.id.settings_handling_video);
        domainListLayout = context.findViewById(R.id.settings_handling_domainlist);
        final EditText domainListEditText = context.findViewById(R.id.settings_handling_domain_edit);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Browser */
        setUpBrowserLinkHandling();
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        readerMode.setChecked(SettingValues.readerMode);
        readerMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.readerMode = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_READER_MODE, SettingValues.readerMode);
            readernight.setEnabled(SettingValues.NightModeState.isEnabled() && SettingValues.readerMode);
        });
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        readernight.setEnabled(SettingValues.NightModeState.isEnabled() && SettingValues.readerMode);
        readernight.setChecked(SettingValues.readerNight);
        readernight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.readerNight = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_READER_NIGHT, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (!Reddit.videoPlugin) {
            handlingVideoLayout.setOnClickListener(v ->
                    LinkUtil.launchMarketUri(context, R.string.youtube_plugin_package));
        } else {
            handlingVideoLayout.setVisibility(View.GONE);
        }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        /* activity_settings_handling_child.xml does not load these elements so we need to null check */
        if (domainListEditText != null & domainListLayout != null) {
            domainListEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SettingValues.alwaysExternal.add(
                            domainListEditText.getText().toString().toLowerCase(Locale.ENGLISH).trim());
                    domainListEditText.setText("");
                    updateFilters();
                }
                return false;
            });
            updateFilters();
        }
    }

    private void setUpBrowserLinkHandling() {
        final RadioGroup browserTypeRadioGroup = context.findViewById(R.id.settings_handling_select_browser_type);
        final RelativeLayout selectBrowserLayout = context.findViewById(R.id.settings_handling_select_browser_layout);
        final TextView webBrowserView = context.findViewById(R.id.settings_handling_browser);

        browserTypeRadioGroup.check(LinkHandlingMode.idResFromValue(SettingValues.linkHandlingMode));
        browserTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SettingValues.linkHandlingMode = LinkHandlingMode.valueFromIdRes(checkedId);
            SettingValues.prefs.edit()
                    .putInt(SettingValues.PREF_LINK_HANDLING_MODE, SettingValues.linkHandlingMode)
                    .apply();
        });

        final HashMap<String, String> installedBrowsers = Reddit.getInstalledBrowsers();
        if (!installedBrowsers.containsKey(SettingValues.selectedBrowser)) {
            SettingValues.selectedBrowser = "";
            SettingValues.prefs.edit()
                    .putString(SettingValues.PREF_SELECTED_BROWSER, SettingValues.selectedBrowser)
                    .apply();
        }
        webBrowserView.setText(installedBrowsers.get(SettingValues.selectedBrowser));
        if (installedBrowsers.size() <= 1) {
            selectBrowserLayout.setVisibility(View.GONE);
        } else {
            selectBrowserLayout.setVisibility(View.VISIBLE);
            selectBrowserLayout.setOnClickListener(v -> {
                final PopupMenu popupMenu = new PopupMenu(context, v);
                final HashMap<MenuItem, String> packageNames = new HashMap<>();

                for (Map.Entry<String, String> entry : installedBrowsers.entrySet()) {
                    final MenuItem menuItem = popupMenu.getMenu().add(entry.getValue());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        menuItem.setTooltipText(entry.getKey());
                    }

                    packageNames.put(menuItem, entry.getKey());
                }

                popupMenu.setOnMenuItemClickListener(item -> {
                    SettingValues.selectedBrowser = packageNames.get(item);
                    SettingValues.prefs.edit()
                            .putString(SettingValues.PREF_SELECTED_BROWSER,
                                    SettingValues.selectedBrowser)
                            .apply();
                    webBrowserView.setText(item.getTitle());
                    return true;
                });
                popupMenu.show();
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.settings_handling_shortlink:
                SettingValues.shareLongLink = !isChecked;
                editSharedBooleanPreference(SettingValues.PREF_LONG_LINK, !isChecked);
                break;
            case R.id.settings_handling_gif:
                SettingValues.gif = isChecked;
                editSharedBooleanPreference(SettingValues.PREF_GIF, isChecked);
                break;
            case R.id.settings_handling_hqgif:
                SettingValues.hqgif = isChecked;
                editSharedBooleanPreference(SettingValues.PREF_HQGIF, isChecked);
                break;
            case R.id.settings_handling_image:
                SettingValues.image = isChecked;
                editSharedBooleanPreference(SettingValues.PREF_IMAGE, isChecked);
                break;
            case R.id.settings_handling_album:
                SettingValues.album = isChecked;
                editSharedBooleanPreference(SettingValues.PREF_ALBUM, isChecked);
                break;
            case R.id.settings_handling_peek:
                SettingValues.peek = isChecked;
                editSharedBooleanPreference(SettingValues.PREF_PEEK, isChecked);
                break;
        }
    }

    private void updateFilters() {
        domainListLayout.removeAllViews();
        for (String s : SettingValues.alwaysExternal) {
            if (!s.isEmpty() && (!Reddit.videoPlugin || !s.contains("youtube.co") && !s.contains("youtu.be"))) {
                final View t = context.getLayoutInflater().inflate(R.layout.account_textview,
                        domainListLayout, false);
                final TextView accountTextViewName = t.findViewById(R.id.name);
                final ImageView accountTextViewRemove = t.findViewById(R.id.remove);
                accountTextViewName.setText(s);
                accountTextViewRemove.setOnClickListener(v -> {
                    SettingValues.alwaysExternal.remove(s);
                    updateFilters();
                });
                domainListLayout.addView(t);
            }
        }
    }

    private void editSharedBooleanPreference(final String settingValueString, final boolean isChecked) {
        SettingValues.prefs.edit().putBoolean(settingValueString, isChecked).apply();
    }

    public enum LinkHandlingMode {
        EXTERNAL(0, R.id.settings_handling_browser_type_external_browser),
        INTERNAL(1, R.id.settings_handling_browser_type_internal_browser),
        CUSTOM_TABS(2, R.id.settings_handling_browser_type_custom_tabs);

        private static final BiMap<Integer, Integer> sBiMap =
                HashBiMap.create(new HashMap<Integer, Integer>() {{
                    put(EXTERNAL.getValue(), EXTERNAL.getIdRes());
                    put(INTERNAL.getValue(), INTERNAL.getIdRes());
                    put(CUSTOM_TABS.getValue(), CUSTOM_TABS.getIdRes());
                }});
        private final int mValue;
        @IdRes
        private final int mIdRes;

        LinkHandlingMode(int value, @IdRes int stringRes) {
            mValue = value;
            mIdRes = stringRes;
        }

        public static int idResFromValue(int value) {
            return sBiMap.get(value);
        }

        public static int valueFromIdRes(@IdRes int idRes) {
            return sBiMap.inverse().get(idRes);
        }

        public int getValue() {
            return mValue;
        }

        public int getIdRes() {
            return mIdRes;
        }
    }
}
