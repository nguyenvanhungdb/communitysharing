package com.example.communitysharing.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PickLocationActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 300;

    public static final String EXTRA_LATITUDE  = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_ADDRESS   = "address";

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedClient;

    private TextView tvSelectedAddress;
    private EditText etSearchLocation;
    private Button btnConfirmLocation;
    private ImageView ivBack, ivMyLocation;

    private double selectedLat = 0;
    private double selectedLng = 0;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // BẮT BUỘC khởi tạo OSMDroid trước khi setContentView
        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(
                getPackageName());

        setContentView(R.layout.activity_pick_location);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        // Ánh xạ views
        mapView            = findViewById(R.id.mapPickLocation);
        tvSelectedAddress  = findViewById(R.id.tvSelectedAddress);
        etSearchLocation   = findViewById(R.id.etSearchLocation);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        ivBack             = findViewById(R.id.ivBack);
        ivMyLocation       = findViewById(R.id.ivMyLocation);

        // Setup OSMDroid map
        setupMap();

        // Xin permission location
        checkLocationPermission();

        // Back
        ivBack.setOnClickListener(v -> finish());

        // My Location
        ivMyLocation.setOnClickListener(v -> moveToMyLocation());

        // Confirm
        btnConfirmLocation.setOnClickListener(v -> {
            if (selectedLat == 0 && selectedLng == 0) {
                Toast.makeText(this,
                        "Please select a location",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent result = new Intent();
            result.putExtra(EXTRA_LATITUDE,  selectedLat);
            result.putExtra(EXTRA_LONGITUDE, selectedLng);
            result.putExtra(EXTRA_ADDRESS,   selectedAddress);
            setResult(RESULT_OK, result);
            finish();
        });

        // Search
        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(
                        etSearchLocation.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void setupMap() {
        // Dùng tile OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Mặc định zoom về Hà Nội
        GeoPoint haNoi = new GeoPoint(21.0285, 105.8542);
        mapView.getController().setCenter(haNoi);

        // Lắng nghe khi map dừng di chuyển
        // → lấy địa chỉ của tâm màn hình
        mapView.addOnFirstLayoutListener(
                (v, left, top, right, bottom) -> {
                    // Map đã sẵn sàng
                });

        // Dùng MapListener để detect khi scroll xong
        mapView.setMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                // Lấy tọa độ tâm màn hình
                GeoPoint center = (GeoPoint)
                        mapView.getMapCenter();
                selectedLat = center.getLatitude();
                selectedLng = center.getLongitude();

                // Reverse geocode
                getAddressFromLatLng(selectedLat, selectedLng);
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                return false;
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay();
            moveToMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void setupMyLocationOverlay() {
        // Overlay hiện vị trí hiện tại trên map
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void moveToMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                GeoPoint myPoint = new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude());
                mapView.getController().animateTo(myPoint);
                mapView.getController().setZoom(16.0);
            }
        });
    }

    // Reverse geocode: tọa độ → địa chỉ
    private void getAddressFromLatLng(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this,
                        Locale.getDefault());
                List<Address> addresses =
                        geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (addr.getThoroughfare() != null) {
                        sb.append(addr.getThoroughfare()).append(", ");
                    }
                    if (addr.getSubLocality() != null) {
                        sb.append(addr.getSubLocality()).append(", ");
                    }
                    if (addr.getLocality() != null) {
                        sb.append(addr.getLocality());
                    }

                    selectedAddress = sb.toString();

                    runOnUiThread(() -> tvSelectedAddress.setText(
                            selectedAddress.isEmpty()
                                    ? "Unknown location"
                                    : selectedAddress));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Geocode: địa chỉ → tọa độ
    private void searchLocation(String query) {
        if (query.isEmpty()) return;

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this,
                        Locale.getDefault());
                List<Address> addresses =
                        geocoder.getFromLocationName(query, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    GeoPoint point = new GeoPoint(
                            addr.getLatitude(), addr.getLongitude());

                    runOnUiThread(() -> {
                        mapView.getController().animateTo(point);
                        mapView.getController().setZoom(16.0);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Location not found",
                            Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay();
            moveToMyLocation();
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