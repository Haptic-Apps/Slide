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
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;
import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;


public class ReorderSubreddits extends BaseActivityAnim {
    String input;

    ArrayList<String> subs;
    CustomAdapter adapter;
    RecyclerView recyclerView;


    @Override
    public void onDestroy() {
        super.onDestroy();
        SubredditStorage.saveSubredditsForHome(new ArrayList<>(subs));

    }

    int done = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sort);
        setupAppBar(R.id.toolbar, R.string.title_reorder_subs, false, true);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        subs = new ArrayList<>(SubredditStorage.subredditsForHome);
        recyclerView = (RecyclerView) findViewById(R.id.subslist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);


        findViewById(R.id.az).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subs = new ArrayList<>(SubredditStorage.sort(subs));
                adapter = new CustomAdapter(subs);
                //  adapter.setHasStableIds(true);

                recyclerView.setAdapter(adapter);
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                done = 0;


                final Dialog d = new MaterialDialog.Builder(ReorderSubreddits.this).title(R.string.general_sub_sync)
                        .content(R.string.misc_please_wait)
                        .progress(true, 100)
                        .cancelable(false).show();
                new AsyncTask<Void, Void, ArrayList<String>>() {
                    @Override
                    protected ArrayList<String> doInBackground(Void... params) {
                        ArrayList<String> newSubs = new ArrayList<>(SubredditStorage.syncSubreddits(false, true));

                        return newSubs;
                    }

                    @Override
                    protected void onPostExecute(ArrayList<String> newSubs) {

                        d.dismiss();
                        for (String s : newSubs) {
                            if (!subs.contains(s)) {
                                done++;
                                subs.add(s);

                            }

                        }
                        adapter.notifyDataSetChanged();
                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                .setTitle(R.string.reorder_sync_complete)
                                .setMessage(done + getString(R.string.reorder_subs_added))
                                .setPositiveButton(R.string.btn_ok, null)
                                .show();
                        recyclerView.setAdapter(adapter);
                    }
                }.execute();


            }
        });
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

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
        dragSortRecycler.setViewHandleId();

       final FloatingActionButton fab =  (FloatingActionButton) findViewById(R.id.add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                        .setItems(new CharSequence[]{"Add a Subreddit", "Add a Collection"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 1) {
                                    if (SubredditStorage.getMultireddits().size() > 0) {
                                        new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                                                .setTitle("Would you like to create a new Collection or import a Multireddit?")
                                                .setPositiveButton("New", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        doCollection();
                                                    }
                                                }).setNegativeButton("Import multi", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String[] multis = new String[SubredditStorage.getMultireddits().size()];
                                                int i = 0;
                                                for (MultiReddit m : SubredditStorage.getMultireddits()) {
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
                                                                MultiReddit r = SubredditStorage.getMultiredditByDisplayName(name);
                                                                StringBuilder b = new StringBuilder();

                                                                for (MultiSubreddit s : r.getSubreddits()) {
                                                                    b.append(s.getDisplayName());
                                                                    b.append("+");
                                                                }
                                                                String finalS = b.toString().substring(0, b.length() - 1);
                                                                Log.v(LogUtil.getTag(), finalS);
                                                                subs.add(finalS);
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

                                } else {
                                    new MaterialDialog.Builder(ReorderSubreddits.this)
                                            .title(R.string.reorder_add_subreddit)
                                            .inputRangeRes(2, 20, R.color.md_red_500)
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
                                            }).show();
                                }
                            }
                        }).show();
            }
        });
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
                    if (dy <= 0 && fab.getId() != 0 && SettingValues.fab) {

                        fab.show();


                    } else {
                        fab.hide();


                }
            }
        });
    }

    public void doCollection() {
        final ArrayList<String> subs2 = SubredditStorage.sort(SubredditStorage.subredditsForHome);
        subs2.remove("frontpage");
        subs2.remove("all");

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
            if (subreddit != null || input.equalsIgnoreCase("friends") || input.equalsIgnoreCase("mod")) {
                subs.add(input);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(subs.size());
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                if (subs.contains(params[0])) return null;
                return Authentication.reddit.getSubreddit(params[0]);
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
                        } catch (Exception e) {

                        }
                    }
                });

                return null;
            }
        }

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

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            String origPos = items.get(position);
            holder.text.setText(origPos);

            holder.itemView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            holder.itemView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(origPos), PorterDuff.Mode.MULTIPLY);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                            .setItems(new CharSequence[]{"Move to Top", "Delete"}, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 1) {
                                        new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle(R.string.reorder_remove_title)
                                                .setMessage(R.string.reorder_remove_msg)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        subs.remove(items.get(holder.getAdapterPosition()));
                                                        adapter.notifyItemRemoved(holder.getAdapterPosition());
                                                    }
                                                }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
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
                    return true;
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialogWrapper.Builder(ReorderSubreddits.this)
                            .setItems(new CharSequence[]{"Move to Top", "Delete"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == 1) {
                                        new AlertDialogWrapper.Builder(ReorderSubreddits.this).setTitle(R.string.reorder_remove_title)
                                                .setMessage(R.string.reorder_remove_msg)
                                                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        subs.remove(items.get(position));
                                                        adapter.notifyItemRemoved(position);
                                                    }
                                                }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();
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
