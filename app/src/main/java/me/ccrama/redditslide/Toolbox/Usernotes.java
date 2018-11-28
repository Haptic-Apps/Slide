package me.ccrama.redditslide.Toolbox;

import android.util.Base64;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.zip.InflaterInputStream;

/**
 * A group of usernotes for a subreddit
 */
public class Usernotes {
    @SerializedName("ver") private int schema;
    private UsernotesConstants constants;
    @SerializedName("blob") private Map<String, List<Usernote>> notes;
    transient private String subreddit;

    public Usernotes(String subreddit) {

    }

    public int getSchema() {
        return schema;
    }

    public UsernotesConstants getConstants() {
        return constants;
    }

    public Map<String, List<Usernote>> getNotes() {
        return notes;
    }

    /**
     * Get the list of usernotes for a user
     * @param user User to get notes for
     * @return List of usernotes
     */
    public List<Usernote> getNotesForUser(String user) {
        return notes.get(user);
    }

    /**
     * Gets the display text for a user using same logic as toolbox
     * @param user User
     * @return (Shortened) usernote text (plus count if additional notes)
     */
    public String getDisplayNoteForUser(String user) {
        int count = getNotesForUser(user).size();
        String noteText = StringUtils.abbreviate(getNotesForUser(user).get(0).getNoteText(), "…", 20);
        if (count > 1) {
            noteText += " (+" + (count - 1) + ")";
        }
        return noteText;
    }

    /**
     * Get the hex color for the primary displayed usernote of a user
     * @param user User
     * @return Hex color string
     */
    public String getDisplayColorForUser(String user) {
        return getColorFromWarningIndex(getNotesForUser(user).get(0).getWarning());
    }

    /**
     * Get a color string from a warning index
     * @param index Index
     * @return Hex color string
     */
    public String getColorFromWarningIndex(int index) {
        if (Toolbox.getConfigForSubreddit(subreddit) != null) { // Subs can have usernotes without a toolbox config
            return Toolbox.getConfigForSubreddit(subreddit).getUsernoteColor(constants.getTypeName(index));
        } else {
            String def = null;
            if (Toolbox.DEFAULT_USERNOTE_TYPES.get(constants.getTypeName(index)) != null) {
                def = Toolbox.DEFAULT_USERNOTE_TYPES.get(constants.getTypeName(index)).get("color");
            }
            if (def != null) {
                return def;
            } else {
                return "#808080"; // gray for non-typed or unknown type notes, same as Toolbox
            }
        }
    }

    /**
     * Get the warning text for a usernote from the index in the warnings array
     *
     * @param index Index in warnings array
     * @param bracket Whether to wrap the returned result in brackets
     * @return Warning text
     */
    public String getWarningTextFromWarningIndex(int index, boolean bracket) {
        StringBuilder result = new StringBuilder(bracket ? "[" : "");
        if (Toolbox.getConfigForSubreddit(subreddit) != null) {
            if (constants.getTypeName(index) != null) {
                String text = Toolbox.getConfigForSubreddit(subreddit).getUsernoteText(constants.getTypeName(index));
                if (!text.isEmpty()) {
                    result.append(text);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            String def = Toolbox.DEFAULT_USERNOTE_TYPES.get(constants.getTypeName(index)).get("text");
            if (def != null) {
                result.append(def);
            } else {
                return "";
            }
        }
        result.append(bracket ? "]" : "");
        return result.toString();
    }

    public String getModNameFromModIndex(int index) {
        return constants.getModName(index);
    }

    /**
     * Sets the Usernotes object's subreddit
     * @param subreddit
     */
    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    /**
     * Allows GSON to deserialize the "blob" into an object
     */
    public static class BlobDeserializer implements JsonDeserializer<Map<String, List<Usernote>>> {
        @Override
        public Map<String, List<Usernote>> deserialize(JsonElement json,
                                                       Type typeOfT,
                                                       JsonDeserializationContext context) throws JsonParseException {

            String decodedBlob = blobToJson(json.getAsString());
            if (decodedBlob == null) {
                return null;
            }
            JsonElement jsonBlob = new JsonParser().parse(decodedBlob);
            Map<String, List<Usernote>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for (Map.Entry<String, JsonElement> userAndNotes : jsonBlob.getAsJsonObject().entrySet()) {
                List<Usernote> notesList = new ArrayList<>();
                for (JsonElement notesArray : userAndNotes.getValue().getAsJsonObject().get("ns").getAsJsonArray()) {
                    notesList.add((Usernote) context.deserialize(notesArray, Usernote.class));
                }
                result.put(userAndNotes.getKey().toLowerCase(), notesList);
            }

            return result;
        }

        /**
         * Converts a base64 encoded and zlib compressed blob into a String.
         * @param blob Blob to convert to string
         * @return Decoded blob
         */
        public static String blobToJson(String blob) {
            final byte[] decoded = Base64.decode(blob, Base64.DEFAULT);

            // Adapted from https://stackoverflow.com/a/33022277
            try {
                ByteArrayInputStream input = new ByteArrayInputStream(decoded);
                InflaterInputStream inflater = new InflaterInputStream(input);

                StringBuilder result = new StringBuilder();
                byte[] buf = new byte[5];
                int rlen;
                while ((rlen = inflater.read(buf)) != -1) {
                    result.append(new String(Arrays.copyOf(buf, rlen)));
                }
                return result.toString();
            } catch (IOException e) {
                return null;
            }
        }
    }

    /**
     * Class describing the "constants" field of a usernotes config
     */
    public class UsernotesConstants {
        @SerializedName("users") private String[] mods; // String array of mods. Usernote mod is index in this
        @SerializedName("warnings") private String[] types; // String array of used type names corresponding to types in the config/defaults. Usernote warning is index in this

        public UsernotesConstants() {
        }

        public String[] getMods() {
            return mods;
        }

        public String[] getTypes() {
            return types;
        }

        public String getTypeName(int index) {
            return types[index];
        }

        public String getModName(int index) {
            return mods[index];
        }
    }
}