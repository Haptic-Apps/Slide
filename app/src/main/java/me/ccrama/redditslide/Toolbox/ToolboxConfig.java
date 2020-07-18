package me.ccrama.redditslide.Toolbox;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class defining a toolbox config. Contains removal reasons, mod macros, usernote colors, domain tags, etc.
 */
public class ToolboxConfig {
    @SerializedName("ver")
    private int schema;

    @JsonAdapter(EmptyStringAsNullTypeAdapter.class)
    private List<Map<String, String>> domainTags;

    @JsonAdapter(EmptyStringAsNullTypeAdapter.class)
    private RemovalReasons removalReasons;

    @JsonAdapter(EmptyStringAsNullTypeAdapter.class)
    private List<Map<String, String>> macros;

    @SerializedName("usernoteColors")
    @JsonAdapter(UsernoteTypeDeserializer.class)
    private Map<String, Map<String, String>> usernoteTypes;

    @JsonAdapter(EmptyStringAsNullTypeAdapter.class)
    private Map<String, String> banMacros;

    public ToolboxConfig() {

    }

    public int getSchema() {
        return schema;
    }

    public List<Map<String, String>> getDomainTags() {
        return domainTags;
    }

    public RemovalReasons getRemovalReasons() {
        return removalReasons;
    }

    public Map<String, Map<String, String>> getUsernoteTypes() {
        return usernoteTypes;
    }

    public String getUsernoteColor(String type) {
        if (usernoteTypes != null && usernoteTypes.get(type) != null && usernoteTypes.get(type).get("color") != null) {
            return usernoteTypes.get(type).get("color");
        } else {
            if (Toolbox.DEFAULT_USERNOTE_TYPES.get(type) != null) {
                return Toolbox.DEFAULT_USERNOTE_TYPES.get(type).get("color");
            } else {
                return "#808080"; // gray for non-typed or unknown type notes, same as Toolbox
            }
        }
    }

    public String getUsernoteText(String type) {
        if (usernoteTypes != null && usernoteTypes.get(type) != null && usernoteTypes.get(type).get("text") != null) {
            return usernoteTypes.get(type).get("text");
        } else {
            if (Toolbox.DEFAULT_USERNOTE_TYPES.get(type) != null) {
                return Toolbox.DEFAULT_USERNOTE_TYPES.get(type).get("text");
            } else {
                return "";
            }
        }
    }

    public static class UsernoteTypeDeserializer implements JsonDeserializer<Map<String, Map<String, String>>> {
        @Override
        public Map<String, Map<String, String>> deserialize(
                JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) { // isn't an array
                return null;
            }
            Map<String, Map<String, String>> result = new HashMap<>();
            for (JsonElement noteType : json.getAsJsonArray()) {
                Map<String, String> details = new HashMap<>();
                details.put("color", noteType.getAsJsonObject().get("color").getAsString());
                details.put("text", noteType.getAsJsonObject().get("text").getAsString());
                result.put(noteType.getAsJsonObject().get("key").getAsString(), details);
            }
            return result;
        }
    }

    // from https://stackoverflow.com/a/48806970, because toolbox uses empty strings to mean null in some instances
    public static final class EmptyStringAsNullTypeAdapter<T> implements JsonDeserializer<T> {
        @Override
        public T deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext context)
                throws JsonParseException {
            if ( jsonElement.isJsonPrimitive() ) {
                final JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                if ( jsonPrimitive.isString() && jsonPrimitive.getAsString().isEmpty() ) {
                    return null;
                }
            }
            return context.deserialize(jsonElement, type);
        }

    }
}
