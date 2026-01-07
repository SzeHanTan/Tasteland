package com.example.tastelandv1.Backend;

import android.content.Context;

import com.example.tastelandv1.AuthResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private SupabaseAPI myApi;
    private static SessionManager sessionManager;
    public static final String SUPABASE_URL = "https://qdukyuxdzlgypanlzggm.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkdWt5dXhkemxneXBhbmx6Z2dtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NjIxMjgsImV4cCI6MjA4MTQzODEyOH0.A9IcD93BFurfFSws6Urc237L7yxu-XOp5RzONvDO_3E";

    private RetrofitClient(Context context) {
        this.sessionManager = new SessionManager(context.getApplicationContext());

        // --- 1. THE AUTHENTICATOR ---
        Authenticator tokenAuthenticator = new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                // Prevent infinite loops: if we already tried 2 times, give up
                if (responseCount(response) >= 2) {
                    return null;
                }

                // A. Get the Refresh Token from storage
                String refreshToken = sessionManager.getRefreshToken();
                if (refreshToken == null) return null; // No token? User must login manually.

                // B. Create a temporary API client just for this refresh call
                SupabaseAPI refreshApi = new Retrofit.Builder()
                        .baseUrl(SUPABASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(SupabaseAPI.class);


                Map<String, String> body = new HashMap<>();
                body.put("refresh_token", refreshToken);

                // C. Call Supabase SYNCHRONOUSLY to get new keys
                retrofit2.Response<AuthResponse> refreshResponse = refreshApi.refreshToken(SUPABASE_KEY, body).execute();

                if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                    // D. Success! Save the new tokens
                    String newAccess = refreshResponse.body().access_token;
                    String newRefresh = refreshResponse.body().refresh_token;
                    sessionManager.updateTokens(newAccess, newRefresh);

                    // E. Retry the ORIGINAL request with the NEW Access Token
                    return response.request().newBuilder()
                            .header("Authorization", "Bearer " + newAccess)
                            .build();
                } else {
                    // Refresh failed (token totally dead).
                    return null;
                }
            }
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .authenticator(tokenAuthenticator)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        myApi = retrofit.create(SupabaseAPI.class);
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    // returns the wrapper class (RetrofitClient)
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public SupabaseAPI getApi() {
        return myApi;
    }
}