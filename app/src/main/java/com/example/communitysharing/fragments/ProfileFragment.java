package com.example.communitysharing.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.ItemDetailActivity;
import com.example.communitysharing.activities.LoginActivity;
import com.example.communitysharing.adapter.ItemAdapter;
import com.example.communitysharing.models.Item;
import com.example.communitysharing.models.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;


import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    // Views
    private ImageView ivAvatar, ivSettings, ivNotification;
    private TextView tvFullName, tvAddress;
    private TextView tabShared, tabRequested;
    private RelativeLayout rlFeaturedItem;  // Dùng RelativeLayout
    private ImageView ivFeaturedImage;
    private TextView tvFeaturedTitle, tvFeaturedSubtitle, tvFeaturedBadge;
    private RecyclerView rvProfileItems;
    private LinearLayout btnMyPosts, btnHistory, btnSettings, btnLogout;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String myUid;

    // Data
    private ItemAdapter adapter;
    private List<Item> allItems      = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();
    private String currentTab = "shared";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_profile, container, false);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myUid     = mAuth.getCurrentUser().getUid();

        // Ánh xạ views - dùng đúng kiểu
        ivAvatar        = view.findViewById(R.id.ivAvatar);
        ivSettings      = view.findViewById(R.id.ivSettings);
        ivNotification  = view.findViewById(R.id.ivNotification);
        tvFullName      = view.findViewById(R.id.tvFullName);
        tvAddress       = view.findViewById(R.id.tvAddress);
        tabShared       = view.findViewById(R.id.tabShared);
        tabRequested    = view.findViewById(R.id.tabRequested);
        rlFeaturedItem  = view.findViewById(R.id.rlFeaturedItem);
        ivFeaturedImage = view.findViewById(R.id.ivFeaturedImage);
        tvFeaturedTitle    = view.findViewById(R.id.tvFeaturedTitle);
        tvFeaturedSubtitle = view.findViewById(R.id.tvFeaturedSubtitle);
        tvFeaturedBadge    = view.findViewById(R.id.tvFeaturedBadge);
        rvProfileItems  = view.findViewById(R.id.rvProfileItems);
        btnMyPosts      = view.findViewById(R.id.btnMyPosts);
        btnHistory      = view.findViewById(R.id.btnHistory);
        btnSettings     = view.findViewById(R.id.btnSettings);
        btnLogout       = view.findViewById(R.id.btnLogout);

        // Setup RecyclerView
        rvProfileItems.setLayoutManager(
                new GridLayoutManager(getContext(), 2));
        adapter = new ItemAdapter(getContext(), filteredItems, item -> {
            Intent intent = new Intent(getContext(),
                    ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });
        rvProfileItems.setAdapter(adapter);

        // Settings icon header
        ivSettings.setOnClickListener(v -> openSettings());
        ivNotification.setOnClickListener(v -> openNotifications());

        // Load data
        loadUserInfo();
        loadItems();

        // Tabs
        setupTabs();

        // Buttons
        setupButtons();

        return view;
    }

    private void loadUserInfo() {
        mDatabase.child("users").child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);
                        if (user == null) return;

                        // Tên
                        String name = user.getFullName() != null
                                ? user.getFullName() : "User";
                        tvFullName.setText(name);

                        // Địa chỉ
                        String addr = user.getAddress() != null
                                ? user.getAddress() : "No location set";
                        tvAddress.setText(" " + addr);

                        // Avatar Base64
                        String avatarUrl = user.getAvatarUrl();
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            try {
                                byte[] bytes = Base64.decode(
                                        avatarUrl, Base64.DEFAULT);
                                Bitmap bitmap = BitmapFactory
                                        .decodeByteArray(bytes, 0, bytes.length);
                                ivAvatar.setImageBitmap(bitmap);
                                ivAvatar.setPadding(0, 0, 0, 0);
                            } catch (Exception e) {
                                // Giữ icon mặc định
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadItems() {
        mDatabase.child("items")
                .orderByChild("ownerId")
                .equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allItems.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Item item = snap.getValue(Item.class);
                            if (item != null) allItems.add(item);
                        }
                        filterItems();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterItems() {
        filteredItems.clear();

        for (Item item : allItems) {
            String type = item.getType() != null
                    ? item.getType() : "sharing";
            if (currentTab.equals("shared")
                    && type.equals("sharing")) {
                filteredItems.add(item);
            } else if (currentTab.equals("requested")
                    && type.equals("requesting")) {
                filteredItems.add(item);
            }
        }

        // Featured item = item đầu tiên
        if (!filteredItems.isEmpty()) {
            showFeaturedItem(filteredItems.get(0));
            rlFeaturedItem.setVisibility(View.VISIBLE);

            // Grid = các item còn lại
            List<Item> gridItems = new ArrayList<>();
            if (filteredItems.size() > 1) {
                gridItems.addAll(
                        filteredItems.subList(1, filteredItems.size()));
            }
            adapter.updateList(gridItems);
        } else {
            rlFeaturedItem.setVisibility(View.GONE);
            adapter.updateList(new ArrayList<>());
        }
    }

    private void showFeaturedItem(Item item) {
        tvFeaturedTitle.setText(
                item.getTitle() != null ? item.getTitle() : "");

        String status = item.getStatus() != null
                ? item.getStatus() : "available";
        tvFeaturedSubtitle.setText(
                "Status: " + status);
        tvFeaturedBadge.setText(
                status.equals("available")
                        ? "ACTIVE SHARE" : status.toUpperCase());

        // Ảnh
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(
                        imageUrl, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory
                        .decodeByteArray(bytes, 0, bytes.length);
                ivFeaturedImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                ivFeaturedImage.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }
        }

        // Click → ItemDetailActivity
        rlFeaturedItem.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(),
                    ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });
    }

    private void setupTabs() {
        tabShared.setOnClickListener(v -> {
            currentTab = "shared";
            setActiveTab(tabShared);
            filterItems();
        });
        tabRequested.setOnClickListener(v -> {
            currentTab = "requested";
            setActiveTab(tabRequested);
            filterItems();
        });
    }

    private void setActiveTab(TextView active) {
        tabShared.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabShared.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        tabRequested.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabRequested.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        active.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_active));
        active.setTextColor(getResources().getColor(
                R.color.colorWhite));
    }

    private void setupButtons() {

        btnMyPosts.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer,
                            new MyPostsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnHistory.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer,
                            new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnSettings.setOnClickListener(v -> openSettings());

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(getContext(),
                                LoginActivity.class);
                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void openSettings() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

    private void openNotifications() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,
                        new NotificationFragment())
                .addToBackStack(null)
                .commit();
    }
}