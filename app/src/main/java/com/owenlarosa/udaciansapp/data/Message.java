package com.owenlarosa.udaciansapp.data;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Owen LaRosa on 11/23/16.
 */

public class Message {

    private String id;
    private String sender;
    private String content;
    private String imageUrl;
    private long date;

    public Message() {}

    public Message(String sender, String content, String imageUrl, long date) {
        this.sender = sender;
        this.content = content;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Create a map consistent of this class's properties
     * This is used when pushing new messages
     * The date property is set to ServerValue.TIMESTAMP for this purpose
     * @return map of the properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> mapped = toProfilePost();
        mapped.put("date", ServerValue.TIMESTAMP);
        return mapped;
    }

    /**
     * Creates a map specifically for posts on the user's profile
     * Same as toMap() excluding the timestamp
     * @return Map of the properties
     */
    public Map<String, Object> toProfilePost() {
        Map<String, Object> mapped = new HashMap<>();
        mapped.put("sender", sender);
        mapped.put("content", content);
        mapped.put("imageUrl", imageUrl);
        return mapped;
    }

}
