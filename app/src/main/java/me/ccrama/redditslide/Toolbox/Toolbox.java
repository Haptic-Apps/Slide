package me.ccrama.redditslide.Toolbox;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import net.dean.jraw.ApiException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.WikiManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Reddit;

/**
 * Main class for /r/toolbox functionality
 */
public class Toolbox {
    public static final Map<String, Map<String, String>> DEFAULT_USERNOTE_TYPES = new HashMap<>();
    private static final long CACHE_TIME_NONEXISTANT = 604800000; // 7 days
    private static final long CACHE_TIME_CONFIG = 86400000; // 24 hours
    private static final long CACHE_TIME_USERNOTES = 3600000; // 1 hour

    private static Map<String, Usernotes> notes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static Map<String, ToolboxConfig> toolboxConfigs = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static SharedPreferences cache = Reddit.getAppContext().getSharedPreferences("toolbox_cache", 0);

    static { // Set the default usernote types. Yes this is ugly but java sucks for stuff like this.
        Map<String, String> goodUser = new HashMap<>();
        goodUser.put("color", "#008000");
        goodUser.put("text", "Good\u00A0Contributor");

        Map<String, String> spamWatch = new HashMap<>();
        spamWatch.put("color", "#ff00ff");
        spamWatch.put("text", "Spam\u00A0Watch");

        Map<String, String> spamWarn = new HashMap<>();
        spamWarn.put("color", "#800080");
        spamWarn.put("text", "Spam\u00A0Warning");

        Map<String, String> abuseWarn = new HashMap<>();
        abuseWarn.put("color", "#ffa500");
        abuseWarn.put("text", "Abuse\u00A0Warning");

        Map<String, String> ban = new HashMap<>();
        ban.put("color", "#ff0000");
        ban.put("text", "Ban");

        Map<String, String> permBan = new HashMap<>();
        permBan.put("color", "#8b0000");
        permBan.put("text", "Permanent\u00A0Ban");

        Map<String, String> botBan = new HashMap<>();
        botBan.put("color", "#000000");
        botBan.put("text", "Bot\u00A0Ban");

        Toolbox.DEFAULT_USERNOTE_TYPES.put("gooduser", goodUser);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("spamwatch", spamWatch);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("spamwarn", spamWarn);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("abusewarn", abuseWarn);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("ban", ban);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("permban", permBan);
        Toolbox.DEFAULT_USERNOTE_TYPES.put("botban", botBan);
    }

    /**
     * Gets a subreddit's usernotes if we have loaded them
     *
     * @param subreddit Sub to get notes for
     * @return Usernotes
     */
    public static Usernotes getUsernotes(String subreddit) {
        return notes.get(subreddit);
    }

    /**
     * Gets a subreddit's toolbox config if we have loaded it
     *
     * @param subreddit Sub to get config fore
     * @return Toolbox config
     */
    public static ToolboxConfig getConfig(String subreddit) {
        return toolboxConfigs.get(subreddit);
    }

