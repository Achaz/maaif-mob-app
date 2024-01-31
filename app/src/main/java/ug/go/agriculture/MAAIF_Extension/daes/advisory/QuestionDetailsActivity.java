package ug.go.agriculture.MAAIF_Extension.daes.advisory;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ug.go.agriculture.MAAIF_Extension.R;
import ug.go.agriculture.MAAIF_Extension.app.AppConfig;
import ug.go.agriculture.MAAIF_Extension.app.AppController;
import ug.go.agriculture.MAAIF_Extension.helper.SQLiteHandler;
import ug.go.agriculture.MAAIF_Extension.helper.SessionManager;
import ug.go.agriculture.MAAIF_Extension.utils.AppStatus;

public class QuestionDetailsActivity extends android.app.Activity{
    private Context mContext = QuestionDetailsActivity.this;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    TextView title;
    ImageView back_img;
    String postId, farmer_id,loggedInUserId;

    TextView postTitleTv, postDescTv, postCommentsTv, postTimeTv, postAuthorTv;
    ImageView postImg, authorImg;
    ScrollView scrollView;
    View shadow;
    LinearLayout commentBox;



    private ArrayList<QuestionResponseEntity> commentList;
    private QuestionResponseAdapter adapter;
    private RecyclerView recyclerView;

    private static final String TAG = QuestionDetailsActivity.class.getSimpleName();
    public static final String TAG_NO = "no";
    private int offSet = 0;
    int no;

    boolean isLoading = false;

    Button commentBtn;
    EditText commentEt;
    String userComment;
    ViewPager viewPager;
    private List<String> images;
    customPageAdapter myCustomPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adivisory_question_detail);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        images = new ArrayList<>();
        viewPager = (ViewPager)findViewById(R.id.viewPagerQuestionImages);


        // Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();

        loggedInUserId = user.get("uid");

        postId = getIntent().getStringExtra("id");
        farmer_id = getIntent().getStringExtra("farmer_id");

        postTitleTv = (TextView) findViewById(R.id.titleTextView);
        postDescTv = (TextView) findViewById(R.id.descriptionEditText);
        postCommentsTv = (TextView) findViewById(R.id.commentsCountTextView);
        postTimeTv = (TextView) findViewById(R.id.dateTextView);
     //   postImg = (ImageView) findViewById(R.id.postImageViewDetail);
        postAuthorTv = findViewById(R.id.authorTextView);
        authorImg = findViewById(R.id.authorImageView);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //general UI
        scrollView = findViewById(R.id.scrollView);
        shadow = findViewById(R.id.shadow);
        commentBox = findViewById(R.id.newCommentContainer);





        /*vertical category recyclerview code is here*/

        recyclerView = findViewById(R.id.commentsRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        commentList = new ArrayList<QuestionResponseEntity>();

        initAdapter();
        //initScrollListener();

        fetchDetails(postId);

        commentBtn = findViewById(R.id.sendButton);
        commentEt = findViewById(R.id.commentEditText);

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComment();
            }
        });

        postAuthorTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mContext, FarmerDetailActivity.class);
