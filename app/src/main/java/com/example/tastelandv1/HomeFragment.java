package com.example.tastelandv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tastelandv1.Food.ui.MyFoodFragment;
import com.example.tastelandv1.Food.database.FoodRepository;
import com.example.tastelandv1.Recipe.database.RecipeRepository;
import com.example.tastelandv1.Shopping.database.ShoppingItem;
import com.example.tastelandv1.Shopping.database.ShoppingRepository;
import com.example.tastelandv1.Shopping.ui.ShoppingList;


public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- PERFORMANCE PREFETCHING ---
        // 1. Prefetch Recipes
        new RecipeRepository(getContext()).getAllRecipes(new RecipeRepository.RecipeCallback() {
            @Override public void onSuccess(java.util.List<com.example.tastelandv1.Recipe.database.Recipe> r) {}
            @Override public void onError(String e) {}
        });

        // 2. Prefetch My Food
        new FoodRepository(getContext()).getFoodItems(new FoodRepository.DataCallback() {
            @Override public void onSuccess(java.util.List<com.example.tastelandv1.Food.database.FoodItem> d) {}
            @Override public void onError(String e) {}
        });

        // 3. Prefetch Shopping List
        new ShoppingRepository(getContext()).getShoppingList(new ShoppingRepository.DataCallback() {
            @Override public void onSuccess(java.util.List<ShoppingItem> d) {}
            @Override public void onError(String e) {}
        });

        // --- Navigation Logic ---
        ImageButton addShoppingButton = view.findViewById(R.id.BtnAddShopping);
        if (addShoppingButton != null) {
            addShoppingButton.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.FCVMain, new ShoppingList())
                        .addToBackStack(null)
                        .commit();
            });
        }

        ImageView myFoodCard = view.findViewById(R.id.IVCardMyfood);
        ImageButton addFoodHomeButton = view.findViewById(R.id.BtnAddFoodHome);
        View.OnClickListener toMyFoodListener = v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.FCVMain, new MyFoodFragment())
                    .addToBackStack(null)
                    .commit();
        };

        if (myFoodCard != null) myFoodCard.setOnClickListener(toMyFoodListener);
        if (addFoodHomeButton != null) addFoodHomeButton.setOnClickListener(toMyFoodListener);

    }
}
