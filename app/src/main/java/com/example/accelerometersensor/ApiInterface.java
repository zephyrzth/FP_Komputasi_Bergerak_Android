package com.example.accelerometersensor;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {
    @POST("map/post")
    Call<PostPutDelData> storeData(@Body DataActivity body);
}
