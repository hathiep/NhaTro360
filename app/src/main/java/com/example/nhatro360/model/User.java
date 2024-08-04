package com.example.nhatro360.model;

import java.util.List;

public class User {
    private String id, fullName, email, phone;
    private List<String> savedRooms;
    private List<String> postedRooms;

    public User() {
    }

    public User(String fullName, String email, String phone, List<String> savedRooms, List<String> postedRooms) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.savedRooms = savedRooms;
        this.postedRooms = postedRooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getSavedRooms() {
        return savedRooms;
    }

    public void setSavedRooms(List<String> savedRooms) {
        this.savedRooms = savedRooms;
    }

    public List<String> getPostedRooms() {
        return postedRooms;
    }

    public void setPostedRooms(List<String> postedRooms) {
        this.postedRooms = postedRooms;
    }
}
