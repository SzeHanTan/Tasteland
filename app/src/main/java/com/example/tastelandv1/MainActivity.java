package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View universalBackButton;

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
        universalBackButton = findViewById(R.id.universal_back_button);

        // --- 1. VISIBILITY CONTROLLER ---
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f.getId() == R.id.FCVMain) {
                    updateHeaderAndProfileVisibility(f);
                    syncBottomNav(f);
                }
            }
        }, true);

        // --- 2. NAVIGATION SETUP ---
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(navListener);

            // Check if we were directed here with a specific tab ID
            int targetNavId = getIntent().getIntExtra("TARGET_NAV_ID", R.id.nav_home);
            
            if (savedInstanceState == null) {
                navigateToTab(targetNavId);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int targetNavId = intent.getIntExtra("TARGET_NAV_ID", -1);
        if (targetNavId != -1 && bottomNav != null) {
            navigateToTab(targetNavId);
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

        boolean isHome = currentFragment instanceof HomeFragment;
        boolean showProfile = isHome || (currentFragment instanceof MyFoodFragment);
        boolean showHeader = showProfile || (currentFragment instanceof RecipeFragment);

        if (headerContainer != null) {
            headerContainer.setVisibility(showHeader ? View.VISIBLE : View.GONE);
        }

        if (profileContainer != null) {
            profileContainer.setVisibility(showProfile ? View.VISIBLE : View.GONE);
        }

        // --- UNIVERSAL BACK BUTTON LOGIC ---
        if (universalBackButton != null) {
            // Show back button on all pages EXCEPT home
            universalBackButton.setVisibility(isHome ? View.GONE : View.VISIBLE);
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                navigateToTab(item.getItemId());
                return true;
            };

    private void navigateToTab(int itemId) {
        Fragment selectedFragment = null;
        String tag = String.valueOf(itemId);

        // If the same tab is already visible, do nothing
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.FCVMain);
        if (current != null && tag.equals(current.getTag())) {
            return;
        }

        if (itemId == R.id.nav_home) {
            // Clear entire back stack when going home
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FCVMain, selectedFragment, tag);
            
            // Only add to back stack if NOT Home
            if (itemId != R.id.nav_home) {
                transaction.addToBackStack(null);
            }
            
            transaction.commit();
        }
    }

    private void syncBottomNav(Fragment fragment) {
        if (bottomNav == null) return;
        
        int itemId = -1;
        if (fragment instanceof HomeFragment) itemId = R.id.nav_home;
        else if (fragment instanceof MyFoodFragment) itemId = R.id.nav_food_list;
        else if (fragment instanceof RecipeFragment) itemId = R.id.nav_recipe;
        else if (fragment instanceof InsightsFragment) itemId = R.id.nav_insight;
        else if (fragment instanceof Profile) itemId = R.id.nav_profile;

        if (itemId != -1 && bottomNav.getSelectedItemId() != itemId) {
            // Use setChecked to avoid re-triggering the listener
            bottomNav.getMenu().findItem(itemId).setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
