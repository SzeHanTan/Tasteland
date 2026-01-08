package com.example.tastelandv1.Food.database;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class FoodItem {
    @SerializedName("id")
    public String id;

    @SerializedName("user_id") // Matches Supabase column
    public String userId;

    @SerializedName("name")
    public String name;

    @SerializedName("due_date") // Matches Supabase column
    public Date dueDate;

    @SerializedName("reminder_date") // Matches Supabase column
    public Date reminderDate;

    @SerializedName("is_finished") // Matches Supabase column
    public boolean isFinished;

    public FoodItem(String userId, String name, Date dueDate, Date reminderDate) {
        this.userId = userId;
        this.name = name;
        this.dueDate = dueDate;
        this.reminderDate = reminderDate;
        this.isFinished = false;
    }
}
