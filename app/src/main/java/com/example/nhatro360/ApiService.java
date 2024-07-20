package com.example.nhatro360;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("https://vapi.vnappmob.com/api/province")
    Call<List<String>> getProvinces();

    @GET("https://vapi.vnappmob.com/api/province/district/{province_id}")
    Call<List<String>> getDistricts(@Query("province_id") String provinceId);

    @GET("https://vapi.vnappmob.com/api/province/ward/{district_id}")
    Call<LocationResponse> getLocationInfo(@Query("lat") double latitude, @Query("lng") double longitude);
}
