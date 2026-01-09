package com.example.tastelandv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;
import com.example.tastelandv1.Notification.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Header extends Fragment {

    private TextView tvGreet, tvDate;

    // 1. Receiver to update the name immediately if you edit the profile elsewhere
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGreeting();
        }
    };

    public Header() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI
        tvGreet = view.findViewById(R.id.TVGreet);
        tvDate = view.findViewById(R.id.TVDate);
        ImageButton btnNotification = view.findViewById(R.id.btnNotificationHeader);

        // Setup Notification Button
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Notification.class);
                startActivity(intent);
            });
        }

        // Set Date and Initial Greeting
        setCurrentDate();

        // Load data immediately from local session (fast)
        updateGreeting();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            IntentFilter filter = new IntentFilter("com.example.tastelandv1.UPDATE_HEADER");
            ContextCompat.registerReceiver(
                    getActivity(),
                    updateReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(updateReceiver);
            } catch (IllegalArgumentException e) {
                // Ignore if receiver wasn't registered
            }
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    // Pulls name from SessionManager and updates TextView
    private void updateGreeting() {
        if (getContext() == null) return;

        SessionManager session = new SessionManager(getContext());
        String username = session.getUsername();

        if (username != null && !username.isEmpty()) {
            tvGreet.setText("Hi, " + username);
        } else {
            tvGreet.setText("Hi, Guest");
        }
    }
}