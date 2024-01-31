/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.daes.dairy;

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
import android.view.Gravity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import im.delight.android.location.SimpleLocation;

public class UnplannedActivityForm extends Activity  implements AdapterView.OnItemSelectedListener {
    private static final String TAG = UnplannedActivityForm.class.getSimpleName();
    private Button btnSaveActivity;
    private EditText beneficiary_males;
    private EditText beneficiary_females;
    private EditText beneficiary_group;
    private EditText beneficiary_ref;
    private EditText beneficiary_ref_contact;
    private EditText subcounty;
    private EditText village;
    private EditText notes;
    private EditText lessons;
    private EditText recommendations;

    private EditText activity_description;
    private SessionManager session;
    private SQLiteHandler db;
    private Spinner spinner;
    private Spinner spinner2;
    private Spinner activity;
    private Spinner topic;
    private Spinner entreprize;
    private Spinner entreprize2;
    private Spinner parish;
    private SimpleLocation mLocation;
    private Spinner district;
    Spinner spinnerEnterprise, spinnerEnterprise2;
    String enterpriseInput, enterprise2Input;

    // enterprise
    String enterpriseNames[];
    HashMap<String ,String> enterpriseValues = new HashMap<String, String>();
    List<String> enterprisesList = new ArrayList<String>();
    // enterprise2
    String enterprise2Names[];
    HashMap<String ,String> enterprise2Values = new HashMap<String, String>();
    List<String> enterprises2List = new ArrayList<String>();
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

