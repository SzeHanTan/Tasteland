package com.example.tastelandv1.Notification;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastelandv1.R;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Notification extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private SupabaseAPI api;
    private SessionManager session;
    private Handler handler = new Handler();
    private Runnable refreshRunnable;
    private String lastSeenNotificationId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        session = new SessionManager(this);
        // --- Toolbar setup ---
        Toolbar toolbar = findViewById(R.id.TBNotification);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // --- Edge to Edge padding ---
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.notification), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- RecyclerView setup ---
        recyclerView = findViewById(R.id.RCNotification);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- Initialize API ---
        api = RetrofitClient.getInstance(this).getApi();

        // --- Start Fetching Data ---
        fetchNotificationsFromDatabase();
        startNotificationListener();
    }

    private void startNotificationListener() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchNotificationsFromDatabase();
                // Change from 30000 to 3000 (3 seconds) for "instant" updates
                handler.postDelayed(this, 3000);
            }
        };
        // Initial execution after 3 seconds
        handler.postDelayed(refreshRunnable, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start polling when user enters the notification screen
        startNotificationListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // STOP the listener when user leaves to save battery and data
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    // CRITICAL: Stop the listener when the activity is closed to save battery/data
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }



    /**
     * Fetches combined notifications (Community, Likes, Expiry) from the
     * unified notification table in Supabase.
     */
    private void fetchNotificationsFromDatabase() {
        String token = "Bearer " + session.getToken();
        String userIdFilter = "eq." + session.getUserId();
        String apiKey = RetrofitClient.SUPABASE_KEY;

        api.getNotifications(apiKey, token, "*", userIdFilter, "created_at.desc")
                .enqueue(new Callback<List<NotificationItem>>() {
                    @Override
                    public void onResponse(Call<List<NotificationItem>> call, Response<List<NotificationItem>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<NotificationItem> list = response.body();
                            NotificationItem newest = list.get(0);

                            String newestId = newest.getNotificationId();
                            String title = newest.getTitle();
                            String message = newest.getMessage();
                            long relatedId = newest.getRelatedId();

                            // 1. Initial run: save the ID
                            if (lastSeenNotificationId.isEmpty()) {
                                lastSeenNotificationId = newestId;

                            }
                            // 2. New message detection: compare and trigger pop-up
                            else if (!newestId.equals(lastSeenNotificationId)) {
                                lastSeenNotificationId = newestId;
                                showStatusBarNotification(title, message, relatedId);
                            }

                            updateUI(list);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NotificationItem>> call, Throwable t) {
                        Log.e("NOTIF_FETCH", "Network error", t);
                    }
                });
    }

    private void showStatusBarNotification(String title, String message, long relatedId) {
        String channelId = "community_alerts";
        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);

        // 1. Create the Notification Channel (Required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    channelId,
                    "Community Messages",
                    android.app.NotificationManager.IMPORTANCE_HIGH); // High importance enables the "pop-up"
            notificationManager.createNotificationChannel(channel);
        }

        // 2. Build the alert
        androidx.core.app.NotificationCompat.Builder builder =
                new androidx.core.app.NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_bell) // Ensure this icon exists in res/drawable!
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH) // For Android 7.1 and below
                        .setAutoCancel(true);

        // 3. Display the notification using a unique ID (timestamp)
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

//    private NotificationItem checkFoodExpiry(FoodItem food) {
//        if (food.dueDate == null) return null; // cite: FoodItem.java
//
//        Calendar now = Calendar.getInstance();
//        Calendar due = Calendar.getInstance();
//        due.setTime(food.dueDate); // cite: FoodItem.java
//
//        // Logic: If reminderDate is set, check if today is the reminder day
//        if (food.reminderDate != null) { // cite: FoodItem.java
//            Calendar reminder = Calendar.getInstance();
//            reminder.setTime(food.reminderDate); // cite: FoodItem.java
//
//            if (isSameDay(now, reminder)) {
//                return new NotificationItem(null, session.getUserId(), "EXPIRY", "Food Reminder",
//                        food.name + " is due soon!", false, null, new Date(), new Date());
//            }
//        } else {
//            // Fallback: Check for 3 days before expiry
//            long diff = food.dueDate.getTime() - System.currentTimeMillis();
//            long days = diff / (24 * 60 * 60 * 1000);
//
//            if (days == 3) {
//                return new NotificationItem(null, session.getUserId(), "EXPIRY", "Expiring Soon",
//                        food.name + " will expire in 3 days.", false, null, new Date(), new Date());
//            }
//        }
//
//        // Always check for the actual expiry day
//        if (isSameDay(now, due)) {
//            return new NotificationItem(null, session.getUserId(), "EXPIRY", "Expired Today!",
//                    food.name + " expires today. Use it now!", false, null, new Date(), new Date());
//        }
//
//        return null;
//    }

    // Helper method to check if two dates are the same day
//    private boolean isSameDay(Calendar cal1, Calendar cal2) {
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
//    }

    private void updateUI(List<NotificationItem> notifications) {
        // Grouping logic remains in your static utility class
        List<NotificationListItem> displayList = NotificationUtils.groupNotificationsByDate(notifications);

        runOnUiThread(() -> {
            adapter = new NotificationAdapter(displayList);
            recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}