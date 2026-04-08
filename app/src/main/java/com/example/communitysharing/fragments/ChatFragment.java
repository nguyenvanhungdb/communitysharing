package com.example.communitysharing.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.activities.ChatDetailActivity;
import com.example.communitysharing.adapter.ConversationAdapter;
import com.example.communitysharing.models.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import android.content.Intent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView rvConversations;
    private LinearLayout llEmpty;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,
                container, false);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvConversations = view.findViewById(R.id.rvConversations);
        llEmpty         = view.findViewById(R.id.llEmpty);

        // Setup RecyclerView
        rvConversations.setLayoutManager(
                new LinearLayoutManager(getContext()));

        adapter = new ConversationAdapter(getContext(),
                conversationList, conversation -> {
            // Mở ChatDetailActivity khi click vào conversation
            Intent intent = new Intent(getContext(),
                    ChatDetailActivity.class);
            intent.putExtra("conversationId",
                    conversation.getConversationId());
            intent.putExtra("otherUserId",
                    conversation.getOtherUserId());
            intent.putExtra("otherUserName",
                    conversation.getOtherUserName());
            startActivity(intent);
        });
        rvConversations.setAdapter(adapter);

        // Load conversations từ Firebase
        loadConversations();

        return view;
    }

    private void loadConversations() {
        String myUid = mAuth.getCurrentUser().getUid();

        // Lắng nghe realtime node chats/{myUid}
        mDatabase.child("chats").child(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        conversationList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Conversation conv = snap.getValue(Conversation.class);
                            if (conv != null) {
                                conversationList.add(conv);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // Hiện empty state nếu không có conversation nào
                        if (conversationList.isEmpty()) {
                            rvConversations.setVisibility(View.GONE);
                            llEmpty.setVisibility(View.VISIBLE);
                        } else {
                            rvConversations.setVisibility(View.VISIBLE);
                            llEmpty.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
