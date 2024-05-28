package org.uiutils;

import com.google.gson.annotations.SerializedName;

public class GithubRelease {
    @SerializedName("tag_name")
    private String tagName;

    public String getTagName() {
        return tagName;
    }
}
