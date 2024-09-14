package com.ui_utils;

import com.google.gson.annotations.SerializedName;

public class GithubRelease {
    @SerializedName("tag_name")
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    @SerializedName("name")
    private String name;

    public String getMcVersion() {
        // in name, return the text within the ()
        return name.substring(name.indexOf("(") + 1, name.indexOf(")"));
    }
}
