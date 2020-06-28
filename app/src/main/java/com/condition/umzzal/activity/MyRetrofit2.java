package com.condition.umzzal.activity;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyRetrofit2 {
   public static final String URL = "http:///";
    static Retrofit mRetrofit;

    public static Retrofit getRetrofit2(){

        if(mRetrofit == null){

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


            httpClient.connectTimeout(5, TimeUnit.MINUTES)
                    .writeTimeout(5,TimeUnit.MINUTES)
                    .readTimeout(5,TimeUnit.MINUTES)
                    .addInterceptor(logging)
                    .build();


            mRetrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();


        }
        return mRetrofit;
    }
}

