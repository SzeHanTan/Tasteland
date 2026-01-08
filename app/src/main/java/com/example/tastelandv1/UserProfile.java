package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    // This ID links to the auth user
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("contact_no")
    private String contactNo;

    @SerializedName("description")
    private String description;

    // Constructor
    public UserProfile(String fullName, String contactNo, String description) {
        this.fullName = fullName;
        this.contactNo = contactNo;
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getContactNo() { return contactNo; }
    public String getDescription() { return description; }
}