package com.example.communitysharing.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.communitysharing.R;
import com.example.communitysharing.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.bumptech.glide.Glide;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messageList.get(position);

        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(msg.getTimestamp()));


        if (msg.getSenderId().equals(currentUserId)) {
            holder.layoutMessage.setGravity(Gravity.END);
        } else {
            holder.layoutMessage.setGravity(Gravity.START);
        }

        //  Hiển thị text / image
        if (msg.isImage()) {
            holder.imgMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setVisibility(View.GONE);

            Glide.with(context)
                    .load(msg.getImageUrl())
                    .into(holder.imgMessage);
        } else {
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.imgMessage.setVisibility(View.GONE);
            holder.tvMessage.setText(msg.getContent());
        }

        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        ImageView imgMessage;
        LinearLayout layoutMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgMessage = itemView.findViewById(R.id.imgMessage);
            layoutMessage = itemView.findViewById(R.id.layoutMessage);
        }
    }
}