package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created on : Dec 30, 2021
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */

public interface FileUploadService {

   // @Headers("Authorization: Basic bWVAaGVyYmVydG11c29rZS5jb206MTIz")
    @Multipart
    @POST("uploadOutbreaksPhoto.php")
    Single<ResponseBody> onFileUpload(@Header ("Authorization") String header, @Part("id") RequestBody mUserName, @Part("name") RequestBody mEmail, @Part MultipartBody.Part file);
}
