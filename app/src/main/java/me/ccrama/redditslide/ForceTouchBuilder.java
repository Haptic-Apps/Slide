package me.ccrama.redditslide;

import android.app.Dialog;
import android.content.Context;

/**
 * Created by carlo_000 on 1/29/2016.
 */
public class ForceTouchBuilder {
    public Dialog popup;
    public Context c;

    public ForceTouchBuilder(Context c, String url){
        this.c = c;
        popup = new Dialog(c);

        ContentType.ImageType t = ContentType.getImageType(url);

        switch(t){

            case REDDIT:
                break;
            case EMBEDDED:
                break;
            case LINK:
                break;
            case SELF:
                break;
            case ALBUM:
                break;
            case IMAGE:
                break;
            case IMGUR:
                break;
            case GIF:
                break;
            case NONE:
                break;
            case VIDEO:
                break;
            case SPOILER:
                break;
        }
    }
}
