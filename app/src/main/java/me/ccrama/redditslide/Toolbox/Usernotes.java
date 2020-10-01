package me.ccrama.redditslide.Toolbox;

import android.util.Base64;

import androidx.annotation.ColorInt;

import com.google.android.exoplayer2.util.ColorParser;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * A group of usernotes for a subreddit
 */
public class Usernotes {
    @SerializedName("ver") private int schema;
    private UsernotesConstants constants;
    @SerializedName("blob") private Map<String, List<Usernote>> notes;

    transient private String subreddit;

    public Usernotes() {
        // for GSON
    }

    public Usernotes(int schema, UsernotesConstants constants, Map<String, List<Usernote>> notes, String subreddit) {
        this.schema = schema;
        this.constants = constants;
        this.notes = notes;
        this.subreddit = subreddit;
    }

    /**
     * Add a usernote to this usernotes object
     *
     * Make sure to persist back to the wiki after doing this!
     *
     * @param user User to add note for
     * @param noteText Note text
     * @param link Toolbox link formatted link
     * @param time Time in ms
     * @param mod Mod making the note
     * @param type optional warning type
     */
    public void createNote(String user, String noteText, String link, long time, String mod, String type) {
        boolean modExists = false;
        int modIndex = -1;
        boolean typeExists = false;
        int typeIndex = -1;

        for (int i = 0; i < constants.getMods().length; i++) {
            if (constants.getMods()[i].equals(mod)) {
                modExists = true;
                modIndex = i;
                break;
            }
        }
        for (int i = 0; i < constants.getTypes().length; i++) {
            if ((constants.getTypes()[i] == null && type == null)
                    || (constants.getTypes()[i] != null && constants.getTypes()[i].equals(type))) {
                typeExists = true;
                typeIndex = i;
                break;
            }
        }

        if (!modExists) {
            modIndex = constants.addMod(mod);
        }
        if (!typeExists) {
            typeIndex = constants.addType(type);
        }

        Usernote note = new Usernote(noteText, link, time / 1000, modIndex, typeIndex);

        if (notes.containsKey(user)) {
            notes.get(user).add(0, note);
        } else {
            List<Usernote> newList = new ArrayList<>();
            newList.add(note);
            notes.put(user, newList);
        }
    }

    /**
     * Remove a usernote for a user
     *
     * Make sure to persist back to the wiki after doing this!
     *
     * @param user User to remove note from
     * @param note Note to remove
     */
    public void removeNote(String user, Usernote note) {
        if (notes.get(user) != null) {
            notes.get(user).remove(note);
            if (notes.get(user).isEmpty()) { // if we just removed the last note, remove the user too
                notes.remove(user);
            }
        }
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
        if (count == 0) {
            return "";
        }
        String noteText = StringUtils.abbreviate(getNotesForUser(user).get(0).getNoteText(), "…", 20);
        if (count > 1) {
            noteText += " (+" + (count - 1) + ")";
        }
        return noteText;
    }

    /**
     * Get the color for the primary displayed usernote of a user
     * @param user User
     * @return A color int
     */
    @ColorInt
    public int getDisplayColorForUser(String user) {
        if (getNotesForUser(user).size() > 0) {
            return getColorFromWarningIndex(getNotesForUser(user).get(0).getWarning());
        } else {
            return 0xFF808080;
        }
    }

