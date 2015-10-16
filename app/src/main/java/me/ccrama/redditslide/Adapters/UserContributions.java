package me.ccrama.redditslide.Adapters;

import net.dean.jraw.models.Contribution;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

public class UserContributions {
    public ArrayList<Contribution> posts;
    public UserContributionPaginator paginator;
    public UserContributions(ArrayList<Contribution> firstData, UserContributionPaginator paginator){
        posts = firstData;
        this.paginator = paginator;
    }
    public void addData(List<Contribution> data){
        posts.addAll(data);
    }
}
