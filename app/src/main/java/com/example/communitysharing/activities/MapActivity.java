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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.communitysharing.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 301;

    public static final String EXTRA_ITEM_LAT     = "itemLat";
    public static final String EXTRA_ITEM_LNG     = "itemLng";
    public static final String EXTRA_ITEM_TITLE   = "itemTitle";
    public static final String EXTRA_ITEM_ADDRESS = "itemAddress";
    public static final String EXTRA_ITEM_OWNER   = "ownerName";
    public static final String EXTRA_ITEM_IMAGE   = "imageBase64";

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedClient;

    private ImageView ivBack, ivMyLocation, ivItemImage;
    private TextView tvItemTitle, tvOwnerName, tvItemAddress, tvDistance;
    private Button btnOpenMap;

    private double itemLat, itemLng;
    private String itemTitle, itemAddress, ownerName, imageBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo OSMDroid
        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(
                getPackageName());

        setContentView(R.layout.activity_map);

        // Lấy data từ Intent
        itemLat     = getIntent().getDoubleExtra(EXTRA_ITEM_LAT, 0);
        itemLng     = getIntent().getDoubleExtra(EXTRA_ITEM_LNG, 0);
        itemTitle   = getIntent().getStringExtra(EXTRA_ITEM_TITLE);
        itemAddress = getIntent().getStringExtra(EXTRA_ITEM_ADDRESS);
        ownerName   = getIntent().getStringExtra(EXTRA_ITEM_OWNER);
        imageBase64 = getIntent().getStringExtra(EXTRA_ITEM_IMAGE);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Ánh xạ views
        mapView       = findViewById(R.id.mapView);
        ivBack        = findViewById(R.id.ivBack);
        ivMyLocation  = findViewById(R.id.ivMyLocation);
        ivItemImage   = findViewById(R.id.ivItemImage);
        tvItemTitle   = findViewById(R.id.tvItemTitle);
        tvOwnerName   = findViewById(R.id.tvOwnerName);
        tvItemAddress = findViewById(R.id.tvItemAddress);
        tvDistance    = findViewById(R.id.tvDistance);
        btnOpenMap    = findViewById(R.id.btnOpenMap);

        // Hiện thông tin
        tvItemTitle.setText(itemTitle != null ? itemTitle : "");
        tvOwnerName.setText(ownerName != null ? ownerName : "");
        tvItemAddress.setText(itemAddress != null ? itemAddress : "");

        // Hiện ảnh Base64
        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] bytes = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        bytes, 0, bytes.length);
                ivItemImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                ivItemImage.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }
        }

        // Setup map
        setupMap();

        // Xin permission
        checkPermissionAndShowMyLocation();

        // Back
        ivBack.setOnClickListener(v -> finish());

        // My Location button
        ivMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null
                    && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(
                        myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(16.0);
            } else {
                Toast.makeText(this,
                        "Getting your location...",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Mở Maps app
        btnOpenMap.setOnClickListener(v -> openInMapsApp());
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Marker vị trí item (màu đỏ mặc định)
        GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);

        Marker itemMarker = new Marker(mapView);
        itemMarker.setPosition(itemPoint);
        itemMarker.setTitle(itemTitle != null ? itemTitle : "Item");
        itemMarker.setSnippet(itemAddress != null ? itemAddress : "");
        itemMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        // Dùng icon mặc định của OSMDroid
        itemMarker.setIcon(ContextCompat.getDrawable(this,
                android.R.drawable.ic_menu_mapmode));
        mapView.getOverlays().add(itemMarker);

        // Zoom về vị trí item
        mapView.getController().setCenter(itemPoint);
    }

    private void checkPermissionAndShowMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            showMyLocationAndDistance();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    private void showMyLocationAndDistance() {
        // Overlay hiện vị trí mình
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Lấy vị trí hiện tại → tính khoảng cách
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                tvDistance.setText("Location unavailable");
                return;
            }

            // Tính khoảng cách từ mình đến item
            float[] results = new float[1];
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    itemLat, itemLng, results);
            float distanceKm = results[0] / 1000f;

            if (distanceKm < 1) {
                // Dưới 1km hiện bằng mét
                tvDistance.setText(
                        String.format("%.0f m from you",
                                results[0]));
            } else {
                tvDistance.setText(
                        String.format("%.1f km from you", distanceKm));
            }

            // Zoom để thấy cả 2 điểm
            GeoPoint myPoint = new GeoPoint(
                    location.getLatitude(), location.getLongitude());
            GeoPoint itemPoint = new GeoPoint(itemLat, itemLng);

            // Tính trung điểm để center camera
            double midLat = (myPoint.getLatitude()
                    + itemPoint.getLatitude()) / 2;
            double midLng = (myPoint.getLongitude()
                    + itemPoint.getLongitude()) / 2;

            mapView.getController().animateTo(
                    new GeoPoint(midLat, midLng));

            // Tự động zoom phù hợp
            double latDiff = Math.abs(myPoint.getLatitude()
                    - itemPoint.getLatitude());
            double lngDiff = Math.abs(myPoint.getLongitude()
                    - itemPoint.getLongitude());
            double maxDiff = Math.max(latDiff, lngDiff);

            double zoom;
            if (maxDiff < 0.01)      zoom = 15;
            else if (maxDiff < 0.05) zoom = 13;
            else if (maxDiff < 0.1)  zoom = 12;
            else if (maxDiff < 0.5)  zoom = 10;
            else                     zoom = 8;

            mapView.getController().setZoom(zoom);
        });
    }

    // Mở Maps app với tọa độ item
    private void openInMapsApp() {
        // Thử mở Google Maps trước
        Uri gmmUri = Uri.parse(
                "geo:" + itemLat + "," + itemLng
                        + "?q=" + itemLat + "," + itemLng
                        + "(" + Uri.encode(itemTitle != null
                        ? itemTitle : "Item") + ")");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Không có app Maps → mở OpenStreetMap trên browser
            Uri browserUri = Uri.parse(
                    "https://www.openstreetmap.org/?mlat="
                            + itemLat + "&mlon=" + itemLng
                            + "#map=16/" + itemLat + "/" + itemLng);
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
            showMyLocationAndDistance();
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
