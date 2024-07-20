package com.example.nhatro360;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentAddress extends Fragment {

    private Spinner spinnerProvince;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private EditText editTextStreet;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<String> provinceIds;
    private List<String> districtIds;
    private JSONArray provincesArray;
    private JSONArray districtsArray;
    private JSONArray wardsArray;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address, container, false);

        spinnerProvince = view.findViewById(R.id.spinner_province);
        spinnerDistrict = view.findViewById(R.id.spinner_district);
        spinnerWard = view.findViewById(R.id.spinner_ward);
        editTextStreet = view.findViewById(R.id.edt_street);
        TextView tvCurrentLocation = view.findViewById(R.id.tv_current_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        tvCurrentLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getCurrentLocation();
            }
        });

        setupSpinners();

        return view;
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            updateAddressFields(latitude, longitude);
                        }
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

                editTextStreet.setText(street);

                if (province != null) {
                    int provincePosition = findProvincePosition(province);
                    if (provincePosition >= 0) {
                        spinnerProvince.setSelection(provincePosition);
                    }
                }

                if (district != null) {
                    int districtPosition = findDistrictPosition(district);
                    if (districtPosition >= 0) {
                        spinnerDistrict.setSelection(districtPosition);
                    }
                }

                if (ward != null) {
                    int wardPosition = findWardPosition(ward);
                    if (wardPosition >= 0) {
                        spinnerWard.setSelection(wardPosition);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findProvincePosition(String province) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerProvince.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(province)) {
                return i;
            }
        }
        return -1;
    }

    private int findDistrictPosition(String district) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerDistrict.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(district)) {
                return i;
            }
        }
        return -1;
    }

    private int findWardPosition(String ward) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerWard.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(ward)) {
                return i;
            }
        }
        return -1;
    }

    private void setupSpinners() {
        List<String> provinces = new ArrayList<>();
        provinceIds = new ArrayList<>();

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

            ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, provinces);
            provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvince.setAdapter(provinceAdapter);

            spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedProvinceId = provinceIds.get(position);
                    fetchDistricts(selectedProvinceId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchDistricts(String provinceId) {
        List<String> districtList = new ArrayList<>();
        districtIds = new ArrayList<>();

        try {
            for (int i = 0; i < districtsArray.length(); i++) {
                JSONObject districtObject = districtsArray.getJSONObject(i);
                if (districtObject.getString("idProvince").equals(provinceId)) {
                    districtList.add(districtObject.getString("name"));
                    districtIds.add(districtObject.getString("idDistrict"));
                }
            }

            ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, districtList);
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(districtAdapter);

            spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDistrictId = districtIds.get(position);
                    fetchWards(selectedDistrictId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchWards(String districtId) {
        List<String> wardList = new ArrayList<>();

        try {
            for (int i = 0; i < wardsArray.length(); i++) {
                JSONObject wardObject = wardsArray.getJSONObject(i);
                if (wardObject.getString("idDistrict").equals(districtId)) {
                    wardList.add(wardObject.getString("name"));
                }
            }

            if (wardList.isEmpty()) {
                Toast.makeText(getActivity(), "No wards found for the selected district", Toast.LENGTH_SHORT).show();
            }

            ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, wardList);
            wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(wardAdapter);

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
                Toast.makeText(getActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
