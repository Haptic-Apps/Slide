package me.ccrama.redditslide.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.ccrama.redditslide.OfflineSubreddit;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.TimeUtils;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class ManageHistory extends BaseActivityAnim  {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_manage_history);
        setupAppBar(R.id.toolbar, R.string.manage_offline_content, true, true);
        SettingsTheme.changed = true;
        findViewById(R.id.clear_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reddit.cachedData.edit().clear().apply();
                finish();
            }
        });
        updateFilters();
    }

    public ArrayList<String> domains = new ArrayList<>();

    public void updateFilters() {
        domains = new ArrayList<>();

        ((LinearLayout) findViewById(R.id.domainlist)).removeAllViews();
        for (final String s : OfflineSubreddit.getAll()) {
            if (!s.isEmpty()) {

                String[] split = s.split(",");

                final String name = "/r/" + split[0] + " → " + ( Long.valueOf(split[1]) == 0?"auto backup":TimeUtils.getTimeAgo(Long.valueOf(split[1]), ManageHistory.this));
                domains.add(name);

                final View t = getLayoutInflater().inflate(R.layout.account_textview, ((LinearLayout) findViewById(R.id.domainlist)), false);

                ((TextView) t.findViewById(R.id.name)).setText(name);
                t.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        domains.remove(name);
                        Reddit.cachedData.edit().remove(s).apply();
                        updateFilters();
                    }
                });
                ((LinearLayout) findViewById(R.id.domainlist)).addView(t);

            }
        }
    }
}