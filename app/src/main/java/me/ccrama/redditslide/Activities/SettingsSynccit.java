package me.ccrama.redditslide.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Synccit.MySynccitReadTask;
import me.ccrama.redditslide.Synccit.MySynccitUpdateTask;
import me.ccrama.redditslide.Synccit.SynccitRead;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class SettingsSynccit extends BaseActivityAnim {


    EditText name;
    EditText auth;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_synccit);
        setupAppBar(R.id.toolbar, "Synccit Integration", true, true);


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
                if (SettingValues.synccitAuth.isEmpty()) {

                    new AlertDialogWrapper.Builder(SettingsSynccit.this)
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
                        .cancelable(false)
                        .show();
                try {
                    SettingValues.synccitName = name.getText().toString();
                    SettingValues.synccitAuth = auth.getText().toString();


                    new MySynccitUpdateTask().execute("16noez");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            new MySynccitReadTask().execute("16noez");
                            if (SynccitRead.visitedIds.contains("16noez")) {
                                //success

                                d.dismiss();
                                SharedPreferences.Editor e = SettingValues.prefs.edit();

                                e.putString(SettingValues.SYNCCIT_NAME, SettingValues.synccitName);
                                e.putString(SettingValues.SYNCCIT_AUTH, SettingValues.synccitAuth);
                                e.apply();
                                (findViewById(R.id.remove)).setEnabled(true);

                                new AlertDialogWrapper.Builder(SettingsSynccit.this)
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

                                new AlertDialogWrapper.Builder(SettingsSynccit.this)
                                        .setTitle(R.string.settings_synccit_failed)
                                        .setMessage(R.string.settings_synccit_failed_msg)
                                        .setPositiveButton(R.string.btn_ok, null).show();
                            }
                        }
                    }, 3000);

                } catch (Exception e) {
                    d.dismiss();

                    new AlertDialogWrapper.Builder(SettingsSynccit.this)
                            .setTitle(R.string.settings_synccit_failed)
                            .setMessage(R.string.settings_synccit_failed_msg)
                            .setPositiveButton(R.string.btn_ok, null).show();

                }


            }
        });

    }


}