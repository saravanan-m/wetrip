package com.wetrip.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rizwan on 10/09/16.
 */
public class Trip {

    @SerializedName("id")
    public int id = 0;

    @SerializedName("name")
    public String name = "";

    @SerializedName("from")
    public String from = "";

    @SerializedName("start_date")
    public String start_date = "";

    @SerializedName("created_by")
    public String created_by = "";

    @SerializedName("updatedAt")
    public String updatedAt = "";

    @SerializedName("createdAt")
    public String createdAt = "";

    public Trip( int id, String name,String status) {
        this.status = status;
        this.id = id;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("status")
    public String status = "";

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
