package com.example.tastelandv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.SearchView;

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
}
