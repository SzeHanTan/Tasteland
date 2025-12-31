package com.example.tastelandv1;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;

    private static final int TYPE_MAIN_POST = 0;
    private static final int TYPE_REPLY = 1;
    private static final int TYPE_RECIPE = 2;
    private static final int TYPE_LEFTOVER = 3;

    private boolean isReplyPage;

    public ChatAdapter(List<ChatMessage> messageList, boolean isReplyPage) {
        this.messageList = messageList;
        this.isReplyPage = isReplyPage;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);
        
        // Use messageType to decide layout
        if ("recipe".equals(msg.getMessageType())) return TYPE_RECIPE;
        if ("leftover".equals(msg.getMessageType())) return TYPE_LEFTOVER;
        
        return msg.isMainPost() ? TYPE_MAIN_POST : TYPE_REPLY;
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case TYPE_RECIPE:
                // Placeholder: replace with your actual recipe card layout
                View recipeView = inflater.inflate(R.layout.item_chat_main_post, parent, false); 
                return new MainPostViewHolder(recipeView);
            case TYPE_LEFTOVER:
                // Placeholder: replace with your actual leftover card layout
                View leftoverView = inflater.inflate(R.layout.item_chat_main_post, parent, false);
                return new MainPostViewHolder(leftoverView);
            case TYPE_MAIN_POST:
                View mainView = inflater.inflate(R.layout.item_chat_main_post, parent, false);
                return new MainPostViewHolder(mainView);
            default:
                View replyView = inflater.inflate(R.layout.item_chat_reply, parent, false);
                return new ReplyViewHolder(replyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof MainPostViewHolder) {
            MainPostViewHolder mainHolder = (MainPostViewHolder) holder;

            // Basic Text Updates
            mainHolder.tvSender.setText(msg.getSenderName());
            mainHolder.tvText.setText(msg.getMessageText());
            mainHolder.tvTime.setText(msg.getTime());
            mainHolder.tvLikes.setText(String.valueOf(msg.getLikeCount()));

            // Handle special content labels if it's a recipe or leftover
            if ("recipe".equals(msg.getMessageType())) {
                mainHolder.tvText.setText("📖 Recipe: " + msg.getMessageText());
            } else if ("leftover".equals(msg.getMessageType())) {
                mainHolder.tvText.setText("🍎 Leftover: " + msg.getMessageText());
            }

            // UI logic for Like/Reply (Keep your existing logic)
            if (isReplyPage) {
                mainHolder.btnReply.setVisibility(View.GONE);
            } else {
                mainHolder.btnReply.setVisibility(View.VISIBLE);
                mainHolder.btnReply.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), Reply.class);
                    intent.putExtra("message_id", msg.getId());
                    intent.putExtra("sender_name", msg.getSenderName());
                    intent.putExtra("message_text", msg.getMessageText());
                    intent.putExtra("time", msg.getTime());
                    v.getContext().startActivity(intent);
                });
            }

            // Like logic
            View.OnClickListener likeListener = v -> {
                boolean liked = msg.isLikedByUser();
                msg.setLikedByUser(!liked);
                msg.setLikeCount(liked ? msg.getLikeCount() - 1 : msg.getLikeCount() + 1);
                notifyItemChanged(position);
            };
            mainHolder.layoutLikeActive.setOnClickListener(likeListener);
            mainHolder.layoutLikeInactive.setOnClickListener(likeListener);

        } else if (holder instanceof ReplyViewHolder) {
            ReplyViewHolder replyHolder = (ReplyViewHolder) holder;
            replyHolder.tvSender.setText(msg.getSenderName());
            replyHolder.tvText.setText(msg.getMessageText());
            replyHolder.tvTime.setText(msg.getTime());
        }
    }

    public static class MainPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvText, tvTime, tvLikes;
        Button btnReply;
        View layoutLikeActive, layoutLikeInactive;

        public MainPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSenderName);
            tvText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikes = itemView.findViewById(R.id.tvLikeCount);
            btnReply = itemView.findViewById(R.id.btnReplyAction);
            layoutLikeActive = itemView.findViewById(R.id.layoutLikeActive);
            layoutLikeInactive = itemView.findViewById(R.id.layoutLikeInactive);
        }
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvText, tvTime;
        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSenderNameReply);
            tvText = itemView.findViewById(R.id.tvMessageTextReply);
            tvTime = itemView.findViewById(R.id.tvTimeReply);
        }
    }
}
