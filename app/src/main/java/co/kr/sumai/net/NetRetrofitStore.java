package co.kr.sumai.net;

import java.util.concurrent.TimeUnit;

import android.content.Context;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetRetrofitStore {
    private static NetRetrofitStore ourInstance;
    private static SumaiService service;
    public static NetRetrofitStore getInstance() {
        return ourInstance;
    }
    private NetRetrofitStore() {
    }
    
    public static void createNetRetrofit(Context context) {
        CookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.sumai.co.kr")
                .addConverterFactory(GsonConverterFactory.create()) // 파싱등록
                .client(okHttpClient)
                .build();

        ourInstance = new NetRetrofitStore();
        service = retrofit.create(SumaiService.class);
    }

    public SumaiService getService() {
        return service;
    }
}