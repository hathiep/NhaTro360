package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentHome.creatPost.FragmentAddress;
import com.example.nhatro360.controller.roomDetail.RoomDetail;
import com.example.nhatro360.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText edtSearch, edtProvince, edtDistrict;
    private ImageView imvDrop, imvFilter;
    private FragmentSingleListRoom fragmentSingleListRoom;
    private FirebaseFirestore db;
    private View overlay;
    private Dialog dialog;
    private List<String> provinceIds, districtIds;
    private JSONArray provincesArray, districtsArray;
    private String provinceId, districtId, province, district;
    private Button btnSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        edtSearch = view.findViewById(R.id.edt_search);
        imvDrop = view.findViewById(R.id.imv_drop);
        overlay = view.findViewById(R.id.overlay);
        province = district = provinceId = districtId = "";
        imvFilter = view.findViewById(R.id.imv_filter);
        imvFilter.setVisibility(View.GONE);
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize fragmentSingleListRoom
        fragmentSingleListRoom = new FragmentSingleListRoom();

        // Lắng nghe sự kiện khi nhấn Enter (Search) trên bàn phím
        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(edtSearch.getText().toString());
                    imvFilter.setVisibility(View.VISIBLE);
                    return true;
                }
                return false;
            }
        });

        imvDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showMenuDialog(view);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                setupPopupMenus();
            }
        });

        return view;
    }

    private void performSearch(String query) {
        View view = getView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        searchRooms(query, new FirestoreCallback() {
            @Override
            public void onCallback(List<Room> searchResults) {
                // Gửi kết quả tìm kiếm tới fragmentSingleListRoom
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("searchResults", (ArrayList<? extends Parcelable>) searchResults);
                fragmentSingleListRoom.setArguments(bundle);

                // Hiển thị fragmentSingleListRoom trong SearchFragment
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container1, fragmentSingleListRoom);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void searchRooms(String query, FirestoreCallback callback) {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> rooms = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            rooms.add(room);
                        }
                        // Lọc kết quả tìm kiếm trên thiết bị
                        List<Room> filteredRooms = filterRoomsByQuery(rooms, query);
                        callback.onCallback(filteredRooms);
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private List<Room> filterRoomsByQuery(List<Room> rooms, String query) {
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getAddress() != null && room.getAddress().toLowerCase().contains(query.toLowerCase())) {
                filteredRooms.add(room);
            }
        }
        return filteredRooms;
    }

    private interface FirestoreCallback {
        void onCallback(List<Room> searchResults);
    }

    private void showMenuDialog(View view) throws JSONException {
        overlay.setVisibility(View.VISIBLE);

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_address);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set dialog width to match parent and height to wrap content
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(layoutParams);

        edtProvince = dialog.findViewById(R.id.edt_province);
        edtDistrict = dialog.findViewById(R.id.edt_district);
        try {
            getArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
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
            } else {
                Log.e(TAG, "District not found or index out of bounds " + districtPosition);
            }
        }
        edtProvince.setText(province);
        edtDistrict.setText(district);
        btnSearch = dialog.findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtProvince.getText().toString().equals("")){
                    Toast.makeText(getActivity(), "Vui lòng chọn Tỉnh/TP", Toast.LENGTH_SHORT).show();
                    return;
                }
                overlay.setVisibility(View.GONE);
                dialog.dismiss();
                province = edtProvince.getText().toString();
                district = edtDistrict.getText().toString();
                if(district.equals("")) edtSearch.setText(province);
                else edtSearch.setText(district + ", " + province);
                performSearch(district + ", " + province);
                imvFilter.setVisibility(View.VISIBLE);
            }
        });
        // Show dialog
        dialog.show();

        dialog.setOnDismissListener(dialogInterface -> {
            // Hide overlay when dialog is dismissed
            overlay.setVisibility(View.GONE);
        });

    }

    private void getArray() throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("provinces.json")));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
        provincesArray = jsonObject.getJSONArray("province");
        districtsArray = jsonObject.getJSONArray("district");
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

    private void fetchDistricts(String provinceId) throws JSONException {
        List<String> districtList = new ArrayList<>();
        districtIds = new ArrayList<>();

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
        }));

    }

    private void showListViewPopup(View anchor, List<String> items, OnItemClickListener listener) {
        if (items.size() == 0) {
            Toast.makeText(getActivity(), "Danh sách trống", Toast.LENGTH_SHORT).show();
            return;
        }

        ListView listView = dialog.findViewById(R.id.list_view_popup);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_seach_address_item,  // Sử dụng layout của bạn
                items
        );
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setVisibility(View.VISIBLE);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                listener.onItemClick(position);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        dialog.show();
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
                districtIds = new ArrayList<>();
                districtId = "";
                fetchDistricts(provinceId);
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
                });
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    interface OnItemClickListener {
        void onItemClick(int position) throws JSONException;
    }
}
