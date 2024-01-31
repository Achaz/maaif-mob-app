/**
 * Author: Herbert Musoke
 * URL: www.herbertmusoke.com
 * twitter: http://twitter.com/HerbertMusoke
 */
package ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import ug.go.agriculture.MAAIF_Extension.home.VolleySingleton;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer.NetworkStateChecker;

public class Review extends Activity {
    private static final String TAG = Review.class.getSimpleName();
    private BroadcastReceiver myReceiver;
    private NetworkStateChecker networkStateChecker;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private Context context;
    private TextView farmers_count;
    private TextView no_farmers_text;
    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "https://extension.agriculture.go.ug/?action=apiSaveFarmer";
    public static final String URL_SYNC_NEW_EOIs = "https://extension.agriculture.go.ug/?action=apiSaveFarmer";


    private ListView listViewNames;

    //List to store all the farmers
    private List<Farmer> registeredFarmers;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private FarmerAdapter farmerGroupAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        // Register the NetworkStateChecker BroadcastReceiver
        networkStateChecker = new NetworkStateChecker();
        registerReceiver(networkStateChecker, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.farmers_sync);

         // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        registeredFarmers = new ArrayList<>();

        farmers_count = (TextView) findViewById(R.id.registered_farmers_count);
        no_farmers_text = (TextView) findViewById(R.id.no_farmers_text);
        listViewNames = (ListView) findViewById(R.id.listViewNames);

        //calling the method to load all the stored farmers
        loadFarmers();

        //the broadcast receiver to update sync status
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //loading the farmers again
                loadFarmers();
            }
        };

        //registering the broadcast receiver to update sync status
        IntentFilter intentFilter = new IntentFilter(DATA_SAVED_BROADCAST);
        registerReceiver(myReceiver, intentFilter);
    }

    private void showMessageDialog(String msgType, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msgType)
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
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


    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Main.class);
        startActivity(i);
        finish();
    }

    /*
     * this method will
     * load the farmers from the database
     * with updated sync status
     * */
    private void loadFarmers() {
        registeredFarmers.clear();
        Cursor cursor = db.getFarmers();
        int count = cursor.getCount();
        farmers_count.setText("Farmers count: " + String.valueOf(count));

        if (count == 0) {
            no_farmers_text.setText("No farmers registered yet...");
        } else {
            no_farmers_text.setVisibility(View.GONE);
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") Farmer farmerGroup = new Farmer(
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
                            cursor.getString(cursor.getColumnIndex("a16")),
                            cursor.getString(cursor.getColumnIndex("a17")),
                            cursor.getString(cursor.getColumnIndex("a18")),
                            cursor.getString(cursor.getColumnIndex("a9x")),
                            cursor.getString(cursor.getColumnIndex("reason"))
                    );
                    registeredFarmers.add(farmerGroup);
                } while (cursor.moveToNext());
            }

            farmerGroupAdapter = new FarmerAdapter(this, R.layout.farmer_data, registeredFarmers);
            listViewNames.setAdapter(farmerGroupAdapter);
        }
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        farmerGroupAdapter.notifyDataSetChanged();
    }

    /*
     * this method is saving the name to the server
     * */
    private void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Farmer...");
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

        // Unregister the NetworkStateChecker BroadcastReceiver
        unregisterReceiver(networkStateChecker);

        // Unregister the other BroadcastReceiver (myReceiver)
        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
            myReceiver = null;
        }
    }


}
