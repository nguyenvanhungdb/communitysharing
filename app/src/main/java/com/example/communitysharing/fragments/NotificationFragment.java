package com.example.communitysharing.fragments;

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
import com.example.communitysharing.activities.ChatDetailActivity;
import com.example.communitysharing.adapter.NotificationAdapter;
import com.example.communitysharing.models.HistoryItem;
import com.example.communitysharing.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private LinearLayout llEmpty;
    private TextView tabAll, tabRequests, tabMessages;

    private NotificationAdapter adapter;
    private List<Notification> allList      = new ArrayList<>();
    private List<Notification> filteredList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String myUid;

    private String currentTab = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_notification, container, false);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myUid     = mAuth.getCurrentUser().getUid();

        // Ánh xạ view
        rvNotifications = view.findViewById(R.id.rvNotifications);
        llEmpty         = view.findViewById(R.id.llEmpty);
        tabAll          = view.findViewById(R.id.tabAll);
        tabRequests     = view.findViewById(R.id.tabRequests);
        tabMessages     = view.findViewById(R.id.tabMessages);

        // Setup RecyclerView
        rvNotifications.setLayoutManager(
                new LinearLayoutManager(getContext()));

        adapter = new NotificationAdapter(getContext(),
                filteredList, new NotificationAdapter.OnNotifActionListener() {

//            @Override
//            public void onAccept(Notification notif) {
//                // Cập nhật trạng thái item thành "borrowed"
//                mDatabase.child("items").child(notif.getItemId())
//                        .child("status").setValue("borrowed");
//
//                // Gửi thông báo lại cho người yêu cầu
//                sendNotification(
//                        notif.getFromUserId(),
//                        "pickup_approved",
//                        "Pickup Approved",
//                        "Your request for " + notif.getItemName()
//                                + " has been approved!",
//                        myUid, notif.getItemId(), notif.getItemName()
//                );
//
//                // Đánh dấu đã đọc
//                markAsRead(notif.getNotificationId());
//                Toast.makeText(getContext(),
//                        "Request accepted!", Toast.LENGTH_SHORT).show();
//
//                // Ghi history cho MÌNH (người share)
//                String historyId1 = mDatabase.child("history")
//                        .child(myUid).push().getKey();
//                HistoryItem myHistory = new HistoryItem(
//                        notif.getItemId(),
//                        notif.getItemName(),
//                        notif.getFromUserId(),
//                        notif.getFromUserName(),
//                        "in_progress",   // Đang tiến hành
//                        "shared"         // Mình là người share
//                );
//
//                myHistory.setHistoryId(historyId1);
//                mDatabase.child("history").child(myUid)
//                        .child(historyId1).setValue(myHistory);
//
//                // Ghi history cho NGƯỜI KIA (người nhận)
//                String historyId2 = mDatabase.child("history")
//                        .child(notif.getFromUserId()).push().getKey();
//                HistoryItem theirHistory = new HistoryItem(
//                        notif.getItemId(),
//                        notif.getItemName(),
//                        myUid,
//                        mAuth.getCurrentUser().getEmail(),
//                        "in_progress",   // Đang tiến hành
//                        "received"       // Họ là người nhận
//                );
//
//                theirHistory.setHistoryId(historyId2);
//                mDatabase.child("history").child(notif.getFromUserId())
//                        .child(historyId2).setValue(theirHistory);
//
//                markAsRead(notif.getNotificationId());
//                Toast.makeText(getContext(),
//                        "Request accepted!", Toast.LENGTH_SHORT).show();
//            }
@Override
public void onAccept(Notification notif) {

    // 1. Update item
    mDatabase.child("items")
            .child(notif.getItemId())
            .child("status")
            .setValue("borrowed");

    // 2. Gửi notif cho người kia
    sendNotification(
            notif.getFromUserId(),
            "pickup_approved",
            getString(R.string.notification_approved_title),
            getString(R.string.notification_approved_message),
            myUid,
            notif.getItemId(),
            notif.getItemName()
    );

    // 3. Ghi history
    writeHistory(notif);

    // 4. XÓA notif → realtime tự mất
    mDatabase.child("notifications")
            .child(myUid)
            .child(notif.getNotificationId())
            .removeValue();

    Toast.makeText(getContext(),
            getString(R.string.notification_accepted_toast),
            Toast.LENGTH_SHORT).show();
}

            // Hàm ghi history
            private void writeHistory(Notification notif) {
                // History của mình (người share)
                String histId1 = mDatabase.child("history")
                        .child(myUid).push().getKey();
                HistoryItem myHistory =
                        new HistoryItem(
                                notif.getItemId(),
                                notif.getItemName(),
                                notif.getFromUserId(),
                                notif.getFromUserName(),
                                "in_progress",
                                "shared");
                myHistory.setHistoryId(histId1);
                mDatabase.child("history").child(myUid)
                        .child(histId1).setValue(myHistory);

                // History của người nhận
                String histId2 = mDatabase.child("history")
                        .child(notif.getFromUserId()).push().getKey();
                HistoryItem theirHistory =
                        new HistoryItem(
                                notif.getItemId(),
                                notif.getItemName(),
                                myUid,
                                mAuth.getCurrentUser().getEmail(),
                                "in_progress",
                                "received");
                theirHistory.setHistoryId(histId2);
                mDatabase.child("history")
                        .child(notif.getFromUserId())
                        .child(histId2).setValue(theirHistory);
            }

            @Override
            public void onDecline(Notification notif) {

                sendNotification(
                        notif.getFromUserId(),
                        "request_declined",
                        getString(R.string.notification_declined_title),
                        getString(R.string.notification_declined_message),
                        myUid,
                        notif.getItemId(),
                        notif.getItemName()
                );

                // XÓA luôn → realtime biến mất
                mDatabase.child("notifications")
                        .child(myUid)
                        .child(notif.getNotificationId())
                        .removeValue();

                Toast.makeText(getContext(),
                        getString(R.string.notification_declined_toast),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReplyNow(Notification notif) {

                String otherUserId = notif.getFromUserId();

                if (otherUserId == null || otherUserId.isEmpty()) {
                    Toast.makeText(getContext(),
                            getString(R.string.notification_user_error),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String convId = getChatId(myUid, otherUserId);

                Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                intent.putExtra("conversationId", convId);
                intent.putExtra("otherUserId", otherUserId);
                intent.putExtra("otherUserName", notif.getFromUserName());

                startActivity(intent);

                markAsRead(notif.getNotificationId());
            }

            @Override
            public void onCoordinatePickup(Notification notif) {
                // Mở chat để hẹn giờ pickup
                String convId = getChatId(myUid, notif.getFromUserId());
                Intent intent = new Intent(getContext(),
                        ChatDetailActivity.class);
                intent.putExtra("conversationId", convId);
                intent.putExtra("otherUserId", notif.getFromUserId());
                intent.putExtra("otherUserName", notif.getFromUserName());
                startActivity(intent);
                markAsRead(notif.getNotificationId());
            }

            @Override
            public void onViewListing(Notification notif) {
                // TODO: Mở ItemDetailActivity
                markAsRead(notif.getNotificationId());
            }

            @Override
            public void onClick(Notification notif) {
                markAsRead(notif.getNotificationId());
            }
        });

        rvNotifications.setAdapter(adapter);

        // Setup tabs
        setupTabs();

        // Load notifications realtime
        loadNotifications();

        return view;
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> {
            currentTab = "all";
            setActiveTab(tabAll);
            filterList();
        });
        tabRequests.setOnClickListener(v -> {
            currentTab = "requests";
            setActiveTab(tabRequests);
            filterList();
        });
        tabMessages.setOnClickListener(v -> {
            currentTab = "messages";
            setActiveTab(tabMessages);
            filterList();
        });
    }

    private void setActiveTab(TextView active) {
        tabAll.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabAll.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        tabRequests.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabRequests.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        tabMessages.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_inactive));
        tabMessages.setTextColor(getResources().getColor(
                R.color.colorTextGray));

        active.setBackground(getResources().getDrawable(
                R.drawable.bg_tab_active));
        active.setTextColor(getResources().getColor(
                R.color.colorWhite));
    }

    private void loadNotifications() {

        mDatabase.child("notifications").child(myUid)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        allList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            Notification notif = snap.getValue(Notification.class);

                            if (notif != null) {

                                // 🔥 QUAN TRỌNG
                                notif.setNotificationId(snap.getKey());

                                // 🔥 FIX NULL DATA
                                if (notif.getType() == null) notif.setType("");
                                if (notif.getTitle() == null) notif.setTitle("");
                                if (notif.getMessage() == null) notif.setMessage("");

                                allList.add(0, notif);
                            }
                        }

                        filterList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // Lọc danh sách theo tab
    private void filterList() {
        filteredList.clear();

        for (Notification notif : allList) {

            // 🔥 chống null
            String type = notif.getType() != null ? notif.getType() : "";

            switch (currentTab) {

                case "all":
                    filteredList.add(notif);
                    break;

                case "requests":
                    if (type.equals("borrow_request")
                            || type.equals("pickup_approved")
                            || type.equals("request_declined")) {

                        filteredList.add(notif);
                    }
                    break;

                case "messages":
                    if (type.equals("new_message")) {
                        filteredList.add(notif);
                    }
                    break;
            }
        }

        adapter.notifyDataSetChanged();

        // Empty state
        if (filteredList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
        }
    }

    // Đánh dấu thông báo đã đọc
    private void markAsRead(String notifId) {
        mDatabase.child("notifications").child(myUid)
                .child(notifId).child("read").setValue(true);
    }

    // Gửi thông báo cho người khác
    public static void sendNotification(
            DatabaseReference db,
            String toUserId,
            String type,
            String title,
            String message,
            String fromUserId,
            String itemId,
            String itemName) {

        String notifId = db.child("notifications")
                .child(toUserId).push().getKey();

        Notification notif = new Notification(
                type, title, message,
                fromUserId, "", itemId, itemName);
        notif.setNotificationId(notifId);

        db.child("notifications").child(toUserId)
                .child(notifId).setValue(notif);
    }

    // Helper gửi từ trong Fragment
    private void sendNotification(String toUserId, String type,
                                  String title, String message,
                                  String fromUserId, String itemId, String itemName) {
        sendNotification(mDatabase, toUserId, type, title,
                message, fromUserId, itemId, itemName);
    }

    // Tạo conversation ID giống ChatFragment
    private String getChatId(String uid1, String uid2) {
        if (uid1 == null || uid2 == null) return null;

        if (uid1.compareTo(uid2) < 0) return uid1 + "_" + uid2;
        return uid2 + "_" + uid1;
    }
}
