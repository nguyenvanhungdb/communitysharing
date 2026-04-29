package com.example.communitysharing.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.HomeActivity;
import com.example.communitysharing.activities.ItemDetailActivity;
import com.example.communitysharing.activities.MapActivity;
import com.example.communitysharing.activities.NewRequestActivity;
import com.example.communitysharing.adapter.ItemAdapter;
import com.example.communitysharing.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvItems;
    private Button btnFilter;
    private ItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Featured
    private LinearLayout llFeatured;
    private TextView tvFeaturedTitle, tvFeaturedDistance;
    private Button btnRequestFeatured;
    private Item featuredItem = null;

    // Tabs
    private TextView tabAll, tabFood, tabClothes, tabTools;
    private String currentCategory = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ view
        rvItems = view.findViewById(R.id.rvItems);
        llFeatured = view.findViewById(R.id.llFeatured);
        btnRequestFeatured = view.findViewById(R.id.btnRequestFeatured);

        tabAll = view.findViewById(R.id.tabAll);
        tabFood = view.findViewById(R.id.tabFood);
        tabClothes = view.findViewById(R.id.tabClothes);
        tabTools = view.findViewById(R.id.tabTools);

        View btnFilter = view.findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(v -> {
            Fragment filterFragment = new FilterFragment();

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, filterFragment)
                    .addToBackStack(null)
                    .commit();
        });
