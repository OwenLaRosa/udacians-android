package com.owenlarosa.udacians.data;

/**
 * Created by Owen LaRosa on 11/23/16.
 */

public class Message {

    private String sender;
    private String content;
    private String imageUrl;

    public Message() {}

    public Message(String sender, String content, String imageUrl) {
        this.sender = sender;
        this.content = content;
        this.imageUrl = imageUrl;
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
}
