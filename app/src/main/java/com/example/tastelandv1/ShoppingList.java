package com.example.tastelandv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShoppingList extends Fragment {

    private ShoppingAdapter adapter;
    private List<ShoppingItem> shoppingList;
    private RecyclerView recyclerView;

    // This is the variable that was causing the error
    private SupabaseAPI supabaseService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize List and Views
        shoppingList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.RVShoppingList);
        ImageButton btnAdd = view.findViewById(R.id.BtnAddFoodItem);

        // 2. Setup RecyclerView
        adapter = new ShoppingAdapter(shoppingList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Initialize the Supabase Service
        // This connects to the RetrofitClient class we created earlier
        supabaseService = RetrofitClient.getInstance().getApi();

        // 4. Load existing data from the cloud immediately
        fetchShoppingList();

        // 5. Setup Add Button Logic
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new empty item
                ShoppingItem newItem = new ShoppingItem("", false);
                // Save it to Supabase
                addItemToCloud(newItem);
            }
        });

        // 6. Visual tweak for the Home Screen (Mini view)
        view.post(new Runnable() {
            @Override
            public void run() {
                if (view.getHeight() < 800) {
                    // Hide headers if the view is small
                    View ivHeader = view.findViewById(R.id.IVHeader);
                    if (ivHeader != null) ivHeader.setVisibility(View.GONE);

                    //View chatBtn = view.findViewById(R.id.IBMyFoodHeaderChat);
                    //if (chatBtn != null) chatBtn.setVisibility(View.GONE);

                    //View profileBtn = view.findViewById(R.id.IBMyFoodHeaderProfile);
                    //if (profileBtn != null) profileBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    // --- HELPER METHODS ---

    private void fetchShoppingList() {
        // 1. Get the user's token from SessionManager
        SessionManager session = new SessionManager(getContext());
        String userToken = session.getToken();

        // If no token, user is not logged in
        if (userToken == null) {
            Toast.makeText(getContext(), "Please login to see your list", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Prepare the Authorization Header
        String authHeader = "Bearer " + userToken;

        // 3. Call Supabase
        supabaseService.getItems(RetrofitClient.SUPABASE_KEY, authHeader).enqueue(new Callback<List<ShoppingItem>>() {
            @Override
            public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Success! Update the list
                    shoppingList.clear();
                    shoppingList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addItemToCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        String userToken = session.getToken();

        if (userToken == null) return;

        String authHeader = "Bearer " + userToken;

        // Call Supabase to add the item
        supabaseService.addItem(RetrofitClient.SUPABASE_KEY, authHeader, "return=minimal", item)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // If successful, reload the list to show the new item
                            fetchShoppingList();
                        } else {
                            Toast.makeText(getContext(), "Could not add item", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}