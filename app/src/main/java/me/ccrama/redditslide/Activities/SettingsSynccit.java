package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask;
import me.ccrama.redditslide.Synccit.SynccitRead;


/**
 * Created by ccrama on 2/16/2015.
 */
public class SettingsSynccit extends BaseActivityAnim {


    EditText name;
    EditText auth;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_synccit);
        setupAppBar(R.id.toolbar, R.string.settings_synccit, true, true);


        name = (EditText) findViewById(R.id.name);
        auth = (EditText) findViewById(R.id.auth);

        name.setText(SettingValues.synccitName);
        auth.setText(SettingValues.synccitAuth);

        if (SettingValues.synccitAuth.isEmpty()) {
            (findViewById(R.id.remove)).setEnabled(false);
        }
        findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SettingValues.synccitAuth.isEmpty()) {

                    new AlertDialog.Builder(SettingsSynccit.this)
                            .setTitle(R.string.settings_synccit_delete)
                            .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SettingValues.synccitName = "";
                                    SettingValues.synccitAuth = "";
                                    SharedPreferences.Editor e = SettingValues.prefs.edit();

                                    e.putString(SettingValues.SYNCCIT_NAME, SettingValues.synccitName);
                                    e.putString(SettingValues.SYNCCIT_AUTH, SettingValues.synccitAuth);
                                    e.apply();
                                    name.setText(SettingValues.synccitName);
                                    auth.setText(SettingValues.synccitAuth);
                                    SynccitRead.visitedIds.removeAll(Collections.singleton("16noez"));
                                }
                            }).setNegativeButton(R.string.btn_no, null)
                            .show();
                }
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog d = new MaterialDialog.Builder(SettingsSynccit.this)
                        .title(R.string.settings_synccit_authenticate)
                        .progress(true, 100)
                        .content(R.string.misc_please_wait)
                        .cancelable(false)
                        .show();
                new MySynccitUpdateTask().execute("16noez");
                SettingValues.synccitName = name.getText().toString();
                SettingValues.synccitAuth = auth.getText().toString();
                try {
                    new MySynccitReadTask().execute("16noez").get();
                    if (SynccitRead.visitedIds.contains("16noez")) {
                                //success
                                d.dismiss();
                                SharedPreferences.Editor e = SettingValues.prefs.edit();

                                e.putString(SettingValues.SYNCCIT_NAME, SettingValues.synccitName);
                                e.putString(SettingValues.SYNCCIT_AUTH, SettingValues.synccitAuth);
                                e.apply();
                                (findViewById(R.id.remove)).setEnabled(true);

                                new AlertDialog.Builder(SettingsSynccit.this)
                                        .setTitle(R.string.settings_synccit_connected)
                                        .setMessage(R.string.settings_synccit_active)
                                        .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        finish();
                                    }
                                }).show();
                            } else {
                                d.dismiss();

                                new AlertDialog.Builder(SettingsSynccit.this)
                                        .setTitle(R.string.settings_synccit_failed)
                                        .setMessage(R.string.settings_synccit_failed_msg)
                                        .setPositiveButton(R.string.btn_ok, null).show();
                            }
                } catch (Exception e) {
                    d.dismiss();

                    new AlertDialog.Builder(SettingsSynccit.this)
                            .setTitle(R.string.settings_synccit_failed)
                            .setMessage(R.string.settings_synccit_failed_msg)
                            .setPositiveButton(R.string.btn_ok, null).show();

                }


            }
        });

    }


}
