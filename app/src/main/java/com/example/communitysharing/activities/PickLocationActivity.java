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

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private Marker centerMarker;

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

        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(
                "CommunitySharing/1.0 (Android; contact@gmail.com)");
//        Configuration.getInstance().setConnectTimeout(5000);
//        Configuration.getInstance().setReadTimeout(5000);

        setContentView(R.layout.activity_pick_location);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        mapView            = findViewById(R.id.mapPickLocation);
        tvSelectedAddress  = findViewById(R.id.tvSelectedAddress);
        etSearchLocation   = findViewById(R.id.etSearchLocation);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        ivBack             = findViewById(R.id.ivBack);
        ivMyLocation       = findViewById(R.id.ivMyLocation);

        setupMap();
        checkLocationPermission();

        ivBack.setOnClickListener(v -> finish());

        ivMyLocation.setOnClickListener(v -> moveToMyLocation());

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

        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearchLocation.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void setupMap() {
        // Tạo custom tile source dùng server khác
        // Không bị chặn như OSM Mapnik
        org.osmdroid.tileprovider.tilesource.XYTileSource tileSource =
                new org.osmdroid.tileprovider.tilesource.XYTileSource(
                        "CartoDB",           // Tên tile source
                        0,                   // Zoom min
                        19,                  // Zoom max
                        256,                 // Tile size
                        ".png",              // Extension
                        new String[]{
                                "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                                "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                                "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
                        },
                        "© CartoDB © OpenStreetMap contributors"
                );

        mapView.setTileSource(tileSource);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        GeoPoint haNoi = new GeoPoint(21.0285, 105.8542);
        mapView.getController().setCenter(haNoi);
        selectedLat = haNoi.getLatitude();
        selectedLng = haNoi.getLongitude();

        addCenterMarker();

        mapView.setMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                GeoPoint center = (GeoPoint) mapView.getMapCenter();
                selectedLat = center.getLatitude();
                selectedLng = center.getLongitude();

                if (centerMarker != null) {
                    centerMarker.setPosition(center);
                }
                getAddressFromNominatim(selectedLat, selectedLng);
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                return false;
            }
        });
    }

    private void addCenterMarker() {
        GeoPoint haNoi = new GeoPoint(21.0285, 105.8542);
        centerMarker = new Marker(mapView);
        centerMarker.setPosition(haNoi);
        centerMarker.setTitle("Selected Location");
        centerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        centerMarker.setIcon(ContextCompat.getDrawable(this,
                android.R.drawable.ic_menu_mylocation));
        mapView.getOverlays().add(centerMarker);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupMyLocationOverlay();
            moveToMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void setupMyLocationOverlay() {
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        }
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
                mapView.getController().setZoom(14.0);

                selectedLat = location.getLatitude();
                selectedLng = location.getLongitude();

                if (centerMarker != null) {
                    centerMarker.setPosition(myPoint);
                }

                getAddressFromNominatim(selectedLat, selectedLng);
            }
        });
    }

    // 🔧 Dùng HttpURLConnection (không cần Volley)
    private void getAddressFromNominatim(double lat, double lng) {
        new Thread(() -> {
            try {
                String urlString = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                        + lat + "&lon=" + lng + "&zoom=18&addressdetails=1&accept-language=vi";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "CommunitySharing/1.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONObject json = new JSONObject(response.toString());
                    String address = json.optString("display_name", "Unknown location");
                    selectedAddress = address;

                    runOnUiThread(() -> tvSelectedAddress.setText(address));
                } else {
                    // Fallback: dùng Geocoder nếu fail
                    getAddressFromGeocoder(lat, lng);
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback
                getAddressFromGeocoder(lat, lng);
            }
        }).start();
    }

    private void getAddressFromGeocoder(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                if (!geocoder.isPresent()) {
                    runOnUiThread(() -> tvSelectedAddress.setText(
                            lat + ", " + lng));
                    return;
                }

                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    if (addr.getThoroughfare() != null) {
                        sb.append(addr.getThoroughfare());
                    }
                    if (addr.getSubLocality() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getSubLocality());
                    }
                    if (addr.getLocality() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getLocality());
                    }
                    if (addr.getAdminArea() != null) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(addr.getAdminArea());
                    }

                    selectedAddress = sb.toString();
                    runOnUiThread(() -> tvSelectedAddress.setText(
                            selectedAddress.isEmpty() ? lat + ", " + lng : selectedAddress));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 🔧 Search dùng HttpURLConnection
    private void searchLocation(String query) {
        if (query.isEmpty()) {
            Toast.makeText(this, "Enter location", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
                String urlString = "https://nominatim.openstreetmap.org/search?format=json&q="
                        + encodedQuery + "&limit=1&countrycodes=vn";

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "CommunitySharing/1.0");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    JSONArray results = new JSONArray(response.toString());
                    if (results.length() > 0) {
                        JSONObject result = results.getJSONObject(0);
                        double lat = result.getDouble("lat");
                        double lon = result.getDouble("lon");
                        String address = result.optString("display_name", "Unknown");

                        GeoPoint point = new GeoPoint(lat, lon);
                        selectedLat = lat;
                        selectedLng = lon;
                        selectedAddress = address;

                        runOnUiThread(() -> {
                            mapView.getController().animateTo(point);
                            mapView.getController().setZoom(14.0);

                            if (centerMarker != null) {
                                centerMarker.setPosition(point);
                            }

                            tvSelectedAddress.setText(address);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this,
                                "Location not found", Toast.LENGTH_SHORT).show());
                    }
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Search error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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