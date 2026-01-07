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
import com.example.tastelandv1.Recipe.database.Recipe;
import com.example.tastelandv1.Recipe.database.RecipeRepository;

import java.util.List;

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

        RecipeRepository repository = new RecipeRepository(requireContext());
        repository.getAllRecipes(new RecipeRepository.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
            }
            @Override
            public void onError(String error) { }
        });

        // --- 1. SETUP "ADD SHOPPING" BUTTON ---
        ImageButton addShoppingButton = view.findViewById(R.id.BtnAddShopping);
        if (addShoppingButton != null) {
            addShoppingButton.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.FCVMain, new ShoppingList())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // --- 2. SETUP "MY FOOD" NAVIGATION ---
        // Both the image card and the plus button should lead to the My Food page
        ImageView myFoodCard = view.findViewById(R.id.IVCardMyfood);
        ImageButton addFoodHomeButton = view.findViewById(R.id.BtnAddFoodHome);

        View.OnClickListener toMyFoodListener = v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.FCVMain, new MyFoodFragment())
                    .addToBackStack(null)
                    .commit();
        };

        if (myFoodCard != null) {
            myFoodCard.setOnClickListener(toMyFoodListener);
        }
        if (addFoodHomeButton != null) {
            addFoodHomeButton.setOnClickListener(toMyFoodListener);
        }
    }
}
