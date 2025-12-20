package com.example.tastelandv1;

public class ShoppingItem {
    private String text;
    private boolean isChecked;

    // Constructor
    public ShoppingItem(String text, boolean isChecked) {
        this.text = text;
        this.isChecked = isChecked;
    }

    // Default constructor (often useful)
    public ShoppingItem() {
        this.text = "";
        this.isChecked = false;
    }

    // Getters and Setters
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
