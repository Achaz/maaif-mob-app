package ug.go.agriculture.MAAIF_Extension.daes.advisory;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.daes.grm.ReportGrievanceForm;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Form extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = Form.class.getSimpleName();
    private Button btnSaveActivity;
    private Spinner entreprize;
    private EditText farmerPhone;
    private EditText farmerName;
    private EditText question;
    private Spinner gReceipt;
    private SessionManager session;
    private SQLiteHandler db;
    private Spinner district;
    private Spinner subcounty;
    private Spinner parish;
    private Spinner village;
    String enterpriseInput;
    // enterprise
    String enterpriseNames[];
    HashMap<String ,String> enterpriseValues = new HashMap<String, String>();
    List<String> enterprisesList = new ArrayList<String>();


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
        setContentView(R.layout.form_advisory);
        district = (Spinner) findViewById(R.id.district);
        subcounty = (Spinner) findViewById(R.id.subcounty);
        parish = (Spinner) findViewById(R.id.parish);
        village = (Spinner) findViewById(R.id.village);
        entreprize = (Spinner) findViewById(R.id.enterpriseSpinner);
        gReceipt = (Spinner) findViewById(R.id.gReceipt);
        farmerName = (EditText) findViewById(R.id.farmerName);
        farmerPhone = (EditText) findViewById(R.id.farmerPhone);
        question = (EditText) findViewById(R.id.question);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);

        // Spinner click listener
        district.setOnItemSelectedListener(this);
        subcounty.setOnItemSelectedListener(this);
        parish.setOnItemSelectedListener(this);

        // Loading spinner data from database
        loadSpinnerDataDistrict();
        loadSpinnerDataEntreprizesDaily();
        // Session manager
        session = new SessionManager(getApplicationContext());

        entreprize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = entreprize.getSelectedItem().toString();
                if(entreprize.getSelectedItemPosition() == 0){
                }else {
                    //manuplate the hash map
                    String g = enterpriseValues.get(text);
                    enterpriseNames = g.split("@");
                    enterpriseInput = enterpriseNames[1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        // Save Planned Activity Click event
        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String district_db = district.getSelectedItem().toString().trim();
                String parish_db = parish.getSelectedItem().toString().trim();
                String subcounty_db = subcounty.getSelectedItem().toString().trim();
                String village_db = village.getSelectedItem().toString().trim();
                String entreprize_db = enterpriseInput;
                String receipt_db = gReceipt.getSelectedItem().toString().trim();
                String name_db = farmerName.getText().toString().trim();
                String question_db = question.getText().toString().trim();
                String phone_db = farmerPhone.getText().toString().trim();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);
                Integer synced = 0;

                String hh = "";
                if(question_db.isEmpty())
                    hh =  hh + "**Question \n ";
                if(name_db.isEmpty())
                    hh =  hh + "**Farmer name \n ";
                if(phone_db.isEmpty() || (phone_db.length() != 10))
                    hh =  hh + "**Phone number \n ";
                if(receipt_db == "---Select Option---")
                    hh =  hh + "**How the farmer question was received \n ";
                if(district_db == "---Select District---")
                    hh =  hh + " District \n ";
                if(subcounty_db == "---Select Subcounty---")
                    hh =  hh + "**Subcounty \n ";
                if(parish_db == "---Select Parish---")
                    hh =  hh + "**Parish  \n ";
                if(village_db == "---Select Village---")
                    hh =  hh + "**Village  \n ";
                if(entreprize_db == "---Select Entreprize---")
                    hh =  hh + "**Entreprize \n ";

                if(hh != "") {
                    String errMsg = "Please enter the required details correctly: \n " + hh;
                    showMessageDialog("Error", errMsg);
                }
                else {
                    // database handler
                    // db.addNewGrievance(district_db,subcounty_db,parish_db,name_db,age_db,gender_db,phone_db,feedback_db,anonymous_db,dateOfGrivance_db,nature_db,type_db,typeNotListed_db,mode_of_receipt_db,gDescription_db,gPastActions_db,settlement_db,lat,lng,ref_num,village_db);

                    // Toast.makeText(getApplicationContext(), "Saved new question Successfully...", Toast.LENGTH_LONG).show();

                    // Redirect User to Review Saved Data activity
                    // Intent intent = new Intent(Form.this,
                            // ug.go.agriculture.MAAIF_Extension.daes.advisory.Main.class);
                    // startActivity(intent);
                    // finish();

                    // Save question to database
                    Map<String, Object> add_question = db.addNewFarmerQuestion(district_db, subcounty_db, parish_db, village_db, name_db, phone_db, question_db, receipt_db, entreprize_db, synced).get(0);
                    String msg = (String) add_question.get("message");
                    boolean isSuccessful = (boolean) add_question.get("success");

                    if (isSuccessful){
                        // reset fields
                        farmerName.setText(""); // farmer name
                        farmerPhone.setText(""); // farmer contact
                        district.setSelection(0); // district
                        subcounty.setSelection(0); // subcounty
                        question.setText(""); // question
                        entreprize.setSelection(0); // enterprise
                        parish.setSelection(0); // parish
                        village.setSelection(0); // village
                        gReceipt.setSelection(0); // receipt

                        showMessageDialog("Success", msg);
                    } else{
                        showMessageDialog("Error", msg);
                    }
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
                ug.go.agriculture.MAAIF_Extension.daes.advisory.Main.class);
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
     * Function to load the spinner data from SQLite database activities
     * */
    private void loadSpinnerDataEntreprizesDaily() {
// database handler
        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Spinner Drop down elements
        List<String> lables = db.getAllEntreprizesDaily();

        Map<String, Object>[] enterpriseArr = new Map[lables.size()]; // create an array of Map objects

        for (int i = 0; i < lables.size(); i++) {
            Map<String, Object> map = new HashMap<>(); // create a new Map object
            map.put("name", lables.get(i));
            enterpriseArr[i] = map; // add the map to the array
        }

        try
        {
            //converting response to json object
            JSONArray providers = new JSONArray(enterpriseArr);

            for (int i = 0; i < providers.length(); i++) {
                String key=((JSONObject)providers.get(i)).getString("name");
                String Value = ((JSONObject)providers.get(i)).getString("name")+" @"+((JSONObject)providers.get(i)).getString("name");
                ;
                enterprisesList.add(key);
                enterpriseValues.put(key,Value);
            }

            ArrayAdapter<String> enterpriseAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprisesList);
            enterpriseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            entreprize.setAdapter(enterpriseAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function to load the spinner data from SQLite database
     * */

  //  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

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
