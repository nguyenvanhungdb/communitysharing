package com.example.communitysharing.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.communitysharing.R;
import com.example.communitysharing.fragments.NotificationFragment;
import com.example.communitysharing.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ItemDetailActivity extends AppCompatActivity {

    private ImageView ivBack, ivShare, ivFavorite, ivItemImage;
    private TextView tvTitle, tvCategory, tvStatus;
    private TextView tvDescription, tvOwnerName, tvLocation;
    private TextView tvViewProfile;
    private LinearLayout llLocation;
    private Button btnChat, btnRequestItem;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private String itemId;
    private Item currentItem;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        itemId    = getIntent().getStringExtra("itemId");
        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ
        ivBack        = findViewById(R.id.ivBack);
        ivShare       = findViewById(R.id.ivShare);
        ivFavorite    = findViewById(R.id.ivFavorite);
        ivItemImage   = findViewById(R.id.ivItemImage);
        tvTitle       = findViewById(R.id.tvTitle);
        tvCategory    = findViewById(R.id.tvCategory);
        tvStatus      = findViewById(R.id.tvStatus);
        tvDescription = findViewById(R.id.tvDescription);
        tvOwnerName   = findViewById(R.id.tvOwnerName);
        tvLocation    = findViewById(R.id.tvLocation);
        llLocation    = findViewById(R.id.llLocation);
        tvViewProfile = findViewById(R.id.tvViewProfile);
        btnChat       = findViewById(R.id.btnChat);
        btnRequestItem = findViewById(R.id.btnRequestItem);

        ivBack.setOnClickListener(v -> finish());
        ivFavorite.setOnClickListener(v -> toggleFavorite());

        loadItemDetail();
    }

    private void loadItemDetail() {
        mDatabase.child("items").child(itemId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentItem = snapshot.getValue(Item.class);
                        if (currentItem == null) return;

                        // Hiện data
                        String title = currentItem.getTitle() != null
                                ? currentItem.getTitle() : "";
                        tvTitle.setText(title);

                        String cat = currentItem.getCategory() != null
                                ? currentItem.getCategory() : "";
                        tvCategory.setText(cat.isEmpty()
                                ? "OTHER" : cat.toUpperCase());

                        String desc = currentItem.getDescription() != null
                                ? currentItem.getDescription() : "";
                        tvDescription.setText(desc);

                        String owner = currentItem.getOwnerName() != null
                                ? currentItem.getOwnerName() : "Unknown";
                        tvOwnerName.setText(owner);

                        String addr = currentItem.getAddress() != null
                                ? currentItem.getAddress() : "No address";
                        tvLocation.setText(addr);

                        String status = currentItem.getStatus() != null
                                ? currentItem.getStatus() : "available";
                        updateStatusBadge(status);

                        // Ảnh Base64
                        String imageUrl = currentItem.getImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(
                                        imageUrl, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory
                                        .decodeByteArray(bytes, 0, bytes.length);
                                ivItemImage.setImageBitmap(bitmap);
                            } catch (Exception e) {
                                ivItemImage.setImageResource(
                                        android.R.drawable.ic_menu_gallery);
                            }
                        }

                        llLocation.setOnClickListener(v -> {
                            Intent intent = new Intent(ItemDetailActivity.this, MapActivity.class);
                            intent.putExtra(MapActivity.EXTRA_SHOW_ALL, false); // ← Chế độ 1 item
                            intent.putExtra(MapActivity.EXTRA_ITEM_LAT,
                                    currentItem.getLatitude());
                            intent.putExtra(MapActivity.EXTRA_ITEM_LNG,
                                    currentItem.getLongitude());
                            intent.putExtra(MapActivity.EXTRA_ITEM_TITLE,
                                    currentItem.getTitle());
                            intent.putExtra(MapActivity.EXTRA_ITEM_ADDRESS,
                                    currentItem.getAddress());
                            intent.putExtra(MapActivity.EXTRA_ITEM_OWNER,
                                    currentItem.getOwnerName());
                            intent.putExtra(MapActivity.EXTRA_ITEM_IMAGE,
                                    currentItem.getImageUrl());
                            intent.putExtra(MapActivity.EXTRA_ITEM_ID,    // ← Thêm itemId
                                    itemId);
                            startActivity(intent);
                        });

                        String myUid = mAuth.getCurrentUser().getUid();

                        // Ẩn nút nếu là item của mình
                        if (currentItem.getOwnerId() != null
                                && currentItem.getOwnerId().equals(myUid)) {
                            btnRequestItem.setVisibility(View.GONE);
                            btnChat.setVisibility(View.GONE);
                            return;
                        }

                        // Item không available → ẩn request
                        if (!status.equals("available")) {
                            btnRequestItem.setEnabled(false);
                            btnRequestItem.setText("Not Available");
                            btnRequestItem.setAlpha(0.5f);
                        }

                        // Nút CHAT → mở ChatDetailActivity trước
                        btnChat.setOnClickListener(v -> {
                            String convId = getChatId(myUid,
                                    currentItem.getOwnerId());
                            Intent intent = new Intent(
                                    ItemDetailActivity.this,
                                    ChatDetailActivity.class);
                            intent.putExtra("conversationId", convId);
                            intent.putExtra("otherUserId",
                                    currentItem.getOwnerId());
                            intent.putExtra("otherUserName",
                                    currentItem.getOwnerName());
                            // Truyền thêm itemId để hiện trong chat
                            intent.putExtra("itemId", itemId);
                            intent.putExtra("itemTitle",
                                    currentItem.getTitle());
                            startActivity(intent);
                        });

                        // Nút REQUEST ITEM → gửi yêu cầu
                        btnRequestItem.setOnClickListener(v -> {
                            if (!status.equals("available")) {
                                Toast.makeText(ItemDetailActivity.this,
                                        "This item is not available",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            sendBorrowRequest(myUid);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ItemDetailActivity.this,
                                "Failed to load item",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendBorrowRequest(String myUid) {
        // Disable nút
        btnRequestItem.setEnabled(false);
        btnRequestItem.setText("Sending...");

        String myEmail = mAuth.getCurrentUser().getEmail();

        // Gửi notification cho chủ item
        NotificationFragment.sendNotification(
                mDatabase,
                currentItem.getOwnerId(),
                "borrow_request",
                "New Borrow Request",
                myEmail + " wants to borrow your "
                        + currentItem.getTitle()
                        + ". Please check chat first.",
                myUid,
                itemId,
                currentItem.getTitle()
        );

        // Cập nhật status → "requested"
        mDatabase.child("items").child(itemId)
                .child("status").setValue("requested");

        Toast.makeText(this,
                "Request sent! Chat with owner to confirm.",
                Toast.LENGTH_LONG).show();

        btnRequestItem.setText("Request Sent ✓");
    }

    private void updateStatusBadge(String status) {
        switch (status) {
            case "available":
                tvStatus.setText("Available Now");
                tvStatus.getBackground().setTint(
                        getResources().getColor(R.color.colorPrimary));
                break;
            case "requested":
                tvStatus.setText("Requested");
                tvStatus.getBackground().setTint(
                        getResources().getColor(R.color.colorOrange));
                break;
            case "borrowed":
                tvStatus.setText("Borrowed");
                tvStatus.getBackground().setTint(
                        getResources().getColor(R.color.colorOrange));
                break;
            case "completed":
                tvStatus.setText("Completed");
                tvStatus.getBackground().setTint(
                        getResources().getColor(R.color.colorTextHint));
                break;
        }
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        ivFavorite.setImageResource(isFavorite
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
        Toast.makeText(this,
                isFavorite ? "Added to favorites"
                        : "Removed from favorites",
                Toast.LENGTH_SHORT).show();
    }

    private String getChatId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) return uid1 + "_" + uid2;
        return uid2 + "_" + uid1;
    }
}