package com.copy.jianshuapp.modellayer.remote;

import com.copy.jianshuapp.JSApi;
import com.copy.jianshuapp.common.FileManager;
import com.copy.jianshuapp.common.VersionChannel;
import com.copy.jianshuapp.modellayer.remote.api.SubscriptionApi;
import com.copy.jianshuapp.modellayer.remote.api.TrendApi;
import com.copy.jianshuapp.modellayer.remote.api.UserApi;
import com.copy.jianshuapp.modellayer.remote.api.mock.MockSubscriptionApi;
import com.copy.jianshuapp.modellayer.remote.api.mock.MockTrendApi;
import com.copy.jianshuapp.modellayer.remote.api.mock.MockUserApi;
import com.copy.jianshuapp.modellayer.remote.interceptors.LoggingInterceptor;
import com.copy.jianshuapp.modellayer.remote.interceptors.UnifiedParameterInterceptor;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.CustomGsonConverterFactory;

/**
 * 远程接口管理
 * @version imkarl 2016-08
 */
public class RemoteManager {
    private static RemoteManager INSTANCE;

    public static RemoteManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RemoteManager();
        }
        return INSTANCE;
    }


    public static final String API_HOST_URL = JSApi.API_HOST;

    private static final long RESPONSE_CACHE_SIZE = 10 * 1024 * 1024;
    private static final long HTTP_CONNECT_TIMEOUT = 10;
    private static final long HTTP_READ_TIMEOUT = 10;
    private static final long HTTP_WRITE_TIMEOUT = 10;

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final Retrofit retrofit;

    private RemoteManager() {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new UnifiedParameterInterceptor());
        if (!VersionChannel.isStable()) {
            httpClientBuilder.addInterceptor(new LoggingInterceptor());
            httpClientBuilder.hostnameVerifier((hostname, session) -> true);
        }
        if (!VersionChannel.isUnitTest()) {
            httpClientBuilder.cache(new Cache(getHttpCacheDir(), RESPONSE_CACHE_SIZE));

            if (VersionChannel.isDebug()) {
                httpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
            }
        }
        httpClient = httpClientBuilder.build();

        gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(API_HOST_URL)
                .client(httpClient)
                .addConverterFactory(CustomGsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        retrofit = retrofitBuilder.build();

        // TODO 支持缓存功能
//        CacheUtils.isDebug(false);
//        CacheUtils.init(DataLayer.getContext(), getKakaCacheDir());
    }

    private Map<Class, Object> mockApi = new HashMap<Class, Object>() {
        {
            put(UserApi.class, new MockUserApi());
            put(TrendApi.class, new MockTrendApi());
            put(SubscriptionApi.class, new MockSubscriptionApi());
        }
    };
    public <T> T createApi(Class<T> apiClass) {
        if (mockApi.containsKey(apiClass)) {
            return (T) mockApi.get(apiClass);
        }

        return retrofit.create(apiClass);
    }

    public Gson gson() {
        return gson;
    }

    public File getHttpCacheDir() {
        return FileManager.getHttpCacheDir("okhttp");
    }
    public File getKakaCacheDir() {
        return FileManager.getHttpCacheDir("kaka");
    }

}
