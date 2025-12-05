package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Make sure this is imported
import android.widget.Toast; // For showing placeholder messages

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Find views using the NEW IDs from your layout ---
        ImageButton chatButton = view.findViewById(R.id.IBHomeHeaderChat);
        ImageButton profileButton = view.findViewById(R.id.IBHomeHeaderProfile);
        ImageButton addShoppingButton = view.findViewById(R.id.BtnAddShopping);
        ImageButton addFoodHomeButton = view.findViewById(R.id.BtnAddFoodHome);

        // --- Set up the click listeners ---

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

        // 3. Add Shopping Button (placeholder action)
        addShoppingButton.setOnClickListener(v -> {
            // TODO: Navigate to Add Shopping List screen
            Toast.makeText(getActivity(), "Add Shopping Clicked", Toast.LENGTH_SHORT).show();
        });

        // 4. Add Food Button (placeholder action)
        addFoodHomeButton.setOnClickListener(v -> {
            // Get the BottomNavigationView from the hosting MainActivity
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    // Set the selected item to be the My Food tab
                    bottomNav.setSelectedItemId(R.id.nav_food_list);
                }
            }
        });

    }
}
