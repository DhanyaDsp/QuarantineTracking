package com.tech.quarantinetracking.webservice;

public class UserProfileRequest {
    private String name;
    private String phoneNumber;
    private String emergencyPhoneNumber;
    private String dob;
    private String gender;
    private String locality;
    private String localPoliceStation;
    private String profile_img;
    private String lat;
    private String lon;
    private String deviceId;

    public UserProfileRequest(String name, String mobile, String emergencyContact, String dob, String gender, String locality, String local_police_station, String lat, String lon, String deviceId) {
        this.name = name;
        this.phoneNumber = mobile;
        this.emergencyPhoneNumber = emergencyContact;
        this.dob = dob;
        this.gender = gender;
        this.locality = locality;
        this.localPoliceStation = local_police_station;
        this.lat = lat;
        this.lon = lon;
        this.deviceId = deviceId;

    }


    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmergencyPhoneNumber() {
        return emergencyPhoneNumber;
    }

    public void setEmergencyPhoneNumber(String emergencyPhoneNumber) {
        this.emergencyPhoneNumber = emergencyPhoneNumber;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLocalPoliceStation() {
        return localPoliceStation;
    }

    public void setLocalPoliceStation(String localPoliceStation) {
        this.localPoliceStation = localPoliceStation;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
