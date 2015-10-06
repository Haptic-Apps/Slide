package me.ccrama.redditslide.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.ColorPreferences;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Views.CreateCardView;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by carlo_000 on 9/17/2015.
 */
public class EditCardsLayout extends BaseActivity {

    TabLayout tabs;
    ViewPager pager;
    String subreddit;
    @Override
    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        if(getIntent() != null && getIntent().hasExtra("subreddit")){
            subreddit = getIntent().getExtras().getString("subreddit","");
        } else {
            subreddit = "";
        }
        getTheme().applyStyle(new ColorPreferences(this).getThemeSubreddit(subreddit, true).getBaseId(), true);
        getTheme().applyStyle(new FontPreferences(this).getFontStyle().getResId(), true);
               setContentView(R.layout.activity_editcards);

        findViewById(R.id.toolbar).setBackgroundColor(Pallete.getColor(subreddit));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Pallete.getDarkerColor(Pallete.getColor(subreddit)));
        }
       final LinearLayout layout = (LinearLayout)findViewById(R.id.card);
        layout.removeAllViews();
        layout.addView(CreateCardView.CreateView(layout));
       final CheckBox largeimage = (CheckBox) findViewById(R.id.flargeimage);
        final CheckBox actionbar = (CheckBox) findViewById(R.id.fabar);
        final CheckBox infobar = (CheckBox) findViewById(R.id.finfo);

        largeimage.setChecked(CreateCardView.isLarge(subreddit));
        actionbar.setChecked(CreateCardView.isActionBar(subreddit));
        infobar.setChecked(CreateCardView.isInfoBar(subreddit));

        largeimage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setLargeThumbnails(isChecked, layout, subreddit));

            }
        });

        actionbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setActionBarVisible(isChecked, layout, subreddit));

            }
        });

        infobar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layout.removeAllViews();
                layout.addView(CreateCardView.setInfoBarVisible(isChecked, layout, subreddit));

            }
        });
        final List<String> list=new ArrayList<String>();
        list.add("Big Card");
        list.add("Small Card");
        list.add("List");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Spinner sp=(Spinner) toolbar.findViewById(R.id.spinner_nav);

        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, subreddit);
                CreateCardView.setActionBarVisible(true, layout, subreddit);
                CreateCardView.setInfoBarVisible(true, layout, subreddit);
                layout.removeAllViews();
                layout.addView(CreateCardView.setLargeThumbnails(true, layout, subreddit));
                largeimage.setChecked(CreateCardView.isLarge(subreddit));
                actionbar.setChecked(CreateCardView.isActionBar(subreddit));
                infobar.setChecked(CreateCardView.isInfoBar(subreddit));
                int chosen = list.indexOf(CreateCardView.getCardView(subreddit).getDisplayName());
                sp.setSelection(chosen);
            }
        });

        ArrayAdapter<String> adp= new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,list);
        adp.setDropDownViewResource(R.layout.spinneritem);
        sp.setAdapter(adp);

        int chosen = list.indexOf(CreateCardView.getCardView(subreddit).getDisplayName());
        sp.setSelection(chosen);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int pos, long arg3) {

              layout.removeAllViews();
                switch (pos) {
                    case 0:
                        layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LARGE, layout, subreddit));
                        break;
                    case 1:
                        //RISING
                        layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.SMALL, layout, subreddit));

                        break;
                    case 2:
                        //CONT
                        layout.addView(CreateCardView.setCardViewType(CreateCardView.CardEnum.LIST, layout, subreddit));

                        break;

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


    }


}
