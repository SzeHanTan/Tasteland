package com.example.tastelandv1;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class MyFood extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_food);

        // Find the button by its ID from your XML
        ImageButton addFoodButton = findViewById(R.id.BtnAddFood);

        // Set the click listener on the button
        addFoodButton.setOnClickListener(view -> {
            showAddFoodFragment();
        });
    }

    private void showAddFoodFragment() {
        // Use the FragmentManager to handle fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        // First, check if the fragment is already added. This prevents creating duplicates
        // if the user taps the button multiple times quickly.
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.fragment_add_food);

        if (existingFragment == null) {
            // Begin a new transaction
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            // Allows for better animations and transitions
            fragmentTransaction.setReorderingAllowed(true);

            // Replace the contents of the container with a new instance of your fragment
            // R.id.fragment_add_food is the ID of your FragmentContainerView
            fragmentTransaction.replace(R.id.fragment_add_food, AddFood.class, null);

            // Add the transaction to the back stack. This is CRUCIAL.
            // It allows the user to close the fragment by pressing the back button.
            fragmentTransaction.addToBackStack(null);

            // Commit the transaction to make the changes visible
            fragmentTransaction.commit();
        }
    }
}
