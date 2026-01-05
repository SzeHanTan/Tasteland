package com.example.tastelandv1.Recipe.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // Added ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import com.example.tastelandv1.R;
import com.example.tastelandv1.Recipe.database.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class RecipeSquareAdapter extends RecyclerView.Adapter<RecipeSquareAdapter.ViewHolder> {
    private Context context;
    private List<Recipe> recipeList;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
        void onFavoriteClick(Recipe recipe, boolean isFavorite);
    }

    public RecipeSquareAdapter(Context context, List<Recipe> recipeList, OnRecipeClickListener listener) {
        this.context = context;
        this.recipeList = recipeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // 1. Load Background Image using Glide
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Show this while loading
                    .error(R.drawable.ic_launcher_background)       // Show this if URL fails
                    .into(holder.IVBackground);
        }

        // 2. Set Text
        holder.TVFoodName.setText(recipe.getTitle());

        // 3. Favorite Button Logic
        holder.BtnFavorite.setImageResource(
                recipe.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );
        holder.BtnFavorite.setOnClickListener(v -> {
            boolean newState = !recipe.isFavorite();
            recipe.setFavorite(newState);
            notifyItemChanged(position);
            listener.onFavoriteClick(recipe, newState);
        });

        // 4. Ingredients Chips
        holder.chipGroupIngredients.removeAllViews();
        List<String> ingredients = recipe.getIngredients();
        int maxChips = 2; // Reduced to 2 because chips take space on image

        if(ingredients != null) {
            for (int i = 0; i < Math.min(ingredients.size(), maxChips); i++) {
                Chip chip = new Chip(context);
                chip.setText(ingredients.get(i));

                // Styling the chip for the image background
                chip.setChipBackgroundColorResource(android.R.color.transparent); // or use R.color.white with alpha
                chip.setChipStrokeColorResource(android.R.color.transparent);
                chip.setEnsureMinTouchTargetSize(false);
                chip.setTextSize(10f);

                // IMPORTANT: Apply the chip background drawable you requested
                chip.setBackgroundResource(R.drawable.chip_background);

                holder.chipGroupIngredients.addView(chip);
            }

            if (ingredients.size() > maxChips) {
                holder.TVMoreIngredients.setText("+" + (ingredients.size() - maxChips));
                holder.TVMoreIngredients.setVisibility(View.VISIBLE);
            } else {
                holder.TVMoreIngredients.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() { return recipeList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView IVBackground; // New Image View
        TextView TVFoodName, TVMoreIngredients;
        ImageButton BtnFavorite;
        ChipGroup chipGroupIngredients;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Bind the new ImageView ID
            IVBackground = itemView.findViewById(R.id.IVRecipeBackground);
            TVFoodName = itemView.findViewById(R.id.TVFoodName);
            BtnFavorite = itemView.findViewById(R.id.BtnFavorite);
            chipGroupIngredients = itemView.findViewById(R.id.chipGroupIngredients);
            TVMoreIngredients = itemView.findViewById(R.id.TVMoreIngredients);
        }
    }
}