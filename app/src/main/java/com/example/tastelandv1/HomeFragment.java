package com.example.tastelandv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

        // --- 1. SETUP "ADD SHOPPING" BUTTON ---
        ImageButton addShoppingButton = view.findViewById(R.id.BtnAddShopping);
        if (addShoppingButton != null) {
            addShoppingButton.setOnClickListener(v -> {
                // Logic from Home.java: Navigate to the ShoppingList Fragment
                getParentFragmentManager().beginTransaction()
                        // Ensure R.id.FCVMain matches the ID in your activity_main.xml
                        .replace(R.id.FCVMain, new ShoppingList())
                        .addToBackStack(null) // Allows user to press Back button to return here
                        .commit();
            });
        }

        // --- 2. SETUP "ADD FOOD" BUTTON ---
        ImageButton addFoodHomeButton = view.findViewById(R.id.BtnAddFoodHome);
        if (addFoodHomeButton != null) {
            addFoodHomeButton.setOnClickListener(v -> {
                // Logic from HomeFragment.java: Switch Bottom Navigation Tab
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_food_list);
                    }
                }
            });
        }
    }
}