        // if we can't access the location yet
        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(this);
        }
        // reduce the precision to 5,000m for privacy reasons
        mLocation.setBlurRadius(5000);

        final double latitudes = mLocation.getLatitude();
        final double longitudes = mLocation.getLongitude();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_activity_unplanned);
        spinner = (Spinner) findViewById(R.id.spinner);
        district = (Spinner) findViewById(R.id.district);
        parish = (Spinner) findViewById(R.id.parish);
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        activity = (Spinner) findViewById(R.id.activity);
        topic = (Spinner) findViewById(R.id.topic);
        entreprize = (Spinner) findViewById(R.id.enterpriseSpinner);
        entreprize2 = (Spinner) findViewById(R.id.enterprise2spinner);
        beneficiary_females = (EditText) findViewById(R.id.beneficiary_females);
        beneficiary_males = (EditText) findViewById(R.id.beneficiary_males);
        beneficiary_group = (EditText) findViewById(R.id.beneficiary_group);
        beneficiary_ref = (EditText) findViewById(R.id.beneficiary_ref);
        beneficiary_ref_contact = (EditText) findViewById(R.id.beneficiary_ref_contact);
        notes = (EditText) findViewById(R.id.notes);
        lessons = (EditText) findViewById(R.id.lessons);
        recommendations = (EditText) findViewById(R.id.recommendations);
        activity_description = (EditText) findViewById(R.id.activity_description);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        parish.setOnItemSelectedListener(this);
        district.setOnItemSelectedListener(this);
        // Loading spinner data from database
        loadSpinnerDataDistrict();
        loadSpinnerDataActivities();
        loadSpinnerDataTopics();
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

        entreprize2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = entreprize2.getSelectedItem().toString();
                if(entreprize2.getSelectedItemPosition() == 0){
                }else {
                    //manuplate the hash map
                    String g = enterprise2Values.get(text);
                    enterprise2Names = g.split("@");
                    enterprise2Input = enterprise2Names[1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });



        // Register Button Click event
        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String district_db = district.getSelectedItem().toString().trim();
                String parish_db = parish.getSelectedItem().toString().trim();
                String subcounnty_db = spinner.getSelectedItem().toString().trim();
                String village_db = spinner2.getSelectedItem().toString().trim();
                String activity_db = activity.getSelectedItem().toString().trim();
                String topic_db = topic.getSelectedItem().toString().trim();
                String entreprize_db = enterpriseInput;
                String entreprize_db2 = enterprise2Input;
                String ben_females = beneficiary_females.getText().toString().trim();
                String ben_males = beneficiary_males.getText().toString().trim();
                String ben_group = beneficiary_group.getText().toString().trim();
                String reference = beneficiary_ref.getText().toString().trim();
                String reference_contact = beneficiary_ref_contact.getText().toString().trim();
                String remarks = notes.getText().toString().trim();
                String lesssons_db = lessons.getText().toString().trim();
                String reccomendations_db = recommendations.getText().toString().trim();
                String activity_description_db = activity_description.getText().toString().trim();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);

                String hh = "";
                if(ben_females.isEmpty())
                    hh =  hh + "**Female beneficiaries \n ";
                if(ben_males.isEmpty())
                    hh =  hh + "**Male beneficiaries \n ";
                if(ben_group.isEmpty())
                    hh =  hh + "**Beneficiary group \n ";
                if(reference.isEmpty())
                    hh =  hh + "**Reference person name \n ";
                if(reference_contact.isEmpty())
                    hh =  hh + "**Reference person contact \n ";
                if(remarks.isEmpty())
                    hh =  hh + "**Remarks \n ";
                if(lesssons_db.isEmpty())
                    hh =  hh + "**Lessons \n ";
                if(reccomendations_db.isEmpty())
                    hh =  hh + "**Recommendations \n ";
                if(activity_description_db.isEmpty())
                    hh =  hh + "**Activity \n ";
                if(district_db == "---Select District---")
                    hh =  hh + " District \n ";
                if(subcounnty_db == "---Select Subcounty---")
                    hh =  hh + " Subcounty \n ";
                if(parish_db == "---Select Parish---")
                    hh =  hh + " Parish  \n ";
                if(village_db == "---Select Village---")
                    hh =  hh + " Village  \n ";
                if(topic_db == "---Select Topic---")
                    hh =  hh + " Topic \n ";
                if(entreprize_db == "---Select Entreprize---")
                    hh =  hh + " Entreprize  \n ";
                if(activity_db == "---Select Activity---")
                    hh =  hh + " Activity  \n ";

                if(hh != "") {
                    String errMsg = "Please enter the required details correctly: \n " + hh;
                    showMessageDialog("Error", errMsg);
                } else {
                    // database handler
                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                    db.addUnPlannedActivity(activity_db,topic_db,entreprize_db,subcounnty_db,village_db,ben_group,reference,reference_contact,ben_males,ben_females,remarks,lat,lng,parish_db,district_db,lesssons_db,reccomendations_db, activity_description_db);
                    showMessageDialog("Success", "Saved unplanned activity successfully...");

                    //Redirect User to Review Saved Data activity
                    Intent intent = new Intent(UnplannedActivityForm.this,
                            Main.class);
                    startActivity(intent);
                    finish();
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
                ug.go.agriculture.MAAIF_Extension.daes.dairy.Main.class);
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
        spinner.setAdapter(dataAdapter);
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
        spinner.setAdapter(dataAdapter);
    }


    /**
     * Function to load the spinner data from SQLite database activities
     * */
    private void loadSpinnerDataActivities() {
// database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Spinner Drop down elements
        List<String> lables = db.getAllActivites();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        activity.setAdapter(dataAdapter);
    }


    /**
     * Function to load the spinner data from SQLite database activities
     * */
    private void loadSpinnerDataTopics() {
// database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Spinner Drop down elements
        List<String> lables = db.getAllTopics();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        topic.setAdapter(dataAdapter);
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
    private void loadSpinnerDataEntreprizes() {
// database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Spinner Drop down elements
        List<String> lables = db.getAllEntreprizes();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        entreprize.setAdapter(dataAdapter);
        entreprize2.setAdapter(dataAdapter);
    }
    private void loadSpinnerDataEntreprizesDaily() {
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
                enterprises2List.add(key);
                enterprise2Values.put(key,Value);
            }

            ArrayAdapter<String> enterpriseAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprisesList);
            enterpriseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            entreprize.setAdapter(enterpriseAdapter);

            ArrayAdapter<String> enterprise2Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprises2List);
            enterprise2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            entreprize2.setAdapter(enterprise2Adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void loadSpinnerDataParish(String Dis, String Subcounty) {
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
        spinner2.setAdapter(dataAdapter);
    }


    //  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        if(parent.getId() == R.id.spinner)
        {
            String dis = district.getSelectedItem().toString() ;
            String subcounty = parent.getSelectedItem().toString() ;
            loadSpinnerDataParish(dis,subcounty);
        }
        else if(parent.getId() == R.id.parish)
        {
            String parish = parent.getSelectedItem().toString() ;
            String subcounty = spinner.getSelectedItem().toString() ;
            loadSpinnerDataVillage(parish,subcounty);
        }else if(parent.getId() == R.id.district)
        {   String district = parent.getSelectedItem().toString() ;
            loadSpinnerDataSubCounty(district);
        }

    }

    //  @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }





}
