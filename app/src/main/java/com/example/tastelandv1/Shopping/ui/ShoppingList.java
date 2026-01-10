package com.example.tastelandv1.Shopping.ui;

import android.annotation.SuppressLint;
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

import com.example.tastelandv1.R;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Shopping.database.ShoppingItem;
import com.example.tastelandv1.Shopping.database.ShoppingRepository;
import com.example.tastelandv1.Backend.SupabaseAPI;

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
    private ShoppingRepository repository;
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

        // Initialize Repository
        repository = new ShoppingRepository(getContext());
        supabaseService = RetrofitClient.getInstance(getContext()).getApi();

        // 2. Setup Adapter
        adapter = new ShoppingAdapter(shoppingList,
                this::updateItemInCloud);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Initialize API
        supabaseService = RetrofitClient.getInstance(getContext()).getApi();
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
        // Optimistic: Add to UI immediately
        shoppingList.add(item);
        adapter.notifyItemInserted(shoppingList.size() - 1);

        SessionManager session = new SessionManager(getContext());
        supabaseService.addItem(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), "return=representation", item)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<ShoppingItem>> call, @NonNull Response<List<ShoppingItem>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Update the local item with the ID from server
                            ShoppingItem serverItem = response.body().get(0);
                            item.setId(serverItem.getId());
                            repository.addToCache(serverItem); // Update Cache
                        }
                    }
                    @Override public void onFailure(@NonNull Call<List<ShoppingItem>> call, @NonNull Throwable t) {}
                });
    }

    // --- API: Update Checkbox Status ---
    private void updateItemInCloud(ShoppingItem item) {
        SessionManager session = new SessionManager(getContext());
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("is_checked", item.isChecked());

        supabaseService.updateItem(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), "eq." + item.getId(), updateMap)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if(!response.isSuccessful()) revertItemState(item);
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) { revertItemState(item); }
                });
    }

    private void revertItemState(ShoppingItem item) {
        item.setChecked(!item.isChecked());
        adapter.notifyDataSetChanged();
        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
    }

    // --- API: Delete Checked Items ---
    private void deleteCheckedItems() {
        SessionManager session = new SessionManager(getContext());
        List<ShoppingItem> toDelete = new ArrayList<>();
        List<Long> ids = new ArrayList<>();

        for (ShoppingItem item : shoppingList) {
            if (item.isChecked()) {
                toDelete.add(item);
                if (item.getId() != null) ids.add(item.getId());
            }
        }

        // Optimistic UI Removal
        shoppingList.removeAll(toDelete);
        adapter.notifyDataSetChanged();

        if (!ids.isEmpty()) {
            String filter = "in.(" + ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
            supabaseService.deleteShoppingItem(RetrofitClient.SUPABASE_KEY, "Bearer " + session.getToken(), filter)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if(response.isSuccessful()) repository.invalidateCache(); // Clear cache to be safe
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) {}
                    });
        }
    }

    // --- API: Fetch List ---
    private void fetchShoppingList() {
        repository.getShoppingList(new ShoppingRepository.DataCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<ShoppingItem> data) {
                shoppingList.clear();
                shoppingList.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
