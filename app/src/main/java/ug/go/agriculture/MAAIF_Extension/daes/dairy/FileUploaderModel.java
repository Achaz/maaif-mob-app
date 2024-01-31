package ug.go.agriculture.MAAIF_Extension.daes.dairy;

/**
 * Created on : Dec 30, 2021
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */

import android.content.Context;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;

import java.io.File;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ug.go.agriculture.MAAIF_Extension.network.CountingRequestBody;

/**
 * Created on : Dec 30, 2018
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class FileUploaderModel implements FileUploaderContract.Model {
    private final ug.go.agriculture.MAAIF_Extension.daes.dairy.FileUploadService service;
    private Context context;
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    public FileUploaderModel(ug.go.agriculture.MAAIF_Extension.daes.dairy.FileUploadService service, Context context) {
        this.service = service;
        this.context = context;
    }

    /**
     * Create request body for image resource
     * @param file
     * @return
     */
    private RequestBody createRequestForImage(File file) {
        return RequestBody.create(MediaType.parse("image/*"), file);
    }

    /**
     * Create request body for video resource
     * @param file
     * @return
     */
    private RequestBody createRequestForVideo(File file) {
        return RequestBody.create(MediaType.parse("video/*"), file);
    }

    /**
     * Create request body for string
     *
     * @param descriptionString
     * @return
     */
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

    /**
     * return multipart part request body
     * @param filePath
     * @return
     */
    private MultipartBody.Part createMultipartBody(String filePath) {
        File file = new File(filePath);
        RequestBody requestBody = createRequestForImage(file);
        return MultipartBody.Part.createFormData("image", file.getName(), requestBody);
    }

    /**
     * return multi part body in format of FlowableEmitter
     * @param filePath
     * @param emitter
     * @return
     */
    private MultipartBody.Part createMultipartBody(String filePath, FlowableEmitter<Double> emitter) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("image", file.getName(), createCountingRequestBody(file, emitter));
    }

    private RequestBody createCountingRequestBody(File file, FlowableEmitter<Double> emitter) {
        RequestBody requestBody = createRequestForImage(file);
        return new CountingRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (1.0 * bytesWritten) / contentLength;
            emitter.onNext(progress);
        });
    }



    @Override
    public Flowable<Double> uploadFile(String selectedFile, String username, String email) {
        RequestBody mUserName = createPartFromString(username);
        RequestBody mEmail = createPartFromString(email);
        return Flowable.create(emitter -> {
            try {
                // database handler
//                SQLiteHandler db = new SQLiteHandler(context);

//                // Fetching user details from SQLite
//                HashMap<String, String> user = db.getUserAuthDetails();
//                String emailx = user.get("email");
//                String passwordx = user.get("password");
//
//                //Handle HTTP Basic Auth here
//                HashMap<String, String> params = new HashMap<String, String>();
//                String creds = String.format("%s:%s", emailx, passwordx);
//                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);

                 String auth = "Enable Auth Next Time.";

                ResponseBody response = service.onFileUpload(auth, mUserName, mEmail, createMultipartBody(selectedFile, emitter)).blockingGet();
                emitter.onComplete();
            } catch (Exception e) {
                emitter.tryOnError(e);
            }
        }, BackpressureStrategy.LATEST);
    }

    @Override
    public Single<ResponseBody> uploadFileWithoutProgress(String filePath, String username, String email) {
        RequestBody mUserName = createPartFromString(username);
        RequestBody mEmail = createPartFromString(email);

//        // database handler
//        SQLiteHandler db = new SQLiteHandler(context);
//
//        // Fetching user details from SQLite
//        HashMap<String, String> user = db.getUserAuthDetails();
//        String emailx = user.get("email");
//        String passwordx = user.get("password");
//
//        //Handle HTTP Basic Auth here
//        HashMap<String, String> params = new HashMap<String, String>();
//        String creds = String.format("%s:%s", emailx, passwordx);
//        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);

        String auth = "Enable Auth Next Time.";




        return service.onFileUpload(auth,mUserName, mEmail, createMultipartBody(filePath));
    }
}