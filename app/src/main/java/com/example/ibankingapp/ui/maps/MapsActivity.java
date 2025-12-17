package com.example.ibankingapp.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ibankingapp.R;
import com.example.ibankingapp.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityMapsBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int LOCATION_PERMISSION_REQUEST = 100;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Xử lý sự kiện bấm nút "Tìm chi nhánh gần nhất"
        binding.fabNearest.setOnClickListener(v -> showNearbyBranches());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (checkPermission()) {
            setupMapUI();
            getCurrentLocation();
        } else {
            requestPermission();
        }
    }

    // Thiết lập giao diện bản đồ
    private void setupMapUI() {
        if (checkPermission()) {
            try {
                mMap.setMyLocationEnabled(true); // Hiển thị chấm xanh vị trí
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    // Lấy vị trí hiện tại và di chuyển camera
    private void getCurrentLocation() {
        if (!checkPermission()) return;

        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            // Zoom camera vào vị trí người dùng
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Giả lập hiển thị các chi nhánh ngân hàng xung quanh
    private void showNearbyBranches() {
        if (mMap == null || currentLocation == null) {
            Toast.makeText(this, "Đang xác định vị trí...", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        // Xóa các marker cũ (nếu có)
        mMap.clear();

        // Tính toán vị trí giả lập xung quanh vị trí hiện tại (cho mục đích Demo)
        double lat = currentLocation.getLatitude();
        double lng = currentLocation.getLongitude();

        // Chi nhánh 1
        LatLng branch1 = new LatLng(lat + 0.002, lng + 0.002);
        mMap.addMarker(new MarkerOptions()
                .position(branch1)
                .title("Chi nhánh Quận 1")
                .snippet("Giờ mở cửa: 8:00 - 17:00")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Chi nhánh 2
        LatLng branch2 = new LatLng(lat - 0.003, lng + 0.001);
        mMap.addMarker(new MarkerOptions()
                .position(branch2)
                .title("ATM Nguyễn Văn Cừ")
                .snippet("Hoạt động 24/7")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Màu xanh cho ATM

        // Chi nhánh 3
        LatLng branch3 = new LatLng(lat + 0.001, lng - 0.003);
        mMap.addMarker(new MarkerOptions()
                .position(branch3)
                .title("PGD Trần Hưng Đạo")
                .snippet("Có quầy giao dịch ưu tiên")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        Toast.makeText(this, "Đã tìm thấy 3 điểm giao dịch gần bạn", Toast.LENGTH_SHORT).show();

        // Zoom out một chút để thấy các chi nhánh
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMapUI();
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Cần cấp quyền vị trí để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}