// ===== 1. NOTIFICATION BUTTON =====
        ImageView ivNotification = view.findViewById(R.id.ivNotification);

        ivNotification.setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity())
                        .loadFragment(new NotificationFragment());
            }
        });
        // ===== 2. VIEW MAP =====
        TextView tvViewMap = view.findViewById(R.id.tvViewMap);

        // Trong HomeFragment.java - tvViewMap click
        tvViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MapActivity.class);
            intent.putExtra(MapActivity.EXTRA_SHOW_ALL, true); // ← Chế độ xem tất cả
            startActivity(intent);
        });

        // ===== 3. NEW REQUEST =====
        view.findViewById(R.id.ivNewRequest).setOnClickListener(v -> {
            startActivity(new Intent(getContext(),
                    NewRequestActivity.class));
        });

        // ===== 4. RECYCLER VIEW =====
        rvItems.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ItemAdapter(getContext(), itemList, item -> {
            Intent intent = new Intent(getContext(),
                    ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });

        rvItems.setAdapter(adapter);

        // ===== 5. FEATURED BUTTON =====
        btnRequestFeatured.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(),
                    NewRequestActivity.class);

            if (featuredItem != null) {
                intent.putExtra("itemTitle", featuredItem.getTitle());
            }

            startActivity(intent);
        });

        // ===== 6. TAB + DATA =====
        setupTabs();
        loadItemsFromFirebase("all");

        // ===== NOTIFICATION BADGE REALTIME =====

        TextView tvBadge = view.findViewById(R.id.tvBadge);

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(myUid);

        notifRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int unreadCount = 0;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Boolean isRead = snap.child("read").getValue(Boolean.class);

                    if (isRead == null || !isRead) {
                        unreadCount++;
                    }
                }

                // 👇 CHÍNH LÀ ĐOẠN BẠN HỎI - đặt ở đây
                if (unreadCount > 0) {

                    tvBadge.setVisibility(View.VISIBLE);

                    if (unreadCount > 9) {
                        tvBadge.setText("9+");
                    } else {
                        tvBadge.setText(String.valueOf(unreadCount));
                    }

                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        return view;
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            setActiveTab(tabAll);
            loadItemsFromFirebase("all");
        });
        tabFood.setOnClickListener(v -> {
            setActiveTab(tabFood);
            loadItemsFromFirebase("food");
        });
        tabClothes.setOnClickListener(v -> {
            setActiveTab(tabClothes);
            loadItemsFromFirebase("clothes");
        });
        tabTools.setOnClickListener(v -> {
            setActiveTab(tabTools);
            loadItemsFromFirebase("tools");
        });
    }

    private void setActiveTab(TextView activeTab) {
        for (TextView tab : new TextView[]{
                tabAll, tabFood, tabClothes, tabTools}) {
            tab.setBackground(getResources().getDrawable(
                    R.drawable.bg_tab_inactive));
            tab.setTextColor(getResources().getColor(
                    R.color.colorTextGray));
        }
        activeTab.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_active));
        activeTab.setTextColor(getResources().getColor(
                R.color.colorWhite));
    }

    private void loadItemsFromFirebase(String category) {
        String myUid = mAuth.getCurrentUser().getUid();

        mDatabase.child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemList.clear();
                        featuredItem = null;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            // Log raw data để debug
                            android.util.Log.d("HomeFragment",
                                    "Raw: " + snap.getValue());

                            // Đọc thủ công từng field
                            Item item = new Item();
                            item.setItemId(snap.getKey());

                            if (snap.child("title").getValue() != null)
                                item.setTitle(snap.child("title")
                                        .getValue(String.class));

                            if (snap.child("ownerId").getValue() != null)
                                item.setOwnerId(snap.child("ownerId")
                                        .getValue(String.class));

                            if (snap.child("ownerName").getValue() != null)
                                item.setOwnerName(snap.child("ownerName")
                                        .getValue(String.class));

                            if (snap.child("description").getValue() != null)
                                item.setDescription(snap.child("description")
                                        .getValue(String.class));

                            if (snap.child("category").getValue() != null)
                                item.setCategory(snap.child("category")
                                        .getValue(String.class));

                            if (snap.child("address").getValue() != null)
                                item.setAddress(snap.child("address")
                                        .getValue(String.class));

                            if (snap.child("status").getValue() != null)
                                item.setStatus(snap.child("status")
                                        .getValue(String.class));

                            if (snap.child("type").getValue() != null)
                                item.setType(snap.child("type")
                                        .getValue(String.class));

                            if (snap.child("imageUrl").getValue() != null)
                                item.setImageUrl(snap.child("imageUrl")
                                        .getValue(String.class));

                            if (snap.child("quantity").getValue() != null) {
                                Long qty = snap.child("quantity")
                                        .getValue(Long.class);
                                if (qty != null)
                                    item.setQuantity(qty.intValue());
                            }

                            if (snap.child("latitude").getValue() != null) {
                                Double lat = snap.child("latitude")
                                        .getValue(Double.class);
                                if (lat != null) item.setLatitude(lat);
                            }

                            if (snap.child("longitude").getValue() != null) {
                                Double lng = snap.child("longitude")
                                        .getValue(Double.class);
                                if (lng != null) item.setLongitude(lng);
                            }

                            android.util.Log.d("HomeFragment",
                                    "Parsed → title=" + item.getTitle()
                                            + " | owner=" + item.getOwnerId()
                                            + " | status=" + item.getStatus());

                            // Bỏ qua nếu không có title
                            if (item.getTitle() == null
                                    || item.getTitle().isEmpty()) continue;

                            // Ẩn item của chính mình
//                            if (myUid.equals(item.getOwnerId())) continue;

                            // Chỉ hiện available
                            String status = item.getStatus() != null
                                    ? item.getStatus() : "";
                            if (!status.equals("available")) continue;

                            // Lọc category
                            if (!category.equals("all")) {
                                String cat = item.getCategory() != null
                                        ? item.getCategory().toLowerCase() : "";
                                if (!cat.equals(category)) continue;
                            }

                            itemList.add(item);
                        }

                        android.util.Log.d("HomeFragment",
                                "Final list size: " + itemList.size());

                        // Cập nhật UI
                        updateUI();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("HomeFragment",
                                "Error: " + error.getMessage());
                    }
                });
    }

    private void updateUI() {
        if (!itemList.isEmpty()) {
            featuredItem = itemList.get(0);

            llFeatured.setVisibility(View.VISIBLE);

            // Grid hiện các item còn lại
            List<Item> gridItems = new ArrayList<>();
            if (itemList.size() > 1) {
                gridItems.addAll(
                        itemList.subList(1, itemList.size()));
            }
            adapter.updateList(gridItems);

        } else {
            llFeatured.setVisibility(View.GONE);
            adapter.updateList(new ArrayList<>());
        }
    }
}