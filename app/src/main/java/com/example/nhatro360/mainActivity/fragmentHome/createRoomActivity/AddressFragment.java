package com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AddressFragment extends Fragment {

    private static final String TAG = "AddressFragment";
    private EditText edtProvince, edtDistrict, edtWard, edtStreet;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> provinceIds, districtIds, wardIds;
    private JSONArray provincesArray, districtsArray, wardsArray;
    private String provinceId, districtId, wardId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        init(view);

        try {
            getArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        TextView tvCurrentLocation = view.findViewById(R.id.tv_current_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        setupPopupMenus();

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

        return view;
    }

    private void init(View view){
        edtProvince = view.findViewById(R.id.edt_province);
        edtDistrict = view.findViewById(R.id.edt_district);
        edtWard = view.findViewById(R.id.edt_ward);
        edtStreet = view.findViewById(R.id.edt_street);
        provinceId = "";
        districtId = "";
        wardId = "";
    }

    // Lấy danh sách tỉnh, huyện, xã từ file provinces.json
    private void getArray() throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("provinces.json")));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
        provincesArray = sortArray(jsonObject.getJSONArray("province"), true);
        districtsArray = sortArray(jsonObject.getJSONArray("district"), false);
        wardsArray = sortArray(jsonObject.getJSONArray("ward"), false);
    }

    // Sắp xếp theo tên
    private JSONArray sortArray(JSONArray arr, boolean prov) throws JSONException {
        List<JSONObject> list = new ArrayList<>();
        int x = 0;
        if(prov) x = 5; // 5 Thành phố trực thuộc trung ương ưu tiên lên đầu không sắp xếp
        for (int i = x; i < arr.length(); i++) {
            list.add(arr.getJSONObject(i));
        }
        Collections.sort(list, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = a.optString("name");
                String valB = b.optString("name");
                return valA.compareTo(valB);
            }
        });

        JSONArray sortedJsonArray = new JSONArray();
        if(prov) for(int i=0; i<5; i++) sortedJsonArray.put(arr.get(i));
        for (JSONObject object : list) sortedJsonArray.put(object);
        return sortedJsonArray;
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        edtProvince.setText("");
        edtDistrict.setText("");
        edtWard.setText("");
        provinceId = "";
        districtId = "";
        wardId = "";
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
                String houseNumber = address.getFeatureName();

                String fullAddress = address.getAddressLine(0);

                if (fullAddress != null) {
                    // Tách chuỗi dựa trên dấu phẩy
                    String[] addressParts = fullAddress.split(",");

                    if (addressParts.length >= 2) {
                        // Phần tử thứ 2 là phường/xã
                        ward = addressParts[1].trim();

                        // Log kết quả để kiểm tra
                        Log.d(TAG, "Ward (Phường/Xã): " + ward);

                        // Cập nhật vào EditText hoặc các trường giao diện khác
                        edtWard.setText(ward);
                    } else {
                        Log.e(TAG, "Không thể tìm thấy Phường/Xã trong địa chỉ");
                    }
                } else {
                    Log.d(TAG, "No address line found");
                }

                // Nếu không có số nhà, sử dụng tọa độ
                if (houseNumber == null || houseNumber.isEmpty()) {
                    houseNumber = latitude + ", " + longitude;
                }
                if(street.equals("null") || street.equals("Unnamed")) edtStreet.setText(houseNumber);
                else edtStreet.setText(houseNumber + ", " + address.getThoroughfare());

                provinceIds = new ArrayList<>();
                for (int i = 0; i < provincesArray.length(); i++) {
                    JSONObject provinceObject = provincesArray.getJSONObject(i);
                    provinceIds.add(provinceObject.getString("idProvince"));
                }
                if (province != null) {
                    int provincePosition = findProvincePosition(province.trim());
                    if (provincePosition >= 0) {
                        edtProvince.setText(province);
                        provinceId = provinceIds.get(provincePosition);
                        fetchDistricts(provinceId);
                    } else {
                        edtProvince.setText(null);
                        Toast.makeText(getActivity(),"Không tìm thấy Tỉnh/TP. Vui lòng chọn trong danh sách!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Province not found or index out of bounds " + provincePosition);
                    }
                }
                districtIds = new ArrayList<>();
                for (int i = 0; i < districtsArray.length(); i++) {
                    JSONObject districtObject = districtsArray.getJSONObject(i);
                    districtIds.add(districtObject.getString("idDistrict"));
                }
                if (district != null) {
                    int districtPosition = findDistrictPosition(district.trim(), provinceId);
                    if (districtPosition >= 0) {
                        edtDistrict.setText(district);
                        districtId = districtIds.get(districtPosition);
                        fetchWards(districtId);
                    } else {
                        edtDistrict.setText(null);
                        Toast.makeText(getActivity(),"Không tìm thấy Quận/Huyện. Vui lòng chọn trong danh sách!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "District not found or index out of bounds " + districtPosition);
                    }
                }
                wardIds = new ArrayList<>();
                for (int i = 0; i < wardsArray.length(); i++) {
                    JSONObject wardsObject = wardsArray.getJSONObject(i);
                    wardIds.add(wardsObject.getString("idWard"));
                }
                if (ward != null) {
                    int wardPosition = findWardPosition(ward.trim(), districtId);
                    Log.e(TAG, "wardPosition" + wardPosition);
                    if (wardPosition >= 0) {
                        edtWard.setText(wardsArray.getJSONObject(wardPosition).getString("name"));
                        wardId = wardIds.get(wardPosition);
                    } else {
                        edtWard.setText(null);
                        Toast.makeText(getActivity(),"Không tìm thấy Phường/Xã. Vui lòng chọn trong danh sách!", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Ward not found or index out of bounds " + wardPosition);
                    }
                }
            } else {
                Log.d(TAG, "No addresses found");
            }
        } catch (IOException | JSONException e) {
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

    private int findDistrictPosition(String district, String provinceId) {
        for (int i = 0; i < districtsArray.length(); i++) {
            try {
                if (districtsArray.getJSONObject(i).getString("idProvince").equals(provinceId) && districtsArray.getJSONObject(i).getString("name").equals(district)) {
                    return i;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private int findWardPosition(String ward, String districtId) {
        for (int i = 0; i < wardsArray.length(); i++) {
            try {
                if (wardsArray.getJSONObject(i).getString("idDistrict").equals(districtId) &&
                        (wardsArray.getJSONObject(i).getString("name").contains(ward) ||
                                ward.contains(wardsArray.getJSONObject(i).getString("name")))) {
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

        try {
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
                districtIds = new ArrayList<>();
                wardIds = new ArrayList<>();
                districtId = "";
                wardId = "";
                fetchDistricts(provinceId);
                fetchWards("-1");
            }));

            edtDistrict.setOnClickListener(v -> {
                if (provinceId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> districtList = new ArrayList<>();
                districtIds = new ArrayList<>(); // Ensure this is cleared and repopulated correctly
                try {
                    for (int i = 0; i < districtsArray.length(); i++) {
                        JSONObject districtObject = districtsArray.getJSONObject(i);
                        if (districtObject.getString("idProvince").equals(provinceId)) {
                            districtList.add(districtObject.getString("name"));
                            districtIds.add(districtObject.getString("idDistrict")); // Add district IDs corresponding to province
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showListViewPopup(v, districtList, (position) -> {
                    edtDistrict.setText(districtList.get(position));
                    districtId = districtIds.get(position);

                    // Clear the ward field and reset its listener
                    edtWard.setText("");
                    wardId = "";
                    fetchWards(districtId);
                });
            });

            edtWard.setOnClickListener(v -> {
                if (provinceId.equals("")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (districtId.equals("") || districtId.equals("-1")) {
                    Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> wardList = new ArrayList<>();
                wardIds = new ArrayList<>(); // Ensure this is cleared and repopulated correctly
                try {
                    for (int i = 0; i < wardsArray.length(); i++) {
                        JSONObject wardObject = wardsArray.getJSONObject(i);
                        if (wardObject.getString("idDistrict").equals(districtId)) {
                            wardList.add(wardObject.getString("name"));
                            wardIds.add(wardObject.getString("idWard")); // Add ward IDs corresponding to district
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showListViewPopup(v, wardList, (position) -> {
                    if(districtId.equals("-1")){
                        Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    edtWard.setText(wardList.get(position));
                    wardId = wardIds.get(position);
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchDistricts(String provinceId) throws JSONException {
        List<String> districtList = new ArrayList<>();
        districtIds = new ArrayList<>();
        wardIds = new ArrayList<>();
        wardId = "";

        if (provinceId.equals("")) {
            Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
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
            districtIds.clear();
            for (int i = 0; i < districtsArray.length(); i++) {
                JSONObject districtObject = districtsArray.getJSONObject(i);
                if (districtObject.getString("idProvince").equals(provinceId)) {
                    districtList.add(districtObject.getString("name"));
                    districtIds.add(districtObject.getString("idDistrict"));
                }
            }
            edtDistrict.setText(districtList.get(position));
            districtId = districtIds.get(position);
            edtWard.setText("");
            wardId = "";
            fetchWards(districtId);
        }));

    }

    private void fetchWards(String districtId) throws JSONException {
        List<String> wardList = new ArrayList<>();
        wardIds = new ArrayList<>();

        if (provinceId.equals("")) {
            Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
            return;
        }
        if (districtId.equals("")) {
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
            if(districtId.equals("-1")){
                Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                return;
            }

            wardIds.clear();
            for (int i = 0; i < wardsArray.length(); i++) {
                JSONObject wardObject = wardsArray.getJSONObject(i);
                if (wardObject.getString("idDistrict").equals(districtId)) {
                    wardList.add(wardObject.getString("name"));
                    wardIds.add(wardObject.getString("idWard"));
                }
            }
            edtWard.setText(wardList.get(position));
            wardId = wardIds.get(position);
        }));

    }

    private void showListViewPopup(View anchor, List<String> items, OnItemClickListener listener) {
        if(items.size() == 0){
            Toast.makeText(getActivity(), "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
            return;
        }
        // Create a new dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.dialog_create_address);

        ListView listView = dialog.findViewById(R.id.list_view_popup);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                listener.onItemClick(position);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
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

    public String getProvince() {
        return edtProvince.getText().toString().trim();
    }

    public String getDistrict() {
        return edtDistrict.getText().toString().trim();
    }

    public String getWard() {
        return edtWard.getText().toString().trim();
    }

    public String getStreet() {
        return edtStreet.getText().toString().trim();
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
        void onItemClick(int position) throws JSONException;
    }
}