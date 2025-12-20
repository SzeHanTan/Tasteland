package com.example.tastelandv1;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- 1. NEW: VISIBILITY CONTROLLER ---
        // This detects every time a fragment changes and hides/shows the header automatically
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);

                // Only check visibility if the fragment is inside your Main Container (FCVMain)
                if (f.getId() == R.id.FCVMain) {
                    updateHeaderAndProfileVisibility(f);
                }
            }
        }, true);

        // --- 2. EXISTING: NAVIGATION SETUP ---
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.FCVMain,
                    new HomeFragment()).commit();
        }
    }

    // --- HELPER METHOD TO SHOW/HIDE HEADERS ---
    private void updateHeaderAndProfileVisibility(Fragment currentFragment) {
        // Find the containers for your Header and Community Profile
        // IMPORTANT: Ensure your activity_main.xml has these IDs!
        View headerContainer = findViewById(R.id.header_container);
        View profileContainer = findViewById(R.id.community_profile_container);

        // Logic: Who gets to see the Profile buttons? (Home & MyFood)
        boolean showProfile = (currentFragment instanceof HomeFragment) ||
                (currentFragment instanceof MyFoodFragment);

        // Logic: Who gets to see the Header? (Home, MyFood, & Recipes)
        // (Assuming you have a RecipeFragment, otherwise remove that part)
        boolean showHeader = showProfile || (currentFragment instanceof RecipeFragment);

        // Apply Visibility
        if (headerContainer != null) {
            headerContainer.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }

        if (profileContainer != null) {
            profileContainer.setVisibility(showProfile ? View.VISIBLE : View.GONE);
        }
    }

    // --- EXISTING: NAVIGATION LISTENER ---
    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_food_list) {
                    selectedFragment = new MyFoodFragment();
                } else if (itemId == R.id.nav_insight) {
                    selectedFragment = new InsightsFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.FCVMain, selectedFragment)
                            .commit();
                }
                return true;
            };
}