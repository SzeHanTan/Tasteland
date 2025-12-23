package com.example.tastelandv1.Recipe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.tastelandv1.R;
import com.example.tastelandv1.Recipe.database.Recipe;
import com.example.tastelandv1.Recipe.database.RecipeRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RecipeActivity extends AppCompatActivity {

    private ImageView IVImage;
    private TextView TVTitle, TVCategory, TVOverview, TVInstructions;
    private ChipGroup CGIngredients;
    private FloatingActionButton fabFavorite;

    private Recipe recipe; // The data object
    private RecipeRepository repository;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // 1. Initialize Views
        Toolbar toolbar = findViewById(R.id.TBToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        // Handle Back Arrow Click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        IVImage = findViewById(R.id.IVDetailImage);
        TVTitle = findViewById(R.id.TVDetailTitle);
        TVCategory = findViewById(R.id.TVDetailCategory);
        TVOverview = findViewById(R.id.TVDetailOverview);
        TVInstructions = findViewById(R.id.TVDetailInstructions);
        CGIngredients = findViewById(R.id.CGDetailIngredients);
        fabFavorite = findViewById(R.id.fabFavorite);

        repository = new RecipeRepository(this);

        // 2. Get Data from Intent
        if (getIntent().hasExtra("RECIPE_OBJ")) {
            recipe = (Recipe) getIntent().getSerializableExtra("RECIPE_OBJ");
            setupUI();
        } else {
            Toast.makeText(this, "Error: No recipe data found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUI() {
        // Text Fields
        TVTitle.setText(recipe.getTitle());
        TVCategory.setText(recipe.getCategory());
        TVOverview.setText(recipe.getOverview());
        TVInstructions.setText(recipe.getInstructions());

        // Image Loading (Glide)
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.color.black)
                    .into(IVImage);
        }

        // Ingredients (Dynamic Chips)
        CGIngredients.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                Chip chip = new Chip(this);
                chip.setText(ingredient);
                chip.setCheckable(false);
                CGIngredients.addView(chip);
            }
        }

        // Setup Favorite Button State
        updateFavoriteIcon(recipe.isFavorite());

        // Favorite Click Listener
        fabFavorite.setOnClickListener(v -> {
            boolean newState = !recipe.isFavorite();
            recipe.setFavorite(newState); // Update local object
            updateFavoriteIcon(newState); // Update UI immediately

            // Update Database
            repository.updateFavoriteStatus(recipe.getId(), newState, new RecipeRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    // Success (Silent)
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RecipeActivity.this, "Failed to update favorite", Toast.LENGTH_SHORT).show();
                    // Revert UI if failed
                    recipe.setFavorite(!newState);
                    updateFavoriteIcon(!newState);
                }
            });
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled); // Filled Heart
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border); // Outline Heart
        }
    }
}