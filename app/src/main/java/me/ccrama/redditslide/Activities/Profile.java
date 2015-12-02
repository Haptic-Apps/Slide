package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Account;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.ContributionsView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Visuals.Palette;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Profile extends BaseActivityAnim {

    private String name;
    private Account account;
    private ViewPager pager;
    private TabLayout tabs;
    private String[] usedArray;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        name = getIntent().getExtras().getString("profile", "");
        applyColorTheme();
        setContentView(R.layout.activity_multireddits);
        setupUserAppBar(R.id.toolbar, name, true, name);

        findViewById(R.id.header).setBackgroundColor(Palette.getColorUser(name));
        findViewById(R.id.sorting).setVisibility(View.GONE);
        findViewById(R.id.edit).setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.create)).setImageDrawable(getResources().getDrawable(R.drawable.infonew));

        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        pager = (ViewPager) findViewById(R.id.content_view);

        setDataSet(new String[]{getString(R.string.profile_overview),
                getString(R.string.profile_comments),
                getString(R.string.profile_submitted),
                getString(R.string.profile_gilded)});


        new getProfile().execute(name);

    }

    private void doClick() {
        if (account == null) {
            new AlertDialogWrapper.Builder(Profile.this)
                    .setTitle(R.string.profile_err_title)
                    .setMessage(R.string.profile_err_msg)
                    .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    onBackPressed();
                }
            }).show();
            return;
        }
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.colorprofile, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Profile.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(name);
                title.setBackgroundColor(Palette.getColorUser(name));

                if (Authentication.isLoggedIn) {
                    dialoglayout.findViewById(R.id.pm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Profile.this, Sendmessage.class);
                            i.putExtra("name", name);
                            startActivity(i);
                        }
                    });
                } else {
                    dialoglayout.findViewById(R.id.pm).setVisibility(View.GONE);
                }


                final View body = dialoglayout.findViewById(R.id.body2);
                body.setVisibility(View.INVISIBLE);
                final View center = dialoglayout.findViewById(R.id.colorExpandFrom);
                dialoglayout.findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int cx = center.getWidth() / 2;
                        int cy = center.getHeight() / 2;

                        int finalRadius = Math.max(body.getWidth(), body.getHeight());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Animator anim =
                                    ViewAnimationUtils.createCircularReveal(body, cx, cy, 0, finalRadius);
                            body.setVisibility(View.VISIBLE);
                            anim.start();
                        } else {
                            body.setVisibility(View.VISIBLE);
                        }
                    }
                });

                LineColorPicker colorPicker = (LineColorPicker) dialoglayout.findViewById(R.id.picker);
                final LineColorPicker colorPicker2 = (LineColorPicker) dialoglayout.findViewById(R.id.picker2);

                colorPicker.setColors(new int[]{
                        getResources().getColor(R.color.md_red_500),
                        getResources().getColor(R.color.md_pink_500),
                        getResources().getColor(R.color.md_purple_500),
                        getResources().getColor(R.color.md_deep_purple_500),
                        getResources().getColor(R.color.md_indigo_500),
                        getResources().getColor(R.color.md_blue_500),
                        getResources().getColor(R.color.md_light_blue_500),
                        getResources().getColor(R.color.md_cyan_500),
                        getResources().getColor(R.color.md_teal_500),
                        getResources().getColor(R.color.md_green_500),
                        getResources().getColor(R.color.md_light_green_500),
                        getResources().getColor(R.color.md_lime_500),
                        getResources().getColor(R.color.md_yellow_500),
                        getResources().getColor(R.color.md_amber_500),
                        getResources().getColor(R.color.md_orange_500),
                        getResources().getColor(R.color.md_deep_orange_500),
                        getResources().getColor(R.color.md_brown_500),
                        getResources().getColor(R.color.md_grey_500),
                        getResources().getColor(R.color.md_blue_grey_500),

                });

                colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int c) {

                        colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), c));
                        colorPicker2.setSelectedColor(c);


                    }
                });

                int currentColor = Palette.getColorUser(name);
                for (int i : colorPicker.getColors()) {
                    for (int i2 : ColorPreferences.getColors(getBaseContext(), i)) {
                        if (i2 == currentColor) {
                            colorPicker.setSelectedColor(i);
                            colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), i));
                            colorPicker2.setSelectedColor(i2);
                            break;
                        }
                    }
                }
                colorPicker2.setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        findViewById(R.id.header).setBackgroundColor(colorPicker2.getColor());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Palette.getDarkerColor(colorPicker2.getColor()));
                        }
                        title.setBackgroundColor(colorPicker2.getColor());
                    }
                });


                {
                    TextView dialogButton = (TextView) dialoglayout.findViewById(R.id.ok);

                    // if button is clicked, close the custom dialog
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Palette.setColorUser(name, colorPicker2.getColor());

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
                    });


                }
                ((TextView) dialoglayout.findViewById(R.id.commentkarma)).setText(account.getCommentKarma() + "");
                ((TextView) dialoglayout.findViewById(R.id.linkkarma)).setText(account.getLinkKarma() + "");

                builder.setView(dialoglayout);
                builder.show();
            }
        });
    }


    private void setDataSet(String[] data) {
        usedArray = data;
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        tabs.setupWithViewPager(pager);


    }

    public boolean isValidUsername(String user) {
        return user.matches("^[a-zA-Z0-9_.-]{3,20}$");
    }

    private class getProfile extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (!isValidUsername(params[0])) {
                    account = null;
                    return null;
                }
                account = Authentication.reddit.getUser(params[0]);
            } catch (NetworkException ignored) {
            }
            return null;

        }

        @Override
        public void onPostExecute(Void voidd) {

            doClick();

        }
    }

    public class ProfilePagerAdapter extends FragmentStatePagerAdapter {

        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new ContributionsView();
            Bundle args = new Bundle();

            args.putString("id", name);
            String place;
            switch(i){
                case 0: place = "overview";
                    break;
                case 1: place = "comments";
                        break;
                case 2: place = "submitted";
                        break;
                case 3: place = "gilded";
                        break;
                default: place = "overview";
            }
            args.putString("where", place);

            f.setArguments(args);

            return f;


        }


        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.length;
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return usedArray[position];
        }
    }
}