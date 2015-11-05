package me.ccrama.redditslide;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Arrays;

import me.ccrama.redditslide.Activities.CommentsScreenSingle;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.SubredditView;

/**
 * Created by ccrama on 9/27/2015.
 */
public class OpenRedditLink {
    public OpenRedditLink(Context c, String url){

        boolean np = url.contains("np");
        url = url.replace("np.","");
        url = url.replace("www.","");
        url = url.replace("http://", "");
        url = url.replace("https://", "");
        url = url.substring(url.indexOf("redd"), url.length());

        if(url.startsWith("/")){
            url = "reddit.com" + url;
        }
        Log.v("Slide", url);
        if(url.endsWith("/")){
            url = url.substring(0, url.length() - 1);

        }
       url =  url.replace("//", "/");

        String[] parts = url.split("/");
        if(parts[parts.length - 1].startsWith("?")){
            parts = Arrays.copyOf(parts, parts.length - 1);


        }
        if (((parts.length == 3 && !url.contains("/u/") && ! url.contains("/user/")) )  || url.contains("search?")) {
            Intent intent = new Intent(c, SubredditView.class);
            intent.putExtra("subreddit", parts[2]);
            c.startActivity(intent);
        } else if (url.contains("/u/") || url.contains("/user/")) {
            Intent myIntent = new Intent(c, Profile.class);
            myIntent.putExtra("profile", parts[2]);
            c.startActivity(myIntent);
        } else if (url.contains("reddit.com") || url.contains("redd.it")) {
            if (parts.length == 7) {


                Intent i = new Intent(c, CommentsScreenSingle.class);
                i.putExtra("subreddit", parts[2]);
                i.putExtra("submission", parts[4]);
                i.putExtra("np", np);
                i.putExtra("loadmore", true);
                String end = parts[6];
                if(end.contains("?")){
                    end = end.substring(0, end.indexOf("?"));
                }
                i.putExtra("context",  end);
                c.startActivity(i);

            } else if(parts.length == 2) {
                Intent i = new Intent(c, CommentsScreenSingle.class);
                i.putExtra("subreddit", "NOTHING");
                i.putExtra("context",  "NOTHING");
                i.putExtra("np", np);

                i.putExtra("submission",  parts[1]);
                c.startActivity(i);

            } else {


                Intent i = new Intent(c, CommentsScreenSingle.class);
                i.putExtra("subreddit", parts[2]);
                i.putExtra("context",  "NOTHING");
                i.putExtra("np", np);
                i.putExtra("submission",  parts[4]);
                c.startActivity(i);

            }
        }


    }
    public OpenRedditLink(Context c, String submission, String subreddit, String id){


                Intent i = new Intent(c, CommentsScreenSingle.class);
                i.putExtra("subreddit", subreddit);
                i.putExtra("context",id);
                i.putExtra("submission",  submission);
                c.startActivity(i);





    }
}
