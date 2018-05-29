package me.ccrama.redditslide;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Deadl on 26/11/2015.
 */
public class SecretConstants {
    private static String apiKey;

    private static String base64EncodedPublicKey;

    public static String getBase64EncodedPublicKey(Context context) {
        if (base64EncodedPublicKey == null) {
            InputStream input;
            try {
                input = context.getAssets().open("secretconstants.properties");
                Properties properties = new Properties();
                properties.load(input);
                base64EncodedPublicKey = properties.getProperty("base64EncodedPublicKey");
            } catch (IOException e) {
                // file not found
                base64EncodedPublicKey = "";
            }

        }
        return base64EncodedPublicKey;
    }

    public static String getApiKey(Context context) {
        if (apiKey == null) {
            InputStream input;
            try {
                input = context.getAssets().open("secretconstants.properties");
                Properties properties = new Properties();
                properties.load(input);
                apiKey = properties.getProperty("apiKey");
            } catch (IOException e) {
                // file not found
                apiKey = "";
            }

        }
        return apiKey;
    }

    public static String getImgurApiKey(Context context) {
        if (apiKey == null) {
            InputStream input;
            try {
                input = context.getAssets().open("secretconstants.properties");
                Properties properties = new Properties();
                properties.load(input);
                apiKey = properties.getProperty("imgur");
            } catch (IOException e) {
                // file not found
                apiKey =
                        "3P3GlZj91emshgWU6YuQL98Q9Zihp1c2vCSjsnOQLIchXPzDLh"; //Testing key, will not work in production
            }

        }
        return apiKey;
    }
}
