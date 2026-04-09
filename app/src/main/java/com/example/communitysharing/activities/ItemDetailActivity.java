package com.example.communitysharing.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    private ImageView ivBack, ivShare, ivFavorite, ivItemImage, ivOwnerAvatar;
    private TextView tvTitle, tvCategory, tvStatus;
    private TextView tvDescription, tvOwnerName, tvLocation;
    private TextView tvViewProfile;
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

        // Lấy itemId từ Intent
        itemId = getIntent().getStringExtra("itemId");

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ view
        ivBack         = findViewById(R.id.ivBack);
        ivShare        = findViewById(R.id.ivShare);
        ivFavorite     = findViewById(R.id.ivFavorite);
        ivItemImage    = findViewById(R.id.ivItemImage);
        ivOwnerAvatar  = findViewById(R.id.ivOwnerAvatar);
        tvTitle        = findViewById(R.id.tvTitle);
        tvCategory     = findViewById(R.id.tvCategory);
        tvStatus       = findViewById(R.id.tvStatus);
        tvDescription  = findViewById(R.id.tvDescription);
        tvOwnerName    = findViewById(R.id.tvOwnerName);
        tvLocation     = findViewById(R.id.tvLocation);
        tvViewProfile  = findViewById(R.id.tvViewProfile);
        btnChat        = findViewById(R.id.btnChat);
        btnRequestItem = findViewById(R.id.btnRequestItem);

        // Back
        ivBack.setOnClickListener(v -> finish());

        // Toggle Favorite
        ivFavorite.setOnClickListener(v -> toggleFavorite());

        // Load dữ liệu item từ Firebase
        loadItemDetail();

        // Nút Chat
        btnChat.setOnClickListener(v -> openChat());

        // Nút Request Item
        btnRequestItem.setOnClickListener(v -> requestItem());
    }

    private void loadItemDetail() {
        mDatabase.child("items").child(itemId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentItem = snapshot.getValue(Item.class);
                        if (currentItem == null) return;

                        // Null check title
                        String title = currentItem.getTitle() != null
                                ? currentItem.getTitle() : "";
                        tvTitle.setText(title);

                        // Null check category - đây là chỗ bị lỗi
                        String category = currentItem.getCategory() != null
                                ? currentItem.getCategory() : "";
                        tvCategory.setText(category.isEmpty()
                                ? "OTHER" : category.toUpperCase());

                        // Null check description
                        String desc = currentItem.getDescription() != null
                                ? currentItem.getDescription() : "";
                        tvDescription.setText(desc);

                        // Null check ownerName
                        String ownerName = currentItem.getOwnerName() != null
                                ? currentItem.getOwnerName() : "Unknown";
                        tvOwnerName.setText(ownerName);

                        // Null check address
                        String address = currentItem.getAddress() != null
                                ? currentItem.getAddress() : "";
                        tvLocation.setText(" " + address);

                        // Null check status
                        String status = currentItem.getStatus() != null
                                ? currentItem.getStatus() : "available";
                        updateStatusBadge(status);

                        // Hiện ảnh từ Base64
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
                        } else {
                            ivItemImage.setImageResource(
                                    android.R.drawable.ic_menu_gallery);
                        }

                        // Ẩn nút nếu là item của chính mình
                        String myUid = mAuth.getCurrentUser().getUid();
                        if (currentItem.getOwnerId() != null
                                && currentItem.getOwnerId().equals(myUid)) {
                            btnRequestItem.setVisibility(View.GONE);
                            btnChat.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ItemDetailActivity.this,
                                "Failed to load item", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatusBadge(String status) {
        switch (status) {
            case "available":
                tvStatus.setText("Available Now");
                tvStatus.getBackground().setTint(
                        getResources().getColor(R.color.colorPrimary));
                break;
            case "borrowed":
                tvStatus.setText("Currently Borrowed");
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
        if (isFavorite) {
            ivFavorite.setImageResource(
                    android.R.drawable.btn_star_big_on);
            Toast.makeText(this, "Added to favorites",
                    Toast.LENGTH_SHORT).show();
        } else {
            ivFavorite.setImageResource(
                    android.R.drawable.btn_star_big_off);
            Toast.makeText(this, "Removed from favorites",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openChat() {
        if (currentItem == null) return;

        String myUid       = mAuth.getCurrentUser().getUid();
        String ownerUid    = currentItem.getOwnerId();
        String ownerName   = currentItem.getOwnerName();

        // Tạo conversationId từ 2 uid
        String convId = getChatId(myUid, ownerUid);

        Intent intent = new Intent(this, ChatDetailActivity.class);
        intent.putExtra("conversationId", convId);
        intent.putExtra("otherUserId", ownerUid);
        intent.putExtra("otherUserName", ownerName);
        startActivity(intent);
    }

    private void requestItem() {
        if (currentItem == null) return;

        String myUid     = mAuth.getCurrentUser().getUid();
        String myEmail   = mAuth.getCurrentUser().getEmail();
        String ownerUid  = currentItem.getOwnerId();

        // Không cho request item của chính mình
        if (ownerUid.equals(myUid)) {
            Toast.makeText(this, "This is your item!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Item đã được mượn
        if (!currentItem.getStatus().equals("available")) {
            Toast.makeText(this, "This item is not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable nút tránh bấm nhiều lần
        btnRequestItem.setEnabled(false);
        btnRequestItem.setText("Requesting...");

        // Gửi thông báo cho chủ item
        NotificationFragment.sendNotification(
                mDatabase,
                ownerUid,                          // Gửi cho chủ item
                "borrow_request",                  // Type
                "New Borrow Request",              // Title
                myEmail + " wants to borrow your "
                        + currentItem.getTitle(),      // Message
                myUid,                             // From
                itemId,                            // ItemId
                currentItem.getTitle()             // ItemName
        );

        Toast.makeText(this,
                "Request sent! Waiting for approval.",
                Toast.LENGTH_SHORT).show();

        btnRequestItem.setEnabled(true);
        btnRequestItem.setText("Request Sent ✓");
    }

    // Tạo conversationId từ 2 uid
    private String getChatId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) return uid1 + "_" + uid2;
        return uid2 + "_" + uid1;
    }
}
