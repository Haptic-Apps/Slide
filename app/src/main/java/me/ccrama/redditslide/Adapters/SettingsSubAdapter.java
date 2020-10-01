package me.ccrama.redditslide.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.MainActivity;
import me.ccrama.redditslide.Activities.SettingsSubreddit;
import me.ccrama.redditslide.Activities.SubredditView;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;


/**
 * Created by ccrama on 8/17/2015.
 */
public class SettingsSubAdapter extends RecyclerView.Adapter<SettingsSubAdapter.ViewHolder> {
    private final ArrayList<String> objects;

    private Activity context;

    public SettingsSubAdapter(Activity context, ArrayList<String> objects) {
        this.objects = objects;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublisteditor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View convertView = holder.itemView;
        final TextView t = convertView.findViewById(R.id.name);
        t.setText(objects.get(position));

        final String subreddit = objects.get(position);
        convertView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
        convertView.findViewById(R.id.color).getBackground().setColorFilter(new PorterDuffColorFilter(Palette.getColor(subreddit), PorterDuff.Mode.MULTIPLY));

        final String DELETE_SUB_SETTINGS_TITLE = (subreddit.contains("/m/")) ? subreddit : ("/r/" + subreddit);
        convertView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(context).setTitle(context.getString(R.string.settings_delete_sub_settings, DELETE_SUB_SETTINGS_TITLE))
                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Palette.removeColor(subreddit);
                                // Remove layout settings
                                SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + subreddit).apply();
                                // Remove accent / font color settings
                                new ColorPreferences(context).removeFontStyle(subreddit);

                                SettingValues.resetPicsEnabled(subreddit);
                                SettingValues.resetSelftextEnabled(subreddit);

                                dialog.dismiss();
                                objects.remove(subreddit);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
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
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
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
        if (subreddits.isEmpty()) {
            return;
        }

        final boolean multipleSubs = (subreddits.size() > 1);
        boolean isAlternateLayout;
        int currentColor;
        int currentAccentColor;

        final ColorPreferences colorPrefs = new ColorPreferences(context);
        final String subreddit = multipleSubs ? null : subreddits.get(0);
        final SwitchCompat bigPics = dialoglayout.findViewById(R.id.bigpics);
        final SwitchCompat selftext = dialoglayout.findViewById(R.id.selftext);

