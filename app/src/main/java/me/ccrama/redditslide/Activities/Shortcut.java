package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.AlertDialogWrapper;

import me.ccrama.redditslide.Adapters.SubredditListingAdapter;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.Visuals.Pallete;

/**
 * Created by ccrama on 10/2/2015.
 */
public class Shortcut extends Activity {
    String name = "";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // The meat of our shortcut


            if(SubredditStorage.alphabeticalSubscriptions == null){
                SubredditStorage.shortcut = this;
            } else {
                doShortcut();
            }

            // The result we are passing back from this activity

        }
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public void doShortcut(){








        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        final Intent shortcutIntent = new Intent(Shortcut.this, OpenContent.class);

                        SubredditStorage.shortcut = null;
                        AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(Shortcut.this);

                        builder.setTitle("Pick a Subreddit");
                        builder.setAdapter(new SubredditListingAdapter(Shortcut.this, SubredditStorage.alphabeticalSubscriptions), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                name = SubredditStorage.alphabeticalSubscriptions.get(which);
                                final Bitmap src;
                                final Bitmap bm2;
                                if (name.toLowerCase().equals("androidcirclejerk")) {
                                    bm2 = drawableToBitmap(getResources().getDrawable(R.drawable.matiasduarte));
                                    Log.v("Slide", "NULL IS " + (bm2 == null));
                                } else {
                                     src = drawableToBitmap(getResources().getDrawable(R.mipmap.blackandwhite));
                                    final int overlayColor = Pallete.getColor(name);
                                    final Paint paint = new Paint();
                                    Canvas c;
                                    final Bitmap bm1 = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
                                    c = new Canvas(bm1);
                                    paint.setColorFilter(new PorterDuffColorFilter(overlayColor, PorterDuff.Mode.OVERLAY));
                                    c.drawBitmap(src, 0, 0, paint);

                                    bm2 = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
                                    c = new Canvas(bm2);
                                    paint.setColorFilter(new PorterDuffColorFilter(overlayColor, PorterDuff.Mode.SRC_ATOP));
                                    c.drawBitmap(src, 0, 0, paint);

                                    paint.setColorFilter(null);
                                    paint.setXfermode(new AvoidXfermode(overlayColor, 0, AvoidXfermode.Mode.TARGET));
                                    c.drawBitmap(bm1, 0, 0, paint);
                                }



                                shortcutIntent.putExtra("url", "reddit.com/r/" + name);
                                Intent intent = new Intent();
                                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "/r/" + name);
                                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bm2);
                                setResult(RESULT_OK, intent);

                                finish();
                            }
                        });

                        builder.create().show();
                    }
                }
        );

    }


}