//                intent.putExtra("id", farmer_id);
//                startActivity(intent);
            }
        });

    }

    private void fetchCommentsFromStart(){
        commentList.clear();
        adapter.notifyDataSetChanged();
        // Fetching data from server
        fetchComments(0);
    }

    public void fetchDetails(String postId){
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(getApplicationContext(), "check internet connection", Toast.LENGTH_SHORT).show();
        } else{

            pDialog.setMessage("Fetching Question Details ...");
            showDialog();
            String url  = AppConfig.GET_QUESTION_DETAILS + postId ;
            StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e("KAMBE RESPONSE", "Response " + response.toString());

                    try {
                        //converting response to json object
                        JSONObject obj = new JSONObject(response);

                        //if no error in response
                        if (!obj.getBoolean("error")) {
                            //Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                            JSONObject rQuestion = obj.getJSONObject("question");
                            images.add(rQuestion.getString("media_url"));
                            images.add(rQuestion.getString("media_url2"));
                            images.add(rQuestion.getString("media_url3"));
                            myCustomPagerAdapter = new customPageAdapter(QuestionDetailsActivity.this, images);
                            viewPager.setAdapter(myCustomPagerAdapter);
                            postTitleTv.setText(rQuestion.getString("body"));
                            postDescTv.setText("phone: "+rQuestion.getString("telephone"));
                           // postCommentsTv.setText("no of responses: "+rQuestion.getString("responses"));
                            postTimeTv.setText(rQuestion.getString("created_at"));
                            postAuthorTv.setText(rQuestion.getString("farmer"));
                            String profile_url = String.valueOf("https://extension.agriculture.go.ug/images/users/user.png");
                            Glide.with(getApplicationContext()).load(profile_url).into(authorImg);

                            fetchComments(0);

                            //name, timestamp, question, has_media, comment_count, author_photo, language, sent_via, media_path

                            //Author image
//                            String profile_url = IMAGES_UPLOAD_ROOT + String.valueOf(obj.getString("author_photo"));
//                            Glide.with(getApplicationContext()).load(profile_url).into(authorImg);

                            if (!obj.getString("media_url").equals("")){

                                //holder.imgPost.getLayoutParams().height = 200;
                                String url = obj.getString("media_url");
                                Glide.with(getApplicationContext()).load(url).into(postImg);

                            }

                            hideDialog();

                            //display general view
                            scrollView.setVisibility(View.VISIBLE);
                            shadow.setVisibility(View.VISIBLE);

                            //if (user.getRoleId()!=5){
                            commentBox.setVisibility(View.VISIBLE);
                            //}



                        } else {
                            Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        hideDialog();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Questions dETAIL Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            })  {};

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "request_question_detail");
        }
    }

    private void fetchComments(int page) {

        String tag_string_req = "req_question_responses";
        //check internet connection
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(this, "check internet connection", Toast.LENGTH_SHORT).show();
        }else{
            String url  = AppConfig.GET_QUESTION_RESPONSES + postId ;

            StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e("QUESTIONr RESPONSE", "Response " + response.toString());

                    try{
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        QuestionResponseEntity listData;
                        // Check for error node in json
                        if (!error) {

                            JSONArray new_questions = jObj.getJSONArray("question_responses");

                            for (int i = 0; i < new_questions.length(); i++) {
                                JSONObject object3 = new_questions.getJSONObject(i);
                                String id = object3.getString("id");
                                String user_name = object3.getString("user_name");
                                String user_id = object3.getString("user_id");
                                String user_role = object3.getString("user_role");
                                String question_response = object3.getString("response");
                                String created_at = object3.getString("created_at");
                                String question_id  = object3.getString("question_id");


                                listData = new QuestionResponseEntity(id,user_id,user_name,user_role, question_response,created_at,question_id);
                                commentList.add(listData);
                            }
                            if (commentList != null) {
                                Log.e("COMMENTLIST", "Data " + commentList);

                                // notifying list adapter about data changes
                                // so that it renders the list view with updated data
                                adapter.notifyDataSetChanged();
                                // adapter = new QuestionResponseAdapter(getApplicationContext(), commentList);
                                // recyclerView.setAdapter(adapter);
                            }
                        }
                        else {
                            // Error in Syncing. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }


                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    // Stopping swipe refresh
                    //mSwipeRefreshLayout.setRefreshing(false);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Questions Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }
            })  {};

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
    }

    private void initAdapter(){
        adapter = new QuestionResponseAdapter(this, commentList);
        recyclerView.setAdapter(adapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == commentList.size() - 1) {
                        //bottom of list!
                        loadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    private void loadMore() {

        Handler handler = new Handler();
        //mSwipeRefreshLayout.setRefreshing(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchComments(offSet);
                //adapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);
    }

    public void saveComment(){
        if (!AppStatus.getInstance(this).isOnline()) {
            Toast.makeText(getApplicationContext(), "Check internet Connection", Toast.LENGTH_SHORT).show();
        } else{

            pDialog.setMessage("Sending Comment ...");
            showDialog();

            //first getting the values
            userComment = commentEt.getText().toString();

            if (!validateComment()) {
                hideDialog();
                return;
            }

            String url  = AppConfig.SAVE_QUESTION_RESPONSE + postId ;
            StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.e("RESPONSE", "Response " + response.toString());

                    try{
                        JSONObject obj = new JSONObject(response);

                        QuestionResponseEntity commentData;

                        if (!obj.getBoolean("error")) {


                            try {

                                JSONObject object3 = obj.getJSONObject("question_response");
                                String id = object3.getString("id");
                                String user_name = object3.getString("user_name");
                                String user_id = object3.getString("user_id");
                                String user_role = object3.getString("user_role");
                                String question_response = object3.getString("response");
                                String created_at = object3.getString("created_at");
                                String question_id = object3.getString("question_id");

                                commentData = new QuestionResponseEntity(id,user_id,user_name,user_role, question_response,created_at,question_id);
                                commentList.add(commentData);

                            } catch (JSONException e) {
                                Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                            }


                            if (commentList != null) {
                                // notifying list adapter about data changes
                                // so that it renders the list view with updated data
                                adapter.notifyItemInserted(commentList.size() - 1);
                            }

                            hideDialog();
                            commentEt.setText("");
                            // AppController.hideKeyboard(QuestionDetailsActivity.this);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        hideDialog();
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
            }) {

                @Override
                protected Map<String, String> getParams() {
                    // Posting parameters to post url
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("farmer_question_id", postId);
                    params.put("response", userComment);
                    params.put("user_id",loggedInUserId);

                    return params;
                }

            };
            strReq.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
        }
    }

    public boolean validateComment() {
        boolean valid = true;
        if(userComment.length() == 0){
            Toast.makeText(this, "Comment is required", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
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
