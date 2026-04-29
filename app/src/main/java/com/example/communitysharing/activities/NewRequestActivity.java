package com.example.communitysharing.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewRequestActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA     = 101;
    private static final int REQUEST_PERMISSION = 200;

    // Views
    private ImageView ivClose, ivAvatar;
    private EditText etItemTitle, etDescription;
    private LinearLayout llUrgencyLow, llUrgencyMedium, llUrgencyHigh;
    private LinearLayout llAddPhoto, llPhotoPlaceholder;
    private ImageView ivReferencePhoto;
    private Button btnPostRequest;
    private TextView tvError;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // State
    private String selectedUrgency = "medium";
    private String imageBase64     = "";
    private Uri currentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);

        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ views
        ivClose            = findViewById(R.id.ivClose);
        etItemTitle        = findViewById(R.id.etItemTitle);
        etDescription      = findViewById(R.id.etDescription);
        llUrgencyLow       = findViewById(R.id.llUrgencyLow);
        llUrgencyMedium    = findViewById(R.id.llUrgencyMedium);
        llUrgencyHigh      = findViewById(R.id.llUrgencyHigh);
        llAddPhoto         = findViewById(R.id.llAddPhoto);
        llPhotoPlaceholder = findViewById(R.id.llPhotoPlaceholder);
        ivReferencePhoto   = findViewById(R.id.ivReferencePhoto);
        btnPostRequest     = findViewById(R.id.btnPostRequest);
        tvError            = findViewById(R.id.tvError);

        // Nếu được mở từ HomeFragment với item có sẵn
        // → tự điền title
        String prefilledTitle = getIntent()
                .getStringExtra("itemTitle");
        if (prefilledTitle != null && !prefilledTitle.isEmpty()) {
            etItemTitle.setText(prefilledTitle);
        }

        // Mặc định chọn Medium
        setUrgencyActive(llUrgencyMedium);

        setupClickListeners();
    }

    private void setupClickListeners() {

        // Close
        ivClose.setOnClickListener(v -> finish());

        // Urgency buttons
        llUrgencyLow.setOnClickListener(v -> {
            selectedUrgency = "low";
            setUrgencyActive(llUrgencyLow);
        });
        llUrgencyMedium.setOnClickListener(v -> {
            selectedUrgency = "medium";
            setUrgencyActive(llUrgencyMedium);
        });
        llUrgencyHigh.setOnClickListener(v -> {
            selectedUrgency = "high";
            setUrgencyActive(llUrgencyHigh);
        });

        // Add photo
        llAddPhoto.setOnClickListener(v -> openImagePicker());

        // Post Request
        btnPostRequest.setOnClickListener(v -> postRequest());
    }
    private void openImagePicker() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Chọn nguồn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, REQUEST_CAMERA);
    }
    // Đổi UI urgency active/inactive
    private void setUrgencyActive(LinearLayout active) {
        // Reset tất cả về inactive
        for (LinearLayout ll : new LinearLayout[]{
                llUrgencyLow, llUrgencyMedium, llUrgencyHigh}) {
            ll.setBackground(getResources().getDrawable(
                    R.drawable.bg_urgency_inactive));
            // Đổi màu text về gray
            TextView tv = (TextView) ll.getChildAt(1);
            if (tv != null) {
                tv.setTextColor(getResources().getColor(
                        R.color.colorTextGray));
                tv.setTypeface(null,
                        android.graphics.Typeface.NORMAL);
            }
            // Đổi màu icon về gray
            ImageView iv = (ImageView) ll.getChildAt(0);
            if (iv != null) {
                iv.setColorFilter(getResources().getColor(
                        R.color.colorTextGray));
            }
        }

        // Set active
        active.setBackground(getResources().getDrawable(
                R.drawable.bg_urgency_active));
        TextView tvActive = (TextView) active.getChildAt(1);
        if (tvActive != null) {
            tvActive.setTextColor(getResources().getColor(
                    R.color.colorPrimary));
            tvActive.setTypeface(null,
                    android.graphics.Typeface.BOLD);
        }
        ImageView ivActive = (ImageView) active.getChildAt(0);
        if (ivActive != null) {
            ivActive.setColorFilter(getResources().getColor(
                    R.color.colorPrimary));
        }
    }

    // Mở camera
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION);
            return;
        }

        File photoFile = createImageFile();
        if (photoFile == null) return;

        currentPhotoUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                photoFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File storageDir = getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES);
            return File.createTempFile(
                    "REQ_" + timeStamp, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {

            Uri imageUri;

            if (data != null && data.getData() != null) {
                //  chọn từ thư viện
                imageUri = data.getData();
            } else {
                //  chụp camera
                imageUri = currentPhotoUri;
            }

            imageBase64 = uriToBase64(imageUri);

            ivReferencePhoto.setImageURI(imageUri);
            ivReferencePhoto.setVisibility(View.VISIBLE);
            llPhotoPlaceholder.setVisibility(View.GONE);
        } {
            // Convert ảnh → Base64
            imageBase64 = uriToBase64(currentPhotoUri);

            // Hiện preview
            ivReferencePhoto.setImageURI(currentPhotoUri);
            ivReferencePhoto.setVisibility(View.VISIBLE);
            llPhotoPlaceholder.setVisibility(View.GONE);
        }
    }

    // Convert URI → Base64 (nén nhỏ)
    private String uriToBase64(Uri uri) {
        try {
            InputStream is = getContentResolver()
                    .openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(is);
            Bitmap resized   = resizeBitmap(original, 600);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= maxSize && h <= maxSize) return bitmap;
        float ratio = (w > h)
                ? (float) maxSize / w
                : (float) maxSize / h;
        return Bitmap.createScaledBitmap(
                bitmap,
                Math.round(w * ratio),
                Math.round(h * ratio),
                true);
    }

    // Đăng request lên Firebase
    private void postRequest() {
        String title = etItemTitle.getText().toString().trim();
        String desc  = etDescription.getText().toString().trim();

        // Validate
        if (TextUtils.isEmpty(title)) {
            showError("Please enter item title");
            etItemTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            showError("Please describe what you need");
            etDescription.requestFocus();
            return;
        }

        // Disable nút
        btnPostRequest.setEnabled(false);
        btnPostRequest.setText("Posting...");
        tvError.setVisibility(View.GONE);

        String uid   = mAuth.getCurrentUser().getUid();
        String email = mAuth.getCurrentUser().getEmail();

        // Tạo Request object
        Request request = new Request(
                uid, email, title, desc, selectedUrgency);

        // Tạo key tự động
        String requestId = mDatabase.child("requests")
                .push().getKey();
        request.setRequestId(requestId);
        request.setImageUrl(imageBase64);

        // Lưu lên Firebase
        mDatabase.child("requests").child(requestId)
                .setValue(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Request posted! 🙌",
                                Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại màn trước
                    } else {
                        showError("Failed to post. Try again.");
                        btnPostRequest.setEnabled(true);
                        btnPostRequest.setText("Post Request");
                    }
                });
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }
}
