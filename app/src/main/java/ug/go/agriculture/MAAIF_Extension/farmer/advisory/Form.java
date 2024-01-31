package ug.go.agriculture.MAAIF_Extension.farmer.advisory;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.farmer.grm.VolleyMultipartRequest;
import ug.go.agriculture.MAAIF_Extension.helper.ImageUploadHelper;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.utils.AppStatus;

public class Form extends AppCompatActivity {
    private Context mContext = Form.this;
    private static final String TAG = Form.class.getSimpleName();
    private Button btnSaveActivity;
    private SQLiteHandler db;
    private SessionManager session;
    String loggedInUserId,outputFileUri;

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_PHOTO = 100;
    private static final int REQUEST_RESULT_OK = 100;
    private ArrayList<String> selectedImages = new ArrayList<>();
    private ProgressDialog pDialog;

    ImageButton btn_attach;
    ImageView back_img, postImage,postImage1, postImage2;
    EditText descriptionEt;
    boolean isAttached;
    TextView chooseText;
    String description;
    ListView lv;
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    RelativeLayout imageContainer;
    TextInputLayout input_layout_description;
    Options options = Options.init()
            .setRequestCode(100)                                           //Request code for activity results
            .setCount(3)                                                   //Number of images to restict selection count
            .setFrontfacing(false)                                         //Front Facing camera on start
            // .setPreSelectedUrls(returnValue)                               //Pre selected Image Urls
            //.setMode()                                       //Option to exclude videos
            .setVideoDurationLimitinSeconds(30)                            //Duration for video recording
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
            .setPath("/pix/images");


    private SimpleLocation mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }

		
		
		// construct a new instance
		mLocation = new SimpleLocation(this);

        db = new SQLiteHandler(getApplicationContext());
        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        loggedInUserId = user.get("uid");


        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_farmer_advisory);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);
//        latitude = (EditText) findViewById(R.id.latitude);
//        longitude = (EditText) findViewById(R.id.longitude);

