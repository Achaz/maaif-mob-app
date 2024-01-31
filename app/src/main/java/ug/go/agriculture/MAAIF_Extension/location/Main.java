package ug.go.agriculture.MAAIF_Extension.location;

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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.home.Login;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener   {

    private ActionBar actionBar;
    private Toolbar toolbar;

    String provider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private TextView txtDistrict;
    private TextView location;
    private TextView nav_name;
    private TextView nav_location;
    private TextView nav_gps;
    private TextView nav_title;
    private TextView nav_profiles_count;
    private TextView nav_activities_count;
    private TextView nav_pending_upload_count;
    private NavigationView navView;
    private TextView txtSyncTime;
    private Button btnSyncLocation;
    private ProgressDialog pDialog;
    private static final String TAG = Login.class.getSimpleName();
    private Spinner spDistrict;
    private TextView tvResults;
    private TextView tvResultCompleted;
    private TextView tvResultCounties;
    private TextView tvResultSubcounties;
    private TextView tvResultParishes;
    private TextView tvResultVillages;
    private LinearLayout llCounties;
    private LinearLayout llSubcounties;
    private LinearLayout llParishes;
    private LinearLayout llVillages;
    String districtInput;
    //District
    String districtNames[];
    HashMap<String ,String> districtValues = new HashMap<String, String>();
    List<String> districtList = new ArrayList<String>();

    private static final int REQUEST_INTERNET = 200;

    private SQLiteHandler mDBHelper;
    private SQLiteDatabase mDb;
    private SessionManager session;
    private SimpleLocation mLocation;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private String user_category;

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

        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.location.Main.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.location.Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }
        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.location.Main.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.location.Main.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }

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
        String name = user.get("first_name")+" "+ user.get("last_name") + "("+ user.get("user_category") + ")";
        String district = user.get("district") + ", " + user.get("subcounty") ;
        String district_id = user.get("district_id");

        if (user_category.equals("Farmer")) {
            Log.d("Here:", "farmer");
            setContentView(R.layout.farmer_sync_locations);
        } else {
            setContentView(R.layout.activity_sync_locations);
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

        location = (TextView) findViewById(R.id.location);
        txtDistrict = (TextView) findViewById(R.id.district);
        txtSyncTime = (TextView) findViewById(R.id.sync_timestamp);
        spDistrict = (Spinner) findViewById(R.id.sp_district);
        btnSyncLocation = (Button) findViewById(R.id.btnSyncLocation);

        tvResults = (TextView) findViewById(R.id.tv_results);
        tvResultCompleted = (TextView) findViewById(R.id.tv_results_completed);
        tvResultSubcounties = (TextView) findViewById(R.id.tv_subcounties);
        tvResultCounties = (TextView) findViewById(R.id.tv_counties);
        tvResultParishes = (TextView) findViewById(R.id.tv_parishes);
        tvResultVillages = (TextView) findViewById(R.id.tv_villages);
        llCounties = (LinearLayout) findViewById(R.id.ll_counties);
        llSubcounties = (LinearLayout) findViewById(R.id.ll_subcounties);
        llParishes = (LinearLayout) findViewById(R.id.ll_parishes);
        llVillages = (LinearLayout) findViewById(R.id.ll_villages);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //Set onclick listener
        navView.setNavigationItemSelectedListener(this);

        // construct a new instance
        mLocation = new SimpleLocation(this);
        // reduce the precision to 5,000m for privacy reasons
        mLocation.setBlurRadius(5000);
        final double latitudes = mLocation.getLatitude();
        final double longitudes = mLocation.getLongitude();
        //  location.setText("Live Location:  Lat: " + latitudes + ", Long: " + longitudes);

        //Get total profiles
        int totalProfiles =  mDBHelper.getCountProfileTotals();
        String tProfile =  String.valueOf(totalProfiles);

        //Get total activities
        int totalActivities =  mDBHelper.getCountActivities();
        String tActivities =  String.valueOf(totalActivities);

        //Get total pending uploads
        int totalPendingUploads =  mDBHelper.getCountPendingUploadTotals();
        String tPendingUploads =  String.valueOf(totalPendingUploads);

        //Set session user values
        nav_name.setText( user.get("first_name")+" "+ user.get("last_name"));
        nav_location.setText(district);
        nav_gps.setText(formatter.format(latitudes) + ", " + formatter.format(longitudes));
        nav_title.setText(user_category);
        nav_profiles_count.setText(tProfile);
        nav_activities_count.setText(tActivities);
        nav_pending_upload_count.setText(tPendingUploads);

        // Displaying the user details on the screen
        txtDistrict.setText(district);
        txtSyncTime.setText(""); // TODO get last sync timestamp

        // Spinner click listener
        spDistrict.setOnItemSelectedListener(ug.go.agriculture.MAAIF_Extension.location.Main.this);
        // Loading spinner data from database
        loadSpinnerDataDistrict();

        spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = spDistrict.getSelectedItem().toString();
                if(spDistrict.getSelectedItemPosition() == 0){
                }else {
                    //manuplate the hash map
                    String g = districtValues.get(text);
                    districtNames = g.split("@");
                    districtInput = districtNames[1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        // sync locations
        btnSyncLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String district_name = districtInput;

                if (!district_name.startsWith("---") ) {
                    // database handler
                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                    HashMap<String, String> district_details = db.getDistrict2Details(district_name.trim());
                    String district_id = district_details.get("id");

                    tvResultCompleted.setVisibility(View.INVISIBLE);
                    tvResults.setText("Processing...");
                    tvResults.setVisibility(View.VISIBLE);
                    llCounties.setVisibility(View.INVISIBLE);
                    llSubcounties.setVisibility(View.INVISIBLE);
                    llParishes.setVisibility(View.INVISIBLE);
                    llVillages.setVisibility(View.INVISIBLE);

//                    checkSeedData(district_id, district);
                    fetchSyncedLocationData(district_id, district_name);

                } else {
                    String hh = "Please enter the required details correctly: \n ";
                    if (district_name == "---Select District---")
                        hh = hh + " District \n ";
                    Toast toast= Toast.makeText(getApplicationContext(),
                            hh, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setTitle("Sync Locations");
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
        Intent intent = new Intent(ug.go.agriculture.MAAIF_Extension.location.Main.this, Login.class);
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
                                ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.location.Main.this,
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

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerDataDistrict() {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getSyncDistrictNames();

        Map<String, Object>[] districtArr = new Map[lables.size()]; // create an array of Map objects

        for (int i = 0; i < lables.size(); i++) {
            Map<String, Object> map = new HashMap<>(); // create a new Map object
            map.put("name", lables.get(i));
            districtArr[i] = map; // add the map to the array
        }

        try
        {
            //converting response to json object
            JSONArray providers = new JSONArray(districtArr);

            for (int i = 0; i < providers.length(); i++) {
                String key=((JSONObject)providers.get(i)).getString("name");
                String Value = ((JSONObject)providers.get(i)).getString("name")+" @"+((JSONObject)providers.get(i)).getString("name");
                ;
                districtList.add(key);
                districtValues.put(key,Value);
            }

            ArrayAdapter<String> districtAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, districtList);
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spDistrict.setAdapter(districtAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void fetchSyncedLocationData(String district_id, String district){
        pDialog.setMessage("Fetching synced location data ...");
        showDialog();

        // insert user district
        mDBHelper.dumpAllDistricts(district_id, district);

        // change title
        tvResults.setText("Results");

        // register district counties, subcounties, parishes and villages
        fetchCounties(district_id);
    }

    private void checkSeedData(String district_id, String district) {
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        pDialog.setMessage("Fetching Seed Data in ...");
        showDialog();

        String url  = AppConfig.ULR_SEEDDATA;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Seed Data Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // insert user district
                        mDBHelper.addDistrict2(district_id, district);

                        // change title
                        tvResults.setText("Results");

                        // register district counties, subcounties, parishes and villages
                        fetchCounties(district_id);

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
                Log.e(TAG, "Dumping Topics Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch counties from server and add them to local mobile db
    private void fetchCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching counties from the servers ...");
//        showDialog();
        Log.d(TAG,"Fetching counties from the servers ..." );

        String url  = AppConfig.URL_DISTRICT_COUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Counties Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray counties = jObj.getJSONArray("counties");
                        int counties_count = counties.length();

                        for(int i = 0; i < counties.length(); i++)
                        {
                            JSONObject object3 = counties.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String district = object3.getString("district_id");

                            // Inserting row in county table
                            mDBHelper.addCounty(id,name,district);
                        }

                        // set and show result
                        llCounties.setVisibility(View.VISIBLE);
                        tvResultCounties.setText(counties_count+"");

                        // Fetch subcounties
                        fetchSubCounties(user_district_id);

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Dumping counties Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch subcounties from server and add them to local mobile db
    private void fetchSubCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching subcounties from the servers ...");
//        showDialog();
        Log.d(TAG, "Fetching subcounties from the servers ...");

        String url  = AppConfig.URL_DISTRICT_SUBCOUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "SubCounties Response: " + response.toString());
//                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray subcounties = jObj.getJSONArray("sub_counties");
                        int subcounties_count = subcounties.length();
                        Log.d(TAG, "subcounties count" + subcounties_count);

                        for(int i = 0; i < subcounties_count; i++)
                        {
                            JSONObject object3 = subcounties.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String county = object3.getString("county_id");

                            // Inserting row in county table
                            mDBHelper.addSubCounty(id,name,county);
                        }

                        // set and show result
                        tvResultSubcounties.setText(subcounties_count+"");
                        llSubcounties.setVisibility(View.VISIBLE);

                        fetchDistrictParishes(user_district_id);

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Dumping sub counties Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch parishes from server and add them to local mobile db
    private void fetchDistrictParishes(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching parishes from the servers ...");
//        showDialog();
        Log.d(TAG, "Fetching parishes from the servers ...");

        String url  = AppConfig.URL_DISTRICT_PARISHES + district_id;
//        String url  = AppConfig.URL_PARISHES;
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "parishes Response: " + response.toString());
//                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray parishes = jObj.getJSONArray("parishes");
                        int parishes_count = parishes.length();
                        Log.d(TAG, "Parishes count" + parishes_count);

                        for(int i = 0; i < parishes_count; i++)
                        {
                            JSONObject object3 = parishes.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String subcounty = object3.getString("subcounty_id");

                            // Inserting row in county table
                            mDBHelper.addParish(id, name, subcounty);
                        }

                        // set and show result
                        tvResultParishes.setText(parishes_count+"");
                        llParishes.setVisibility(View.VISIBLE);

                        fetchDistrictVillages(district_id);

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Adding parish Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch villages from server and add them to local mobile db
    private void fetchDistrictVillages(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching district villages from the servers ...");
//        showDialog();

        Log.d(TAG, "Fetching district villages from the servers ...");

        String url  = AppConfig.URL_DISTRICT_VILLAGES + district_id;
//        String url  = AppConfig.URL_VILLAGES;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "villages Response: " + response.toString());
                // hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray villages = jObj.getJSONArray("villages");
                        int village_count = villages.length();
                        Log.d(TAG, "Villages count" + village_count);
                        for(int i = 0; i < village_count; i++)
                        {
                            JSONObject object3 = villages.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String parish = object3.getString("parish_id");

                            // Inserting row in county table
                            mDBHelper.addVillage(id, name, parish);
                        }

                        // set and show result
                        tvResultVillages.setText(village_count+"");
                        llVillages.setVisibility(View.VISIBLE);
                        tvResultCompleted.setVisibility(View.VISIBLE);

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Adding village Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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

    /**
     * Function to load the spinner data from SQLite database
     * */

    //  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
    }

    //  @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

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
            Intent i = new Intent(getApplicationContext(), ug.go.agriculture.MAAIF_Extension.home.Main.class);
            startActivity(i);
            finish();

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
}
