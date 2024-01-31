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

public class ChangePassword extends Activity {
    private static final String TAG = "Change Password";
    private EditText passwordEditText;
    private Button changePasswordButton;
    private EditText repeatPasswordEditText;
    private String user_id;
    private String secret_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        passwordEditText = findViewById(R.id.new_password);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        repeatPasswordEditText = findViewById(R.id.repeat_password);

        // session id details
        user_id = getSessionAccountId();
        secret_code = getSecretCode();
        Log.d("User Account ID verification", user_id);
        Log.d("secret verification", secret_code);

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAccountPassword();
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

    private void changeAccountPassword() {
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = repeatPasswordEditText.getText().toString().trim();

        // check if passwords were entered
        if (password.isEmpty() || repeatPassword.isEmpty()) {
            showErrorDialog("Enter password in the fields");
            return;
        }
        // check if passwords match
        if (repeatPassword.equals(password)) {
            repeatPasswordEditText.setError("Passwords are not similar");
            repeatPasswordEditText.requestFocus();
            passwordEditText.requestFocus();
            return;
        }

        JSONObject jsonRequestData = new JSONObject();
        try {
            jsonRequestData.put("user_id", user_id);
            jsonRequestData.put("password", password);
            jsonRequestData.put("secret_code", secret_code);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "Error creating JSON request data: " + e.getMessage());
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FORGOT_PASSWORD_CHANGE_PASSWORD, jsonRequestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean error = response.getBoolean("error");
                            String message = response.getString("message");

                            // Check for error node in json
                            if (!error) {
                                /** change password successful **/

                                destroySession();

                                showErrorDialog(message);

                                // Launch verify account activity
                                Intent intent = new Intent(ChangePassword.this,
                                        Login.class);
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
                Log.e(TAG, "Volley Error: " + error.getMessage());
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
