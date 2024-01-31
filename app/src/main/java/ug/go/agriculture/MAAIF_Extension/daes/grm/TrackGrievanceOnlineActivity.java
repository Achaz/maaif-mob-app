/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.daes.grm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ug.go.agriculture.MAAIF_Extension.R;


public class TrackGrievanceOnlineActivity extends Activity {
    private ProgressBar progressBar;
    private AlertDialog loadingDialog;

    /**
     * WebViewClient subclass loads all hyperlinks in the existing WebView
     */
    public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * WebChromeClient subclass handles UI-related calls
     * Note: think chrome as in decoration, not the Chrome browser
     */
    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }
    }

    WebView mWebView;

    /** Called when the activity is first created. */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        String ref = getIntent().getStringExtra("ref");

        // progressBar = findViewById(R.id.progressBar);

        String url = null;
        try {
            url = "https://extension.agriculture.go.ug/?action=apiTrackGrievance&ref="+ URLEncoder.encode(ref, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();


        //progressBar.setVisibility(View.VISIBLE);  // show the progress bar
        showLoadingDialog();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideLoadingDialog(); // hide dialog
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    // Read data on the worker thread
                    final String responseData = response.body().string();

                    // Parse the data with Gson
                    Gson gson = new Gson();
                    ResponseData responseDataObj = gson.fromJson(responseData, ResponseData.class);

                    // Generate the HTML
                    final String html = generateHtml(responseDataObj);

                    // Run view-related code back on the main thread
                    TrackGrievanceOnlineActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WebView mWebView = (WebView) findViewById(R.id.webView1);
                            mWebView.loadData(html, "text/html", "UTF-8");
                        }
                    });
                }
            }
        });
    }

    private String generateHtml(ResponseData responseData) {
        StringBuilder html = new StringBuilder();
         html.append("<html><head><style>@font-face {font-family: 'Nunito'; src: url('file:///android_asset/font/nunito_regular.ttf');} body {font-family: 'Nunito';}</style></head><body>");
//        html.append("<html><head></head><body>");
        html.append("<h1>").append("Grievance Details").append("</h1>");
        // for (Map.Entry<String, DataItem> entry : responseData.data.entrySet()) {}

        Complainant item = responseData.data.complainant;
        Log.d("Response data Locations", String.valueOf(responseData.data.complainant.parish_id));
        html.append("<h4>").append("Complaint Details").append("</h4>");
        html.append("<p>").append("Complainant name: " + (item.complainant_name != null ? item.complainant_name : "N/A")).append("</p>");
        html.append("<p>").append("Complainant age: " + (item.complainant_age != null ? item.complainant_age : "N/A")).append("</p>");
        html.append("<p>").append("Complainant gender: " + (item.complainant_gender != null ? item.complainant_gender : "N/A")).append("</p>");
        html.append("<p>").append("Complainant phone number: " + (item.complainant_phone != null ? item.complainant_phone : "N/A")).append("</p>");
        if(item.complainant_anonymous == null) {
            html.append("<p>").append("Complainant anonymous: N/A").append("</p>");
        } else if(item.complainant_anonymous.equals("0")) {
            html.append("<p>").append("Complainant anonymous: NO").append("</p>");
        } else if(item.complainant_anonymous.equals("1")) {
            html.append("<p>").append("Complainant anonymous: YES").append("</p>");
        }

        if (responseData.data.location != null) {
            html.append("<h4>").append("Location Details").append("</h4>");
            html.append("<p>").append("District: " + (responseData.data.location.district != null ? responseData.data.location.district : "N/A")).append("</p>");
            html.append("<p>").append("Subcounty: " + (responseData.data.location.subcounty != null ? responseData.data.location.subcounty : "N/A")).append("</p>");
            html.append("<p>").append("Parish: " + (responseData.data.location.parish != null ? responseData.data.location.parish : "N/A")).append("</p>");
        }

        html.append("<h4>").append("About Grievance").append("</h4>");
        html.append("<p>").append("Date: " + (item.date_of_grivance != null ? item.date_of_grivance : "N/A")).append("</p>");
        html.append("<p>").append("Type: " + (responseData.data.grievance_type != null ? responseData.data.grievance_type : "N/A")).append("</p>");
        html.append("<p>").append("Nature: " + (responseData.data.grievance_nature != null ? responseData.data.grievance_nature : "N/A")).append("</p>");
        html.append("<p>").append("Mode of recipient: " + (responseData.data.mode_of_receipt != null ? responseData.data.mode_of_receipt : "N/A")).append("</p>");
        html.append("<p>").append("Is Resolved: " + (responseData.data.is_resolved != null ? responseData.data.is_resolved : "N/A")).append("</p>");
        html.append("<p>").append("Is Settled: " + (responseData.data.grievance_settled != null ? responseData.data.grievance_settled : "N/A")).append("</p>");
        html.append("<p>").append("Description: " + (item.description != null ? item.description : "N/A")).append("</p>");
        html.append("<p>").append("Past Actions: " + (item.past_actions != null ? item.past_actions : "N/A")).append("</p>");
        html.append("<p>").append("GRC Level: " + (responseData.data.grievance_grc_level != null ? responseData.data.grievance_grc_level : "N/A")).append("</p>");
        html.append("<p>").append("Comment: " + (responseData.data.complaint_feedback != null ? responseData.data.complaint_feedback : "N/A")).append("</p>");
        //if (item != null) {}

        html.append("</body></html>");
        return html.toString();
    }

    private static class Location {
        String district;
        String subcounty;
        String parish;
    }

    private static class Complainant {
        String id;
        String parish_id;
        String complainant_name;
        String complainant_age;
        String complainant_gender;
        String complainant_phone;
        String complainant_feedback_mode;
        String complainant_anonymous;
        String date_of_grivance;
        String grievance_nature_id;
        String grivance_type_id;
        String grievance_type_if_not_specified;
        String mode_of_receipt_id;
        String description;
        String past_actions;
        String gps_latitude;
        String gps_longitude;
        String ref_number;
        String days_at_sgrc;
        String days_at_dgrc;
        String days_at_ngrc;
        String grivance_status_id;
        String grievance_settlement_id;
        String date_dgrc;
        String date_ngrc;
        String is_resolved;
        Location location;
    }

    private static class ResponseData {
        String message;
        boolean error;
        Data data;
    }

    private static class Data {
        Complainant complainant;
        String grievance_nature;
        String grievance_type;
        String mode_of_receipt;
        String grievance_grc_level;
        String grievance_settled;
        String complaint_feedback;
        String is_resolved;
        Location location;
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.track_grievance_online_dialog, null);
        builder.setView(view);
        builder.setCancelable(false); // Prevent users from canceling the dialog

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to leave GRM Grivance Tracker Online? ");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(getApplicationContext(),
                        Main.class);
                startActivity(i);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.show();
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
}
