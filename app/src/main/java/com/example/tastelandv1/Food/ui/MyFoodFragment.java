package com.example.tastelandv1.Food.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar; // Import
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastelandv1.Food.database.FoodItem;
import com.example.tastelandv1.Food.database.FoodRepository;
import com.example.tastelandv1.R;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyFoodFragment extends Fragment {

    private RecyclerView rvPrevious, rvToday, rvFuture;
    private FoodAdapter adapterPrevious, adapterToday, adapterFuture;
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private FoodRepository repository;
    private ProgressBar progressBar; // NFR: Loading Logic
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MyFoodFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new FoodRepository(getContext()); // Use Repository

        rvPrevious = view.findViewById(R.id.RVPrevious);
        rvToday = view.findViewById(R.id.RVToday);
        rvFuture = view.findViewById(R.id.RVFuture);
        progressBar = view.findViewById(R.id.PGMyFood);

        rvPrevious.setLayoutManager(new LinearLayoutManager(getContext()));
        rvToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFuture.setLayoutManager(new LinearLayoutManager(getContext()));

        ImageButton addFoodButton = view.findViewById(R.id.BtnAddFoodItem);

        getChildFragmentManager().setFragmentResultListener("refresh_request", this, (requestKey, bundle) -> {
            repository.invalidateCache(); // Clear cache to fetch fresh
            fetchFoodFromSupabase();
        });

        addFoodButton.setOnClickListener(v -> showAddFoodFragment());

        fetchFoodFromSupabase();
    }

    private void fetchFoodFromSupabase() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // Show Loading

        repository.getFoodItems(new FoodRepository.DataCallback() {
            @Override
            public void onSuccess(List<FoodItem> data) {
                if (progressBar != null) progressBar.setVisibility(View.GONE); // Hide Loading
                allFoodItems = data;
                categorizeAndDisplayAll();
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void categorizeAndDisplayAll() {
        executor.execute(() -> {
            List<FoodItem> previousList = new ArrayList<>();
            List<FoodItem> todayList = new ArrayList<>();
            List<FoodItem> futureList = new ArrayList<>();

            Date today = resetTime(new Date());

            for (FoodItem item : allFoodItems) {
                Date itemDate = resetTime(item.dueDate);
                if (itemDate.before(today)) previousList.add(item);
                else if (itemDate.equals(today)) todayList.add(item);
                else if (itemDate.after(today)) futureList.add(item);
            }

            // Update UI on Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    adapterPrevious = new FoodAdapter(previousList, (item, isChecked) -> { if (isChecked) deleteFoodFromSupabase(item); });
                    rvPrevious.setAdapter(adapterPrevious);

                    adapterToday = new FoodAdapter(todayList, (item, isChecked) -> { if (isChecked) deleteFoodFromSupabase(item); });
                    rvToday.setAdapter(adapterToday);

                    adapterFuture = new FoodAdapter(futureList, (item, isChecked) -> { if (isChecked) deleteFoodFromSupabase(item); });
                    rvFuture.setAdapter(adapterFuture);
                });
            }
        });
    }

    private void deleteFoodFromSupabase(FoodItem item) {
        allFoodItems.remove(item);
        categorizeAndDisplayAll();

        String userToken = new SessionManager(getContext()).getToken();
        RetrofitClient.getInstance(getContext()).getApi().deleteFoodItem("Bearer " + userToken, RetrofitClient.SUPABASE_KEY, "eq." + item.id)
                .enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Item finished!", Toast.LENGTH_SHORT).show();
                            allFoodItems.remove(item);
                            repository.invalidateCache(); // Update Cache
                            categorizeAndDisplayAll();
                        } else {
                            Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                            allFoodItems.add(item);
                            categorizeAndDisplayAll();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        allFoodItems.add(item);
                        categorizeAndDisplayAll();
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