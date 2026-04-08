package com.example.communitysharing.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.adapter.ItemAdapter;
import com.example.communitysharing.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView rvItems;
    private ItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private DatabaseReference mDatabase;

    // Tabs
    private TextView tabAll, tabFood, tabClothes, tabTools;
    private String currentCategory = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ view
        rvItems   = view.findViewById(R.id.rvItems);
        tabAll    = view.findViewById(R.id.tabAll);
        tabFood   = view.findViewById(R.id.tabFood);
        tabClothes = view.findViewById(R.id.tabClothes);
        tabTools  = view.findViewById(R.id.tabTools);

        // Setup RecyclerView dạng Grid 2 cột
        GridLayoutManager gridManager = new GridLayoutManager(getContext(), 2);
        rvItems.setLayoutManager(gridManager);

        // Setup Adapter
        adapter = new ItemAdapter(getContext(), itemList, item -> {
            // TODO: Mở ItemDetailActivity khi click
//             Intent intent = new Intent(getContext(), ItemDetailActivity.class);
//             intent.putExtra("itemId", item.getItemId());
//             startActivity(intent);
        });
        rvItems.setAdapter(adapter);

        // Setup tab click
        setupTabs();

        // Load data từ Firebase
        loadItemsFromFirebase("all");


        // Trong HomeFragment.java, thêm vào onCreate:
        view.findViewById(R.id.ivNotification).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new NotificationFragment())
                    .addToBackStack(null) // Có thể back lại
                    .commit();
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

    // Đổi tab active/inactive
    private void setActiveTab(TextView activeTab) {
        // Reset tất cả về inactive
        tabAll.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabAll.setTextColor(getResources().getColor(R.color.colorTextGray));

        tabFood.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabFood.setTextColor(getResources().getColor(R.color.colorTextGray));

        tabClothes.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabClothes.setTextColor(getResources().getColor(R.color.colorTextGray));

        tabTools.setBackground(getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabTools.setTextColor(getResources().getColor(R.color.colorTextGray));

        // Set tab được chọn thành active
        activeTab.setBackground(getResources().getDrawable(R.drawable.bg_tab_active));
        activeTab.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    // Load items từ Firebase Realtime Database
    private void loadItemsFromFirebase(String category) {
        mDatabase.child("items").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                itemList.clear();
                for (DataSnapshot itemSnap : snapshot.getChildren()) {
                    Item item = itemSnap.getValue(Item.class);
                    if (item == null) continue;

                    // Lọc theo category
                    if (category.equals("all")) {
                        itemList.add(item);
                    } else {
                        if (item.getCategory() != null &&
                                item.getCategory().toLowerCase().equals(category)) {
                            itemList.add(item);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }
}
