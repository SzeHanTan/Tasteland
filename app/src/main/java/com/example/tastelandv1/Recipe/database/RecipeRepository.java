package com.example.tastelandv1.Recipe.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.tastelandv1.RetrofitClient;
import com.example.tastelandv1.SupabaseAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static final String API_KEY = RetrofitClient.SUPABASE_KEY;

    private final SupabaseAPI api;
    private final Context context;
    private String currentUserId = ""; // Need this for queries

    public RecipeRepository(Context context) {
        this.context = context;
        this.api = RetrofitClient.getInstance().getApi();
    }

    // --- HELPER: Get Token & ID ---
    private String getAuthToken() {
        SharedPreferences prefs = context.getSharedPreferences("TastelandPrefs", Context.MODE_PRIVATE);
        // Assuming you saved "user_id" along with the token during Login
        this.currentUserId = prefs.getString("user_id", "");
        String token = prefs.getString("access_token", "");
        return "Bearer " + token;
    }

    // --- 1. LOAD DATA (MERGE LOGIC) ---
    public void getAllRecipes(RecipeCallback callback) {
        String token = getAuthToken();

        // Step A: Fetch All Recipes
        api.getAllRecipes(API_KEY, token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Recipe> allRecipes = response.body();

                    // Step B: Now Fetch User's Favorites to compare
                    fetchFavoritesAndMerge(allRecipes, token, callback);
                } else {
                    callback.onError("Error fetching recipes: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void fetchFavoritesAndMerge(List<Recipe> recipes, String token, RecipeCallback callback) {
        if (currentUserId.isEmpty()) {
            // If not logged in, just return recipes with no favorites marked
            callback.onSuccess(recipes);
            return;
        }

        // Fetch IDs from 'favorites' table
        api.getMyFavorites(API_KEY, token, "eq." + currentUserId).enqueue(new Callback<List<FavoriteEntry>>() {
            @Override
            public void onResponse(Call<List<FavoriteEntry>> call, Response<List<FavoriteEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 1. Create a Set of favorite IDs for fast lookup
                    Set<Integer> favIds = new HashSet<>();
                    for (FavoriteEntry entry : response.body()) {
                        favIds.add(entry.getRecipeId());
                    }

                    // 2. Loop through recipes and mark the matches
                    for (Recipe r : recipes) {
                        r.setFavorite(favIds.contains(r.getId())); // This sets the boolean in MEMORY only
                    }

                    // 3. Return the merged list to the UI
                    callback.onSuccess(recipes);
                } else {
                    // Even if favorites fail, return the recipes at least
                    callback.onSuccess(recipes);
                }
            }

            @Override
            public void onFailure(Call<List<FavoriteEntry>> call, Throwable t) {
                callback.onSuccess(recipes); // Fallback
            }
        });
    }

    // --- 2. UPDATE FAVORITE (ADD/REMOVE ROW) ---
    public void updateFavoriteStatus(int recipeId, boolean isFavorite, SimpleCallback callback) {
        String token = getAuthToken();

        if (isFavorite) {
            // Action: ADD to favorites table
            FavoriteRequest body = new FavoriteRequest(currentUserId, recipeId);
            api.addFavorite(API_KEY, token, body).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) callback.onSuccess();
                    else callback.onError("Failed to add: " + response.code());
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    callback.onError(t.getMessage());
                }
            });
        } else {
            // Action: DELETE from favorites table
            // Query: delete where user_id = X AND recipe_id = Y
            api.removeFavorite(API_KEY, token, "eq." + currentUserId, "eq." + recipeId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) callback.onSuccess();
                            else callback.onError("Failed to remove: " + response.code());
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            callback.onError(t.getMessage());
                        }
                    });
        }
    }

    // Interfaces (Same as before)
    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
        void onError(String error);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}