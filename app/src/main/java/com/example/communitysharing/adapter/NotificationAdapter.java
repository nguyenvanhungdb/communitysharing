package com.example.communitysharing.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Notification;

import java.util.List;

public class NotificationAdapter extends
        RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private Context context;
    private List<Notification> list;

    public interface OnNotifActionListener {
        void onAccept(Notification notif);
        void onDecline(Notification notif);
        void onReplyNow(Notification notif);
        void onCoordinatePickup(Notification notif);
        void onViewListing(Notification notif);
        void onClick(Notification notif);
    }

    private OnNotifActionListener listener;

    public NotificationAdapter(Context context,
                               List<Notification> list,
                               OnNotifActionListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder,
                                 int position) {
        Notification notif = list.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        holder.tvTime.setText(formatTimeAgo(notif.getTimestamp()));

        // Đổi background nếu đã đọc
        if (notif.isRead()) {
            holder.llRoot.setBackground(
                    context.getResources().getDrawable(
                            R.drawable.bg_notification_read));
        } else {
            holder.llRoot.setBackground(
                    context.getResources().getDrawable(
                            R.drawable.bg_notification_unread));
        }

        // Ẩn tất cả action trước
        holder.llActions.setVisibility(View.GONE);
        holder.llPickupActions.setVisibility(View.GONE);
        holder.tvReplyNow.setVisibility(View.GONE);

        // Hiện action theo type
        switch (notif.getType()) {
            case "borrow_request":
                // Đổi màu icon cam
                holder.ivIcon.getBackground().setTint(
                        context.getResources().getColor(R.color.colorOrange));
                holder.llActions.setVisibility(View.VISIBLE);
                holder.btnAccept.setOnClickListener(v -> {
                    if (listener != null) listener.onAccept(notif);
                });
                holder.btnDecline.setOnClickListener(v -> {
                    if (listener != null) listener.onDecline(notif);
                });
                break;

            case "pickup_approved":
                // Màu xanh lá
                holder.ivIcon.getBackground().setTint(
                        context.getResources().getColor(R.color.colorPrimary));
                holder.llPickupActions.setVisibility(View.VISIBLE);
                holder.btnCoordinatePickup.setOnClickListener(v -> {
                    if (listener != null) listener.onCoordinatePickup(notif);
                });
                holder.btnViewListing.setOnClickListener(v -> {
                    if (listener != null) listener.onViewListing(notif);
                });
                break;

            case "new_message":
                holder.ivIcon.getBackground().setTint(
                        context.getResources().getColor(R.color.colorAccent));
                holder.tvReplyNow.setVisibility(View.VISIBLE);
                holder.tvReplyNow.setOnClickListener(v -> {
                    if (listener != null) listener.onReplyNow(notif);
                });
                break;

            default:
                // community, system → không có action button
                holder.ivIcon.getBackground().setTint(
                        context.getResources().getColor(R.color.colorTextHint));
                break;
        }

        // Click vào cả item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(notif);
        });
    }

    // Format "2m ago", "1h ago", "1d ago"
    private String formatTimeAgo(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        long hours   = diff / (60 * 60 * 1000);
        long days    = diff / (24 * 60 * 60 * 1000);

        if (minutes < 60)  return minutes + "m ago";
        if (hours   < 24)  return hours   + "h ago";
        return days + "d ago";
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void updateList(List<Notification> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llRoot, llActions, llPickupActions;
        ImageView ivIcon;
        TextView tvTitle, tvMessage, tvTime, tvReplyNow;
        Button btnAccept, btnDecline;
        Button btnCoordinatePickup, btnViewListing;

        NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            llRoot              = itemView.findViewById(R.id.llNotificationItem);
            ivIcon              = itemView.findViewById(R.id.ivIcon);
            tvTitle             = itemView.findViewById(R.id.tvTitle);
            tvMessage           = itemView.findViewById(R.id.tvMessage);
            tvTime              = itemView.findViewById(R.id.tvTime);
            llActions           = itemView.findViewById(R.id.llActions);
            llPickupActions     = itemView.findViewById(R.id.llPickupActions);
            tvReplyNow          = itemView.findViewById(R.id.tvReplyNow);
            btnAccept           = itemView.findViewById(R.id.btnAccept);
            btnDecline          = itemView.findViewById(R.id.btnDecline);
            btnCoordinatePickup = itemView.findViewById(R.id.btnCoordinatePickup);
            btnViewListing      = itemView.findViewById(R.id.btnViewListing);
        }
    }
}