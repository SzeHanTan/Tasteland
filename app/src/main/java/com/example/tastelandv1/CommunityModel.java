package com.example.tastelandv1;

public class CommunityModel {
    private String name;
    private int image;

    public CommunityModel(String name, int image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }
}

