package co.kr.sumai.net;

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

    SumaiService service = retrofit.create(SumaiService.class);

    public SumaiService getService() {
        return service;
    }
}