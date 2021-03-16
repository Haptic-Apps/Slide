package me.ccrama.redditslide.util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by TacoTheDank on 03/15/2021.
 */
public class StringUtil {
    public static String arrayToString(final ArrayList<String> array) {
        return arrayToStringInternal(array, ",", 1);
    }

    public static String arrayToString(final ArrayList<String> array, final String separator) {
        return arrayToStringInternal(array, separator, separator.length());
    }

    private static String arrayToStringInternal(final ArrayList<String> array,
                                                final String separator, final int separatorLength) {
        if (array != null) {
            final StringBuilder b = new StringBuilder();
            for (String s : array) {
                b.append(s).append(separator);
            }
            String f = b.toString();
            if (f.length() > 0) {
                f = f.substring(0, f.length() - separatorLength);
            }
            return f;
        }
        return "";
    }

    public static ArrayList<String> stringToArray(final String string) {
        final ArrayList<String> f = new ArrayList<>();
        Collections.addAll(f, string.split(","));
        return f;
    }
}
