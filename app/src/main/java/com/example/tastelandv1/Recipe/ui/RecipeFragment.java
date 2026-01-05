package com.example.tastelandv1.Recipe.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
    private LinearLayout contentContainer;
    private LinearLayout buttonContainer;
    private View emptyStateView;
    private NestedScrollView scrollView;
    private EditText searchBar; // For Search Feature

    private RecipeRepository repository;

    // --- Data Variables ---
    private List<Recipe> allRecipes = new ArrayList<>();
    private String activeTabId = "all"; // Default is Home
    private String currentSearchQuery = ""; // Current search text
    private List<MaterialButton> tabButtons = new ArrayList<>();

    // --- CONFIGURATION ---
    private final List<CategoryConfig> categories = new ArrayList<>();

    public RecipeFragment() {
        // Define Categories
        categories.add(new CategoryConfig("favourite", "My Favorites ❤️", Recipe::isFavorite));
        categories.add(new CategoryConfig("trending", "Trending Food 🔥", r -> "Trending Now".equalsIgnoreCase(r.getCategory())));
        categories.add(new CategoryConfig("local", "Local Food 🇲🇾", r -> "Local Food".equalsIgnoreCase(r.getCategory())));
        categories.add(new CategoryConfig("foreign", "Foreign Food 🌍", r -> "Foreign Food".equalsIgnoreCase(r.getCategory())));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        contentContainer = view.findViewById(R.id.LLContentContainer);
        buttonContainer = view.findViewById(R.id.llButtonContainer);
        emptyStateView = view.findViewById(R.id.LLEmptyState);
        scrollView = view.findViewById(R.id.NSVScrollViewContent);
        searchBar = view.findViewById(R.id.ETSearchBar);

        repository = new RecipeRepository(getContext());

        setupSearchListener();
        setupTabs();
        loadRecipes();
    }

    // ==========================================
    //              SEARCH LOGIC
    // ==========================================
    private void setupSearchListener() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                renderContent(); // Re-render list immediately
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ==========================================
    //              TAB LOGIC
    // ==========================================
    private void setupTabs() {
        buttonContainer.removeAllViews();
        tabButtons.clear();

        addButton("all", "Home (All)");

        for (CategoryConfig cat : categories) {
            addButton(cat.id, cat.label);
        }
        updateButtonStyles();
    }

    private void addButton(String id, String label) {
        MaterialButton btn = new MaterialButton(getContext());
        btn.setText(label);
        btn.setTag(id);
        btn.setTextSize(12f);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.recipe_btn_bg);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 24, 0);
        btn.setLayoutParams(params);

        btn.setOnClickListener(v -> {
            activeTabId = id;
            updateButtonStyles();
            renderContent();
        });

        buttonContainer.addView(btn);
        tabButtons.add(btn);
    }

    private void updateButtonStyles() {
        for (MaterialButton btn : tabButtons) {
            String tag = (String) btn.getTag();
            btn.setBackgroundTintList(null); // Reset

            if (tag.equals(activeTabId)) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                btn.setTextColor(Color.WHITE);
            } else {
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
                        // Log to verify data arrival
                        Log.d("RecipeFragment", "Loaded " + recipes.size() + " recipes.");
                        renderContent();
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
    //              RENDER LOGIC (THE FIX)
    // ==========================================
    private void renderContent() {
        contentContainer.removeAllViews();
        boolean anythingShown = false;

        // SCENARIO A: SPECIFIC TAB SELECTED
        if (!activeTabId.equals("all")) {
            CategoryConfig config = null;
            for(CategoryConfig c : categories) if(c.id.equals(activeTabId)) config = c;

            if(config != null) {
                List<Recipe> filtered = filterRecipes(config.filter);
                if (!filtered.isEmpty()) {
                    inflateSection(config.label, filtered);
                    anythingShown = true;
                }
            }
        }
        // SCENARIO B: HOME VIEW (The Fix is Here)
        else {
            // --- ADD THIS BLOCK ---
            // 1. Show ALL loaded recipes first to prove data is there
            List<Recipe> allData = filterRecipes(r -> true); // Match everything
            if (!allData.isEmpty()) {
                inflateSection("All Recipes", allData);
                anythingShown = true;
            }
            // ----------------------

            // 2. Then show your specific categories
            for (CategoryConfig cat : categories) {
                List<Recipe> filtered = filterRecipes(cat.filter);
                if (!filtered.isEmpty()) {
                    inflateSection(cat.label, filtered);
                    anythingShown = true;
                }
            }
        }

        // Handle Empty State
        if (anythingShown) {
            scrollView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            scrollView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        }
    }

    // Helper: Filters by Logic AND Search Query
    private List<Recipe> filterRecipes(FilterLogic categoryLogic) {
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : allRecipes) {
            // 1. Check Category
            boolean matchesCategory = categoryLogic.matches(r);

            // 2. Check Search (Title or Ingredients)
            boolean matchesSearch = false;
            if (currentSearchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                if (r.getTitle() != null && r.getTitle().toLowerCase().contains(currentSearchQuery)) {
                    matchesSearch = true;
                }
            }

            if (matchesCategory && matchesSearch) {
                result.add(r);
            }
        }
        return result;
    }

    // Helper: Add Section to Screen
    private void inflateSection(String title, List<Recipe> recipeList) {
        View sectionView = LayoutInflater.from(contentContainer.getContext())
                .inflate(R.layout.item_category_section, contentContainer, false);

        TextView TVSectionTitle = sectionView.findViewById(R.id.TVSectionTitle);
        RecyclerView RV = sectionView.findViewById(R.id.RVSectionGrid);

        TVSectionTitle.setText(title);

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
        RV.setLayoutManager(new GridLayoutManager(getContext(), 2));
        RV.setAdapter(adapter);

        RV.setNestedScrollingEnabled(false);

        contentContainer.addView(sectionView);
    }

    private void updateFavoriteInDatabase(Recipe recipe, boolean isFavorite) {
        repository.updateFavoriteStatus(recipe.getId(), isFavorite, new RecipeRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (activeTabId.equals("favourite") && !isFavorite) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> renderContent());
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed: " + error, Toast.LENGTH_SHORT).show();
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