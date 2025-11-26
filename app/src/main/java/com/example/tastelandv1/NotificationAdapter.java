package com.example.tastelandv1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
            ((NotificationViewHolder) holder).itemView.setAlpha(n.isRead() ? 0.6f : 1.0f);

            // Optional click to mark as read
            holder.itemView.setOnClickListener(v -> {
                n.setRead(true);
                notifyItemChanged(position);
            });
        }
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
