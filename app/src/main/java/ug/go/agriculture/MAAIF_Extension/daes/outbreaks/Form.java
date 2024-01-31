package ug.go.agriculture.MAAIF_Extension.daes.outbreaks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.daes.dairy.PlannedActivityForm;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Form extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = Form.class.getSimpleName();
    private Button btnSaveActivity;
    private Spinner entreprize;
//    private EditText farmerPhone;
//    private EditText farmerName;
//    private EditText question;
//    private Spinner gReceipt;
    private SessionManager session;
    private SQLiteHandler db;
    private Spinner district;
    private Spinner subcounty;
    private Spinner parish;
    private Spinner village;
    private TextView a49;
    private Button btn;
    private Spinner a1;
    private EditText a2;
    private EditText a3;
    private EditText a4;
    private Spinner a5;
    private Spinner a6;
    private Spinner a7;
    private Spinner a8;
    private TextView a1ax;
    private TextView a1bx;
    private TextView a1cx;
    private Spinner a1a;
    private Spinner a1b;
    private EditText a1c;
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
        setContentView(R.layout.form_outbreaks);
        district = (Spinner) findViewById(R.id.district);
        subcounty = (Spinner) findViewById(R.id.subcounty);
        parish = (Spinner) findViewById(R.id.parish);
        village = (Spinner) findViewById(R.id.village);
        entreprize = (Spinner) findViewById(R.id.enterpriseSpinner);
        a49 = (TextView) findViewById(R.id.a49);
        btn = (Button) findViewById(R.id.btn);
        btnSaveActivity = (Button) findViewById(R.id.btnSaveActivity);
        a1 = (Spinner) findViewById(R.id.a1);
        a2 = (EditText) findViewById(R.id.a2);
        a3 = (EditText) findViewById(R.id.a3);
        a4 = (EditText) findViewById(R.id.a4);
        a5 = (Spinner) findViewById(R.id.a5);
        a6 = (Spinner) findViewById(R.id.a6);
        a7 = (Spinner) findViewById(R.id.a7);
        a8 = (Spinner) findViewById(R.id.a8);
        a1ax = (TextView) findViewById(R.id.a1ax);
        a1bx = (TextView) findViewById(R.id.a1bx);
        a1cx = (TextView) findViewById(R.id.a1cx);
        a1a = (Spinner) findViewById(R.id.a1a);
        a1b = (Spinner) findViewById(R.id.a1b);
        a1c = (EditText) findViewById(R.id.a1c);


        //Hide Nature and Types of Events
        a1a.setVisibility(View.GONE);
        a1b.setVisibility(View.GONE);
        a1c.setVisibility(View.GONE);
        a1ax.setVisibility(View.GONE);
        a1bx.setVisibility(View.GONE);
        a1cx.setVisibility(View.GONE);

        // Spinner click listener
        district.setOnItemSelectedListener(this);
        subcounty.setOnItemSelectedListener(this);
        parish.setOnItemSelectedListener(this);
        a1.setOnItemSelectedListener(this);





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


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize a new date picker dialog fragment
                DialogFragment dFragment = new DatePickerFragment();
                // Show the date picker dialog fragment
                dFragment.show(getFragmentManager(), "Date Picker");
            }
        });

        // Save Planned Activity Click event
        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String district_db = district.getSelectedItem().toString().trim();
                String parish_db = parish.getSelectedItem().toString().trim();
                String subcounnty_db = subcounty.getSelectedItem().toString().trim();
                String village_db = village.getSelectedItem().toString().trim();
                String entreprize_db = enterpriseInput;
                String date_db = a49.getText().toString().trim();
                String a1_db = a1.getSelectedItem().toString().trim();
                String a1a_db = a1a.getSelectedItem().toString().trim();
                String a1b_db = a1b.getSelectedItem().toString().trim();
                String a1c_db = a1c.getText().toString().trim();
                String a2_db = a2.getText().toString().trim();
                String a3_db = a3.getText().toString().trim();
                String a4_db = a4.getText().toString().trim();
                String a5_db = a5.getSelectedItem().toString().trim();
                String a6_db = a6.getSelectedItem().toString().trim();
                String a7_db = a7.getSelectedItem().toString().trim();
                String a8_db = a8.getSelectedItem().toString().trim();
                String lat = Double.toString(latitudes);
                String lng = Double.toString(longitudes);

                String hh = "";
                if(date_db.isEmpty())
                    hh =  hh + "**date \n ";
                if(a2_db.isEmpty())
                    hh =  hh + "**Outbreak description \n ";
                if(a3_db.isEmpty())
                    hh =  hh + "**Reference person name \n ";
                if(a4_db.isEmpty())
                    hh =  hh + "**Reference person contact \n ";
                if(a5_db == "---Select Option---")
                    hh =  hh + "**If livestock were affected \n ";
                if(a6_db == "---Select Option---")
                    hh =  hh + "**If plants/crops were affected \n ";
                if(a7_db == "---Select Option---")
                    hh =  hh + "**If fish were affected \n ";
                if(a8_db == "---Select Option---")
                    hh =  hh + "**Rate \n ";
                if(district_db == "---Select District---")
                    hh =  hh + " District \n ";
                if(subcounnty_db == "---Select Subcounty---")
                    hh =  hh + " Subcounty \n ";
                if(parish_db == "---Select Parish---")
                    hh =  hh + " Parish  \n ";
                if(village_db == "---Select Village---")
                    hh =  hh + " Village  \n ";
                if(a1_db == "---Select Event Type---")
                    hh =  hh + " Event type \n ";
                if(a1_db == "Outbreak" && a1c_db.isEmpty()) {
                    hh =  hh + "**Name of disease \n ";
                }
                if(entreprize_db == "---Select Entreprize---")
                    hh =  hh + " Entreprize  \n ";
                if(a1a_db == "---Select Option---")
                    hh =  hh + " Outbreak type  \n ";

                if(hh != "") {
                    String errMsg = "Please enter the required details correctly: \n " + hh;
                    showMessageDialog("Error", errMsg);
                } else {
                    // database handler
                    db.addOutbreakCrisis(district_db,subcounnty_db,parish_db,village_db,lat,lng,entreprize_db,date_db,a1_db,a2_db,a3_db,a4_db,a5_db,a6_db,a7_db,a8_db,a1a_db,a1b_db,a1c_db);
                    Toast.makeText(getApplicationContext(), "Saved reported crisis/outbreak successfully...", Toast.LENGTH_SHORT).show();

                    //Redirect User to Review Saved Data activity
                    Intent intent = new Intent(Form.this,
                            Main.class);
                    startActivity(intent);
                    finish();
                }


