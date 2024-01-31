package ug.go.agriculture.MAAIF_Extension.daes.grm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.Manifest;


import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;

public class WelcomeActivity extends Activity {

	LocationManager locationManager;
	String provider;
	private Button btnLoginActivity;
	private Button btnGetHelp;
	private Button btnGeneralDashboard;
	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

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

		// construct a new instance
		mLocation = new SimpleLocation(this);

		// reduce the precision to 5,000m for privacy reasons
		mLocation.setBlurRadius(5000);



		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);

		btnLoginActivity = (Button) findViewById(R.id.btnLoginActivity);
		btnGeneralDashboard = (Button) findViewById(R.id.btnGeneralDashboard);
		btnGetHelp = (Button) findViewById(R.id.btnGetHelp);

		btnGetHelp.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						HelpActivity.class);
				startActivity(i);
				finish();
			}
		});


//		btnLoginActivity.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View view) {
//                Intent i = new Intent(getApplicationContext(),
//                        LoginActivity.class);
//                startActivity(i);
//                finish();
//            }
//        });

        btnGeneralDashboard.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        Main.class);
                startActivity(i);
                finish();
            }
        });






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
								ActivityCompat.requestPermissions(WelcomeActivity.this,
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

	@Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage("Do you want exit the E-GRM app? ");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				finish();
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent i = new Intent(getApplicationContext(),
						Main.class);
				startActivity(i);
				finish();
			}
		});
		builder.show();
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
