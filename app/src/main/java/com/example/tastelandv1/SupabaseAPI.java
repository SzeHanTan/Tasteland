package com.example.tastelandv1;

import com.example.tastelandv1.Recipe.database.FavoriteEntry;
import com.example.tastelandv1.Recipe.database.FavoriteRequest;
import com.example.tastelandv1.Recipe.database.Recipe;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface SupabaseAPI {

    // --- AUTHENTICATION ENDPOINTS ---

    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(
            @Header("apikey") String apiKey,
            @Body AuthRequest request
    );

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(
            @Header("apikey") String apiKey,
            @Body AuthRequest request
    );

    // --- DATABASE ENDPOINTS ---

    @GET("rest/v1/shopping_items?select=*&order=id.asc")
    Call<List<ShoppingItem>> getItems(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token // We now pass the user token here
    );

    @POST("rest/v1/shopping_items")
    Call<Void> addItem(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Header("Prefer") String returnType,
            @Body ShoppingItem item
    );

    @GET("rest/v1/profiles?select=*")
    Call<List<UserProfile>> getMyProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/profiles")
    Call<Void> updateProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idQuery, // We need to specify WHICH row to update (eq.USER_ID)
            @Body UserProfile profile
    );

    // --- RECIPE ENDPOINTS ---

    // 1. Get All Recipes (Standard)
    @GET("rest/v1/recipes?select=*")
    Call<List<Recipe>> getAllRecipes(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- FAVORITES ENDPOINTS (NEW) ---

    // 2. Get My Favorites (Returns a list of IDs for the logged-in user)
    @GET("rest/v1/favorites?select=recipe_id")
    Call<List<FavoriteEntry>> getMyFavorites(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdQuery // "eq.USER_UUID"
    );

    // 3. Add a Favorite (POST to favorites table)
    @POST("rest/v1/favorites")
    Call<Void> addFavorite(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body FavoriteRequest body // Needs a simple object with recipe_id and user_id
    );

    // 4. Remove a Favorite (DELETE from favorites table)
    @DELETE("rest/v1/favorites")
    Call<Void> removeFavorite(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdQuery,   // "eq.USER_UUID"
            @Query("recipe_id") String recipeIdQuery // "eq.123"
    );
}