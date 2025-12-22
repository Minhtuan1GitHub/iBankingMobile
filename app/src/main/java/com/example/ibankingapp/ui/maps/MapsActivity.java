package com.example.ibankingapp.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity"; // Tag để lọc log
    private ActivityMapsBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final int LOCATION_PERMISSION_REQUEST = 100;
    private Location currentLocation;
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjNjZjI3ZTI2YzgzOTQ5MTlhYzVmNGZmMzUxNTIwY2VmIiwiaCI6Im11cm11cjY0In0=";
    private ExecutorService executorService;
    private Handler mainHandler;
    private Polyline currentPolyline; // Lưu đường đi hiện tại

    // Dữ liệu fake các chi nhánh ngân hàng tại TP.HCM
    private static class BankBranch {
        String name;
        LatLng location;

        BankBranch(String name, double lat, double lng) {
            this.name = name;
            this.location = new LatLng(lat, lng);
        }
    }

    private final BankBranch[] bankBranches = {
        new BankBranch("iBanking - Chi nhánh Bến Thành", 10.7727, 106.6980),
        new BankBranch("iBanking - Chi nhánh Điện Biên Phủ", 10.7805, 106.6954),
        new BankBranch("iBanking - Chi nhánh Nguyễn Huệ", 10.7745, 106.7011),
        new BankBranch("iBanking - Chi nhánh Lê Duẩn", 10.7787, 106.7023),
        new BankBranch("iBanking - Chi nhánh Đồng Khởi", 10.7764, 106.7020),
        new BankBranch("iBanking - Chi nhánh Pasteur", 10.7788, 106.6969),
        new BankBranch("iBanking - Chi nhánh Quận 3", 10.7719, 106.7023),
        new BankBranch("iBanking - Chi nhánh Phú Nhuận", 10.7751, 106.7000),
        new BankBranch("iBanking - Chi nhánh Tân Bình", 10.7776, 106.7013),
        new BankBranch("iBanking - Chi nhánh Bình Thạnh", 10.7748, 106.7046)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ExecutorService và Handler
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.fabNearest.setOnClickListener(v -> showNearbyBranches());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp ExecutorService
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: Bản đồ đã sẵn sàng");
        if (checkPermission()) {
            setupMapUI();
            getCurrentLocation();
        } else {
            Log.w(TAG, "onMapReady: Chưa có quyền vị trí, đang yêu cầu...");
            requestPermission();
        }
    }

    private void setupMapUI() {
        if (checkPermission()) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                Log.d(TAG, "setupMapUI: Đã bật MyLocationEnabled");

                // Thêm listener cho marker click để vẽ đường đi
                mMap.setOnMarkerClickListener(marker -> {
                    LatLng destination = marker.getPosition();
                    if (currentLocation != null) {
                        drawRoute(currentLocation, destination);
                        marker.showInfoWindow();
                    }
                    return true;
                });
            } catch (SecurityException e) {
                Log.e(TAG, "setupMapUI: Lỗi bảo mật", e);
            }
        }
    }

    private void getCurrentLocation() {
        if (!checkPermission()) {
            Log.w(TAG, "getCurrentLocation: Không có quyền truy cập vị trí");
            return;
        }

        Log.d(TAG, "getCurrentLocation: Đang lấy vị trí...");
        try {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            Log.d(TAG, "getCurrentLocation: Đã lấy được vị trí: Lat=" + location.getLatitude() + ", Lng=" + location.getLongitude());

                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            if (mMap != null) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                        } else {
                            Log.e(TAG, "getCurrentLocation: Location trả về null (Có thể do GPS tắt hoặc chưa có fix vị trí)");
                            Toast.makeText(this, "Không thể lấy vị trí hiện tại. Hãy bật GPS và thử lại.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "getCurrentLocation: Lỗi khi lấy vị trí", e);
                        Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "getCurrentLocation: Lỗi quyền truy cập", e);
        }
    }

    private void showNearbyBranches() {
        if (currentLocation == null) {
            Log.w(TAG, "showNearbyBranches: currentLocation là null, thử lấy lại vị trí");
            Toast.makeText(this, "Đang lấy vị trí, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        Log.d(TAG, "showNearbyBranches: Hiển thị các chi nhánh ngân hàng iBanking");

        // Xóa polyline cũ nếu có
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }

        // Xóa các marker cũ
        if (mMap != null) {
            mMap.clear();
        }

        // Hiển thị tất cả các chi nhánh ngân hàng iBanking
        LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        for (BankBranch branch : bankBranches) {
            mMap.addMarker(new MarkerOptions()
                    .position(branch.location)
                    .title(branch.name)
                    .snippet("Nhấn để xem đường đi")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }

        // Tìm chi nhánh gần nhất
        BankBranch nearest = findNearestBranch(userLocation);
        if (nearest != null) {
            // Tính khoảng cách
            float[] results = new float[1];
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                nearest.location.latitude, nearest.location.longitude,
                results
            );
            float distanceKm = results[0] / 1000;

            // Đánh dấu chi nhánh gần nhất bằng màu đỏ
            mMap.addMarker(new MarkerOptions()
                    .position(nearest.location)
                    .title(nearest.name + " (GẦN NHẤT)")
                    .snippet(String.format("Khoảng cách: %.2f km", distanceKm))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            Toast.makeText(this,
                String.format("Chi nhánh iBanking gần nhất:\n%s (%.2f km)\nĐang tìm đường đi...",
                    nearest.name, distanceKm),
                Toast.LENGTH_LONG).show();

            // Zoom để hiển thị tất cả các marker
            adjustCameraToShowAllBranches(userLocation);

            // Tự động vẽ đường đi đến chi nhánh gần nhất
            drawRoute(currentLocation, nearest.location);
        }
    }

    // Tìm chi nhánh gần nhất
    private BankBranch findNearestBranch(LatLng userLocation) {
        BankBranch nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (BankBranch branch : bankBranches) {
            float[] results = new float[1];
            Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                branch.location.latitude, branch.location.longitude,
                results
            );

            if (results[0] < minDistance) {
                minDistance = results[0];
                nearest = branch;
            }
        }

        return nearest;
    }

    // Điều chỉnh camera để hiển thị tất cả các chi nhánh
    private void adjustCameraToShowAllBranches(LatLng userLocation) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(userLocation);

        for (BankBranch branch : bankBranches) {
            builder.include(branch.location);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100;
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }


    private void drawRoute(Location from, LatLng to) {
        Toast.makeText(this, "Đang tìm đường đi ngắn nhất...", Toast.LENGTH_SHORT).show();

        // Xóa đường đi cũ
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        // Sử dụng ExecutorService để gọi API directions
        executorService.execute(() -> {
            String routeJson = fetchRoute(from, to);
            mainHandler.post(() -> displayRoute(routeJson, to));
        });
    }

    // Gọi API OpenRouteService để lấy đường đi
    private String fetchRoute(Location from, LatLng to) {
        OkHttpClient client = new OkHttpClient();

        // API directions của OpenRouteService
        String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + ORS_API_KEY +
                "&start=" + from.getLongitude() + "," + from.getLatitude() +
                "&end=" + to.longitude + "," + to.latitude;

        Log.d(TAG, "fetchRoute: Calling URL: " + url);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String result = response.body().string();
                Log.d(TAG, "fetchRoute: API Response: " + result);
                return result;
            } else {
                String errorBody = "";
                if (response.body() != null) {
                    errorBody = response.body().string();
                }
                Log.e(TAG, "fetchRoute: API Error Code: " + response.code() + ", Body: " + errorBody);
            }
        } catch (IOException e) {
            Log.e(TAG, "fetchRoute: Lỗi kết nối mạng", e);
        }
        return null;
    }

    // Hiển thị đường đi trên bản đồ
    private void displayRoute(String routeJson, LatLng destination) {
        if (routeJson == null) {
            Toast.makeText(this, "Không thể tìm đường đi", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject(routeJson);
            JSONArray features = jsonObject.getJSONArray("features");

            if (features.length() > 0) {
                JSONObject feature = features.getJSONObject(0);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                JSONObject properties = feature.getJSONObject("properties");
                JSONObject summary = properties.getJSONObject("summary");

                // Lấy thông tin khoảng cách và thời gian
                double distance = summary.getDouble("distance") / 1000; // chuyển sang km
                double duration = summary.getDouble("duration") / 60; // chuyển sang phút

                // Tạo danh sách các điểm trên đường đi
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLUE);
                polylineOptions.width(10);

                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray point = coordinates.getJSONArray(i);
                    double lng = point.getDouble(0);
                    double lat = point.getDouble(1);
                    polylineOptions.add(new LatLng(lat, lng));
                }

                // Vẽ đường đi
                currentPolyline = mMap.addPolyline(polylineOptions);

                // Hiển thị thông tin
                Toast.makeText(this,
                    String.format("Khoảng cách: %.2f km\nThời gian dự kiến: %.0f phút", distance, duration),
                    Toast.LENGTH_LONG).show();

                // Zoom camera để hiển thị toàn bộ đường đi
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                builder.include(destination);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));

                Log.d(TAG, "displayRoute: Đã vẽ đường đi thành công");
            }
        } catch (Exception e) {
            Log.e(TAG, "displayRoute: Lỗi parse JSON", e);
            Toast.makeText(this, "Lỗi xử lý dữ liệu đường đi", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission() {
        boolean hasFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "checkPermission: Fine=" + hasFine + ", Coarse=" + hasCoarse);
        return hasFine;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Quyền đã được cấp");
                setupMapUI();
                getCurrentLocation();
            } else {
                Log.w(TAG, "onRequestPermissionsResult: Quyền bị từ chối");
                Toast.makeText(this, "Cần cấp quyền vị trí để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            }
        }
    }
}