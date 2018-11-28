package me.ccrama.redditslide.Toolbox;

import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import me.ccrama.redditslide.Authentication;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.managers.WikiManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main class for /r/toolbox functionality
 */
public class Toolbox {
    public static final Map<String, Map<String, String>> DEFAULT_USERNOTE_TYPES = new HashMap<>();
    private static Map<String, Usernotes> notes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private static Map<String, ToolboxConfig> toolboxen = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

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
    public static Usernotes getUsernotesForSubreddit(String subreddit) {
        return notes.get(subreddit);
    }

    /**
     * Gets a subreddit's toolbox config if we have loaded it
     *
     * @param subreddit Sub to get config fore
     * @return Toolbox config
     */
    public static ToolboxConfig getConfigForSubreddit(String subreddit) {
        return toolboxen.get(subreddit);
    }

    /**
     * Load a subreddit's usernotes from the sub's usernotes wiki page
     *
     * @param subreddit Sub to load notes for
     */
    public static void loadUsernotesForSubreddit(String subreddit) {
        new AsyncLoadUsernotes().execute(subreddit);
    }

    /**
     * Load a subreddit's toolbox config from the sub's toolbox wiki page
     * @param subreddit Sub to load config for
     */
    public static void loadConfigForSubreddit(String subreddit) {
        new AsyncLoadToolboxConfig().execute(subreddit);
    }

    private static class AsyncLoadUsernotes extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddit) {
            WikiManager manager = new WikiManager(Authentication.reddit);
            Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<Map<String, List<Usernote>>>() {}.getType(),
                    new Usernotes.BlobDeserializer()).create();
            try {
                Usernotes result = gson.fromJson(manager.get(subreddit[0], "usernotes").getContent(), Usernotes.class);
                if (result != null && result.getSchema() == 6) {
                    result.setSubreddit(subreddit[0]);
                    notes.put(subreddit[0], result);
                }
            } catch (NetworkException | JsonParseException ignored) {
            }
            return null;
        }
    }

    private static class AsyncLoadToolboxConfig extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... subreddit) {
            WikiManager manager = new WikiManager(Authentication.reddit);
            Gson gson = new Gson();
            try {
                ToolboxConfig result = gson.fromJson(manager.get(subreddit[0], "toolbox").getContent(),
                        ToolboxConfig.class);
                if (result != null && result.getSchema() == 1) {
                    toolboxen.put(subreddit[0], result);
                }
            } catch (NetworkException ignored) {
            }
            return null;
        }
    }
}
