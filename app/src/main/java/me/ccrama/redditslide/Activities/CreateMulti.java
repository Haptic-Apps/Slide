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

package me.ccrama.redditslide.Activities;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;


public class CreateMulti extends BaseActivity {

    ArrayList<String> subs;
    CustomAdapter adapter;
    EditText title;
    RecyclerView recyclerView;
    String old;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        getTheme().applyStyle(new ColorPreferences(this).getFontStyle().getBaseId(), true);
        setContentView(R.layout.activity_createmulti);
        final Toolbar b = (Toolbar) findViewById(R.id.toolbar);
        b.setBackgroundColor(Pallete.getDefaultColor());
        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        setSupportActionBar(b);
        getSupportActionBar().setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getDefaultColor()));
            CreateMulti.this.setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.title_create_multi), ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_launcher)).getBitmap(), Pallete.getDefaultColor()));
        }
        title = (EditText) findViewById(R.id.name);


        subs = new ArrayList<>();
        if(getIntent().hasExtra("multi")){
            String multi = getIntent().getExtras().getString("multi");
            old = multi;
            title.setText(multi.replace("%20", " "));
            for(MultiReddit multiReddit : SubredditStorage.multireddits){
                if(multiReddit.getDisplayName().equals(multi)){
                    for(MultiSubreddit sub : multiReddit.getSubreddits()) {
                        subs.add(sub.getDisplayName().toLowerCase());
                    }
                }
            }
        }
        recyclerView = (RecyclerView) findViewById(R.id.subslist);


        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (title.getText().toString().isEmpty()) {
                    new AlertDialogWrapper.Builder(CreateMulti.this)
                            .setTitle(R.string.multireddit_title_empty)
                            .setMessage(R.string.multireddit_title_empty_msg)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    title.requestFocus();

                                }
                            }).show();
                } else if (subs.size() == 0) {
                    new AlertDialogWrapper.Builder(CreateMulti.this)
                            .setTitle(R.string.multireddit_no_subs)
                            .setMessage(R.string.multireddit_no_subs_msg)
                            .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                } else {
                    new SaveMulti().execute();
                }
            }
        });
        adapter = new CustomAdapter(subs);
        //  adapter.setHasStableIds(true);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

    public void doDoneStuff() {
        Intent i = new Intent(this, MultiredditOverview.class);
        startActivity(i);
        finish();
    }

    boolean delete = false;

    public class SaveMulti extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if(delete){
                    new MultiRedditManager(Authentication.reddit).delete(old);

                } else {
                    if (old != null && !old.isEmpty() && !old.replace(" ", "").equals(title.getText().toString().replace(" ", ""))) {
                        new MultiRedditManager(Authentication.reddit).rename(old, title.getText().toString().replace(" ", ""));
                    }
                    new MultiRedditManager(Authentication.reddit).createOrUpdate(new MultiRedditUpdateRequest.Builder(Authentication.name, title.getText().toString().replace(" ", "")).subreddits(subs).build());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new SubredditStorage.SyncMultireddits(CreateMulti.this).execute();

                        }
                    });
                }

            } catch (final ApiException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CreateMulti.this)
                                .setTitle(R.string.err_title)
                                .setMessage(R.string.misc_err + ": " + e.getExplanation() + "\n" + R.string.misc_retry)
                                .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((FloatingActionButton) findViewById(R.id.send)).show();

                            }
                        }).create().show();
                    }
                });
                e.printStackTrace();
            }
            return null;
        }
    }

    public void showSelectDialog() {
        final String[] all = new String[SubredditStorage.alphabeticalSubscriptions.size() - 2];
        final List<String> s2 = new ArrayList<>(subs);
        boolean[] checked = new boolean[all.length];

        int i = 0;
        for (String s : SubredditStorage.alphabeticalSubscriptions) {
            if(!(s.equals("all") || s.equals("frontpage"))) {
                all[i] = s;
                if (s2.contains(s)) {
                    checked[i] = true;
                }
                i++;
            }
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
                }).setTitle(R.string.multireddit_selector).setPositiveButton(getString(R.string.btn_add).toUpperCase(), new DialogInterface.OnClickListener() {
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
