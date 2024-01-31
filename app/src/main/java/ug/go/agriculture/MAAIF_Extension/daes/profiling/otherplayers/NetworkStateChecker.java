package ug.go.agriculture.MAAIF_Extension.daes.profiling.otherplayers;

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
import ug.go.agriculture.MAAIF_Extension.daes.profiling.otherplayers.Review;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

/**
 * Created by Robert Muhereza
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteHandler db;
    private static final String TAG = "Other market player profiling...";


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
                Cursor cursor = db.getUnsyncedOtherMarketPlayers();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveToServer(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getInt(cursor.getColumnIndex("synced")),
                                cursor.getString(cursor.getColumnIndex("a1")),
                                cursor.getString(cursor.getColumnIndex("a2")),
                                cursor.getString(cursor.getColumnIndex("a3")),
                                cursor.getString(cursor.getColumnIndex("a4")),
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
                                cursor.getString(cursor.getColumnIndex("a16"))
                                );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void saveToServer(final int id, final int synced,
                              final String a1, final String a2, final String a3, final String a4, final String a5, final String a6,final String a7,final String a8, final String a9,final String a10,
                              final String a11, final String a12, final String a13, final String a14, final String a15, final String a16) {

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
            jsonRequestData.put("a10", a10);
            jsonRequestData.put("a11", a11);
            jsonRequestData.put("a12", a12);
            jsonRequestData.put("a13", a13);
            jsonRequestData.put("a14", a14);
            jsonRequestData.put("a15", a15);
            jsonRequestData.put("a16", a16);
            // ... add all your other parameters
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, ug.go.agriculture.MAAIF_Extension.daes.profiling.otherplayers.Review.URL_SAVE_NAME, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");
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
                                String other_market_player_id = response.getString("a19");
                                Log.d(TAG, "Other market player id " + other_market_player_id);

                                //updating the status in sqlite
                                db.updateSyncedOtherMarketPlayer(id, ug.go.agriculture.MAAIF_Extension.daes.profiling.otherplayers.Review.NAME_SYNCED_WITH_SERVER, other_market_player_id);
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
