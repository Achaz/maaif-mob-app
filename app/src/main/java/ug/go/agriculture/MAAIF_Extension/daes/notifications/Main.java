package ug.go.agriculture.MAAIF_Extension.daes.notifications;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.HashMap;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class Main extends Activity {
	
	String provider;
	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

	private TextView txtName;
	private TextView txtDistrict;
	private TextView location;
	private FloatingActionButton btnPendingFarmerQns;
	private FloatingActionButton btnReviewAnsweredQns;
	private FloatingActionButton btnGoBack;
	private static final int REQUEST_INTERNET = 200;

    private SQLiteHandler mDBHelper;
    private SQLiteDatabase mDb;
	private SessionManager session;
	private SimpleLocation mLocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_market);

		// database handler
		SQLiteHandler db = new SQLiteHandler(getApplicationContext());


		txtName = (TextView) findViewById(R.id.name);
		location = (TextView) findViewById(R.id.location);
		txtDistrict = (TextView) findViewById(R.id.district);
		btnPendingFarmerQns = (FloatingActionButton) findViewById(R.id.btnPendingFarmerQns);
		btnReviewAnsweredQns = (FloatingActionButton) findViewById(R.id.btnReviewAnsweredQns);
		btnGoBack = (FloatingActionButton) findViewById(R.id.btnGoBack);


		// construct a new instance
		mLocation = new SimpleLocation(this);


    	// reduce the precision to 5,000m for privacy reasons
		mLocation.setBlurRadius(5000);

		final double latitudes = mLocation.getLatitude();
		final double longitudes = mLocation.getLongitude();
		location.setText("Live Location:  Lat: " + latitudes + ", Long: " + longitudes);

		// SqLite database handler
		//db = new SQLiteHandler(getApplicationContext());

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


		// Fetching user details from SQLite
		HashMap<String, String> user = mDBHelper.getUserDetails();

		String name = user.get("first_name")+" "+ user.get("last_name") + "("+ user.get("user_category") + ")";
		String email = user.get("email");

		String district = user.get("district");
		if ( !user.get("subcounty").isEmpty() && user.get("subcounty") != null)
			district = district + ", " + user.get("subcounty");

		// Displaying the user details on the screen
		txtName.setText(name);
		txtDistrict.setText(district);




		btnPendingFarmerQns.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        ug.go.agriculture.MAAIF_Extension.home.Main.class);
                startActivity(i);
                finish();
            }
        });



		btnReviewAnsweredQns.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        ug.go.agriculture.MAAIF_Extension.home.Main.class);
                startActivity(i);
                finish();
            }
        });


    }





	@Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
		Intent i = new Intent(getApplicationContext(),
				ug.go.agriculture.MAAIF_Extension.home.Main.class);
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




}
