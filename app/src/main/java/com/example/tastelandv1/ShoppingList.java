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
    private SupabaseAPI supabaseService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shoppingList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.RVShoppingList);
        ImageButton btnAdd = view.findViewById(R.id.BtnAddFoodItem);

        // 1. We don't need the complex listener anymore, just a simple adapter
        // (We will update the adapter code in Step 2)
        adapter = new ShoppingAdapter(shoppingList, null);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        supabaseService = RetrofitClient.getInstance().getApi();
        fetchShoppingList();

        // 2. NEW BUTTON LOGIC: Show a Popup Dialog
        btnAdd.setOnClickListener(v -> showAddItemDialog());
    }

    // --- NEW METHOD: Show the Popup ---
    private void showAddItemDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Add Shopping Item");

        // Set up the input
        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setHint("Type item name (e.g. Milk)");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemText = input.getText().toString().trim();
            if (!itemText.isEmpty()) {
                // Create the item object HERE, with the text already filled in
                ShoppingItem newItem = new ShoppingItem(itemText, false);
                addItemToCloud(newItem);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    private void fetchShoppingList() {
        SessionManager session = new SessionManager(getContext());
        if (session.getToken() == null) return;

        supabaseService.getItems(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken())
                .enqueue(new Callback<List<ShoppingItem>>() {
                    @Override
                    public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            shoppingList.clear();
                            shoppingList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {}
                });
    }

// Inside ShoppingList.java

    // --- UPDATED METHOD: Save to Cloud ---
    private void addItemToCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();

        if (token == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // "return=representation" ensures we get the ID back from Supabase
        supabaseService.addItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, "return=representation", item)
                .enqueue(new Callback<List<ShoppingItem>>() {
                    @Override
                    public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. Get the real item (now includes ID and UserID from DB)
                            ShoppingItem createdItem = response.body().get(0);

                            // 2. Add to list and refresh
                            shoppingList.add(createdItem);
                            adapter.notifyItemInserted(shoppingList.size() - 1);
                            recyclerView.smoothScrollToPosition(shoppingList.size() - 1);
                        } else {
                            Toast.makeText(getContext(), "Failed to save item", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateItemInCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();

        // If item has no ID, we can't update it (it hasn't been created yet)
        if (token == null || item.getId() == null) return;

        String idFilter = "id=eq." + item.getId();

        supabaseService.updateItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, item)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        // Successfully saved text.
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
    }
}