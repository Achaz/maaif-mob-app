package ug.go.agriculture.MAAIF_Extension.home;

//import static androidx.core.app.NotificationCompatJellybean.TAG;
//import androidx.core.app.NotificationCompatJellybean;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
//import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
//import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;

public class VerifyAccountActivity extends AppCompatActivity {

    private EditText verificationCodeEditText;
    private Button verifyButton;
    private TextView resendCodeTextView;
    private ProgressDialog pDialog;
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        verifyButton = findViewById(R.id.verifyButton);
        resendCodeTextView = findViewById(R.id.resendCodeTextView);

        // session id details
        user_id = getSessionAccountId();
        Log.d("User Account ID verification", String.valueOf(user_id));

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAccount();
            }
        });

        resendCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode();
            }
        });
    }

    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
    }

    private int getSessionAccountId(){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("farmer_self_registration_session", MODE_PRIVATE);

        // Get the data from session
        String farmer_user_id = sharedPreferences.getString("farmer_user_id", "");
        return Integer.parseInt(farmer_user_id);
    }

    private void destroySession(){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("farmer_self_registration_session", MODE_PRIVATE);

        // Get the SharedPreferences editor
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Clear the data in session
        editor.clear();

        // Commit the changes to SharedPreferences
        editor.commit();
    }

    private void verifyAccount() {
        String code = verificationCodeEditText.getText().toString().trim();

        if (code.isEmpty() || code.length() != 5) {
            verificationCodeEditText.setError("Please enter a valid 5-digit code");
            verificationCodeEditText.requestFocus();
            return;
        }

        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("user_id", user_id);
            jsonRequestData.put("verification_code", code);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("Verify Account", "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_VERIFY_ACCOUNT, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean error = response.getBoolean("error");
                            String message = response.getString("message");

                            // Check for error node in json
                            if (!error) {
                                /** verification successful **/

                                destroySession();

                                showErrorDialog(message);

                                // Launch verify account activity
                                Intent intent = new Intent(ug.go.agriculture.MAAIF_Extension.home.VerifyAccountActivity.this,
                                        ug.go.agriculture.MAAIF_Extension.home.Login.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Error in login. Get the error message
                                showErrorDialog(message);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            String errMsg = e.getMessage();
                            Log.e("JSON Error", errMsg);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Verify Account", "Volley Error: " + error.getMessage());
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

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, "req_verify_account");
    }

    private void resendVerificationCode() {
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("user_id", user_id);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSON request Error", "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_RESEND_VERIFICATION_CODE, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean error = response.getBoolean("error");
                            String message = response.getString("message");

                            // Check for error node in json
                            if (!error) {
                                showErrorDialog(message);
                            } else {
                                // Error in login. Get the error message
                                message = message + " Contact MAAIF for assistance...";
                                showErrorDialog(message);
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Log.d("JSON Error", "Json error: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Error", error.getMessage());
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

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, "req_resend_verification_code");
    }

    @Override public void onBackPressed() { AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(getApplicationContext(),
                RegisterForm.class);
        startActivity(i);
        finish();
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
