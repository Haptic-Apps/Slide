package me.ccrama.redditslide.Flair;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Richtext {

    @SerializedName("e")
    @Expose
    private String e;
    @SerializedName("t")
    @Expose
    private String t;
    @SerializedName("a")
    @Expose
    private String a;
    @SerializedName("u")
    @Expose
    private String u;

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

}