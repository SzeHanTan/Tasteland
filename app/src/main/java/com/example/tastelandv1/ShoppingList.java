package com.example.tastelandv1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private ImageButton btnClear;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views
        shoppingList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.RVShoppingList);
        ImageButton btnAdd = view.findViewById(R.id.BtnAddFoodItem);
        btnClear = view.findViewById(R.id.BtnClearChecked);

        // 2. Setup Adapter
        // We pass a listener that runs whenever a checkbox is clicked
        adapter = new ShoppingAdapter(shoppingList, new ShoppingAdapter.OnItemChangeListener() {
            @Override
            public void onItemChanged(ShoppingItem item) {
                // When user clicks checkbox, update DB immediately
                updateItemInCloud(item);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Initialize API
        supabaseService = RetrofitClient.getInstance().getApi();
        fetchShoppingList();

        // 4. Button Logic
        btnAdd.setOnClickListener(v -> showAddItemDialog());

        if (btnClear != null) {
            btnClear.setOnClickListener(v -> showClearConfirmDialog());
        }
    }

    // --- DIALOG: Add Item ---
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Shopping Item");

        final EditText input = new EditText(getContext());
        input.setHint("Type item name (e.g. Milk)");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String itemText = input.getText().toString().trim();
            if (!itemText.isEmpty()) {
                ShoppingItem newItem = new ShoppingItem(itemText, false);
                addItemToCloud(newItem);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- DIALOG: Confirm Clear ---
    private void showClearConfirmDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Clear Shopping List")
                .setMessage("Remove all checked items?")
                .setPositiveButton("Yes, Clear", (dialog, which) -> deleteCheckedItems())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- API: Add Item ---
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
                            ShoppingItem createdItem = response.body().get(0);
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

    // --- API: Update Checkbox Status ---
    private void updateItemInCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (token == null || item.getId() == null) return;

        String idFilter = "id=eq." + item.getId();

        supabaseService.updateItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, item)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
    }

    // --- API: Delete Checked Items ---
    private void deleteCheckedItems() {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (token == null) return;

        List<ShoppingItem> itemsToDelete = new ArrayList<>();
        for (ShoppingItem item : shoppingList) {
            if (item.isChecked()) {
                itemsToDelete.add(item);
            }
        }

        if (itemsToDelete.isEmpty()) {
            Toast.makeText(getContext(), "No checked items to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        for (ShoppingItem item : itemsToDelete) {
            String idFilter = "id=eq." + item.getId();
            supabaseService.deleteShoppingItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {}
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
        }

        shoppingList.removeAll(itemsToDelete);
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Cleared " + itemsToDelete.size() + " items", Toast.LENGTH_SHORT).show();
    }

    // --- API: Fetch List ---
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
}