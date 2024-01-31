package ug.go.agriculture.MAAIF_Extension.helper.uploadservice;

import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface FileUploadService {
    @Multipart
    @POST("api.php")
    Single<ResponseBody> onFileUpload(@Part("username") RequestBody mUserName, @Part("email") RequestBody mEmail, @Part MultipartBody.Part file);
}
