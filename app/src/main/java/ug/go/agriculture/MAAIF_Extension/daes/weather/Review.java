/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.daes.weather;

import android.annotation.SuppressLint;
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
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

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
import ug.go.agriculture.MAAIF_Extension.daes.advisory.NetworkStateChecker;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.home.VolleySingleton;

public class Review extends android.app.Activity {
    private static final String TAG = Review.class.getSimpleName();
    private TextView weather_info_text;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    public String district;
    public String subcounty;
    public String parish;
    public String type;


    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "https://extension.agriculture.go.ug/api/saveEdiaryActivities.php";

    private ListView listViewNames;

    //List to store all the names
    private List<Name> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private NameAdapter nameAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        Intent intent = getIntent();
        district = intent.getStringExtra("district");
        subcounty = intent.getStringExtra("subcounty");
        parish = intent.getStringExtra("parish");
        type = intent.getStringExtra("type");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        weather_info_text = (TextView) findViewById(R.id.weather_info_text);
        weather_info_text.setText("Weather info for: " + type);

         // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        names = new ArrayList<>();

        listViewNames = (ListView) findViewById(R.id.listViewNames);

        //calling the method to load all the stored names
        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //loading the names again
                loadNames();
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


    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadNames() {
        names.clear();
        Cursor cursor = db.getWeatherData(district,subcounty,parish,type);
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Name name = new Name(
                        cursor.getString(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("parish_id")),
                        cursor.getString(cursor.getColumnIndex("latitude")),
                        cursor.getString(cursor.getColumnIndex("longitude")),
                        cursor.getString(cursor.getColumnIndex("forecast_date")),
                        cursor.getString(cursor.getColumnIndex("maximum_temperature")),
                        cursor.getString(cursor.getColumnIndex("minimum_temperature")),
                        cursor.getString(cursor.getColumnIndex("average_temperature")),
                        cursor.getString(cursor.getColumnIndex("temperature_units")),
                        cursor.getString(cursor.getColumnIndex("rainfall_chance")),
                        cursor.getString(cursor.getColumnIndex("rainfall_amount")),
                        cursor.getString(cursor.getColumnIndex("rainfall_units")),
                        cursor.getString(cursor.getColumnIndex("windspeed_average")),
                        cursor.getString(cursor.getColumnIndex("windspeed_units")),
                        cursor.getString(cursor.getColumnIndex("wind_direction")),
                        cursor.getString(cursor.getColumnIndex("windspeed_maximum")),
                        cursor.getString(cursor.getColumnIndex("windspeed_minimum")),
                        cursor.getString(cursor.getColumnIndex("cloudcover")),
                        cursor.getString(cursor.getColumnIndex("sunshine_level")),
                        cursor.getString(cursor.getColumnIndex("soil_temperature")),
                        cursor.getString(cursor.getColumnIndex("created_at")),
                        cursor.getString(cursor.getColumnIndex("updated_at")),
                        cursor.getString(cursor.getColumnIndex("icon")),
                        cursor.getString(cursor.getColumnIndex("desc"))
                );
                names.add(name);
            } while (cursor.moveToNext());
        }

        nameAdapter = new NameAdapter(this, R.layout.weather, names);
        listViewNames.setAdapter(nameAdapter);
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
    }

    /*
     * this method is saving the name to ther server
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
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        unregisterReceiver(broadcastReceiver);
//    }

//    @Override
//    protected void onStop()
//    {
//        unregisterReceiver(broadcastReceiver);
//        super.onStop();
//    }
}
