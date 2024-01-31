package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

/**
 * Created on : Dec 1, 2022
 * Author     : Herbert Musoke
 * Website    : https://twitter.com/HerbertMusoke
 */

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ug.go.agriculture.MAAIF_Extension.BuildConfig;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Review;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.picker.ImageContract;
import ug.go.agriculture.MAAIF_Extension.picker.ImagePresenter;
import ug.go.agriculture.MAAIF_Extension.utils.CommonUtils;
import ug.go.agriculture.MAAIF_Extension.utils.FileCompressor;

public class PhotoUpload extends AppCompatActivity implements ImageContract.View, FileUploaderContract.View {

    static final int REQUEST_TAKE_PHOTO = 1001;
    static final int REQUEST_GALLERY_PHOTO = 1002;
    static String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    @BindView(R.id.user_profile_photo)
    ImageButton userProfilePhoto;
    @BindView(R.id.textViewProgress)
    TextView txtProgress;
    @BindView(R.id.upload_file_progress)
    Button uploadFileProgress;

    private ImagePresenter mImagePresenter;
    private FileUploaderPresenter mUploaderPresenter;
    private FileCompressor mCompressor;

    File mPhotoFile;

    private EditText photoType;
    private SQLiteHandler db;

    String id;
    String a1;
    String a2;
    String a3;
    String a4;
    String a5;
    String a6;
    String a7;
    String a8;
    String a9;
    String a10;
    String a11;
    String a12;
    String a13;
    String a14;
    String a15;
    String a16;
    String a17;
    String a18;
    String a19;
    String a20;
    String a21;
    String a22;
    String a23;
    String a24;

