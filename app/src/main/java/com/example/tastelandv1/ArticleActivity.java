package com.example.tastelandv1;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ArticleActivity extends AppCompatActivity {

    public static final String EXTRA_ARTICLE_ID = "extra_article_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        // Get the article ID passed from InsightsFragment
        int articleId = getIntent().getIntExtra(EXTRA_ARTICLE_ID, 1);

        // Load the appropriate article fragment
        Fragment articleFragment = getArticleFragment(articleId);

        if (articleFragment != null && savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.articleFragmentContainer, articleFragment);
            transaction.commit();
        }

        // Set up the back button
        ImageButton backButton = findViewById(R.id.BtnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    /**
     * Returns the appropriate article fragment based on the article ID
     */
    private Fragment getArticleFragment(int articleId) {
        switch (articleId) {
            case 1:
                return new Article1Fragment();
            case 2:
                return new Article2Fragment();
            case 3:
                return new Article3Fragment();
            case 4:
                return new Article4Fragment();
            case 5:
                return new Article5Fragment();
            case 6:
                return new Article6Fragment();
            case 7:
                return new Article7Fragment();
            default:
                return new Article1Fragment(); // Fallback
        }
    }
}