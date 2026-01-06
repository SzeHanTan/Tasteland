package com.example.tastelandv1;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private SupabaseAPI myApi;
    public static final String SUPABASE_URL = "https://qdukyuxdzlgypanlzggm.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkdWt5dXhkemxneXBhbmx6Z2dtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NjIxMjgsImV4cCI6MjA4MTQzODEyOH0.A9IcD93BFurfFSws6Urc237L7yxu-XOp5RzONvDO_3E";

    // private constructor to set up Retrofit once
    private RetrofitClient() {

        // 1. Create a custom OkHttpClient with longer timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // Increase to 60s
                .readTimeout(60, TimeUnit.SECONDS)    // Increase to 60s
                .writeTimeout(60, TimeUnit.SECONDS)   // Increase to 60s
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        myApi = retrofit.create(SupabaseAPI.class);
    }

    // returns the wrapper class (RetrofitClient)
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public SupabaseAPI getApi() {
        return myApi;
    }
}