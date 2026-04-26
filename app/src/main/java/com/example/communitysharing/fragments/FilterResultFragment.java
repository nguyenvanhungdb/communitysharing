package com.example.communitysharing.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.ItemDetailActivity;
import com.example.communitysharing.adapter.ItemAdapter;
import com.example.communitysharing.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FilterResultFragment extends Fragment {

    private RecyclerView rvItems;
    private ItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private String category = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_filter_result, container, false);
        ImageView btnBack = view.findViewById(R.id.btnBack);
        rvItems = view.findViewById(R.id.rvItems);
        rvItems.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new ItemAdapter(getContext(), itemList, item -> {
            Intent intent = new Intent(getContext(), ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        rvItems.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        // nhận category từ FilterFragment
        if (getArguments() != null) {
            category = getArguments().getString("category", "all");
        }

        loadFilteredData();

        return view;
    }


    private void loadFilteredData() {
        mDatabase.child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        itemList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Item item = snap.getValue(Item.class);
                            if (item == null) continue;
                            item.setItemId(snap.getKey());

                            // chỉ lấy available
                            if (!"available".equals(item.getStatus())) continue;

                            // lọc category
                            if (!category.equals("all")) {
                                if (item.getCategory() == null ||
                                        !item.getCategory().equalsIgnoreCase(category))
                                    continue;
                            }

                            itemList.add(item);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}