package com.example.tastelandv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Header extends Fragment {

    private TextView tvGreet, tvDate;
    private SupabaseAPI supabaseService;

    // 1. Define the Receiver to listen for updates
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // When signal received, update the name from Session immediately
            SessionManager session = new SessionManager(context);
            tvGreet.setText("Hi, " + session.getUsername());
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

        tvGreet = view.findViewById(R.id.TVGreet);
        tvDate = view.findViewById(R.id.TVDate);
        ImageButton btnNotification = view.findViewById(R.id.btnNotificationHeader);

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), Notification.class);
                startActivity(intent);
            });
        }

        setCurrentDate();

        supabaseService = RetrofitClient.getInstance().getApi();
        SessionManager session = new SessionManager(getContext());
        tvGreet.setText("Hi, " + session.getUsername());

        fetchUserName();
    }

    // 2. Start listening when the fragment is visible
    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            IntentFilter filter = new IntentFilter("com.example.tastelandv1.UPDATE_HEADER");
            getActivity().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            // Note: If 'Context.RECEIVER_NOT_EXPORTED' is red/error, remove it.
            // It is required for newer Android versions (API 34+), but for older ones just use:
            // getActivity().registerReceiver(updateReceiver, filter);
        }
    }

    // 3. Stop listening when fragment is hidden/destroyed
    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(updateReceiver);
            } catch (IllegalArgumentException e) {
                // Receiver not registered, ignore
            }
        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    private void fetchUserName() {
        // ... (Your existing code) ...
    }
}