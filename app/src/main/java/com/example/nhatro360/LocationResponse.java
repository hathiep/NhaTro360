package com.example.nhatro360;

public class LocationResponse {
    private String province;
    private String district;
    private String ward;
    private String address;

    public LocationResponse() {
    }

    public LocationResponse(String province, String district, String ward, String address) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.address = address;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
