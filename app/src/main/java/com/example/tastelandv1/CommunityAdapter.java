package com.example.tastelandv1;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {

    private Context context;
    private List<CommunityModel> communityList;

    public CommunityAdapter(Context context, List<CommunityModel> communityList) {
        this.context = context;
        this.communityList = communityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_community, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CommunityModel model = communityList.get(position);

        holder.name.setText(model.getName());
        holder.icon.setImageResource(model.getImage());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Community.class);
            intent.putExtra("community_name", model.getName());
            intent.putExtra("community_image", model.getImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return communityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.communityIcon);
            name = itemView.findViewById(R.id.communityName);
        }
    }
}
