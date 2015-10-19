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
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import java.util.ArrayList;

import me.ccrama.redditslide.Activities.BaseActivity;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


public class ListViewDraggingAnimation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_sort);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        setSupportActionBar(b);
        getSupportActionBar().setTitle("Reorder Pins");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            ListViewDraggingAnimation.this.setTaskDescription(new ActivityManager.TaskDescription("Reorder Pins", ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }

        final ArrayList<String> subs = new ArrayList<>(SubredditStorage.getPins());
        if(subs != null && !subs.isEmpty()) {
            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.subslist);


            findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubredditStorage.setPins(subs);
                    finish();
                }
            });
            final CustomAdapter adapter = new CustomAdapter(subs, this);
            //  adapter.setHasStableIds(true);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(null);

            DragSortRecycler dragSortRecycler = new DragSortRecycler();
            dragSortRecycler.setViewHandleId(R.id.name);
            dragSortRecycler.setFloatingAlpha(0.4f);
            dragSortRecycler.setAutoScrollSpeed(0.3f);
            dragSortRecycler.setAutoScrollWindow(0.1f);

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
            new AlertDialogWrapper.Builder(this).setTitle("No Pins (yet)!").setMessage("You can pin your favorite subreddits to the front of your subreddit list. To do this, open a subreddit sidebar (click the I icon or swipe from the right edge of the screen), and check 'PINNED'!")
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }
    }



    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder>
    {
        private Context context;

        private ArrayList<String> items;

        public static class ViewHolder extends RecyclerView.ViewHolder
        {
            TextView text;
            public ViewHolder(View itemView)
            {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.name);


            }
        }

        public CustomAdapter(ArrayList<String> items, Context context)
        {
            this.items = items;
            this.context = context;

        }

        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subforsublist, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {

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
