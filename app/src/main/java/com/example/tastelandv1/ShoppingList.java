package com.example.tastelandv1;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        adapter = new ShoppingAdapter(shoppingList, new ShoppingAdapter.OnItemChangeListener() {
            @Override
            public void onItemChanged(ShoppingItem item) {
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
        
        // Wrap EditText in a FrameLayout for padding
        FrameLayout container = new FrameLayout(getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        
        // Convert dp to pixels for standard dialog padding (24dp is standard)
        int paddingPx = (int) (24 * getResources().getDisplayMetrics().density);
        params.leftMargin = paddingPx;
        params.rightMargin = paddingPx;
        input.setLayoutParams(params);
        container.addView(input);
        
        builder.setView(container);

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

        supabaseService.addItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, "return=representation", item)
                .enqueue(new Callback<List<ShoppingItem>>() {
                    @Override
                    public void onResponse(Call<List<ShoppingItem>> call, Response<List<ShoppingItem>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            ShoppingItem createdItem = response.body().get(0);
                            shoppingList.add(createdItem);
                            adapter.notifyItemInserted(shoppingList.size() - 1);
                            recyclerView.smoothScrollToPosition(shoppingList.size() - 1);
                        } else {
                            Toast.makeText(getContext(), "Failed to save item.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error while adding item.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- API: Update Checkbox Status ---
    private void updateItemInCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (token == null || item.getId() == null) return;

        // Correct filter: "eq.123"
        String idFilter = "eq." + item.getId();

        // Create a map with only the field to be updated
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("is_checked", item.isChecked());

        supabaseService.updateItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter, updateMap)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            // Revert on failure
                            item.setChecked(!item.isChecked());
                            int position = shoppingList.indexOf(item);
                            if (position != -1) {
                                adapter.notifyItemChanged(position);
                            }
                            Toast.makeText(getContext(), "Failed to update item state.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        // Revert on failure
                        item.setChecked(!item.isChecked());
                        int position = shoppingList.indexOf(item);
                        if (position != -1) {
                            adapter.notifyItemChanged(position);
                        }
                        Toast.makeText(getContext(), "Network Error: Failed to update item.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- API: Delete Checked Items ---
    private void deleteCheckedItems() {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();
        if (token == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        List<ShoppingItem> itemsToDelete = new ArrayList<>();
        List<Long> idsToDelete = new ArrayList<>();
        for (ShoppingItem item : shoppingList) {
            if (item.isChecked()) {
                itemsToDelete.add(item);
                if (item.getId() != null) {
                    idsToDelete.add(item.getId());
                }
            }
        }

        if (itemsToDelete.isEmpty()) {
            Toast.makeText(getContext(), "No checked items to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idsToDelete.isEmpty()) { // Items exist only locally
            shoppingList.removeAll(itemsToDelete);
            adapter.notifyDataSetChanged();
            return;
        }

        // Correct filter: "in.(1,2,3)"
        String idFilter = "in.(" + idsToDelete.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";

        supabaseService.deleteShoppingItem(RetrofitClient.SUPABASE_KEY, "Bearer " + token, idFilter)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            shoppingList.removeAll(itemsToDelete);
                            adapter.notifyDataSetChanged(); // OK here, since multiple items are removed
                            Toast.makeText(getContext(), "Cleared " + itemsToDelete.size() + " items", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to clear items from database.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "Network error while clearing items.", Toast.LENGTH_SHORT).show();
                    }
                });
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
                    public void onFailure(Call<List<ShoppingItem>> call, Throwable t) {
                        Toast.makeText(getContext(), "Failed to fetch shopping list.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
