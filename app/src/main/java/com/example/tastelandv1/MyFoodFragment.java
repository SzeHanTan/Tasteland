package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFoodFragment extends Fragment {

    // 1. Declare three RecyclerViews and Adapters
    private RecyclerView rvPrevious, rvToday, rvFuture;
    private FoodAdapter adapterPrevious, adapterToday, adapterFuture;

    private List<FoodItem> allFoodItems = new ArrayList<>();
    private String userToken;

    public MyFoodFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Initialize the three RecyclerViews
        rvPrevious = view.findViewById(R.id.RVPrevious);
        rvToday = view.findViewById(R.id.RVToday);
        rvFuture = view.findViewById(R.id.RVFuture);

        // Set LayoutManagers for all
        rvPrevious.setLayoutManager(new LinearLayoutManager(getContext()));
        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFuture.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Header Buttons
        ImageButton addFoodButton = view.findViewById(R.id.BtnAddFoodItem);
        //ImageButton chatButton = view.findViewById(R.id.IBMyFoodHeaderChat);
        //ImageButton profileButton = view.findViewById(R.id.IBMyFoodHeaderProfile);

        // Listen for the "refresh_list" result from the AddFood fragment
        getChildFragmentManager().setFragmentResultListener("refresh_request", this, (requestKey, bundle) -> {
            // When the signal is received, fetch fresh data from the cloud
            fetchFoodFromSupabase();
        });
        // ----------------------

        fetchFoodFromSupabase();
        // Get User Token
        SessionManager session = new SessionManager(getContext());
        userToken = session.getToken();

        // Set Click Listeners
        addFoodButton.setOnClickListener(v -> showAddFoodFragment());
        //chatButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), GroupChatList.class)));
        //profileButton.setOnClickListener(v -> Toast.makeText(getActivity(), "Profile Clicked", Toast.LENGTH_SHORT).show());

        // 3. Initial Data Fetch
        fetchFoodFromSupabase();
    }

    private void fetchFoodFromSupabase() {
        String authToken = "Bearer " + userToken;

        RetrofitClient.getInstance().getApi().getFoodItems(authToken, RetrofitClient.SUPABASE_KEY, "*", "due_date.asc")

                .enqueue(new Callback<List<FoodItem>>() {
                    @Override
                    public void onResponse(Call<List<FoodItem>> call, Response<List<FoodItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            allFoodItems = response.body();
                            // 4. Categorize and display all lists at once
                            categorizeAndDisplayAll();
                        } else {
                            Log.e("SupabaseError", "Fetch failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<FoodItem>> call, Throwable t) {
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void categorizeAndDisplayAll() {
        List<FoodItem> previousList = new ArrayList<>();
        List<FoodItem> todayList = new ArrayList<>();
        List<FoodItem> futureList = new ArrayList<>();

        Date today = resetTime(new Date());

        for (FoodItem item : allFoodItems) {
            Date itemDate = resetTime(item.dueDate);

            if (itemDate.before(today)) {
                previousList.add(item);
            } else if (itemDate.equals(today)) {
                todayList.add(item);
            } else if (itemDate.after(today)) {
                futureList.add(item);
            }
        }

        // 5. Initialize and set adapters for each list
        // Now calling deleteFoodFromSupabase when the checkbox is clicked
        adapterPrevious = new FoodAdapter(previousList, (item, isChecked) -> {
            if (isChecked) deleteFoodFromSupabase(item);
        });
        rvPrevious.setAdapter(adapterPrevious);

        adapterToday = new FoodAdapter(todayList, (item, isChecked) -> {
            if (isChecked) deleteFoodFromSupabase(item);
        });
        rvToday.setAdapter(adapterToday);

        adapterFuture = new FoodAdapter(futureList, (item, isChecked) -> {
            if (isChecked) deleteFoodFromSupabase(item);
        });
        rvFuture.setAdapter(adapterFuture);

    }

    private void updateFoodStatus(FoodItem item, boolean isChecked) {
        String authToken = "Bearer " + userToken;
        Map<String, Object> update = new HashMap<>();
        update.put("is_finished", isChecked);

        RetrofitClient.getInstance().getApi().updateFoodItemStatus(
                authToken, RetrofitClient.SUPABASE_KEY, "eq." + item.id, update
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    item.isFinished = isChecked;
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteFoodFromSupabase(FoodItem item) {
        String authToken = "Bearer " + userToken;

        RetrofitClient.getInstance().getApi().deleteFoodItem(
                authToken,
                RetrofitClient.SUPABASE_KEY,
                "eq." + item.id
        ).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Item finished and removed!", Toast.LENGTH_SHORT).show();

                    // Remove from the local list and notify the adapters
                    allFoodItems.remove(item);
                    categorizeAndDisplayAll();
                } else {
                    Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showAddFoodFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.findFragmentById(R.id.fragment_add_food_container) == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.replace(R.id.fragment_add_food_container, AddFood.class, null);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private Date resetTime(Date date) {
        if (date == null) return new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
