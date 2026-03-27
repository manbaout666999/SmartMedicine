// ChatMessage.java
package com.example.smartmedicine.base;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    private String content;
    private int type; // 0: user, 1: ai
    private long timestamp;

    public ChatMessage(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getContent() { return content; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }

    public void setContent(String content) { this.content = content; }
    public void setType(int type) { this.type = type; }
}