package ug.go.agriculture.MAAIF_Extension.daes.dairy;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import ug.go.agriculture.MAAIF_Extension.BuildConfig;

/**
 * Created on : Dec 30, 2021
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */
public class ServiceGenerator {
    public static ug.go.agriculture.MAAIF_Extension.daes.dairy.FileUploadService createService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES).addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = requestBuilder = original.newBuilder()
                            .method(original.method(), original.body());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }).build();

        return new Retrofit
                .Builder()
                .baseUrl(BuildConfig.BASE_URL_EDIARY)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ug.go.agriculture.MAAIF_Extension.daes.dairy.FileUploadService.class);
    }
}
