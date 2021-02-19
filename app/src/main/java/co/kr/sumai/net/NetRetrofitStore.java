package co.kr.sumai.net;

import java.util.concurrent.TimeUnit;

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

    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://www.sumai.co.kr")
            .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
            .client(okHttpClient)
            .build();

    SumaiService service = retrofit.create(SumaiService.class);

    public SumaiService getService() {
        return service;
    }
}