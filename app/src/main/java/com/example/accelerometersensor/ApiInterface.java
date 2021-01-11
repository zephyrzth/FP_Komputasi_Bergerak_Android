package com.example.accelerometersensor;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiInterface {
    @POST
    Call<PostPutDelData> storeData(@Url String postUrl, @Body DataActivity body);
}
