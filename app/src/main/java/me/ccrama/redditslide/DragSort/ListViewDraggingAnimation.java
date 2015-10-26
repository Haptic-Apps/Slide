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

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


public class ListViewDraggingAnimation extends BaseActivity {

    ArrayList<String> subs;
    CustomAdapter adapter;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_sort);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        setSupportActionBar(b);
        getSupportActionBar().setTitle(R.string.title_reorder_pins);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            ListViewDraggingAnimation.this.setTaskDescription(new ActivityManager.TaskDescription("Reorder Pins", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

       subs = SubredditStorage.getPins();
        if (subs != null && !subs.isEmpty()) {
          recyclerView = (RecyclerView) findViewById(R.id.subslist);


            findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubredditStorage.setPins(new ArrayList<>(subs));
                    finish();
                }
            });
           adapter = new CustomAdapter(subs);
            //  adapter.setHasStableIds(true);

            recyclerView.setAdapter(adapter);
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

            recyclerView.addItemDecoration(dragSortRecycler);
            recyclerView.addOnItemTouchListener(dragSortRecycler);
            recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());
        } else {
            new AlertDialogWrapper.Builder(this)
                    .setTitle(R.string.pins_err_title)
                    .setMessage(R.string.pins_err_msg)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }


    public void showSelectDialog() {
        final String[] all = new String[SubredditStorage.alphabeticalSubscriptions.size()];
        final List<String> s2 = new ArrayList<>(subs);
        boolean[] checked = new boolean[all.length];

        int i = 0;
        for (String s : SubredditStorage.alphabeticalSubscriptions) {
            all[i] = s;
            if (s2.contains(s)) {
                checked[i] = true;
            }
            i++;
        }
        final ArrayList<String> toCheck = new ArrayList<>();


        toCheck.addAll(subs);
        new AlertDialogWrapper.Builder(this)
                .setMultiChoiceItems(all, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (!isChecked) {
                            toCheck.remove(all[which]);
                        } else {
                            toCheck.add(all[which]);
                        }
                        Log.v("Slide", "Done with " + all[which]);
                    }
                }).setTitle("Select subreddits to pin").setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                subs = toCheck;
                Log.v("Slide", subs.size() + "SIZE ");
                adapter = new CustomAdapter(subs);
                recyclerView.setAdapter(adapter);

            }
        }).show();
    }

    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<String> items;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;

            public ViewHolder(View itemView) {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.name);


            }
        }

        public CustomAdapter(ArrayList<String> items) {
            this.items = items;

        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublist, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            String origPos = items.get(position);
            holder.text.setText(origPos);

            holder.itemView.findViewById(R.id.color).setBackgroundResource(R.drawable.circle);
            holder.itemView.findViewById(R.id.color).getBackground().setColorFilter(Pallete.getColor(origPos), PorterDuff.Mode.MULTIPLY);


        }

        @Override
        public int getItemCount() {
            return items.size();
        }


    }
}
