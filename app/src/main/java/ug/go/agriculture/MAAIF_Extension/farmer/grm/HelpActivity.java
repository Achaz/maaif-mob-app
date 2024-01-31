package ug.go.agriculture.MAAIF_Extension.farmer.grm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import im.delight.android.location.SimpleLocation;
import ug.go.agriculture.MAAIF_Extension.R;


public class HelpActivity extends Activity {


	private Button btnExit;
	private ImageView screen1;

    private SimpleLocation mLocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {


		//Check if location is enabled
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			//Stay Silent
			//Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
		}else{
			showGPSDisabledAlertToUser();
		}


		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

        btnExit = (Button) findViewById(R.id.btnExit);
		Button btnHelpOnline = (Button) findViewById(R.id.btnHelpOnline);




    	// construct a new instance
		mLocation = new SimpleLocation(this);


    	// reduce the precision to 5,000m for privacy reasons
		mLocation.setBlurRadius(5000);



		btnHelpOnline.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						GRMHelpOnlineActivity.class);
				startActivity(i);
				finish();
			}
		});


        btnExit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        WelcomeActivity.class);
                startActivity(i);
                finish();
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



}
