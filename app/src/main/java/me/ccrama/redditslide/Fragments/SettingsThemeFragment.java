package me.ccrama.redditslide.Fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.Activities.Slide;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.ColorPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.databinding.ChooseaccentBinding;
import me.ccrama.redditslide.databinding.ChoosemainBinding;
import me.ccrama.redditslide.databinding.ChoosethemesmallBinding;
import me.ccrama.redditslide.databinding.NightmodeBinding;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.OnSingleClickListener;
import me.ccrama.redditslide.util.ProUtil;
import uz.shift.colorpicker.LineColorPicker;

public class SettingsThemeFragment<ActivityType extends BaseActivity & SettingsFragment.RestartActivity> {

    public static boolean changed;
    private final ActivityType context;
    int back;

    public SettingsThemeFragment(final ActivityType context) {
        this.context = context;
    }

    public void Bind() {
        final RelativeLayout colorTintModeLayout = (RelativeLayout) context.findViewById(R.id.settings_theme_colorTintMode);
        final TextView currentTintTextView = (TextView) context.findViewById(R.id.settings_theme_tint_current);
        final SwitchCompat tintEverywhereSwitch = (SwitchCompat) context.findViewById(R.id.settings_theme_tint_everywhere);
        final SwitchCompat colorNavbarSwitch = (SwitchCompat) context.findViewById(R.id.settings_theme_colorNavbar);
        final SwitchCompat colorIconSwitch = (SwitchCompat) context.findViewById(R.id.settings_theme_colorAppIcon);

        back = new ColorPreferences(context).getFontStyle().getThemeType();

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* App Theme */
        setupSettingsThemePrimary();
        setupSettingsThemeAccent();
        setupSettingsThemeBase();
        setupSettingsThemeNight();

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//* Tinting */

        colorTintModeLayout.setOnClickListener(v -> {
            final PopupMenu popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.color_tinting_mode_settings, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.none:
                        setTintingMode(false, false);
                        break;
                    case R.id.background:
                        setTintingMode(true, false);
                        break;
                    case R.id.name:
                        setTintingMode(true, true);
                        break;
                }
                currentTintTextView.setText(
                        SettingValues.colorBack
                                ? SettingValues.colorSubName
                                ? context.getString(R.string.subreddit_name_tint)
                                : context.getString(R.string.card_background_tint)
                                : context.getString(R.string.misc_none));
                boolean enabled = !currentTintTextView.getText().equals(context.getString(R.string.misc_none));
                tintEverywhereSwitch.setEnabled(enabled);
                tintEverywhereSwitch.setChecked(SettingValues.colorEverywhere);
                tintEverywhereSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SettingValues.colorEverywhere = isChecked;
                    editSharedBooleanPreference(SettingValues.PREF_COLOR_EVERYWHERE, isChecked);
                });
                return true;
            });

            popup.show();
        });

        //Color tinting mode
        currentTintTextView.setText(
                SettingValues.colorBack
                        ? SettingValues.colorSubName
                        ? context.getString(R.string.subreddit_name_tint)
                        : context.getString(R.string.card_background_tint)
                        : context.getString(R.string.misc_none));

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        boolean enabled = !currentTintTextView.getText().equals(context.getString(R.string.misc_none));
        tintEverywhereSwitch.setEnabled(enabled);
        tintEverywhereSwitch.setChecked(SettingValues.colorEverywhere);
        tintEverywhereSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.colorEverywhere = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_COLOR_EVERYWHERE, isChecked);
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        colorNavbarSwitch.setChecked(SettingValues.colorNavBar);
        colorNavbarSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingsThemeFragment.changed = true;
            SettingValues.colorNavBar = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_COLOR_NAV_BAR, isChecked);
            context.themeSystemBars("");
            if (!isChecked) {
                context.getWindow().setNavigationBarColor(Color.TRANSPARENT);
            }
        });

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        colorIconSwitch.setChecked(SettingValues.colorIcon);
        colorIconSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SettingValues.colorIcon = isChecked;
            editSharedBooleanPreference(SettingValues.PREF_COLOR_ICON, isChecked);
            if (isChecked) {
                setComponentState(
                        Slide.class.getPackage().getName() + ".Slide",
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

                setComponentState(
                        ColorPreferences.getIconName(context,
                                Reddit.colors.getInt("DEFAULTCOLOR", 0)),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
            } else {
                setComponentState(
                        ColorPreferences.getIconName(context,
                                Reddit.colors.getInt("DEFAULTCOLOR", 0)),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

                setComponentState(
                        Slide.class.getPackage().getName() + ".Slide",
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
            }
        });

    }

    private void setupSettingsThemePrimary() {
        final LinearLayout mainTheme = (LinearLayout) context.findViewById(R.id.settings_theme_main);
        mainTheme.setOnClickListener(v -> {
            final ChoosemainBinding choosemainBinding = ChoosemainBinding.inflate(context.getLayoutInflater());
            final TextView title = choosemainBinding.title;
            title.setBackgroundColor(Palette.getDefaultColor());

            final LineColorPicker colorPicker = choosemainBinding.picker;
            final LineColorPicker colorPicker2 = choosemainBinding.picker2;

            colorPicker.setColors(ColorPreferences.getBaseColors(context));
            int currentColor = Palette.getDefaultColor();
            for (int i : colorPicker.getColors()) {
                for (int i2 : ColorPreferences.getColors(context.getBaseContext(), i)) {
                    if (i2 == currentColor) {
                        colorPicker.setSelectedColor(i);
                        colorPicker2.setColors(ColorPreferences.getColors(context.getBaseContext(), i));
                        colorPicker2.setSelectedColor(i2);
                        break;
                    }
                }
            }

            colorPicker.setOnColorChangedListener(c -> {
                SettingsThemeFragment.changed = true;
                colorPicker2.setColors(ColorPreferences.getColors(context.getBaseContext(), c));
                colorPicker2.setSelectedColor(c);
            });

            colorPicker2.setOnColorChangedListener(i -> {
                SettingsThemeFragment.changed = true;
                title.setBackgroundColor(colorPicker2.getColor());
                final Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
                if (toolbar != null)
                    toolbar.setBackgroundColor(colorPicker2.getColor());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = context.getWindow();
                    window.setStatusBarColor(
                            Palette.getDarkerColor(colorPicker2.getColor()));
                }
                context.setRecentBar(context.getString(R.string.title_theme_settings),
                        colorPicker2.getColor());
            });

            choosemainBinding.ok.setOnClickListener(v1 -> {
                if (SettingValues.colorIcon) {
                    setComponentState(
                            ColorPreferences.getIconName(context,
                                    Reddit.colors.getInt("DEFAULTCOLOR", 0)),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

                    setComponentState(
                            ColorPreferences.getIconName(context, colorPicker2.getColor()),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
                }
                Reddit.colors.edit()
                        .putInt("DEFAULTCOLOR", colorPicker2.getColor())
                        .apply();
                context.restartActivity();
            });

            new AlertDialog.Builder(context)
                    .setView(choosemainBinding.getRoot())
                    .show();
        });
    }

    private void setupSettingsThemeAccent() {
        final LinearLayout accentLayout = (LinearLayout) context.findViewById(R.id.settings_theme_accent);
        accentLayout.setOnClickListener(v -> {
            final ChooseaccentBinding chooseaccentBinding = ChooseaccentBinding.inflate(context.getLayoutInflater());
            final TextView title = chooseaccentBinding.title;
            title.setBackgroundColor(Palette.getDefaultColor());

            final LineColorPicker colorPicker = chooseaccentBinding.picker3;

            int[] arrs = new int[ColorPreferences.getNumColorsFromThemeType(0)];
            int i = 0;
            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                if (type.getThemeType() == ColorPreferences.ColorThemeOptions.Dark.getValue()) {
                    arrs[i] = ContextCompat.getColor(context, type.getColor());
                    i++;
                }
            }

            colorPicker.setColors(arrs);
            colorPicker.setSelectedColor(ContextCompat.getColor(context,
                    new ColorPreferences(context).getFontStyle().getColor()));

            chooseaccentBinding.ok.setOnClickListener(v1 -> {
                SettingsThemeFragment.changed = true;
                int color = colorPicker.getColor();
                ColorPreferences.Theme t = null;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (ContextCompat.getColor(context, type.getColor()) == color
                            && back == type.getThemeType()) {
                        t = type;
                        LogUtil.v("Setting to " + t.getTitle());
                        break;
                    }
                }
                new ColorPreferences(context).setFontStyle(t);
                context.restartActivity();
            });

            new AlertDialog.Builder(context)
                    .setView(chooseaccentBinding.getRoot())
                    .show();
        });
    }

    void setupSettingsThemeBase() {
        final LinearLayout themeBase = (LinearLayout) context.findViewById(R.id.settings_theme_base);
        themeBase.setOnClickListener(v -> {
            final ChoosethemesmallBinding choosethemesmallBinding
                    = ChoosethemesmallBinding.inflate(context.getLayoutInflater());
            final View root = choosethemesmallBinding.getRoot();
            final TextView title = choosethemesmallBinding.title;

            title.setBackgroundColor(Palette.getDefaultColor());

            if (SettingValues.isNight()) {
                choosethemesmallBinding.nightmsg.setVisibility(View.VISIBLE);
            }

            for (final Pair<Integer, Integer> pair : ColorPreferences.themePairList) {
                root.findViewById(pair.first)
                        .setOnClickListener(v1 -> {
                            SettingsThemeFragment.changed = true;
                            final String[] names = new ColorPreferences(context).getFontStyle()
                                    .getTitle()
                                    .split("_");
                            final String name = names[names.length - 1];
                            final String newName = name.replace("(", "");
                            for (final ColorPreferences.Theme theme : ColorPreferences.Theme.values()) {
                                if (theme.toString().contains(newName)
                                        && theme.getThemeType() == pair.second) {
                                    back = theme.getThemeType();
                                    new ColorPreferences(context).setFontStyle(theme);
                                    context.restartActivity();
                                    break;
                                }
                            }
                        });
            }

            new AlertDialog.Builder(context)
                    .setView(root)
                    .show();
        });
    }

    private void setupSettingsThemeNight() {
        final LinearLayout nightMode = (LinearLayout) context.findViewById(R.id.settings_theme_night);
        nightMode.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if (SettingValues.isPro) {
                    final NightmodeBinding nightmodeBinding = NightmodeBinding.inflate(context.getLayoutInflater());

                    final View root = nightmodeBinding.getRoot();
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setView(root);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    dialog.setOnDismissListener(dialog1 -> {
                        //todo save
                    });
                    final Spinner startSpinner = nightmodeBinding.startSpinner;
                    final Spinner endSpinner = nightmodeBinding.endSpinner;
                    final AppCompatSpinner nightModeStateSpinner = nightmodeBinding.nightModeState;

                    nightModeStateSpinner.setAdapter(NightModeArrayAdapter.createFromResource(
                            dialog.getContext(), R.array.night_mode_state, android.R.layout.simple_spinner_dropdown_item));
                    nightModeStateSpinner.setSelection(SettingValues.nightModeState);
                    nightModeStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            startSpinner.setEnabled(position == SettingValues.NightModeState.MANUAL.ordinal());
                            endSpinner.setEnabled(position == SettingValues.NightModeState.MANUAL.ordinal());
                            SettingValues.nightModeState = position;
                            SettingValues.prefs.edit()
                                    .putInt(SettingValues.PREF_NIGHT_MODE_STATE, position)
                                    .apply();
                            SettingsThemeFragment.changed = true;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    for (final Pair<Integer, Integer> pair : ColorPreferences.themePairList) {
                        final RadioButton radioButton = root.findViewById(pair.first);
                        if (radioButton != null) {
                            if (SettingValues.nightTheme == pair.second) {
                                radioButton.setChecked(true);
                            }
                            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                if (isChecked) {
                                    SettingsThemeFragment.changed = true;
                                    SettingValues.nightTheme = pair.second;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_NIGHT_THEME,
                                                    pair.second)
                                            .apply();
                                }
                            });
                        }
                    }

                    boolean nightState = SettingValues.nightModeState == SettingValues.NightModeState.MANUAL.ordinal();
                    startSpinner.setEnabled(nightState);
                    endSpinner.setEnabled(nightState);
                    final List<String> timesStart = new ArrayList<String>() {{
                        add("6pm");
                        add("7pm");
                        add("8pm");
                        add("9pm");
                        add("10pm");
                        add("11pm");
                    }};
                    nightmodeBinding.startSpinnerLayout.setVisibility(View.VISIBLE);
                    final ArrayAdapter<String> startAdapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, timesStart);
                    startAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    startSpinner.setAdapter(startAdapter);

                    //set the currently selected pref
                    startSpinner.setSelection(startAdapter.getPosition(
                            SettingValues.nightStart + "pm"));

                    startSpinner.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view,
                                                           int position, long id) {
                                    //get the time, but remove the "pm" from the string when parsing
                                    final int time = Integer.parseInt(
                                            ((String) startSpinner.getItemAtPosition(
                                                    position)).replaceAll("pm", ""));

                                    SettingValues.nightStart = time;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_NIGHT_START, time)
                                            .apply();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });


                    final List<String> timesEnd = new ArrayList<String>() {{
                        add("12am");
                        add("1am");
                        add("2am");
                        add("3am");
                        add("4am");
                        add("5am");
                        add("6am");
                        add("7am");
                        add("8am");
                        add("9am");
                        add("10am");
                    }};
                    nightmodeBinding.endSpinnerLayout.setVisibility(View.VISIBLE);
                    final ArrayAdapter<String> endAdapter = new ArrayAdapter<>(context,
                            android.R.layout.simple_spinner_item, timesEnd);
                    endAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    endSpinner.setAdapter(endAdapter);

                    //set the currently selected pref
                    endSpinner.setSelection(endAdapter.getPosition(
                            SettingValues.nightEnd + "am"));

                    endSpinner.setOnItemSelectedListener(
                            new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view,
                                                           int position, long id) {
                                    //get the time, but remove the "am" from the string when parsing
                                    final int time = Integer.parseInt(
                                            ((String) endSpinner.getItemAtPosition(
                                                    position)).replaceAll("am", ""));

                                    SettingValues.nightEnd = time;
                                    SettingValues.prefs.edit()
                                            .putInt(SettingValues.PREF_NIGHT_END, time)
                                            .apply();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });

                    nightmodeBinding.ok.setOnClickListener(v ->
                            dialog.dismiss());

                } else {
                    ProUtil.proUpgradeMsg(context, R.string.general_nighttheme_ispro)
                            .setNegativeButton(R.string.btn_no_thanks, (dialog, whichButton) ->
                                    dialog.dismiss())
                            .show();
                }
            }
        });
    }

    private void setComponentState(final String cls, final int componentEnabledState) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, cls),
                componentEnabledState,
                PackageManager.DONT_KILL_APP);
    }

    private void setTintingMode(boolean colorBack, boolean subName) {
        SettingValues.colorBack = colorBack;
        SettingValues.colorSubName = subName;
        editSharedBooleanPreference(SettingValues.PREF_COLOR_BACK, colorBack);
        editSharedBooleanPreference(SettingValues.PREF_COLOR_SUB_NAME, subName);
    }

    private void editSharedBooleanPreference(final String settingValueString, final boolean isChecked) {
        SettingValues.prefs.edit().putBoolean(settingValueString, isChecked).apply();
    }

    private static class NightModeArrayAdapter {
        public static ArrayAdapter<? extends CharSequence> createFromResource(@NonNull Context context,
                                                                              @ArrayRes int textArrayResId,
                                                                              @LayoutRes int layoutTypeResId) {
            CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
            if (!Reddit.canUseNightModeAuto) {
                strings = ArrayUtils.remove(strings, SettingValues.NightModeState.AUTOMATIC.ordinal());
            }
            return new ArrayAdapter<>(context, layoutTypeResId, Arrays.asList(strings));
        }
    }
}
