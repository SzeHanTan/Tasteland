package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Make sure to import ImageButton
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MyFoodFragment extends Fragment {

    public MyFoodFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find the button using the fragment's 'view' object.
        ImageButton addFoodButton = view.findViewById(R.id.BtnAddShoppingItem);
        ImageButton chatButton = view.findViewById(R.id.IBMyFoodHeaderChat);
        ImageButton profileButton = view.findViewById(R.id.IBMyFoodHeaderProfile);

        // 2. Set the click listener on the button.
        addFoodButton.setOnClickListener(v -> {
            showAddFoodFragment();
        });

        // 1. Chat Button Navigation
        chatButton.setOnClickListener(v -> {
            // Create an Intent to start GroupChatList activity
            Intent intent = new Intent(getActivity(), GroupChatList.class);
            startActivity(intent);
        });

        // 2. Profile Button (placeholder action)
        profileButton.setOnClickListener(v -> {
            // TODO: Handle profile button click (e.g., navigate to ProfileFragment)
            Toast.makeText(getActivity(), "Profile Clicked", Toast.LENGTH_SHORT).show();
        });

    }


    private void showAddFoodFragment() {
        // Use getChildFragmentManager() for nested fragments.
        // This tells the fragment to manage its own children.
        FragmentManager fragmentManager = getChildFragmentManager();

        // Check if the fragment is already added to this container.
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.fragment_add_food);

        if (existingFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);

            // IMPORTANT: Use the ID of the NEW, LOCAL container.
            fragmentTransaction.replace(R.id.fragment_add_food, AddFood.class, null);

            // You can still use addToBackStack. The back button will now
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void categorizeAndDisplay(List<FoodItem> items) {
        Date today = resetTime(new Date());

        List<FoodItem> previous = new ArrayList<>();
        List<FoodItem> todayItems = new ArrayList<>();
        List<FoodItem> future = new ArrayList<>();

        for (FoodItem item : items) {
            Date itemDate = resetTime(item.dueDate);
            if (itemDate.before(today)) {
                previous.add(item);
            } else if (itemDate.equals(today)) {
                todayItems.add(item);
            } else {
                future.add(item);
            }
        }
        // Update your UI/Adapters here
    }

    private Date resetTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


}
