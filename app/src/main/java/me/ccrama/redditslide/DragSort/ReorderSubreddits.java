/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ccrama.redditslide.DragSort;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.Activities.SettingsTheme;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.UserSubscriptions;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;

public class ReorderSubreddits extends BaseActivityAnim {

    private ArrayList<String> subs;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private String input;
    public static final String MULTI_REDDIT = "/m/";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reorder_subs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.refresh:
                done = 0;
                final Dialog d = new MaterialDialog.Builder(ReorderSubreddits.this).title(R.string.general_sub_sync)
                        .content(R.string.misc_please_wait)
                        .progress(true, 100)
                        .cancelable(false).show();
                new AsyncTask<Void, Void, ArrayList<String>>() {
                    @Override
                    protected ArrayList<String> doInBackground(Void... params) {
                        ArrayList<String> newSubs = new ArrayList<>(UserSubscriptions.syncSubreddits(ReorderSubreddits.this));
                        return newSubs;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<String> newSubs) {
                        d.dismiss();
                        // Determine if we should insert subreddits at the end of the list or sorted
                        boolean sorted = (subs.equals(UserSubscriptions.sortNoExtras(subs)));
                        Resources res = getResources();

                        for (String s : newSubs) {
                            if (!subs.contains(s)) {
                                done++;
                                subs.add(s);
                            }
                        }
                        if (sorted && done > 0) {
                            subs = UserSubscriptions.sortNoExtras(subs);
                            adapter = new CustomAdapter(subs);
                            recyclerView.setAdapter(adapter);
                        } else if (done > 0) {
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(subs.size());
                        }
                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                .setTitle(R.string.reorder_sync_complete)
                                .setMessage(res.getQuantityString(R.plurals.reorder_subs_added, done, done))
                                .setPositiveButton(R.string.btn_ok, null)
                                .show();
                    }
                }.execute();
                return true;
            case R.id.alphabetize:
                subs = UserSubscriptions.sortNoExtras(subs);
                adapter = new CustomAdapter(subs);
                //  adapter.setHasStableIds(true);
                recyclerView.setAdapter(adapter);
                return true;
            case R.id.info:
                new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                        .setTitle(R.string.reorder_subs_FAQ)
                        .setMessage(Html.fromHtml("<b>How do I refresh subreddits?</b><br>" +
                                "To refresh subreddits, click the refresh icon in the toolbar!<br><br>" +
                                "<b>I unsubscribed from some subreddits but they are still in the list!</b><br>" +
                                "Slide will convert unsubscribed subs from the browser into casual subscriptions. To remove them, long press on the subreddit(s) and click remove!<br><br>" +
                                "<b>How do I use collections?</b><br>" +
                                "To create a collection, click the + FAB and select 'Create a collection'. This allows you to have multiple subs acting as one subreddit in the main screen!"))
                        .show();
                return true;
        }
        return false;
    }

    @Override
    public void onPause() {
        UserSubscriptions.setSubscriptions(new ArrayList<>(subs));
        SettingsTheme.changed = true;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isMultiple) {
            chosen = new ArrayList<>();
            doOldToolbar();
            adapter.notifyDataSetChanged();
            isMultiple = false;
        } else {
            super.onBackPressed();
        }
    }

    private ArrayList<String> chosen = new ArrayList<>();
    private boolean isMultiple;
    private int done = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overrideSwipeFromAnywhere();
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sort);
        setupAppBar(R.id.toolbar, R.string.settings_manage_subscriptions, false, true);
        mToolbar.setPopupTheme(new ColorPreferences(this).getFontStyle().getBaseId());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        subs = new ArrayList<>(UserSubscriptions.getSubscriptions(this));
        recyclerView = (RecyclerView) findViewById(R.id.subslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);

        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId();
        dragSortRecycler.setFloatingAlpha();
        dragSortRecycler.setAutoScrollSpeed();
        dragSortRecycler.setAutoScrollWindow();

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                String item = subs.remove(from);
                subs.add(to, item);
                adapter.notifyDataSetChanged();
            }
        });

        dragSortRecycler.setOnDragStateChangedListener(new DragSortRecycler.OnDragStateChangedListener() {
            @Override
            public void onDragStart() {
            }

            @Override
            public void onDragStop() {
            }
        });
        final FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.add);

        {
            FloatingActionButton collection = (FloatingActionButton) findViewById(R.id.collection);
            Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.collection, null);
            collection.setIconDrawable(icon);
            collection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fab.collapse();
                    if (UserSubscriptions.getMultireddits() != null && !UserSubscriptions.getMultireddits().isEmpty()) {
                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                .setTitle(R.string.create_or_import_multi)
                                .setPositiveButton(R.string.btn_new, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        doCollection();
                                    }
                                }).setNegativeButton(R.string.btn_import_multi, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final String[] multis = new String[UserSubscriptions.getMultireddits().size()];
                                int i = 0;
                                for (MultiReddit m : UserSubscriptions.getMultireddits()) {
                                    multis[i] = m.getDisplayName();
                                    i++;
                                }
                                MaterialDialog.Builder builder = new MaterialDialog.Builder(ReorderSubreddits.this);
                                builder.title(R.string.reorder_subreddits_title)
                                        .items(multis)
                                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                                            @Override
                                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {

                                                String name = multis[which];
                                                MultiReddit r = UserSubscriptions.getMultiredditByDisplayName(name);
                                                StringBuilder b = new StringBuilder();

                                                for (MultiSubreddit s : r.getSubreddits()) {
                                                    b.append(s.getDisplayName());
                                                    b.append("+");
                                                }
                                                String finalS = b.toString().substring(0, b.length() - 1);
                                                Log.v(LogUtil.getTag(), finalS);
                                                subs.add(MULTI_REDDIT + r.getDisplayName());
                                                UserSubscriptions.setSubNameToProperties(MULTI_REDDIT + r.getDisplayName(), b.toString());
                                                adapter.notifyDataSetChanged();
                                                recyclerView.smoothScrollToPosition(subs.size());
                                                return false;
                                            }
                                        }).show();
                            }
                        }).show();
                    } else {
                        doCollection();
                    }
                }
            });
        }
        {
            FloatingActionButton collection = (FloatingActionButton) findViewById(R.id.sub);
            Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.sub, null);
            collection.setIconDrawable(icon);
            collection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fab.collapse();
                    MaterialDialog.Builder b = new MaterialDialog.Builder(ReorderSubreddits.this)
                            .title(R.string.reorder_add_subreddit)
                            .inputRangeRes(2, 21, R.color.md_red_500)
                            .alwaysCallInputCallback()
                            .input(getString(R.string.reorder_subreddit_name), null, false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence raw) {
                                    input = raw.toString().replaceAll("\\s", ""); //remove whitespace from input
                                }
                            })
                            .positiveText(R.string.btn_add)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    new AsyncGetSubreddit().execute(input);
                                }
                            })
                            .negativeText(R.string.btn_cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {

                                }
                            });
                    if(Authentication.isLoggedIn && Authentication.didOnline)
                        b.neutralText("Add & Subscribe").onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new AsyncGetSubreddit().execute(input);
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        AccountManager m = new AccountManager(Authentication.reddit);
                                        for (String s : chosen) {
                                            m.subscribe(s);
                                        }
                                        return null;
                                    }
                                }.execute();
                            }
                        });
                    b.show();
                }
            });
        }
        {
            FloatingActionButton collection = (FloatingActionButton) findViewById(R.id.domain);
            Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.link, null);
            collection.setIconDrawable(icon);
            collection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fab.collapse();
                    new MaterialDialog.Builder(ReorderSubreddits.this)
                            .title("Add a domain")
                            .inputRangeRes(2, 21, R.color.md_red_500)
                            .alwaysCallInputCallback()
                            .input("example.com (only the domain name)", null, false, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence raw) {
                                    input = raw.toString().replaceAll("\\s", ""); //remove whitespace from input
                                }
                            })
                            .positiveText(R.string.btn_add)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    try {
                                        String url = (input);

                                        List<String> sortedSubs = UserSubscriptions.sortNoExtras(subs);

                                        if (sortedSubs.equals(subs)) {
                                            subs.add(url);
                                            subs = UserSubscriptions.sortNoExtras(subs);
                                            adapter = new CustomAdapter(subs);
                                            recyclerView.setAdapter(adapter);
                                        } else {
                                            subs.add(url);
                                            adapter.notifyDataSetChanged();
                                            recyclerView.smoothScrollToPosition(subs.size());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        //todo make this better
                                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                                .setTitle("URL Invalid")
                                                .setMessage("Please try again").show();

                                    }
                                }
                            })
                            .negativeText(R.string.btn_cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {

                                }
                            }).show();
                }
            });
        }
        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
        dragSortRecycler.setViewHandleId();

        if (subs != null && !subs.isEmpty()) {
            adapter = new CustomAdapter(subs);
            //  adapter.setHasStableIds(true);
            recyclerView.setAdapter(adapter);
        } else {
            subs = new ArrayList<>();
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING) {
                    diff += dy;
                } else {
                    diff = 0;
                }
                if (dy <= 0 && fab.getId() != 0) {

                } else {
                    fab.collapse();
                }

            }
        });
    }

    public int diff;

    public void doCollection() {
        final ArrayList<String> subs2 = UserSubscriptions.sort(UserSubscriptions.getSubscriptions(this));
        subs2.remove("frontpage");
        subs2.remove("all");

        ArrayList<String> toRemove = new ArrayList<>();
        for (String s : subs2) {
            if (s.contains(".") || s.contains("/m/")) {
                toRemove.add(s);
            }
        }
        subs2.removeAll(toRemove);

        final CharSequence[] subsAsChar = subs2.toArray(new CharSequence[subs2.size()]);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(ReorderSubreddits.this);
        builder.title(R.string.reorder_subreddits_title)
                .items(subsAsChar)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        ArrayList<String> selectedSubs = new ArrayList<>();
                        for (int i : which) {
                            selectedSubs.add(subsAsChar[i].toString());
                        }

                        StringBuilder b = new StringBuilder();

                        for (String s : selectedSubs) {
                            b.append(s);
                            b.append("+");
                        }
                        String finalS = b.toString().substring(0, b.length() - 1);
                        Log.v(LogUtil.getTag(), finalS);
                        subs.add(finalS);
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(subs.size());
                        return true;
                    }
                })
                .positiveText(R.string.btn_add)
                .negativeText(R.string.btn_cancel)
                .show();
    }

    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {
        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null || input.equalsIgnoreCase("friends") || input.equalsIgnoreCase("all") || input.equalsIgnoreCase("frontpage") || input.equalsIgnoreCase("mod")) {
                List<String> sortedSubs = UserSubscriptions.sortNoExtras(subs);

                if (sortedSubs.equals(subs)) {
                    subs.add(input);
                    subs = UserSubscriptions.sortNoExtras(subs);
                    adapter = new CustomAdapter(subs);
                    recyclerView.setAdapter(adapter);
                } else {
                    subs.add(input);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(subs.size());
                }
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            String sub = params[0];
            if (!sub.equalsIgnoreCase("all") && !sub.equalsIgnoreCase("friends") && !sub.equalsIgnoreCase("mod") && !sub.equalsIgnoreCase("frontpage")) {
                try {
                    return (subs.contains(params[0]) ? null : Authentication.reddit.getSubreddit(params[0]));
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                        .setTitle(R.string.subreddit_err)
                                        .setMessage(getString(R.string.subreddit_err_msg, params[0]))
                                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                            }
                                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                    }
                                }).show();
                            } catch (Exception ignored) {
                            }
                        }
                    });
                }
            }
            return null;
        }
    }

    public void doOldToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setVisibility(View.VISIBLE);
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<String> items;

        public CustomAdapter(ArrayList<String> items) {
            this.items = items;
        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublistdrag, parent, false);
            return new ViewHolder(v);
        }

        public void doNewToolbar() {
            mToolbar.setVisibility(View.GONE);
            mToolbar = (Toolbar) findViewById(R.id.toolbar2);
            mToolbar.setTitle(chosen.size() + " selected");
            mToolbar.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle(R.string.reorder_remove_title)
                            .setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (String s : chosen) {
                                        int index = subs.indexOf(s);
                                        subs.remove(index);
                                        adapter.notifyItemRemoved(index);
                                    }
                                    isMultiple = false;
                                    chosen = new ArrayList<>();
                                    doOldToolbar();

                                }
                            });
                    if (Authentication.isLoggedIn && Authentication.didOnline)
                        b.setNeutralButton("REMOVE & UNSUBSCRIBE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (String s : chosen) {
                                    int index = subs.indexOf(s);
                                    subs.remove(index);
                                    adapter.notifyItemRemoved(index);
                                }
                                new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        AccountManager m = new AccountManager(Authentication.reddit);
                                        for (String s : chosen) {
                                            m.unsubscribe(Authentication.reddit.getSubreddit(s));
                                        }
                                        return null;
                                    }
                                }.execute();
                                isMultiple = false;
                                chosen = new ArrayList<>();
                                doOldToolbar();
                            }
                        });
                    b.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
                }
            });
            mToolbar.findViewById(R.id.top).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (String s : chosen) {
                        int index = subs.indexOf(s);
                        subs.remove(index);
                        subs.add(0, s);
                    }
                    isMultiple = false;
                    doOldToolbar();
                    chosen = new ArrayList<>();
                    notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(0);
                }
            });
        }


        public void updateToolbar() {
            mToolbar.setTitle(chosen.size() + " selected");
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String origPos = items.get(position);
            holder.text.setText(origPos);

            if (chosen.contains(origPos)) {
                holder.itemView.setBackgroundColor(Palette.getDarkerColor(holder.text.getCurrentTextColor()));
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.itemView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            holder.itemView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(origPos), PorterDuff.Mode.MULTIPLY);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isMultiple) {
                        isMultiple = true;
                        chosen = new ArrayList<>();
                        chosen.add(origPos);

                        doNewToolbar();
                        holder.itemView.setBackgroundColor(Palette.getDarkerColor(Palette.getDefaultAccent()));
                        holder.text.setTextColor(Color.WHITE);
                    } else if (chosen.contains(origPos)) {
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                        //set the color of the text back to what it should be
                        int[] textColorAttr = new int[]{R.attr.font};
                        TypedArray ta = obtainStyledAttributes(textColorAttr);
                        holder.text.setTextColor(ta.getColor(0, Color.BLACK));
                        ta.recycle();

                        chosen.remove(origPos);

                        if (chosen.isEmpty()) {
                            isMultiple = false;
                            doOldToolbar();
                        }
                    } else {
                        chosen.add(origPos);
                        holder.itemView.setBackgroundColor(Palette.getDarkerColor(Palette.getDefaultAccent()));
                        holder.text.setTextColor(Color.WHITE);
                        updateToolbar();
                    }
                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isMultiple) {
                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                .setItems(new CharSequence[]{"Move to Top", "Delete"}, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 1) {
                                            AlertDialogWrapper.Builder b = new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle(R.string.reorder_remove_title)
                                                    .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            subs.remove(items.get(position));
                                                            adapter.notifyItemRemoved(position);
                                                        }
                                                    }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    });
                                            if (Authentication.isLoggedIn && Authentication.didOnline)
                                                b.setNeutralButton("REMOVE & UNSUBSCRIBE", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        final String sub = items.get(position);
                                                        subs.remove(sub);
                                                        adapter.notifyItemRemoved(position);
                                                        new AsyncTask<Void, Void, Void>() {
                                                            @Override
                                                            protected Void doInBackground(Void... params) {
                                                                AccountManager m = new AccountManager(Authentication.reddit);
                                                                m.unsubscribe(Authentication.reddit.getSubreddit(sub));
                                                                return null;
                                                            }
                                                        }.execute();
                                                    }
                                                });
                                            b.show();
                                        } else {
                                            String s = items.get(holder.getAdapterPosition());
                                            int index = subs.indexOf(s);
                                            subs.remove(index);
                                            subs.add(0, s);

                                            notifyItemMoved(holder.getAdapterPosition(), 0);
                                            recyclerView.smoothScrollToPosition(0);
                                        }
                                    }
                                }).show();
                    } else {
                        if (chosen.contains(origPos)) {
                            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                            //set the color of the text back to what it should be
                            int[] textColorAttr = new int[]{R.attr.font};
                            TypedArray ta = obtainStyledAttributes(textColorAttr);
                            holder.text.setTextColor(ta.getColor(0, Color.BLACK));
                            ta.recycle();

                            chosen.remove(origPos);
                            updateToolbar();

                            if (chosen.isEmpty()) {
                                isMultiple = false;
                                doOldToolbar();
                            }
                        } else {
                            chosen.add(origPos);
                            holder.itemView.setBackgroundColor(Palette.getDarkerColor(Palette.getDefaultAccent()));
                            holder.text.setTextColor(Color.WHITE);
                            updateToolbar();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.name);
            }
        }
    }
}
