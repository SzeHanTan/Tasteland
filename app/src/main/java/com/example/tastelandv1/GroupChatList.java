package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class GroupChatList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommunityAdapter adapter;
    private List<CommunityModel> communityList;
    private List<CommunityModel> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_list);

        recyclerView = findViewById(R.id.recyclerViewCommunity);
        SearchView searchView = findViewById(R.id.SVCommunity);

        communityList = new ArrayList<>();
        communityList.add(new CommunityModel("Taste & Togetherness", R.drawable.ic_groups));
        communityList.add(new CommunityModel("PJ area best food!!", R.drawable.ic_groups));
        communityList.add(new CommunityModel("Life Hacks", R.drawable.ic_groups));
        communityList.add(new CommunityModel("Recipe for Students", R.drawable.ic_groups));
        communityList.add(new CommunityModel("Healthy Recipe", R.drawable.ic_groups));
        communityList.add(new CommunityModel("XXX Community 1", R.drawable.ic_groups));
        communityList.add(new CommunityModel("XXX Community 2", R.drawable.ic_groups));
        communityList.add(new CommunityModel("XXX Community 3", R.drawable.ic_groups));



        filteredList = new ArrayList<>(communityList);

        adapter = new CommunityAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupSearch(searchView);


        ImageButton btnNotification = findViewById(R.id.btnNotification);

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent(current context, the page you want to go to)
                Intent intent = new Intent(GroupChatList.this, Notification.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Set the home item as selected by default
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_new_team) {
                showCommunityDialog(true); // Show Create Pop-up
                return false; // Return false so the "Home" icon stays highlighted
            } else if (id == R.id.nav_join_team) {
                showCommunityDialog(false);// Show Join Pop-up
                return false;
            } else if (id == R.id.nav_home) {
                return true;
            }
            return false;
        });
    }

    private void setupSearch(SearchView searchView) {

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filteredList.clear();

                for (CommunityModel item : communityList) {
                    if (item.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(item);
                    }
                }

                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    private void showCommunityDialog(boolean isNewTeam) {
        // Create the dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_team_action, null);

        TextView title = view.findViewById(R.id.tvDialogTitle);
        EditText input = view.findViewById(R.id.etTeamInput);
        Button confirm = view.findViewById(R.id.btnConfirm);

        // Customize text based on which button was clicked
        if (isNewTeam) {
            title.setText("Create New Community");
            input.setHint("Enter community name...");
        } else {
            title.setText("Join a Community");
            input.setHint("Enter invitation code...");
        }

        builder.setView(view);
        androidx.appcompat.app.AlertDialog dialog = builder.create();

        // Make the background of the actual dialog transparent so our CardView corners show
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        confirm.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                if (isNewTeam) {
                    // Logic to add a new community to your list locally
                    CommunityModel newCommunity = new CommunityModel(value, R.drawable.ic_groups);
                    communityList.add(0, newCommunity); // Add to top of main list
                    filteredList.add(0, newCommunity);  // Add to top of visible list
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    Toast.makeText(this, "Community Created!", Toast.LENGTH_SHORT).show();
                } else {
                    // Placeholder for Join logic
                    Toast.makeText(this, "Joining: " + value, Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } else {
                input.setError("Field cannot be empty");
            }
        });

        dialog.show();
    }

}

