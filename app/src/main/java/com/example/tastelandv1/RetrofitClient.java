package com.example.tastelandv1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance = null;
    private SupabaseAPI myApi;
    public static final String SUPABASE_URL = "https://qdukyuxdzlgypanlzggm.supabase.co/";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFkdWt5dXhkemxneXBhbmx6Z2dtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU4NjIxMjgsImV4cCI6MjA4MTQzODEyOH0.A9IcD93BFurfFSws6Urc237L7yxu-XOp5RzONvDO_3E";

    // private constructor to set up Retrofit once
    private RetrofitClient() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
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