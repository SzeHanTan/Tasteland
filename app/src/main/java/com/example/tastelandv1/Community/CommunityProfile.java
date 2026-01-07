package com.example.tastelandv1.Community;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tastelandv1.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CommunityProfile extends Fragment {

    public CommunityProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- 1. CHAT BUTTON LOGIC ---
        ImageButton btnChat = view.findViewById(R.id.IBHomeHeaderChat2);
        if (btnChat != null) {
            btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), GroupChatList.class);
                startActivity(intent);
            });
        }

        // --- 2. PROFILE BUTTON LOGIC ---
        // This button now behaves identically to the Profile icon in the bottom menu
        ImageButton btnProfile = view.findViewById(R.id.IBHomeHeaderProfile2);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                    if (bottomNav != null) {
                        // Triggers the same navigation logic as the bottom menu
                        bottomNav.setSelectedItemId(R.id.nav_profile);
                    }
                }
            });
        }
    }
}