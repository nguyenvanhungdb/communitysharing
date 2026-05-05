//package com.example.communitysharing.activities;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.util.Base64;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.communitysharing.R;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//
//import org.osmdroid.config.Configuration;
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
//import org.osmdroid.util.GeoPoint;
//import org.osmdroid.views.MapView;
//import org.osmdroid.views.overlay.Marker;
//import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
//import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
//
//public class MapActivity extends AppCompatActivity {
//
//    private static final int REQUEST_LOCATION = 301;
//
//    public static final String EXTRA_ITEM_LAT     = "itemLat";
//    public static final String EXTRA_ITEM_LNG     = "itemLng";
//    public static final String EXTRA_ITEM_TITLE   = "itemTitle";
//    public static final String EXTRA_ITEM_ADDRESS = "itemAddress";
//    public static final String EXTRA_ITEM_OWNER   = "ownerName";
//    public static final String EXTRA_ITEM_IMAGE   = "imageBase64";
//
//    private MapView mapView;
//    private MyLocationNewOverlay myLocationOverlay;
//    private FusedLocationProviderClient fusedClient;
//
//    private ImageView ivBack, ivMyLocation, ivItemImage;
//    private TextView tvItemTitle, tvOwnerName, tvItemAddress, tvDistance;
//    private Button btnOpenMap;
//
//    private double itemLat, itemLng;
//    private String itemTitle, itemAddress, ownerName, imageBase64;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Khởi tạo OSMDroid
//        Configuration.getInstance().load(this,
//                PreferenceManager.getDefaultSharedPreferences(this));
//        Configuration.getInstance().setUserAgentValue(
//                "CommunitySharing/1.0 (Android; contact@gmail.com)");
//
//        setContentView(R.layout.activity_map);
//
//        // Lấy data từ Intent
//        itemLat     = getIntent().getDoubleExtra(EXTRA_ITEM_LAT, 0);
//        itemLng     = getIntent().getDoubleExtra(EXTRA_ITEM_LNG, 0);
//        itemTitle   = getIntent().getStringExtra(EXTRA_ITEM_TITLE);
//        itemAddress = getIntent().getStringExtra(EXTRA_ITEM_ADDRESS);
//        ownerName   = getIntent().getStringExtra(EXTRA_ITEM_OWNER);
//        imageBase64 = getIntent().getStringExtra(EXTRA_ITEM_IMAGE);
//
//        fusedClient = LocationServices.getFusedLocationProviderClient(this);
//
//        // Ánh xạ views
//        mapView       = findViewById(R.id.mapView);
//        ivBack        = findViewById(R.id.ivBack);
//        ivMyLocation  = findViewById(R.id.ivMyLocation);
//        ivItemImage   = findViewById(R.id.ivItemImage);
//        tvItemTitle   = findViewById(R.id.tvItemTitle);
//        tvOwnerName   = findViewById(R.id.tvOwnerName);
//        tvItemAddress = findViewById(R.id.tvItemAddress);
//        tvDistance    = findViewById(R.id.tvDistance);
//        btnOpenMap    = findViewById(R.id.btnOpenMap);
//
//        // Hiện thông tin
//        tvItemTitle.setText(itemTitle != null ? itemTitle : "");
//        tvOwnerName.setText(ownerName != null ? ownerName : "");
//        tvItemAddress.setText(itemAddress != null ? itemAddress : "");
//
//        // Hiện ảnh Base64
//        if (imageBase64 != null && !imageBase64.isEmpty()) {
//            try {
//                byte[] bytes = Base64.decode(imageBase64, Base64.DEFAULT);
//                Bitmap bitmap = BitmapFactory.decodeByteArray(
//                        bytes, 0, bytes.length);
//                ivItemImage.setImageBitmap(bitmap);
//            } catch (Exception e) {
//                ivItemImage.setImageResource(
//                        android.R.drawable.ic_menu_gallery);
//            }
//        }
//
//        // Setup map
//        setupMap();
//
//        // Xin permission
//        checkPermissionAndShowMyLocation();
//
//        // Back
//        ivBack.setOnClickListener(v -> finish());
//
//        // My Location button
//        ivMyLocation.setOnClickListener(v -> {
//            if (myLocationOverlay != null
//                    && myLocationOverlay.getMyLocation() != null) {
//                mapView.getController().animateTo(
//                        myLocationOverlay.getMyLocation());
//                mapView.getController().setZoom(16.0);
//            } else {
//                Toast.makeText(this,
//                        "Getting your location...",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Mở Maps app
//        btnOpenMap.setOnClickListener(v -> openInMapsApp());
//    }
//
//    private void setupMap() {
//        org.osmdroid.tileprovider.tilesource.XYTileSource tileSource =
//                new org.osmdroid.tileprovider.tilesource.XYTileSource(
//                        "CartoDB",           // Tên tile source
//                        0,                   // Zoom min
//                        19,                  // Zoom max
//                        256,                 // Tile size
//                        ".png",              // Extension
//                        new String[]{
//                                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
//                                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
//                                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
//                        },
//                        "© CartoDB © OpenStreetMap contributors"
//                );
//
//        mapView.setTileSource(tileSource);
//        mapView.setMultiTouchControls(true);
//        mapView.getController().setZoom(15.0);
//
//        // Marker vị trí item (màu đỏ mặc định)
//        GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);
//
//        Marker itemMarker = new Marker(mapView);
//        itemMarker.setPosition(itemPoint);
//        itemMarker.setTitle(itemTitle != null ? itemTitle : "Item");
//        itemMarker.setSnippet(itemAddress != null ? itemAddress : "");
//        itemMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        // Dùng icon mặc định của OSMDroid
//        itemMarker.setIcon(ContextCompat.getDrawable(this,
//                android.R.drawable.ic_menu_mapmode));
//        mapView.getOverlays().add(itemMarker);
//
//        // Zoom về vị trí item
//        mapView.getController().setCenter(itemPoint);
//    }
//
//    private void checkPermissionAndShowMyLocation() {
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            showMyLocationAndDistance();
//        } else {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_LOCATION);
//        }
//    }
//
//    private void showMyLocationAndDistance() {
//        // Overlay hiện vị trí mình
//        myLocationOverlay = new MyLocationNewOverlay(
//                new GpsMyLocationProvider(this), mapView);
//        myLocationOverlay.enableMyLocation();
//        myLocationOverlay.enableFollowLocation();
//        mapView.getOverlays().add(myLocationOverlay);
//
//        // Lấy vị trí hiện tại → tính khoảng cách
//        fusedClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location == null) {
//                tvDistance.setText("Location unavailable");
//                return;
//            }
//
//            // Tính khoảng cách từ mình đến item
//            float[] results = new float[1];
//            Location.distanceBetween(
//                    location.getLatitude(), location.getLongitude(),
//                    itemLat, itemLng, results);
//            float distanceKm = results[0] / 1000f;
//
//            if (distanceKm < 1) {
//                // Dưới 1km hiện bằng mét
//                tvDistance.setText(
//                        String.format("%.0f m from you",
//                                results[0]));
//            } else {
//                tvDistance.setText(
//                        String.format("%.1f km from you", distanceKm));
//            }
//
//            // Zoom để thấy cả 2 điểm
//            GeoPoint myPoint = new GeoPoint(
//                    location.getLatitude(), location.getLongitude());
//            GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);
//
//            // Tính trung điểm để center camera
//            double midLat = (myPoint.getLatitude()
//                    + itemPoint.getLatitude()) / 2;
//            double midLng = (myPoint.getLongitude()
//                    + itemPoint.getLongitude()) / 2;
//
//            mapView.getController().animateTo(
//                    new GeoPoint(midLat, midLng));
//
//            // Tự động zoom phù hợp
//            double latDiff = Math.abs(myPoint.getLatitude()
//                    - itemPoint.getLatitude());
//            double lngDiff = Math.abs(myPoint.getLongitude()
//                    - itemPoint.getLongitude());
//            double maxDiff = Math.max(latDiff, lngDiff);
//
//            double zoom;
//            if (maxDiff < 0.01)      zoom = 15;
//            else if (maxDiff < 0.05) zoom = 13;
//            else if (maxDiff < 0.1)  zoom = 12;
//            else if (maxDiff < 0.5)  zoom = 10;
//            else                     zoom = 8;
//
//            mapView.getController().setZoom(zoom);
//        });
//    }
//
//    // Mở Maps app với tọa độ item
//    private void openInMapsApp() {
//        // Thử mở Google Maps trước
//        Uri gmmUri = Uri.parse(
//                "geo:" + itemLat + "," + itemLng
//                        + "?q=" + itemLat + "," + itemLng
//                        + "(" + Uri.encode(itemTitle != null
//                        ? itemTitle : "Item") + ")");
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
//
//        if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//        } else {
//            // Không có app Maps → mở OpenStreetMap trên browser
//            Uri browserUri = Uri.parse(
//                    "https://www.openstreetmap.org/?mlat="
//                            + itemLat + "&mlon=" + itemLng
//                            + "#map=16/" + itemLat + "/" + itemLng);
//            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode,
//                permissions, grantResults);
//        if (requestCode == REQUEST_LOCATION
//                && grantResults.length > 0
//                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            showMyLocationAndDistance();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mapView.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//}


