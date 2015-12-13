package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 8/17/2015.
 */
public class SettingsSubAdapter extends ArrayAdapter<String> {
    private final ArrayList<String> objects;
    private String subToDisplay;


    public SettingsSubAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);
        this.objects = objects;
    }

    public SettingsSubAdapter(Context context, ArrayList<String> objects, String subToDisplay) {
        super(context, 0, objects);
        this.objects = objects;
        this.subToDisplay = subToDisplay;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subforsublisteditor, parent, false);
        }
        final TextView t =
                ((TextView) convertView.findViewById(R.id.name));
        t.setText(objects.get(position));

        final String subreddit = objects.get(position);
        convertView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
        convertView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY);

        convertView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(getContext()).setTitle(getContext().getString(R.string.settings_delete_sub_settings, subreddit))
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Palette.removeColor(subreddit);
                                SettingValues.prefs.edit().remove("PRESET" + subreddit).apply();
                                dialog.dismiss();
                                objects.remove(subreddit);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });
        if (subToDisplay != null) showSubEditor(subToDisplay);
        subToDisplay = null;
        convertView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubEditor(subreddit);
            }
        });
        return convertView;
    }

    private void showSubEditor(final String subreddit) {
        int style = new ColorPreferences(getContext()).getThemeSubreddit(subreddit);
        final Context contextThemeWrapper = new ContextThemeWrapper(getContext(), style);
        LayoutInflater localInflater = ((Activity) getContext()).getLayoutInflater().cloneInContext(contextThemeWrapper);
        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(getContext());
        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
        title.setText("/r/" + subreddit);
        title.setBackgroundColor(Palette.getColor(subreddit));

        {

            LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
            final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

            colorPicker.setColors(new int[]{
                    getContext().getResources().getColor(R.color.md_red_500),
                    getContext().getResources().getColor(R.color.md_pink_500),
                    getContext().getResources().getColor(R.color.md_purple_500),
                    getContext().getResources().getColor(R.color.md_deep_purple_500),
                    getContext().getResources().getColor(R.color.md_indigo_500),
                    getContext().getResources().getColor(R.color.md_blue_500),
                    getContext().getResources().getColor(R.color.md_light_blue_500),
                    getContext().getResources().getColor(R.color.md_cyan_500),
                    getContext().getResources().getColor(R.color.md_teal_500),
                    getContext().getResources().getColor(R.color.md_green_500),
                    getContext().getResources().getColor(R.color.md_light_green_500),
                    getContext().getResources().getColor(R.color.md_lime_500),
                    getContext().getResources().getColor(R.color.md_yellow_500),
                    getContext().getResources().getColor(R.color.md_amber_500),
                    getContext().getResources().getColor(R.color.md_orange_500),
                    getContext().getResources().getColor(R.color.md_deep_orange_500),
                    getContext().getResources().getColor(R.color.md_brown_500),
                    getContext().getResources().getColor(R.color.md_grey_500),
                    getContext().getResources().getColor(R.color.md_blue_grey_500),

            });
            int currentColor = Palette.getColor(subreddit);
            for (int i : colorPicker.getColors()) {
                for (int i2 : getColors(i)) {
                    if (i2 == currentColor) {
                        colorPicker.setSelectedColor(i);
                        colorPicker2.setColors(getColors(i));
                        colorPicker2.setSelectedColor(i2);
                        break;
                    }
                }
            }
            colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int c) {

                    colorPicker2.setColors(getColors(c));
                    colorPicker2.setSelectedColor(c);


                }
            });
            colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {

                    title.setBackgroundColor(colorPicker2.getColor());
                }
            });
            final LineColorPicker colorPickeracc = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);


            {
                         /* TODO   TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.reset);

                            // if button is clicked, close the custom dialog
                            dialogButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Palette.removeColor(subreddit);
                                    hea.setBackgroundColor(Palette.getDefaultColor());
                                    findViewById(R.id.header).setBackgroundColor(Palette.getDefaultColor());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        Window window = getWindow();
                                        window.setStatusBarColor(Palette.getDarkerColor(Palette.getDefaultColor()));
                                        MainActivity.this.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

                                    }
                                    title.setBackgroundColor(Palette.getDefaultColor());


                                    int cx = center.getWidth() / 2;
                                    int cy = center.getHeight() / 2;

                                    int initialRadius = body.getWidth();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                        Animator anim =
                                                ViewAnimationUtils.createCircularReveal(body, cx, cy, initialRadius, 0);

                                        anim.addListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                body.setVisibility(View.GONE);
                                            }
                                        });
                                        anim.start();

                                    } else {
                                        body.setVisibility(View.GONE);

                                    }

                                }
                            });*/


            }
            final RadioButton def = (RadioButton) dialoglayout.findViewById(R.id.def);
            final RadioButton alt = (RadioButton) dialoglayout.findViewById(R.id.alt);


            {


                int[] arrs = new int[ColorPreferences.Theme.values().length / 3];
                int i = 0;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType() == 0) {
                        arrs[i] = getContext().getResources().getColor(type.getColor());

                        i++;
                    }
                }

                colorPickeracc.setColors(arrs);

                int topick = new ColorPreferences(getContext()).getFontStyleSubreddit(subreddit).getColor();
                for (int color : arrs) {
                    if (color == topick) {
                        colorPickeracc.setSelectedColorPosition(color);
                        break;

                    }
                }

            }


            int i = (SettingValues.prefs.contains("PRESET" + subreddit) ? 1 : 0);
            if (i == 0) {
                def.setChecked(true);
            } else {
                alt.setChecked(true);
            }


            def.setText(R.string.settings_layout_default);
            alt.setText(R.string.settings_title_alternative_layout);


            builder.setView(dialoglayout);
            final Dialog d = builder.show();
            {
                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Palette.setColor(subreddit, colorPicker2.getColor());
                        int color = colorPickeracc.getColor();
                        ColorPreferences.Theme t = null;
                        for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                            if (getContext().getResources().getColor(type.getColor()) == color && Reddit.themeBack == type.getThemeType()) {
                                t = type;
                                break;
                            }
                        }

                        new ColorPreferences(getContext()).setFontStyle(t, subreddit);


                        if (alt.isChecked()) {
                            SettingValues.prefs.edit().putBoolean("PRESET" + subreddit, true).apply();
                        } else {
                            SettingValues.prefs.edit().remove("PRESET" + subreddit).apply();
                        }
                        notifyDataSetChanged();
                        d.dismiss();

                    }
                });


            }
            {
                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.cancel);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        d.dismiss();


                    }
                });
            }
        }
    }

    private int[] getColors(int c) {
        if (c == getContext().getResources().getColor(R.color.md_red_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_red_100),
                    getContext().getResources().getColor(R.color.md_red_200),
                    getContext().getResources().getColor(R.color.md_red_300),
                    getContext().getResources().getColor(R.color.md_red_400),
                    getContext().getResources().getColor(R.color.md_red_500),
                    getContext().getResources().getColor(R.color.md_red_600),
                    getContext().getResources().getColor(R.color.md_red_700),
                    getContext().getResources().getColor(R.color.md_red_800),
                    getContext().getResources().getColor(R.color.md_red_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_pink_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_pink_100),
                    getContext().getResources().getColor(R.color.md_pink_200),
                    getContext().getResources().getColor(R.color.md_pink_300),
                    getContext().getResources().getColor(R.color.md_pink_400),
                    getContext().getResources().getColor(R.color.md_pink_500),
                    getContext().getResources().getColor(R.color.md_pink_600),
                    getContext().getResources().getColor(R.color.md_pink_700),
                    getContext().getResources().getColor(R.color.md_pink_800),
                    getContext().getResources().getColor(R.color.md_pink_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_purple_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_purple_100),
                    getContext().getResources().getColor(R.color.md_purple_200),
                    getContext().getResources().getColor(R.color.md_purple_300),
                    getContext().getResources().getColor(R.color.md_purple_400),
                    getContext().getResources().getColor(R.color.md_purple_500),
                    getContext().getResources().getColor(R.color.md_purple_600),
                    getContext().getResources().getColor(R.color.md_purple_700),
                    getContext().getResources().getColor(R.color.md_purple_800),
                    getContext().getResources().getColor(R.color.md_purple_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_deep_purple_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_deep_purple_100),
                    getContext().getResources().getColor(R.color.md_deep_purple_200),
                    getContext().getResources().getColor(R.color.md_deep_purple_300),
                    getContext().getResources().getColor(R.color.md_deep_purple_400),
                    getContext().getResources().getColor(R.color.md_deep_purple_500),
                    getContext().getResources().getColor(R.color.md_deep_purple_600),
                    getContext().getResources().getColor(R.color.md_deep_purple_700),
                    getContext().getResources().getColor(R.color.md_deep_purple_800),
                    getContext().getResources().getColor(R.color.md_deep_purple_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_indigo_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_indigo_100),
                    getContext().getResources().getColor(R.color.md_indigo_200),
                    getContext().getResources().getColor(R.color.md_indigo_300),
                    getContext().getResources().getColor(R.color.md_indigo_400),
                    getContext().getResources().getColor(R.color.md_indigo_500),
                    getContext().getResources().getColor(R.color.md_indigo_600),
                    getContext().getResources().getColor(R.color.md_indigo_700),
                    getContext().getResources().getColor(R.color.md_indigo_800),
                    getContext().getResources().getColor(R.color.md_indigo_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_blue_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_blue_100),
                    getContext().getResources().getColor(R.color.md_blue_200),
                    getContext().getResources().getColor(R.color.md_blue_300),
                    getContext().getResources().getColor(R.color.md_blue_400),
                    getContext().getResources().getColor(R.color.md_blue_500),
                    getContext().getResources().getColor(R.color.md_blue_600),
                    getContext().getResources().getColor(R.color.md_blue_700),
                    getContext().getResources().getColor(R.color.md_blue_800),
                    getContext().getResources().getColor(R.color.md_blue_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_light_blue_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_light_blue_100),
                    getContext().getResources().getColor(R.color.md_light_blue_200),
                    getContext().getResources().getColor(R.color.md_light_blue_300),
                    getContext().getResources().getColor(R.color.md_light_blue_400),
                    getContext().getResources().getColor(R.color.md_light_blue_500),
                    getContext().getResources().getColor(R.color.md_light_blue_600),
                    getContext().getResources().getColor(R.color.md_light_blue_700),
                    getContext().getResources().getColor(R.color.md_light_blue_800),
                    getContext().getResources().getColor(R.color.md_light_blue_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_cyan_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_cyan_100),
                    getContext().getResources().getColor(R.color.md_cyan_200),
                    getContext().getResources().getColor(R.color.md_cyan_300),
                    getContext().getResources().getColor(R.color.md_cyan_400),
                    getContext().getResources().getColor(R.color.md_cyan_500),
                    getContext().getResources().getColor(R.color.md_cyan_600),
                    getContext().getResources().getColor(R.color.md_cyan_700),
                    getContext().getResources().getColor(R.color.md_cyan_800),
                    getContext().getResources().getColor(R.color.md_cyan_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_teal_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_teal_100),
                    getContext().getResources().getColor(R.color.md_teal_200),
                    getContext().getResources().getColor(R.color.md_teal_300),
                    getContext().getResources().getColor(R.color.md_teal_400),
                    getContext().getResources().getColor(R.color.md_teal_500),
                    getContext().getResources().getColor(R.color.md_teal_600),
                    getContext().getResources().getColor(R.color.md_teal_700),
                    getContext().getResources().getColor(R.color.md_teal_800),
                    getContext().getResources().getColor(R.color.md_teal_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_green_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_green_100),
                    getContext().getResources().getColor(R.color.md_green_200),
                    getContext().getResources().getColor(R.color.md_green_300),
                    getContext().getResources().getColor(R.color.md_green_400),
                    getContext().getResources().getColor(R.color.md_green_500),
                    getContext().getResources().getColor(R.color.md_green_600),
                    getContext().getResources().getColor(R.color.md_green_700),
                    getContext().getResources().getColor(R.color.md_green_800),
                    getContext().getResources().getColor(R.color.md_green_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_light_green_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_light_green_100),
                    getContext().getResources().getColor(R.color.md_light_green_200),
                    getContext().getResources().getColor(R.color.md_light_green_300),
                    getContext().getResources().getColor(R.color.md_light_green_400),
                    getContext().getResources().getColor(R.color.md_light_green_500),
                    getContext().getResources().getColor(R.color.md_light_green_600),
                    getContext().getResources().getColor(R.color.md_light_green_700),
                    getContext().getResources().getColor(R.color.md_light_green_800),
                    getContext().getResources().getColor(R.color.md_light_green_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_lime_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_lime_100),
                    getContext().getResources().getColor(R.color.md_lime_200),
                    getContext().getResources().getColor(R.color.md_lime_300),
                    getContext().getResources().getColor(R.color.md_lime_400),
                    getContext().getResources().getColor(R.color.md_lime_500),
                    getContext().getResources().getColor(R.color.md_lime_600),
                    getContext().getResources().getColor(R.color.md_lime_700),
                    getContext().getResources().getColor(R.color.md_lime_800),
                    getContext().getResources().getColor(R.color.md_lime_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_yellow_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_yellow_100),
                    getContext().getResources().getColor(R.color.md_yellow_200),
                    getContext().getResources().getColor(R.color.md_yellow_300),
                    getContext().getResources().getColor(R.color.md_yellow_400),
                    getContext().getResources().getColor(R.color.md_yellow_500),
                    getContext().getResources().getColor(R.color.md_yellow_600),
                    getContext().getResources().getColor(R.color.md_yellow_700),
                    getContext().getResources().getColor(R.color.md_yellow_800),
                    getContext().getResources().getColor(R.color.md_yellow_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_amber_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_amber_100),
                    getContext().getResources().getColor(R.color.md_amber_200),
                    getContext().getResources().getColor(R.color.md_amber_300),
                    getContext().getResources().getColor(R.color.md_amber_400),
                    getContext().getResources().getColor(R.color.md_amber_500),
                    getContext().getResources().getColor(R.color.md_amber_600),
                    getContext().getResources().getColor(R.color.md_amber_700),
                    getContext().getResources().getColor(R.color.md_amber_800),
                    getContext().getResources().getColor(R.color.md_amber_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_orange_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_orange_100),
                    getContext().getResources().getColor(R.color.md_orange_200),
                    getContext().getResources().getColor(R.color.md_orange_300),
                    getContext().getResources().getColor(R.color.md_orange_400),
                    getContext().getResources().getColor(R.color.md_orange_500),
                    getContext().getResources().getColor(R.color.md_orange_600),
                    getContext().getResources().getColor(R.color.md_orange_700),
                    getContext().getResources().getColor(R.color.md_orange_800),
                    getContext().getResources().getColor(R.color.md_orange_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_deep_orange_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_deep_orange_100),
                    getContext().getResources().getColor(R.color.md_deep_orange_200),
                    getContext().getResources().getColor(R.color.md_deep_orange_300),
                    getContext().getResources().getColor(R.color.md_deep_orange_400),
                    getContext().getResources().getColor(R.color.md_deep_orange_500),
                    getContext().getResources().getColor(R.color.md_deep_orange_600),
                    getContext().getResources().getColor(R.color.md_deep_orange_700),
                    getContext().getResources().getColor(R.color.md_deep_orange_800),
                    getContext().getResources().getColor(R.color.md_deep_orange_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_brown_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_brown_100),
                    getContext().getResources().getColor(R.color.md_brown_200),
                    getContext().getResources().getColor(R.color.md_brown_300),
                    getContext().getResources().getColor(R.color.md_brown_400),
                    getContext().getResources().getColor(R.color.md_brown_500),
                    getContext().getResources().getColor(R.color.md_brown_600),
                    getContext().getResources().getColor(R.color.md_brown_700),
                    getContext().getResources().getColor(R.color.md_brown_800),
                    getContext().getResources().getColor(R.color.md_brown_900)
            };
        } else if (c == getContext().getResources().getColor(R.color.md_grey_500)) {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_grey_100),
                    getContext().getResources().getColor(R.color.md_grey_200),
                    getContext().getResources().getColor(R.color.md_grey_300),
                    getContext().getResources().getColor(R.color.md_grey_400),
                    getContext().getResources().getColor(R.color.md_grey_500),
                    getContext().getResources().getColor(R.color.md_grey_600),
                    getContext().getResources().getColor(R.color.md_grey_700),
                    getContext().getResources().getColor(R.color.md_grey_800),
                    getContext().getResources().getColor(R.color.md_grey_900)
            };
        } else {
            return new int[]{
                    getContext().getResources().getColor(R.color.md_blue_grey_100),
                    getContext().getResources().getColor(R.color.md_blue_grey_200),
                    getContext().getResources().getColor(R.color.md_blue_grey_300),
                    getContext().getResources().getColor(R.color.md_blue_grey_400),
                    getContext().getResources().getColor(R.color.md_blue_grey_500),
                    getContext().getResources().getColor(R.color.md_blue_grey_600),
                    getContext().getResources().getColor(R.color.md_blue_grey_700),
                    getContext().getResources().getColor(R.color.md_blue_grey_800),
                    getContext().getResources().getColor(R.color.md_blue_grey_900)
            };

        }
    }

}