        //Selected multiple subreddits
        if (multipleSubs) {
            //Check if all selected subs have the same settings
            int previousSubColor = 0;
            int previousSubAccent = 0;
            bigPics.setChecked(SettingValues.bigPicEnabled);
            selftext.setChecked(SettingValues.cardText);
            boolean sameMainColor = true;
            boolean sameAccentColor = true;

            for (String sub : subreddits) {
                int currentSubColor = Palette.getColor(sub);
                int currentSubAccent = colorPrefs.getColor("");

                if (previousSubColor != 0 && previousSubAccent != 0) {
                    if (currentSubColor != previousSubColor) {
                        sameMainColor = false;
                    } else if (currentSubAccent != previousSubAccent) {
                        sameAccentColor = false;
                    }
                }
                if (!sameMainColor && !sameAccentColor) {
                    break;
                }

                previousSubAccent = currentSubAccent;
                previousSubColor = currentSubColor;
            }

            currentColor = Palette.getDefaultColor();
            currentAccentColor = colorPrefs.getColor("");
            isAlternateLayout = false;

            //If all selected subs have the same settings, display them
            if (sameMainColor) {
                currentColor = previousSubColor;
            }
            if (sameAccentColor) {
                currentAccentColor = previousSubAccent;
            }
        } else {  //Is only one selected sub
            currentColor = Palette.getColor(subreddit);
            isAlternateLayout = SettingValues.prefs.contains(Reddit.PREF_LAYOUT + subreddit);
            currentAccentColor = colorPrefs.getColor(subreddit);
            bigPics.setChecked(SettingValues.isPicsEnabled(subreddit));
            selftext.setChecked(SettingValues.isSelftextEnabled(subreddit));

        }

        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);

        final TextView title = dialoglayout.findViewById(R.id.title);
        title.setBackgroundColor(currentColor);

        if (multipleSubs) {

            StringBuilder titleStringBuilder = new StringBuilder();
            for (String sub : subreddits) {
                //if the subreddit is the frontpage, don't put "/r/" in front of it
                if (sub.equals("frontpage")) {
                    titleStringBuilder.append(sub).append(", ");
                } else {
                    if (sub.contains("/m/")) {
                        titleStringBuilder.append(sub).append(", ");
                    } else {
                        titleStringBuilder.append("/r/").append(sub).append(", ");
                    }
                }
            }
            String titleString = titleStringBuilder.toString();
            titleString = titleString.substring(0, titleString.length() - 2);
            title.setMaxLines(3);
            title.setText(titleString);
        } else {
            if (subreddit.contains("/m/")) {
                title.setText(subreddit);
            } else {
                //if the subreddit is the frontpage, don't put "/r/" in front of it
                title.setText(((subreddit.equals("frontpage")) ? "frontpage" : "/r/" + subreddit));
            }
        }

        {
            //Primary color pickers
            final LineColorPicker colorPickerPrimary = dialoglayout.findViewById(R.id.picker);
            //shades of primary colors
            final LineColorPicker colorPickerPrimaryShades =
                    dialoglayout.findViewById(R.id.picker2);

            colorPickerPrimary.setColors(ColorPreferences.getBaseColors(context));

            //Iterate through all colors and check if it matches the current color of the sub, then select it
            for (int i : colorPickerPrimary.getColors()) {
                for (int i2 : ColorPreferences.getColors(context, i)) {
                    if (i2 == currentColor) {
                        colorPickerPrimary.setSelectedColor(i);
                        colorPickerPrimaryShades.setColors(ColorPreferences.getColors(context, i));
                        colorPickerPrimaryShades.setSelectedColor(i2);
                        break;
                    }
                }
            }

            //Base color changed
            colorPickerPrimary.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int c) {
                    //Show variations of the base color
                    colorPickerPrimaryShades.setColors(ColorPreferences.getColors(context, c));
                    colorPickerPrimaryShades.setSelectedColor(c);
                }
            });
            colorPickerPrimaryShades.setOnColorChangedListener(new OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateColor(colorPickerPrimaryShades.getColor(), subreddit);
                    }
                    title.setBackgroundColor(colorPickerPrimaryShades.getColor());
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

            //Accent color picker
            final LineColorPicker colorPickerAcc = dialoglayout.findViewById(R.id.picker3);

            {
                //Get all possible accent colors (for day theme)
                int[] arrs = new int[ColorPreferences.getNumColorsFromThemeType(0)];
                int i = 0;
                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                    if (type.getThemeType() == ColorPreferences.ColorThemeOptions.Dark.getValue()) {
                        arrs[i] = ContextCompat.getColor(context, type.getColor());
                        i++;
                    }
                    colorPickerAcc.setColors(arrs);
                    colorPickerAcc.setSelectedColor(currentAccentColor);
                }
            }

            builder.setView(dialoglayout);
            builder.setCancelable(false);
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).updateColor(Palette.getColor(subreddit), subreddit);
                    }
                }
            }).setNeutralButton(R.string.btn_reset, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String subTitles;

                    if (multipleSubs) {
                        StringBuilder subTitlesBuilder = new StringBuilder();
                        for (String sub : subreddits) {
                            //if the subreddit is the frontpage, don't put "/r/" in front of it
                            if (sub.equals("frontpage")) {
                                subTitlesBuilder.append(sub).append(", ");
                            } else {
                                subTitlesBuilder.append("/r/").append(sub).append(", ");
                            }
                        }
                        subTitles = subTitlesBuilder.toString();
                        subTitles = subTitles.substring(0, subTitles.length() - 2);
                    } else {
                        //if the subreddit is the frontpage, don't put "/r/" in front of it
                        subTitles = (subreddit.equals("frontpage") ? "frontpage" : "/r/" + subreddit);
                    }
                    String titleStart = context.getString(R.string.settings_delete_sub_settings, subTitles);
                    titleStart = titleStart.replace("/r//r/", "/r/");
                    if (titleStart.contains("/r/frontpage")) {
                        titleStart = titleStart.replace("/r/frontpage", "frontpage");
                    }
                    new AlertDialogWrapper.Builder(context).setTitle(titleStart)
                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (String sub : subreddits) {
                                        Palette.removeColor(sub);
                                        // Remove layout settings
                                        SettingValues.prefs.edit().remove(Reddit.PREF_LAYOUT + sub).apply();
                                        // Remove accent / font color settings
                                        new ColorPreferences(context).removeFontStyle(sub);

                                        SettingValues.resetPicsEnabled(sub);
                                        SettingValues.resetSelftextEnabled(sub);
                                    }

                                    if (context instanceof MainActivity) {
                                        ((MainActivity) context).reloadSubs();
                                    } else if (context instanceof SettingsSubreddit) {
                                        ((SettingsSubreddit) context).reloadSubList();
                                    } else if (context instanceof SubredditView) {
                                        ((SubredditView) context).restartTheme();
                                    }
                                }
                            }).setNegativeButton(R.string.btn_no, null).show();
                }
            }).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final int newPrimaryColor = colorPickerPrimaryShades.getColor();
                    final int newAccentColor = colorPickerAcc.getColor();

                    for (String sub : subreddits) {
                        // Set main color
                        if (bigPics.isChecked() != SettingValues.isPicsEnabled(sub)) {
                            SettingValues.setPicsEnabled(sub, bigPics.isChecked());
                        }
                        if (selftext.isChecked() != SettingValues.isSelftextEnabled(sub)) {
                            SettingValues.setSelftextEnabled(sub, selftext.isChecked());
                        }
                        //Only do set colors if either subreddit theme color has changed
                        if (Palette.getColor(sub) != newPrimaryColor || Palette.getDarkerColor(sub) != newAccentColor) {

                            if (newPrimaryColor != Palette.getDefaultColor()) {
                                Palette.setColor(sub, newPrimaryColor);
                            } else {
                                Palette.removeColor(sub);
                            }

                            // Set accent color
                            ColorPreferences.Theme t = null;

                            //Do not save accent color if it matches the default accent color
                            if (newAccentColor != ContextCompat.getColor(context, colorPrefs.getFontStyle().getColor()) || newAccentColor != ContextCompat.getColor(context, colorPrefs.getFontStyleSubreddit(sub).getColor())) {
                                LogUtil.v("Accent colors not equal");
                                int back = new ColorPreferences(context).getFontStyle().getThemeType();
                                for (ColorPreferences.Theme type : ColorPreferences.Theme.values()) {
                                    if (ContextCompat.getColor(context, type.getColor()) == newAccentColor && back == type.getThemeType()) {
                                        t = type;
                                        LogUtil.v("Setting accent color to " + t.getTitle());
                                        break;
                                    }
                                }
                            } else {
                                new ColorPreferences(context).removeFontStyle(sub);
                            }

                            if (t != null) {
                                colorPrefs.setFontStyle(t, sub);
                            }
                        }

                        // Set layout
                        SettingValues.prefs.edit().putBoolean(Reddit.PREF_LAYOUT + sub, true).apply();
                    }

                    //Only refresh stuff if the user changed something
                    if (Palette.getColor(subreddit) != newPrimaryColor || Palette.getDarkerColor(subreddit) != newAccentColor) {
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).reloadSubs();
                        } else if (context instanceof SettingsSubreddit) {
                            ((SettingsSubreddit) context).reloadSubList();
                        } else if (context instanceof SubredditView) {
                            ((SubredditView) context).restartTheme();
                        }
                    }
                }
            }).show();
        }
    }

    public void prepareAndShowSubEditor(ArrayList<String> subreddits) {
        if (subreddits.size() == 1) prepareAndShowSubEditor(subreddits.get(0));
        else if (subreddits.size() > 1) {
            LayoutInflater localInflater = context.getLayoutInflater();
            final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);
            showSubThemeEditor(subreddits, context, dialoglayout);
        }
    }

    private void prepareAndShowSubEditor(String subreddit) {
        int style = new ColorPreferences(context).getThemeSubreddit(subreddit);
        final Context contextThemeWrapper = new ContextThemeWrapper(context, style);
        LayoutInflater localInflater = context.getLayoutInflater().cloneInContext(contextThemeWrapper);
        final View dialoglayout = localInflater.inflate(R.layout.colorsub, null);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(subreddit);
        showSubThemeEditor(arrayList, context, dialoglayout);
    }

}
