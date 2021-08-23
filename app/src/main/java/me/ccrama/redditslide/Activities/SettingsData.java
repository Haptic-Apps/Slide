package me.ccrama.redditslide.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import org.mp4parser.muxer.Edit;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Fragments.SettingsDataFragment;
import me.ccrama.redditslide.R;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsData extends BaseActivityAnim {

    private SettingsDataFragment fragment = new SettingsDataFragment(this);
    SharedPreferences preferences;
    EditText dataLimit;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_datasaving);
        setupAppBar(R.id.toolbar, R.string.settings_data, true, true);

        ((ViewGroup) findViewById(R.id.settings_datasaving)).addView(
                getLayoutInflater().inflate(R.layout.activity_settings_datasaving_child, null));

        fragment.Bind();

        View lin = findViewById(R.id.myLayout);    //initialise the layout
        dataLimit = (EditText)lin.findViewById(R.id.settings_dataAlertLimit);  //use the layout to access the widgets
        preferences = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        String dlimit = preferences.getString("limit", String.valueOf(R.string.settings_dataLimit_default));
        dataLimit.setText(dlimit);    //updates the value to the old one
    }

    //edits the shared preferences variable to store the current data limit value
    public void setDataLimit(View view) {
        if(dataLimit.getText()!=null && dataLimit.getText().toString().length()!=0){
            String limit = dataLimit.getText().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("limit",limit);
            editor.commit();
            Toast.makeText(SettingsData.this, R.string.multi_saved_successfully,Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(SettingsData.this, R.string.settings_dataLimit_default,Toast.LENGTH_SHORT).show();
        }
    }

    //returns the current data limit set
    public int getDataLimit(){
        String dlimit = preferences.getString("limit","");
        if(dlimit.length()==0){
            return -1;
        }else{
            return Integer.parseInt(dlimit);
        }
    }

}
