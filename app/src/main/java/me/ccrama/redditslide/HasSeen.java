package me.ccrama.redditslide;

import me.ccrama.redditslide.Synccit.SynccitRead;

/**
 * Created by ccrama on 7/19/2015.
 */
public class HasSeen {

    public static boolean getSeen(String fullname) {
        if(fullname.contains("t3_")){
            fullname = fullname.substring(fullname.indexOf("t3_" + 3), fullname.length());
        }
        return Reddit.seen.contains(fullname) || SynccitRead.visitedIds.contains(fullname);
    }

    public static void addSeen(String fullname) {
        if(fullname.contains("t3_")){
            fullname = fullname.substring(fullname.indexOf("t3_" + 3), fullname.length());
        }
        Reddit.seen.edit().putBoolean(fullname, false).apply();
        SynccitRead.newVisited.add(fullname);
        SynccitRead.visitedIds.add(fullname);
    }


    public static boolean getHidden(String fullname) {
        return Reddit.hidden.contains(fullname);

    }

    public static void setHidden(String fullname) {
        Reddit.hidden.edit().putBoolean(fullname, false).apply();
    }

    public static void undoHidden(String fullname) {
        Reddit.hidden.edit().remove(fullname).apply();
    }
}
