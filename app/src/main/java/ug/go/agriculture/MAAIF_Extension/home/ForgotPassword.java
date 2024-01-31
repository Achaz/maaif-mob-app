package ug.go.agriculture.MAAIF_Extension.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class ForgotPassword extends Activity {
    private static final String TAG = "Forgot Password";
    private Button btnSubmit;
    private EditText inputContact;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        inputContact = (EditText) findViewById(R.id.contact);
        btnSubmit = (Button) findViewById(R.id.submitButton);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // submit button Click Event
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String contact = inputContact.getText().toString().trim();

                // Check for empty data in the form
                if (!contact.isEmpty()) {
                    // check if account exists
                    checkUserDetails(contact);

                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                                    "Please enter the the contact you used on account creation!", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    /**
     * function to verify account details from the server
     * */
    private void checkUserDetails(final String contact) {
        Log.d("Into the sky: ", "Off we go to forgot password...");
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("phone_number", contact);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FORGOT_PASSWORD, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Boolean status = response.getBoolean("error");
                            Log.d(TAG, "Server response: " + response.getString("message"));
                            if(status){
                                String error_message = response.getString("message");
                                showErrorDialog(error_message);

                            } else {
                                // access user id and save it in local storage
                                String account_id = response.getString("user_id");
                                String code = response.getString("code");
                                Log.d("User Account ID forgot password", account_id);
                                saveAccountDetails(account_id, code);

                                // Launch verify account activity
                                Intent intent = new Intent(ForgotPassword.this,
                                        VerifyAccount.class);
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

    private void saveAccountDetails(String user_id, String uid){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("forgot_password_session", MODE_PRIVATE);

        // Get the SharedPreferences editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Put the data in session
        editor.putString("account_id", user_id);
        editor.putString("secret_code", uid);

        // Commit the changes to SharedPreferences
        editor.commit();
    }

    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                Login.class);
        startActivity(i);
        finish();
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