    /**
     * Get a color from a warning index
     * @param index Index
     * @return A color int
     */
    @ColorInt
    public int getColorFromWarningIndex(int index) {
        String color = "#808080";

        ToolboxConfig config = Toolbox.getConfig(subreddit);
        if (config != null) { // Subs can have usernotes without a toolbox config
            color = config.getUsernoteColor(constants.getTypeName(index));
        } else {
            Map<String, String> defaults =
                    Toolbox.DEFAULT_USERNOTE_TYPES.get(constants.getTypeName(index));

            if (defaults != null) {
                String defaultColor = defaults.get("color");

                if (defaultColor != null) {
                    color = defaultColor;
                }
            }
        }

        try {
            return ColorParser.parseCssColor(color);
        } catch (IllegalArgumentException e) {
            return 0xFF808080;
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
        if (Toolbox.getConfig(subreddit) != null) {
            if (constants.getTypeName(index) != null) {
                String text = Toolbox.getConfig(subreddit).getUsernoteText(constants.getTypeName(index));
                if (!text.isEmpty()) {
                    result.append(text);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            if (constants.getTypeName(index) != null) {
                String def = Toolbox.DEFAULT_USERNOTE_TYPES.get(constants.getTypeName(index)).get("text");
                if (def != null) {
                    result.append(def);
                } else {
                    return "";
                }
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
        public Map<String, List<Usernote>> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {

            String decodedBlob = blobToJson(json.getAsString());
            if (decodedBlob == null) {
                return null;
            }
            JsonElement jsonBlob = JsonParser.parseString(decodedBlob);
            Map<String, List<Usernote>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

            for (Map.Entry<String, JsonElement> userAndNotes : jsonBlob.getAsJsonObject().entrySet()) {
                List<Usernote> notesList = new ArrayList<>();
                for (JsonElement notesArray : userAndNotes.getValue().getAsJsonObject().get("ns").getAsJsonArray()) {
                    notesList.add(context.deserialize(notesArray, Usernote.class));
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
     * Allows GSON to serialize the usernotes map into a blob
     */
    public static class BlobSerializer implements JsonSerializer<Map<String, List<Usernote>>> {
        @Override
        public JsonElement serialize(Map<String, List<Usernote>> src, Type srcType, JsonSerializationContext context) {
            Map<String, Map<String, List<Usernote>>> notes = new HashMap<>();
            for (Map.Entry<String, List<Usernote>> entry : src.entrySet()) {
                Map<String, List<Usernote>> newNotes = new HashMap<>();
                newNotes.put("ns", entry.getValue());
                notes.put(entry.getKey(), newNotes);
            }
            String encodedBlob = jsonToBlob(context.serialize(notes).toString());
            return context.serialize(encodedBlob);
        }

        /**
         * Converts a JSON string into a zlib compressed and base64 encoded blog
         *
         * @param json JSON to turn into blob
         * @return Blob
         */
        public static String jsonToBlob(String json) {
            // Adapted from https://stackoverflow.com/a/33022277
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                DeflaterOutputStream deflater = new DeflaterOutputStream(output);
                deflater.write(json.getBytes());
                deflater.flush();
                deflater.close();

                return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
            } catch (IOException e) {
                return null;
            }
        }
    }

    /**
     * Class describing the "constants" field of a usernotes config
     */
    public static class UsernotesConstants {
        @SerializedName("users") private String[] mods; // String array of mods. Usernote mod is index in this
        @SerializedName("warnings") private String[] types; // String array of used type names corresponding to types in the config/defaults. Usernote warning is index in this

        public UsernotesConstants() {
            // for GSON
        }

        public UsernotesConstants(String[] mods, String[] types) {
            this.mods = mods;
            this.types = types;
        }

        public String[] getMods() {
            return mods;
        }

        /**
         * Add a new user to the mods array
         *
         * Does not check for duplicates!
         *
         * @param user User to add
         * @return Index of added mod
         */
        public int addMod(String user) {
            String[] newMods = new String[mods.length + 1];
            System.arraycopy(mods, 0, newMods, 0, mods.length);
            newMods[newMods.length - 1] = user;
            mods = newMods;
            return newMods.length - 1;
        }

        public String[] getTypes() {
            return types;
        }

        /**
         * Adds a type to the warnings array
         *
         * Does not check for duplicates!
         *
         * @param type Type to add
         * @return Index of added type
         */
        public int addType(String type) {
            String[] newTypes = new String[types.length + 1];
            System.arraycopy(types, 0, newTypes, 0, types.length);
            newTypes[newTypes.length - 1] = type;
            types = newTypes;
            return newTypes.length - 1;
        }

        public String getTypeName(int index) {
            return types[index];
        }

        public String getModName(int index) {
            return mods[index];
        }
    }
}
