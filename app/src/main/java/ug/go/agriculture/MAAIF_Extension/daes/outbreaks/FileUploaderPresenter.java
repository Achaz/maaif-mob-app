package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

import android.text.TextUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created on : Dec 30, 2021
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */
public class FileUploaderPresenter implements FileUploaderContract.Presenter {

    private final FileUploaderContract.Model model;
    private final FileUploaderContract.View view;

    private Disposable videoUploadDisposable;

    public FileUploaderPresenter(FileUploaderContract.View view, FileUploaderContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onFileSelected(String selectedFilePath, String id_str, String photoDescription) {
        if (TextUtils.isEmpty(selectedFilePath)) {
            view.showErrorMessage("No Photo Selected");
            return;
        }
        videoUploadDisposable = model.uploadFile(selectedFilePath, id_str, photoDescription)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        progress -> view.setUploadProgress((int) (100 * progress)),
                        error -> view.showErrorMessage(error.getMessage()),
                        () -> view.uploadCompleted()
                );
    }

    @Override
    public void onFileSelectedWithoutShowProgress(String selectedFilePath, String userName, String email) {
        if (TextUtils.isEmpty(selectedFilePath)) {
            view.showErrorMessage("No Photo Selected");
            return;
        }
        videoUploadDisposable = model.uploadFileWithoutProgress(selectedFilePath, userName, email)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> view.uploadCompleted(),
                        error -> view.showErrorMessage(error.getMessage())
                );
    }

    @Override
    public void cancel() {
        if (videoUploadDisposable != null && !videoUploadDisposable.isDisposed()) {
            videoUploadDisposable.dispose();
        }
    }
}
