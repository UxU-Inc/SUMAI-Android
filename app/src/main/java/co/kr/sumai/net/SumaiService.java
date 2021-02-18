package co.kr.sumai.net;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SumaiService {
    @POST("/api/account/login")
    Call<LoginResponse> postLogin(@Body LoginRequest loginRequest);

    @GET("/api/account/getinfo")
    Call<LoginInforResponse> getInfo();
}

