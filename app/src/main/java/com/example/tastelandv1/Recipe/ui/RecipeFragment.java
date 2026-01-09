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
import android.widget.ProgressBar;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecipeFragment extends Fragment {

    // --- UI Variables ---
    private LinearLayout contentContainer;
    private LinearLayout buttonContainer;
    private View emptyStateView;
    private NestedScrollView scrollView;
    private EditText searchBar;
    private RecipeRepository repository;
    private ProgressBar progressBar;

    // --- Data Variables ---
    private List<Recipe> allRecipes = new ArrayList<>();
    private String activeTabId = "all"; // Default is Home
    private String currentSearchQuery = ""; // Current search text
    private List<MaterialButton> tabButtons = new ArrayList<>();

    // --- CONFIGURATION ---
    private final List<CategoryConfig> categories = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RecipeFragment() {
        // Define Categories
        categories.add(new CategoryConfig("favourite", "My Favorites ❤️", Recipe::isFavorite));
        categories.add(new CategoryConfig("trending", "Trending Food 🔥", r -> "Trending Now".equalsIgnoreCase(r.getCategory())));
        categories.add(new CategoryConfig("local", "Local Food 🇲🇾", r -> "Local Food".equalsIgnoreCase(r.getCategory())));
        categories.add(new CategoryConfig("foreign", "Foreign Food 🌍", r -> "Foreign Food".equalsIgnoreCase(r.getCategory())));
    }

    // Static Factory Method for Navigation
    public static RecipeFragment newInstance(String activeTabId) {
        RecipeFragment fragment = new RecipeFragment();
        Bundle args = new Bundle();
        args.putString("ACTIVE_TAB", activeTabId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check for navigation arguments
        if (getArguments() != null) {
            activeTabId = getArguments().getString("ACTIVE_TAB", "all");
        }

        // Initialize Views
        contentContainer = view.findViewById(R.id.LLContentContainer);
        buttonContainer = view.findViewById(R.id.llButtonContainer);
        emptyStateView = view.findViewById(R.id.LLEmptyState);
        scrollView = view.findViewById(R.id.NSVScrollViewContent);
        searchBar = view.findViewById(R.id.ETSearchBar);
        progressBar = view.findViewById(R.id.PGRecipe);

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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repository.getAllRecipes(new RecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        allRecipes = recipes;
                        Log.d("RecipeFragment", "Loaded " + recipes.size() + " recipes.");
                        renderContent();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                    );
                }
            }
        });
    }

    // ==========================================
    //              RENDER LOGIC (THE FIX)
    // ==========================================
    private void renderContent() {
        if (getActivity() == null) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // 1. Run Filtering Calculation in Background Thread
        executor.execute(() -> {

            // List of views to be added (calculated in bg)
            final List<SectionData> sectionsToRender = new ArrayList<>();
            boolean anythingFound = false;

            // SCENARIO A: SPECIFIC TAB
            if (!activeTabId.equals("all")) {
                CategoryConfig config = null;
                for(CategoryConfig c : categories) if(c.id.equals(activeTabId)) config = c;

                if(config != null) {
                    List<Recipe> filtered = filterRecipes(config.filter);
                    if (!filtered.isEmpty()) {
                        sectionsToRender.add(new SectionData(config.label, filtered));
                        anythingFound = true;
                    }
                }
            }
            // SCENARIO B: HOME VIEW
            else {
                List<Recipe> allData = filterRecipes(r -> true);
                if (!allData.isEmpty()) {
                    sectionsToRender.add(new SectionData("All Recipes", allData));
                    anythingFound = true;
                }

                for (CategoryConfig cat : categories) {
                    List<Recipe> filtered = filterRecipes(cat.filter);
                    if (!filtered.isEmpty()) {
                        sectionsToRender.add(new SectionData(cat.label, filtered));
                        anythingFound = true;
                    }
                }
            }

            final boolean showEmpty = !anythingFound;

            // 2. Update UI on Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);

                    // Clear only when ready to replace
                    contentContainer.removeAllViews();

                    if (showEmpty) {
                        scrollView.setVisibility(View.GONE);
                        emptyStateView.setVisibility(View.VISIBLE);
                    } else {
                        scrollView.setVisibility(View.VISIBLE);
                        emptyStateView.setVisibility(View.GONE);

                        // Inflate views
                        for (SectionData section : sectionsToRender) {
                            inflateSection(section.title, section.recipes);
                        }
                    }
                });
            }
        });
    }

    private static class SectionData {
        String title;
        List<Recipe> recipes;
        SectionData(String t, List<Recipe> r) { title = t; recipes = r; }
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

    @Override
    public void onResume() {
        super.onResume();
        if (repository != null) {
            loadRecipes();
        }
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
