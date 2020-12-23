package me.ccrama.redditslide;

/**
 * Created by carlo_000 on 10/19/2015.
 */
public class SanitizeField {
    public static String sanitizeString(String input) {
        char[] allowed = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();
        char[] charArray = input.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : charArray) {
            for (char a : allowed) {
                if (c == a) result.append(a);
            }
        }
        return result.toString();
    }
}
