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
            InputStream input = null;
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
            if (BuildConfig.DEBUG) {
                apiKey = "UNDEFINED";
            } else {
                InputStream input = null;
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
        }
        return apiKey;
    }
}
