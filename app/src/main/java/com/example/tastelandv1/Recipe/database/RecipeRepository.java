package com.example.tastelandv1.Recipe.database;

import android.content.Context;
import com.example.tastelandv1.RetrofitClient;
import com.example.tastelandv1.SessionManager; // IMPORT THIS
import com.example.tastelandv1.SupabaseAPI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {
    private static final String API_KEY = RetrofitClient.SUPABASE_KEY;
    private final SupabaseAPI api;
    private final SessionManager sessionManager; // Use SessionManager
    private String currentUserId = "";

    public RecipeRepository(Context context) {
        this.api = RetrofitClient.getInstance().getApi();
        this.sessionManager = new SessionManager(context);
    }

    private String getAuthToken() {
        // FIX: Get credentials from the correct file (SessionManager)
        String token = sessionManager.getToken();
        this.currentUserId = sessionManager.getUserId();

        if (this.currentUserId == null) this.currentUserId = "";
        if (token == null || token.isEmpty()) return "Bearer " + API_KEY;
        return "Bearer " + token;
    }

    public void getAllRecipes(RecipeCallback callback) {
        String token = getAuthToken();

        api.getAllRecipes(API_KEY, token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Logic: Fetch recipes -> Then fetch favorites -> Merge them
                    fetchFavoritesAndMerge(response.body(), token, callback);
                } else {
                    // FIX: Handle Expired Token (401 Error)
                    if (response.code() == 401) {
                        sessionManager.logout();
                        callback.onError("Session expired. Please login again.");
                    } else {
                        callback.onError("Error: " + response.code());
                    }
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
            callback.onSuccess(recipes); // User not logged in, return raw list
            return;
        }

        // Fetch ONLY the IDs of recipes this user likes
        api.getMyFavorites(API_KEY, token, "eq." + currentUserId).enqueue(new Callback<List<FavoriteEntry>>() {
            @Override
            public void onResponse(Call<List<FavoriteEntry>> call, Response<List<FavoriteEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> favIds = new HashSet<>();
                    for (FavoriteEntry entry : response.body()) {
                        favIds.add(entry.getRecipeId());
                    }
                    // Loop through recipes and set the boolean flag
                    for (Recipe r : recipes) {
                        r.setFavorite(favIds.contains(r.getId()));
                    }
                }
                callback.onSuccess(recipes);
            }

            @Override
            public void onFailure(Call<List<FavoriteEntry>> call, Throwable t) {
                callback.onSuccess(recipes); // Fail gracefully (show recipes without hearts)
            }
        });
    }

    public void updateFavoriteStatus(int recipeId, boolean isFavorite, SimpleCallback callback) {
        String token = getAuthToken();
        if (currentUserId.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        if (isFavorite) {
            // INSERT into favorites table
            FavoriteRequest body = new FavoriteRequest(currentUserId, recipeId);
            api.addFavorite(API_KEY, token, body).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) callback.onSuccess();
                    else callback.onError("Failed to add: " + response.code());
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
            });
        } else {
            // DELETE from favorites table
            api.removeFavorite(API_KEY, token, "eq." + currentUserId, "eq." + recipeId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) callback.onSuccess();
                    else callback.onError("Failed to remove: " + response.code());
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
            });
        }
    }

    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
        void onError(String error);
    }
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}