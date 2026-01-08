package com.example.tastelandv1.Community;

import com.google.gson.annotations.SerializedName;

public class CommunityModel {

    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    @SerializedName("image_res")
    private int imageRes;

    @SerializedName("invitation_code")
    private String invitationCode;

    @SerializedName("updated_at")
    private String updatedAt;

    public CommunityModel(String name, int imageRes, String invitationCode) {
        this.name = name;
        this.imageRes = imageRes;
        this.invitationCode = invitationCode;
    }

    public String getName() {
        return name;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public Integer getId() {
        return id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
