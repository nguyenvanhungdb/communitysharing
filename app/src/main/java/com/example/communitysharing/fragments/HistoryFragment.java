package com.example.communitysharing.fragments;

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

        // Ánh xạ view
        rvHistory   = view.findViewById(R.id.rvHistory);
        llEmpty     = view.findViewById(R.id.llEmpty);
        tabShared   = view.findViewById(R.id.tabShared);
        tabReceived = view.findViewById(R.id.tabReceived);

        // Setup RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HistoryAdapter(getContext(), filteredList, item -> {
            // TODO: Mở ItemDetailActivity
            // Intent intent = new Intent(getContext(),
            //     ItemDetailActivity.class);
            // intent.putExtra("itemId", item.getItemId());
            // startActivity(intent);
        });
        rvHistory.setAdapter(adapter);

        // Setup tabs
        setupTabs();

        // Load data từ Firebase
        loadHistory();

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
        // Reset cả 2 tab
        tabShared.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabShared.setTextColor(
                getResources().getColor(R.color.colorTextGray));

        tabReceived.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabReceived.setTextColor(
                getResources().getColor(R.color.colorTextGray));

        // Set tab active
        active.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_active));
        active.setTextColor(
                getResources().getColor(R.color.colorWhite));
    }

    private void loadHistory() {
        // Lắng nghe realtime node history/{myUid}
        mDatabase.child("history").child(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            HistoryItem item = snap.getValue(HistoryItem.class);
                            if (item != null) {
                                allList.add(0, item); // Mới nhất lên đầu
                            }
                        }
                        filterList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // Lọc shared hoặc received
    private void filterList() {
        filteredList.clear();
        for (HistoryItem item : allList) {
            String type = item.getType() != null ? item.getType() : "";
            if (type.equals(currentTab)) {
                filteredList.add(item);
            }
        }
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