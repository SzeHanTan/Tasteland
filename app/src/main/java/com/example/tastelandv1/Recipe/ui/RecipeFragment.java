package com.example.tastelandv1.Recipe.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastelandv1.R;
import com.example.tastelandv1.Recipe.RecipeActivity;
import com.example.tastelandv1.Recipe.database.Recipe;
import com.example.tastelandv1.Recipe.database.RecipeRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeFragment extends Fragment {

    // --- UI Variables ---
    private LinearLayout contentContainer; // Where we stack the grids
    private LinearLayout buttonContainer;  // Where we put the buttons
    private View emptyStateView;           // The "No Recipe" view
    private NestedScrollView scrollView;   // The scrollable area (to hide when empty)

    private RecipeRepository repository;

    // --- Data Variables ---
    private List<Recipe> allRecipes = new ArrayList<>();
    private String activeTabId = "all"; // Default is Home
    private List<MaterialButton> tabButtons = new ArrayList<>();

    // --- CONFIGURATION: Define your categories here ---
    private final List<CategoryConfig> categories = new ArrayList<>();

    public RecipeFragment() {
        // 1. Favorite Category (Logic: r.isFavorite() == true)
        categories.add(new CategoryConfig("favourite", "My Favorites ❤️", Recipe::isFavorite));

        // 2. Trending Category (Logic: Check tags for 'Trending')
        categories.add(new CategoryConfig("trending", "Trending Now 🔥", r -> r.getTags() != null && r.getTags().contains("Trending")));

        // 3. Local Category (Logic: Check category string)
        categories.add(new CategoryConfig("local", "Local Food 🇲🇾", r -> "Local".equalsIgnoreCase(r.getCategory())));

        // 4. Foreign Category
        categories.add(new CategoryConfig("foreign", "Foreign Food 🌍", r -> "Foreign".equalsIgnoreCase(r.getCategory())));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Views (Matching IDs in your XML)
        contentContainer = view.findViewById(R.id.LLContentContainer);
        buttonContainer = view.findViewById(R.id.llButtonContainer);
        emptyStateView = view.findViewById(R.id.LLEmptyState);
        scrollView = view.findViewById(R.id.NSVScrollViewContent); // ID from updated XML

        // 2. Initialize Repository
        repository = new RecipeRepository(getContext());

        // 3. Setup UI
        setupTabs();
        loadRecipes();
    }

    // ==========================================
    //              TAB / BUTTON LOGIC
    // ==========================================
    private void setupTabs() {
        buttonContainer.removeAllViews();
        tabButtons.clear();

        // Add "Home" Button first
        addButton("all", "Home (All)");

        // Add Category Buttons
        for (CategoryConfig cat : categories) {
            addButton(cat.id, cat.label);
        }

        updateButtonStyles(); // Set initial colors
    }

    private void addButton(String id, String label) {
        MaterialButton btn = new MaterialButton(getContext());
        btn.setText(label);
        btn.setTag(id);
        btn.setTextSize(12f);
        btn.setAllCaps(false);

        // IMPORTANT: Use your custom background drawable
        btn.setBackgroundResource(R.drawable.recipe_btn_bg);

        // Layout Params for spacing (Margins)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 24, 0); // Spacing between buttons
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            activeTabId = id;
            updateButtonStyles();
            renderContent(); // Re-draw the screen based on selection
        });

        buttonContainer.addView(btn);
        tabButtons.add(btn);
    }

    private void updateButtonStyles() {
        for (MaterialButton btn : tabButtons) {
            String tag = (String) btn.getTag();

            // Reset tint to ensure custom drawable shows correctly
            btn.setBackgroundTintList(null);

            if (tag.equals(activeTabId)) {
                // ACTIVE: Black Tint (High contrast)
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                btn.setTextColor(Color.WHITE);
            } else {
                // INACTIVE: No Tint (Shows original drawable colors)
                btn.setTextColor(Color.BLACK);
            }
        }
    }

    // ==========================================
    //              DATA LOADING
    // ==========================================
    private void loadRecipes() {
        repository.getAllRecipes(new RecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allRecipes = recipes;
                        renderContent(); // Draw the screen now that we have data
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // ==========================================
    //              RENDER LOGIC
    // ==========================================
    private void renderContent() {
        contentContainer.removeAllViews();
        boolean anythingShown = false;

        // SCENARIO A: SPECIFIC TAB SELECTED (e.g., Favorites)
        if (!activeTabId.equals("all")) {
            CategoryConfig config = null;
            for(CategoryConfig c : categories) if(c.id.equals(activeTabId)) config = c;

            if(config != null) {
                List<Recipe> filtered = filterRecipes(config.filter);

                // Only show if data exists
                if (!filtered.isEmpty()) {
                    inflateSection(config.label, filtered);
                    anythingShown = true;
                }
            }
        }

        // SCENARIO B: HOME VIEW (Stack Categories Up-to-Down)
        else {
            for (CategoryConfig cat : categories) {
                List<Recipe> filtered = filterRecipes(cat.filter);

                // If a category has recipes, show it. If empty, SKIP IT.
                if (!filtered.isEmpty()) {
                    inflateSection(cat.label, filtered);
                    anythingShown = true;
                }
            }
        }

        // --- HANDLE EMPTY STATE VISIBILITY ---
        if (anythingShown) {
            // Show Content, Hide Empty Message
            scrollView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            // Hide Content, Show Empty Message
            scrollView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    // Helper: Filter list based on logic
    private List<Recipe> filterRecipes(FilterLogic logic) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : allRecipes) {
            if (logic.matches(r)) result.add(r);
        }
        return result;
    }

    // Helper: Add a "Strip" (Header + Grid) to the screen
    private void inflateSection(String title, List<Recipe> recipeList) {
        View sectionView = getLayoutInflater().inflate(R.layout.item_category_section, contentContainer, false);

        TextView tvTitle = sectionView.findViewById(R.id.TVSectionTitle);
        RecyclerView rv = sectionView.findViewById(R.id.RVSectionGrid);

        tvTitle.setText(title);

        RecipeSquareAdapter adapter = new RecipeSquareAdapter(getContext(), recipeList, new RecipeSquareAdapter.OnRecipeClickListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(getContext(), RecipeActivity.class);
                intent.putExtra("RECIPE_OBJ", recipe);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Recipe recipe, boolean isFavorite) {
                updateFavoriteInDatabase(recipe, isFavorite);
            }
        });

        // 2 Columns Grid
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv.setAdapter(adapter);

        contentContainer.addView(sectionView);
    }

    private void updateFavoriteInDatabase(Recipe recipe, boolean isFavorite) {
        repository.updateFavoriteStatus(recipe.getId(), isFavorite, new RecipeRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // If viewing "Favorites" tab and user un-favorites, refresh the UI immediately
                if(activeTabId.equals("favourite") && !isFavorite) {
                    if(getActivity() != null) {
                        getActivity().runOnUiThread(() -> renderContent());
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to update favorite", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ==========================================
    //              HELPER CLASSES
    // ==========================================
    private static class CategoryConfig {
        String id;
        String label;
        FilterLogic filter;

        CategoryConfig(String id, String label, FilterLogic filter) {
            this.id = id;
            this.label = label;
            this.filter = filter;
        }
    }

    interface FilterLogic {
        boolean matches(Recipe recipe);
    }
}