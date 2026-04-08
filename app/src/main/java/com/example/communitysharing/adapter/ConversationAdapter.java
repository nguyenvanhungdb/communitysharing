package com.example.communitysharing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private Context context;
    private List<Conversation> list;

    public interface OnConversationClickListener {
        void onClick(Conversation conversation);
    }

    private OnConversationClickListener listener;

    public ConversationAdapter(Context context, List<Conversation> list,
                               OnConversationClickListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder,
                                 int position) {
        Conversation conv = list.get(position);

        holder.tvName.setText(conv.getOtherUserName());
        holder.tvLastMessage.setText(conv.getLastMessage());
        holder.tvTime.setText(formatTime(conv.getLastMessageTime()));

        // Hiện badge nếu có tin chưa đọc
        if (conv.getUnreadCount() > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(String.valueOf(conv.getUnreadCount()));
            holder.tvLastMessage.setTextColor(
                    context.getResources().getColor(R.color.colorTextDark));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }

        // Click vào conversation
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(conv);
        });
    }

    // Format timestamp thành "HH:mm" hoặc "dd/MM"
    private String formatTime(long timestamp) {
        if (timestamp == 0) return "";
        Date date = new Date(timestamp);
        long now  = System.currentTimeMillis();
        long diff = now - timestamp;

        // Nếu trong ngày hôm nay → hiện giờ:phút
        if (diff < 24 * 60 * 60 * 1000) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(date);
        }
        // Nếu hôm qua hoặc lâu hơn → hiện ngày/tháng
        return new SimpleDateFormat("dd/MM", Locale.getDefault())
                .format(date);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void updateList(List<Conversation> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLastMessage, tvTime, tvUnread;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName        = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime        = itemView.findViewById(R.id.tvTime);
            tvUnread      = itemView.findViewById(R.id.tvUnread);
        }
    }
}
