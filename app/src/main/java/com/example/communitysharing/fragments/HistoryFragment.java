package com.example.communitysharing.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.ItemDetailActivity;
import com.example.communitysharing.adapter.HistoryAdapter;
import com.example.communitysharing.models.HistoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private LinearLayout llEmpty;
    private TextView tabShared, tabReceived;

    private HistoryAdapter adapter;
    private List<HistoryItem> allList      = new ArrayList<>();
    private List<HistoryItem> filteredList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String myUid;
    private String currentTab = "shared";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_history, container, false);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myUid     = mAuth.getCurrentUser().getUid();

        rvHistory   = view.findViewById(R.id.rvHistory);
        llEmpty     = view.findViewById(R.id.llEmpty);
        tabShared   = view.findViewById(R.id.tabShared);
        tabReceived = view.findViewById(R.id.tabReceived);

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(getContext(), filteredList, item -> {
            // Click → ItemDetailActivity
            Intent intent = new Intent(getContext(),
                    ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });
        rvHistory.setAdapter(adapter);

        setupTabs();
        loadFromItems(); // Load từ node items

        return view;
    }

    private void setupTabs() {
        tabShared.setOnClickListener(v -> {
            currentTab = "shared";
            setActiveTab(tabShared);
            filterList();
        });
        tabReceived.setOnClickListener(v -> {
            currentTab = "received";
            setActiveTab(tabReceived);
            filterList();
        });
    }

    private void setActiveTab(TextView active) {
        tabShared.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabShared.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        tabReceived.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabReceived.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        active.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_active));
        active.setTextColor(getResources().getColor(
                R.color.colorWhite));
    }

    // Load từ node "items" - convert Item → HistoryItem
    private void loadFromItems() {
        mDatabase.child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            // Đọc thủ công từng field
                            String itemId    = snap.getKey();
                            String ownerId   = snap.child("ownerId")
                                    .getValue(String.class);
                            String ownerName = snap.child("ownerName")
                                    .getValue(String.class);
                            String title     = snap.child("title")
                                    .getValue(String.class);
                            String status    = snap.child("status")
                                    .getValue(String.class);
                            String imageUrl  = snap.child("imageUrl")
                                    .getValue(String.class);
                            Long createdAt   = snap.child("createdAt")
                                    .getValue(Long.class);


                            if (imageUrl != null) {
                                android.util.Log.d("HistoryFragment",
                                        "Item: " + title + ", ImageUrl length: " + imageUrl.length());
                                android.util.Log.d("HistoryFragment",
                                        "ImageUrl prefix: " + imageUrl.substring(0, Math.min(50, imageUrl.length())));
                            }

                            if (title == null) continue;

                            // Convert sang HistoryItem
                            HistoryItem historyItem = new HistoryItem();
                            historyItem.setItemId(itemId);
                            historyItem.setItemTitle(title);
                            historyItem.setItemImageUrl(
                                    imageUrl != null ? imageUrl : "");
                            historyItem.setTimestamp(
                                    createdAt != null ? createdAt : 0);

                            // Tab SHARED = item mình đăng (ownerId = myUid)
                            if (myUid.equals(ownerId)) {
                                historyItem.setType("shared");
                                historyItem.setOtherUserId("");
                                historyItem.setOtherUserName("Community");
                                historyItem.setStatus(
                                        status != null ? status : "available");
                                allList.add(historyItem);
                            }
                            // Tab RECEIVED = item người khác đăng
                            // mà mình đã request (status = borrowed/completed)
                            else if (status != null
                                    && (status.equals("borrowed")
                                    || status.equals("completed"))) {
                                // Kiểm tra xem mình có request item này không
                                // bằng cách check node requests
                                checkIfRequested(snap, historyItem, ownerName);
                            }
                        }

                        filterList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // Kiểm tra xem user hiện tại có request item này không
    private void checkIfRequested(DataSnapshot itemSnap,
                                  HistoryItem historyItem, String ownerName) {
        // Đọc danh sách requests của item này
        mDatabase.child("requests")
                .orderByChild("itemId")
                .equalTo(historyItem.getItemId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot reqSnap : snapshot.getChildren()) {
                            String requesterId = reqSnap.child("requesterId")
                                    .getValue(String.class);
                            if (myUid.equals(requesterId)) {
                                // Mình đã request item này
                                historyItem.setType("received");
                                historyItem.setOtherUserId("");
                                historyItem.setOtherUserName(
                                        ownerName != null ? ownerName : "");
                                historyItem.setStatus(
                                        historyItem.getStatus() != null
                                                ? historyItem.getStatus() : "completed");
                                allList.add(historyItem);
                                filterList(); // Cập nhật UI
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterList() {
        filteredList.clear();
        for (HistoryItem item : allList) {
            String type = item.getType() != null
                    ? item.getType() : "";
            if (type.equals(currentTab)) {
                filteredList.add(item);
            }
        }

        // Sắp xếp mới nhất lên đầu
        filteredList.sort((a, b) ->
                Long.compare(b.getTimestamp(), a.getTimestamp()));

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }
}