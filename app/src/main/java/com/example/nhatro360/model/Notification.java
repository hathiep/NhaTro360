package com.example.nhatro360.model;

import com.google.firebase.Timestamp;

public class Notification {
    private String message;
    private Timestamp time;
    private int type;

    public Notification() {
    }

    public Notification(String message, Timestamp time, int type) {
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
