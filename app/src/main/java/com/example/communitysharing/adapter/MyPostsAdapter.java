package com.example.communitysharing.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Item;

import java.util.List;

public class MyPostsAdapter extends
        RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder> {

    private Context context;
    private List<Item> list;

    public interface OnMyPostActionListener {
        void onEdit(Item item);
        void onComplete(Item item);
        void onDelete(Item item);
        void onClick(Item item);
    }

    private OnMyPostActionListener listener;

    public MyPostsAdapter(Context context, List<Item> list,
                          OnMyPostActionListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyPostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_my_post, parent, false);
        return new MyPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostViewHolder holder,
                                 int position) {
        Item item = list.get(position);

        // Title
        String title = item.getTitle() != null ? item.getTitle() : "";
        holder.tvTitle.setText(title);

        // Description
        String desc = item.getDescription() != null
                ? item.getDescription() : "";
        holder.tvDescription.setText(desc);

        // Type badge: SHARING / REQUESTING
        String type = item.getType() != null ? item.getType() : "sharing";
        holder.tvTypeBadge.setText(type.equals("sharing")
                ? context.getString(R.string.my_posts_type_sharing)
                : context.getString(R.string.my_posts_type_requesting));
        if (type.equals("sharing")) {
            holder.tvTypeBadge.getBackground().setTint(
                    context.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.tvTypeBadge.getBackground().setTint(
                    context.getResources().getColor(R.color.colorOrange));
        }

        // Status
        String status = item.getStatus() != null
                ? item.getStatus() : "available";
        setStatus(holder, status);

        // Ảnh Base64
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(imageUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length);
                holder.ivItemImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivItemImage.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivItemImage.setImageResource(
                    android.R.drawable.ic_menu_gallery);
        }

        // Click toàn bộ card
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(item);
        });

        // Nút Edit
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item);
        });

        // Nút Complete
        holder.btnComplete.setOnClickListener(v -> {
            if (listener != null) listener.onComplete(item);
        });

        // Nút Delete
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    private void setStatus(MyPostViewHolder holder, String status) {
        switch (status) {
            case "available":
                holder.tvStatus.setText(context.getString(R.string.my_posts_status_live));
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorPrimary));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorPrimary));
                break;
            case "borrowed":
                holder.tvStatus.setText(context.getString(R.string.my_posts_status_progress));
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorOrange));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorOrange));
                break;
            case "completed":
                holder.tvStatus.setText(context.getString(R.string.my_posts_status_completed));
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorTextHint));
                holder.viewStatusDot.getBackground().setTint(
                        context.getResources().getColor(R.color.colorTextHint));
                break;
            default:
                holder.tvStatus.setText(context.getString(R.string.my_posts_status_requested));
                holder.tvStatus.setTextColor(
                        context.getResources().getColor(R.color.colorOrange));
                break;
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    public void updateList(List<Item> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged();
    }

    static class MyPostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvTypeBadge, tvTitle, tvDescription, tvStatus;
        LinearLayout btnEdit, btnComplete, btnDelete;
        View viewStatusDot;

        MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage   = itemView.findViewById(R.id.ivItemImage);
            tvTypeBadge   = itemView.findViewById(R.id.tvTypeBadge);
            tvTitle       = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus      = itemView.findViewById(R.id.tvStatus);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            btnEdit       = itemView.findViewById(R.id.btnEdit);
            btnComplete   = itemView.findViewById(R.id.btnComplete);
            btnDelete     = itemView.findViewById(R.id.btnDelete);
        }
    }
}
