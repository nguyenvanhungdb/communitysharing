package com.example.communitysharing.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
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
import com.example.communitysharing.activities.PickLocationActivity;
import com.example.communitysharing.models.Item;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShareFragment extends Fragment {
    private Context mContext;

    private String editItemId = null;
    // Request codes
    private static final int REQUEST_CAMERA_MAIN    = 101;
    private static final int REQUEST_CAMERA_PHOTO2  = 102;
    private static final int REQUEST_CAMERA_PHOTO3  = 103;
    private static final int REQUEST_PERMISSION     = 200;
    private static final int REQUEST_PICK_LOCATION  = 300;
    private static final int REQUEST_LOCATION_PERM  = 400;

    // Views
    private FrameLayout flMainPhoto, flPhoto2, flPhoto3;
    private ImageView ivMainPhoto, ivPhoto2, ivPhoto3;
    private LinearLayout llMainPhotoPlaceholder;
    private EditText etTitle, etDescription, etAddress;
    private Spinner spinnerCategory;
    private TextView tvQuantity, btnMinus, btnPlus, tvError;
    private TextView tvPickedLocation;
    private LinearLayout llPickup, llDelivery;
    private LinearLayout btnUseCurrentLocation, btnPickOnMap;
    private Button btnPostItem;

    // Firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Location
    private FusedLocationProviderClient fusedClient;
    private double pickedLat     = 0;
    private double pickedLng     = 0;
    private String pickedAddress = "";

    // Ảnh Base64
    private String imageBase64Main  = "";
    private String imageBase64Two   = "";
    private String imageBase64Three = "";

    // Camera
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
        View view = inflater.inflate(
                R.layout.fragment_share, container, false);

        mAuth       = FirebaseAuth.getInstance();
        mDatabase   = FirebaseDatabase.getInstance().getReference();
        fusedClient = LocationServices
                .getFusedLocationProviderClient(getContext());

        // Ánh xạ views
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
        tvPickedLocation       = view.findViewById(R.id.tvPickedLocation);
        btnUseCurrentLocation  = view.findViewById(R.id.btnUseCurrentLocation);
        btnPickOnMap           = view.findViewById(R.id.btnPickOnMap);

        setupSpinner();
        checkEditMode();
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
                categories);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {

        // Camera
        flMainPhoto.setOnClickListener(v -> {
            currentPhotoSlot = 1;
            openImagePicker();
        });
        flPhoto2.setOnClickListener(v -> {
            currentPhotoSlot = 2;
            openImagePicker();
        });
        flPhoto3.setOnClickListener(v -> {
            currentPhotoSlot = 3;
            openImagePicker();
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
            llPickup.setBackground(getResources().getDrawable(
                    R.drawable.bg_delivery_active));
            llDelivery.setBackground(getResources().getDrawable(
                    R.drawable.bg_delivery_inactive));
        });
        llDelivery.setOnClickListener(v -> {
            delivery = "delivery";
            llDelivery.setBackground(getResources().getDrawable(
                    R.drawable.bg_delivery_active));
            llPickup.setBackground(getResources().getDrawable(
                    R.drawable.bg_delivery_inactive));
        });

        // ===== LẤY VỊ TRÍ HIỆN TẠI =====
        btnUseCurrentLocation.setOnClickListener(v ->
                getCurrentLocation());

        // ===== CHỌN TRÊN MAP =====
        btnPickOnMap.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(),
                    PickLocationActivity.class);
            startActivityForResult(intent, REQUEST_PICK_LOCATION);
        });

        // Post item
        btnPostItem.setOnClickListener(v -> postItem());
    }

    // ===== LẤY GPS HIỆN TẠI =====
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERM);
            return;
        }

        // Đổi UI nút thành loading
        tvPickedLocation.setText("Getting your location...");
        tvPickedLocation.setTextColor(
                getResources().getColor(R.color.colorTextGray));

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        Toast.makeText(getContext(),
                                "Cannot get location. Try again or pick on map.",
                                Toast.LENGTH_SHORT).show();
                        tvPickedLocation.setText("No location selected yet");
                        return;
                    }

                    pickedLat = location.getLatitude();
                    pickedLng = location.getLongitude();

                    // Reverse geocode → lấy địa chỉ
                    getAddressFromLatLng(pickedLat, pickedLng);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Location error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Reverse geocode: tọa độ → địa chỉ
    private void getAddressFromLatLng(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(
                        getContext(), Locale.getDefault());
                List<Address> addresses =
                        geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (addr.getThoroughfare() != null)
                        sb.append(addr.getThoroughfare()).append(", ");
                    if (addr.getSubLocality() != null)
                        sb.append(addr.getSubLocality()).append(", ");
                    if (addr.getLocality() != null)
                        sb.append(addr.getLocality());

                    pickedAddress = sb.toString().isEmpty()
                            ? lat + ", " + lng
                            : sb.toString();
                } else {
                    // Không có địa chỉ → dùng tọa độ
                    pickedAddress = lat + ", " + lng;
                }

                // Cập nhật UI trên main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            updateLocationUI());
                }

            } catch (IOException e) {
                e.printStackTrace();
                pickedAddress = lat + ", " + lng;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            updateLocationUI());
                }
            }
        }).start();
    }

    // Cập nhật UI sau khi có địa chỉ
    private void updateLocationUI() {
        tvPickedLocation.setText(pickedAddress);
        tvPickedLocation.setTextColor(
                getResources().getColor(R.color.colorTextDark));

        // Tự động điền vào ô address
        etAddress.setText(pickedAddress);

        // Đổi màu nền 2 nút để biết đã chọn
        btnUseCurrentLocation.setBackground(
                getResources().getDrawable(R.drawable.bg_delivery_active));
        btnPickOnMap.setBackground(
                getResources().getDrawable(R.drawable.bg_delivery_inactive));

        Toast.makeText(getContext(),
                "Location set! ✓", Toast.LENGTH_SHORT).show();
    }

    // ===== CAMERA =====
    private void openImagePicker() {
        String[] options = {"Chụp ảnh", "Chọn từ thư viện"};

        new AlertDialog.Builder(getContext())
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
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSION);
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

        int requestCode = (currentPhotoSlot == 1) ? REQUEST_CAMERA_MAIN :
                (currentPhotoSlot == 2) ? REQUEST_CAMERA_PHOTO2 :
                        REQUEST_CAMERA_PHOTO3;

        startActivityForResult(intent, requestCode);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        int requestCode = (currentPhotoSlot == 1) ? REQUEST_CAMERA_MAIN :
                (currentPhotoSlot == 2) ? REQUEST_CAMERA_PHOTO2 :
                        REQUEST_CAMERA_PHOTO3;

        startActivityForResult(intent, requestCode);
    }

    private File createImageFile() {
        try {
            String ts = new SimpleDateFormat(
                    "yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            File dir = getContext()
                    .getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("PHOTO_" + ts, ".jpg", dir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ===== NHẬN KẾT QUẢ TỪ PickLocationActivity =====
        if (requestCode == REQUEST_PICK_LOCATION
                && resultCode == Activity.RESULT_OK
                && data != null) {

            pickedLat     = data.getDoubleExtra(
                    PickLocationActivity.EXTRA_LATITUDE, 0);
            pickedLng     = data.getDoubleExtra(
                    PickLocationActivity.EXTRA_LONGITUDE, 0);
            pickedAddress = data.getStringExtra(
                    PickLocationActivity.EXTRA_ADDRESS);

            if (pickedAddress == null) pickedAddress = "";

            // Cập nhật UI
            tvPickedLocation.setText(pickedAddress.isEmpty()
                    ? pickedLat + ", " + pickedLng
                    : pickedAddress);
            tvPickedLocation.setTextColor(
                    getResources().getColor(R.color.colorTextDark));

            // Điền vào ô address
            etAddress.setText(pickedAddress);

            // Đổi màu nút Pick on Map thành active
            btnPickOnMap.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_active));
            btnUseCurrentLocation.setBackground(
                    getResources().getDrawable(R.drawable.bg_delivery_inactive));

            Toast.makeText(getContext(),
                    "Location selected! ✓", Toast.LENGTH_SHORT).show();
            return;
        }

        // ===== NHẬN KẾT QUẢ TỪ CAMERA =====
        if (resultCode != Activity.RESULT_OK) return;

        Uri selectedImageUri;

        // KIỂM TRA: Nếu data != null và data.getData() != null thì là chọn từ THƯ VIỆN
        if (data != null && data.getData() != null) {
            selectedImageUri = data.getData();
        } else {
            // Nếu không có data thì là vừa CHỤP ẢNH xong
            selectedImageUri = currentPhotoUri;
        }

        // Chuyển sang Base64 bằng URI đúng
        String base64 = uriToBase64(selectedImageUri);

        // Hiển thị lên đúng Slot
        if (requestCode == REQUEST_CAMERA_MAIN) {
            imageBase64Main = base64;
            ivMainPhoto.setImageURI(selectedImageUri); // Dùng selectedImageUri thay vì currentPhotoUri
            ivMainPhoto.setVisibility(View.VISIBLE);
            llMainPhotoPlaceholder.setVisibility(View.GONE);

        } else if (requestCode == REQUEST_CAMERA_PHOTO2) {
            imageBase64Two = base64;
            ivPhoto2.setImageURI(selectedImageUri);
            ivPhoto2.setVisibility(View.VISIBLE);
            flPhoto2.findViewById(R.id.ivAddPhoto2).setVisibility(View.GONE);

        } else if (requestCode == REQUEST_CAMERA_PHOTO3) {
            imageBase64Three = base64;
            ivPhoto3.setImageURI(selectedImageUri);
            ivPhoto3.setVisibility(View.VISIBLE);
            flPhoto3.findViewById(R.id.ivAddPhoto3).setVisibility(View.GONE);
        }
    }

    // Convert URI → Base64
    private String uriToBase64(Uri uri) {
        try {
            InputStream is = getContext()
                    .getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(is);
            Bitmap resized   = resizeBitmap(original, 800);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= maxSize && h <= maxSize) return bitmap;
        float ratio = w > h
                ? (float) maxSize / w
                : (float) maxSize / h;
        return Bitmap.createScaledBitmap(
                bitmap, Math.round(w * ratio),
                Math.round(h * ratio), true);
    }

    // ===== ĐĂNG LÊN FIREBASE =====
//    private void postItem() {
//        String title    = etTitle.getText().toString().trim();
//        String desc     = etDescription.getText().toString().trim();
//        String address  = etAddress.getText().toString().trim();
//        String category = spinnerCategory.getSelectedItem().toString();
//
//        if (TextUtils.isEmpty(title)) {
//            showError("Please enter item title");
//            etTitle.requestFocus();
//            return;
//        }
//        if (TextUtils.isEmpty(desc)) {
//            showError("Please enter a description");
//            etDescription.requestFocus();
//            return;
//        }
//        if (TextUtils.isEmpty(address)) {
//            showError("Please enter your address");
//            etAddress.requestFocus();
//            return;
//        }
//
//        // Cảnh báo nếu chưa chọn vị trí trên map
//        if (pickedLat == 0 && pickedLng == 0) {
//            Toast.makeText(getContext(),
//                    "Tip: Add a location so others can find your item!",
//                    Toast.LENGTH_SHORT).show();
//        }
//
//        btnPostItem.setEnabled(false);
//        btnPostItem.setText("Posting...");
//        tvError.setVisibility(View.GONE);
//
//        String uid       = mAuth.getCurrentUser().getUid();
//        String ownerName = mAuth.getCurrentUser().getEmail();
//
//        String itemId = mDatabase.child("items").push().getKey();
//
//        Item item = new Item(uid, ownerName, title,
//                desc, category, address, "sharing");
//        item.setItemId(itemId);
//        item.setImageUrl(imageBase64Main);
//        item.setQuantity(quantity);
//
//        // ===== LƯU TỌA ĐỘ MAP =====
//        item.setLatitude(pickedLat);
//        item.setLongitude(pickedLng);
//        item.setExactAddress(pickedAddress);
//
//        btnPostItem.setEnabled(false);
//        btnPostItem.setText(editItemId != null ? "Updating..." : "Posting...");
//        tvError.setVisibility(View.GONE);
//
//        if (editItemId != null) {
//            // ===== CHẾ ĐỘ EDIT - cập nhật item cũ =====
//            updateExistingItem(title, desc, category, address);
//        } else {
//            // ===== CHẾ ĐỘ TẠO MỚI =====
//            createNewItem(uid, ownerName, title, desc, category, address);
//        }
//
//        mDatabase.child("items").child(itemId).setValue(item)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(getContext(),
//                                "Item posted successfully! 🎉",
//                                Toast.LENGTH_SHORT).show();
//                        resetForm();
//                    } else {
//                        showError("Failed to post. Please try again.");
//                        btnPostItem.setEnabled(true);
//                        btnPostItem.setText("Post Item ▷");
//                    }
//                });
//    }

    private void postItem() {
        String title    = etTitle.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();
        String address  = etAddress.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            showError("Please enter item title");
            etTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            showError("Please enter a description");
            etDescription.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            showError("Please enter your address");
            etAddress.requestFocus();
            return;
        }

        if (pickedLat == 0 && pickedLng == 0) {
            Toast.makeText(mContext,
                    "Tip: Add a location so others can find your item!",
                    Toast.LENGTH_SHORT).show();
        }

        btnPostItem.setEnabled(false);
        btnPostItem.setText(editItemId != null ? "Updating..." : "Posting...");
        tvError.setVisibility(View.GONE);

        String uid       = mAuth.getCurrentUser().getUid();
        String ownerName = mAuth.getCurrentUser().getEmail();

        // Phân nhánh rõ ràng - KHÔNG có code thừa bên dưới
        if (editItemId != null) {
            updateExistingItem(title, desc, category, address);
        } else {
            createNewItem(uid, ownerName, title, desc, category, address);
        }

        // XÓA TOÀN BỘ ĐOẠN NÀY - đây là nguyên nhân lỗi:
        // mDatabase.child("items").child(itemId).setValue(item)...
    }
    private void resetForm() {
        etTitle.setText("");
        etDescription.setText("");
        etAddress.setText("");
        tvQuantity.setText("1");
        tvPickedLocation.setText("No location selected yet");
        tvPickedLocation.setTextColor(
                getResources().getColor(R.color.colorTextHint));

        quantity      = 1;
        pickedLat     = 0;
        pickedLng     = 0;
        pickedAddress = "";
        imageBase64Main  = "";
        imageBase64Two   = "";
        imageBase64Three = "";

        ivMainPhoto.setVisibility(View.GONE);
        llMainPhotoPlaceholder.setVisibility(View.VISIBLE);
        ivPhoto2.setVisibility(View.GONE);
        ivPhoto3.setVisibility(View.GONE);

        // Reset nút location
        btnUseCurrentLocation.setBackground(
                getResources().getDrawable(R.drawable.bg_delivery_inactive));
        btnPickOnMap.setBackground(
                getResources().getDrawable(R.drawable.bg_delivery_inactive));

        btnPostItem.setEnabled(true);
        btnPostItem.setText("Post Item ▷");
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else if (requestCode == REQUEST_LOCATION_PERM
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(getContext(),
                    "Permission required",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private void checkEditMode() {
        Bundle args = getArguments();
        if (args == null) return; // Không có data = tạo mới bình thường

        editItemId = args.getString("editItemId");
        if (editItemId == null) return;

        // Đang edit → điền data vào form
        String title    = args.getString("editTitle", "");
        String desc     = args.getString("editDesc", "");
        String address  = args.getString("editAddress", "");
        String category = args.getString("editCategory", "");
        String imageUrl = args.getString("editImageUrl", "");
        pickedLat       = args.getDouble("editLat", 0);
        pickedLng       = args.getDouble("editLng", 0);
        pickedAddress   = address;

        // Điền vào các ô input
        etTitle.setText(title);
        etDescription.setText(desc);
        etAddress.setText(address);

        // Set spinner category
        String[] categories = {
                "Furniture", "Food", "Clothes",
                "Tools", "Electronics", "Kitchen",
                "Garden", "Books", "Other"
        };
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(category)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        // Hiện ảnh cũ nếu có
        if (!imageUrl.isEmpty()) {
            imageBase64Main = imageUrl;
            new Thread(() -> {
                try {
                    byte[] bytes = Base64.decode(
                            imageUrl.trim(), Base64.NO_WRAP);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            bytes, 0, bytes.length);
                    if (bitmap != null && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ivMainPhoto.setImageBitmap(bitmap);
                            ivMainPhoto.setColorFilter(null);
                            ivMainPhoto.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP);
                            ivMainPhoto.setVisibility(View.VISIBLE);
                            llMainPhotoPlaceholder.setVisibility(View.GONE);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Hiện địa chỉ đã chọn
        if (!address.isEmpty()) {
            tvPickedLocation.setText(address);
            tvPickedLocation.setTextColor(
                    getResources().getColor(R.color.colorTextDark));
        }

        // Đổi title header và nút
        btnPostItem.setText("Update Item ▷");

        android.util.Log.d("ShareFragment",
                "Edit mode: itemId=" + editItemId);
    }

    private void updateExistingItem(String title, String desc,
                                    String category, String address) {

        mDatabase.child("items").child(editItemId)
                .child("title").setValue(title);
        mDatabase.child("items").child(editItemId)
                .child("description").setValue(desc);
        mDatabase.child("items").child(editItemId)
                .child("category").setValue(category);
        mDatabase.child("items").child(editItemId)
                .child("latitude").setValue(pickedLat);
        mDatabase.child("items").child(editItemId)
                .child("longitude").setValue(pickedLng);

        if (!imageBase64Main.isEmpty()) {
            mDatabase.child("items").child(editItemId)
                    .child("imageUrl").setValue(imageBase64Main);
        }

        // Chỉ lắng nghe 1 lần, dùng address làm field cuối
        mDatabase.child("items").child(editItemId)
                .child("address").setValue(address)
                .addOnCompleteListener(task -> {

                    // Kiểm tra Fragment còn attached không
                    if (!isAdded() || getContext() == null) return;

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Item updated! ✓",
                                Toast.LENGTH_SHORT).show();

                        if (getParentFragmentManager()
                                .getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    } else {
                        showError("Update failed. Try again.");
                        btnPostItem.setEnabled(true);
                        btnPostItem.setText("Update Item ▷");
                    }
                });
    }


    // Tạo item mới (giữ nguyên code cũ)
    private void createNewItem(String uid, String ownerName,
                               String title, String desc,
                               String category, String address) {

        String itemId = mDatabase.child("items").push().getKey();

        Item item = new Item(uid, ownerName, title,
                desc, category, address, "sharing");
        item.setItemId(itemId);
        item.setImageUrl(imageBase64Main);
        item.setQuantity(quantity);
        item.setLatitude(pickedLat);
        item.setLongitude(pickedLng);
        item.setExactAddress(pickedAddress);

        mDatabase.child("items").child(itemId).setValue(item)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Item posted! 🎉",
                                Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        showError("Failed to post. Try again.");
                        btnPostItem.setEnabled(true);
                        btnPostItem.setText("Post Item ▷");
                    }
                });
    }


}