package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccrama on 9/17/2015.
 */
class UserContributions {
    private final ArrayList<Contribution> posts;
    private final UserContributionPaginator paginator;
    public UserContributions(ArrayList<Contribution> firstData, UserContributionPaginator paginator){
        posts = firstData;
        this.paginator = paginator;
    }
    public void addData(List<Contribution> data){
        posts.addAll(data);
    }
}
