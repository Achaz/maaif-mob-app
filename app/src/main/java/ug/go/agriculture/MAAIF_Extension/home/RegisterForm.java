/**
 * Author: Herbert Musoke
 * URL: www.herbertmusoke.com
 * twitter: http://twitter.com/HerbertMusoke
 */
package ug.go.agriculture.MAAIF_Extension.home;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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

import afu.org.checkerframework.checker.nullness.qual.NonNull;
import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.daes.profiling.farmer.Review;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class RegisterForm extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = ug.go.agriculture.MAAIF_Extension.home.RegisterForm.class.getSimpleName();
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
    private EditText a8;
    private EditText a9;
    private Spinner a10;
    private Spinner a11;
    private Spinner a12;
    private Spinner a13;
    private Spinner a14;
    private Spinner a15;
    private Spinner a16;
    private ProgressDialog pDialog;
    //  private SQLiteHandler db;
    private SQLiteHandler mDBHelper;
    private String account_registration_url = "https://extension.agriculture.go.ug/?action=apiFarmerSelfRegistration";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private SimpleLocation mLocation;
    private Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {


        //Check if location is enabled
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Stay Silent
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }

		// construct a new instance
		mLocation = new SimpleLocation(this);

        db = new SQLiteHandler(getApplicationContext());
        // reduce the precision to 5,000m for privacy reasons
		mLocation.setBlurRadius(5000);

        final double a26 = mLocation.getLatitude();
        final double a27 = mLocation.getLongitude();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        mDBHelper = new SQLiteHandler(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);

        a1 = (EditText) findViewById(R.id.a1); // surname
        a2 = (EditText) findViewById(R.id.a2); // first name
        a3 = (Spinner) findViewById(R.id.a3); // farmer type
        a5 = (EditText) findViewById(R.id.a5); // email
        a6 = (EditText) findViewById(R.id.a6); // NIN number
        a7 = (EditText) findViewById(R.id.a7); // contact
        a8 = (EditText) findViewById(R.id.a8); // password
        a9 = (EditText) findViewById(R.id.a9); // repeat password
        a10 = (Spinner) findViewById(R.id.a10); // district
        a4 = (Spinner) findViewById(R.id.a4); // gender
        a11 = (Spinner) findViewById(R.id.a11); // subcounty
        a12 = (Spinner) findViewById(R.id.a12); // parish
        a13 = (Spinner) findViewById(R.id.a13); // village
        btnSaveEOI = (Button) findViewById(R.id.btnSaveEOI);

        // Spinner click listener
        a4.setOnItemSelectedListener(this);
        a10.setOnItemSelectedListener(this);
        a11.setOnItemSelectedListener(this);
        a12.setOnItemSelectedListener(this);


        // Fetching user details from the server
        String districtsUrl  = AppConfig.URL_DISTRICTS;
        fetchDataAndPopulateSpinner("districts", districtsUrl, "a10");

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Save Planned Activity Click event
        btnSaveEOI.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                new AlertDialog.Builder(ug.go.agriculture.MAAIF_Extension.home.RegisterForm.this)
                        .setTitle("Create New Account")
                        .setMessage("Are you sure you want to save? You will not be able to alter the data afterwards.")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String b1 = a1.getText().toString().trim();
                                String b2 = a2.getText().toString().trim();
                                String b3 = a3.getSelectedItem().toString().trim();
                                String b4 = a4.getSelectedItem().toString().trim();
                                String b5 = a5.getText().toString().trim(); // email
                                String b6 = a6.getText().toString().trim(); // NIN number
                                String b7 = a7.getText().toString().trim(); // contact
                                String b8 = a8.getText().toString().trim(); // password
                                String b9 = a9.getText().toString().trim(); // repeat password
                                String b10 = a10.getSelectedItem().toString().trim();
                                String b11 = a11.getSelectedItem().toString().trim();
                                String b12 = a12.getSelectedItem().toString().trim();
                                // getSelectedItemId()

                                if(a13.getSelectedItem() != null){
                                    String b13 = a13.getSelectedItem().toString().trim();

                                    String hh = "";
                                    // surname
                                    if(b1.isEmpty())
                                        hh =  hh + "**Surname \n ";

                                    // first name
                                    if(b2.isEmpty())
                                        hh =  hh + "**Firstname(s) \n ";

                                    // password
                                    if(b8.isEmpty())
                                        hh =  hh + "**Password \n ";

                                    // repeat password
                                    if(b9.isEmpty())
                                        hh =  hh + "**Password Again \n ";

                                    // farmer category
                                    if(b3.isEmpty())
                                        hh =  hh + "**Description of User  \n ";

                                    // gender
                                    if(b4 == "---Select Gender---" || b4.startsWith("---Select Gender---"))
                                        hh =  hh + "**Gender  \n ";

                                    // contact
                                    if(b7.isEmpty() || (b7.length() != 10))
                                        hh =  hh + "**Phone Number  \n ";

                                    // district
                                    if(b10 == ""  || b10 == null)
                                        hh =  hh + "**District \n ";

                                    // subcounty
                                    if(b11 == ""  || b11 == null)
                                        hh =  hh + "**Subcounty \n ";

                                    // parish
                                    if(b12 == ""  || b12 == null)
                                        hh =  hh + "**Parish  \n ";

                                    // village
                                    if(b13 == "" || b13 == null)
                                        hh =  hh + "**Village  \n ";

                                    // check if passwords match
                                    if (!(b8.equals(b9))) {
                                        // repeatPasswordEditText.setError("Passwords do not match");
                                        hh =  hh + "**Passwords do not match  \n ";
                                    }

                                    if(hh != ""){
                                        String errMsg = "Please enter the required details correctly: \n " + hh;
                                        showErrorDialog(errMsg);
                                    }else{
                                        int farmer_category = (int) a3.getSelectedItemId();
                                        int district = (int) a10.getSelectedItemId();
                                        int subcounty = (int) a11.getSelectedItemId();
                                        int parish = (int) a12.getSelectedItemId();
                                        // Register with server here
                                        checkRegistration(b1, b2, farmer_category, b4, b5, b6, b7, b8, district, subcounty, parish);
                                    }
                                } else {
                                    showErrorDialog("Please make sure you select your village...");
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

    // success dialog
    private void showSuccessDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congrats...")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
    }

    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Login.class);
        startActivity(i);
        finish();
    }


    /**
     * function to verify login details in mysql db
     **/
    private void checkRegistration(final String a1, final String a2, final int a3, final String a4, final String a5, final String a6, final String a7, final String a8, final int a10,  final int a11, final int a12) {
        Log.d("Into the sky: ", "Off we go to self register farmer...");
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("a1", a1);
            jsonRequestData.put("a2", a2);
            jsonRequestData.put("a3", a3);
            jsonRequestData.put("a4", a4);
            jsonRequestData.put("a5", a5);
            jsonRequestData.put("a6", a6);
            jsonRequestData.put("a7", a7);
            jsonRequestData.put("a8", a8);
            jsonRequestData.put("a10", a10);
            jsonRequestData.put("a11", a11);
            jsonRequestData.put("a12", a12);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, account_registration_url, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");
                            Log.d(TAG, "Server response: " + response.getString("message"));
                            if(status){
                                String error_message = response.getString("message");
                                error_message = error_message.toLowerCase();
                                if (error_message == "duplicate record"){
                                    error_message = "Account already exists. Please contact MAAIF for assistance...";
                                }
                                showErrorDialog(error_message);

                            } else {
                                // access user id and save it in local storage
                                String farmer_user_id = response.getString("user_id");
                                Log.d("User Account ID register", farmer_user_id);
                                saveAccountDetails(farmer_user_id);

                                // Launch verify account activity
                                Intent intent = new Intent(ug.go.agriculture.MAAIF_Extension.home.RegisterForm.this,
                                        ug.go.agriculture.MAAIF_Extension.home.VerifyAccountActivity.class);
                                startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error parsing JSON response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Volley error: " + error);
                    }
                }) {
            // If you need to add headers, override the getHeaders() method
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void saveAccountDetails(String user_id){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("farmer_self_registration_session", MODE_PRIVATE);

        // Get the SharedPreferences editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Put the data in session
        editor.putString("farmer_user_id", user_id);

        // Commit the changes to SharedPreferences
        editor.commit();
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

    private void fetchDataAndPopulateSpinner(String locations, String url, String spinnerVariable) {

        // Create a request queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // Create a string request to fetch the data from the server
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Process the response and convert it into a list of items for the spinner
                        List<Item> items = processResponse(response, locations);

                        // Create an ArrayAdapter to hold the items
                        ItemArrayAdapter adapter = new ItemArrayAdapter(RegisterForm.this, items);
//                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Populate the spinner with the items
                        int spinnerId = getResources().getIdentifier(spinnerVariable, "id", getPackageName());
                        Spinner locSpinner = findViewById(spinnerId);
                        locSpinner.setAdapter(adapter);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                Log.e("Volley Error", "Error fetching data: " + error.getMessage());
            }
        });

        // Add the string request to the request queue
        requestQueue.add(stringRequest);
    }

    private List<Item> processResponse(String response, String locations) {
        List<Item> items = new ArrayList<>();

        try {
            JSONObject jObj = new JSONObject(response);
            boolean error = jObj.getBoolean("error");

            // Check for error node in json
            if (!error) {

                // convert array
                JSONArray locs = jObj.getJSONArray(locations);
                int locs_count = locs.length();
                for(int i = 0; i < locs_count; i++)
                {
                    JSONObject locObj = locs.getJSONObject(i);
                    String id = locObj.getString("id");
                    String name = locObj.getString("name");
                    Log.d(TAG, locations+": "+name+id);
                    items.add(new Item(Integer.parseInt(id), name));
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

        return items;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.a10)
        {
            Log.d(TAG, "district spinner selected");
            Item selectedItem = (Item) a10.getSelectedItem();
            int selectedId = selectedItem.getId();
            String selectedName = selectedItem.getName();
            Log.d(TAG, selectedName + selectedId);
            String url = AppConfig.URL_DISTRICT_SUBCOUNTIES + selectedId;
            fetchDataAndPopulateSpinner("sub_counties", url, "a11");
        }
        else if(parent.getId() == R.id.a11)
        {
            Log.d(TAG, "sub-county spinner selected");
            Item selectedItem = (Item) a10.getSelectedItem();
            int selectedId = selectedItem.getId();
            String url = AppConfig.URL_DISTRICT_PARISHES + selectedId;
            fetchDataAndPopulateSpinner("parishes", url, "a12");
        }
        else if(parent.getId() == R.id.a12)
        {
            Log.d(TAG, "parish spinner selected");
            Item selectedItem = (Item) a10.getSelectedItem();
            int selectedId = selectedItem.getId();
            String url = AppConfig.URL_DISTRICT_VILLAGES + selectedId;
            fetchDataAndPopulateSpinner("villages", url, "a13");
        }

    }

  //  @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }


	@Override
	protected void onResume() {
//		super.onResume();

		// make the device update its location
//		mLocation.beginUpdates();

        super.onResume();
        requestLocationPermissions();
//        if (mLocation != null) {
//            mLocation.beginUpdates();
//        }
	}

	@Override
	protected void onPause() {
		// stop location updates (saves battery)
//		mLocation.endUpdates();

//		super.onPause();
//        if (mLocation != null) {
//            mLocation.endUpdates();
//        }
        super.onPause();
//        requestLocationPermissions();
	}


    private void requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permissions granted, proceed with accessing location services
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with accessing location services
            } else {
                // Permissions not granted, inform the user
                Toast.makeText(this, "Location permissions are required for this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadSpinnerDataEntreprizes() {
// database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        // Spinner Drop down elements
        List<String> lables = db.getAllEntreprizesList();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        a14.setAdapter(dataAdapter);
        a15.setAdapter(dataAdapter);
        a16.setAdapter(dataAdapter);

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
