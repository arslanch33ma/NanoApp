package com.example.aarshad.nanoapp;

/**
 * Created by aarshad on 7/20/16.
 */
public class LocationInfo {

    private int _id;
    private String _uid;
    private String _name;
    private String _lat;
    private String _lng;
    private String _address;
    private String _time;


    public LocationInfo(){
    }

    public LocationInfo(String uid, String name, String lat, String lng, String address, String time){
        this._uid = uid;
        this._name = name;
        this._lat = lat;
        this._lng = lng;
        this._address = address;
        this._time = time;


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

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
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

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }
}