package com.example.tastelandv1.Insights.ui;

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

import com.example.tastelandv1.Insights.ArticleActivity;
import com.example.tastelandv1.R;

public class InsightsFragment extends Fragment {

    public InsightsFragment() {
        // Required empty public constructor
    }

    public static InsightsFragment newInstance() {
        return new InsightsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all seven interactive elements
        Button btnExplore = view.findViewById(R.id.BtnExplore);
        ImageButton btnHabits1 = view.findViewById(R.id.BtnHabits1);
        ImageButton btnHabits2 = view.findViewById(R.id.BtnHabits2);
        ImageButton btnHabits3 = view.findViewById(R.id.BtnHabits3);
        ImageButton btnTips1 = view.findViewById(R.id.BtnTips1);
        ImageButton btnTips2 = view.findViewById(R.id.BtnTips2);
        ImageButton btnTips3 = view.findViewById(R.id.BtnTips3);

        // Set up click listeners - passing article IDs (1-7)
        btnExplore.setOnClickListener(v -> navigateToArticle(1));
        btnHabits1.setOnClickListener(v -> navigateToArticle(2));
        btnHabits2.setOnClickListener(v -> navigateToArticle(3));
        btnHabits3.setOnClickListener(v -> navigateToArticle(4));
        btnTips1.setOnClickListener(v -> navigateToArticle(5));
        btnTips2.setOnClickListener(v -> navigateToArticle(6));
        btnTips3.setOnClickListener(v -> navigateToArticle(7));
    }

    /**
     * Helper method to launch the ArticleActivity with the correct article ID
     * @param articleId The ID of the article to display (1-7)
     */
    private void navigateToArticle(int articleId) {
        Intent intent = new Intent(requireActivity(), ArticleActivity.class);
        // Use EXTRA_ARTICLE_ID (not EXTRA_ARTICLE_LAYOUT_ID)
        intent.putExtra(ArticleActivity.EXTRA_ARTICLE_ID, articleId);
        startActivity(intent);
    }
}