package com.example.tastelandv1.Recipe.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;

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
        this.api = RetrofitClient.getInstance(context).getApi();
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
        final Set<Integer>[] favouritesHolder = new Set[]{null};
        final int[] completedCount = {0};
        final boolean[] hasError = {false};

        // Synchronization Helper
        Runnable checkAndMerge = () -> {
            synchronized (completedCount) {
                completedCount[0]++;
                // Only proceed if both calls (Recipes + Favourites) are done
                if (completedCount[0] == 2) {
                    if (recipesHolder[0] != null) {
                        List<Recipe> result = recipesHolder[0];
                        // Merge Favourites if available
                        if (favouritesHolder[0] != null) {
                            for (Recipe r : result) {
                                r.setFavourite(favouritesHolder[0].contains(r.getId()));
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

        // Call B: Fetch Favourites (Parallel)
        if (currentUserId.isEmpty()) {
            favouritesHolder[0] = new HashSet<>(); // No user = no favourites
            checkAndMerge.run();
        } else {
            api.getMyFavourites(API_KEY, token, "eq." + currentUserId).enqueue(new Callback<List<FavouriteEntry>>() {
                @Override
                public void onResponse(Call<List<FavouriteEntry>> call, Response<List<FavouriteEntry>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Set<Integer> ids = new HashSet<>();
                        for (FavouriteEntry entry : response.body()) ids.add(entry.getRecipeId());
                        favouritesHolder[0] = ids;
                    }
                    checkAndMerge.run();
                }
                @Override
                public void onFailure(Call<List<FavouriteEntry>> call, Throwable t) {
                    // Fail silently for favourites, just show recipes without hearts
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

    public void updateFavouriteStatus(int recipeId, boolean isFavourite, SimpleCallback callback) {
        String token = getAuthToken();
        if (currentUserId.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        // Optimistic Update: Update cache immediately for UI responsiveness
        if (memoryCache != null) {
            for (Recipe r : memoryCache) {
                if (r.getId() == recipeId) {
                    r.setFavourite(isFavourite);
                    break;
                }
            }
        }

        if (isFavourite) {
            FavouriteRequest body = new FavouriteRequest(currentUserId, recipeId);
            api.addFavourite(API_KEY, token, body).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) callback.onSuccess();
                    else callback.onError("Failed to add: " + response.code());
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) { callback.onError(t.getMessage()); }
            });
        } else {
            api.removeFavourite(API_KEY, token, "eq." + currentUserId, "eq." + recipeId).enqueue(new Callback<Void>() {
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