    public static void createUsernotes(String subreddit) {
        notes.put(subreddit, new Usernotes(6, new Usernotes.UsernotesConstants(new String[] {}, new String[] {}),
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER), subreddit));
    }

    /**
     * Ensures that a subreddit's config is cached
     *
     * @param subreddit Subreddit to cache
     */
    public static void ensureConfigCachedLoaded(String subreddit) {
        ensureConfigCachedLoaded(subreddit, true);
    }

    /**
     * Ensures that a subreddit's config is cached
     *
     * @param subreddit Subreddit to cache
     * @param thorough Whether to reload from net/cache if toolboxConfigs already contains something for subreddit
     */
    public static void ensureConfigCachedLoaded(String subreddit, boolean thorough) {
        if (!thorough && toolboxConfigs.containsKey(subreddit)) {
            return;
        }
        long lastCached = cache.getLong(subreddit + "_config_timestamp", -1);
        boolean exists = cache.getBoolean(subreddit + "_config_exists", true);
        if ((!exists && System.currentTimeMillis() - lastCached > CACHE_TIME_NONEXISTANT) // Sub doesn't have config
                || System.currentTimeMillis() - lastCached > CACHE_TIME_CONFIG // Config outdated
                || lastCached == -1) { // Config not cached
            new AsyncLoadToolboxConfig().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subreddit);
        } else {
            Gson gson = new Gson();
            try {
                ToolboxConfig result = gson.fromJson(cache.getString(subreddit + "_config_data", null),
                        ToolboxConfig.class);
                if (result != null && result.getSchema() == 1) {
                    toolboxConfigs.put(subreddit, result);
                }
            } catch (JsonParseException e) { // cached config was invalid
                new AsyncLoadToolboxConfig().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subreddit);
            }
        }
    }

    /**
     * Ensures that a subreddit's usernotes are cached
     *
     * @param subreddit Subreddit to cache
     */
    public static void ensureUsernotesCachedLoaded(String subreddit) {
        ensureUsernotesCachedLoaded(subreddit, true);
    }

    /**
     * Ensures that a subreddit's usernotes are cached
     *
     * @param subreddit Subreddit to cache
     * @param thorough Whether to reload from net/cache if notes already contains something for subreddit
     */
    public static void ensureUsernotesCachedLoaded(String subreddit, boolean thorough) {
        if (!thorough && notes.containsKey(subreddit)) {
            return;
        }
        long lastCached = cache.getLong(subreddit + "_usernotes_timestamp", -1);
        boolean exists = cache.getBoolean(subreddit + "_usernotes_exists", true);
        if ((!exists && System.currentTimeMillis() - lastCached > CACHE_TIME_NONEXISTANT) // Sub doesn't have usernotes
                || System.currentTimeMillis() - lastCached > CACHE_TIME_USERNOTES // Usernotes outdated
                || lastCached == -1) { // Usernotes not cached
            new AsyncLoadUsernotes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subreddit);
        } else {
            Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, List<Usernote>>>() {}.getType(),
                    new Usernotes.BlobDeserializer()).create();
            try {
                Usernotes result = gson.fromJson(cache.getString(subreddit + "_usernotes_data", null), Usernotes.class);
                if (result != null && result.getSchema() == 6) {
                    result.setSubreddit(subreddit);
                    notes.put(subreddit, result);
                } else {
                    notes.remove(subreddit);
                }
            } catch (JsonParseException e) { // cached usernotes were invalid
                new AsyncLoadUsernotes().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subreddit);
            }
        }
    }

    /**
     * Download a subreddit's usernotes
     *
     * @param subreddit
     */
    public static void downloadUsernotes(String subreddit) {
        WikiManager manager = new WikiManager(Authentication.reddit);
        Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, List<Usernote>>>() {}.getType(),
                new Usernotes.BlobDeserializer()).create();
        try {
            String data = manager.get(subreddit, "usernotes").getContent();
            Usernotes result = gson.fromJson(data, Usernotes.class);
            cache.edit().putLong(subreddit + "_usernotes_timestamp", System.currentTimeMillis()).apply();
            if (result != null && result.getSchema() == 6) {
                result.setSubreddit(subreddit);
                notes.put(subreddit, result);
                cache.edit().putBoolean(subreddit + "_usernotes_exists", true)
                        .putString(subreddit + "_usernotes_data", data).apply();
            } else {
                cache.edit().putBoolean(subreddit + "_usernotes_exists", false).apply();
            }
        } catch (NetworkException | JsonParseException e) {
            if (e instanceof JsonParseException) {
                notes.remove(subreddit);
            }
            cache.edit().putLong(subreddit + "_usernotes_timestamp", System.currentTimeMillis())
                    .putBoolean(subreddit + "_usernotes_exists", false).apply();
        }
    }

    /**
     * Download a subreddit's Toolbox config
     *
     * @param subreddit
     */
    public static void downloadToolboxConfig(String subreddit) {
        WikiManager manager = new WikiManager(Authentication.reddit);
        Gson gson = new Gson();
        try {
            String data = manager.get(subreddit, "toolbox").getContent();
            ToolboxConfig result = gson.fromJson(data, ToolboxConfig.class);
            cache.edit().putLong(subreddit + "_config_timestamp", System.currentTimeMillis()).apply();
            if (result != null && result.getSchema() == 1) {
                toolboxConfigs.put(subreddit, result);
                cache.edit().putBoolean(subreddit + "_config_exists", true)
                        .putString(subreddit + "_config_data", data)
                        .apply();
            } else {
                cache.edit().putBoolean(subreddit + "_config_exists", false).apply();
            }
        } catch (NetworkException | JsonParseException e) {
            if (e instanceof JsonParseException) {
                toolboxConfigs.remove(subreddit);
            }
            cache.edit().putLong(subreddit + "_config_timestamp", System.currentTimeMillis())
                    .putBoolean(subreddit + "_config_exists", false).apply();
        }
    }

    /**
     * Upload a subreddit's usernotes to the wiki
     *
     * @param subreddit Sub to upload usernotes for
     * @param editReason Reason for the wiki edit
     */
    public static void uploadUsernotes(String subreddit, String editReason) {
        WikiManager manager = new WikiManager(Authentication.reddit);
        Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, List<Usernote>>>() {}.getType(),
                new Usernotes.BlobSerializer()).disableHtmlEscaping().create();
        String data = gson.toJson(getUsernotes(subreddit));
        try {
            manager.edit(subreddit, "usernotes", data, "\"" + editReason + "\" via Slide");
            cache.edit().putBoolean(subreddit + "_usernotes_exists", true)
                    .putLong(subreddit + "_usernotes_timestamp", System.currentTimeMillis())
                    .putString(subreddit + "_usernotes_data", data)
                    .apply();
        } catch (NetworkException | ApiException e) {
            ensureUsernotesCachedLoaded(subreddit); // load back from cache if we failed to upload. keeps state correct
        }
    }

    private static class AsyncLoadUsernotes extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddit) {
            downloadUsernotes(subreddit[0]);
            return null;
        }
    }

    private static class AsyncLoadToolboxConfig extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddit) {
            downloadToolboxConfig(subreddit[0]);
            return null;
        }
    }
}
