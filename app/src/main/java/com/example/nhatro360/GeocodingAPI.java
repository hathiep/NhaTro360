package com.example.nhatro360;

import com.example.nhatro360.models.GeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingAPI {
    @GET("maps/api/geocode/json")
    Call<GeocodingResponse> getGeocoding(
            @Query("address") String address,
            @Query("key") String apiKey
    );
}

