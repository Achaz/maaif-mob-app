/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.daes.grm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import im.delight.android.location.SimpleLocation;

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
                int nature_db = gNature.getSelectedItemPosition();
                int mode_of_receipt_db = gModeofReceipt.getSelectedItemPosition();
                int type_db = gType.getSelectedItemPosition();
                int settlement_db = gSettlement.getSelectedItemPosition();
                int feedback_db = feedbackMode.getSelectedItemPosition();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);
                String ref_num = code + "/" + year + "/" + month;
                String dateOfGrivance_db = year + "/"+ month + "/"+ day;

                String hh = "";
                if(gPastActions_db.isEmpty())
                    hh =  hh + "**Past actions \n ";
                if(gDescription_db.isEmpty())
                    hh =  hh + "**Grievance description \n ";
                if(age_db.isEmpty())
                    hh =  hh + "**Contact person age \n ";
                if(name_db.isEmpty())
                    hh =  hh + "**Reference person name \n ";
                if(phone_db.isEmpty() || (phone_db.length() != 10))
                    hh =  hh + "**Reference person contact \n ";
                if(settlement_db == 0)
                    hh =  hh + "**Was grievance settled \n ";
                if(mode_of_receipt_db == 0)
                    hh =  hh + "**Was grievance filled \n ";
                if(feedback_db == 0)
                    hh =  hh + "**Feedback \n ";
                if(anonymous_db == "---Select Option---" || anonymous_db.startsWith("---Select Option---"))
                    hh =  hh + "**Anonymous \n ";
                if(district_db == "---Select District---")
                    hh =  hh + " District \n ";
                if(subcounty_db == "---Select Subcounty---")
                    hh =  hh + "**Subcounty \n ";
                if(parish_db == "---Select Parish---")
                    hh =  hh + "**Parish  \n ";
                if(village_db == "---Select Village---")
                    hh =  hh + "**Village  \n ";
                if(gender_db == "---Select Gender---" || gender_db.startsWith("---Select Gender---"))
                    hh =  hh + "**Gender \n ";
                if(nature_db == 0)
                    hh =  hh + "**Nature  \n ";
                if(type_db == 0)
                    hh =  hh + "**Grievance type  \n ";

                if(hh != "") {
                    String errMsg = "Please enter the required details correctly: \n " + hh;
                    showMessageDialog("Error", errMsg);
                }
                else {
                    String grievance_date = "";

                    // Define the input and output date formats
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy/M/d", Locale.getDefault());
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    try {
                        // Parse the input date string
                        Date inputDate = inputDateFormat.parse(dateOfGrivance_db);

                        // Format the date using the output format
                        if (inputDate != null) {
                            dateOfGrivance_db = outputDateFormat.format(inputDate);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    // check what was selected for anonymous
                    if(anonymous_db == "yes")
                        anonymous_db = "1";
                    else
                        anonymous_db = "0";

                    // database handler
                    db.addNewGrievance(district_db,subcounty_db,parish_db,name_db,age_db,gender_db,phone_db,String.valueOf(feedback_db),anonymous_db,dateOfGrivance_db,String.valueOf(nature_db),String.valueOf(type_db),typeNotListed_db,String.valueOf(mode_of_receipt_db),gDescription_db,gPastActions_db,String.valueOf(settlement_db),lat,lng,ref_num,village_db);

                    Toast.makeText(getApplicationContext(), "Saved Grievance Successfully...", Toast.LENGTH_LONG).show();

                    //Redirect User to Review Saved Data activity
                    Intent intent = new Intent(ReportGrievanceForm.this,
                            Main.class);
                    startActivity(intent);
                    finish();

//                    Log.d(TAG, "User submitted data:");
//                    Log.d(TAG, "Parish: " + parish_db);
//                    Log.d(TAG, "Name: " + name_db);
//                    Log.d(TAG, "Age: " + age_db);
//                    Log.d(TAG, "Gender: " + gender_db);
//                    Log.d(TAG, "Phone: " + phone_db);
//                    Log.d(TAG, "Feedback: " + feedback_db);
//                    Log.d(TAG, "Anonymous: " + anonymous_db);
//                    Log.d(TAG, "Date of grievance: " + dateOfGrivance_db);
//                    Log.d(TAG, "Grievance nature: " + nature_db);
//                    Log.d(TAG, "Grievance type: " + type_db);
//                    Log.d(TAG, "Grievance type not listed: " + typeNotListed_db);
//                    Log.d(TAG, "Mode of receipt: " + mode_of_receipt_db);
//                    Log.d(TAG, "Description: " + gDescription_db);
//                    Log.d(TAG, "Past actions: " + gPastActions_db);
//                    Log.d(TAG, "Settle otherwise: " + settlement_db);
//                    Log.d(TAG, "ref_number: " + ref_num);
                }
            }
        });
    }

    private void showMessageDialog(String msgType, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(msgType)
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
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
        List<String> lables = db.getAllVillages(parish, subcounty, district_user);

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
