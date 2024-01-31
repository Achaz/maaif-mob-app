package ug.go.agriculture.MAAIF_Extension.farmer.outbreaks;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

/**
 * Created by Belal on 1/27/2017.
 */

public class NetworkStateChecker extends BroadcastReceiver {

    //context and database helper object
    private Context context;
    private SQLiteHandler db;


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
                Cursor cursor = db.getUnsyncedOutbreaks();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveName(

                                cursor.getInt(cursor.getColumnIndex("id")),
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
                                cursor.getString(cursor.getColumnIndex("a16")),
                                cursor.getString(cursor.getColumnIndex("a17")),
                                cursor.getString(cursor.getColumnIndex("a18")),
                                cursor.getString(cursor.getColumnIndex("a19")),
                                cursor.getString(cursor.getColumnIndex("a20")),
                                cursor.getString(cursor.getColumnIndex("a21")),
                                cursor.getString(cursor.getColumnIndex("a22")),
                                cursor.getString(cursor.getColumnIndex("a23")),
                                cursor.getString(cursor.getColumnIndex("a24"))

                                );
                    } while (cursor.moveToNext());
                }
            }
        }
    }



    private void saveName(final Integer id,
                          final String a1,final String a2, final String a3, final String a4, final String a5,
                          final String a6, final String a7, final String a8,final  String a9,final  String a10,
                          final String a11,final String a12, final String a13, final String a14, final String a15,
                          final String a16, final String a17, final String a18,final  String a19,final  String a20,
                          final  String a21,final  String a22, final String a23, final String a24

    ) {




        String URL = Review.URL_SAVE_NAME;


        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        JSONObject jsonParams = new JSONObject();
        try {
            //input your API parameters
            jsonParams.put("id", id);
            jsonParams.put("a1", a1);
            jsonParams.put("a2", a2);
            jsonParams.put("a3", a3);
            jsonParams.put("a4", a4);
            jsonParams.put("a5", a5);
            jsonParams.put("a6", a6);
            jsonParams.put("a7", a7);
            jsonParams.put("a8", a8);
            jsonParams.put("a9", a9);
            jsonParams.put("a10", a10);
            jsonParams.put("a11", a11);
            jsonParams.put("a12", a12);
            jsonParams.put("a13", a13);
            jsonParams.put("a14", a14);
            jsonParams.put("a15", a15);
            jsonParams.put("a16", a16);
            jsonParams.put("a17", a17);
            jsonParams.put("a18", a18);
            jsonParams.put("a19", a19);
            jsonParams.put("a20", a20);
            jsonParams.put("a21", a21);
            jsonParams.put("a22", a22);
            jsonParams.put("a23", a23);
            jsonParams.put("a24", a24);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Enter the correct url for your api service site

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            String a21 = response.getString("a21");

                            //updating the status in sqlite
                            db.updateCrisisStatus(id, 1, a21);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //sending the broadcast to refresh the list
                        context.sendBroadcast(new Intent(Review.DATA_SAVED_BROADCAST));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                // database handler
                SQLiteHandler db = new SQLiteHandler(context);

                // Fetching user details from SQLite
               // HashMap<String, String> user = db.getUserAuthDetails();
                String email = "vv@vv.vv";
                String password = "123";

                //Handle HTTP Basic Auth here
                HashMap<String, String> params = new HashMap<String, String>();
                String creds = String.format("%s:%s", email, password);
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);
                params.put("Authorization", auth);
                return params;
            }


        };
        requestQueue.add(jsonObjectRequest);


    }

}
