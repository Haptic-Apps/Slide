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

    public UserContributions(ArrayList<Contribution> firstData, UserContributionPaginator paginator) {
        posts = firstData;
        UserContributionPaginator paginator1 = paginator;
    }

    public void addData(List<Contribution> data) {
        posts.addAll(data);
    }
}
