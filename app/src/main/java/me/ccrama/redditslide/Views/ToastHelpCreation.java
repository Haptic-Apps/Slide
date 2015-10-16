package me.ccrama.redditslide.Views;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class ToastHelpCreation {
    public static void makeToast(View view, String message, Context context){

        int x = view.getLeft();
        int y = view.getTop() + 2*view.getHeight();
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP|Gravity.LEFT, x, y);
        toast.show();
    }
}
