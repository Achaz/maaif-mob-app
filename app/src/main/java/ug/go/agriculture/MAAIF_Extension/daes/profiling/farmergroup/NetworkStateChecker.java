package ug.go.agriculture.MAAIF_Extension.daes.profiling.farmergroup;

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
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.home.VolleySingleton;
//import ug.go.agriculture.MAAIF_Extension.daes.profiling.farmergroup.Review;

/**
 * Created by Herbert Musoke on 1/27/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteHandler db;
    private static final String TAG = "Farmer group profiling";


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
                Cursor cursor = db.getUnsyncedFarmerGroups();
                Log.d(TAG, "launching...");
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveToServer(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getInt(cursor.getColumnIndex("parish_id")),
                                cursor.getInt(cursor.getColumnIndex("enterprise_id")),
                                cursor.getString(cursor.getColumnIndex("year_of_establishment")),
                                cursor.getString(cursor.getColumnIndex("a1")),
                                cursor.getString(cursor.getColumnIndex("a2")),
                                cursor.getString(cursor.getColumnIndex("a3")),
                                cursor.getString(cursor.getColumnIndex("a4")),
                                cursor.getString(cursor.getColumnIndex("a5")),
                                cursor.getString(cursor.getColumnIndex("a6")),
                                cursor.getString(cursor.getColumnIndex("a12")),
                                cursor.getString(cursor.getColumnIndex("a13")),
                                cursor.getString(cursor.getColumnIndex("a14")),
                                cursor.getString(cursor.getColumnIndex("a15")),
                                cursor.getString(cursor.getColumnIndex("a16"))
                                );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void saveToServer(final int id, final int parish_id, final int enterprise_id, final String year_of_establishment,
                              final String a1, final String a2, final String a3, final String a4, final String a5, final String a6, final String a12, final String a13, final String a14, final String a15, final String a16) {
        Log.d(TAG, "reached and yet to conquer...");
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("farmer_group", a1);
            jsonRequestData.put("category", a2);
            jsonRequestData.put("level_of_operation", a3);
            jsonRequestData.put("parish_id", String.valueOf(parish_id));
            jsonRequestData.put("establishment_year", year_of_establishment);
            jsonRequestData.put("contact_person_name", a6);
            jsonRequestData.put("latitude", a15);
            jsonRequestData.put("longitude", a16);
            jsonRequestData.put("contact_person_email", a4);
            jsonRequestData.put("contact_person_phone_number", a5);
            jsonRequestData.put("number_of_members", a14);
            jsonRequestData.put("is_group_registered", a12);
            jsonRequestData.put("registration_id", a13);
            jsonRequestData.put("enterprise", String.valueOf(enterprise_id));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }
        Log.d(TAG, "almost there...");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Review.URL_SAVE_NAME, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");
                            Log.d(TAG, response.getString("message"));
                            if(status){
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = new JSONArray(response.getString("message"));
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
                                //updating the status in sqlite
                                db.updateSyncedFarmerGroup(id, Review.NAME_SYNCED_WITH_SERVER);
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
