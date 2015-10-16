package me.ccrama.redditslide.Visuals;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import me.ccrama.redditslide.ContentType;
import me.ccrama.redditslide.R;

public class StyleView {
    public static void styleActivity(Activity a){
        Pallete p = Pallete.getDefaultPallete();
        a.findViewById(R.id.contentView).setBackgroundColor(p.backgroundColor);
    }

    public static void styleCard(Pallete subredditPallete, View v) {
        int fontColor = subredditPallete.theme.getFontColor();
        ((TextView)v.findViewById(R.id.title)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.subreddit)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.information)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.contenttitle)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.contenturl)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.comments)).setTextColor(fontColor);
        ((TextView)v.findViewById(R.id.score)).setTextColor(fontColor);

        ((CardView)v.findViewById(R.id.card)).setCardBackgroundColor(subredditPallete.theme.getCardBackgroundColor());

    }
    public static void setPartsofCard(View v, String url){
        ContentType.ImageType t = ContentType.getImageType(url);

    }
}
