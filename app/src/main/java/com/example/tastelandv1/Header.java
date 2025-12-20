package com.example.tastelandv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    public Header() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_header, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreet = view.findViewById(R.id.TVGreet);
        tvDate = view.findViewById(R.id.TVDate);

        setCurrentDate();

        supabaseService = RetrofitClient.getInstance().getApi();

        fetchUserName();
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        tvDate.setText(currentDate);
    }

    private void fetchUserName() {
        SessionManager session = new SessionManager(getContext());
        String token = session.getToken();

        // If not logged in, just show default
        if (token == null) {
            tvGreet.setText("Hi, Guest");
            return;
        }

        supabaseService.getMyProfile(RetrofitClient.SUPABASE_KEY, "Bearer " + token)
                .enqueue(new Callback<List<UserProfile>>() {
                    @Override
                    public void onResponse(Call<List<UserProfile>> call, Response<List<UserProfile>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            UserProfile user = response.body().get(0);

                            String name = user.getFullName();
                            if (name == null || name.isEmpty()) {
                                name = "User";
                            }

                            tvGreet.setText("Hi, " + name);
                        } else {
                            tvGreet.setText("Hi, User");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                    }
                });
    }
}