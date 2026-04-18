package com.example.communitysharing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.models.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends
        RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<HistoryItem> list;

    public interface OnHistoryClickListener {
        void onClick(HistoryItem item);
    }

    private OnHistoryClickListener listener;

    public HistoryAdapter(Context context, List<HistoryItem> list,
                          OnHistoryClickListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder,
                                 int position) {
        HistoryItem item = list.get(position);

        // Title
        String title = item.getItemTitle() != null
                ? item.getItemTitle() : "";
        holder.tvItemTitle.setText(title);

        // Date
        holder.tvDate.setText(formatDate(item.getTimestamp()));

        // Shared with
        String otherName = item.getOtherUserName() != null
                ? item.getOtherUserName() : "Unknown";
        String typeLabel = "shared".equals(item.getType())
                ? "Shared with " : "Received from ";
        holder.tvSharedWith.setText(typeLabel + otherName);

        // Status
        String status = item.getStatus() != null
                ? item.getStatus() : "completed";
        setStatus(holder, status);

        String imageUrl = item.getItemImageUrl();
        holder.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            final String imageToLoad = imageUrl;
            new Thread(() -> {
                try {
                    byte[] bytes = Base64.decode(imageToLoad.trim(), Base64.NO_WRAP);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    if (bitmap == null) {
                        bytes = Base64.decode(imageToLoad.trim(), Base64.DEFAULT);
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        android.util.Log.d("BitmapTest",
                                "retry bitmap=" + (bitmap == null ? "NULL" : "OK"));
                    }

                    final Bitmap finalBitmap = bitmap;
                    ((android.app.Activity) context).runOnUiThread(() -> {
                        android.util.Log.d("BitmapTest",
                                "runOnUiThread finalBitmap="
                                        + (finalBitmap == null ? "NULL" : "OK"));
                        if (finalBitmap != null) {
                            holder.ivItemImage.setImageBitmap(finalBitmap);
                            holder.ivItemImage.setPadding(0, 0, 0, 0);
                            holder.ivItemImage.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP);

                            android.util.Log.d("BitmapTest2", "setImageBitmap called!");
                        }
                    });
                } catch (Exception e) {
                    android.util.Log.e("BitmapTest", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });


    }

    private void setStatus(HistoryViewHolder holder, String status) {
        switch (status) {
            case "completed":
                holder.tvStatus.setText("Completed");
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorPrimary));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorPrimary));
                break;

            case "in_progress":
                holder.tvStatus.setText("In Progress");
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorOrange));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorOrange));
                break;

            case "cancelled":
                holder.tvStatus.setText("Cancelled");
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorRed));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorRed));
                break;

            default:
                holder.tvStatus.setText("Completed");
                break;
        }
    }

    // Format timestamp → "Oct 12, 2023"
    private String formatDate(long timestamp) {
        if (timestamp == 0) return "";
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(new Date(timestamp));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void updateList(List<HistoryItem> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage, ivTypeIcon, ivOtherAvatar;
        TextView tvDate, tvItemTitle, tvStatus, tvSharedWith;
        View viewStatusDot;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage   = itemView.findViewById(R.id.ivItemImage);
            ivTypeIcon    = itemView.findViewById(R.id.ivTypeIcon);
            ivOtherAvatar = itemView.findViewById(R.id.ivOtherAvatar);
            tvDate        = itemView.findViewById(R.id.tvDate);
            tvItemTitle   = itemView.findViewById(R.id.tvItemTitle);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            tvSharedWith  = itemView.findViewById(R.id.tvSharedWith);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
        }
    }
}