package ug.go.agriculture.MAAIF_Extension.daes.grm;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.ResponseBody;

/**
 * Created on : April 14, 2023
 * Author     : Robert Muhereza
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
