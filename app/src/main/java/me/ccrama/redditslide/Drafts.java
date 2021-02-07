package me.ccrama.redditslide;

import android.content.SharedPreferences;

import java.util.ArrayList;


/**
 * Created by l3d00m on 11/13/2015.
 */
public class Drafts  {

    public static ArrayList<String> getDrafts(){
        ArrayList<String> drafts = new ArrayList<>();
        for(String s : Authentication.authentication.getString(SettingValues.PREF_DRAFTS, "").split("</newdraft>")){
            if(!s.trim().isEmpty()){
                drafts.add(s);
            }
        }
        return drafts;
    }


    public static void addDraft(String s) {
        ArrayList<String> drafts = getDrafts();
        drafts.add(s);
        save(drafts);
    }

    public static void deleteDraft(int position){
        ArrayList<String> drafts = getDrafts();
        drafts.remove(position);
        save(drafts);
    }


    public static void save(ArrayList<String> drafts) {
        SharedPreferences.Editor e = Authentication.authentication.edit();
        e.putString(SettingValues.PREF_DRAFTS, Reddit.arrayToString(drafts, "</newdraft>"));
        e.commit();
    }


}