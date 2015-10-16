package me.ccrama.redditslide.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.SideArrayAdapter;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.Visuals.StyleView;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class CommentView extends BaseActivity {
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit("ASDF"), true);

        setContentView(R.layout.activity_overview);
        StyleView.styleActivity(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Slide");
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.setStatusBarColor(Pallete.getDefaultColor());
        }
        tabs = (TabLayout) findViewById(R.id.sliding_tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);

        pager = (ViewPager) findViewById(R.id.contentView);

        setDataSet(SubredditStorage.subredditsForHome);
        doSidebar();

    }

    public OverviewPagerAdapter adapter;

    public ViewPager pager;
    public TabLayout tabs;

    public List<String> usedArray;

    public void setDataSet(List<String> data) {
        usedArray = data;
        if (adapter == null) {
            adapter = new OverviewPagerAdapter(getSupportFragmentManager());
        } else {
            adapter.notifyDataSetChanged();
        }
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);
        tabs.setupWithViewPager(pager);
    }

    public class OverviewPagerAdapter extends FragmentStatePagerAdapter {

        public OverviewPagerAdapter(FragmentManager fm) {
            super(fm);
            pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    hea.setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    findViewById(R.id.header).setBackgroundColor(Pallete.getColor(usedArray.get(position)));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Window window = getWindow();
                        window.setStatusBarColor(Pallete.getDarkerColor(usedArray.get(position)));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        @Override
        public Fragment getItem(int i) {

            Fragment f = new CommentPage();
            Bundle args = new Bundle();

            args.putString("id", usedArray.get(i));

            f.setArguments(args);

            return f;


        }




        @Override
        public int getCount() {
            if (usedArray == null) {
                return 1;
            } else {
                return usedArray.size();
            }
        }


        @Override
        public CharSequence getPageTitle(int position) {
            Log.v("Slide", usedArray.get(position));
            return usedArray.get(position);
        }
    }


    public View hea;

    public DrawerLayout drawerLayout;
    public void doSidebar() {
        ListView l = (ListView) findViewById(R.id.drawerlistview);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflater.inflate(R.layout.drawerbottom, l, false);
        l.addFooterView(footer, null, false);
        View header;

        if(Authentication.isLoggedIn) {

            header = inflater.inflate(R.layout.drawer_loggedin, l, false);
            hea = header.findViewById(R.id.back);
            l.addHeaderView(header, null, false);
            ((TextView)header.findViewById(R.id.name)).setText(Authentication.name);
            header.findViewById(R.id.multi).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                    new android.support.v7.app.AlertDialog.Builder(CommentView.this)
                            .setTitle("Multireddits coming soon!")
                            .setMessage("Multireddits will be included in an update very soon! Keep your eyes out for it :)")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).show();

                }
            });
           /* header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, Profile.class);
                    inte.putExtra("name", Authentication.name);
                    Overview.this.startActivity(inte);
                }
            });*/
            header.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Authentication.authentication.edit().remove("lasttoken").apply();
                    ((Reddit)CommentView.this.getApplicationContext()).restart();
                }
            });
            /*header.findViewById(R.id.saved).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, SavedView.class);
                    inte.putExtra("type", "Saved");
                    Overview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.upvoted).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, SavedView.class);
                    inte.putExtra("type", "Liked");

                    Overview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, Inbox.class);
                    Overview.this.startActivity(inte);
                }
            });
            header.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, Submit.class);
                    Overview.this.startActivity(inte);
                }
            });
            footer.findViewById(R.id.support).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, DonateView.class);

                    Overview.this.startActivity(inte);
                }
            });*/
        } else {
            header = inflater.inflate(R.layout.drawer_loggedout, l, false);
            l.addHeaderView(header, null, false);


            /*header.findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent inte = new Intent(Overview.this, Login.class);
                    Overview.this.startActivity(inte);
                }
            });*/
        }

        /*header.findViewById(R.id.prof).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText input = new EditText(Overview.this);

                new android.support.v7.app.AlertDialog.Builder(Overview.this)
                        .setTitle("Enter Username")
                        .setView(input)
                        .setPositiveButton("Go to user", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Editable value = input.getText();
                                Intent inte = new Intent(Overview.this, Profile.class);
                                inte.putExtra("name", value.toString());
                                Overview.this.startActivity(inte);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

            }
        });*/
            footer.findViewById(R.id.tablet).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                  /*  Intent inte = new Intent(Overview.this, Overview.class);
                    inte.putExtra("type", UpdateSubreddits.COLLECTIONS);
                    Overview.this.startActivity(inte);*/
                    if (Reddit.tabletUI) {
                        new android.support.v7.app.AlertDialog.Builder(CommentView.this)
                                .setTitle("Multi-Column settings coming soon!")
                                .setMessage("In the next update, you will be able to set the amount of cards you would like to see and which veiws to apply Multi-Column to!")
                                .setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                }).show();
                    } else {
                        new android.support.v7.app.AlertDialog.Builder(CommentView.this)
                                .setTitle("Unlock Grid Layout")
                                .setMessage("I have opted to make Multi-Column a paid feature of Slide for Reddit. I am a student developer, and can't keep up the pace of development if I have to get a supplementary job to support myself. This Multi-Column is in lieu of ads or locking already unlocked content, and the app will function normally without purchasing it!\n\nWould you like to unlock Multi-Column?")
                                .setPositiveButton("Sure!", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=me.ccrama.slideforreddittabletuiunlock")));
                                        }
                                    }
                                }).setNegativeButton("No thank you", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).show();
                    }
                }
            });


      /*  footer.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte = new Intent(Overview.this, Setting.class);
                Overview.this.startActivityForResult(inte, 3);
            }
        });*/
            ArrayList<String> copy = new ArrayList<>();
            for (String s : SubredditStorage.alphabeticalSubscriptions) {
                copy.add(s);
            }

            final SideArrayAdapter adapter = new SideArrayAdapter(this, copy);
            l.setAdapter(adapter);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    drawerLayout,
                    toolbar,
                    R.string.hello_world,
                    R.string.hello_world
            )

            {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    invalidateOptionsMenu();
                    syncState();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    invalidateOptionsMenu();
                    syncState();
                }
            };

            actionBarDrawerToggle.syncState();
            header.findViewById(R.id.back).setBackgroundColor(Pallete.getColor("alsdkfjasld"));
            drawerLayout.setDrawerListener(actionBarDrawerToggle);
            final EditText e = ((EditText) header.findViewById(R.id.sort));

            e.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    adapter.getFilter().filter(e.getText().toString());

                }
            });


    }


}
