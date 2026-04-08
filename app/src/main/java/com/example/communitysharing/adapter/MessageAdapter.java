package com.example.communitysharing.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 2 loại view: tin nhắn gửi và nhận
    private static final int TYPE_SENT     = 1;
    private static final int TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList,
                          String currentUserId) {
        this.context       = context;
        this.messageList   = messageList;
        this.currentUserId = currentUserId;
    }

    // Xác định type dựa vào senderId
    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        Message msg = messageList.get(position);
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(msg.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            SentViewHolder h = (SentViewHolder) holder;
            h.tvMessage.setText(msg.getContent());
            h.tvTime.setText(time + " ✓✓");
        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder h = (ReceivedViewHolder) holder;
            h.tvMessage.setText(msg.getContent());
            h.tvTime.setText(time);
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    public void updateList(List<Message> newList) {
        messageList.clear();
        messageList.addAll(newList);
        notifyDataSetChanged();
    }

    // ViewHolder tin nhắn gửi
    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
        }
    }

    // ViewHolder tin nhắn nhận
    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
        }
    }
}
