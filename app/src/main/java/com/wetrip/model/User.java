package com.wetrip.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rizwan on 10/09/16.
 */
public class User {
    @SerializedName("id")
    public int id = 0;

    @SerializedName("name")
    public String name = "";

    @SerializedName("photo")
    public String photo = "";

    @SerializedName("mobile")
    public String mobile = "";

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @SerializedName("token")
    public String token = "";




}
