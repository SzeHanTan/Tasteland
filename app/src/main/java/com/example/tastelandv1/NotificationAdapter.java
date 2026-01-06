package com.example.tastelandv1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<NotificationListItem> items;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTIFICATION = 1;

    public NotificationAdapter(List<NotificationListItem> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType() == NotificationListItem.Type.HEADER ? TYPE_HEADER : TYPE_NOTIFICATION;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_notification_header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_notification_item, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationListItem item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerText.setText(item.getHeaderTitle());
        } else if (holder instanceof NotificationViewHolder) {
            NotificationItem n = item.getNotification();
            ((NotificationViewHolder) holder).title.setText(n.getTitle());
            ((NotificationViewHolder) holder).message.setText(n.getMessage());

            // Format date as "Nov 26"
            if (n.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                ((NotificationViewHolder) holder).date.setText(sdf.format(n.getCreatedAt()));
            }

            // Fade out read notifications
            holder.itemView.setAlpha(n.isRead() ? 0.6f : 1.0f);

            holder.itemView.setOnClickListener(v -> {
                if (!n.isRead()) {
                    markAsRead(n, position, holder.itemView);
                }
            });
        }
    }

    /**
     * Updates the notification status to 'is_read = true' in Supabase.
     */
    private void markAsRead(NotificationItem n, int position, View itemView) {
        SessionManager session = new SessionManager(itemView.getContext()); // cite: DB_GUIDE.md
        String token = "Bearer " + session.getToken(); // cite: DB_GUIDE.md
        String apiKey = RetrofitClient.SUPABASE_KEY;

        Map<String, Object> update = new HashMap<>();
        update.put("is_read", true); // cite: NotificationItem.java

        SupabaseAPI api = RetrofitClient.getInstance().getApi();
        api.updateNotificationStatus(apiKey, token, "eq." + n.getNotificationId(), update)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            n.setRead(true); // cite: NotificationItem.java
                            notifyItemChanged(position); // cite: NotificationAdapter.java
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(itemView.getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView headerText;
        HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.TVNotificationHeader);
        }
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, date;
        NotificationViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.TVNotificationTitle);
            message = itemView.findViewById(R.id.TVNotificationMessage);
            date = itemView.findViewById(R.id.TVNotificationDate);
        }
    }
}
