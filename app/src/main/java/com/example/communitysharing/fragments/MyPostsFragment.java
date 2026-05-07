package com.example.communitysharing.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.ItemDetailActivity;
import com.example.communitysharing.adapter.MyPostsAdapter;
import com.example.communitysharing.models.Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyPostsFragment extends Fragment {

    private RecyclerView rvMyPosts;
    private LinearLayout llEmpty;
    private TextView tabActive, tabArchived;

    

    private MyPostsAdapter adapter;
    private List<Item> allList      = new ArrayList<>();
    private List<Item> filteredList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String myUid;
    private String currentTab = "active";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_my_posts, container, false);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myUid     = mAuth.getCurrentUser().getUid();

        // Ánh xạ view
        rvMyPosts   = view.findViewById(R.id.rvMyPosts);
        llEmpty     = view.findViewById(R.id.llEmpty);
        tabActive   = view.findViewById(R.id.tabActive);
        tabArchived = view.findViewById(R.id.tabArchived);


        // Setup RecyclerView
        rvMyPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MyPostsAdapter(getContext(), filteredList,
                new MyPostsAdapter.OnMyPostActionListener() {

                    @Override
                    public void onClick(Item item) {
                        // Mở ItemDetailActivity
                        Intent intent = new Intent(getContext(),
                                ItemDetailActivity.class);
                        intent.putExtra("itemId", item.getItemId());
                        startActivity(intent);
                    }

                    @Override
                    public void onEdit(Item item) {
                        // Tạo bundle truyền data item sang ShareFragment
                        Bundle bundle = new Bundle();
                        bundle.putString("editItemId",    item.getItemId());
                        bundle.putString("editTitle",     item.getTitle());
                        bundle.putString("editDesc",      item.getDescription());
                        bundle.putString("editAddress",   item.getAddress());
                        bundle.putString("editCategory",  item.getCategory());
                        bundle.putString("editImageUrl",  item.getImageUrl());
                        bundle.putDouble("editLat",       item.getLatitude());
                        bundle.putDouble("editLng",       item.getLongitude());

                        ShareFragment shareFragment = new ShareFragment();
                        shareFragment.setArguments(bundle);

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragmentContainer, shareFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onComplete(Item item) {
                        // Hiện dialog xác nhận
                        showCompleteDialog(item);
                    }

                    @Override
                    public void onDelete(Item item) {
                        // Hiện dialog xác nhận xóa
                        showDeleteDialog(item);
                    }
                });

        rvMyPosts.setAdapter(adapter);



        // Setup tabs
        setupTabs();

        // Load data
        loadMyPosts();

        return view;
    }

    private void setupTabs() {
        tabActive.setOnClickListener(v -> {
            currentTab = "active";
            setActiveTab(tabActive);
            filterList();
        });
        tabArchived.setOnClickListener(v -> {
            currentTab = "archived";
            setActiveTab(tabArchived);
            filterList();
        });
    }

    private void setActiveTab(TextView active) {
        tabActive.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabActive.setTextColor(
                getResources().getColor(R.color.colorTextGray));

        tabArchived.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_inactive));
        tabArchived.setTextColor(
                getResources().getColor(R.color.colorTextGray));

        active.setBackground(
                getResources().getDrawable(R.drawable.bg_tab_active));
        active.setTextColor(
                getResources().getColor(R.color.colorWhite));
    }

    private void loadMyPosts() {
        // Query chỉ lấy items của mình theo ownerId
        mDatabase.child("items")
                .orderByChild("ownerId")
                .equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Item item = snap.getValue(Item.class);
                            if (item != null) allList.add(item);
                        }
                        filterList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void filterList() {
        filteredList.clear();
        for (Item item : allList) {
            String status = item.getStatus() != null
                    ? item.getStatus() : "available";

            if (currentTab.equals("active")) {
                // Tab Active: available + borrowed
                if (status.equals("available")
                        || status.equals("borrowed")) {
                    filteredList.add(item);
                }
            } else {
                // Tab Archived: completed + cancelled
                if (status.equals("completed")
                        || status.equals("cancelled")) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            rvMyPosts.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvMyPosts.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    // Dialog xác nhận Complete
    private void showCompleteDialog(Item item) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.my_posts_complete_title))
                .setMessage(getString(R.string.my_posts_complete_message,
                        item.getTitle()))
                .setPositiveButton(getString(R.string.my_posts_complete_yes), (dialog, which) -> {
                    // Cập nhật status trên Firebase
                    mDatabase.child("items")
                            .child(item.getItemId())
                            .child("status")
                            .setValue("completed")
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(),
                                        getString(R.string.my_posts_marked_completed),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(getString(R.string.common_cancel), null)
                .show();
    }

    // Dialog xác nhận Delete
    private void showDeleteDialog(Item item) {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.my_posts_delete_title))
                .setMessage(getString(R.string.my_posts_delete_message,
                        item.getTitle()))
                .setPositiveButton(getString(R.string.my_posts_delete), (dialog, which) -> {
                    // Xóa item khỏi Firebase
                    mDatabase.child("items")
                            .child(item.getItemId())
                            .removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(),
                                        getString(R.string.my_posts_deleted),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton(getString(R.string.common_cancel), null)
                .show();
    }
}
