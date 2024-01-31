/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 */
package ug.go.agriculture.MAAIF_Extension.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

public class Login extends Activity {
    private static final String TAG = Login.class.getSimpleName();
    private Button btnLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    //  private SQLiteHandler db;
    private SQLiteHandler mDBHelper;
    private SQLiteDatabase db;
    private TextView sign_up;
    private TextView forgot_password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_advanced);

        sign_up = (TextView) findViewById(R.id.sign_up);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        mDBHelper = new SQLiteHandler(this);

        try {
            mDBHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            db = mDBHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }


        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Login.this, Main.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);

                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                                    "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch register activity
                Intent intent = new Intent(Login.this,
                        RegisterForm.class);
                startActivity(intent);
                finish();
            }
        });

        forgot_password.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Launch register activity
                Intent intent = new Intent(Login.this,
                        ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
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
                        String district_id = user.getString("district_id");

                        // Inserting row in users table
                        mDBHelper.addUser(first_name,last_name,username,email,password,phone,subcounty,district,user_category,photo,gender,created,user_category_id,uid,district_id);

                        // reset login
                        mDBHelper.resetIsLoggedIn();

                        if(user_category_id.startsWith("50")){ // Farmer
                            // Launch main farmer activity
                            Intent intent = new Intent(Login.this, MainFarmer.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            // Launch main activity
                            Intent intent = new Intent(Login.this, Main.class);
                            startActivity(intent);
                            finish();
                        }
                        // if(user_category_id.startsWith("50")){}else{}

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Log.d("Error:",  e.getMessage());
                    // JSON error
                    // e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Unable to login. Please check your internet connection..", Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Unable to login. Please check your internet connection...", Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkUserPlannedActivities() {
        // Tag used to cancel the request
        String tag_string_req = "req_activities";

        pDialog.setMessage("Fetching User Data in ...");
        showDialog();

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String uid = user.get("uid");


        String url  = AppConfig.URL_QUATELY_ACTIVITIES + uid ;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Quarterly Activities Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        //String uid = jObj.getString("uid");
                        JSONObject user = jObj.getJSONObject("user");
                        String activities = user.getString("activities");
                        String entreprizes = user.getString("entreprizes");
                        String topics = user.getString("topics");

                        // Inserting row in users table
                        mDBHelper.addQP(activities,topics,entreprizes);


                    } else {
                        // Error in Syncing. Get the error message
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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void checkUserActivities() {
        // Tag used to cancel the request
        String tag_string_req = "req_farmers";

        pDialog.setMessage("Fetching User Activities in ...");
        showDialog();

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        String uid = user.get("uid");


        String url  = AppConfig.URL_USER_ACTIVITIES + uid;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User Activities Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray activities = jObj.getJSONArray("activities");




                        for(int i = 0; i < activities.length(); i++)
                        {
                            JSONObject object3 = activities.getJSONObject(i);
                            String a1 = object3.getString("a1");
                            String a2 = object3.getString("a2");
                            String a3 = object3.getString("a3");
                            String a4 = object3.getString("a4");
                            String a5 = object3.getString("a5");
                            String a6 = object3.getString("a6");
                            String a7 = object3.getString("a7");
                            String a8 = object3.getString("a8");
                            String a9 = object3.getString("a9");
                            String a10 = object3.getString("a10");
                            String a11 = object3.getString("a11");
                            String a12 = object3.getString("a12");
                            String a13 = object3.getString("a13");
                            String a14 = object3.getString("a14");
                            String a15 = object3.getString("a15");
                            String a16 = object3.getString("a16");
                            String a17 = object3.getString("a17");
                            String a18 = object3.getString("a18");
                            String a19 = object3.getString("a19");
                            String a20 = object3.getString("a20");
                            String a21 = object3.getString("a21");
                            String a22 = object3.getString("a22");
                            String a23 = object3.getString("a23");
                            String a24 = object3.getString("a24");
                            String a25 = object3.getString("a25");
                            String a26 = object3.getString("a26");
                            String a27 = object3.getString("a27");
                            String a28 = object3.getString("a28");
                            String a29 = object3.getString("a29");
                            String a30 = object3.getString("a30");
                            String a31 = object3.getString("a31");
                            String a32 = object3.getString("a32");
                            String a33 = object3.getString("a33");
                            String a34 = object3.getString("a34");
                            String a35 = object3.getString("a35");
                            String a36 = object3.getString("a36");
                            String a37 = object3.getString("a37");
                            String a38 = object3.getString("a38");
                            String a39 = object3.getString("a39");
                            String a40 = object3.getString("a40");
                            String a41 = object3.getString("a41");
                            String a42 = object3.getString("a42");
                            String a43 = object3.getString("a43");
                            String a44 = object3.getString("a44");
                            String a45 = object3.getString("a45");
                            String a46 = object3.getString("a46");
                            String a47 = object3.getString("a47");
                            String a48 = object3.getString("a48");
                            String a49 = object3.getString("a49");
                            String a50 = object3.getString("a50");



                            // Inserting row in users table
                            mDBHelper.dumpActivities(
                                    a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,
                                    a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,
                                    a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,
                                    a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,
                                    a41,a42,a43,a44,a45,a46,a47,a48,a49,a50
                            );

                        }




                    } else {
                        // Error in Syncing. Get the error message
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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkUserOutbreaks() {
        // Tag used to cancel the request
        String tag_string_req = "req_farmers";

        pDialog.setMessage("Fetching User Outbreaks in ...");
        showDialog();

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        String uid = user.get("uid");

        String url  = AppConfig.URL_USER_OUTBREAKS + uid;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "User Outbreaks Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray outbreaks = jObj.getJSONArray("outbreaks");

                        for(int i = 0; i < outbreaks.length(); i++)
                        {
                            JSONObject object3 = outbreaks.getJSONObject(i);
                            String a1 = object3.getString("a1");
                            String a2 = object3.getString("a2");
                            String a3 = object3.getString("a3");
                            String a4 = object3.getString("a4");
                            String a5 = object3.getString("a5");
                            String a6 = object3.getString("a6");
                            String a7 = object3.getString("a7");
                            String a8 = object3.getString("a8");
                            String a9 = object3.getString("a9");
                            String a10 = object3.getString("a10");
                            String a11 = object3.getString("a11");
                            String a12 = object3.getString("a12");
                            String a13 = object3.getString("a13");
                            String a14 = object3.getString("a14");
                            String a15 = object3.getString("a15");
                            String a16 = object3.getString("a16");
                            String a17 = object3.getString("a17");
                            String a18 = object3.getString("a18");
                            String a19 = object3.getString("a19");
                            String a20 = object3.getString("a20");
                            String a21 = object3.getString("a21");
                            String a22 = object3.getString("a22");
                            String a23 = object3.getString("a23");
                            String a24 = object3.getString("a24");
                            String a25 = object3.getString("a25");
                            String a26 = object3.getString("a26");
                            String a27 = object3.getString("a27");
                            String a28 = object3.getString("a28");
                            String a29 = object3.getString("a29");
                            String a30 = object3.getString("a30");
                            String a31 = object3.getString("a31");
                            String a32 = object3.getString("a32");
                            String a33 = object3.getString("a33");
                            String a34 = object3.getString("a34");
                            String a35 = object3.getString("a35");
                            String a36 = object3.getString("a36");
                            String a37 = object3.getString("a37");
                            String a38 = object3.getString("a38");
                            String a39 = object3.getString("a39");
                            String a40 = object3.getString("a40");
                            String a41 = object3.getString("a41");
                            String a42 = object3.getString("a42");
                            String a43 = object3.getString("a43");
                            String a44 = object3.getString("a44");
                            String a45 = object3.getString("a45");
                            String a46 = object3.getString("a46");
                            String a47 = object3.getString("a47");
                            String a48 = object3.getString("a48");
                            String a49 = object3.getString("a49");
                            String a50 = object3.getString("a50");

                            // Inserting row in users table
                            mDBHelper.dumpOutbreaks(
                                    a1,a2,a3,a4,a5,a6,a7,a8,a9,a10,
                                    a11,a12,a13,a14,a15,a16,a17,a18,a19,a20,
                                    a21,a22,a23,a24,a25,a26,a27,a28,a29,a30,
                                    a31,a32,a33,a34,a35,a36,a37,a38,a39,a40,
                                    a41,a42,a43,a44,a45,a46,a47,a48,a49,a50
                            );

                        }




                    } else {
                        // Error in Syncing. Get the error message
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
                Log.e(TAG, "Quarterly Activities Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch enterprises from server and add them to local mobile db
    private void fetchEnterprises(){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        pDialog.setMessage("Fetching Enterprises from the servers ...");
        showDialog();

        String url  = AppConfig.URL_ENTREPRISES;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Enterprises Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray entreprises = jObj.getJSONArray("entreprises");

                        for(int i = 0; i < entreprises.length(); i++)
                        {
                            JSONObject object3 = entreprises.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("parent_id");

                            // Inserting row in entreprizes table
                            mDBHelper.dumpAllEntreprizes(id,name,category);
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
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Dumping Enterprises Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch counties from server and add them to local mobile db
    private void fetchCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        Log.d(TAG,"Fetching counties from the servers ..." );

        String url  = AppConfig.URL_DISTRICT_COUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Counties Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray counties = jObj.getJSONArray("counties");

                        for(int i = 0; i < counties.length(); i++)
                        {
                            JSONObject object3 = counties.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String district = object3.getString("district_id");

                            // Inserting row in county table
                            mDBHelper.addCounty(id,name,district);
                        }
                        // Fetch subcounties
                        fetchSubCounties(user_district_id);

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
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Dumping counties Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch subcounties from server and add them to local mobile db
    private void fetchSubCounties(String user_district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching subcounties from the servers ...");
//        showDialog();
        Log.d(TAG, "Fetching subcounties from the servers ...");

        String url  = AppConfig.URL_DISTRICT_SUBCOUNTIES + user_district_id;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "SubCounties Response: " + response.toString());
//                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray subcounties = jObj.getJSONArray("sub_counties");
                        int subcounties_count = subcounties.length();
                        Log.d(TAG, "subcounties count" + subcounties_count);

                        for(int i = 0; i < subcounties_count; i++)
                        {
                            JSONObject object3 = subcounties.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String county = object3.getString("county_id");

                            // Inserting row in county table
                            mDBHelper.addSubCounty(id,name,county);
                        }

                        fetchDistrictParishes(user_district_id);

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
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Dumping sub counties Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch parishes from server and add them to local mobile db
    private void fetchDistrictParishes(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

//        pDialog.setMessage("Fetching parishes from the servers ...");
//        showDialog();
        Log.d(TAG, "Fetching parishes from the servers ...");

        String url  = AppConfig.URL_DISTRICT_PARISHES + district_id;
//        String url  = AppConfig.URL_PARISHES;
        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "parishes Response: " + response.toString());
//                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray parishes = jObj.getJSONArray("parishes");
                        int parishes_count = parishes.length();
                        Log.d(TAG, "Parishes count" + parishes_count);

                        for(int i = 0; i < parishes_count; i++)
                        {
                            JSONObject object3 = parishes.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String subcounty = object3.getString("subcounty_id");

                            // Inserting row in county table
                            mDBHelper.addParish(id, name, subcounty);
                        }

                        fetchDistrictVillages(district_id);

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
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Adding parish Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    //    fetch villages from server and add them to local mobile db
    private void fetchDistrictVillages(String district_id){
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        Log.d(TAG, "Fetching district villages from the servers ...");

        String url  = AppConfig.URL_DISTRICT_VILLAGES + district_id;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Now store the user in SQLite
                        JSONArray villages = jObj.getJSONArray("villages");
                        int village_count = villages.length();
                        Log.d(TAG, "Villages count" + village_count);
                        for(int i = 0; i < village_count; i++)
                        {
                            JSONObject object3 = villages.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String parish = object3.getString("parish_id");

                            // Inserting row in county table
                            mDBHelper.addVillage(id, name, parish);
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
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Adding village Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void checkSeedData(String district_id, String district) {
        // Tag used to cancel the request
        String tag_string_req = "req_topics";

        String url  = AppConfig.ULR_SEEDDATA;

        StringRequest strReq = new StringRequest(Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Seed Data Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        // Reset all seed data tables
                        mDBHelper.resetAllTablesSeedData();

                        // Activities
                        JSONArray activities = jObj.getJSONArray("activities");
                        for(int i = 0; i < activities.length(); i++)
                        {
                            JSONObject object3 = activities.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("category");

                            // Inserting row in activities table
                            mDBHelper.dumpAllActivities(id, name, category);
                        }

                        // Entreprizes
                        JSONArray entreprizes = jObj.getJSONArray("entreprizes");
                        for(int i = 0; i < entreprizes.length(); i++)
                        {
                            JSONObject object3 = entreprizes.getJSONObject(i);
                            String id = object3.getString("id");
                            String name = object3.getString("name");
                            String category = object3.getString("parent_id");

                            // Inserting row in entreprizes table
                            mDBHelper.dumpAllEntreprizes(id,name,category);
                        }

                        // insert user district
                        mDBHelper.dumpAllDistricts(district_id, district);

                        // register district counties, subcounties, parishes and villages
                        fetchCounties(district_id);

                        JSONArray districts = jObj.getJSONArray("districts");
                        int districts_count = districts.length();
                        for(int i = 0; i < districts_count; i++)
                        {
                            JSONObject object4 = districts.getJSONObject(i);
                            String id = object4.getString("id");
                            String name = object4.getString("name");

                            // Inserting districts in district2 table
                            mDBHelper.addDistrict(id,name);
                        }

                    } else {
                        // Error in Syncing. Get the error message
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
                Log.e(TAG, "Dumping Topics Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {};

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
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
