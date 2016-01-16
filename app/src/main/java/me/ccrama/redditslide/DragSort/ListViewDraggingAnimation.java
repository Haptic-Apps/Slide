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

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.BaseActivityAnim;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;


public class ListViewDraggingAnimation extends BaseActivityAnim {

    ArrayList<String> subs;
    CustomAdapter adapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_sort);
        setupAppBar(R.id.toolbar, R.string.title_reorder_pins, false, true);


        subs = new ArrayList<>(SubredditStorage.subredditsForHome);
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

        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.setOnScrollListener(dragSortRecycler.getScrollListener());
        dragSortRecycler.setLeftDragArea(R.id.dragit);
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubredditStorage.saveSubredditsForHome(new ArrayList<>(subs));
                finish();
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



    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
        private final ArrayList<String> items;

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
            holder.itemView.findViewById(R.id.color).getBackground().setColorFilter(Palette.getColor(origPos), PorterDuff.Mode.MULTIPLY);


        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView text;

            public ViewHolder(View itemView) {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.name);


            }
        }


    }
}
