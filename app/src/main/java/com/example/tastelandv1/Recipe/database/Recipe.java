package com.example.tastelandv1.Recipe.database;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("overview")
    private String overview;

    @SerializedName("ingredients")
    private List<String> ingredients;

    @SerializedName("instructions")
    private String instructions;

    @SerializedName("category")
    private String category;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("created_at") // Maps JSON 'created_at' to Java 'createdAt'
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("is_favourite")
    private Boolean isFavourite;

    @SerializedName("image_url")
    private String imageUrl;

    // Required empty constructor for serialization
    public Recipe() {
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }


    public Boolean isFavourite() {
        return isFavourite != null ? isFavourite : false;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}