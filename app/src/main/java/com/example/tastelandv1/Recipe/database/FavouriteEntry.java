package com.example.tastelandv1.Recipe.database;

import com.google.gson.annotations.SerializedName;

public class FavouriteEntry {
    @SerializedName("recipe_id")
    private int recipeId;

    // We only really need the ID to match it against the recipes
    public int getRecipeId() {
        return recipeId;
    }

    // Constructor for sending data (if needed)
    public FavouriteEntry(int recipeId) {
        this.recipeId = recipeId;
    }
}