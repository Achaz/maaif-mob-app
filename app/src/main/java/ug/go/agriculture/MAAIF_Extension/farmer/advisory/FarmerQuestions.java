package ug.go.agriculture.MAAIF_Extension.farmer.advisory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.daes.advisory.FarmerQuestionRecyclerAdapter;
import ug.go.agriculture.MAAIF_Extension.daes.advisory.QuestionEntity;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.utils.AppStatus;

public class FarmerQuestions extends android.app.Activity {
    private static final String TAG = FarmerQuestions.class.getSimpleName();
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private FarmerQuestionRecyclerAdapter questionsAdapter;
    private List<QuestionEntity> questions;
    private RecyclerView recyclerView;
    private String totalQuestions;
    private TextView vTotal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisory_questions);
        questions = new ArrayList<QuestionEntity>();
        recyclerView = findViewById(R.id.recyclerview);
        vTotal = findViewById(R.id.tvmenutitle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        initAdapter();
        if(AppStatus.getInstance(this).isOnline()){
            checkForNewQuestions();
        }
        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        //Get total questions
        int totalQ =  db.getCountAllQuestions();
        totalQuestions =  String.valueOf(totalQ);
        vTotal.setText("Total Questions: "+totalQuestions);
        loadQuestions();

    }

    private void initAdapter(){
        questionsAdapter = new FarmerQuestionRecyclerAdapter(this, questions);
        recyclerView.setAdapter(questionsAdapter);
    }

    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */
    private void loadQuestions() {
        if(questions != null)
        {
            questions.clear();
        }
        HashMap<String, String> user = db.getUserDetails();

        String userId = user.get("uid");
        Cursor cursor = db.getMyQuestions(Integer.valueOf(userId));
        if (cursor.moveToFirst()) {
            do {
                QuestionEntity question = new QuestionEntity(
                        cursor.getString(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("keyword")),
                        cursor.getString(cursor.getColumnIndex("farmer_id")),
                        cursor.getString(cursor.getColumnIndex("farmer")),
                        cursor.getString(cursor.getColumnIndex("parish_id")),
                        cursor.getString(cursor.getColumnIndex("telephone")),
                        cursor.getString(cursor.getColumnIndex("body")),
                        cursor.getString(cursor.getColumnIndex("enterprise_id")),
                        cursor.getString(cursor.getColumnIndex("inquiry_source")),
                        cursor.getString(cursor.getColumnIndex("created_at")),
                        cursor.getString(cursor.getColumnIndex("updated_at")),
                        cursor.getInt(cursor.getColumnIndex("has_media")),
                        cursor.getString(cursor.getColumnIndex("media_url")),
                        cursor.getString(cursor.getColumnIndex("responses")),
                        cursor.getString(cursor.getColumnIndex("sender")),
                        cursor.getString(cursor.getColumnIndex("user_id"))
                );
                questions.add(question);
            } while (cursor.moveToNext());
        }

        questionsAdapter.notifyDataSetChanged();
//        questionsAdapter = new FarmerQuestionRecyclerAdapter(this, questions);
//        recyclerView.setAdapter(questionsAdapter);
    }

    private void checkForNewQuestions() {
        // Tag used to cancel the request
        String tag_string_req = "req_questions";

        pDialog.setMessage("Fetching New Questions ...");
        showDialog();

        // database handler
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());

        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        String uid = user.get("uid");


        String url  = AppConfig.GET_QUESTIONS + uid ;

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "get questions: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        JSONArray new_questions = jObj.getJSONArray("questions");

                        for(int i = 0; i < new_questions.length(); i++)
                        {
                            JSONObject object3 = new_questions.getJSONObject(i);
                            String id = object3.getString("id");
                            String parish_id = object3.getString("parish_id");
                            String farmer_id = object3.getString("farmer_id");
                            String farmer = object3.getString("farmer");
                            String telephone = object3.getString("telephone");
                            String body = object3.getString("body");
                            String enterprise_id = object3.getString("enterprise_id");
                            String inquiry_source = object3.getString("inquiry_source");
                            String has_media = object3.getString("has_media");
                            String media_url = object3.getString("media_url");
                            String responses = object3.getString("responses");
                            String created_at = object3.getString("created_at");
                            String updated_at = object3.getString("updated_at");
                            String keyword = object3.getString("keyword");
                            String sender = object3.getString("sender");
                            String user_id = object3.getString("user_id");



                            // Inserting row in users table
                            db.addNewQuestion(id,
                                    keyword,farmer_id,farmer,parish_id,telephone,body,enterprise_id,inquiry_source,created_at,updated_at,
                                    has_media,media_url,responses,sender,user_id);
                        }

                        // recyclerView.notifyAll();

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
                Log.e(TAG, "Questions Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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
    @Override
    public void onBackPressed(){
//remove super.onBackPressed() and you can handle intent to mainActivity or
//any other activity
        Intent i = new Intent(getApplicationContext(),
                Main.class);
        startActivity(i);
        finish();
    }
}
