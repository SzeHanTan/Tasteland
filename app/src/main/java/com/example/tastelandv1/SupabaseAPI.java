package com.example.tastelandv1;

import com.example.tastelandv1.Recipe.database.FavoriteEntry;
import com.example.tastelandv1.Recipe.database.FavoriteRequest;
import com.example.tastelandv1.Recipe.database.Recipe;

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
            @Header("Authorization") String token
    );

    @PATCH("rest/v1/shopping_items")
    Call<Void> updateItem(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idFilter,
            @Body Map<String, Object> item
    );

    @POST("rest/v1/shopping_items")
    Call<List<ShoppingItem>> addItem(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Header("Prefer") String returnType,
            @Body ShoppingItem item
    );

    @DELETE("rest/v1/shopping_items")
    Call<Void> deleteShoppingItem(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idFilter
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
            @Query("id") String idQuery,
            @Body UserProfile profile
    );

    @GET("rest/v1/food_items")
    Call<List<FoodItem>> getFoodItems(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Query("select") String select,
            @Query("order") String order
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
            @Query("id") String idFilter
    );

    // --- COMMUNITY & MEMBERSHIP ---

    @GET("rest/v1/community_members")
    Call<List<Map<String, Object>>> getMemberRecords(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdFilter,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/communities")
    Call<List<CommunityModel>> getCommunitiesByIds(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idsFilter, 
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/communities")
    Call<List<CommunityModel>> getCommunities(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("rest/v1/communities")
    Call<List<CommunityModel>> getCommunityByCode(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("invitation_code") String codeFilter,
            @Query("select") String select
    );

    @POST("rest/v1/communities")
    Call<List<CommunityModel>> createCommunity(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Header("Prefer") String returnType,
            @Body CommunityModel community
    );

    @PATCH("rest/v1/communities")
    Call<Void> updateCommunity(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idFilter,
            @Body Map<String, Object> updateData
    );

    @POST("rest/v1/community_members")
    Call<Void> joinCommunity(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> membershipData
    );

    @DELETE("rest/v1/community_members")
    Call<Void> leaveCommunity(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdFilter,
            @Query("community_id") String communityIdFilter
    );

    // --- MESSAGING ---

    @GET("rest/v1/community_posts")
    Call<List<ChatMessage>> getCommunityPosts(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("group_id") String groupIdFilter,
            @Query("parent_message_id") String parentIdFilter,
            @Query("is_pinned") String isPinnedFilter,
            @Query("select") String select,
            @Query("order") String order
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
            @Query("id") String idFilter,
            @Body Map<String, Object> updateData
    );

    @PATCH("rest/v1/community_posts")
    Call<Void> updatePinStatus(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String idFilter,
            @Query("group_id") String groupFilter,
            @Body Map<String, Object> updateData
    );

    // --- LIKE TRACKING ---

    @GET("rest/v1/message_likes")
    Call<List<Map<String, Object>>> getMyLikes(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdFilter
    );

    @POST("rest/v1/message_likes")
    Call<Void> addLikeRecord(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Body Map<String, Object> likeData
    );

    @DELETE("rest/v1/message_likes")
    Call<Void> removeLikeRecord(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("user_id") String userIdFilter,
            @Query("message_id") String messageIdFilter
    );

    // --- RECIPE ENDPOINTS ---

    // 1. Get All Recipes (Standard)
    @GET("rest/v1/recipes?select=*")
    Call<List<Recipe>> getAllRecipes(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token
    );

    // --- FAVORITES ENDPOINTS ---

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

    @GET("rest/v1/notification") // Matches your 'notification' table
    Call<List<NotificationItem>> getNotifications(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("select") String select,       // Added for standard Supabase query
            @Query("user_id") String userIdFilter, // e.g. "eq.UUID"
            @Query("order") String order          // e.g. "created_at.desc"
    );

    @PATCH("rest/v1/notification")
    Call<Void> updateNotificationStatus(
            @Header("apikey") String apiKey,
            @Header("Authorization") String token,
            @Query("id") String notificationIdFilter,
            @Body Map<String, Object> updates
    );


}
