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
import com.example.communitysharing.models.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private List<Item> itemList;

    // Interface để xử lý click
    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    private OnItemClickListener listener;

    public ItemAdapter(Context context, List<Item> itemList,
                       OnItemClickListener listener) {
        this.context  = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        // Set dữ liệu vào view
        holder.tvTitle.setText(item.getTitle());
        holder.tvDistance.setText("Nearby");
        if(item.getCategory() != null){
            holder.tvCategory.setText(item.getCategory().toUpperCase());
        }

        // Đổi màu tag theo category
        int tagColor;
        switch (item.getCategory().toLowerCase()) {
            case "food":
                tagColor = context.getResources().getColor(R.color.colorOrange);
                break;
            case "tools":
                tagColor = context.getResources().getColor(R.color.colorPrimary);
                break;
            default:
                tagColor = context.getResources().getColor(R.color.colorAccent);
                break;
        }
        holder.tvCategory.getBackground().setTint(tagColor);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        // Hiển thị ảnh từ Base64
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(
                        item.getImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        decodedBytes, 0, decodedBytes.length);
                holder.ivItemImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivItemImage.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivItemImage.setImageResource(
                    android.R.drawable.ic_menu_gallery);
        }
    }


    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Cập nhật danh sách khi Firebase trả về data mới
    public void updateList(List<Item> newList) {
        itemList.clear();
        itemList.addAll(newList);
        notifyDataSetChanged();
    }


    // ViewHolder
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage, ivFavorite;
        TextView tvTitle, tvDistance, tvCategory;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            ivFavorite  = itemView.findViewById(R.id.ivFavorite);
            tvTitle     = itemView.findViewById(R.id.tvItemTitle);
            tvDistance  = itemView.findViewById(R.id.tvDistance);
            tvCategory  = itemView.findViewById(R.id.tvCategory);
        }
    }
}
