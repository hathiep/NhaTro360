package com.example.nhatro360;

import java.util.List;

public class Room {
    private String id; // Thêm trường id
    private String title;
    private String address;
    private String price;
    private String area;
    private String timePosted;
    private List<String> images; // Danh sách URL hình ảnh
    private boolean hasWifi;
    private boolean hasPrivateWC;
    private boolean hasParking;
    private boolean hasFreeTime;
    private boolean hasKitchen;
    private boolean hasAirConditioner;
    private boolean hasFridge;
    private boolean hasWashingMachine;
    private String phone; // Số điện thoại liên hệ
    private String host; // Chủ nhà
    private String detail; // Chi tiết mô tả phòng

    public Room() {
        // No-argument constructor required for Firestore deserialization
    }

    public Room(String id, String title, String address, String price, String area, String timePosted, List<String> images,
                boolean hasWifi, boolean hasPrivateWC, boolean hasParking, boolean hasFreeTime, boolean hasKitchen,
                boolean hasAirConditioner, boolean hasFridge, boolean hasWashingMachine, String phone, String host, String detail) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.price = price;
        this.area = area;
        this.timePosted = timePosted;
        this.images = images;
        this.hasWifi = hasWifi;
        this.hasPrivateWC = hasPrivateWC;
        this.hasParking = hasParking;
        this.hasFreeTime = hasFreeTime;
        this.hasKitchen = hasKitchen;
        this.hasAirConditioner = hasAirConditioner;
        this.hasFridge = hasFridge;
        this.hasWashingMachine = hasWashingMachine;
        this.phone = phone;
        this.host = host;
        this.detail = detail;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public boolean isHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public boolean isHasPrivateWC() {
        return hasPrivateWC;
    }

    public void setHasPrivateWC(boolean hasPrivateWC) {
        this.hasPrivateWC = hasPrivateWC;
    }

    public boolean isHasParking() {
        return hasParking;
    }

    public void setHasParking(boolean hasParking) {
        this.hasParking = hasParking;
    }

    public boolean isHasFreeTime() {
        return hasFreeTime;
    }

    public void setHasFreeTime(boolean hasFreeTime) {
        this.hasFreeTime = hasFreeTime;
    }

    public boolean isHasKitchen() {
        return hasKitchen;
    }

    public void setHasKitchen(boolean hasKitchen) {
        this.hasKitchen = hasKitchen;
    }

    public boolean isHasAirConditioner() {
        return hasAirConditioner;
    }

    public void setHasAirConditioner(boolean hasAirConditioner) {
        this.hasAirConditioner = hasAirConditioner;
    }

    public boolean isHasFridge() {
        return hasFridge;
    }

    public void setHasFridge(boolean hasFridge) {
        this.hasFridge = hasFridge;
    }

    public boolean isHasWashingMachine() {
        return hasWashingMachine;
    }

    public void setHasWashingMachine(boolean hasWashingMachine) {
        this.hasWashingMachine = hasWashingMachine;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
