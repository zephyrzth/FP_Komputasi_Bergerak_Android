package com.example.accelerometersensor;

import com.google.gson.annotations.SerializedName;

public class PostPutDelData {
    @SerializedName("status")
    String status;
    @SerializedName("data")
    DataActivity dataActivity;
    @SerializedName("message")
    String message;

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDataPhoto(DataActivity dataActivity) {
        this.dataActivity = dataActivity;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public DataActivity getDataPhoto() {
        return dataActivity;
    }

    public String getMessage() {
        return message;
    }
}
