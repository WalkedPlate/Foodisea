package com.example.foodisea.item;

public class LogItem {
    private String message;
    private String timestamp;
    private int iconResourceId;

    public LogItem(String message, String timestamp, int iconResourceId) {
        this.message = message;
        this.timestamp = timestamp;
        this.iconResourceId = iconResourceId;
    }

    // Getters
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public int getIconResourceId() { return iconResourceId; }
}