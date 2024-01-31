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

public class VerifyAccount extends Activity {

    private static final String TAG = "Verify Forgot Password Account";
    private ProgressDialog pDialog;
    private EditText verificationCodeEditText;
    private Button verifyButton;
    private TextView resendCodeTextView;
    private String user_id;
    private String secret_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);

        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        verifyButton = findViewById(R.id.verifyButton);
        resendCodeTextView = findViewById(R.id.resendCodeTextView);

        // session id details
        user_id = getSessionAccountId();
        secret_code = getSecretCode();
        Log.d("User Account ID verification", user_id);
        Log.d("secret verification", secret_code);

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

    private String getSessionAccountId(){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("forgot_password_session", MODE_PRIVATE);

        // Get the data from session
        String account_id = sharedPreferences.getString("account_id", "");
        return account_id;
    }

    private String getSecretCode(){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("forgot_password_session", MODE_PRIVATE);

        // Get the data from session
        String account_secret_code = sharedPreferences.getString("secret_code", "");
        return account_secret_code;
    }

    private void destroySession(){
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("forgot_password_session", MODE_PRIVATE);

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
            jsonRequestData.put("secret_code", secret_code);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FORGOT_PASSWORD_VERIFY_ACCOUNT, jsonRequestData,
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
                                Intent intent = new Intent(VerifyAccount.this,
                                        ChangePassword.class);
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
        AppController.getInstance().addToRequestQueue(jsonObjReq, TAG);
    }

    private void resendVerificationCode() {
        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("user_id", user_id);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSON request Error", "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FORGOT_PASSWORD_RESEND_VERIFICATION_CODE, jsonRequestData,
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
                Login.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
