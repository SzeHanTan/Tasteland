package com.example.tastelandv1;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class ArticleActivity extends AppCompatActivity {

    // Define the key for passing the article layout ID
    public static final String EXTRA_ARTICLE_LAYOUT_ID = "extra_article_layout_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Get the layout resource ID passed from the InsightsFragment
        int layoutId = getIntent().getIntExtra(EXTRA_ARTICLE_LAYOUT_ID, 0);

        if (layoutId != 0) {
            // 2. Set the content view using the specific article layout ID
            setContentView(layoutId);
        } else {
            // Handle error case if no layout ID is passed (e.g., set an error view)
            // For now, we'll just use a placeholder if no valid ID is found.
            // You may need to create a dedicated error layout for a production app.
            setContentView(R.layout.activity_article_1); // Fallback to article 1
        }

        // The layout is loaded, now find the back button inside the loaded layout
        ImageButton backButton = findViewById(R.id.BtnBack);

        // 3. Set up the back button to simply close the current activity
        // This will navigate back to the previous context (the InsightsFragment)
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }
}