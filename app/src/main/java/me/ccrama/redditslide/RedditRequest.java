package me.ccrama.redditslide;

import android.os.AsyncTask;

/**
 * Created by carlo_000 on 2/4/2016.
 */
public class RedditRequest {
    public RedditRequest(final Request toDo) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    toDo.doAsync();
                } catch (Exception e) {
                    if (toDo.onRequestFailed != null)
                        toDo.onRequestFailed(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (toDo.doOnMainThread != null)
                    toDo.doOnMainThread();
            }
        }.execute();
    }

}
