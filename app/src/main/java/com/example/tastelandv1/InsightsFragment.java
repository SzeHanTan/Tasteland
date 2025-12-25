package com.example.tastelandv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InsightsFragment extends Fragment {

    // (Existing boilerplate code for newInstance, onCreate, onCreateView remains)
    // Removed to keep the code concise, but ensure they are present in your file.

    public InsightsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find all seven interactive elements
        Button btnExplore = view.findViewById(R.id.BtnExplore);
        ImageButton btnHabits1 = view.findViewById(R.id.BtnHabits1);
        ImageButton btnHabits2 = view.findViewById(R.id.BtnHabits2);
        ImageButton btnHabits3 = view.findViewById(R.id.BtnHabits3);
        ImageButton btnTips1 = view.findViewById(R.id.BtnTips1);
        ImageButton btnTips2 = view.findViewById(R.id.BtnTips2);
        ImageButton btnTips3 = view.findViewById(R.id.BtnTips3);

        // 2. Set up click listeners for all seven buttons

        // BtnExplore -> activity_article_1.xml
        btnExplore.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_1));

        // BtnHabits1 -> activity_article_2.xml
        btnHabits1.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_2));

        // BtnHabits2 -> activity_article_3.xml
        btnHabits2.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_3));

        // BtnHabits3 -> activity_article_4.xml
        btnHabits3.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_4));

        // BtnTips1 -> activity_article_5.xml
        btnTips1.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_5));

        // BtnTips2 -> activity_article_6.xml
        btnTips2.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_6));

        // BtnTips3 -> activity_article_7.xml
        btnTips3.setOnClickListener(v -> navigateToArticle(R.layout.fragment_article_7));
    }

    /**
     * Helper method to launch the ArticleActivity with the correct layout ID.
     * @param layoutId The resource ID of the target article layout (e.g., R.layout.activity_article_1).
     */
    private void navigateToArticle(int layoutId) {
        Intent intent = new Intent(requireActivity(), ArticleActivity.class);
        // Pass the layout resource ID to the activity
        intent.putExtra(ArticleActivity.EXTRA_ARTICLE_LAYOUT_ID, layoutId);
        startActivity(intent);
    }
}