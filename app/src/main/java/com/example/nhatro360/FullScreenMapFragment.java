package com.example.nhatro360;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FullScreenMapFragment extends Fragment {

    private GoogleMap mMap;
    private LatLng markerPosition;

    public FullScreenMapFragment() {
        // Required empty public constructor
    }

    public static FullScreenMapFragment newInstance(LatLng position) {
        FullScreenMapFragment fragment = new FullScreenMapFragment();
        Bundle args = new Bundle();
        args.putParcelable("marker_position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            markerPosition = getArguments().getParcelable("marker_position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_screen_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.full_screen_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    if (markerPosition != null) {
                        mMap.addMarker(new MarkerOptions().position(markerPosition).title("Marker Position"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPosition, 15));
                    }
                }
            });
        }
    }
}
