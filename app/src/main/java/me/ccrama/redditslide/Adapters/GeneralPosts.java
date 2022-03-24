package me.ccrama.redditslide.Adapters;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import net.dean.jraw.models.Contribution;

import java.util.ArrayList;

/**
 * Created by carlo_000 on 12/3/2015.
 */
public class GeneralPosts {
    public ArrayList<Contribution> posts ;
    public boolean nomore;
    public SwipeRefreshLayout refreshLayout;
}