package com.example.communitysharing.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.communitysharing.R;
import com.example.communitysharing.utils.LocaleManager;
import com.example.communitysharing.models.Item;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 301;

    // Extras từ ItemDetail (chế độ xem 1 item)
    public static final String EXTRA_ITEM_LAT     = "itemLat";
    public static final String EXTRA_ITEM_LNG     = "itemLng";
    public static final String EXTRA_ITEM_TITLE   = "itemTitle";
    public static final String EXTRA_ITEM_ADDRESS = "itemAddress";
    public static final String EXTRA_ITEM_OWNER   = "ownerName";
    public static final String EXTRA_ITEM_IMAGE   = "imageBase64";
    public static final String EXTRA_ITEM_ID      = "itemId";

    // Extra để phân biệt chế độ
    // true = xem tất cả items, false = xem 1 item
    public static final String EXTRA_SHOW_ALL = "showAll";

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedClient;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Views
    private ImageView ivBack, ivMyLocation;
    private TextView tvMapTitle;
    private LinearLayout llItemCard;
    private ImageView ivItemImage;
    private TextView tvItemTitle, tvOwnerName, tvItemAddress,
            tvDistance, tvItemStatus;
    private Button btnViewDetail, btnOpenMap;

    // Data item đang được chọn
    private Item selectedItem = null;
    private double myLat = 0, myLng = 0;

    // Chế độ
    private boolean showAll = false;

    // Data từ intent (chế độ 1 item)
    private double itemLat, itemLng;
    private String itemTitle, itemAddress, ownerName,
            imageBase64, itemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleManager.applySavedLocale(this);

        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(
                "CommunitySharing/1.0 (Android; contact@gmail.com)");

        setContentView(R.layout.activity_map);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth     = FirebaseAuth.getInstance();
        fusedClient = LocationServices
                .getFusedLocationProviderClient(this);

        // Lấy chế độ
        showAll = getIntent().getBooleanExtra(EXTRA_SHOW_ALL, false);

        // Ánh xạ views
        mapView       = findViewById(R.id.mapView);
        ivBack        = findViewById(R.id.ivBack);
        ivMyLocation  = findViewById(R.id.ivMyLocation);
        tvMapTitle    = findViewById(R.id.tvMapTitle);
        llItemCard    = findViewById(R.id.llItemCard);
        ivItemImage   = findViewById(R.id.ivItemImage);
        tvItemTitle   = findViewById(R.id.tvItemTitle);
        tvOwnerName   = findViewById(R.id.tvOwnerName);
        tvItemAddress = findViewById(R.id.tvItemAddress);
        tvDistance    = findViewById(R.id.tvDistance);
        tvItemStatus  = findViewById(R.id.tvItemStatus);
        btnViewDetail = findViewById(R.id.btnViewDetail);
        btnOpenMap    = findViewById(R.id.btnOpenMap);

        // Setup map tile
        setupMapTile();

        // Back
        ivBack.setOnClickListener(v -> finish());

        // My Location
        ivMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null
                    && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(
                        myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(15.0);
            } else {
                Toast.makeText(this, getString(R.string.common_getting_location),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Xin permission rồi setup theo chế độ
        checkPermission();
    }

    private void setupMapTile() {
        org.osmdroid.tileprovider.tilesource.XYTileSource tileSource =
                new org.osmdroid.tileprovider.tilesource.XYTileSource(
                        "CartoDB", 0, 19, 256, ".png",
                        new String[]{
                                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
                        },
                        "© CartoDB © OpenStreetMap contributors"
                );
        mapView.setTileSource(tileSource);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(14.0);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupAfterPermission();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    private void setupAfterPermission() {
        // Bật overlay vị trí mình
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Lấy vị trí mình
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                myLat = location.getLatitude();
                myLng = location.getLongitude();
            }

            if (showAll) {
                // Chế độ xem tất cả items
                setupShowAllMode(location);
            } else {
                // Chế độ xem 1 item
                setupSingleItemMode(location);
            }
        });
    }

    // ===== CHẾ ĐỘ XEM TẤT CẢ ITEMS =====
    private void setupShowAllMode(Location myLocation) {
        tvMapTitle.setText(getString(R.string.map_nearby_items));
        llItemCard.setVisibility(View.GONE);

        // Center về vị trí mình hoặc Hà Nội
        if (myLocation != null) {
            mapView.getController().setCenter(
                    new GeoPoint(myLocation.getLatitude(),
                            myLocation.getLongitude()));
        } else {
            mapView.getController().setCenter(
                    new GeoPoint(21.0285, 105.8542));
        }

        // Load tất cả items từ Firebase
        String myUid = mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getUid() : "";

        mDatabase.child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Xóa markers cũ (giữ lại overlay vị trí mình)
                        mapView.getOverlays().clear();
                        mapView.getOverlays().add(myLocationOverlay);

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            // Đọc thủ công
                            String oid    = snap.child("ownerId")
                                    .getValue(String.class);
                            String title  = snap.child("title")
                                    .getValue(String.class);
                            String addr   = snap.child("address")
                                    .getValue(String.class);
                            String status = snap.child("status")
                                    .getValue(String.class);
                            String owner  = snap.child("ownerName")
                                    .getValue(String.class);
                            String imgUrl = snap.child("imageUrl")
                                    .getValue(String.class);
                            String iid    = snap.getKey();
                            Double lat    = snap.child("latitude")
                                    .getValue(Double.class);
                            Double lng    = snap.child("longitude")
                                    .getValue(Double.class);

                            if (title == null) continue;
                            // Bỏ qua item không có vị trí
                            if (lat == null || lng == null
                                    || (lat == 0 && lng == 0)) continue;
                            // Chỉ hiện available
                            if (!"available".equals(status)) continue;

                            // Tạo Item object
                            Item item = new Item();
                            item.setItemId(iid);
                            item.setTitle(title);
                            item.setAddress(addr != null ? addr : "");
                            item.setStatus(status);
                            item.setOwnerName(owner != null ? owner : "");
                            item.setImageUrl(imgUrl != null ? imgUrl : "");
                            item.setLatitude(lat);
                            item.setLongitude(lng);
                            item.setOwnerId(oid != null ? oid : "");

                            // Thêm marker
                            addItemMarker(item, myLocation);
                        }

                        mapView.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // Thêm marker cho từng item
