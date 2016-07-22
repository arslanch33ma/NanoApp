package com.example.aarshad.nanoapp;

/**
 * Created by aarshad on 7/20/16.
 */
public class LocationInfo {

    private int _id;
    private String _uid;
    private String _lat;
    private String _lng;
    private String _address;


    public LocationInfo(){
    }

    public LocationInfo(String uid, String lat, String lng, String address){
        this._uid = uid;
        this._lat = lat;
        this._lng = lng;
        this._address = address;

    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public int get_id() {
        return _id;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_lat(String _lat) {
        this._lat = _lat;
    }

    public String get_lat() {
        return _lat;
    }

    public String get_lng() {
        return _lng;
    }

    public void set_lng(String _lng) {

        this._lng = _lng;
    }

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {

        this._address = _address;
    }
}