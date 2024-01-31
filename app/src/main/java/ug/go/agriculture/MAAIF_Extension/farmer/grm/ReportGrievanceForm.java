/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.farmer.grm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class ReportGrievanceForm extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = ReportGrievanceForm.class.getSimpleName();
    private Spinner district;
    private Spinner subcounty;
    private Spinner parish;
    private Spinner village;
    private Spinner cGender;
    private Spinner cAnonymous;
    private Spinner gNature;
    private Spinner gType;
    private Spinner gSettlement;
    private Spinner gModeofReceipt;
    private Spinner feedbackMode;
    private EditText latitude;
    private EditText longitude;
    private EditText name;
    private EditText age;
    private EditText phone;
    private EditText email;
    private EditText typeNotListed;
    private EditText gDescription;
    private EditText gPastActions;
    private Button btnSavedGrievance;
    private SessionManager session;
    private SQLiteHandler db;



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
        setContentView(R.layout.report_grievance);
        district = (Spinner) findViewById(R.id.district);
        subcounty = (Spinner) findViewById(R.id.subcounty);
        parish = (Spinner) findViewById(R.id.parish);
        village = (Spinner) findViewById(R.id.village);
        cGender = (Spinner) findViewById(R.id.cGender);
        cAnonymous = (Spinner) findViewById(R.id.cAnonymous);
        gNature = (Spinner) findViewById(R.id.gNature);
        gModeofReceipt = (Spinner) findViewById(R.id.gReceipt);
        gType = (Spinner) findViewById(R.id.gType);
        gSettlement = (Spinner) findViewById(R.id.gSettlement);
        feedbackMode = (Spinner) findViewById(R.id.feedbackMode);
        name= (EditText) findViewById(R.id.name);
        age= (EditText) findViewById(R.id.age);
        phone= (EditText) findViewById(R.id.phone);
        email= (EditText) findViewById(R.id.email);
        typeNotListed= (EditText) findViewById(R.id.typeNotListed);
        gDescription= (EditText) findViewById(R.id.gDescription);
        gPastActions= (EditText) findViewById(R.id.gPastAction);
        btnSavedGrievance = (Button) findViewById(R.id.btnSavedGrievance);


        Date date = new Date();
       // Choose time zone in which you want to interpret your Date
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Kampala"));
        cal.setTime(date);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH)+1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        
        // Loading spinner data from database
        loadSpinnerDataDistrict();

        // Spinner click listener
        district.setOnItemSelectedListener(this);
        subcounty.setOnItemSelectedListener(this);
        parish.setOnItemSelectedListener(this);
        gNature.setOnItemSelectedListener(this);

       session = new SessionManager(getApplicationContext());


       // Save Grievance Activity Click event
        btnSavedGrievance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {


                // database handler
                SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                // Fetching user details from SQLite
                HashMap<String, String> districtx = db.getDistrictDetails(district.getSelectedItem().toString().trim());
                String code = districtx.get("name");



                String name_db = name.getText().toString().trim();
                String age_db = age.getText().toString().trim();
                String phone_db = phone.getText().toString().trim();
                String typeNotListed_db = typeNotListed.getText().toString().trim();
                String gDescription_db = gDescription.getText().toString().trim();
                String gPastActions_db = gPastActions.getText().toString().trim();
                String district_db = district.getSelectedItem().toString().trim();
                String subcounty_db = subcounty.getSelectedItem().toString().trim();
                String parish_db = parish.getSelectedItem().toString().trim();
                String village_db = village.getSelectedItem().toString().trim();
                String gender_db = cGender.getSelectedItem().toString().trim();
                String anonymous_db = cAnonymous.getSelectedItem().toString().trim();
                String nature_db = gNature.getSelectedItem().toString().trim();
                String mode_of_receipt_db = gModeofReceipt.getSelectedItem().toString().trim();
                String type_db = gType.getSelectedItem().toString().trim();
                String settlement_db = gSettlement.getSelectedItem().toString().trim();
                String feedback_db = feedbackMode.getSelectedItem().toString().trim();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);
                String ref_num = code + "/" + year + "/" + month;
                String dateOfGrivance_db = year + "/"+ month + "/"+ day;


                if (!subcounty_db.isEmpty()  && !parish_db.isEmpty()  && !name_db.isEmpty() && !phone_db.isEmpty()  && !gDescription_db.isEmpty() && !age_db.isEmpty() ) {

                    // database handler
                         db.addNewGrievance(district_db, subcounty_db, parish_db,
                                 name_db,age_db,  gender_db, phone_db,  feedback_db, anonymous_db, dateOfGrivance_db,
                                nature_db,type_db,typeNotListed_db,mode_of_receipt_db,gDescription_db,gPastActions_db,settlement_db,
                                 lat,lng,ref_num,village_db);

                    Toast.makeText(getApplicationContext(),
                            "Saved Grievance Successfully!", Toast.LENGTH_LONG)
                            .show();


                    //Redirect User to Review Saved Data activity
                    Intent intent = new Intent(ReportGrievanceForm.this,
                            Main.class);
                    startActivity(intent);
                    finish();

                }
                //Check if location is empty
                else  if(lat == "0.0"){
                    Toast.makeText(getApplicationContext(),
                            "Please enable your GPS location details!", Toast.LENGTH_LONG)
                            .show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),
                            "Please enter all the required details!", Toast.LENGTH_LONG)
                            .show();

                }
            }
        });



    }



    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Main.class);
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
    private void loadSpinnerDataSubCounty( String district) {
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
    private void loadSpinnerDataVillage(String parish, String subcounty) {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        String district_user = user.get("district");

        // Spinner Drop down elements
        List<String> lables = db.getAllVillages(parish,subcounty,district_user);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        village.setAdapter(dataAdapter);
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerDataDistrict() {
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
        district.setAdapter(dataAdapter);
    }
/**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerDataType(String Nature) {
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Spinner Drop down elements
        List<String> lables = db.getAllTypes(Nature);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        gType.setAdapter(dataAdapter);
    }




  //  @Override
     public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

         if(parent.getId() == R.id.subcounty)
         {
             String Dis = district.getSelectedItem().toString() ;
             String subcounty = parent.getSelectedItem().toString() ;
             loadSpinnerDataParish(Dis,subcounty);
         }
         else if(parent.getId() == R.id.parish)
         {   String parish = parent.getSelectedItem().toString() ;
             String subcounty = this.subcounty.getSelectedItem().toString() ;
             loadSpinnerDataVillage(parish,subcounty);
         } else if(parent.getId() == R.id.district)
         {   String district = parent.getSelectedItem().toString() ;
             loadSpinnerDataSubCounty(district);
         }
          else if(parent.getId() == R.id.gNature)
            {
                String nature = parent.getSelectedItem().toString() ;
                loadSpinnerDataType(nature);
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
