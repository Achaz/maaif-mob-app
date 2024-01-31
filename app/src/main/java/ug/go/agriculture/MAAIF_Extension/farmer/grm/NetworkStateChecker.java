package ug.go.agriculture.MAAIF_Extension.farmer.grm;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

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
                Cursor cursor = db.getUnsyncedGrievances();
                if (cursor.moveToFirst()) {
                    do {
                        //calling the method to save the unsynced name to MySQL
                        saveName(
                                cursor.getInt(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("c_district")),
                                cursor.getString(cursor.getColumnIndex("c_subcounty")),
                                cursor.getString(cursor.getColumnIndex("c_parish")),
                                cursor.getString(cursor.getColumnIndex("c_name")),
                                cursor.getString(cursor.getColumnIndex("c_age")),
                                cursor.getString(cursor.getColumnIndex("c_gender")),
                                cursor.getString(cursor.getColumnIndex("c_phone")),
                                cursor.getString(cursor.getColumnIndex("c_feedback")),
                                cursor.getString(cursor.getColumnIndex("c_anonymmous")),
                                cursor.getString(cursor.getColumnIndex("date_of_grivance")),
                                cursor.getString(cursor.getColumnIndex("grievance_nature")),
                                cursor.getString(cursor.getColumnIndex("grivance_type")),
                                cursor.getString(cursor.getColumnIndex("grievance_type_if_not_specified")),
                                cursor.getString(cursor.getColumnIndex("mode_of_receipt_id")),
                                cursor.getString(cursor.getColumnIndex("description")),
                                cursor.getString(cursor.getColumnIndex("past_actions")),
                                cursor.getString(cursor.getColumnIndex("grievance_settle_otherwise_id")),
                                cursor.getString(cursor.getColumnIndex("gps_latitude")),
                                cursor.getString(cursor.getColumnIndex("gps_longitude")),
                                cursor.getString(cursor.getColumnIndex("ref_number")),
                                cursor.getInt(cursor.getColumnIndex("synced"))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    /*
     * method taking two arguments
     * name that is to be saved and id of the name from SQLite
     * if the name is successfully sent
     * we will update the status as synced in SQLite
     * */
    private void saveName(final Integer id, final String district, final String subcounty, final String parish, final String name, final String age,
                          final String gender,  final String phone,  final String feedback, final String anonymmous,
                          final String date_of_grievance,  final String gNature, final String gType, final String gTypeNotListed, final String modeReceipt,
                          final String description, final String past_actions,
                          final String settle_otherwise, final String latitude, final String longitude, final String ref_number, final Integer synced) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Review.URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                String ref = obj.getString("ref");

                                //updating the status in sqlite
                                db.updateGrievanceStatus(id, Review.NAME_SYNCED_WITH_SERVER);
                                db.updateGrievanceRef(id, ref);

                                //sending the broadcast to refresh the list
                                context.sendBroadcast(new Intent(Review.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("complainant_district", district);
                params.put("complainant_subcounty", subcounty);
                params.put("complainant_parish", parish);
                params.put("complainant_name", name);
                params.put("complainant_age", age);
                params.put("complainant_gender", gender);
                params.put("complainant_phone", phone);
                 params.put("complainant_feedback_mode", feedback);
                params.put("complainant_anonymous", anonymmous);
                params.put("date_of_grivance", date_of_grievance);
                params.put("grievance_nature", gNature);
                params.put("grivance_type", gType);
                params.put("grievance_type_if_not_specified", gTypeNotListed);
                params.put("mode_of_receipt", modeReceipt);
                params.put("description", description);
                params.put("past_actions", past_actions);
                 params.put("grievance_settle_otherwise", settle_otherwise);
                params.put("gps_latitude", latitude);
                params.put("gps_longitude", longitude);
                params.put("ref_number", ref_number);

                return params;
            }
        };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

}
