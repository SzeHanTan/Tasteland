package com.example.tastelandv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Make sure to import ImageButton

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MyFoodFragment extends Fragment {

    public MyFoodFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // This connects the XML layout to your fragment class.
        return inflater.inflate(R.layout.fragment_my_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find the button using the fragment's 'view' object.
        ImageButton addFoodButton = view.findViewById(R.id.BtnAddFood);

        // 2. Set the click listener on the button.
        addFoodButton.setOnClickListener(v -> {
            showAddFoodFragment();
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

}
