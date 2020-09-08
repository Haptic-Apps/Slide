package me.ccrama.redditslide.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import net.dean.jraw.fluent.FluentRedditClient;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.Account;
import net.dean.jraw.models.Trophy;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.ContributionsView;
import me.ccrama.redditslide.Fragments.HistoryView;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.UserTags;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LinkUtil;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SortingUtil;
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
    public boolean isSavedView;

    private void scrollToTabAfterLayout(final int tabIndex) {
        //from http://stackoverflow.com/a/34780589/3697225
        if (tabs != null) {
            final ViewTreeObserver observer = tabs.getViewTreeObserver();

            if (observer.isAlive()) {
                observer.dispatchOnGlobalLayout(); // In case a previous call is waiting when this call is made
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        tabs.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        tabs.getTabAt(tabIndex).select();
                    }
                });
            }
        }
    }

    public static boolean isValidUsername(String user) {
        /* https://github.com/reddit/reddit/blob/master/r2/r2/lib/validator/validator.py#L261 */
        return user.matches("^[a-zA-Z0-9_-]{3,20}$");
    }

    private boolean friend;
    private MenuItem sortItem;
    private MenuItem categoryItem;
    public static Sorting profSort;
    public static TimePeriod profTime;

    @Override
    public void onCreate(Bundle savedInstance) {
        overrideSwipeFromAnywhere();

        super.onCreate(savedInstance);

        name = getIntent().getExtras().getString(EXTRA_PROFILE, "");

        setShareUrl("https://reddit.com/u/" + name);

        applyColorTheme();
        setContentView(R.layout.activity_profile);
        setupUserAppBar(R.id.toolbar, name, true, name);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());

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

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                isSavedView = position == 6;
                findViewById(R.id.header).animate()
                        .translationY(0)
                        .setInterpolator(new LinearInterpolator())
                        .setDuration(180);
                if (sortItem != null) {
                    if (position < 3) {
                        sortItem.setVisible(true);
                    } else {
                        sortItem.setVisible(false);
                    }
                }
                if (categoryItem != null && Authentication.me != null && Authentication.me.hasGold()) {
                    if (position == 6) {
                        categoryItem.setVisible(true);
                    } else {
                        categoryItem.setVisible(false);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (getIntent().hasExtra(EXTRA_SAVED) && name.equals(Authentication.name)) {
            pager.setCurrentItem(6);
        }
        if (getIntent().hasExtra(EXTRA_COMMENT) && name.equals(Authentication.name)) {
            pager.setCurrentItem(1);
        }
        if (getIntent().hasExtra(EXTRA_SUBMIT) && name.equals(Authentication.name)) {
            pager.setCurrentItem(2);
        }
        if (getIntent().hasExtra(EXTRA_HISTORY) && name.equals(Authentication.name)) {
            pager.setCurrentItem(8);
        }
        if (getIntent().hasExtra(EXTRA_UPVOTE) && name.equals(Authentication.name)) {
            pager.setCurrentItem(4);
        }
        isSavedView = pager.getCurrentItem() == 6;
        if (pager.getCurrentItem() != 0) {
            scrollToTabAfterLayout(pager.getCurrentItem());
        }
    }

    private void doClick() {
        if (account == null) {
            try {
                new AlertDialogWrapper.Builder(Profile.this)
                        .setTitle(R.string.profile_err_title)
                        .setCancelable(false)
                        .setMessage(R.string.profile_err_msg)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).setCancelable(false).setOnDismissListener(new DialogInterface.OnDismissListener() {
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
        if (account.getDataNode().has("is_suspended") && account.getDataNode().get("is_suspended").asBoolean()
                && !name.equalsIgnoreCase(Authentication.name)) {
            try {
                new AlertDialogWrapper.Builder(Profile.this)
                        .setTitle(R.string.account_suspended)
                        .setCancelable(false)
                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }).show();
            } catch (MaterialDialog.DialogException e) {
                Log.w(LogUtil.getTag(), "Activity already in background, dialog not shown " + e);
            }
        }
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
            } catch (RuntimeException ignored) {
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
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        }

        @Override
        public Fragment getItem(int i) {

            if (i < 8) {
                Fragment f = new ContributionsView();
                Bundle args = new Bundle();

                args.putString("id", name);
                String place;
                switch (i) {
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
                    case 0:
                    default:
                        place = "overview";
                }
                args.putString("where", place);

                f.setArguments(args);
                return f;
            } else {
                return new HistoryView();
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
        final Spannable[] base = SortingUtil.getSortingSpannables(profSort);
        for (Spannable s : base) {
            MenuItem m = popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (Spannable s : base) {
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
                        openPopupTime();
                        return true;
                    case 4:
                        profSort = (Sorting.CONTROVERSIAL);
                        openPopupTime();
                        return true;
                }

                SortingUtil.sorting.put(name.toLowerCase(Locale.ENGLISH), profSort);

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

    public void openPopupTime() {
        PopupMenu popup = new PopupMenu(Profile.this, findViewById(R.id.anchor), Gravity.RIGHT);
        final Spannable[] base = SortingUtil.getSortingTimesSpannables(profTime);
        for (Spannable s : base) {
            MenuItem m = popup.getMenu().add(s);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                LogUtil.v("Chosen is " + item.getOrder());
                int i = 0;
                for (Spannable s : base) {
                    if (s.equals(item.getTitle())) {
                        break;
                    }
                    i++;
                }
                switch (i) {
                    case 0:
                        profTime = (TimePeriod.HOUR);
                        break;
                    case 1:
                        profTime = (TimePeriod.DAY);
                        break;
                    case 2:
                        profTime = (TimePeriod.WEEK);
                        break;
                    case 3:
                        profTime = (TimePeriod.MONTH);
                        break;
                    case 4:
                        profTime = (TimePeriod.YEAR);
                        break;
                    case 5:
                        profTime = (TimePeriod.ALL);
                        break;
                }

                SortingUtil.sorting.put(name.toLowerCase(Locale.ENGLISH), profSort);
                SortingUtil.times.put(name.toLowerCase(Locale.ENGLISH), profTime);

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

    public String category;
    public String subreddit;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        //used to hide the sort item on certain Profile tabs
        sortItem = menu.findItem(R.id.sort);
        categoryItem = menu.findItem(R.id.category);
        categoryItem.setVisible(false);
        sortItem.setVisible(false);

        int position = pager == null ? 0 : pager.getCurrentItem();
        if (sortItem != null) {
            if (position < 3) {
                sortItem.setVisible(true);
            } else {
                sortItem.setVisible(false);
            }
        }
        if (categoryItem != null && Authentication.me != null && Authentication.me.hasGold()) {
            if (position == 6) {
                categoryItem.setVisible(true);
            } else {
                categoryItem.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
                onBackPressed();
                break;
            case (R.id.category):
                new AsyncTask<Void, Void, List<String>>() {
                    Dialog d;

                    @Override
                    public void onPreExecute() {
                        d = new MaterialDialog.Builder(Profile.this)
                                .progress(true, 100)
                                .content(R.string.misc_please_wait)
                                .title(R.string.profile_category_loading)
                                .show();
                    }

                    @Override
                    protected List<String> doInBackground(Void... params) {
                        try {
                            List<String> categories = new ArrayList<>(new AccountManager(Authentication.reddit).getSavedCategories());
                            categories.add(0, "No category");
                            return categories;
                        } catch (Exception e) {
                            e.printStackTrace();
                            //probably has no categories?
                            return new ArrayList<String>() {{
                                add(0, "No category");
                            }};
                        }
                    }

                    @Override
                    public void onPostExecute(final List<String> data) {
                        try {
                            new MaterialDialog.Builder(Profile.this).items(data)
                                    .title(R.string.profile_category_select)
                                    .itemsCallback(new MaterialDialog.ListCallback() {
                                        @Override
                                        public void onSelection(MaterialDialog dialog, final View itemView, int which, CharSequence text) {
                                            final String t = data.get(which);
                                            if (which == 0)
                                                category = null;
                                            else
                                                category = t;
                                            int current = pager.getCurrentItem();
                                            ProfilePagerAdapter adapter = new ProfilePagerAdapter(getSupportFragmentManager());
                                            pager.setAdapter(adapter);
                                            pager.setOffscreenPageLimit(1);

                                            tabs.setupWithViewPager(pager);
                                            pager.setCurrentItem(current);
                                        }
                                    }).show();
                            if (d != null) {
                                d.dismiss();
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case (R.id.info):
                if (account != null && trophyCase != null) {
                    LayoutInflater inflater = getLayoutInflater();
                    final View dialoglayout = inflater.inflate(R.layout.colorprofile, null);
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Profile.this);
                    final TextView title = dialoglayout.findViewById(R.id.title);
                    title.setText(name);

                    if (account.getDataNode().has("is_employee")
                            && account.getDataNode().get("is_employee").asBoolean()) {
                        SpannableStringBuilder admin = new SpannableStringBuilder("[A]");
                        admin.setSpan(new RelativeSizeSpan(.67f), 0, admin.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        title.append(" ");
                        title.append(admin);
                    }

                    dialoglayout.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Reddit.defaultShareText(getString(R.string.profile_share, name),
                                    "https://www.reddit.com/u/" + name, Profile.this);
                        }
                    });

                    final int currentColor = Palette.getColorUser(name);
                    title.setBackgroundColor(currentColor);

                    String info = getString(R.string.profile_age,
                            TimeUtils.getTimeSince(account.getCreated().getTime(), Profile.this));
               /*todo better if (account.hasGold() &&account.getDataNode().has("gold_expiration") ) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(account.getDataNode().get("gold_expiration").asLong());
                    info.append("Gold expires on " + new SimpleDateFormat("dd/MM/yy").format(c.getTime()));
                }*/

                    ((TextView) dialoglayout.findViewById(R.id.moreinfo)).setText(info);

                    String tag = UserTags.getUserTag(name);
                    if (tag.isEmpty()) {
                        tag = getString(R.string.profile_tag_user);
                    } else {
                        tag = getString(R.string.profile_tag_user_existing, tag);
                    }

                    ((TextView) dialoglayout.findViewById(R.id.tagged)).setText(tag);
                    LinearLayout l = dialoglayout.findViewById(R.id.trophies_inner);

                    dialoglayout.findViewById(R.id.tag).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MaterialDialog.Builder b = new MaterialDialog.Builder(Profile.this)
                                    .title(getString(R.string.profile_tag_set, name))
                                    .input(getString(R.string.profile_tag), UserTags.getUserTag(name), false, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {

                                        }
                                    }).positiveText(R.string.profile_btn_tag)
                                    .neutralText(R.string.btn_cancel);

                            if (UserTags.isUserTagged(name)) {
                                b.negativeText(R.string.profile_btn_untag);
                            }
                            b.onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    UserTags.setUserTag(name, dialog.getInputEditText().getText().toString());
                                    String tag = UserTags.getUserTag(name);
                                    if (tag.isEmpty()) {
                                        tag = getString(R.string.profile_tag_user);
                                    } else {
                                        tag = getString(R.string.profile_tag_user_existing, tag);
                                    }
                                    ((TextView) dialoglayout.findViewById(R.id.tagged)).setText(tag);
                                }
                            }).onNeutral(null).onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    UserTags.removeUserTag(name);
                                    String tag = UserTags.getUserTag(name);
                                    if (tag.isEmpty()) {
                                        tag = getString(R.string.profile_tag_user);
                                    } else {
                                        tag = getString(R.string.profile_tag_user_existing, tag);
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
                            ((Reddit) getApplicationContext()).getImageLoader().displayImage(t.getIcon(), ((ImageView) view.findViewById(R.id.image)));
                            ((TextView) view.findViewById(R.id.trophyTitle)).setText(t.getFullName());
                            if (t.getAboutUrl() != null && !t.getAboutUrl().equalsIgnoreCase("null")) {
                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        LinkUtil.openUrl(LinkUtil.formatURL(t.getAboutUrl()).toString(),
                                                Palette.getColorUser(account.getFullName()),
                                                Profile.this);
                                    }
                                });
                            }
                            l.addView(view);
                        }
                    }
                    if (Authentication.isLoggedIn) {
                        dialoglayout.findViewById(R.id.pm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(Profile.this, SendMessage.class);
                                i.putExtra(SendMessage.EXTRA_NAME, name);
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
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            }
                        });

                        dialoglayout.findViewById(R.id.block_body).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AsyncTask<Void, Void, Boolean>() {
                                    @Override
                                    protected Boolean doInBackground(Void... params) {
                                        Map<String, String> map = new HashMap();
                                        map.put("account_id", "t2_" + account.getId());
                                        try {
                                            Authentication.reddit.execute(Authentication.reddit.request().post(map)
                                                    .path("/api/block_user")
                                                    .build());
                                        } catch (Exception ex) {
                                            return false;
                                        }
                                        return true;
                                    }

                                    @Override
                                    public void onPostExecute(Boolean blocked) {
                                        if (!blocked) {
                                            Toast.makeText(getBaseContext(), getString(R.string.err_block_user), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getBaseContext(), getString(R.string.success_block_user), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                    } else {
                        dialoglayout.findViewById(R.id.pm).setVisibility(View.GONE);
                    }

                    dialoglayout.findViewById(R.id.multi_body).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent inte = new Intent(Profile.this, MultiredditOverview.class);
                                    inte.putExtra(EXTRA_PROFILE, name);
                                    Profile.this.startActivity(inte);
                                }
                            }
                    );

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

                    LineColorPicker colorPicker = dialoglayout.findViewById(R.id.picker);
                    final LineColorPicker colorPicker2 = dialoglayout.findViewById(R.id.picker2);

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
                        TextView dialogButton = dialoglayout.findViewById(R.id.ok);

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
                    {
                        final TextView dialogButton = dialoglayout.findViewById(R.id.reset);

                        // if button is clicked, close the custom dialog
                        dialogButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Palette.removeUserColor(name);

                                Snackbar.make(dialogButton, "User color removed", Snackbar.LENGTH_SHORT).show();

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

                    ((TextView) dialoglayout.findViewById(R.id.commentkarma)).setText(String.format(Locale.getDefault(), "%d", account.getCommentKarma()));
                    ((TextView) dialoglayout.findViewById(R.id.linkkarma)).setText(String.format(Locale.getDefault(), "%d", account.getLinkKarma()));
                    ((TextView) dialoglayout.findViewById(R.id.totalKarma)).setText(String.format(Locale.getDefault(), "%d", account.getCommentKarma() + account.getLinkKarma()));

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
                return true;

            case (R.id.sort):
                openPopup();
                return true;
        }
        return false;
    }
}
