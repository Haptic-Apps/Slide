package me.ccrama.redditslide.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.rey.material.widget.Slider;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;
import me.ccrama.redditslide.util.IabHelper;
import me.ccrama.redditslide.util.IabResult;
import me.ccrama.redditslide.util.Purchase;


/**
 * Created by carlo_000 on 5/26/2015.
 */
public class DonateView extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(""), true);

        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
        setContentView(R.layout.activity_donate);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setTitle("Support Slide for Reddit");

        setSupportActionBar(t);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Pallete.getDarkerColor(getResources().getColor(R.color.md_light_green_500)));
        }
        final Slider sl_discrete = (Slider) findViewById(R.id.slider_sl_discrete);
        final TextView ads = (TextView) findViewById(R.id.ads);
        final TextView hours = (TextView) findViewById(R.id.hours);
        final TextView money = (TextView) findViewById(R.id.money);

        ads.setText( sl_discrete.getValue() * 330 + "");
        hours.setText(String.valueOf((double) 10 / sl_discrete.getValue() ) + "");
        money.setText("$" + sl_discrete.getValue() );


        sl_discrete.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                ads.setText( newValue * 330 + "");
                hours.setText(String.valueOf((double) newValue / 10 ) + "");
                money.setText("$" + newValue);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        findViewById(R.id.donate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = "";
                if(Authentication.isLoggedIn){
                    name = Authentication.name;
                }
                if(Reddit.mHelper != null)
                Reddit.mHelper.flagEndAsync();
                Reddit.mHelper.launchPurchaseFlow(DonateView.this, "donation_" + sl_discrete.getValue(),1, mPurchaseFinishedListener, name);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {

            if (result.isFailure()) {
                Log.d("Slide", "Error purchasing: " + result);
                AlertDialog.Builder builder = new AlertDialog.Builder(DonateView.this);
                builder.setTitle("Donation unsuccessful :(");
                builder.setMessage("Unfortunately, the donation did not go through. Make sure you are connected to the internet and try again! Thank you for your support!");
                builder.setNeutralButton("Ok", null);
                builder.show();
            }
            else if (purchase.getSku().contains("donation")) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DonateView.this);
                        builder.setTitle("Donation successful!");
                        builder.setMessage("Thank you very much for your support of Slide for Reddit! It really means a lot to me. If you encounter any issues, shoot me an email or post to /r/slideforreddit anytime!");
                        builder.setPositiveButton("You're welcome!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               DonateView.this.finish();
                            }
                        });

                        builder.show();
                    }
                });
            }

        }
    };
}