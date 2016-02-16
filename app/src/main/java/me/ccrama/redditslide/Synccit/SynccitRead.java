package me.ccrama.redditslide.Synccit;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by carlo_000 on 2/16/2016.
 */
public class SynccitRead {
    public static ArrayList<String> visitedIds = new ArrayList<>();
    public static ArrayList<String> newVisited = new ArrayList<>();

    static void setVisited(Set<String> visitedIds2) {
        visitedIds = new ArrayList<>();
        for(String s : visitedIds2) {
            visitedIds.add(s);
        }
    }
}
