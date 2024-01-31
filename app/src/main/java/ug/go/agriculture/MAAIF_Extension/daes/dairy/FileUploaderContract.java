package ug.go.agriculture.MAAIF_Extension.daes.dairy;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

/**
 * Created on : Dec 30, 2021
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */
public class FileUploaderContract {
    public interface View {
        void showErrorMessage(String message);

        void uploadCompleted();

        void setUploadProgress(int progress);
    }

    interface Presenter {
        void onFileSelected(String selectedFile, String userName, String email);

        void onFileSelectedWithoutShowProgress(String selectedFilePath, String userName, String email);

        void cancel();
    }

    interface Model {
        Flowable<Double> uploadFile(String selectedFilePath, String userName, String email);

        Single<ResponseBody> uploadFileWithoutProgress(String filePath, String userName, String email);
    }
}
