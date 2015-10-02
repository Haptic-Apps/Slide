package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;

/**
 * Created by carlo_000 on 10/2/2015.
 */
public class Shortcut extends Activity {
    String name = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // The meat of our shortcut
            final Intent shortcutIntent = new Intent(this, OpenContent.class);


            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Pick a Subreddit");
            builder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, SubredditStorage.alphabeticalSubscriptions), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    name = SubredditStorage.alphabeticalSubscriptions.get(which);
                    shortcutIntent.putExtra("url" ,"reddit.com/r/" + name);


                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "/r/" + name);

                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(Shortcut.this, R.mipmap.ic_launcher));
                    setResult(RESULT_OK, intent);

                    finish();
                }
            });

            AlertDialog alert = builder.create();

            alert.show();
            // The result we are passing back from this activity

        }


}
