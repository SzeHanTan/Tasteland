package com.example.tastelandv1.Food.database;

import android.content.Context;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodRepository {
    private static List<FoodItem> memoryCache = null;
    private final SupabaseAPI api;
    private final SessionManager session;

    public FoodRepository(Context context) {
        this.api = RetrofitClient.getInstance(context).getApi();
        this.session = new SessionManager(context);
    }

    public void getFoodItems(final DataCallback callback) {
        if (memoryCache != null) {
            callback.onSuccess(new ArrayList<>(memoryCache));
        }

        String token = session.getToken();
        if (token == null) {
            callback.onError("Not Logged In");
            return;
        }

        api.getFoodItems("Bearer " + token, RetrofitClient.SUPABASE_KEY, "*", "due_date.asc")
                .enqueue(new Callback<List<FoodItem>>() {
                    @Override
                    public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            memoryCache = response.body();
                            callback.onSuccess(memoryCache);
                        } else {
                            if (memoryCache == null) callback.onError("Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                        if (memoryCache == null) callback.onError(t.getMessage());
                    }
                });
    }

    public void addFoodItem(FoodItem item, final SimpleCallback callback) {
        String token = session.getToken();
        if (token == null) {
            callback.onError("User not logged in");
            return;
        }

        String authToken = "Bearer " + token;

        api.createFoodItem(authToken, RetrofitClient.SUPABASE_KEY, item)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Clear cache so the new item shows up next time we load
                            invalidateCache();
                            callback.onSuccess();
                        } else {
                            callback.onError("Failed to save: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        callback.onError("Network Error: " + t.getMessage());
                    }
                });
    }

    public void invalidateCache() {
        memoryCache = null;
    }

    public interface DataCallback {
        void onSuccess(List<FoodItem> data);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}