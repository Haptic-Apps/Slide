package me.ccrama.redditslide.Flair;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RichFlair {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("text_editable")
    @Expose (serialize = true, deserialize = false)
    private Boolean textEditable;
    @SerializedName("allowable_content")
    @Expose
    private String allowableContent;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("id")
    @Expose
    private String id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getTextEditable() {
        return textEditable;
    }

    public void setTextEditable(Boolean textEditable) {
        this.textEditable = textEditable;
    }

    public String getAllowableContent() {
        return allowableContent;
    }

    public void setAllowableContent(String allowableContent) {
        this.allowableContent = allowableContent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
