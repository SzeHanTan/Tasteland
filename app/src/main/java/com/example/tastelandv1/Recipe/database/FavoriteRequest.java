package com.example.tastelandv1.Recipe.database;
import com.google.gson.annotations.SerializedName;

public class FavoriteRequest {
    @SerializedName("user_id") String userId;
    @SerializedName("recipe_id") int recipeId;

    public FavoriteRequest(String userId, int recipeId) {
        this.userId = userId;
        this.recipeId = recipeId;
    }
}