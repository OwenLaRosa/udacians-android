package com.owenlarosa.udacians.data;

import java.util.Map;

/**
 * Created by Owen LaRosa on 11/23/16.
 */

public class Message {

    private String sender;
    private String content;
    private String imageUrl;
    private Map<String, String> date;

    public Message() {}

    public Message(String sender, String content, String imageUrl, Map<String, String> date) {
        this.sender = sender;
        this.content = content;
        this.imageUrl = imageUrl;
        this.date = date;
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

    public Map<String, String> getDate() {
        return date;
    }

    public void setDate(Map<String, String> date) {
        this.date = date;
    }
}
