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

import net.dean.jraw.models.Subreddit;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;


public class ListViewDraggingAnimation extends BaseActivityAnim {
    String input;

    ArrayList<String> subs;
    CustomAdapter adapter;
    RecyclerView recyclerView;
    private class AsyncGetSubreddit extends AsyncTask<String, Void, Subreddit> {

        @Override
        public void onPostExecute(Subreddit subreddit) {
            if (subreddit != null) {
                subs.add(input);
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(subs.size());
            }
        }

        @Override
        protected Subreddit doInBackground(final String... params) {
            try {
                return Authentication.reddit.getSubreddit(params[0]);
            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new AlertDialogWrapper.Builder(ListViewDraggingAnimation.this)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sort);
        setupAppBar(R.id.toolbar, R.string.title_reorder_subs, false, true);


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
                final Dialog d = new MaterialDialog.Builder(ListViewDraggingAnimation.this).title(R.string.general_sub_sync)
                        .progress(true, 100)
                        .cancelable(false).show();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        subs = new ArrayList<>(SubredditStorage.syncSubreddits(false, true));

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {

                        d.dismiss();
                        adapter = new CustomAdapter(subs);
                        //  adapter.setHasStableIds(true);

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
        recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());
        dragSortRecycler.setViewHandleId();
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubredditStorage.saveSubredditsForHome(new ArrayList<>(subs));
                finish();
            }
        });
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new MaterialDialog.Builder(ListViewDraggingAnimation.this)
                       .title("Add a subreddit")
                       .inputRangeRes(3, 21, R.color.md_red_500)
                       .alwaysCallInputCallback()
                       .input("Subreddit name", null, false, new MaterialDialog.InputCallback() {
                           @Override
                           public void onInput(MaterialDialog dialog, CharSequence raw) {
                               input = raw.toString();
                           }
                       })
                       .positiveText("Add")
                       .onPositive(new MaterialDialog.SingleButtonCallback() {
                           @Override
                           public void onClick(MaterialDialog dialog, DialogAction which) {
                               Log.v("Slide", input);
                              new AsyncGetSubreddit().execute(input);

                           }
                       })
                       .negativeText("Cancel")
                       .onNegative(new MaterialDialog.SingleButtonCallback() {
                           @Override
                           public void onClick(MaterialDialog dialog, DialogAction which) {

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
        public void onBindViewHolder(ViewHolder holder, final int position) {

            String origPos = items.get(position);
            holder.text.setText(origPos);

            holder.itemView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            holder.itemView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(origPos), PorterDuff.Mode.MULTIPLY);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialogWrapper.Builder(ListViewDraggingAnimation.this).setTitle("Remove this sub?")
                            .setMessage("You will not be unsubscribed, but will not see this subreddit in your sidebar!")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    subs.remove(items.get(position));
                                    adapter.notifyItemRemoved(position);
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public  class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;

            public ViewHolder(View itemView) {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.name);


            }
        }


    }
}