    ImageView imageViewStatus1;
    ImageView imageViewStatus5;
    ImageView imageViewStatus9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        id = (String) intent.getStringExtra("id");
        a1 = intent.getStringExtra("a1");
        a2 = intent.getStringExtra("a2");
        a3 = intent.getStringExtra("a3");
        a4 = intent.getStringExtra("a4");
        a5 = intent.getStringExtra("a5");
        a6 = intent.getStringExtra("a6");
        a7 = intent.getStringExtra("a7");
        a8 = intent.getStringExtra("a8");
        a9 = intent.getStringExtra("a9");
        a10 = intent.getStringExtra("a10");
        a11 = intent.getStringExtra("a11");
        a12 = intent.getStringExtra("a12");
        a13 = intent.getStringExtra("a13");
        a14 = intent.getStringExtra("a14");
        a15 = intent.getStringExtra("a15");
        a16 = intent.getStringExtra("a16");
        a17 = intent.getStringExtra("a17");
        a18 = intent.getStringExtra("a18");
        a19 = intent.getStringExtra("a19");
        a20 = intent.getStringExtra("a20");
        a21 = intent.getStringExtra("a21");
        a22 = intent.getStringExtra("a22");
        a23 = intent.getStringExtra("a23");
        a24 = intent.getStringExtra("a24");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload_outbreak);

        photoType = (EditText) findViewById(R.id.photoType);
        TextView name = (TextView) findViewById(R.id.editTextName);
        TextView phone = (TextView) findViewById(R.id.phone);
        TextView subcounty =(TextView) findViewById(R.id.subcounty);
        TextView parish = (TextView) findViewById(R.id.parish);
        TextView village = (TextView) findViewById(R.id.status);
        TextView spousename = (TextView) findViewById(R.id.spousename);
        TextView xgender = (TextView) findViewById(R.id.xgender);
        TextView xhasspouse = (TextView) findViewById(R.id.xhaspouse);
        TextView spousephone = (TextView) findViewById(R.id.spousephone);
        TextView ref = (TextView) findViewById(R.id.ref);

        name.setText(a9);
        subcounty.setText("Subcounty/Parish: " + a2+ "/" + a3);
        xgender.setText("District : "+ a1);
        phone.setText("Village: " + a4);
        parish.setText("Entreprize: " + a7);
        spousename.setVisibility(View.GONE);
        ref.setText("Reference Contact : " + a12);
        xhasspouse.setVisibility(View.GONE);
        spousephone.setText("Reference: "+ a11);
        village.setVisibility(View.GONE);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        ButterKnife.bind(this);
        mImagePresenter = new ImagePresenter(this);
        mUploaderPresenter = new FileUploaderPresenter(this, new FileUploaderModel(ServiceGenerator.createService(),getApplicationContext()));
        mCompressor = new FileCompressor(this);
    }

    @OnClick({R.id.user_profile_photo, R.id.upload_file_progress})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_profile_photo:
                selectImage();
                break;
            case R.id.upload_file_progress:
                String photoCategory =  photoType.getText().toString().trim();
                mUploaderPresenter.onFileSelected(mImagePresenter.getImage(), a21, photoCategory);
                break;
            default:
                break;
        }
    }

    private void selectImage() {
        txtProgress.setText("");
        final CharSequence[] items = {getString(R.string.take_photo), getString(R.string.choose_gallery),
                getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoUpload.this);
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Capture Photo")) {
                mImagePresenter.cameraClick();
            } else if (items[item].equals("Choose from Library")) {
                mImagePresenter.chooseGalleryClick();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public boolean checkPermission() {
        for (String mPermission : permissions) {
            int result = ActivityCompat.checkSelfPermission(this, mPermission);
            if (result == PackageManager.PERMISSION_DENIED) return false;
        }
        return true;
    }

    @Override
    public void showPermissionDialog(boolean isGallery) {
        Dexter.withActivity(this).withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            if (isGallery) {
                                mImagePresenter.chooseGalleryClick();
                            } else {
                                mImagePresenter.cameraClick();
                            }
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(error -> showErrorDialog())
                .onSameThread()
                .check();
    }

    @Override
    public File getFilePath() {
        return getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    }

    @Override
    public void openSettings() {

    }

    @Override
    public void startCamera(File file) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (file != null) {
                Uri mPhotoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                mPhotoFile = file;
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    @Override
    public void chooseGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    File resultedFile = mCompressor.compressToFile(mPhotoFile);
                    mImagePresenter.saveImage(resultedFile.getPath());
                    mImagePresenter.showPreview(resultedFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == REQUEST_GALLERY_PHOTO) {
                Uri selectedImage = data.getData();
                try {
                    File resultedFile = mCompressor.compressToFile(new File(Objects.requireNonNull(getRealPathFromUri(selectedImage))));
                    mImagePresenter.saveImage(resultedFile.getPath());
                    mImagePresenter.showPreview(resultedFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public File newFile() {
        Calendar cal = Calendar.getInstance();
        long timeInMillis = cal.getTimeInMillis();
        String mFileName = String.valueOf(timeInMillis) + ".jpeg";
        File mFilePath = getFilePath();
        try {
            File newFile = new File(mFilePath.getAbsolutePath(), mFileName);
            newFile.createNewFile();
            return newFile;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void showErrorDialog() {
        Toast.makeText(getApplicationContext(), getString(R.string.error_message), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayImagePreview(File mFile) {
        Glide.with(PhotoUpload.this).load(mFile).apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.camera)).into(userProfilePhoto);
    }


    @Override
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_need_permission));
        builder.setMessage(getString(R.string.message_grant_permission));
        builder.setPositiveButton(getString(R.string.label_setting), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void uploadCompleted() {
        CommonUtils.hideLoading();
        Toast.makeText(getApplicationContext(), getString(R.string.file_upload_successful), Toast.LENGTH_LONG).show();


        Intent i = new Intent(getApplicationContext(),
                Review.class);
        startActivity(i);
        finish();
    }

    @Override
    public void setUploadProgress(int progress) {

        txtProgress.setText("File Uploading ..." + String.valueOf(progress) +"%");
    }


}
