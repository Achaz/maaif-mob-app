/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.daes.grm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;

public class Review extends Activity {
    private static final String TAG = Review.class.getSimpleName();
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private TextView grievance_count;
    private TextView no_grievances_text;

    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "https://extension.agriculture.go.ug/api/saveGrievances.php";

    private ListView listViewNames;

    //List to store all the names
    private List<NameGRM> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    // adapter-object for list view
    private NameAdapterGRM nameAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Handle the received broadcast
            }
        };
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.grievances_sync);

         // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        names = new ArrayList<>();

        grievance_count = (TextView) findViewById(R.id.registered_grievances_count);
        no_grievances_text = (TextView) findViewById(R.id.no_grievances_text);

        listViewNames = (ListView) findViewById(R.id.listViewNames);

        //calling the method to load all the stored names
        loadGrievances();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadGrievances();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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


    @Override public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Main.class);
        startActivity(i);
        finish();
    }

    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */
    private void loadGrievances() {
        names.clear();
        Cursor cursor = db.getGrievances();
        int count = cursor.getCount();
        grievance_count.setText("Grievances count: " + String.valueOf(count));

        if (count == 0) {
            no_grievances_text.setText("No grievances registered yet...");
        } else {
            no_grievances_text.setVisibility(View.GONE);
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") NameGRM name = new NameGRM(
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
                    names.add(name);
                } while (cursor.moveToNext());
            }

            nameAdapter = new NameAdapterGRM(this, R.layout.names, names);
            listViewNames.setAdapter(nameAdapter);
        }
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
    }

    /*
     * this method is saving the name to the server
     * */
    private void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Name...");
        progressDialog.show();

        //final String name = editTextName.getText().toString().trim();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                //saveNameToLocalStorage(name, NAME_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                //saveNameToLocalStorage(name, NAME_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //on error storing the name to sqlite with status unsynced
                        //saveNameToLocalStorage(name, NAME_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
               // params.put("name", activity_id);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    // @Override
    public void onClick(View view) {
        saveNameToServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

}
