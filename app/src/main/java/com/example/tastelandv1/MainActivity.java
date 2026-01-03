package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- 0. SESSION GUARD ---
        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // --- 1. VISIBILITY CONTROLLER ---
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f.getId() == R.id.FCVMain) {
                    updateHeaderAndProfileVisibility(f);
                }
            }
        }, true);

        // --- 2. NAVIGATION SETUP ---
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(navListener);

            // Check if we were directed here with a specific tab ID
            int targetNavId = getIntent().getIntExtra("TARGET_NAV_ID", R.id.nav_home);
            
            if (savedInstanceState == null) {
                bottomNav.setSelectedItemId(targetNavId);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int targetNavId = intent.getIntExtra("TARGET_NAV_ID", -1);
        if (targetNavId != -1 && bottomNav != null) {
            bottomNav.setSelectedItemId(targetNavId);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateHeaderAndProfileVisibility(Fragment currentFragment) {
        View headerContainer = findViewById(R.id.header_container);
        View profileContainer = findViewById(R.id.community_profile_container);

        boolean showProfile = (currentFragment instanceof HomeFragment) ||
                (currentFragment instanceof MyFoodFragment);

        boolean showHeader = showProfile || (currentFragment instanceof RecipeFragment);

        if (headerContainer != null) {
            headerContainer.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }

        if (profileContainer != null) {
            profileContainer.setVisibility(showProfile ? View.VISIBLE : View.GONE);
        }
    }

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
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new Profile();
                } else if (itemId == R.id.nav_recipe) {
                    selectedFragment = new RecipeFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.FCVMain, selectedFragment)
                            .commit();
                }
                return true;
            };
}