package ug.go.agriculture.MAAIF_Extension.daes.grm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

/**
 * Created by Belal on 1/27/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteHandler db;
    private static final String TAG = "EGRM Review";

    @SuppressLint("Range")
    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        db = new SQLiteHandler(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //if there is a network
        if (activeNetwork != null) {
            //if connected to wifi or mobile data plan
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //getting all the unsynced names
                Cursor cursor = db.getUnsyncedGrievances();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveGrievanceToServer(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("a1")),
                                cursor.getString(cursor.getColumnIndex("a2")),
                                cursor.getString(cursor.getColumnIndex("a3")),
                                cursor.getString(cursor.getColumnIndex("a5")),
                                cursor.getString(cursor.getColumnIndex("a6")),
                                cursor.getString(cursor.getColumnIndex("a7")),
                                cursor.getString(cursor.getColumnIndex("a8")),
                                cursor.getString(cursor.getColumnIndex("a9")),
                                cursor.getString(cursor.getColumnIndex("a10")),
                                cursor.getString(cursor.getColumnIndex("a11")),
                                cursor.getString(cursor.getColumnIndex("a12")),
                                cursor.getString(cursor.getColumnIndex("a13")),
                                cursor.getString(cursor.getColumnIndex("a14")),
                                cursor.getString(cursor.getColumnIndex("a15")),
                                cursor.getString(cursor.getColumnIndex("a16")),
                                cursor.getString(cursor.getColumnIndex("a17")),
                                cursor.getString(cursor.getColumnIndex("a18")),
                                cursor.getString(cursor.getColumnIndex("a19")),
                                cursor.getString(cursor.getColumnIndex("a20")),
                                cursor.getString(cursor.getColumnIndex("a21")),
                                cursor.getInt(cursor.getColumnIndex("synced"))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    /*
     * method taking 23 arguments
     * if the grievance is successfully sent
     * we will update the status as synced in SQLite
     * */

    private void saveGrievanceToServer(final Integer id, final String district, final String subCounty, final String parish, final String name, final String age,
                          final String gender, final String phone, final String feedback, final String anonymous,
                          final String dateOfGrievance, final String grievanceNature, final String grievanceType, final String grievanceTypeNotListed, final String modeOfReceipt,
                          final String description, final String pastActions,
                          final String settleOtherwise, final String latitude, final String longitude, final String refNumber, final Integer synced) {

        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("complainant_parish", parish);
            jsonRequestData.put("complainant_name", name);
            jsonRequestData.put("complainant_age", age);
            jsonRequestData.put("complainant_gender", gender);
            jsonRequestData.put("complainant_phone", phone);
            jsonRequestData.put("complainant_feedback_mode", feedback);
            jsonRequestData.put("complainant_anonymous", anonymous);
            jsonRequestData.put("date_of_grivance", dateOfGrievance);
            jsonRequestData.put("grievance_nature", grievanceNature);
            jsonRequestData.put("grivance_type", grievanceType);
            jsonRequestData.put("grievance_type_if_not_specified", grievanceTypeNotListed);
            jsonRequestData.put("mode_of_receipt", modeOfReceipt);
            jsonRequestData.put("description", description);
            jsonRequestData.put("past_actions", pastActions);
            jsonRequestData.put("grievance_settle_otherwise", settleOtherwise);
            jsonRequestData.put("gps_latitude", latitude);
            jsonRequestData.put("gps_longitude", longitude);
            jsonRequestData.put("ref_number", refNumber);
            // ... add all your other parameters
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Review.URL_SAVE_NAME, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");

                            Log.d(TAG, "Message: " + response.getString("message"));
                            if(status){
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(response.getString("errors"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d("JSONExample", "Error parsing JSON array string: " + e.getMessage());
                                }

                                if (jsonArray != null) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        try {
                                            String name = jsonArray.getString(i);
                                            Log.d("JSONExample", "Error at index " + i + ": " + name);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.d("JSONExample", "Error getting element at index " + i + ": " + e.getMessage());
                                        }
                                    }
                                }

                            } else {
                                String grievance_id = response.getString("grievance_id");
                                String grievance_reference = response.getString("ref_number");

                                //updating the status in sqlite
                                db.updateGrievance(id, Review.NAME_SYNCED_WITH_SERVER, grievance_id, grievance_reference);
                                // updating reference number in DB
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error parsing JSON response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error: " + error);
                    }
                }) {
            // If you need to add headers, override the getHeaders() method
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

}
