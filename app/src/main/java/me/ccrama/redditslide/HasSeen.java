package me.ccrama.redditslide;

/**
 * Created by ccrama on 7/19/2015.
 */
public class HasSeen {

    public static boolean getSeen(String fullname){
        return Reddit.seen.contains(fullname);
    }

    public static void addSeen(String fullname){
        Reddit.seen.edit().putBoolean(fullname, false).apply();
    }


    public static boolean getHidden(String fullname){
        return Reddit.hidden.contains(fullname);

    }
    public static void setHidden(String fullname){
        Reddit.hidden.edit().putBoolean(fullname, false).apply();
    }

    public static void undoHidden(String fullname){
        Reddit.hidden.edit().remove(fullname).apply();
    }
}