//                if (    !subcounnty_db.startsWith("---") &&
//                        !village_db.startsWith("---") &&
//                        !district_db.startsWith("---") &&
//                        !parish_db.startsWith("---") &&
//                        !entreprize_db.isEmpty() ) {
//
//                    // database handler
//                    db.addOutbreakCrisis(district_db,subcounnty_db,parish_db,village_db,lat,lng,entreprize_db,date_db,a1_db,a2_db,a3_db,a4_db,a5_db,a6_db,a7_db,a8_db,a1a_db,a1b_db,a1c_db);
//
//                    Toast.makeText(getApplicationContext(),
//                            "Reported Crisis/Outbreak Successfully!", Toast.LENGTH_LONG)
//                            .show();
//
//
//                    //Redirect User to Review Saved Data activity
//                    Intent intent = new Intent(Form.this,
//                            Main.class);
//                    startActivity(intent);
//                    finish();
//
//                } else {
//                    String hh = "Please enter the required details correctly: \n ";
//                    if(district_db == "---Select District---")
//                        hh =  hh + " District \n ";
//                    if(subcounnty_db == "---Select Subcounty---")
//                        hh =  hh + " Subcounty \n ";
//                    if(parish_db == "---Select Parish---")
//                        hh =  hh + " Parish  \n ";
//                    if(village_db == "---Select Village---")
//                        hh =  hh + " Village  \n ";
////                    if(topic_db == "---Select Topic---")
////                        hh =  hh + " Topic \n ";
////                    if(entreprize_db == "---Select Entreprize---")
////                        hh =  hh + " Entreprize  \n ";
////                    if(activity_db == "---Select Activity---")
////                        hh =  hh + " Activity  \n ";
////                    if(ben_group.isEmpty())
////                        hh =  hh + " Beneficiary Group \n ";
////                    if(reference.isEmpty())
////                        hh =  hh + " Reference Person \n ";
////                    if(reference_contact.isEmpty())
////                        hh =  hh + " Reference Person Contact  \n ";
////                    if(ben_females.isEmpty())
////                        hh =  hh + " Males Reached  \n ";
////                    if(ben_males.isEmpty())
////                        hh =  hh + " Females Reached  \n ";
////                    if(remarks.isEmpty())
////                        hh =  hh + " Challenges faced on the Activity  \n ";
////                    if(lesssons_db.isEmpty())
////                        hh =  hh + " Lessons learnt on the Activity  \n ";
////                    if(reccomendations_db.isEmpty())
////                        hh =  hh + " Recommendations about on the Activity  \n ";
//                     Toast toast= Toast.makeText(getApplicationContext(),
//                            hh, Toast.LENGTH_LONG);
//                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
//                    toast.show();
//                }
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
                ug.go.agriculture.MAAIF_Extension.daes.outbreaks.Main.class);
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
        }
        else if(parent.getId() == R.id.district)
        {   String district = parent.getSelectedItem().toString() ;
            loadSpinnerDataSubCounty(district);
        }
        else if(parent.getId() == R.id.a1)
        {   String valuex = parent.getSelectedItem().toString() ;

            if(valuex.startsWith("Outbreak")){
                a1a.setVisibility(View.VISIBLE);
                a1b.setVisibility(View.GONE);
                a1c.setVisibility(View.VISIBLE);
                a1ax.setVisibility(View.VISIBLE);
                a1bx.setVisibility(View.GONE);
                a1cx.setVisibility(View.VISIBLE);
            }
            else if(valuex.startsWith("Crisis"))
            {
                a1a.setVisibility(View.GONE);
                a1b.setVisibility(View.VISIBLE);
                a1c.setVisibility(View.GONE);
                a1ax.setVisibility(View.GONE);
                a1bx.setVisibility(View.VISIBLE);
                a1cx.setVisibility(View.GONE);
            }
            else
            {
                a1a.setVisibility(View.GONE);
                a1b.setVisibility(View.GONE);
                a1c.setVisibility(View.GONE);
                a1ax.setVisibility(View.GONE);
                a1bx.setVisibility(View.GONE);
                a1cx.setVisibility(View.GONE);
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


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog dpd = new DatePickerDialog(getActivity(),
                    AlertDialog.THEME_HOLO_LIGHT,this,year,month,day);

            // Add 1000 days to Calendar
            calendar.add(Calendar.MONTH, 6);

            // Set the Calendar new date as maximum date of date picker
            dpd.getDatePicker().setMaxDate(calendar.getTimeInMillis());

            // Subtract 6 days from Calendar updated date
            calendar.add(Calendar.MONTH, -12);

            // Set the Calendar new date as minimum date of date picker
            dpd.getDatePicker().setMinDate(calendar.getTimeInMillis());

            // So, now date picker selectable date range is 7 days only

            // Return the DatePickerDialog
            return  dpd;
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            // Do something with the chosen date
            TextView tv = (TextView) getActivity().findViewById(R.id.a49);

            // Create a Date variable/object with user chosen date
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();

            // Format the date using style and locale
            SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = dt1.format(chosenDate);

            // Display the chosen date to app interface
            tv.setText(formattedDate);
        }
    }








}
