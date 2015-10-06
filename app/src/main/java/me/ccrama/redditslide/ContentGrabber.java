package me.ccrama.redditslide;

import android.os.AsyncTask;

import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.SubmissionSearchPaginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.UserContributionPaginator;

import java.util.ArrayList;
import java.util.List;

import me.ccrama.redditslide.Adapters.InboxContributions;
import me.ccrama.redditslide.Adapters.SearchPosts;
import me.ccrama.redditslide.Adapters.SubredditPosts;
import me.ccrama.redditslide.Adapters.UserContributions;

/**
 * Created by ccrama on 9/17/2015.
 */
public class ContentGrabber {


    /*Inbox Data*/
    public enum InboxValue{
        INBOX("Inbox"), UNREAD("Unread"), MESSAGES("Messages"), SENT("Sent"), MENTIONS("Mentions");
        String displayName;
         InboxValue(String s){
            this.displayName = s;
        }
        public String getDisplayName(){
            return displayName;
        }
        public String getWhereName(){
            return displayName.toLowerCase();
        }

    }
    public List<String> getInboxValues(){
        ArrayList<String> inboxValues = new ArrayList<>();
        for(InboxValue value : InboxValue.values()){
            inboxValues.add(value.getDisplayName());
        }
        return inboxValues;
    }
    public static InboxContributions grabInbox( final InboxValue value){
        new AsyncTask<Void, Void, InboxContributions>() {

            protected InboxContributions doInBackground(Void... unused) {
               InboxPaginator paginator =  new InboxPaginator(Authentication.reddit, value.getDisplayName().toLowerCase());
                paginator.setSorting(Reddit.defaultSorting);
                if(Reddit.defaultSorting == Sorting.CONTROVERSIAL || Reddit.defaultSorting == Sorting.TOP){
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
               return new InboxContributions(new ArrayList<>(paginator.next()), paginator);
            }

        }.execute();
        return null;

    }

    public static boolean addMessages(final InboxContributions baseData){
        new AsyncTask<Void, Void, Boolean>() {

            protected Boolean doInBackground(Void... unused) {
                try {
                    baseData.addData(baseData.paginator.next());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

        }.execute();
        return false;
    }
    /*Subreddit Data*/

    public static SubredditPosts getPosts(final String subreddit){
        new AsyncTask<Void, Void, SubredditPosts>() {

            protected SubredditPosts doInBackground(Void... unused) {
                SubredditPaginator paginator =  new SubredditPaginator(Authentication.reddit, subreddit);
                paginator.setSorting(Reddit.defaultSorting);
                if(Reddit.defaultSorting == Sorting.CONTROVERSIAL || Reddit.defaultSorting == Sorting.TOP){
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
                return new SubredditPosts(new ArrayList<>(paginator.next()), paginator);
            }

        }.execute();
        return null;

    }

    public static boolean addPosts(final SubredditPosts baseData){
        new AsyncTask<Void, Void, Boolean>() {

            protected Boolean doInBackground(Void... unused) {
                try {
                    baseData.addData(baseData.paginator.next());
                    return true;
                } catch (Exception e) {
                   return false;
                }
            }

        }.execute();
        return false;
    }

     /*Search Posts*/

    public static SearchPosts searchPosts(final String subreddit){
        new AsyncTask<Void, Void, SearchPosts>() {

            protected SearchPosts doInBackground(Void... unused) {
                SubmissionSearchPaginator paginator =  new SubmissionSearchPaginator(Authentication.reddit, subreddit);
                paginator.setSorting(Reddit.defaultSorting);
                if(Reddit.defaultSorting == Sorting.CONTROVERSIAL || Reddit.defaultSorting == Sorting.TOP){
                    paginator.setTimePeriod(Reddit.timePeriod);
                }
                return new SearchPosts(new ArrayList<>(paginator.next()), paginator);
            }

        }.execute();
        return null;

    }

    public static boolean addSearchObjects(final SearchPosts baseData){
        new AsyncTask<Void, Void, Boolean>() {

            protected Boolean doInBackground(Void... unused) {
                try {
                    baseData.addData(baseData.paginator.next());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

        }.execute();
        return false;
    }


    /*User Profile*/

    public enum ProfileType{
        OVERVIEW("Overview"), GILDED("Gilded"), COMMENTS("Comments"), SUBMITTED("Submitted");
        String displayName;
        ProfileType(String s){
            this.displayName = s;
        }
        public String getDisplayName(){
            return displayName;
        }
    }
    public List<String> getProfileValues(){
        ArrayList<String> inboxValues = new ArrayList<>();


        for(ProfileType value : ProfileType.values()){
            inboxValues.add(value.getDisplayName());
        }
        return inboxValues;
    }

    public static UserContributions getContributions(final String username, final ProfileType type){
        new AsyncTask<Void, Void, UserContributions>() {

            protected UserContributions doInBackground(Void... unused) {
                UserContributionPaginator paginator =  new UserContributionPaginator(Authentication.reddit, type.getDisplayName().toLowerCase(), username);

                return new UserContributions(new ArrayList<>(paginator.next()), paginator);
            }

        }.execute();
        return null;

    }

    public static boolean getMoreContributions(final UserContributions baseData){
        new AsyncTask<Void, Void, Boolean>() {

            protected Boolean doInBackground(Void... unused) {
                try {
                    baseData.addData(baseData.paginator.next());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

        }.execute();
        return false;
    }
}
