package com.example.tastelandv1.Recipe;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
    private FloatingActionButton fabFavourite;

    private Recipe recipe;
    private RecipeRepository repository;

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
        toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Match MainActivity: check if there are fragments to pop
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });

        IVImage = findViewById(R.id.IVDetailImage);
        TVTitle = findViewById(R.id.TVDetailTitle);
        TVCategory = findViewById(R.id.TVDetailCategory);
        TVOverview = findViewById(R.id.TVDetailOverview);
        TVInstructions = findViewById(R.id.TVDetailInstructions);
        CGIngredients = findViewById(R.id.CGDetailIngredients);
        fabFavourite = findViewById(R.id.fabFavourite);

        repository = new RecipeRepository(this);

        // 2. Get Data from Intent
        if (getIntent().hasExtra("RECIPE_OBJ")) {
            // Check if Android version is Tiramisu (API 33) or newer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                recipe = getIntent().getSerializableExtra("RECIPE_OBJ", Recipe.class);
            } else {
                // For older Android versions, use the old method
                recipe = (Recipe) getIntent().getSerializableExtra("RECIPE_OBJ");
            }

            if (recipe != null) setupUI();
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

        if (recipe.getInstructions() != null) {
            // This ensures that if Supabase sends "\\n", it becomes a real line break
            TVInstructions.setText(recipe.getInstructions().replace("\\n", "\n"));
        } else {
            TVInstructions.setText("");
        }

        // Image Loading (Glide)
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache original & resized versions
                    .thumbnail(0.1f) // Display 10% quality instantly while loading full image
                    .placeholder(R.color.black)
                    .error(R.color.black)
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

        // Setup Favourite Button State
        updateFavouriteIcon(recipe.isFavourite());

        // Favourite Click Listener
        fabFavourite.setOnClickListener(v -> {
            boolean newState = !recipe.isFavourite();
            recipe.setFavourite(newState); // Update local object
            updateFavouriteIcon(newState); // Update UI immediately

            // Update Database
            repository.updateFavouriteStatus(recipe.getId(), newState, new RecipeRepository.SimpleCallback() {
                @Override
                public void onSuccess() {
                    // Success (Silent)
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RecipeActivity.this, "Failed to update favourite", Toast.LENGTH_SHORT).show();
                    // Revert UI if failed
                    recipe.setFavourite(!newState);
                    updateFavouriteIcon(!newState);
                }
            });
        });
    }

    private void updateFavouriteIcon(boolean isFavourite) {
        if (isFavourite) {
            fabFavourite.setImageResource(R.drawable.ic_favourite_filled); // Filled Heart
        } else {
            fabFavourite.setImageResource(R.drawable.ic_favourite_border); // Outline Heart
        }
    }
}