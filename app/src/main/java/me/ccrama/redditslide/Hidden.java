package me.ccrama.redditslide;

import android.content.SharedPreferences;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.Submission;

/**
 * Created by carlo_000 on 10/16/2015.
 */
public class Hidden {
    public static SharedPreferences hidden;
    public static boolean isHidden(Submission s){
        return hidden.contains(s.getFullName());
    }
    public static void setHidden(Contribution s){
        hidden.edit().putBoolean(s.getFullName(), true).apply();
    }
    public static void undoHidden(Contribution s){
        hidden.edit().remove(s.getFullName()).apply();
    }

}
