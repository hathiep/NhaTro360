package com.example.nhatro360;

import java.util.List;

public class Room {
    private String id; // Thêm trường id
    private String title;
    private String address;
    private String price;
    private String area;
    private String timePosted;
    private List<Integer> imageResourceIds; // Danh sách hình ảnh
    private boolean hasWifi;
    private boolean hasPrivateWC;
    private boolean hasParking;
    private boolean hasFreeTime;
    private boolean hasKitchen;
    private boolean hasAirConditioner;
    private boolean hasFridge;
    private boolean hasWashingMachine;

    public Room() {
        // No-argument constructor required for Firestore deserialization
    }

    public Room(String id, String title, String address, String price, String area, String timePosted, List<Integer> imageResourceIds,
                boolean hasWifi, boolean hasPrivateWC, boolean hasParking, boolean hasFreeTime, boolean hasKitchen,
                boolean hasAirConditioner, boolean hasFridge, boolean hasWashingMachine) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.timePosted = timePosted;
        this.imageResourceIds = imageResourceIds;
        this.hasWifi = hasWifi;
        this.hasPrivateWC = hasPrivateWC;
        this.hasParking = hasParking;
        this.hasFreeTime = hasFreeTime;
        this.hasKitchen = hasKitchen;
        this.hasAirConditioner = hasAirConditioner;
        this.hasFridge = hasFridge;
        this.hasWashingMachine = hasWashingMachine;
    }

    // Getter and setter for id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Other getters and setters
    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getPrice() {
        return price;
    }

    public String getArea() {
        return area;
    }

    public String getTimePosted() {
        return timePosted;
    }

    public List<Integer> getImageResourceIds() {
        return imageResourceIds;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public boolean isHasPrivateWC() {
        return hasPrivateWC;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public boolean isHasFreeTime() {
        return hasFreeTime;
    }

    public boolean isHasKitchen() {
        return hasKitchen;
    }

    public boolean isHasAirConditioner() {
        return hasAirConditioner;
    }

    public boolean isHasFridge() {
        return hasFridge;
    }

    public boolean isHasWashingMachine() {
        return hasWashingMachine;
    }
}
