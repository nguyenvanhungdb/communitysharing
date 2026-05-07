package com.example.communitysharing.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communitysharing.R;
import com.example.communitysharing.adapter.MessageAdapter;
import com.example.communitysharing.models.Conversation;
import com.example.communitysharing.models.Message;
import com.example.communitysharing.utils.LocaleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private FrameLayout btnSend;
    private TextView tvOtherUserName;
    private ImageView ivBack;

    private static final int PICK_IMAGE_REQUEST = 1;
    ImageView btnAdd;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String myUid;
    private String myName;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.applySavedLocale(this);
        setContentView(R.layout.activity_chat_detail);

        // Lấy data từ Intent
        conversationId = getIntent().getStringExtra("conversationId");
        otherUserId    = getIntent().getStringExtra("otherUserId");
        otherUserName  = getIntent().getStringExtra("otherUserName");

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        myUid     = mAuth.getCurrentUser().getUid();
        myName    = mAuth.getCurrentUser().getEmail();

        // Ánh xạ view
        rvMessages      = findViewById(R.id.rvMessages);
        etMessage       = findViewById(R.id.etMessage);
        btnSend         = findViewById(R.id.btnSend);
        tvOtherUserName = findViewById(R.id.tvOtherUserName);
        ivBack          = findViewById(R.id.ivBack);

        // Hiện tên người nhận
        tvOtherUserName.setText(otherUserName);

        // Setup RecyclerView
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll xuống cuối
        rvMessages.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(this, messageList, myUid);
        rvMessages.setAdapter(adapter);

// nút add
        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> openGallery());

        // Back button
        ivBack.setOnClickListener(v -> finish());

        // Nút Send
        btnSend.setOnClickListener(v -> sendMessage());

        // Bấm Enter trên bàn phím cũng gửi
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        // Load tin nhắn realtime
        loadMessages();
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        // Xóa text input ngay
        etMessage.setText("");

        // Tạo Message object
        Message message = new Message(myUid, myName, content);
        String messageId = mDatabase.child("messages")
                .child(conversationId).push().getKey();
        message.setMessageId(messageId);

        // 1. Lưu tin nhắn vào node messages/{conversationId}
        mDatabase.child("messages")
                .child(conversationId)
                .child(messageId)
                .setValue(message);

        // 2. Cập nhật conversation của MÌNH
        Conversation myConv = new Conversation(
                conversationId, otherUserId, otherUserName, content);
        mDatabase.child("chats").child(myUid)
                .child(conversationId).setValue(myConv);

        // 3. Cập nhật conversation của NGƯỜI KIA
        Conversation theirConv = new Conversation(
                conversationId, myUid, myName, content);
        mDatabase.child("chats").child(otherUserId)
                .child(conversationId).setValue(theirConv);
    }

    private void loadMessages() {
        // Lắng nghe realtime toàn bộ tin nhắn trong conversation
        mDatabase.child("messages").child(conversationId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Message msg = snap.getValue(Message.class);
                            if (msg != null) messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();

                        // Scroll xuống tin nhắn mới nhất
                        if (!messageList.isEmpty()) {
                            rvMessages.scrollToPosition(
                                    messageList.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
    private void sendImage(Uri uri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        String fileName = "chat_images/" + System.currentTimeMillis();
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                        String messageId = mDatabase.child("messages")
                                .child(conversationId).push().getKey();

                        Message message = new Message(
                                myUid,
                                myName,
                                downloadUri.toString(),
                                true // là ảnh
                        );

                        message.setMessageId(messageId);

                        // Lưu message
                        mDatabase.child("messages")
                                .child(conversationId)
                                .child(messageId)
                                .setValue(message);

                        // Update chat cho mình
                        Conversation myConv = new Conversation(
                                conversationId, otherUserId, otherUserName, "[image]");
                        mDatabase.child("chats").child(myUid)
                                .child(conversationId).setValue(myConv);

                        // Update chat cho người kia
                        Conversation theirConv = new Conversation(
                                conversationId, myUid, myName, "[image]");
                        mDatabase.child("chats").child(otherUserId)
                                .child(conversationId).setValue(theirConv);
                    });
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            sendImage(imageUri); // hàm bạn đã viết
        }
    }

}
