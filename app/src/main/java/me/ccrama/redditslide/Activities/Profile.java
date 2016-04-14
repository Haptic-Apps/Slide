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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.Trophy;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.ContributionsView;
import me.ccrama.redditslide.Fragments.HistoryView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.CustomTabUtil;
import me.ccrama.redditslide.util.LogUtil;
import uz.shift.colorpicker.LineColorPicker;
import uz.shift.colorpicker.OnColorChangedListener;

/**
 * Created by ccrama on 9/17/2015.
 */
public class Profile extends BaseActivityAnim {

    public static final String EXTRA_PROFILE = "profile";
    public static final String EXTRA_SAVED = "saved";
    public static final String EXTRA_COMMENT = "comment";
    public static final String EXTRA_SUBMIT = "submitted";
    public static final String EXTRA_UPVOTE = "upvoted";
    public static final String EXTRA_HISTORY = "history";
    private String name;
    private Account account;
    private List<Trophy> trophyCase;
    private ViewPager pager;
    private TabLayout tabs;
    private String[] usedArray;

    public static boolean isValidUsername(String user) {
        /* https://github.com/reddit/reddit/blob/master/r2/r2/lib/validator/validator.py#L261 */
        return user.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    boolean friend;

    public static Sorting profSort;
    public static TimePeriod profTime;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        name = getIntent().getExtras().getString(EXTRA_PROFILE, "");
        applyColorTheme();
        setContentView(R.layout.activity_profile);
        setupUserAppBar(R.id.toolbar, name, true, name);

        profSort = Sorting.HOT;
        profTime = TimePeriod.ALL;

        findViewById(R.id.header).setBackgroundColor(Palette.getColorUser(name));

        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setSelectedTabIndicatorColor(new ColorPreferences(Profile.this).getColor("no sub"));


        pager = (ViewPager) findViewById(R.id.content_view);
        if (name.equals(Authentication.name))
            setDataSet(new String[]{getString(R.string.profile_overview),
                    getString(R.string.profile_comments),
                    getString(R.string.profile_submitted),
                    getString(R.string.profile_gilded),
                    getString(R.string.profile_upvoted),
                    getString(R.string.profile_downvoted),
                    getString(R.string.profile_saved),
                    getString(R.string.profile_hidden),
                    getString(R.string.profile_history)
            });

        else setDataSet(new String[]{getString(R.string.profile_overview),
                getString(R.string.profile_comments),
                getString(R.string.profile_submitted),
                getString(R.string.profile_gilded)});


        new getProfile().execute(name);

        findViewById(R.id.sort).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPopup();
            }
        });

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                findViewById(R.id.header).animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                if (position < 3) {
                    findViewById(R.id.sort).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.sort).setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (getIntent().hasExtra(EXTRA_SAVED) && name.equals(Authentication.name))
            pager.setCurrentItem(6);
        if (getIntent().hasExtra(EXTRA_COMMENT) && name.equals(Authentication.name))
            pager.setCurrentItem(1);
        if (getIntent().hasExtra(EXTRA_SUBMIT) && name.equals(Authentication.name))
            pager.setCurrentItem(2);
        if (getIntent().hasExtra(EXTRA_HISTORY) && name.equals(Authentication.name))
            pager.setCurrentItem(8);
        if (getIntent().hasExtra(EXTRA_UPVOTE) && name.equals(Authentication.name))
            pager.setCurrentItem(4);
        if (pager.getCurrentItem() < 3) {
            findViewById(R.id.sort).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.sort).setVisibility(View.GONE);
        }
    }

    private void doClick() {
        if (account == null) {
            try {
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
            } catch (MaterialDialog.DialogException e) {
                Log.w(LogUtil.getTag(), "Activity already in background, dialog not shown " + e);
            }
            return;
        }
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.colorprofile, null);
                AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Profile.this);
                final TextView title = (TextView) dialoglayout.findViewById(R.id.title);
                title.setText(name);

                dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Reddit.defaultShareText(name + "'s profile" + "\n" + "https://www.reddit.com/u/" + name, Profile.this);
                    }
                });
                final int currentColor = Palette.getColorUser(name);
                title.setBackgroundColor(currentColor);
                StringBuilder info = new StringBuilder();
                info.append("Redditor for ");
                info.append(TimeUtils.getLengthTimeSince(account.getCreated().getTime(), Profile.this));
                info.append(". ");
               /*todo better if (account.hasGold() &&account.getDataNode().has("gold_expiration") ) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(account.getDataNode().get("gold_expiration").asLong());
                    info.append("Gold expires on " + new SimpleDateFormat("dd/MM/yy").format(c.getTime()));
                }*/
                ((TextView) dialoglayout.findViewById(R.id.moreinfo)).setText(info.toString());

                String tag = UserTags.getUserTag(name);
                if (tag.isEmpty()) {
                    tag = "Tag user";
                } else {
                    tag = "User tagged as '" + tag + "'";
                }
                ((TextView) dialoglayout.findViewById(R.id.tagged)).setText(tag);
                LinearLayout l = (LinearLayout) dialoglayout.findViewById(R.id.trophies_inner);

                dialoglayout.findViewById(R.id.tag).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialDialog.Builder b = new MaterialDialog.Builder(Profile.this)
                                .title("Set tag for " + name)
                                .input("Tag", UserTags.getUserTag(name), false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {

                                    }
                                }).positiveText("Set tag")
                                .neutralText(R.string.btn_cancel);

                        if (UserTags.isUserTagged(name)) {
                            b.negativeText("Remove tag");
                        }
                        b.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                UserTags.setUserTag(name, dialog.getInputEditText().getText().toString());
                                String tag = UserTags.getUserTag(name);
                                if (tag.isEmpty()) {
                                    tag = "Tag user";
                                } else {
                                    tag = "User tagged as '" + tag + "'";
                                }
                                ((TextView) dialoglayout.findViewById(R.id.tagged)).setText(tag);
                            }
                        }).onNeutral(null).onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                UserTags.removeUserTag(name);
                                String tag = UserTags.getUserTag(name);
                                if (tag.isEmpty()) {
                                    tag = "Tag user";
                                } else {
                                    tag = "User tagged as '" + tag + "'";
                                }
                                ((TextView) dialoglayout.findViewById(R.id.tagged)).setText(tag);
                            }
                        }).show();
                    }
                });
                if (trophyCase.isEmpty()) {
                    dialoglayout.findViewById(R.id.trophies).setVisibility(View.GONE);
                } else {
                    for (final Trophy t : trophyCase) {
                        View view = getLayoutInflater().inflate(R.layout.trophy, null);
                        // Edit
                        ((Reddit) getApplicationContext()).getImageLoader().displayImage(t.getIcon(), ((ImageView) view.findViewById(R.id.image)));
                        ((TextView) view.findViewById(R.id.trophyTitle)).setText(t.getFullName());
                        if (t.getAboutUrl() != null)
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CustomTabUtil.openUrl("https://reddit.com" + t.getAboutUrl(), Palette.getColorUser(account.getFullName()), Profile.this);
                                }
                            });
                        l.addView(view);
                    }
                }
                if (Authentication.isLoggedIn) {
                    dialoglayout.findViewById(R.id.pm).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Profile.this, Sendmessage.class);
                            i.putExtra(Sendmessage.EXTRA_NAME, name);
                            startActivity(i);
                        }
                    });

                    friend = account.isFriend();
                    if (friend) {
                        ((TextView) dialoglayout.findViewById(R.id.friend)).setText(R.string.profile_remove_friend);
                    } else {
                        ((TextView) dialoglayout.findViewById(R.id.friend)).setText(R.string.profile_add_friend);

                    }
                    dialoglayout.findViewById(R.id.friend_body).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    if (friend) {
                                        try {
                                            new AccountManager(Authentication.reddit).deleteFriend(name);
                                        } catch (Exception ignored) {
                                            //Will throw java.lang.IllegalStateException: No Content-Type header was found, but it still works.
                                        }
                                        friend = false;

                                    } else {
                                        new AccountManager(Authentication.reddit).updateFriend(name);
                                        friend = true;


                                    }
                                    return null;
                                }

                                @Override
                                public void onPostExecute(Void voids) {
                                    if (friend) {
                                        ((TextView) dialoglayout.findViewById(R.id.friend)).setText(R.string.profile_remove_friend);
                                    } else {
                                        ((TextView) dialoglayout.findViewById(R.id.friend)).setText(R.string.profile_add_friend);
                                    }
                                }
                            }.execute();

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

                colorPicker.setColors(ColorPreferences.getBaseColors(Profile.this));

                colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int c) {

                        colorPicker2.setColors(ColorPreferences.getColors(getBaseContext(), c));
                        colorPicker2.setSelectedColor(c);


                    }
                });

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
                        if (mToolbar != null)
                            mToolbar.setBackgroundColor(colorPicker2.getColor());
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

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        findViewById(R.id.header).setBackgroundColor(currentColor);
                        if (mToolbar != null)
                            mToolbar.setBackgroundColor(currentColor);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(Palette.getDarkerColor(currentColor));
                        }
                    }
                });

                builder.setView(dialoglayout);
                builder.show();
            }
        });
    }

    private void setDataSet(String[] data) {
        usedArray = data;
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(1);
        tabs.setupWithViewPager(pager);


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
                trophyCase = new FluentRedditClient(Authentication.reddit).user(params[0]).trophyCase();
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

            if(i < 8) {
                Fragment f = new ContributionsView();
                Bundle args = new Bundle();

                args.putString("id", name);
                String place;
                switch (i) {
                    case 0:
                        place = "overview";
                        break;
                    case 1:
                        place = "comments";
                        break;
                    case 2:
                        place = "submitted";
                        break;
                    case 3:
                        place = "gilded";
                        break;
                    case 4:
                        place = "liked";
                        break;
                    case 5:
                        place = "disliked";
                        break;
                    case 6:
                        place = "saved";
                        break;
                    case 7:
                        place = "hidden";
                        break;
                    default:
                        place = "overview";
                }
                args.putString("where", place);

                f.setArguments(args);
                return f;
            }
            else {
                Fragment f = new HistoryView();
                return f;
            }


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

    public void openPopup() {
        PopupMenu popup = new PopupMenu(Profile.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final String[] base = Reddit.getSortingStrings(getBaseContext());
        for (String s : base) {
            popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (String s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        profSort = (Sorting.HOT);
                        break;
                    case 1:
                        profSort = (Sorting.NEW);

                        break;
                    case 2:
                        profSort = (Sorting.RISING);

                        break;
                    case 3:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.HOUR);

                        break;
                    case 4:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.DAY);

                        break;
                    case 5:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.WEEK);

                        break;
                    case 6:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.MONTH);

                        break;
                    case 7:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.YEAR);

                        break;
                    case 8:
                        profSort = (Sorting.TOP);
                        profTime = (TimePeriod.ALL);

                        break;
                    case 9:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.HOUR);

                        break;
                    case 10:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.DAY);

                        break;
                    case 11:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.WEEK);

                    case 12:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.MONTH);

                    case 13:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.YEAR);

                    case 14:
                        profSort = (Sorting.CONTROVERSIAL);
                        profTime = (TimePeriod.ALL);

                }

                Reddit.sorting.put(name, profSort);
                Reddit.times.put(name, profTime);
                int current = pager.getCurrentItem();
                ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
                pager.setAdapter(adapter);
                pager.setOffscreenPageLimit(1);
                tabs.setupWithViewPager(pager);
                pager.setCurrentItem(current);

                return true;
            }
        });
        popup.show();


    }

}