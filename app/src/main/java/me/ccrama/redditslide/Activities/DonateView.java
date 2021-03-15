package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.rey.material.widget.Slider;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.billing.IabHelper;
import me.ccrama.redditslide.util.billing.IabResult;
import me.ccrama.redditslide.util.billing.Inventory;
import me.ccrama.redditslide.util.billing.Purchase;
import me.ccrama.redditslide.util.billing.SkuDetails;


/**
 * Created by carlo_000 on 5/26/2015.
 * Allows a user to donate to Slide using Google Play's IabHelper
 */
public class DonateView extends BaseActivityAnim {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        disableSwipeBackLayout();
        super.onCreate(savedInstanceState);

        applyColorTheme();
        setContentView(R.layout.activity_donate);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setTitle(R.string.settings_title_support);
        setRecentBar(getString(R.string.settings_title_support), Palette.getDarkerColor(
                ContextCompat.getColor(DonateView.this, R.color.md_light_green_500)));

        setSupportActionBar(t);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(Palette.getDarkerColor(
                    ContextCompat.getColor(DonateView.this, R.color.md_light_green_500)));
            if (SettingValues.colorNavBar) {
                window.setNavigationBarColor(Palette.getDarkerColor(
                        ContextCompat.getColor(DonateView.this, R.color.md_light_green_500)));
            }
        }

        final Slider slider = (Slider) findViewById(R.id.slider_sl_discrete);
        slider.setValue(4, false);
        final TextView ads = (TextView) findViewById(R.id.ads);
        final TextView hours = (TextView) findViewById(R.id.hours);
        final TextView money = (TextView) findViewById(R.id.money);

        slider.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos,
                    int oldValue, int newValue) {
                ads.setText(" " + newValue * 330 + " ");
                hours.setText(" " + (double) newValue / 10 + " ");
                money.setText("$" + newValue);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ads.setText(" " + 4 * 330 + " ");
        hours.setText(" " + (double) 4 / 10 + " ");
        money.setText("$" + 4);

        findViewById(R.id.donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = "";
                if (Authentication.isLoggedIn) {
                    name = Authentication.name;
                }
                if (Reddit.mHelper != null) {
                    Reddit.mHelper.flagEndAsync();
                }
                Reddit.mHelper.queryInventoryAsync(new IabHelper.QueryInventoryFinishedListener() {
                    @Override
                    public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                        if(inv != null) {
                                    SkuDetails donation = inv.getSkuDetails("donation_" + slider.getValue());
                            LogUtil.v("Trying to get donation_" + slider.getValue());
                            if (donation != null) {
                                LogUtil.v("Not null");
                                Reddit.mHelper.launchPurchaseFlow(DonateView.this, donation.getSku(),
                                        4000, new IabHelper.OnIabPurchaseFinishedListener() {
                                            @Override
                                            public void onIabPurchaseFinished(IabResult result,
                                                    Purchase info) {
                                                if(result.isSuccess()){
                                                    new AlertDialog.Builder(DonateView.this).setTitle("Thank you!").setMessage("Thank you very much for your support :)").setPositiveButton(R.string.btn_done, null).show();
                                                } else {
                                                    new AlertDialog.Builder(DonateView.this).setTitle("Uh oh, something went wrong.").setMessage("Please try again soon! Sorry for the inconvenience.").setPositiveButton("Ok", null).show();
                                                }
                                            }
                                        });
                            } else {
                                LogUtil.v("Null");
                            }
                        } else {
                            new AlertDialog.Builder(DonateView.this).setTitle("Uh oh, something went wrong.").setMessage("Please try again soon! Sorry for the inconvenience.").setPositiveButton("Ok", null).show();
                        }
                    }
                });
            }
        });
    }
}
