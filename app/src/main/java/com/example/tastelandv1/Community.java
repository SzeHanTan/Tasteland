package com.example.tastelandv1.;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class Community extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Get passed data from intent
        String communityName = getIntent().getStringExtra("community_name");
        int communityImage = getIntent().getIntExtra("community_image", 0);

        // Connect UI
        TextView nameView = findViewById(R.id.communityName);
        ImageView imageView = findViewById(R.id.communityImage);

        nameView.setText(communityName);
        imageView.setImageResource(communityImage);
    }
}
