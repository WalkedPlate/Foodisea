package com.example.foodisea.models;

public class Message {
    private String text;
    private boolean sentByCurrentUser;
    private int profileImageResId;
    private String time;// ID del recurso de imagen del perfil

    public Message(String text, boolean sentByCurrentUser, int profileImageResId, String time) {
        this.text = text;
        this.sentByCurrentUser = sentByCurrentUser;
        this.profileImageResId = profileImageResId;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSentByCurrentUser() {
        return sentByCurrentUser;
    }

    public void setSentByCurrentUser(boolean sentByCurrentUser) {
        this.sentByCurrentUser = sentByCurrentUser;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }

    public void setProfileImageResId(int profileImageResId) {
        this.profileImageResId = profileImageResId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
