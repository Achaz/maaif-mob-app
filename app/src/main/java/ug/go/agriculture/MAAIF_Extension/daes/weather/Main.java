package ug.go.agriculture.MAAIF_Extension.daes.weather;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.home.Login;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {
    private static final String TAG = Main.class.getSimpleName();
    private ActionBar actionBar;
    private Toolbar toolbar;
    private ProgressDialog pDialog;

    String provider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private TextView txtName;
    private TextView txtDistrict;
    private TextView location;
    private TextView nav_name;
    private TextView nav_location;
    private TextView nav_gps;
    private TextView parishes;
    private TextView records;
    private TextView nav_title;
    private TextView nav_profiles_count;
    private TextView nav_activities_count;
    private TextView nav_pending_upload_count;
    private NavigationView navView;
    private FloatingActionButton btnDownloadUpdates;
    private FloatingActionButton btnSearchWeatherInfo;
    private FloatingActionButton btnGoBack;


    private static final int REQUEST_INTERNET = 200;

    private SQLiteHandler mDBHelper;
    private SQLiteDatabase mDb;
    private SessionManager session;
    private SimpleLocation mLocation;
    private String user_category;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkLocationPermission();

        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }
        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // SqLite database handler
        mDBHelper = new SQLiteHandler(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDb = mDBHelper.getReadableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }

        // session manager
        session = new SessionManager(getApplicationContext());
        if (!session.isLoggedIn()) {
            logoutUser();
        }

        DecimalFormat formatter = new DecimalFormat("####.000000");

        // Fetching user details from SQLite
        HashMap<String, String> user = mDBHelper.getUserDetails();
        user_category = user.get("user_category");
        String name = user.get("first_name")+" "+ user.get("last_name") + "("+ user_category + ")";

        if (user_category.equals("Farmer")) {
            Log.d("Here:", "farmer");
            setContentView(R.layout.menu_farmer_weather);
        } else {
            setContentView(R.layout.activity_drawer_main_weather);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSupportActionBar(toolbar);
        initToolbar();
        initNavigationMenu();

        //Drawer Header view - set attributes
        navView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        nav_name = (TextView) headerView.findViewById(R.id.nav_name);
        nav_location = (TextView) headerView.findViewById(R.id.nav_location);
        nav_gps = (TextView) headerView.findViewById(R.id.nav_gps);
        nav_title = (TextView) headerView.findViewById(R.id.nav_title);
        nav_pending_upload_count = (TextView) headerView.findViewById(R.id.nav_pending_upload_count);
        nav_profiles_count = (TextView) headerView.findViewById(R.id.nav_profiles_count);
        nav_activities_count = (TextView) headerView.findViewById(R.id.nav_activities_count);

        parishes = (TextView) findViewById(R.id.parishes);
        records = (TextView) findViewById(R.id.records);
        txtName = (TextView) findViewById(R.id.name);
        location = (TextView) findViewById(R.id.location);
        txtDistrict = (TextView) findViewById(R.id.district);
        btnSearchWeatherInfo = (FloatingActionButton) findViewById(R.id.btnSearchWeatherInfo);
        btnDownloadUpdates = (FloatingActionButton) findViewById(R.id.btnDownloadUpdates);
        btnGoBack = (FloatingActionButton) findViewById(R.id.btnGoBack);

        //Set onclick listener
        navView.setNavigationItemSelectedListener(this);

        // construct a new instance
        mLocation = new SimpleLocation(this);
        // reduce the precision to 5,000m for privacy reasons
        mLocation.setBlurRadius(5000);
        final double latitudes = mLocation.getLatitude();
        final double longitudes = mLocation.getLongitude();
        //  location.setText("Live Location:  Lat: " + latitudes + ", Long: " + longitudes);

        String district = user.get("district");
        if ( !user.get("subcounty").isEmpty() && user.get("subcounty") != null)
            district = district + ", " + user.get("subcounty");

        int totalParishes =  mDBHelper.getCountWeatherParishes(user.get("district"));
        String tParish =  String.valueOf(totalParishes);

        int totalRecords =  mDBHelper.getCountWeatherData();
        String tRecords =  String.valueOf(totalRecords);


        //Get total profiles
        int totalProfiles =  mDBHelper.getCountProfileTotals();
        String tProfile =  String.valueOf(totalProfiles);

        //Get total activities
        int totalActivities =  mDBHelper.getCountActivities();
        String tActivities =  String.valueOf(totalActivities);

        //Get total pending uploads
        int totalPendingUploads =  mDBHelper.getCountPendingUploadTotals();
        String tPendingUploads =  String.valueOf(totalPendingUploads);

        //Set seeion user values
        nav_name.setText( user.get("first_name")+" "+ user.get("last_name"));
        nav_location.setText(district);
        nav_gps.setText(formatter.format(latitudes) + ", " + formatter.format(longitudes));
        nav_title.setText(user_category);
        nav_profiles_count.setText(tProfile);
        nav_activities_count.setText(tActivities);
        nav_pending_upload_count.setText(tPendingUploads);
        parishes.setText(tParish);
        records.setText(tRecords);

        // Displaying the user details on the screen
        txtName.setText(name);
        txtDistrict.setText(district);

        btnDownloadUpdates.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                downloadLatestWeatherUpdatesForDIstrict();
            }
        });

        btnSearchWeatherInfo.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        ug.go.agriculture.MAAIF_Extension.daes.weather.Form.class);
                startActivity(i);
                finish();
            }
        });


        btnGoBack.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                user_category = user_category.toUpperCase();
                if(user_category.equals("FARMER")){
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.home.MainFarmer.class);
                    startActivity(i);
                    finish();
                }else{
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.home.Main.class);
                    startActivity(i);
                    finish();
                }
            }
        });

    }

    private void downloadLatestWeatherUpdatesForDIstrict() {
        // Tag used to cancel the request
        String tag_string_req = "req_activities";

        pDialog.setMessage("Fetching New Weather Information...");
        showDialog();

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String uid = user.get("district");


        String url  = AppConfig.URL_WEEATHER_INFO + uid ;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Weather Information: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        //String uid = jObj.getString("uid");
                        JSONArray weather_info = jObj.getJSONArray("weather_info");

                        //Delete all current weather data
                        mDBHelper.resetWeatherTables();

                        for(int i = 0; i < weather_info.length(); i++)
                        {
                            JSONObject object3 = weather_info.getJSONObject(i);
                            String id = object3.getString("id");
                            String parish_id = object3.getString("parish_id");
                            String latitude = object3.getString("latitude");
                            String longitude = object3.getString("longitude");
                            String forecast_date = object3.getString("forecast_date");
                            String maximum_temperature = object3.getString("maximum_temperature");
                            String minimum_temperature = object3.getString("minimum_temperature");
                            String average_temperature = object3.getString("average_temperature");
                            String temperature_units = object3.getString("temperature_units");
                            String rainfall_chance = object3.getString("rainfall_chance");
                            String rainfall_amount = object3.getString("rainfall_amount");
                            String rainfall_units = object3.getString("rainfall_units");
                            String windspeed_average = object3.getString("windspeed_average");
                            String windspeed_units = object3.getString("windspeed_units");
                            String wind_direction = object3.getString("wind_direction");
                            String windspeed_maximum = object3.getString("windspeed_maximum");
                            String windspeed_minimum = object3.getString("windspeed_minimum");
                            String cloudcover = object3.getString("cloudcover");
                            String sunshine_level = object3.getString("sunshine_level");
                            String soil_temperature = object3.getString("soil_temperature");
                            String created_at = object3.getString("created_at");
                            String updated_at = object3.getString("updated_at");
                            String icon = object3.getString("weather_icon");
                            String desc = object3.getString("weather_description");



                            // Inserting row in users table
                            mDBHelper.addNewWeatherInfo(
                                   id,parish_id,latitude,longitude,forecast_date,maximum_temperature,minimum_temperature,average_temperature,temperature_units,
                                    rainfall_chance,rainfall_amount,rainfall_units,windspeed_average,windspeed_units,wind_direction,windspeed_maximum,windspeed_minimum,
                                    cloudcover,sunshine_level,soil_temperature,created_at,updated_at,icon,desc);
                        }

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Weather Info Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setTitle("Weather Advisory");
        }
    }

    private void initNavigationMenu() {

        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        db.resetAllTables();

        // Launching the login activity
        Intent intent = new Intent(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);

        user_category = user_category.toUpperCase();
        if(user_category.equals("FARMER")){
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.home.MainFarmer.class);
            startActivity(i);
            finish();
        }else{
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.home.Main.class);
            startActivity(i);
            finish();
        }
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

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("The app requires location permissions to work properly. Press ok to enable.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.weather.Main.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                        // construct a new instance
                        mLocation = new SimpleLocation(this);

                        // reduce the precision to 5,000m for privacy reasons
                        mLocation.setBlurRadius(5000);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //locationManager.requestLocationUpdates(provider, 400, 1, this);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // locationManager.removeUpdates(this);

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_ediary) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.dairy.Main.class);
            startActivity(i);
            finish();



        } else if (id == R.id.nav_grm) {

            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.grm.Main.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_advisory) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.advisory.Main.class);
            startActivity(i);
            finish();



        } else if (id == R.id.nav_profiles) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.profiling.Main.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_outbreaks) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_weather) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.daes.weather.Main.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.settings.Main.class);
            startActivity(i);
            finish();

        } else if (id == R.id.nav_profile) {
            Intent i = new Intent(getApplicationContext(),
                    ug.go.agriculture.MAAIF_Extension.profile.Main.class);
            startActivity(i);
            finish();

        }
//        else if (id == R.id.nav_dashboard) {
//            Intent i = new Intent(getApplicationContext(),
//                    ug.go.agriculture.MAAIF_Extension.daes.dairy.Dashboard.class);
//            startActivity(i);
//            finish();
//
//        }
        else if (id == R.id.nav_homie) {
            user_category = user_category.toUpperCase();
            if(user_category.equals("FARMER")){
                Intent i = new Intent(getApplicationContext(),
                        ug.go.agriculture.MAAIF_Extension.home.MainFarmer.class);
                startActivity(i);
                finish();
            }else{
                Intent i = new Intent(getApplicationContext(),
                        ug.go.agriculture.MAAIF_Extension.home.Main.class);
                startActivity(i);
                finish();
            }

        } else if (id == R.id.nav_logout) {

            logoutUser();
        } else if (id == R.id.nav_sync_locations) {
            Intent i = new Intent(getApplicationContext(), ug.go.agriculture.MAAIF_Extension.location.Main.class);
            startActivity(i);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( pDialog!=null && pDialog.isShowing() ){
            pDialog.cancel();
        }
    }

}
