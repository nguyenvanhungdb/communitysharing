package com.example.communitysharing.fragments;

import android.Manifest;
import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.communitysharing.R;
import com.example.communitysharing.models.Item;
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

public class ShareFragment extends Fragment {

    // Request codes
    private static final int REQUEST_CAMERA_MAIN   = 101;
    private static final int REQUEST_CAMERA_PHOTO2 = 102;
    private static final int REQUEST_CAMERA_PHOTO3 = 103;
    private static final int REQUEST_PERMISSION    = 200;

    // Views
    private FrameLayout flMainPhoto, flPhoto2, flPhoto3;
    private ImageView ivMainPhoto, ivPhoto2, ivPhoto3;
    private LinearLayout llMainPhotoPlaceholder;
    private EditText etTitle, etDescription, etAddress;
    private Spinner spinnerCategory;
    private TextView tvQuantity, btnMinus, btnPlus, tvError;
    private LinearLayout llPickup, llDelivery;
    private Button btnPostItem;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Ảnh dạng Base64
    private String imageBase64Main = "";
    private String imageBase64Two  = "";
    private String imageBase64Three = "";

    // Camera URI tạm
    private Uri currentPhotoUri;
    private int currentPhotoSlot = 0;

    // State
    private int quantity    = 1;
    private String delivery = "pickup";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share, container, false);

        // Khởi tạo Firebase (không cần Storage nữa)
        mAuth     = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ view
        flMainPhoto            = view.findViewById(R.id.flMainPhoto);
        flPhoto2               = view.findViewById(R.id.flPhoto2);
        flPhoto3               = view.findViewById(R.id.flPhoto3);
        ivMainPhoto            = view.findViewById(R.id.ivMainPhoto);
        ivPhoto2               = view.findViewById(R.id.ivPhoto2);
        ivPhoto3               = view.findViewById(R.id.ivPhoto3);
        llMainPhotoPlaceholder = view.findViewById(R.id.llMainPhotoPlaceholder);
        etTitle                = view.findViewById(R.id.etTitle);
        etDescription          = view.findViewById(R.id.etDescription);
        etAddress              = view.findViewById(R.id.etAddress);
        spinnerCategory        = view.findViewById(R.id.spinnerCategory);
        tvQuantity             = view.findViewById(R.id.tvQuantity);
        btnMinus               = view.findViewById(R.id.btnMinus);
        btnPlus                = view.findViewById(R.id.btnPlus);
        llPickup               = view.findViewById(R.id.llPickup);
        llDelivery             = view.findViewById(R.id.llDelivery);
        btnPostItem            = view.findViewById(R.id.btnPostItem);
        tvError                = view.findViewById(R.id.tvError);

        setupSpinner();
        setupClickListeners();

        return view;
    }

    private void setupSpinner() {
        String[] categories = {
                "Furniture", "Food", "Clothes",
                "Tools", "Electronics", "Kitchen",
                "Garden", "Books", "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {

        // Chụp ảnh
        flMainPhoto.setOnClickListener(v -> {
            currentPhotoSlot = 1;
            openCamera();
        });
        flPhoto2.setOnClickListener(v -> {
            currentPhotoSlot = 2;
            openCamera();
        });
        flPhoto3.setOnClickListener(v -> {
            currentPhotoSlot = 3;
            openCamera();
        });

        // Quantity
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });
        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
        });

        // Pickup / Delivery
        llPickup.setOnClickListener(v -> {
            delivery = "pickup";
            llPickup.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_active));
            llDelivery.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_inactive));
        });
        llDelivery.setOnClickListener(v -> {
            delivery = "delivery";
            llDelivery.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_active));
            llPickup.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_inactive));
        });

        // Post
        btnPostItem.setOnClickListener(v -> postItem());
    }

    // ===== CAMERA =====

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(),
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
                getContext(),
                getContext().getPackageName() + ".fileprovider",
                photoFile
        );

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        int requestCode;
        if      (currentPhotoSlot == 1) requestCode = REQUEST_CAMERA_MAIN;
        else if (currentPhotoSlot == 2) requestCode = REQUEST_CAMERA_PHOTO2;
        else                            requestCode = REQUEST_CAMERA_PHOTO3;

        startActivityForResult(intent, requestCode);
    }

    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getContext()
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("PHOTO_" + timeStamp, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        // Chuyển ảnh từ URI → Bitmap → nén → Base64
        String base64 = uriToBase64(currentPhotoUri);

        if (requestCode == REQUEST_CAMERA_MAIN) {
            imageBase64Main = base64;
            // Hiển thị preview
            ivMainPhoto.setImageURI(currentPhotoUri);
            ivMainPhoto.setVisibility(View.VISIBLE);
            llMainPhotoPlaceholder.setVisibility(View.GONE);

        } else if (requestCode == REQUEST_CAMERA_PHOTO2) {
            imageBase64Two = base64;
            ivPhoto2.setImageURI(currentPhotoUri);
            ivPhoto2.setVisibility(View.VISIBLE);
            flPhoto2.findViewById(R.id.ivAddPhoto2).setVisibility(View.GONE);

        } else if (requestCode == REQUEST_CAMERA_PHOTO3) {
            imageBase64Three = base64;
            ivPhoto3.setImageURI(currentPhotoUri);
            ivPhoto3.setVisibility(View.VISIBLE);
            flPhoto3.findViewById(R.id.ivAddPhoto3).setVisibility(View.GONE);
        }
    }

    // ===== CONVERT ẢNH SANG BASE64 =====
    // Nén ảnh xuống còn ~200KB để không vượt giới hạn Firebase (10MB/node)

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContext()
                    .getContentResolver().openInputStream(uri);

            // Decode ảnh gốc
            Bitmap original = BitmapFactory.decodeStream(inputStream);

            // Nén ảnh xuống max 800x800 để tiết kiệm dung lượng
            Bitmap resized = resizeBitmap(original, 800);

            // Nén thành JPEG chất lượng 60%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 60, baos);

            // Chuyển sang Base64
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Thu nhỏ ảnh giữ tỉ lệ
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) return bitmap;

        float ratio;
        if (width > height) {
            ratio = (float) maxSize / width;
        } else {
            ratio = (float) maxSize / height;
        }

        int newWidth  = Math.round(width  * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    // ===== ĐĂNG LÊN FIREBASE =====

    private void postItem() {
        String title       = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String address     = etAddress.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            showError("Please enter item title");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(description)) {
            showError("Please enter a description");
            etDescription.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            showError("Please enter your address");
            etAddress.requestFocus();
            return;
        }

        String uid       = mAuth.getCurrentUser().getUid();
        String ownerName = mAuth.getCurrentUser().getEmail();

        btnPostItem.setEnabled(false);
        btnPostItem.setText("Posting...");
        tvError.setVisibility(View.GONE);

        // Tạo key tự động
        String itemId = mDatabase.child("items").push().getKey();

        // Tạo Item object
        Item item = new Item(uid, ownerName, title,
                description, category, address, "sharing");
        item.setItemId(itemId);
        item.setImageUrl(imageBase64Main); // lưu Base64 vào imageUrl
        item.setQuantity(quantity);

        // Lưu lên Firebase Database
        mDatabase.child("items").child(itemId).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Item posted successfully! 🎉",
                                Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        showError("Failed to post. Please try again.");
                        btnPostItem.setEnabled(true);
                        btnPostItem.setText("Post Item ▷");
                    }
                });
    }

    private void resetForm() {
        etTitle.setText("");
        etDescription.setText("");
        etAddress.setText("");
        tvQuantity.setText("1");
        quantity        = 1;
        imageBase64Main  = "";
        imageBase64Two   = "";
        imageBase64Three = "";

        ivMainPhoto.setVisibility(View.GONE);
        llMainPhotoPlaceholder.setVisibility(View.VISIBLE);
        ivPhoto2.setVisibility(View.GONE);
        ivPhoto3.setVisibility(View.GONE);

        btnPostItem.setEnabled(true);
        btnPostItem.setText("Post Item ▷");
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(getContext(),
                    "Camera permission required",
                    Toast.LENGTH_SHORT).show();
        }
    }
}