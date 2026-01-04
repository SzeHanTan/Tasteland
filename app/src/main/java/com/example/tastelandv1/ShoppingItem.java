package com.example.tastelandv1;

import com.google.gson.annotations.SerializedName;

public class ShoppingItem {

    @SerializedName("id")
    private Long id; // Must be Long to match int8 in Supabase

    @SerializedName("text")
    private String text;

    @SerializedName("is_checked")
    private boolean isChecked;

    @SerializedName("user_id") // Good practice to include this
    private String userId;

    // 1. Default Constructor (Crucial for Retrofit/Gson)
    public ShoppingItem() {
    }

    // 2. Constructor for creating NEW items (ID is null, UserID is null - DB handles it)
    public ShoppingItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}