package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Community.GroupChatList;
import com.example.tastelandv1.Insights.ui.InsightsFragment;
import com.example.tastelandv1.Recipe.ui.RecipeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View universalBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        universalBackButton = findViewById(R.id.universal_back_button);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f.getId() == R.id.FCVMain) {
                    updateHeaderVisibility(f);
                    syncBottomNav(f);
                }
            }
        }, true);

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("TARGET_NAV_ID", R.id.nav_home);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {
                    navigateToTab(id);
                }
                return true;
            });

            int targetNavId = getIntent().getIntExtra("TARGET_NAV_ID", R.id.nav_home);
            if (savedInstanceState == null) {
                navigateToTab(targetNavId);
            }
        }
    }

    // --- ADDED: Method to refresh Header fragment immediately ---
    public void refreshHeader() {
        Fragment headerFragment = getSupportFragmentManager().findFragmentById(R.id.header_container);
        if (headerFragment instanceof Header) {
            // Option A: Call its update method directly
            ((Header) headerFragment).updateGreeting();
            // Option B: Sync with DB too
            ((Header) headerFragment).fetchUserName();
        }
        
        // Also send broadcast for any other listeners
        sendBroadcast(new Intent("com.example.tastelandv1.UPDATE_HEADER"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        int targetNavId = intent.getIntExtra("TARGET_NAV_ID", -1);
        if (targetNavId != -1) {
            navigateToTab(targetNavId);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateHeaderVisibility(Fragment currentFragment) {
        View headerContainer = findViewById(R.id.header_container);
        ImageView backgroundHeader = findViewById(R.id.imageView2);
        boolean isHome = currentFragment instanceof HomeFragment;

        if (headerContainer != null) headerContainer.setVisibility(isHome ? View.VISIBLE : View.GONE);
        if (backgroundHeader != null) backgroundHeader.setVisibility(isHome ? View.VISIBLE : View.GONE);
        if (universalBackButton != null) universalBackButton.setVisibility(isHome ? View.GONE : View.VISIBLE);
    }

    public void navigateToRecipesWithCategory(String categoryId) {
        RecipeFragment selectedFragment = RecipeFragment.newInstance(categoryId);
        String tag = String.valueOf(R.id.nav_recipe);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.FCVMain, selectedFragment, tag);
        ft.addToBackStack(null);
        ft.commit();

        if (bottomNav != null) {
            bottomNav.getMenu().findItem(R.id.nav_recipe).setChecked(true);
        }
    }

    private void navigateToTab(int itemId) {
        if (itemId == R.id.nav_community) {
            Intent intent = new Intent(this, GroupChatList.class);
            startActivity(intent);
            return; 
        }

        if (itemId == R.id.nav_home) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FCVMain, new HomeFragment(), String.valueOf(R.id.nav_home))
                    .commit();
            return;
        }

        Fragment selectedFragment = null;
        String tag = String.valueOf(itemId);

        Fragment current = getSupportFragmentManager().findFragmentById(R.id.FCVMain);
        if (current != null && tag.equals(current.getTag())) return;

        if (itemId == R.id.nav_insight) selectedFragment = new InsightsFragment();
        else if (itemId == R.id.nav_profile) selectedFragment = new Profile();
        else if (itemId == R.id.nav_recipe) selectedFragment = new RecipeFragment();

        if (selectedFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.FCVMain, selectedFragment, tag);
            if (current != null) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }

    private void syncBottomNav(Fragment fragment) {
        if (bottomNav == null) return;
        int itemId = -1;
        if (fragment instanceof HomeFragment) itemId = R.id.nav_home;
        else if (fragment instanceof RecipeFragment) itemId = R.id.nav_recipe;
        else if (fragment instanceof InsightsFragment) itemId = R.id.nav_insight;
        else if (fragment instanceof Profile) itemId = R.id.nav_profile;

        if (itemId != -1 && bottomNav.getSelectedItemId() != itemId) {
            bottomNav.getMenu().findItem(itemId).setChecked(true);
        }
    }
}
