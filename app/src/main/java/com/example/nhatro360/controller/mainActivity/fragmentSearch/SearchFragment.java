package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.nhatro360.R;
import com.example.nhatro360.models.Room;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private EditText edtSearch;
    private ImageView imvFilter;
    private FragmentSingleListRoom fragmentSingleListRoom;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        edtSearch = view.findViewById(R.id.edt_search);
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
                transaction.replace(R.id.fragment_container, fragmentSingleListRoom);
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
}
