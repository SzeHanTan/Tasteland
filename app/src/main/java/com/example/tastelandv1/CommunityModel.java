package com.example.tastelandv1;

public class CommunityModel {

    private String name;
    private int imageRes;

    public CommunityModel(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() {
        return name;
    }

    public int getImageRes() {
        return imageRes;
    }
}
