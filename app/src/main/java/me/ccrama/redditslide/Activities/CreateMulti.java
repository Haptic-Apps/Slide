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

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.MultiRedditUpdateRequest;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.MultiRedditManager;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.MultiSubreddit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Palette;


public class CreateMulti extends BaseActivityAnim {

    ArrayList<String> subs;
    CustomAdapter adapter;
    EditText title;
    RecyclerView recyclerView;
    String old;
    boolean delete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_createmulti);
        setupAppBar(R.id.toolbar, R.string.title_multireddits, true);

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        title = (EditText) findViewById(R.id.name);

        subs = new ArrayList<>();
        if (getIntent().hasExtra("multi")) {
            String multi = getIntent().getExtras().getString("multi");
            old = multi;
            title.setText(multi.replace("%20", " "));
            for (MultiReddit multiReddit : SubredditStorage.multireddits) {
                if (multiReddit.getDisplayName().equals(multi)) {
                    for (MultiSubreddit sub : multiReddit.getSubreddits()) {
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

    public void showSelectDialog() {
        final String[] all = new String[SubredditStorage.alphabeticalSubscriptions.size() - 2];
        final List<String> s2 = new ArrayList<>(subs);
        boolean[] checked = new boolean[all.length];

        int i = 0;
        for (String s : SubredditStorage.alphabeticalSubscriptions) {
            if (!(s.equals("all") || s.equals("frontpage"))) {
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

    public class SaveMulti extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String multiName = title.getText().toString().replace(" ", "").replace("-", "_");
                Pattern validName = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_]{2,20}$");
                Matcher m = validName.matcher(multiName);

                if (!m.matches()) {
                    Log.v("CreateMulti", "Invalid multi name");
                    throw new IllegalArgumentException(multiName);
                }
                if (delete) {
                    Log.v("CreateMulti", "Deleting");
                    new MultiRedditManager(Authentication.reddit).delete(old);

                } else {
                    if (old != null && !old.isEmpty() && !old.replace(" ", "").equals(multiName)) {
                        Log.v("CreateMulti", "Renaming");
                        new MultiRedditManager(Authentication.reddit).rename(old, multiName);
                    }
                    Log.v("CreateMulti", "Create or Update, Name: " + multiName);
                    new MultiRedditManager(Authentication.reddit).createOrUpdate(new MultiRedditUpdateRequest.Builder(Authentication.name, multiName).subreddits(subs).build());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("CreateMulti", "Update Subreddits");
                            new SubredditStorage.SyncMultireddits(CreateMulti.this).execute();
                        }
                    });
                }

            } catch (final NetworkException |  ApiException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CreateMulti.this)
                                .setTitle(R.string.err_title)
                                .setMessage(e instanceof ApiException ? getString(R.string.misc_err) + ": " + ((ApiException)e).getExplanation() + "\n" + getString(R.string.misc_retry): getString(R.string.misc_err))
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
            } catch (  IllegalArgumentException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialogWrapper.Builder(CreateMulti.this)
                                .setTitle(R.string.multireddit_invalid_name)
                                .setMessage(R.string.multireddit_invalid_name_msg)
                                .setNeutralButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                }).create().show();
                    }
                });
            }
            return null;
        }
    }
}
