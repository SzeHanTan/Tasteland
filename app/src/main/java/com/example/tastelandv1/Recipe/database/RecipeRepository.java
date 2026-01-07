package com.example.tastelandv1.Recipe.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.tastelandv1.RetrofitClient;
import com.example.tastelandv1.SessionManager;
import com.example.tastelandv1.SupabaseAPI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeRepository {
    private static final String API_KEY = RetrofitClient.SUPABASE_KEY;
    private final SupabaseAPI api;
    private final SessionManager sessionManager;
    private String currentUserId = "";

    // Cache to ensure sub-3s loading on subsequent visits
    private static List<Recipe> memoryCache = null;

    public RecipeRepository(Context context) {
        this.api = RetrofitClient.getInstance().getApi();
        this.sessionManager = new SessionManager(context);
    }

    private String getAuthToken() {
        String token = sessionManager.getToken();
        this.currentUserId = sessionManager.getUserId();
        if (this.currentUserId == null) this.currentUserId = "";
        if (token == null || token.isEmpty()) return "Bearer " + API_KEY;
        return "Bearer " + token;
    }

    public void getAllRecipes(RecipeCallback callback) {
        // 1. FAST PATH: Return cache instantly (Performance Requirement)
        if (memoryCache != null && !memoryCache.isEmpty()) {
            callback.onSuccess(new ArrayList<>(memoryCache));
            return;
        }

        String token = getAuthToken();

        // 2. PARALLEL EXECUTION: Run both requests simultaneously
        // Wrappers to hold results
        final List<Recipe>[] recipesHolder = new List[]{null};
        final Set<Integer>[] favoritesHolder = new Set[]{null};
        final int[] completedCount = {0};
        final boolean[] hasError = {false};

        // Synchronization Helper
        Runnable checkAndMerge = () -> {
            synchronized (completedCount) {
                completedCount[0]++;
                // Only proceed if both calls (Recipes + Favorites) are done
                if (completedCount[0] == 2) {
                    if (recipesHolder[0] != null) {
                        List<Recipe> result = recipesHolder[0];
                        // Merge Favorites if available
                        if (favoritesHolder[0] != null) {
                            for (Recipe r : result) {
                                r.setFavorite(favoritesHolder[0].contains(r.getId()));
                            }
                        }
                        updateCacheAndNotify(result, callback);
                    } else {
                        callback.onError("Failed to load recipe data.");
                    }
                }
            }
        };

        // Call A: Fetch Recipes
        api.getAllRecipes(API_KEY, token).enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (response.isSuccessful()) recipesHolder[0] = response.body();
                else hasError[0] = true;
                checkAndMerge.run();
            }
            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                hasError[0] = true;
                checkAndMerge.run();
            }
        });

        // Call B: Fetch Favorites (Parallel)
        if (currentUserId.isEmpty()) {
            favoritesHolder[0] = new HashSet<>(); // No user = no favorites
            checkAndMerge.run();
        } else {
            api.getMyFavorites(API_KEY, token, "eq." + currentUserId).enqueue(new Callback<List<FavoriteEntry>>() {
                @Override
                public void onResponse(Call<List<FavoriteEntry>> call, Response<List<FavoriteEntry>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Set<Integer> ids = new HashSet<>();
                        for (FavoriteEntry entry : response.body()) ids.add(entry.getRecipeId());
                        favoritesHolder[0] = ids;
                    }
                    checkAndMerge.run();
                }
                @Override
                public void onFailure(Call<List<FavoriteEntry>> call, Throwable t) {
                    // Fail silently for favorites, just show recipes without hearts
                    checkAndMerge.run();
                }
            });
        }
    }

    private void updateCacheAndNotify(List<Recipe> freshData, RecipeCallback callback) {
        memoryCache = freshData;
        // Ensure callback runs on Main Thread
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(freshData));
    }

    public void updateFavoriteStatus(int recipeId, boolean isFavorite, SimpleCallback callback) {
        String token = getAuthToken();
        if (currentUserId.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Optimistic Update: Update cache immediately for UI responsiveness
        if (memoryCache != null) {
            for (Recipe r : memoryCache) {
                if (r.getId() == recipeId) {
                    r.setFavorite(isFavorite);
                    break;
                }
            }
        }

        if (isFavorite) {
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