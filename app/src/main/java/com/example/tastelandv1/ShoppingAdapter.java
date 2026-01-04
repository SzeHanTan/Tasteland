package com.example.tastelandv1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView; // Changed from EditText to TextView
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> items;
    // We keep the listener only for the Checkbox (if you want to implement check/delete later)
    private OnItemChangeListener listener;

    public interface OnItemChangeListener {
        void onItemChanged(ShoppingItem item);
    }

    public ShoppingAdapter(List<ShoppingItem> items, OnItemChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We can reuse your existing layout, but we will treat the EditText as a TextView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        // 1. Set Text (We disable editing here to avoid confusion)
        holder.tvItemText.setText(item.getText());
        holder.tvItemText.setFocusable(false); // Make it read-only
        holder.tvItemText.setClickable(false);

        holder.cbItem.setOnCheckedChangeListener(null);
        holder.cbItem.setChecked(item.isChecked());

        // 2. Handle Checkbox clicks (Optional: for crossing off items)
        holder.cbItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            if (listener != null) {
                listener.onItemChanged(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbItem;
        TextView tvItemText; // Changed to TextView to show it's read-only

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItem = itemView.findViewById(R.id.cb_item);
            // We cast it to TextView so we can set text easily
            tvItemText = itemView.findViewById(R.id.et_item_text);
        }
    }
}