package ug.go.agriculture.MAAIF_Extension.farmer.weather;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Form extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = Form.class.getSimpleName();
    private Button btnSaveActivity;
    private SessionManager session;
    private SQLiteHandler db;
    private EditText district;
    private Spinner subcounty;
    private Spinner parish;
    private Spinner type;
    private SQLiteHandler mDBHelper;



    private SimpleLocation mLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {


        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Stay Silent
            //Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }



        // construct a new instance
        mLocation = new SimpleLocation(this);

        db = new SQLiteHandler(getApplicationContext());
        // reduce the precision to 5,000m for privacy reasons
        mLocation.setBlurRadius(5000);

        final double latitudes = mLocation.getLatitude();
        final double longitudes = mLocation.getLongitude();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_weather);
        district = (EditText) findViewById(R.id.district);
        subcounty = (Spinner) findViewById(R.id.subcounty);
        parish = (Spinner) findViewById(R.id.parish);
        type = (Spinner) findViewById(R.id.type);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);

        // Spinner click listener
        subcounty.setOnItemSelectedListener(this);
        parish.setOnItemSelectedListener(this);

        // Loading spinner data from database
        // SqLite database handler
        mDBHelper = new SQLiteHandler(this);

        // Fetching user details from SQLite
        HashMap<String, String> user = mDBHelper.getUserDetails();
        String districtx = user.get("district");

        district.setText(districtx);
        loadSpinnerDataSubCounty(districtx);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Save Planned Activity Click event
        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String district_db = district.getText().toString().trim();
                String parish_db = parish.getSelectedItem().toString().trim();
                String subcounnty_db = subcounty.getSelectedItem().toString().trim();
                String type_db = type.getSelectedItem().toString().trim();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);


                if (    !subcounnty_db.startsWith("---") &&
                        //       !village_db.startsWith("---") &&
                        !district_db.startsWith("---") &&
                        !parish_db.startsWith("---")

                ) {
//
//                    // database handler
//                         db.addPlannedActivity(activity_db,topic_db,entreprize_db,subcounnty_db,village_db,ben_group,reference,reference_contact,ben_males,ben_females,remarks,lat,lng,parish_db,district_db,lesssons_db,reccomendations_db);

                    Toast.makeText(getApplicationContext(),
                            "Got some weather info for you!", Toast.LENGTH_LONG)
                            .show();


                    //Redirect User to Review Saved Data activity
                    Intent intent = new Intent(Form.this,
                            Review.class);
                    intent.putExtra("district",district_db);
                    intent.putExtra("subcounty",subcounnty_db);
                    intent.putExtra("parish",parish_db);
                    intent.putExtra("type",type_db);
                    startActivity(intent);
                    finish();

                } else {
                    String hh = "Please enter the required details correctly: \n ";
                    if(district_db == "---Select District---")
                        hh =  hh + " District \n ";
                    if(subcounnty_db == "---Select Subcounty---")
                        hh =  hh + " Subcounty \n ";
                    if(parish_db == "---Select Parish---")
                        hh =  hh + " Parish  \n ";
//                    if(village_db == "---Select Village---")
//                        hh =  hh + " Village  \n ";
//                    if(topic_db == "---Select Topic---")
//                        hh =  hh + " Topic \n ";
//                    if(entreprize_db == "---Select Entreprize---")
//                        hh =  hh + " Entreprize  \n ";
//                    if(activity_db == "---Select Activity---")
//                        hh =  hh + " Activity  \n ";
//                    if(ben_group.isEmpty())
//                        hh =  hh + " Beneficiary Group \n ";
//                    if(reference.isEmpty())
//                        hh =  hh + " Reference Person \n ";
//                    if(reference_contact.isEmpty())
//                        hh =  hh + " Reference Person Contact  \n ";
//                    if(ben_females.isEmpty())
//                        hh =  hh + " Males Reached  \n ";
//                    if(ben_males.isEmpty())
//                        hh =  hh + " Females Reached  \n ";
//                    if(remarks.isEmpty())
//                        hh =  hh + " Challenges faced on the Activity  \n ";
//                    if(lesssons_db.isEmpty())
//                        hh =  hh + " Lessons learnt on the Activity  \n ";
//                    if(reccomendations_db.isEmpty())
//                        hh =  hh + " Recommendations about on the Activity  \n ";
                    Toast toast= Toast.makeText(getApplicationContext(),
                            hh, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            }
        });



    }



    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                ug.go.agriculture.MAAIF_Extension.daes.weather.Main.class);
        startActivity(i);
        finish();
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



    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData() {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getAllDistrictNames();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        subcounty.setAdapter(dataAdapter);
    }


    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerDataSubCounty(String district) {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getAllDistrictSubcounty(district);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        subcounty.setAdapter(dataAdapter);
    }

    private void loadSpinnerDataParish(String Dis,String Subcounty) {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getAllSubcountyParish(Dis,Subcounty);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        parish.setAdapter(dataAdapter);
    }


    /**
     * Function to load the spinner data from SQLite database
     * */
//    private void loadSpinnerDataDistrict() {
//        // database handler
//        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
//
//        // Spinner Drop down elements
//        List<String> lables = db.getAllDistrictNames();
//
//        // Creating adapter for spinner
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_item, lables);
//
//        // Drop down layout style - list view with radio button
//        dataAdapter
//                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // attaching data adapter to spinner
//        district.setAdapter(dataAdapter);
//    }






    /**
     * Function to load the spinner data from SQLite database
     * */

    //  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        if(parent.getId() == R.id.subcounty)
        {
            String Dis = district.getText().toString() ;
            String subcounty = parent.getSelectedItem().toString() ;
            loadSpinnerDataParish(Dis,subcounty);
        }
        else if(parent.getId() == R.id.parish)
        {   String parish = parent.getSelectedItem().toString() ;
            String subcounty = this.subcounty.getSelectedItem().toString() ;
            //          loadSpinnerDataVillage(parish,subcounty);
        }
    }

    //  @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    protected void onResume() {
        super.onResume();

        // make the device update its location
        mLocation.beginUpdates();
    }

    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        mLocation.endUpdates();

        super.onPause();
    }





}
