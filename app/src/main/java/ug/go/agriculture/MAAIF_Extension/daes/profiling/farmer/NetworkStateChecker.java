package ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer;

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

import ug.go.agriculture.MAAIF_Extension.home.VolleySingleton;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

/**
 * Created by Herbert Musoke on 1/27/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteHandler db;
    String TAG = "Farmer registration: ";


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
                Log.d("On fetching data from db: ", "Launching");
                //getting all the unsynced names
                Cursor cursor = db.getUnsyncedFarmers();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveToServer(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getInt(cursor.getColumnIndex("synced")),
                                cursor.getString(cursor.getColumnIndex("a1")),
                                cursor.getString(cursor.getColumnIndex("a2")),
                                cursor.getInt(cursor.getColumnIndex("farmer_type")),
                                cursor.getString(cursor.getColumnIndex("gender")),
                                cursor.getString(cursor.getColumnIndex("a5")),
                                cursor.getString(cursor.getColumnIndex("a6")),
                                cursor.getString(cursor.getColumnIndex("a7")),
                                cursor.getInt(cursor.getColumnIndex("education_level")),
                                cursor.getInt(cursor.getColumnIndex("primary_language")),
                                cursor.getInt(cursor.getColumnIndex("secondary_language")),
                                cursor.getInt(cursor.getColumnIndex("district_id")),
                                cursor.getInt(cursor.getColumnIndex("subcounty_id")),
                                cursor.getInt(cursor.getColumnIndex("parish_id")),
                                cursor.getInt(cursor.getColumnIndex("village_id")),
                                cursor.getInt(cursor.getColumnIndex("enterprise_one")),
                                cursor.getInt(cursor.getColumnIndex("enterprise_two")),
                                cursor.getInt(cursor.getColumnIndex("enterprise_three")),
                                cursor.getString(cursor.getColumnIndex("a17")),
                                cursor.getString(cursor.getColumnIndex("a18"))
                                );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void saveToServer(final int id, final int synced, final String a1, final String a2, final int a3, final String a4, final String a5, final String a6,final String a7,final int a8,final int a9, final int a9x, final int a10, final int a11, final int a12, final int a13, final int a14, final int a15, final int a16,final String a17,final String a18) {
        Log.d("Into the sky: ", "Off we go...");
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("a1", a1);
            jsonRequestData.put("a2", a2);
            jsonRequestData.put("a3", a3);
            jsonRequestData.put("a4", a4);
            jsonRequestData.put("a5", a5);
            jsonRequestData.put("a6", a6);
            jsonRequestData.put("a7", a7);
            jsonRequestData.put("a8", a8);
            jsonRequestData.put("a9", a9);
            jsonRequestData.put("a9x", a9x);
            jsonRequestData.put("a10", a10);
            jsonRequestData.put("a11", a11);
            jsonRequestData.put("a12", a12);
            jsonRequestData.put("a13", a13);
            jsonRequestData.put("a14", a14);
            jsonRequestData.put("a15", a15);
            jsonRequestData.put("a16", a16);
            jsonRequestData.put("a17", a17);
            jsonRequestData.put("a18", a18);
            jsonRequestData.put("id", id);

            Log.d("first name", a1);
            Log.d("last name", a2);
            Log.d("farmer category", String.valueOf(a3));
            Log.d("gender", a4);
            Log.d("contact", a5);
            Log.d("email", a6);
            Log.d("nin", a7);
            Log.d("level of education", String.valueOf(a8));
            Log.d("primary language", String.valueOf(a9));
            Log.d("secondary language", String.valueOf(a9x));
            Log.d("district", String.valueOf(a10));
            Log.d("subcounty", String.valueOf(a11));
            Log.d("parish", String.valueOf(a12));
            Log.d("village", String.valueOf(a13));
            Log.d("enterprise one", String.valueOf(a14));
            Log.d("enterprise two", String.valueOf(a15));
            Log.d("enterprise three", String.valueOf(a16));
            Log.d("farmer belongs to group", a17);
            Log.d("farmer group", a18);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer.Review.URL_SAVE_NAME, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");
                            Log.d(TAG, "Server response: " + status);
                            if(status){
                                String failure_reason = response.getString("message");

                                //updating the status in sqlite
                                Log.d("Failure Reason", "Failure reason: " + failure_reason);
                                db.updateSyncedFarmerProfile(id, 2, failure_reason);

                            } else {
                                //updating the status in sqlite
                                db.updateSyncedFarmerProfile(id, Review.NAME_SYNCED_WITH_SERVER, "");
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
