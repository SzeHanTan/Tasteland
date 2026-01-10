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

    @SerializedName("referral_code")
    private String referralCode;

    @SerializedName("referred_by")
    private String referredBy;

    // Constructor
    public UserProfile(String fullName, String contactNo, String description) {
        this.fullName = fullName;
        this.contactNo = contactNo;
        this.description = description;
    }

    // Constructor with referral info
    public UserProfile(String fullName, String contactNo, String description, String referralCode, String referredBy) {
        this.fullName = fullName;
        this.contactNo = contactNo;
        this.description = description;
        this.referralCode = referralCode;
        this.referredBy = referredBy;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getContactNo() { return contactNo; }
    public String getDescription() { return description; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public String getReferredBy() { return referredBy; }
    public void setReferredBy(String referredBy) { this.referredBy = referredBy; }
}