//        latitude.setText(latitudes+ "  " );
//        longitude.setText(longitudes + " ");


        // Session manager
        session = new SessionManager(getApplicationContext());

        imageContainer = findViewById(R.id.imageContainer);
        //input_layout_description = findViewById(R.id.input_layout_description);

        descriptionEt = findViewById(R.id.farmer_question);

        postImage = findViewById(R.id.imageView);
        postImage1 = findViewById(R.id.imageView1);
        postImage2 = findViewById(R.id.imageView2);
        chooseText = findViewById(R.id.chooseText);

        btn_attach = findViewById(R.id.btn_attach);
        btn_attach.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Pix.start(Form.this, options);
                //requestPermission();
            }
        });



        // Save Planned Activity Click event
        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost();
            }
        });



    }

    private void savePost() {
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
        } else {



            description = descriptionEt.getText().toString();

            if (description.length() == 0) {
                Toast.makeText(this, "question is required", Toast.LENGTH_SHORT).show();
                return;
            }
            String url  = AppConfig.SEND_FARMER_QUESTION ;
            showDialog();
            // ... then
            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String resultResponse = new String(response.data);
                    Log.e("SAVING RESPONSE",resultResponse);
                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(resultResponse);

                        //if no error in response
                        if (!obj.getBoolean("error")) {
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                            hideDialog();
                            //redirect
                            Intent intent = new Intent(mContext, FarmerQuestions.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            //overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                        } else {
                            //Log.e("ERROR IN SAVING",)
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                        }
                    } catch (JSONException e) {
                        hideDialog();
                        e.printStackTrace();

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Questions sending Error: " + error.getMessage());
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("sender", "app");
                    params.put("question", description);
                    params.put("user_id", loggedInUserId);
                    return params;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    // file name could found file base or direct access from real path
                    // for now just get bitmap data from ImageView
                    if(selectedImages.size() > 0){
                        if(selectedImages.size() == 3) {
                            params.put("image_post", new DataPart("file_avatar.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage.getDrawable()), "image/jpeg"));
                            params.put("image_post1", new DataPart("file_avatar1.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage1.getDrawable()), "image/jpeg"));
                            params.put("image_post2", new DataPart("file_avatar2.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage2.getDrawable()), "image/jpeg"));

                        }
                        else if(selectedImages.size() == 2){
                            params.put("image_post", new DataPart("file_avatar.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage.getDrawable()), "image/jpeg"));
                            params.put("image_post1", new DataPart("file_avatar1.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage1.getDrawable()), "image/jpeg"));
                        }
                        else if(selectedImages.size() == 1){
                            params.put("image_post", new DataPart("file_avatar.jpg", ImageUploadHelper.getFileDataFromDrawable(getBaseContext(), postImage.getDrawable()), "image/jpeg"));
                        }
                    }

                    return params;
                }
            };
            multipartRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

          //  VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(multipartRequest, "request_question_send");

        } //end if for connection
    }



    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Main.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_RESULT_OK) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

            imageContainer.setVisibility(View.VISIBLE);
            final int maxSize = 960;
            int outWidth,outWidth2,outWidth3;
            int outHeight,outHeight2,outHeight3;
            switch (returnValue.size()){
                case 0 : break;
                case 1 :
                    try {
                        Uri imageUri1 = Uri.fromFile(new File(returnValue.get(0)));
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri1);
                        int inWidth = bitmap.getWidth();
                        int inHeight = bitmap.getHeight();
                        if(inWidth > inHeight){
                            outWidth = maxSize;
                            outHeight = (inHeight * maxSize) / inWidth;
                        } else {
                            outHeight = maxSize;
                            outWidth = (inWidth * maxSize) / inHeight;
                        }
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
                        postImage.setVisibility(View.VISIBLE);
                        postImage.setImageBitmap(resizedBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    try {
                        Uri imageUri1 = Uri.fromFile(new File(returnValue.get(0)));
                        Uri imageUri2 = Uri.fromFile(new File(returnValue.get(1)));
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri1);
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri2);
                        int inWidth = bitmap.getWidth();
                        int inHeight = bitmap.getHeight();
                        int inWidth2 = bitmap2.getWidth();
                        int inHeight2 = bitmap2.getHeight();
                        if(inWidth > inHeight){
                            outWidth = maxSize;
                            outHeight = (inHeight * maxSize) / inWidth;
                        } else {
                            outHeight = maxSize;
                            outWidth = (inWidth * maxSize) / inHeight;
                        }
                        if(inWidth2 > inHeight2){
                            outWidth2 = maxSize;
                            outHeight2 = (inHeight2 * maxSize) / inWidth2;
                        } else {
                            outHeight2 = maxSize;
                            outWidth2 = (inWidth2 * maxSize) / inHeight2;
                        }
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
                        Bitmap resizedBitmap2 = Bitmap.createScaledBitmap(bitmap2, outWidth2, outHeight2, false);
                        postImage.setVisibility(View.VISIBLE);
                        postImage1.setVisibility(View.VISIBLE);
                        postImage.setImageBitmap(resizedBitmap);
                        postImage1.setImageBitmap(resizedBitmap2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        Uri imageUri1 = Uri.fromFile(new File(returnValue.get(0)));
                        Uri imageUri2 = Uri.fromFile(new File(returnValue.get(1)));
                        Uri imageUri3 = Uri.fromFile(new File(returnValue.get(2)));
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri1);
                        Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri2);
                        Bitmap bitmap3 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri3);
                        int inWidth = bitmap.getWidth();
                        int inHeight = bitmap.getHeight();
                        int inWidth2 = bitmap2.getWidth();
                        int inHeight2 = bitmap2.getHeight();
                        int inWidth3 = bitmap3.getWidth();
                        int inHeight3 = bitmap3.getHeight();
                        if(inWidth > inHeight){
                            outWidth = maxSize;
                            outHeight = (inHeight * maxSize) / inWidth;
                        } else {
                            outHeight = maxSize;
                            outWidth = (inWidth * maxSize) / inHeight;
                        }
                        if(inWidth2 > inHeight2){
                            outWidth2 = maxSize;
                            outHeight2 = (inHeight2 * maxSize) / inWidth2;
                        } else {
                            outHeight2 = maxSize;
                            outWidth2 = (inWidth2 * maxSize) / inHeight2;
                        }
                        if(inWidth3 > inHeight3){
                            outWidth3 = maxSize;
                            outHeight3 = (inHeight3 * maxSize) / inWidth3;
                        } else {
                            outHeight3 = maxSize;
                            outWidth3 = (inWidth3 * maxSize) / inHeight3;
                        }
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
                        Bitmap resizedBitmap2 = Bitmap.createScaledBitmap(bitmap2, outWidth2, outHeight2, false);
                        Bitmap resizedBitmap3 = Bitmap.createScaledBitmap(bitmap3, outWidth3, outHeight3, false);
                        postImage.setVisibility(View.VISIBLE);
                        postImage1.setVisibility(View.VISIBLE);
                        postImage2.setVisibility(View.VISIBLE);
                        postImage.setImageBitmap(resizedBitmap);
                        postImage1.setImageBitmap(resizedBitmap2);
                        postImage2.setImageBitmap(resizedBitmap3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

            }


        }
    }


    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to Settings To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    void requestPermission() {

        if(PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }else {
                //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }else {

            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
                    captureIntent, 0);
            for (ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName,
                        res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                cameraIntents.add(intent);
            }

            final Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_PICK);

// Chooser of filesystem options.
            final Intent chooserIntent = Intent.createChooser(galleryIntent,
                    "Select Image from");

// Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    cameraIntents.toArray(new Parcelable[]{}));
            chooserIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            //          startActivityForResult(chooserIntent, REQUEST_PICK_PHOTO);
            //Permission Granted, lets go pick photo
            // capture picture
            //if everything is ok we will open image chooser
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            //           startActivityForResult(i, REQUEST_PICK_PHOTO);
        }
    }






    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Pix.start(, Options.init().setRequestCode(100));
                    Pix.start(Form.this, options);
                } else {
                    Toast.makeText(Form.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

  //  @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
	
	
	@Override
	protected void onResume() {
		super.onResume();

		// make the device update its location
		mLocation.beginUpdates();
	}

	@Override
	protected void onPause() {
		// stop location updates (saves battery)
		mLocation.endUpdates();

		super.onPause();
	}


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }




}
