package com.example.tastelandv1.Shopping.database;

import android.content.Context;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingRepository {
    private static List<ShoppingItem> memoryCache = null; // The Cache
    private final SupabaseAPI api;
    private final SessionManager session;

    public ShoppingRepository(Context context) {
        this.api = RetrofitClient.getInstance(context).getApi();
        this.session = new SessionManager(context);
    }

    public void getShoppingList(final DataCallback callback) {
        // 1. Return Cache Immediately
        if (memoryCache != null) {
            callback.onSuccess(new ArrayList<>(memoryCache)); // Return copy
            return;
        }

        String token = session.getToken();
        if (token == null) {
            callback.onError("Not Logged In");
            return;
        }

        // 2. Fetch from Network
        api.getItems(RetrofitClient.SUPABASE_KEY, "Bearer " + token).enqueue(new Callback<List<ShoppingItem>>() {
            @Override
            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    memoryCache = response.body(); // Update Cache
                    callback.onSuccess(memoryCache);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // Helper to update cache locally when items are added/removed
    public void invalidateCache() {
        memoryCache = null;
    }

    public void addToCache(ShoppingItem item) {
        if (memoryCache != null) memoryCache.add(item);
    }

    public interface DataCallback {
        void onSuccess(List<ShoppingItem> data);
        void onError(String error);
    }
}