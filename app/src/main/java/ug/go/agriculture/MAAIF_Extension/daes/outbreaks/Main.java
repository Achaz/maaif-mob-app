package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

import android.Manifest;
import android.app.AlertDialog;
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

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.home.Login;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener  {

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
    private NavigationView navView;
    private FloatingActionButton btnAddNewEvent;
    private FloatingActionButton btnReviewMyEvents;
    private FloatingActionButton btnGoBack;


    private static final int REQUEST_INTERNET = 200;

    private SQLiteHandler mDBHelper;
    private SQLiteDatabase mDb;
    private SessionManager session;
    private SimpleLocation mLocation;
    private TextView outbreaks;
    private TextView crises;
    private TextView pending;
    private TextView total;
    private TextView uploaded;
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

        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_INTERNET);
        }
        if (ContextCompat.checkSelfPermission(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_INTERNET);
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
        String name = user.get("first_name")+" "+ user.get("last_name") + "("+ user_category + ")";

        if (user_category.equals("Farmer")) {
            setContentView(R.layout.menu_farmer_outbreaks);
        } else {
            setContentView(R.layout.activity_drawer_main_outbreaks);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        btnAddNewEvent = (FloatingActionButton) findViewById(R.id.btnAddNewEvent);
        btnReviewMyEvents = (FloatingActionButton) findViewById(R.id.btnReviewMyEvents);
        btnGoBack = (FloatingActionButton) findViewById(R.id.btnGoBack);

        outbreaks = (TextView) findViewById(R.id.all_outbreaks);
        crises = (TextView) findViewById(R.id.crises);
        pending = (TextView) findViewById(R.id.pending);
        uploaded = (TextView) findViewById(R.id.synced);
        total = (TextView) findViewById(R.id.total);
        outbreaks_crises_stats();

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

        // Displaying the user details on the screen
        txtName.setText(name);
        txtDistrict.setText(district);

        if(btnReviewMyEvents != null){
            btnAddNewEvent.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            Form.class);
                    startActivity(i);
                    finish();
                }
            });
        }else{
            Log.e("btn", "Add new event btn is null");
        }

        if (btnReviewMyEvents != null) {
            btnReviewMyEvents.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(),
                            Review.class);
                    startActivity(i);
                    finish();
                }
            });
        } else {
            Log.e("btn", "review btn is null");
        }

        if (btnGoBack != null) {
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
        } else {
            Log.e("btn", "back btn is null");
        }

    }

    private void outbreaks_crises_stats(){
        SQLiteHandler handler = new SQLiteHandler(this);
        Map<String, Object> farmer_groups_stats = handler.outbreaks_crises_stats().get(0);
        int tot_outbreaks = (int) farmer_groups_stats.get("outbreaks");
        int tot_crises = (int) farmer_groups_stats.get("crises");
        int tot_pending = (int) farmer_groups_stats.get("unsynced");
        int tot_uploaded = (int) farmer_groups_stats.get("synced");
        int tot = (int) farmer_groups_stats.get("all");

        if (outbreaks != null) {
            outbreaks.setText(String.valueOf(tot_outbreaks));
        } else {
            Log.e("STATS", "outbreaks is null");
        }

        if (crises != null) {
            crises.setText(String.valueOf(tot_crises));
        } else {
            Log.e("STATS", "crises is null");
        }

        if (pending != null) {
            pending.setText(String.valueOf(tot_pending));
        } else {
            Log.e("STATS", "pending is null");
        }

        if (uploaded != null) {
            uploaded.setText(String.valueOf(tot_uploaded));
        } else {
            Log.e("STATS", "uploaded is null");
        }

        if (total != null) {
            total.setText(String.valueOf(tot));
        } else {
            Log.e("STATS", "total is null");
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
            actionBar.setTitle("Outbreaks");
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
        Intent intent = new Intent(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this, Login.class);
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
                                ActivityCompat.requestPermissions(ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.this,
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
}
