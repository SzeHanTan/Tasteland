package com.example.tastelandv1.Community;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastelandv1.R;
import com.example.tastelandv1.Backend.RetrofitClient;
import com.example.tastelandv1.Backend.SessionManager;
import com.example.tastelandv1.Backend.SupabaseAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;
    private boolean isReplyPage;
    private String communityName;

    private static final int TYPE_MAIN_POST = 0;
    private static final int TYPE_REPLY = 1;
    private static final int TYPE_RECIPE = 2;
    private static final int TYPE_LEFTOVER = 3;
    private static final int TYPE_DATE_HEADER = 4;
    private static final int TYPE_SYSTEM = 5;

    public ChatAdapter(List<ChatMessage> messageList, boolean isReplyPage, String communityName) {
        this.messageList = messageList;
        this.isReplyPage = isReplyPage;
        this.communityName = communityName;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messageList.get(position);
        if ("date_header".equals(msg.getMessageType())) return TYPE_DATE_HEADER;
        if ("system".equals(msg.getMessageType())) return TYPE_SYSTEM;
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
        if (viewType == TYPE_DATE_HEADER) {
            return new DateHeaderViewHolder(inflater.inflate(R.layout.activity_notification_header_item, parent, false));
        }
        if (viewType == TYPE_SYSTEM) {
            return new SystemViewHolder(inflater.inflate(R.layout.item_chat_system, parent, false));
        }
        if (viewType == TYPE_REPLY) {
            return new ReplyViewHolder(inflater.inflate(R.layout.item_chat_reply, parent, false));
        }
        return new MainPostViewHolder(inflater.inflate(R.layout.item_chat_main_post, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);

        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).tvDate.setText(msg.getMessageText());
        } else if (holder instanceof SystemViewHolder) {
            ((SystemViewHolder) holder).tvSystem.setText(msg.getMessageText());
        } else if (holder instanceof MainPostViewHolder) {
            MainPostViewHolder mainHolder = (MainPostViewHolder) holder;

            mainHolder.tvSender.setText(msg.getSenderName());
            mainHolder.tvText.setText(msg.getMessageText());
            mainHolder.tvTime.setText(msg.getTime());
            mainHolder.tvLikes.setText(String.valueOf(msg.getLikeCount()));

            if ("recipe".equals(msg.getMessageType())) {
                mainHolder.tvText.setText("📖 Recipe: " + msg.getMessageText());
            } else if ("leftover".equals(msg.getMessageType())) {
                mainHolder.tvText.setText("🍎 Leftover: " + msg.getMessageText());
            }

            mainHolder.layoutLikeActive.setVisibility(msg.isLikedByUser() ? View.VISIBLE : View.GONE);
            mainHolder.layoutLikeInactive.setVisibility(msg.isLikedByUser() ? View.GONE : View.VISIBLE);

            View.OnClickListener likeListener = v -> toggleLike(msg, position, holder.itemView);
            mainHolder.layoutLikeActive.setOnClickListener(likeListener);
            mainHolder.layoutLikeInactive.setOnClickListener(likeListener);

            mainHolder.btnReply.setVisibility(isReplyPage ? View.GONE : View.VISIBLE);
            mainHolder.btnReply.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Reply.class);
                intent.putExtra("message_id", msg.getId());
                intent.putExtra("group_id", msg.getGroupId()); 
                intent.putExtra("sender_name", msg.getSenderName());
                intent.putExtra("message_text", msg.getMessageText());
                intent.putExtra("time", msg.getTimeFull());
                intent.putExtra("like_count", msg.getLikeCount());
                intent.putExtra("community_name", communityName);
                v.getContext().startActivity(intent);
            });

            if (mainHolder.btnPin != null) {
                if (isReplyPage) {
                    mainHolder.btnPin.setVisibility(View.GONE);
                } else {
                    mainHolder.btnPin.setVisibility(View.VISIBLE);
                    mainHolder.btnPin.setColorFilter(msg.isPinned() ? 0xFFFFD700 : 0xFF888888); 
                    mainHolder.btnPin.setOnClickListener(v -> togglePin(msg, position, holder.itemView));
                }
            }

        } else if (holder instanceof ReplyViewHolder) {
            ReplyViewHolder replyHolder = (ReplyViewHolder) holder;
            replyHolder.tvSender.setText(msg.getSenderName());
            replyHolder.tvText.setText(msg.getMessageText());
            replyHolder.tvTime.setText(msg.getTime());
            replyHolder.tvLikes.setText(String.valueOf(msg.getLikeCount()));

            replyHolder.layoutLikeActive.setVisibility(msg.isLikedByUser() ? View.VISIBLE : View.GONE);
            replyHolder.layoutLikeInactive.setVisibility(msg.isLikedByUser() ? View.GONE : View.VISIBLE);

            View.OnClickListener likeListener = v -> toggleLike(msg, position, holder.itemView);
            replyHolder.layoutLikeActive.setOnClickListener(likeListener);
            replyHolder.layoutLikeInactive.setOnClickListener(likeListener);
        }
    }

    private void togglePin(ChatMessage msg, int position, View view) {
        boolean originalStatus = msg.isPinned();
        boolean newPinStatus = !msg.isPinned();

        SessionManager session = new SessionManager(view.getContext());
        SupabaseAPI api = RetrofitClient.getInstance(view.getContext()).getApi();
        String token = "Bearer " + session.getToken();

        // 1. Optimistic Update: Update UI immediately
        if (newPinStatus) {
            // Unpin all others locally
            for (ChatMessage m : messageList) m.setPinned(false);
        }
        msg.setPinned(newPinStatus);
        notifyDataSetChanged(); // Refresh list instantly

        // 2. Background Network Operations
        if (newPinStatus) {
            // Logic: Unpin all in DB -> Then Pin this one
            Map<String, Object> unpinAll = new HashMap<>();
            unpinAll.put("is_pinned", false);

            api.updatePinStatus(RetrofitClient.SUPABASE_KEY, token, null, "eq." + msg.getGroupId(), unpinAll)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            // Now pin the specific one
                            applyPinToThisMessage(api, token, msg, position, view, true, false);
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            revertPin(msg, originalStatus); // Revert on failure
                        }
                    });
        } else {
            // Logic: Just unpin this one
            applyPinToThisMessage(api, token, msg, position, view, false, true);
        }
    }

    private void applyPinToThisMessage(SupabaseAPI api, String token, ChatMessage msg, int position, View view, boolean status, boolean allowRevert) {
        Map<String, Object> update = new HashMap<>();
        update.put("is_pinned", status);
        api.updatePinStatus(RetrofitClient.SUPABASE_KEY, token, "eq." + msg.getId(), null, update)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful() && allowRevert) {
                            revertPin(msg, !status);
                            if (view.getContext() instanceof Community) {
                                ((Community) view.getContext()).onResume(); 
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (allowRevert) revertPin(msg, !status);
                    }
                });
    }

    private void revertPin(ChatMessage msg, boolean originalStatus) {
        // Run on UI Thread if called from background
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            msg.setPinned(originalStatus);
            notifyDataSetChanged();
        });
    }
    private void toggleLike(ChatMessage msg, int position, View view) {
        SessionManager session = new SessionManager(view.getContext());
        SupabaseAPI api = RetrofitClient.getInstance(view.getContext()).getApi();
        String userId = session.getUserId();
        String token = "Bearer " + session.getToken();
        
        boolean currentlyLiked = msg.isLikedByUser();
        boolean newLikeStatus = !currentlyLiked;
        int newCount = newLikeStatus ? msg.getLikeCount() + 1 : Math.max(0, msg.getLikeCount() - 1);

        msg.setLikedByUser(newLikeStatus);
        msg.setLikeCount(newCount);
        notifyItemChanged(position);

        if (newLikeStatus) {
            Map<String, Object> likeData = new HashMap<>();
            likeData.put("user_id", userId);
            likeData.put("message_id", msg.getId());
            
            api.addLikeRecord(RetrofitClient.SUPABASE_KEY, token, likeData).enqueue(new Callback<>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) syncLikeCount(msg, newCount, token, api);
                    else rollbackLike(msg, position, currentlyLiked, newCount);
                }
                @Override public void onFailure(Call<Void> call, Throwable t) { rollbackLike(msg, position, currentlyLiked, newCount); }
            });
        } else {
            api.removeLikeRecord(RetrofitClient.SUPABASE_KEY, token, "eq." + userId, "eq." + msg.getId()).enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) syncLikeCount(msg, newCount, token, api);
                    else rollbackLike(msg, position, currentlyLiked, newCount);
                }
                @Override public void onFailure(Call<Void> call, Throwable t) { rollbackLike(msg, position, currentlyLiked, newCount); }
            });
        }
    }

    private void syncLikeCount(ChatMessage msg, int count, String token, SupabaseAPI api) {
        Map<String, Object> update = new HashMap<>();
        update.put("like_count", count);
        api.updateLikeCount(RetrofitClient.SUPABASE_KEY, token, "eq." + msg.getId(), update).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void rollbackLike(ChatMessage msg, int position, boolean oldStatus, int attemptedCount) {
        msg.setLikedByUser(oldStatus);
        msg.setLikeCount(oldStatus ? attemptedCount + 1 : attemptedCount - 1);
        notifyItemChanged(position);
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.TVNotificationHeader);
        }
    }

    public static class SystemViewHolder extends RecyclerView.ViewHolder {
        TextView tvSystem;
        public SystemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSystem = itemView.findViewById(R.id.tvSystemMessage);
        }
    }

    public static class MainPostViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvText, tvTime, tvLikes;
        Button btnReply;
        ImageButton btnPin;
        View layoutLikeActive, layoutLikeInactive;

        public MainPostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSenderName);
            tvText = itemView.findViewById(R.id.tvMessageText);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLikes = itemView.findViewById(R.id.tvLikeCount);
            btnReply = itemView.findViewById(R.id.btnReplyAction);
            btnPin = itemView.findViewById(R.id.btnPin);
            layoutLikeActive = itemView.findViewById(R.id.layoutLikeActive);
            layoutLikeInactive = itemView.findViewById(R.id.layoutLikeInactive);
        }
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvText, tvTime, tvLikes;
        View layoutLikeActive, layoutLikeInactive;
        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSenderNameReply);
            tvText = itemView.findViewById(R.id.tvMessageTextReply);
            tvTime = itemView.findViewById(R.id.tvTimeReply);
            tvLikes = itemView.findViewById(R.id.tvLikeCountReply);
            layoutLikeActive = itemView.findViewById(R.id.layoutLikeActiveReply);
            layoutLikeInactive = itemView.findViewById(R.id.layoutLikeInactiveReply);
        }
    }
}
