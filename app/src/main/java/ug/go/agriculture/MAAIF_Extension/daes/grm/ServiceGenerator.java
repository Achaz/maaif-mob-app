package ug.go.agriculture.MAAIF_Extension.daes.grm;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import ug.go.agriculture.MAAIF_Extension.BuildConfig;
import ug.go.agriculture.MAAIF_Extension.daes.grm.FileUploadService;

/**
 * Created on : April 13, 2023
 * Author     : Robert Muhereza
 */
public class ServiceGenerator {

    public static FileUploadService createService() {
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
                .baseUrl("https://extension.agriculture.go.ug/")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(FileUploadService.class);
    }
    // .baseUrl(BuildConfig.BASE_URL_GRM)
}
