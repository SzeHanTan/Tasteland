package com.example.tastelandv1;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.ViewHolder> {

    private List<ShoppingItem> items;

    public ShoppingAdapter(List<ShoppingItem> items) {
        this.items = items;
    }

    // Helper method to add a new item
    public void addItem() {
        items.add(new ShoppingItem());
        notifyItemInserted(items.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingItem item = items.get(position);

        // 1. Clear previous listeners to avoid conflicts when scrolling
        holder.checkBox.setOnCheckedChangeListener(null);

        // 2. Set current values
        holder.checkBox.setChecked(item.isChecked());
        holder.editText.setText(item.getText());

        // 3. Re-attach CheckBox listener
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
        });

        // 4. Attach TextWatcher to save text changes
        // We use a tag or simply re-instantiate to ensure we are updating the correct item object
        holder.currentTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        holder.editText.addTextChangedListener(holder.currentTextWatcher);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;
        TextWatcher currentTextWatcher; // Keep reference to remove it later if needed

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_item);
            editText = itemView.findViewById(R.id.et_item_text);
        }
    }
}