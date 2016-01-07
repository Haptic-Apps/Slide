package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SettingsSubreddit;
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


    public SettingsSubAdapter(Context context, ArrayList<String> objects) {
        super(context, 0, objects);
        this.objects = objects;
    }

    /**
     * Displays the subreddit color chooser
     * It is possible to color multiple subreddits at the same time
     *
     * @param subreddits   Subreddits as an array
     * @param context      Context for getting colors
     * @param dialoglayout The subchooser layout (R.layout.colorsub)
     */
    public static void showSubThemeEditor(final ArrayList<String> subreddits, final Activity context, View dialoglayout) {
        if (subreddits.size() == 0) return;

        boolean multipleSubs = subreddits.size() > 1;
        boolean isAlternateLayout;
        int currentColor;
        int currentAccentColor;
        final ColorPreferences colorPrefs = new ColorPreferences(context);

        final String subreddit = multipleSubs ? null : subreddits.get(0);

        if (multipleSubs) {
            //Check if all selected subs have the same settings
            int previousSubColor = 0;
            int previousSubAccent = 0;
            boolean sameMainColor = true;
            boolean sameAccentColor = true;
            for (String sub : subreddits) {
                int currentSubColor = Palette.getColor(sub);
                int currentSubAccent = colorPrefs.getColor("");

                if (previousSubColor != 0 && previousSubAccent != 0){
                    if (currentSubColor != previousSubColor) {
                        sameMainColor = false;
                    } else if (currentSubAccent != previousSubAccent){
                        sameAccentColor = false;
                    }
                }
                if (!sameMainColor && !sameAccentColor) break;

                previousSubAccent = currentSubAccent;
                previousSubColor = currentSubColor;
            }

            currentColor = Palette.getDefaultColor();
            currentAccentColor = colorPrefs.getColor("");
            isAlternateLayout = false;

            //If all selected subs have the same settings, display them
            if (sameMainColor)
                currentColor = previousSubColor;
            if (sameAccentColor)
                currentAccentColor = previousSubAccent;
        }
        //Is only one selected sub
        else {
            currentColor = Palette.getColor(subreddit);
            isAlternateLayout = SettingValues.prefs.contains(Reddit.PREF_LAYOUT + subreddit);
            currentAccentColor = colorPrefs.getColor(subreddit);
        }

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
        final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
        title.setBackgroundColor(currentColor);

        if (multipleSubs) {
            String titleString = "";
            for (String sub : subreddits) {
                titleString = titleString + "/r/" + sub + ", ";
            }
            titleString = titleString.substring(0, titleString.length() - 2);
            title.setMaxLines(3);
            title.setText(titleString);
        } else {
            title.setText("/r/" + subreddit);
        }


        {

            LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
            final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

            colorPicker.setColors(ColorPreferences.getBaseColors(context));

            //Iterate through all colors and check if it matches the current color of the sub, then select it
            for (int i : colorPicker.getColors()) {
                for (int i2 : ColorPreferences.getColors(context, i)) {
                    if (i2 == currentColor) {
                        colorPicker.setSelectedColor(i);
                        colorPicker2.setColors(ColorPreferences.getColors(context, i));
                        colorPicker2.setSelectedColor(i2);
                        break;
                    }
                }
            }

            //Base color changed
            colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int c) {
                    //Show variations of the base color
                    colorPicker2.setColors(ColorPreferences.getColors(context, c));
                    colorPicker2.setSelectedColor(c);


                }
            });
            colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {


                    if (context instanceof MainActivity)
                        ((MainActivity) context).updateColor(colorPicker2.getColor(), subreddit);


                    title.setBackgroundColor(colorPicker2.getColor());
                }
            });

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
                                        context.setTaskDescription(new ActivityManager.TaskDescription(subreddit, ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), colorPicker2.getColor()));

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

            final LineColorPicker colorPickeracc = (LineColorPicker) dialoglayout.findViewById(R.id.picker3);

            {
                //Get all possible accent colors (for day theme)
                int[] arrs = new int[ColorPreferences.Theme.values().length / 3];
                int i = 0;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType() == 0) {
                        arrs[i] = ContextCompat.getColor(context, type.getColor());

                        i++;
                    }

                    colorPickeracc.setColors(arrs);
                    colorPickeracc.setSelectedColor(currentAccentColor);

                }
            }




            builder.setView(dialoglayout);
            final Dialog d = builder.show();
            {
                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.cancel);

                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                    }
                });
            }
            {
                TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int accentColor = colorPickeracc.getColor();
                        int mainColor = colorPicker2.getColor();
                        ColorPreferences.Theme t = null;

                        //Do not save accent color if it matches the default accent color
                        if (accentColor != colorPrefs.getColor("")) {
                            for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                if (ContextCompat.getColor(context, type.getColor()) == accentColor && Reddit.themeBack == type.getThemeType()) {
                                    t = type;
                                    break;
                                }
                            }
                        }


                        for (String sub : subreddits) {
                            // Set main color
                            if (mainColor != Palette.getDefaultColor())
                                Palette.setColor(sub, mainColor);
                            // Set accent color
                            if (t != null) colorPrefs.setFontStyle(t, sub);

                            // Set layout

                                SettingValues.prefs.edit().putBoolean(Reddit.PREF_LAYOUT + sub, true).apply();

                        }
                        if (context instanceof MainActivity)
                            ((MainActivity) context).reloadSubs();
                        else if (context instanceof SettingsSubreddit) {
                            ((SettingsSubreddit) context).reloadSubList();
                        }
                        d.dismiss();

                    }
                });


            }
        }
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
                                // Remove layout settings
                                SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + subreddit).apply();
                                // Remove accent / font color settings
                                new ColorPreferences(getContext()).removeFontStyle(subreddit);

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
        convertView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareAndShowSubEditor(subreddit);
            }
        });
        return convertView;
    }

    public void prepareAndShowSubEditor(ArrayList<String> subreddits) {
        if (subreddits.size() == 1) prepareAndShowSubEditor(subreddits.get(0));
        else if (subreddits.size() > 1) {
            LayoutInflater localInflater = ((Activity) getContext()).getLayoutInflater();
            final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
            showSubThemeEditor(subreddits, ((Activity) getContext()), dialoglayout);
        }
    }

    private void prepareAndShowSubEditor(String subreddit) {
        int style = new ColorPreferences(getContext()).getThemeSubreddit(subreddit);
        final Context contextThemeWrapper = new ContextThemeWrapper(getContext(), style);
        LayoutInflater localInflater = ((Activity) getContext()).getLayoutInflater().cloneInContext(contextThemeWrapper);
        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(subreddit);
        showSubThemeEditor(arrayList, ((Activity) getContext()), dialoglayout);
    }

}
