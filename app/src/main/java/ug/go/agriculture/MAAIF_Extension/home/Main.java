package ug.go.agriculture.MAAIF_Extension.home;

//import static androidx.core.app.NotificationCompatJellybean.TAG;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.constraintlayout.widget.CoordinatorLayout;
//import androidx.coordinatorlayout.widget.CoordinatorLayout;

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
import ug.go.agriculture.MAAIF_Extension.daes.dairy.Dashboard;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

    private static final String TAG = Main.class.getSimpleName();

    private ActionBar actionBar;
    private Toolbar toolbar;

    String provider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private TextView txtName;
    private TextView txtDistrict;
    private TextView location;
    private TextView nav_name;
    private TextView nav_location;
    private TextView nav_gps;
    private TextView nav_title;
    private TextView nav_profiles_count;
    private TextView nav_activities_count;
    private TextView nav_pending_upload_count;
    private FloatingActionButton btnMainEdiaryActivity;
    private FloatingActionButton btnMainProfiling;
    private FloatingActionButton btnMainAdvisory;
    private FloatingActionButton btnOnlineReport;
    private FloatingActionButton btnMainEvents;
    private FloatingActionButton btnGRM;
    //private FloatingActionButton btnEMarket;
    //private FloatingActionButton btnKMU;
    private FloatingActionButton btnLogout;
    private FloatingActionButton btnWeather;
   // private FloatingActionButton btnSettings;
    private FloatingActionButton btnMyProfile;
    private NavigationView navView;

    private ProgressBar progressBar;
    private ProgressDialog pDialog;

    private AlertDialog loadingDialog;

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
        //setContentView(R.layout.activity_drawer_main);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //initToolbar();
        //initNavigationMenu();
        checkLocationPermission();

        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            showGPSDisabledAlertToUser();
        }

        if (ContextCompat.checkSelfPermission(Main.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }
        if (ContextCompat.checkSelfPermission(Main.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Main.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // SqLite database handler
        mDBHelper = new SQLiteHandler(this);

        try
        {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try
        {
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
        String district = user.get("district");
        if ( !user.get("subcounty").isEmpty() && user.get("subcounty") != null)
            district = district + ", " + user.get("subcounty");
        String district_name = user.get("district").toUpperCase();
        String user_category_id = user.get("user_category_id");
        String district_id = user.get("district_id");

        if (user_category.equals("Farmer")) {
            Log.d("Here:", "farmer");
            setContentView(R.layout.activity_drawer_main_farmer);
        } else {
            setContentView(R.layout.activity_drawer_main);
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

        txtName = (TextView) findViewById(R.id.name);
        location = (TextView) findViewById(R.id.location);
        txtDistrict = (TextView) findViewById(R.id.district);

        btnMainEdiaryActivity = (FloatingActionButton) findViewById(R.id.btnMainEdiaryActivity);
        btnMainProfiling = (FloatingActionButton) findViewById(R.id.btnMainProfiling);
        btnMainAdvisory = (FloatingActionButton) findViewById(R.id.btnMainAdvisory);
        btnOnlineReport = (FloatingActionButton) findViewById(R.id.btnOnlineReport);
        btnMainEvents = (FloatingActionButton) findViewById(R.id.btnMainEvents);
        btnGRM = (FloatingActionButton) findViewById(R.id.btnGRM);
        btnWeather = (FloatingActionButton) findViewById(R.id.btnWeather);
        btnLogout = (FloatingActionButton) findViewById(R.id.btnLogoutNew);
        btnMyProfile = (FloatingActionButton) findViewById(R.id.btnMyProfile);

        //Set onclick listener
        navView.setNavigationItemSelectedListener(this);

        // construct a new instance
        mLocation = new SimpleLocation(this);
        // reduce the precision to 5,000m for privacy reasons
        mLocation.setBlurRadius(5000);
        final double latitudes = mLocation.getLatitude();
        final double longitudes = mLocation.getLongitude();

        // check if user is logged in
        String is_user_logged_in =  mDBHelper.checkIfUserIsLoggedIn();

        if (is_user_logged_in == "no") {
            // add user logged in flag
            mDBHelper.addLoggedIn();
            // alert dialog
            loadData(district_id, district_name);
        }

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
        nav_title.setText(user.get("user_category"));
        //nav_profiles_count.setText(tProfile);
        if (nav_profiles_count != null) {
            nav_profiles_count.setText(tProfile);
        } else {
            Log.e("MainActivity", "TextView not found");
        }
        //nav_activities_count.setText(tActivities);
        if (nav_activities_count != null) {
            nav_activities_count.setText(tActivities);
        } else {
            Log.e("MainActivity", "nav_activities_count not found");
        }
        //nav_pending_upload_count.setText(tPendingUploads);
        if (nav_pending_upload_count != null) {
            nav_pending_upload_count.setText(tPendingUploads);
        } else {
            Log.e("MainActivity", "nav_pending_upload_count not found");
        }

        // Displaying the user details on the screen
        txtName.setText(name);
        txtDistrict.setText(district);


        //set button click events
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        if (btnMainEdiaryActivity != null) {
            btnMainEdiaryActivity.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.dairy.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnMyProfile != null) {
            btnMyProfile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.profile.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnWeather != null) {
            btnWeather.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.weather.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnMainProfiling != null) {
            btnMainProfiling.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.profiling.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnGRM != null) {
            btnGRM.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.grm.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnMainAdvisory != null) {
            btnMainAdvisory.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.advisory.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnOnlineReport != null) {
            btnOnlineReport.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.location.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }

        if (btnMainEvents != null) {
            btnMainEvents.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setTitle("MAAIF E-Extension - Home");
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
        if (item.getItemId() != android.R.id.home) {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(Main.this, Login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want exit E-Extension? ");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(getApplicationContext(),
                        Main.class);
                startActivity(i);
                finish();
            }
        });
        builder.show();
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
                                ActivityCompat.requestPermissions(Main.this,
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
            Intent i = new Intent(getApplicationContext(), ug.go.agriculture.MAAIF_Extension.home.Main.class);
            startActivity(i);
            finish();

        }
         else if (id == R.id.nav_logout) {
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

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.progress_dialog, null);
        builder.setView(view);
        builder.setCancelable(false); // Prevent users from canceling the dialog

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void loadData(String district_id, String district) {
        showLoadingDialog();

        // Simulate data loading
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // database handler
                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());

                    // Fetching user details from SQLite
                    HashMap<String, String> user = db.getUserDetails();

                    String uid = user.get("uid");

                    //Check for Quarterly Activities
                    checkUserPlannedActivities(uid);
                    checkUserActivities(uid);
                    checkUserOutbreaks(uid);

                    // TODO check new topics,activities,new locations and more here
                     checkSeedData(district_id, district);

                    // Simulate a network call or heavy processing
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Hide the loading dialog when data is loaded
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingDialog();
                    }
                });
            }
        }).start();
    }

    private void checkUserPlannedActivities(String uid) {
        // Tag used to cancel the request
        String tag_string_req = "req_activities";

        String url  = AppConfig.URL_QUATELY_ACTIVITIES + uid ;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Quarterly Activities Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        //String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String activities = user.getString("activities");
                        String entreprizes = user.getString("entreprizes");
                        String topics = user.getString("topics");

                        // Inserting row in users table
                        mDBHelper.addQP(activities,topics,entreprizes);


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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Server response error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void checkUserActivities(String uid) {
        // Tag used to cancel the request
        String tag_string_req = "req_farmers";

        String url  = AppConfig.URL_USER_ACTIVITIES + uid;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User Activities Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray activities = jObj.getJSONArray("activities");

                        for(int i = 0; i < activities.length(); i++)
                        {
                            JSONObject object3 = activities.getJSONObject(i);
                            String a1 = object3.getString("a1");
                            String a2 = object3.getString("a2");
                            String a3 = object3.getString("a3");
                            String a4 = object3.getString("a4");
                            String a5 = object3.getString("a5");
                            String a6 = object3.getString("a6");
                            String a7 = object3.getString("a7");
                            String a8 = object3.getString("a8");
                            String a9 = object3.getString("a9");
                            String a10 = object3.getString("a10");
                            String a11 = object3.getString("a11");
                            String a12 = object3.getString("a12");
                            String a13 = object3.getString("a13");
                            String a14 = object3.getString("a14");
                            String a15 = object3.getString("a15");
                            String a16 = object3.getString("a16");
                            String a17 = object3.getString("a17");
                            String a18 = object3.getString("a18");
                            String a19 = object3.getString("a19");
                            String a20 = object3.getString("a20");
                            String a21 = object3.getString("a21");
                            String a22 = object3.getString("a22");
                            String a23 = object3.getString("a23");
                            String a24 = object3.getString("a24");
                            String a25 = object3.getString("a25");
                            String a26 = object3.getString("a26");
                            String a27 = object3.getString("a27");
                            String a28 = object3.getString("a28");
                            String a29 = object3.getString("a29");
                            String a30 = object3.getString("a30");
                            String a31 = object3.getString("a31");
                            String a32 = object3.getString("a32");
                            String a33 = object3.getString("a33");
                            String a34 = object3.getString("a34");
                            String a35 = object3.getString("a35");
                            String a36 = object3.getString("a36");
                            String a37 = object3.getString("a37");
                            String a38 = object3.getString("a38");
                            String a39 = object3.getString("a39");
                            String a40 = object3.getString("a40");
                            String a41 = object3.getString("a41");
                            String a42 = object3.getString("a42");
                            String a43 = object3.getString("a43");
                            String a44 = object3.getString("a44");
                            String a45 = object3.getString("a45");
                            String a46 = object3.getString("a46");
                            String a47 = object3.getString("a47");
                            String a48 = object3.getString("a48");
                            String a49 = object3.getString("a49");
                            String a50 = object3.getString("a50");



                            // Inserting row in users table
                            mDBHelper.dumpActivities(
                                    a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,
                                    a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,
                                    a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,
                                    a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,
                                    a41,a42,a43,a44,a45,a46,a47,a48,a49,a50
                            );

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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Server response error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkUserOutbreaks(String uid) {
        // Tag used to cancel the request
        String tag_string_req = "req_farmers";

        String url  = AppConfig.URL_USER_OUTBREAKS + uid;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User Outbreaks Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray outbreaks = jObj.getJSONArray("outbreaks");

                        for(int i = 0; i < outbreaks.length(); i++)
                        {
                            JSONObject object3 = outbreaks.getJSONObject(i);
                            String a1 = object3.getString("a1");
                            String a2 = object3.getString("a2");
                            String a3 = object3.getString("a3");
                            String a4 = object3.getString("a4");
                            String a5 = object3.getString("a5");
                            String a6 = object3.getString("a6");
                            String a7 = object3.getString("a7");
                            String a8 = object3.getString("a8");
                            String a9 = object3.getString("a9");
                            String a10 = object3.getString("a10");
                            String a11 = object3.getString("a11");
                            String a12 = object3.getString("a12");
                            String a13 = object3.getString("a13");
                            String a14 = object3.getString("a14");
                            String a15 = object3.getString("a15");
                            String a16 = object3.getString("a16");
                            String a17 = object3.getString("a17");
                            String a18 = object3.getString("a18");
                            String a19 = object3.getString("a19");
                            String a20 = object3.getString("a20");
                            String a21 = object3.getString("a21");
                            String a22 = object3.getString("a22");
                            String a23 = object3.getString("a23");
                            String a24 = object3.getString("a24");
                            String a25 = object3.getString("a25");
                            String a26 = object3.getString("a26");
                            String a27 = object3.getString("a27");
                            String a28 = object3.getString("a28");
                            String a29 = object3.getString("a29");
                            String a30 = object3.getString("a30");
                            String a31 = object3.getString("a31");
                            String a32 = object3.getString("a32");
                            String a33 = object3.getString("a33");
                            String a34 = object3.getString("a34");
                            String a35 = object3.getString("a35");
                            String a36 = object3.getString("a36");
                            String a37 = object3.getString("a37");
                            String a38 = object3.getString("a38");
                            String a39 = object3.getString("a39");
                            String a40 = object3.getString("a40");
                            String a41 = object3.getString("a41");
                            String a42 = object3.getString("a42");
                            String a43 = object3.getString("a43");
                            String a44 = object3.getString("a44");
                            String a45 = object3.getString("a45");
                            String a46 = object3.getString("a46");
                            String a47 = object3.getString("a47");
                            String a48 = object3.getString("a48");
                            String a49 = object3.getString("a49");
                            String a50 = object3.getString("a50");

                            // Inserting row in users table
                            mDBHelper.dumpOutbreaks(
                                    a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,
                                    a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,
                                    a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,
                                    a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,
                                    a41,a42,a43,a44,a45,a46,a47,a48,a49,a50
                            );

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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), "Server response error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch enterprises from server and add them to local mobile db
    private void fetchEnterprises(){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        String url  = AppConfig.URL_ENTREPRISES;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Enterprises Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray entreprises = jObj.getJSONArray("entreprises");

                        for(int i = 0; i < entreprises.length(); i++)
                        {
                            JSONObject object3 = entreprises.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("parent_id");

                            // Inserting row in entreprizes table
                            mDBHelper.dumpAllEntreprizes(id,name,category);
                        }

                    } else {
                        // Error in Syncing. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), "Server response error: " + errorMsg, Toast.LENGTH_LONG).show();
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
                Log.e(TAG, "Dumping Enterprises Error: " + error.getMessage());
                String errorMessage = error.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Unknown error occurred";
                }
                Toast.makeText(getApplicationContext(), "Server response error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch counties from server and add them to local mobile db
    private void fetchCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";
        Log.d(TAG,"Fetching counties from the servers ..." );

        String url  = AppConfig.URL_DISTRICT_COUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Counties Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray counties = jObj.getJSONArray("counties");

                        for(int i = 0; i < counties.length(); i++)
                        {
                            JSONObject object3 = counties.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String district = object3.getString("district_id");

                            // Inserting row in county table
                            mDBHelper.addCounty(id,name,district);
                        }
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
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch subcounties from server and add them to local mobile db
    private void fetchSubCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";
        Log.d(TAG, "Fetching subcounties from the servers ...");

        String url  = AppConfig.URL_DISTRICT_SUBCOUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "SubCounties Response: " + response.toString());

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
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch parishes from server and add them to local mobile db
    private void fetchDistrictParishes(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        Log.d(TAG, "Fetching parishes from the servers ...");

        String url  = AppConfig.URL_DISTRICT_PARISHES + district_id;
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "parishes Response: " + response.toString());
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
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch villages from server and add them to local mobile db
    private void fetchDistrictVillages(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        Log.d(TAG, "Fetching district villages from the servers ...");

        String url  = AppConfig.URL_DISTRICT_VILLAGES + district_id;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

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
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkSeedData(String district_id, String district) {
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        String url  = AppConfig.ULR_SEEDDATA;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Seed Data Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Reset all seed data tables
                        mDBHelper.resetAllTablesSeedData();

                        // Activities
                        JSONArray activities = jObj.getJSONArray("activities");
                        for(int i = 0; i < activities.length(); i++)
                        {
                            JSONObject object3 = activities.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("category");

                            // Inserting row in activities table
                            mDBHelper.dumpAllActivities(id, name, category);
                        }

                        // Entreprizes
                        JSONArray entreprizes = jObj.getJSONArray("entreprizes");
                        for(int i = 0; i < entreprizes.length(); i++)
                        {
                            JSONObject object3 = entreprizes.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("parent_id");

                            // Inserting row in entreprizes table
                            mDBHelper.dumpAllEntreprizes(id,name,category);
                        }

                        // insert user district
                        mDBHelper.dumpAllDistricts(district_id, district);

                        // register district counties, subcounties, parishes and villages
                        fetchCounties(district_id);

                        JSONArray districts = jObj.getJSONArray("districts");
                        int districts_count = districts.length();
                        for(int i = 0; i < districts_count; i++)
                        {
                            JSONObject object4 = districts.getJSONObject(i);
                            String id = object4.getString("id");
                            String name = object4.getString("name");

                            // Inserting districts in district2 table
                            mDBHelper.addDistrict(id,name);
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
                String errorMessage = "An error occurred fetching seed data from the server...";
                Log.e(TAG, errorMessage);
                 Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                // hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


}
