package com.example.tastelandv1;

import java.util.List;
import java.util.Map;

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

    @POST("rest/v1/profiles")
    Call<Void> createProfile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> profileData
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

    @GET("rest/v1/food_items")
    Call<List<FoodItem>> getFoodItems(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Query("select") String select,
            @Query("order") String order // To sort by dueDate
    );

    @PATCH("rest/v1/food_items")
    Call<Void> updateFoodItemStatus(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Query("id") String eqId,
            @Body Map<String, Object> updates
    );

    @POST("rest/v1/food_items")
    Call<Void> createFoodItem(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Body FoodItem foodItem
    );

    @DELETE("rest/v1/food_items")
    Call<Void> deleteFoodItem(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Query("id") String idFilter // Pass "eq." + item.id
    );

    @GET("rest/v1/communities")
    Call<List<CommunityModel>> getCommunities(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("select") String select
    );

    @GET("rest/v1/communities")
    Call<List<CommunityModel>> getCommunityByCode(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("invitation_code") String codeFilter, // e.g., "eq.klangk"
            @Query("select") String select
    );

    @POST("rest/v1/communities")
    Call<Void> createCommunity(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Header("Prefer") String returnType,
            @Body CommunityModel community
    );

    @GET("rest/v1/community_posts")
    Call<List<ChatMessage>> getCommunityPosts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("group_id") String groupIdFilter, // New: filter by group
            @Query("select") String select,         // Use "*"
            @Query("order") String order            // Use "created_at.asc"
    );

    @POST("rest/v1/community_posts")
    Call<Void> postMessage(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Header("Prefer") String returnType,
            @Body ChatMessage message
    );

    @PATCH("rest/v1/community_posts")
    Call<Void> updateLikeCount(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idFilter, // e.g., "eq.101"
            @Body Map<String, Object> updateData
    );

}