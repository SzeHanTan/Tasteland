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

// Change the generic type to <RecyclerView.ViewHolder> to allow multiple types
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;

    // Ensure these names match what you use in onCreateViewHolder
    private static final int TYPE_MAIN_POST = 0;
    private static final int TYPE_REPLY = 1;

    private boolean isReplyPage;

    // Update constructor to accept this new boolean
    public ChatAdapter(List<ChatMessage> messageList, boolean isReplyPage) {
        this.messageList = messageList;
        this.isReplyPage = isReplyPage;
    }
    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        // Use the constants defined above
        return messageList.get(position).isMainPost() ? TYPE_MAIN_POST : TYPE_REPLY;
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MAIN_POST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_main_post, parent, false);
            return new MainPostViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_reply, parent, false);
            return new ReplyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof MainPostViewHolder) {
            MainPostViewHolder mainHolder = (MainPostViewHolder) holder;

            // --- 1. REPLY BUTTON LOGIC ---
            if (isReplyPage) {
                // Hide the reply button in Pic 2
                mainHolder.btnReply.setVisibility(View.GONE);
            } else {
                // Show the reply button in Pic 1
                mainHolder.btnReply.setVisibility(View.VISIBLE);
                mainHolder.btnReply.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), Reply.class);
                    intent.putExtra("sender_name", msg.getSenderName());
                    intent.putExtra("message_text", msg.getMessageText());
                    intent.putExtra("time", msg.getTime());
                    v.getContext().startActivity(intent);
                });
            }

            // --- 2. LIKE BUTTON LOGIC (Always shown) ---
            mainHolder.tvLikes.setText(String.valueOf(msg.getLikeCount()));
            if (msg.isLikedByUser()) {
                mainHolder.layoutLikeActive.setVisibility(View.VISIBLE);
                mainHolder.layoutLikeInactive.setVisibility(View.GONE);
            } else {
                mainHolder.layoutLikeActive.setVisibility(View.GONE);
                mainHolder.layoutLikeInactive.setVisibility(View.VISIBLE);
            }

            // 1. Set text content
            mainHolder.tvSender.setText(msg.getSenderName());
            mainHolder.tvText.setText(msg.getMessageText());
            mainHolder.tvTime.setText(msg.getTime());
            mainHolder.tvLikes.setText(String.valueOf(msg.getLikeCount()));

            // 2. Handle Like Toggle UI
            if (msg.isLikedByUser()) {
                mainHolder.layoutLikeActive.setVisibility(View.VISIBLE);
                mainHolder.layoutLikeInactive.setVisibility(View.GONE);
            } else {
                mainHolder.layoutLikeActive.setVisibility(View.GONE);
                mainHolder.layoutLikeInactive.setVisibility(View.VISIBLE);
            }

            // 3. Like Click Listeners
            View.OnClickListener likeListener = v -> {
                boolean liked = msg.isLikedByUser();
                msg.setLikedByUser(!liked);
                msg.setLikeCount(liked ? msg.getLikeCount() - 1 : msg.getLikeCount() + 1);
                notifyItemChanged(position);
            };
            mainHolder.layoutLikeActive.setOnClickListener(likeListener);
            mainHolder.layoutLikeInactive.setOnClickListener(likeListener);

            // 4. Reply Click Listener
            mainHolder.btnReply.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Reply.class);
                intent.putExtra("sender_name", msg.getSenderName());
                intent.putExtra("message_text", msg.getMessageText());
                intent.putExtra("time", msg.getTime());

                intent.putExtra("hide_like", true);
                v.getContext().startActivity(intent);
            });

        } else if (holder instanceof ReplyViewHolder) {
            ReplyViewHolder replyHolder = (ReplyViewHolder) holder;
            replyHolder.tvSender.setText(msg.getSenderName());
            replyHolder.tvText.setText(msg.getMessageText());
            replyHolder.tvTime.setText(msg.getTime());
        }
    }

    // --- VIEW HOLDERS ---

    // ViewHolder for the Green Bubble (Main Post)
    public static class MainPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvText, tvTime, tvLikes;
        Button btnReply;
        View layoutLikeActive;
        View layoutLikeInactive;

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

    // ViewHolder for the Grey Bubble (Replies)
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