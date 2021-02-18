package co.kr.sumai;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofitStore {
    private static NetRetrofitStore ourInstance = new NetRetrofitStore();
    public static NetRetrofitStore getInstance() {
        return ourInstance;
    }
    private NetRetrofitStore() {
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.sumai.co.kr")
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .build();

    RetrofitService service = retrofit.create(RetrofitService.class);

    public RetrofitService getService() {
        return service;
    }
}