package com.swiftintern.Activity;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.swiftintern.Helper.AppController;
import com.swiftintern.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class StudentProfile extends AppCompatActivity {

    TextView name, email, phone, last, membership;
    SharedPreferences sharedPreferences;
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String STUDENT = "student";
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        name = (TextView) findViewById(R.id.text_name_name);
        email = (TextView) findViewById(R.id.text_email_name);
        phone = (TextView) findViewById(R.id.text_phone_name);
        last = (TextView) findViewById(R.id.text_lastlogin_name);
        membership = (TextView) findViewById(R.id.text_membership_name);
        sharedPreferences = getApplicationContext().getSharedPreferences("UserInfo", getApplicationContext().MODE_PRIVATE);
        token = sharedPreferences.getString("token", null);
        Uri uri = Uri.parse(BASE).buildUpon().appendPath(STUDENT+".json").build();
        searchUser(uri.toString());
    }

    private void searchUser(String url) {
        Log.d("MyApp", "searchUser URL" + url);
        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "User Response" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonObjectuser = jsonObject.getJSONObject("user");
                    name.setText(jsonObjectuser.getString("_name"));
                    email.setText(jsonObjectuser.getString("_email"));
                    last.setText(jsonObjectuser.getString("_last_login"));
                    phone.setText(jsonObjectuser.getString("_phone"));
                    membership.setText(jsonObjectuser.getString("_type"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "searchUser Error" + error.getMessage() );
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("email", sharedPreferences.getString("email", "") );
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Content-Language", "en-US");
                params.put("acess-token", token);
                return params;
            }
        };
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }
}
