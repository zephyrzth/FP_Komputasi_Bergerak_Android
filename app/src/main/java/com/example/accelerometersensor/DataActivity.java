package com.example.accelerometersensor;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DataActivity {

    @SerializedName("nama_user")
    private String nama_user;
    @SerializedName("label_aktivitas")
    private int label_aktivitas;
    @SerializedName("locations")
    private Double[] locations;

    public DataActivity() {

    }

    public DataActivity(String nama_user, int label_aktivitas, Double[] locations) {
        this.nama_user = nama_user;
        this.label_aktivitas = label_aktivitas;
        this.locations = locations;
    }

    public void setNama_user(String nama_user) {
        this.nama_user = nama_user;
    }

    public void setLabel_aktivitas(int label_aktivitas) {
        this.label_aktivitas = label_aktivitas;
    }

    public void setLocations(Double[] locations) {
        this.locations = locations;
    }

    public String getNama_user() {
        return nama_user;
    }

    public int getLabel_aktivitas() {
        return label_aktivitas;
    }

    public Double[] getLocations() {
        return locations;
    }
}
