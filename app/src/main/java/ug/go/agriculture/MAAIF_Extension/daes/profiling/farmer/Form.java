/**
 * Author: Herbert Musoke
 * URL: www.herbertmusoke.com
 * twitter: http://twitter.com/HerbertMusoke
 */
package ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Form extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = Form.class.getSimpleName();
    private Button btnSaveEOI;
    private SessionManager session;
    private SQLiteHandler db;
    private EditText a1;
    private EditText a2;
    private Spinner a3;
    private Spinner a4;
    private EditText a5;
    private EditText a6;
    private EditText a7;
    private Spinner a8;
    private Spinner a9;
    private Spinner a9x;
    private Spinner a10;
    private Spinner a11;
    private Spinner a12;
    private Spinner a13;
    private Spinner a14;
    private Spinner a15;
    private Spinner a16;
    private Spinner a17;
    private EditText a18;
    private TextView a18x;

    String enterpriseInput, enterprise2Input, enterprise3Input;

    // enterprise
    String enterpriseNames[];
    HashMap<String ,String> enterpriseValues = new HashMap<String, String>();
    List<String> enterprisesList = new ArrayList<String>();

    // enterprise2
    String enterprise2Names[];
    HashMap<String ,String> enterprise2Values = new HashMap<String, String>();
    List<String> enterprises2List = new ArrayList<String>();

    // enterprise3
    String enterprise3Names[];
    HashMap<String ,String> enterprise3Values = new HashMap<String, String>();
    List<String> enterprises3List = new ArrayList<String>();

    private SimpleLocation mLocation;
    private Context context;
    private ProgressDialog pDialog;
    private SQLiteHandler mDBHelper;
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

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        mDBHelper = new SQLiteHandler(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        // construct a new instance
		mLocation = new SimpleLocation(this);

        db = new SQLiteHandler(getApplicationContext());
        // reduce the precision to 5,000m for privacy reasons
		mLocation.setBlurRadius(5000);

        final double a26 = mLocation.getLatitude();
        final double a27 = mLocation.getLongitude();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_farmer);
        a1 = (EditText) findViewById(R.id.a1); // surname
        a2 = (EditText) findViewById(R.id.a2); // first name
        a3 = (Spinner) findViewById(R.id.a3); // farmer category
        a5 = (EditText) findViewById(R.id.a5); // email
        a6 = (EditText) findViewById(R.id.a6); // NIN
        a7 = (EditText) findViewById(R.id.a7); // phone number
        a8 = (Spinner) findViewById(R.id.a8); // education level
        a9 = (Spinner) findViewById(R.id.a9); // primary language
        a9x = (Spinner) findViewById(R.id.a9x); // secondary language
        a10 = (Spinner) findViewById(R.id.a10); // district
        a4 = (Spinner) findViewById(R.id.a4); // gender
        a11 = (Spinner) findViewById(R.id.a11); // sub county
        a12 = (Spinner) findViewById(R.id.a12); // parish
        a13 = (Spinner) findViewById(R.id.a13); // village
        a14 = (Spinner) findViewById(R.id.a14); // enterprise one
        a15 = (Spinner) findViewById(R.id.a15); // enterprise two
        a16 = (Spinner) findViewById(R.id.a16); // enterprise three
        a17 = (Spinner) findViewById(R.id.a17); // does farmer belong to aa group
        a18 = (EditText) findViewById(R.id.a18); // farmer group
        a18x = (TextView) findViewById(R.id.a18x);
        btnSaveEOI = (Button) findViewById(R.id.btnSaveEOI);

        // Spinner click listener
        a4.setOnItemSelectedListener(this);
        a11.setOnItemSelectedListener(this);
        a12.setOnItemSelectedListener(this);
        a10.setOnItemSelectedListener(this);
        a17.setOnItemSelectedListener(this);

        // Fetching user details from SQLite
        loadSpinnerDataDistrict();
        loadSpinnerDataEntreprizes();

        // Session manager
        session = new SessionManager(getApplicationContext());

        a14.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = a14.getSelectedItem().toString();
                if(a14.getSelectedItemPosition() == 0){
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

        a15.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = a15.getSelectedItem().toString();
                if(a15.getSelectedItemPosition() == 0){
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

        a16.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String text = a16.getSelectedItem().toString();
                if(a16.getSelectedItemPosition() == 0){
                }else {
                    //manuplate the hash map
                    String g = enterprise3Values.get(text);
                    enterprise3Names = g.split("@");
                    enterprise3Input = enterprise3Names[1];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });

        // Save Farmer Click event
        btnSaveEOI.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new AlertDialog.Builder(ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer.Form.this)
                        .setTitle("Save Farmer Profile")
                        .setMessage("Are you sure you want to save this farmer profile?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String b1 = a1.getText().toString().trim(); // surname
                                String b2 = a2.getText().toString().trim(); // first name
                                String b3 = a3.getSelectedItem().toString().trim(); // farmer category
                                int farmer_category = a3.getSelectedItemPosition();
                                String b4 = a4.getSelectedItem().toString().trim(); // gender
                                String b5 = a5.getText().toString().trim(); // email
                                String b6 = a6.getText().toString().trim(); // NIN
                                String b7 = a7.getText().toString().trim(); // phone number
                                String b8 = a8.getSelectedItem().toString().trim(); // education level
                                int education_level = a8.getSelectedItemPosition();
                                String b9 = a9.getSelectedItem().toString().trim(); // primary language
                                int primary_language = a9.getSelectedItemPosition();
                                String b9x = a9x.getSelectedItem().toString().trim(); // secondary language
                                int secondary_language = a9x.getSelectedItemPosition();
                                String b10 = a10.getSelectedItem().toString().trim(); // district
                                String b11 = a11.getSelectedItem().toString().trim(); // sub-county
                                String b12 = a12.getSelectedItem().toString().trim(); // parish
                                String b13 = a13.getSelectedItem().toString().trim(); // village
                                String b14 = enterpriseInput; // Enterprise one
                                String b15 = enterprise2Input; // Enterprise two
                                String b16 = enterprise3Input; // Enterprise three
                                String b17 = a17.getSelectedItem().toString().trim(); // does farmer belong to a group?
                                String b18 = a18.getText().toString().trim(); // farmer group
                                int synced = 0;

                                // Save farmer profile to database
                                String hh = "";
                                if(b1.isEmpty())
                                    hh =  hh + "**Surname \n ";

                                if(b2.isEmpty())
                                    hh =  hh + "**Firstname(s) \n ";

                                if(b3.isEmpty())
                                    hh =  hh + "**Farmer category  \n ";

                                if(b4 == "---Select Gender---" || b4.startsWith("---Select Gender---"))
                                    hh =  hh + "**Gender  \n ";

                                if(b6.isEmpty())
                                    hh =  hh + "**NIN number \n ";

                                if(b7.isEmpty() || (b7.length() != 10))
                                    hh =  hh + "**Phone Number  \n ";

                                if(b8 == "---Select Option---" || b8.startsWith("---Select Option---"))
                                    hh =  hh + "**Education Level  \n ";

                                if(b9 == "---Select Option---" || b9.startsWith("---Select Option---"))
                                    hh =  hh + "**Primary language  \n ";

                                if(b10 == "---Select District---" || b10.startsWith("---Select District---"))
                                    hh =  hh + "**District  \n ";

                                if(b11 == "---Select Subcounty---"  || b11.startsWith("---Select Subcounty---"))
                                    hh =  hh + "**Subcounty \n ";

                                if(b12 == "---Select Parish---"  || b12.startsWith("---Select Parish---"))
                                    hh =  hh + "**Parish  \n ";

                                if(b13 == "---Select Village---" || b13.startsWith("---Select Village---"))
                                    hh =  hh + "**Village  \n ";

                                //if(hh != ""){
                                // if (userEmail != null && !userEmail.isEmpty() && !userEmail.equals("null"))
                                if (!hh.isEmpty()){
                                    String errMsg = "Please enter the required details correctly: \n " + hh;
                                    showMessageDialog("Error", errMsg);
                                }else{
                                    // Save farmer to database
                                    Map<String, Object> add_farmer_profile = mDBHelper.addFarmer(b1, b2, b3, b4, b7, b5, b6, b8, b9, b9x, b10, b11, b12, b13, b14, b15, b16, b17, b18, synced, farmer_category, education_level, primary_language, secondary_language).get(0);
                                    String msg = (String) add_farmer_profile.get("message");
                                    boolean isSuccessful = (boolean) add_farmer_profile.get("success");

                                    if (isSuccessful){
                                        // reset fields
                                        a1.setText(""); // surname
                                        a2.setText(""); // first name
                                        a3.setSelection(0); // farmer category
                                        a4.setSelection(0); // gender
                                        a5.setText(""); // email
                                        a6.setText(""); // NIN
                                        a7.setText(""); // phone number
                                        a8.setSelection(0); // education level
                                        a9.setSelection(0); // primary language
                                        a9x.setSelection(0); // secondary language
                                        a10.setSelection(0); // district
                                        a11.setSelection(0); // sub-county
                                        a12.setSelection(0); // parish
                                        a13.setSelection(0); // village
                                        a14.setSelection(0); // Enterprise one
                                        a15.setSelection(0); // Enterprise two
                                        a16.setSelection(0); // Enterprise three
                                        a17.setSelection(0); // does farmer belong to a group?
                                        a18.setText(""); // farmer group

                                        showMessageDialog("Success", msg);
                                    } else{
                                        showMessageDialog("Error", msg);
                                    }
                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

    }

    // error dialog
    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
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


    /**
     * function to verify login details in mysql db
     * */
    private void checkRegistration(final String b1, final String b2, final String b3,final String b4, final String b5, final String b6,final String b7, final String b8, final String b9,final String b10, final String b11 ) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Registering  and Logging User in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_NEW, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        //String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String first_name = user.getString("first_name");
                        String last_name = user.getString("last_name");
                        String username = user.getString("username");
                        String email = user.getString("email");
                        String password = user.getString("password");
                        String phone = user.getString("phone");
                        String subcounty = user.getString("subcounty");
                        String district = user.getString("district");
                        String user_category = user.getString("user_category");
                        String uid = user.getString("uid");
                        String user_category_id = user.getString("user_category_id");
                        String photo = user.getString("photo");
                        String gender = user.getString("gender");
                        String created = user.getString("created");

                        // Inserting row in users table
                        mDBHelper.addUser(first_name,last_name,username,email,password,phone,subcounty,district,user_category,photo,gender,created,user_category_id,uid, "00");


                        // Launch main activity
                        Intent intent = new Intent(Form.this,
                                Main.class);
                        startActivity(intent);
                        finish();


                    } else {
                        // Error in login. Get the error message
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
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("a1", b1);
                params.put("a2", b2);
                params.put("a3", b3);
                params.put("a4", b4);
                params.put("a5", b5);
                params.put("a6", b6);
                params.put("a7", b7);
                params.put("a8", b8);
                params.put("a9", b9);
                params.put("a10", b10);
                params.put("a11", b11);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
        a10.setAdapter(dataAdapter);
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
        a11.setAdapter(dataAdapter);

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
        a12.setAdapter(dataAdapter);
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
        a13.setAdapter(dataAdapter);

    }


    /**
     * Function to load the spinner data from SQLite database
     * */

    //  @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {

        if(parent.getId() == R.id.a10)
        {

            String Dis = a10.getSelectedItem().toString();
            loadSpinnerDataSubCounty(Dis);
        }
        else if(parent.getId() == R.id.a11)
        {

            String Dis = a10.getSelectedItem().toString();
            String subcounty = parent.getSelectedItem().toString() ;
            loadSpinnerDataParish(Dis,subcounty);
        }
        else if(parent.getId() == R.id.a12)
        {
            String a12 = parent.getSelectedItem().toString() ;
            String subcounty = a11.getSelectedItem().toString() ;
            loadSpinnerDataVillage(a12,subcounty);
        }
        else if(parent.getId() == R.id.a17)
        {
            String a17 = parent.getSelectedItem().toString() ;
            if(a17.startsWith("Yes")){

             a18.setVisibility(View.VISIBLE);
             a18x.setVisibility(View.VISIBLE);
            }
            else
                if(a17.startsWith("No")){

                    a18.setVisibility(View.GONE);
                    a18x.setVisibility(View.GONE);
                }
                else
                {
                    //Keep Silent
                    a18.setVisibility(View.GONE);
                    a18x.setVisibility(View.GONE);
                }
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


    private void loadSpinnerDataEntreprizes() {
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

                enterprises3List.add(key);
                enterprise3Values.put(key,Value);
            }

            ArrayAdapter<String> enterpriseAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprisesList);
            enterpriseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            a14.setAdapter(enterpriseAdapter);

            ArrayAdapter<String> enterprise2Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprises2List);
            enterprise2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            a15.setAdapter(enterprise2Adapter);

            ArrayAdapter<String> enterprise3Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, enterprises2List);
            enterprise3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            a16.setAdapter(enterprise3Adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //int month = calendar.get(Calendar.MONTH);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            //int day = calendar.get(Calendar.DAY_OF_MONTH);

            /*
                We should use THEME_HOLO_LIGHT, THEME_HOLO_DARK or THEME_TRADITIONAL only.

                The THEME_DEVICE_DEFAULT_LIGHT and THEME_DEVICE_DEFAULT_DARK does not work
                perfectly. This two theme set disable color of disabled dates but users can
                select the disabled dates also.

                Other three themes act perfectly after defined enabled date range of date picker.
                Those theme completely hide the disable dates from date picker object.
             */
            DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);

            dpd.getDatePicker().findViewById(getResources().getIdentifier("day","id","android")).setVisibility(View.GONE);
            dpd.getDatePicker().findViewById(getResources().getIdentifier("month","id","android")).setVisibility(View.GONE);

            /*
                add(int field, int value)
                    Adds the given amount to a Calendar field.
             */
            // Add 3 days to Calendar
            calendar.add(Calendar.DATE, -6570);

            /*
                getTimeInMillis()
                    Returns the time represented by this Calendar,
                    recomputing the time from its fields if necessary.

                getDatePicker()
                Gets the DatePicker contained in this dialog.

                setMinDate(long minDate)
                    Sets the minimal date supported by this NumberPicker
                    in milliseconds since January 1, 1970 00:00:00 in getDefault() time zone.

                setMaxDate(long maxDate)
                    Sets the maximal date supported by this DatePicker in milliseconds
                    since January 1, 1970 00:00:00 in getDefault() time zone.
             */

            // Set the Calendar new date as maximum date of date picker
            dpd.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            // Subtract 6 days from Calendar updated date
            calendar.add(Calendar.DATE, -36500);

            // Set the Calendar new date as minimum date of date picker
            dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());

            // So, now date picker selectable date range is 7 days only

            // Return the DatePickerDialog
            return  dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            // Do something with the chosen date
            TextView tv = (TextView) getActivity().findViewById(R.id.a3);

            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();

            // Format the date using style and locale
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
            String formattedDate = df.format(chosenDate);

            // Display the chosen date to app interface
            tv.setText(formattedDate);
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
}