//    private void addItemMarker(Item item, Location myLocation) {
//        GeoPoint point = new GeoPoint(
//                item.getLatitude(), item.getLongitude());
//
//        Marker marker = new Marker(mapView);
//        marker.setPosition(point);
//        marker.setTitle(item.getTitle());
//        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        marker.setIcon(ContextCompat.getDrawable(this,
//                android.R.drawable.ic_menu_mapmode));
//
//        // Click marker → hiện card item
//        marker.setOnMarkerClickListener((m, map) -> {
//            selectedItem = item;
//            showItemCard(item, myLocation);
//            return true;
//        });
//
//        mapView.getOverlays().add(marker);
//    }

    private void addItemMarker(Item item, Location myLocation) {

        // Log xem marker có được tạo không
        android.util.Log.d("MapActivity",
                "Adding marker: " + item.getTitle()
                        + " at " + item.getLatitude() + "," + item.getLongitude());

        GeoPoint point = new GeoPoint(
                item.getLatitude(), item.getLongitude());

        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(item.getTitle());
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Nếu có ảnh → decode Base64 làm icon marker
        String imageUrl = item.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    // Thử NO_WRAP trước
                    byte[] bytes = Base64.decode(imageUrl.trim(), Base64.NO_WRAP);
                    Bitmap original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // Nếu null thử DEFAULT
                    if (original == null) {
                        bytes = Base64.decode(imageUrl.trim(), Base64.DEFAULT);
                        original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }

                    if (original != null) {
                        Bitmap markerBitmap = createCircularMarkerBitmap(original);
                        runOnUiThread(() -> {
                            marker.setIcon(new android.graphics.drawable
                                    .BitmapDrawable(getResources(), markerBitmap));
                            mapView.invalidate();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            // Không có ảnh → dùng icon mặc định
            marker.setIcon(ContextCompat.getDrawable(this,
                    android.R.drawable.ic_menu_mapmode));
        }

        // Click marker → hiện card
        marker.setOnMarkerClickListener((m, map) -> {
            selectedItem = item;
            showItemCard(item, myLocation);
            return true;
        });

        mapView.getOverlays().add(marker);
    }

    // Tạo bitmap hình tròn có viền để làm icon marker
    private Bitmap createCircularMarkerBitmap(Bitmap source) {
        int size = 120; // px - kích thước marker

        // Scale ảnh gốc về size
        Bitmap scaled = Bitmap.createScaledBitmap(source, size, size, true);

        // Tạo bitmap mới với kích thước lớn hơn để có viền + đuôi
        int totalHeight = size + 30; // 30px cho đuôi tam giác
        Bitmap result = Bitmap.createBitmap(size, totalHeight,
                Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas =
                new android.graphics.Canvas(result);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);

        // Vẽ hình tròn trắng làm nền (viền)
        paint.setColor(android.graphics.Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Clip hình tròn cho ảnh
        android.graphics.Path circlePath = new android.graphics.Path();
        circlePath.addCircle(size / 2f, size / 2f,
                (size / 2f) - 6, // 6px viền trắng
                android.graphics.Path.Direction.CW);
        canvas.clipPath(circlePath);

        // Vẽ ảnh vào trong hình tròn
        canvas.drawBitmap(scaled, 0, 0, paint);

        return result;
    }

    // Hiện card item khi click marker
    private void showItemCard(Item item, Location myLocation) {
        llItemCard.setVisibility(View.VISIBLE);

        tvItemTitle.setText(item.getTitle());
        tvOwnerName.setText(item.getOwnerName());
        tvItemAddress.setText(item.getAddress());
        tvItemStatus.setText(getString(R.string.common_available_now));

        // Tính khoảng cách
        if (myLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    item.getLatitude(), item.getLongitude(), results);
            float dist = results[0];
            if (dist < 1000) {
                tvDistance.setText(String.format("%.0f m away", dist));
            } else {
                tvDistance.setText(
                        String.format("%.1f km away", dist / 1000f));
            }
        } else {
            tvDistance.setText(getString(R.string.common_distance_unknown));
        }

        // Load ảnh
        // Thay đoạn load ảnh trong showItemCard()
        String imageUrl = item.getImageUrl();

        android.util.Log.d("MapImg",
                "imageUrl null=" + (imageUrl == null)
                        + " | empty=" + (imageUrl != null && imageUrl.isEmpty())
                        + " | length=" + (imageUrl != null ? imageUrl.length() : 0));

        ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            new Thread(() -> {
                try {
                    // Thử NO_WRAP trước
                    byte[] bytes = Base64.decode(imageUrl.trim(), Base64.NO_WRAP);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // Nếu null thử DEFAULT
                    if (bitmap == null) {
                        bytes = Base64.decode(imageUrl.trim(), Base64.DEFAULT);
                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }

                    final Bitmap finalBitmap = bitmap;
                    runOnUiThread(() -> {
                        if (finalBitmap != null) {
                            ivItemImage.setColorFilter(null);
                            ivItemImage.setImageBitmap(finalBitmap);
                            ivItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
//        String imageUrl = item.getImageUrl();
//        ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery);
//        if (imageUrl != null && !imageUrl.isEmpty()) {
//            new Thread(() -> {
//                try {
//                    byte[] bytes = Base64.decode(
//                            imageUrl.trim(), Base64.NO_WRAP);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(
//                            bytes, 0, bytes.length);
//                    if (bitmap != null) {
//                        runOnUiThread(() -> {
//                            ivItemImage.setImageBitmap(bitmap);
//                            ivItemImage.setScaleType(
//                                    ImageView.ScaleType.CENTER_CROP);
//                        });
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }

        // Nút View Detail → ItemDetailActivity
        btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    ItemDetailActivity.class);
            intent.putExtra("itemId", item.getItemId());
            startActivity(intent);
        });

        // Nút Open in Maps
        btnOpenMap.setOnClickListener(v ->
                openInMapsApp(item.getLatitude(), item.getLongitude(),
                        item.getTitle()));
    }

    // ===== CHẾ ĐỘ XEM 1 ITEM =====
//    private void setupSingleItemMode(Location myLocation) {
//        itemLat     = getIntent().getDoubleExtra(EXTRA_ITEM_LAT, 0);
//        itemLng     = getIntent().getDoubleExtra(EXTRA_ITEM_LNG, 0);
//        itemTitle   = getIntent().getStringExtra(EXTRA_ITEM_TITLE);
//        itemAddress = getIntent().getStringExtra(EXTRA_ITEM_ADDRESS);
//        ownerName   = getIntent().getStringExtra(EXTRA_ITEM_OWNER);
//        imageBase64 = getIntent().getStringExtra(EXTRA_ITEM_IMAGE);
//        itemId      = getIntent().getStringExtra(EXTRA_ITEM_ID);
//
//        tvMapTitle.setText("Item Location");
//
//        // Hiện card item
//        llItemCard.setVisibility(View.VISIBLE);
//        tvItemTitle.setText(itemTitle != null ? itemTitle : "");
//        tvOwnerName.setText(ownerName != null ? ownerName : "");
//        tvItemAddress.setText(itemAddress != null ? itemAddress : "");
//        tvItemStatus.setText("AVAILABLE NOW");
//
//        // Marker item
//        GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);
//        Marker marker = new Marker(mapView);
//        marker.setPosition(itemPoint);
//        marker.setTitle(itemTitle != null ? itemTitle : "Item");
//        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//        marker.setIcon(ContextCompat.getDrawable(this,
//                android.R.drawable.ic_menu_mapmode));
//        mapView.getOverlays().add(marker);
//
//        // Load ảnh
//        if (imageBase64 != null && !imageBase64.isEmpty()) {
//            new Thread(() -> {
//                try {
//                    byte[] bytes = Base64.decode(imageBase64.trim(), Base64.NO_WRAP);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//                    if (bitmap == null) {
//                        bytes = Base64.decode(imageBase64.trim(), Base64.DEFAULT);
//                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                    }
//
//                    final Bitmap finalBitmap = bitmap;
//                    runOnUiThread(() -> {
//                        if (finalBitmap != null) {
//                            ivItemImage.setColorFilter(null);
//                            ivItemImage.setImageBitmap(finalBitmap);
//                            ivItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//
//        // Tính khoảng cách
//        if (myLocation != null) {
//            float[] results = new float[1];
//            Location.distanceBetween(
//                    myLocation.getLatitude(), myLocation.getLongitude(),
//                    itemLat, itemLng, results);
//            float dist = results[0];
//            if (dist < 1000) {
//                tvDistance.setText(String.format("%.0f m away", dist));
//            } else {
//                tvDistance.setText(
//                        String.format("%.1f km away", dist / 1000f));
//            }
//        }
//
//        // Center map
//        if (myLocation != null) {
//            double midLat = (myLocation.getLatitude() + itemLat) / 2;
//            double midLng = (myLocation.getLongitude() + itemLng) / 2;
//            mapView.getController().setCenter(
//                    new GeoPoint(midLat, midLng));
//        } else {
//            mapView.getController().setCenter(itemPoint);
//        }
//
//        // Nút View Detail
//        btnViewDetail.setOnClickListener(v -> {
//            if (itemId != null) {
//                Intent intent = new Intent(this,
//                        ItemDetailActivity.class);
//                intent.putExtra("itemId", itemId);
//                startActivity(intent);
//            }
//        });
//
//        // Nút Open in Maps
//        btnOpenMap.setOnClickListener(v ->
//                openInMapsApp(itemLat, itemLng, itemTitle));
//    }
    private void setupSingleItemMode(Location myLocation) {
        itemLat     = getIntent().getDoubleExtra(EXTRA_ITEM_LAT, 0);
        itemLng     = getIntent().getDoubleExtra(EXTRA_ITEM_LNG, 0);
        itemTitle   = getIntent().getStringExtra(EXTRA_ITEM_TITLE);
        itemAddress = getIntent().getStringExtra(EXTRA_ITEM_ADDRESS);
        ownerName   = getIntent().getStringExtra(EXTRA_ITEM_OWNER);
        imageBase64 = getIntent().getStringExtra(EXTRA_ITEM_IMAGE);
        itemId      = getIntent().getStringExtra(EXTRA_ITEM_ID);

        tvMapTitle.setText(getString(R.string.map_item_location));

        // Hiện card
        llItemCard.setVisibility(View.VISIBLE);
        tvItemTitle.setText(itemTitle != null ? itemTitle : "");
        tvOwnerName.setText(ownerName != null ? ownerName : "");
        tvItemAddress.setText(itemAddress != null ? itemAddress : "");
        tvItemStatus.setText(getString(R.string.common_available_now));

        // Tạo marker
        GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);
        Marker marker = new Marker(mapView);
        marker.setPosition(itemPoint);
        marker.setTitle(itemTitle != null ? itemTitle : getString(R.string.common_item));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set icon mặc định trước
        marker.setIcon(ContextCompat.getDrawable(this,
                android.R.drawable.ic_menu_mapmode));

        mapView.getOverlays().add(marker);

        // ===== Load ảnh cho MARKER =====
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            new Thread(() -> {
                try {
                    byte[] bytes = Base64.decode(
                            imageBase64.trim(), Base64.NO_WRAP);
                    Bitmap original = BitmapFactory.decodeByteArray(
                            bytes, 0, bytes.length);

                    if (original == null) {
                        bytes = Base64.decode(
                                imageBase64.trim(), Base64.DEFAULT);
                        original = BitmapFactory.decodeByteArray(
                                bytes, 0, bytes.length);
                    }

                    if (original != null) {
                        Bitmap markerBitmap =
                                createCircularMarkerBitmap(original);
                        runOnUiThread(() -> {
                            marker.setIcon(
                                    new android.graphics.drawable.BitmapDrawable(
                                            getResources(), markerBitmap));
                            mapView.invalidate();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // ===== Load ảnh cho CARD =====
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            new Thread(() -> {
                try {
                    byte[] bytes = Base64.decode(
                            imageBase64.trim(), Base64.NO_WRAP);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(
                            bytes, 0, bytes.length);

                    if (bitmap == null) {
                        bytes = Base64.decode(
                                imageBase64.trim(), Base64.DEFAULT);
                        bitmap = BitmapFactory.decodeByteArray(
                                bytes, 0, bytes.length);
                    }

                    final Bitmap finalBitmap = bitmap;
                    runOnUiThread(() -> {
                        if (finalBitmap != null) {
                            ivItemImage.setColorFilter(null);
                            ivItemImage.setImageBitmap(finalBitmap);
                            ivItemImage.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP);
                            ivItemImage.setPadding(0, 0, 0, 0);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Tính khoảng cách
        if (myLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    itemLat, itemLng, results);
            float dist = results[0];
            if (dist < 1000) {
                tvDistance.setText(String.format("%.0f m away", dist));
            } else {
                tvDistance.setText(
                        String.format("%.1f km away", dist / 1000f));
            }
        }

        // Center map
        if (myLocation != null) {
            double midLat = (myLocation.getLatitude() + itemLat) / 2;
            double midLng = (myLocation.getLongitude() + itemLng) / 2;
            mapView.getController().setCenter(
                    new GeoPoint(midLat, midLng));
        } else {
            mapView.getController().setCenter(itemPoint);
        }

        // Nút View Detail
        btnViewDetail.setOnClickListener(v -> {
            if (itemId != null) {
                Intent intent = new Intent(this, ItemDetailActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
            }
        });

        // Nút Open in Maps
        btnOpenMap.setOnClickListener(v ->
                openInMapsApp(itemLat, itemLng, itemTitle));
    }

    private void openInMapsApp(double lat, double lng, String title) {
        Uri gmmUri = Uri.parse("geo:" + lat + "," + lng
                + "?q=" + lat + "," + lng
                + "(" + Uri.encode(title != null ? title : getString(R.string.common_item)) + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse(
                    "https://www.openstreetmap.org/?mlat=" + lat
                            + "&mlon=" + lng + "#map=16/" + lat + "/" + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupAfterPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
