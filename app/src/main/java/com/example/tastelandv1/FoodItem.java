package com.example.tastelandv1;

import java.util.Date;
import java.util.UUID;

public class FoodItem {

    // --- Attributes from your requirements ---
    private String id;
    private String name;
    private Date dueDate;
    private Date reminderDate; // Can be null
    private boolean isFinished;
    private Date createdAt;
    private Date updatedAt;

    // --- Constructors ---

    /**
     * Constructor for creating a brand new food item.
     * Generates a unique ID and sets initial timestamps.
     *
     * Best Practice: Store all dates in your database as UTC.
     * This prevents a huge number of problems if your app is ever used by people in different timezones.
     * You save a universal, standardized time, and then you format it for display in the user's local timezone whenever you need to show it.
     */
    public FoodItem(String name, Date dueDate, Date reminderDate) {
        this.id = UUID.randomUUID().toString(); // Generate a unique identifier
        this.name = name;
        this.dueDate = dueDate;
        this.reminderDate = reminderDate;
        this.isFinished = false; // New items are not finished
        this.createdAt = new Date(); // Set creation timestamp to now
        this.updatedAt = new Date(); // Set updated timestamp to now
    }

    /**
     * A no-argument constructor is often required for database libraries like Firebase/Firestore.
     */
    public FoodItem() {
        // Default constructor
    }


    // --- Getters and Setters ---
    // These allow other parts of your app to access and modify the data.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
