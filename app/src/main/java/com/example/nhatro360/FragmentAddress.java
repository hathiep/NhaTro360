package com.example.nhatro360;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.nhatro360.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentAddress extends Fragment {

    private static final String TAG = "FragmentAddress";
    private EditText edtProvince;
    private EditText edtDistrict;
    private EditText edtWard;
    private EditText edtStreet;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> provinceIds;
    private List<String> districtIds;
    private List<String> wardIds;
    private JSONArray provincesArray;
    private JSONArray districtsArray;
    private JSONArray wardsArray;
    private String provinceId, districtId, wardId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        edtProvince = view.findViewById(R.id.edt_province);
        edtDistrict = view.findViewById(R.id.edt_district);
        edtWard = view.findViewById(R.id.edt_ward);
        edtStreet = view.findViewById(R.id.edt_street);
        provinceId = "";
        districtId = "";
        wardId = "";
        TextView tvCurrentLocation = view.findViewById(R.id.tv_current_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        tvCurrentLocation.setOnClickListener(v -> {
            Log.d(TAG, "Current location TextView clicked");
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getCurrentLocation();
            }
        });

        setupPopupMenus();

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Log.d(TAG, "Attempting to get current location");

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        Log.d(TAG, "Location obtained: " + location.toString());
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateAddressFields(latitude, longitude);
                    } else {
                        Log.d(TAG, "Location is null");
                    }
                });
    }

    private void updateAddressFields(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String province = address.getAdminArea();
                String district = address.getSubAdminArea();
                String ward = address.getLocality();
                String street = address.getThoroughfare();

                edtStreet.setText(street);

                if (province != null) {
                    int provincePosition = findProvincePosition(province);
                    if (provincePosition >= 0 && provincePosition < provinceIds.size()) {
                        edtProvince.setText(province);
                        provinceId = provinceIds.get(provincePosition);
                        fetchDistricts(provinceId);
                    } else {
                        Log.e(TAG, "Province not found or index out of bounds");
                    }
                }

                if (district != null) {
                    int districtPosition = findDistrictPosition(district);
                    if (districtPosition >= 0 && districtPosition < districtIds.size()) {
                        edtDistrict.setText(district);
                        districtId = districtIds.get(districtPosition);
                        fetchWards(districtId);
                    } else {
                        Log.e(TAG, "District not found or index out of bounds");
                    }
                }

                if (ward != null) {
                    int wardPosition = findWardPosition(ward);
                    if (wardPosition >= 0 && wardPosition < wardIds.size()) {
                        edtWard.setText(ward);
                        wardId = wardIds.get(wardPosition);
                    } else {
                        Log.e(TAG, "Ward not found or index out of bounds");
                    }
                }
            } else {
                Log.d(TAG, "No addresses found");
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception", e);
        }
    }

    private int findProvincePosition(String province) {
        for (int i = 0; i < provincesArray.length(); i++) {
            try {
                if (provincesArray.getJSONObject(i).getString("name").equals(province)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private int findDistrictPosition(String district) {
        for (int i = 0; i < districtsArray.length(); i++) {
            try {
                if (districtsArray.getJSONObject(i).getString("name").equals(district)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private int findWardPosition(String ward) {
        for (int i = 0; i < wardsArray.length(); i++) {
            try {
                if (wardsArray.getJSONObject(i).getString("name").equals(ward)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private void setupPopupMenus() {
        List<String> provinces = new ArrayList<>();
        provinceIds = new ArrayList<>();
        List<String> districts = new ArrayList<>();
        districtIds = new ArrayList<>();
        List<String> wards = new ArrayList<>();
        wardIds = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("provinces.json")));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            provincesArray = jsonObject.getJSONArray("province");
            districtsArray = jsonObject.getJSONArray("district");
            wardsArray = jsonObject.getJSONArray("ward");

            for (int i = 0; i < provincesArray.length(); i++) {
                JSONObject provinceObject = provincesArray.getJSONObject(i);
                provinces.add(provinceObject.getString("name"));
                provinceIds.add(provinceObject.getString("idProvince"));
            }

            edtProvince.setOnClickListener(v -> showListViewPopup(v, provinces, (position) -> {
                edtProvince.setText(provinces.get(position));
                provinceId = provinceIds.get(position);
                edtDistrict.setText("");
                edtWard.setText("");
                districtId = "";
                wardId = "";
                fetchDistricts(provinceId);
            }));

            edtDistrict.setOnClickListener(v -> {
                if (provinceId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/Tp", Toast.LENGTH_SHORT).show();
                    return;
                }
                showListViewPopup(v, districts, (position) -> {
                    edtDistrict.setText(districts.get(position));
                    districtId = districtIds.get(position);
                    edtWard.setText("");
                    wardId = "";
                    fetchWards(districtId);
                });
            });

            edtWard.setOnClickListener(v -> {
                if (provinceId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/Tp", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (districtId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                showListViewPopup(v, wards, (position) -> {
                    edtWard.setText(wards.get(position));
                    wardId = wardIds.get(position);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showListViewPopup(View anchor, List<String> items, OnItemClickListener listener) {
        // Create a new dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.popup_list);

        ListView listView = dialog.findViewById(R.id.list_view_popup);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            listener.onItemClick(position);
            dialog.dismiss();
        });

        // Configure dialog to appear from the bottom and occupy half of the screen height
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM; // Display from the bottom
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Full width
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            layoutParams.height = display.getHeight() / 2; // Half of the screen height
            window.setAttributes(layoutParams);

            // Remove padding and set dialog to bám sát màn hình
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(null);
        }

        dialog.show();
    }

    private void fetchDistricts(String provinceId) {
        List<String> districtList = new ArrayList<>();
        districtIds = new ArrayList<>();

        try {
            if(districtsArray.length() == 0){
                Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/Tp", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < districtsArray.length(); i++) {
                JSONObject districtObject = districtsArray.getJSONObject(i);
                if (districtObject.getString("idProvince").equals(provinceId)) {
                    districtList.add(districtObject.getString("name"));
                    districtIds.add(districtObject.getString("idDistrict"));
                }
            }

            edtDistrict.setOnClickListener(v -> showListViewPopup(v, districtList, (position) -> {
                edtDistrict.setText(districtList.get(position));
                districtId = districtIds.get(position);
                edtWard.setText("");
                wardId = "";
                fetchWards(districtId);
            }));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchWards(String districtId) {
        List<String> wardList = new ArrayList<>();
        wardIds = new ArrayList<>();

        try {
            if(districtsArray.length() == 0){
                Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                return;
            }
            if(wardsArray.length() == 0){
                Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < wardsArray.length(); i++) {
                JSONObject wardObject = wardsArray.getJSONObject(i);
                if (wardObject.getString("idDistrict").equals(districtId)) {
                    wardList.add(wardObject.getString("name"));
                    wardIds.add(wardObject.getString("idWard"));
                }
            }

            edtWard.setOnClickListener(v -> showListViewPopup(v, wardList, (position) -> {
                edtWard.setText(wardList.get(position));
                wardId = wardIds.get(position);
            }));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface OnItemClickListener {
        void onItemClick(int position);
    }
}
//Sửa lại để nếu người dùng chưa chọn Tỉnh/Tp mà click vào chọn Quận/Huyện, chọn Xã/Phường thì thông báo Vui lòng chọn Tỉnh/Tp, nếu đã chọn Tỉnh/TP mà chưa chọn Quận/Huyện nhưng lại click vào chọn Xã/Phường thì thông báo Vui lòng chọn Quận/Huyện