package com.swiftintern.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QualificationDetails extends Fragment {

    public QualificationDetails() {
        // Required empty public constructor
    }

    View view;
    EditText degree, gpa, year, major;
    AutoCompleteTextView university;
    Button button;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<String> college = new ArrayList<>();
    ArrayAdapter<String> adapter;
    int count, page=1;
    String sUniversity, sDegree, sGpa, sYear, sMajor;
    String token, ID;
    ProgressDialog progressDialog;
    boolean update;
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATION = "organizations";
    private final String STUDENTS = "students";
    private final String QUALIFICATION = "qualification";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_qualification_details, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getActivity().MODE_PRIVATE);
        editor = getActivity().getSharedPreferences("UserInfo", getActivity().MODE_PRIVATE).edit();
        token = sharedPreferences.getString("token", null);
        Log.v("MyApp", getClass().toString() +"Token in QualificationDetails.class is " + token );

        try {
            Bundle extras = getArguments();
            ID = extras.getString("ID");
            Log.v("MyApp", getClass().toString() + " Extras: " + ID);
            update = true;
        } catch (Exception e){
            update = false;
        }

        university = (AutoCompleteTextView) view.findViewById(R.id.text_userinfo_university_name);
        degree = (EditText) view.findViewById(R.id.text_userinfo_degree_name);
        gpa = (EditText) view.findViewById(R.id.text_userinfo_gpa_name);
        year = (EditText) view.findViewById(R.id.text_userinfo_year_name);
        major = (EditText) view.findViewById(R.id.text_userinfo_major_name);
        button = (Button) view.findViewById(R.id.button_submit_userinfo);

        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION + ".json")
                .appendQueryParameter("page", Integer.toString(page))
                .appendQueryParameter("type", "institute")
                .appendQueryParameter("limit", "500").build();
        searchCollege(uri.toString());

        keyevent();
        submit();

        return view;
    }

    public void keyevent(){
        year.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEND){
                    if(university.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter University", Toast.LENGTH_SHORT).show();
                    } else if(degree.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Degree", Toast.LENGTH_SHORT).show();
                    } else if(major.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Major", Toast.LENGTH_SHORT).show();
                    } else if(gpa.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Percentage", Toast.LENGTH_SHORT).show();
                    } else if(year.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Passing Year", Toast.LENGTH_SHORT).show();
                    } else {
                        sUniversity = university.getText().toString();
                        sDegree = degree.getText().toString();
                        sMajor = major.getText().toString();
                        sGpa = gpa.getText().toString();
                        sYear = year.getText().toString();
                        SaveQual();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void submit(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (university.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter University", Toast.LENGTH_SHORT).show();
                } else if (degree.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Degree", Toast.LENGTH_SHORT).show();
                } else if (major.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Major", Toast.LENGTH_SHORT).show();
                } else if (gpa.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Percentage", Toast.LENGTH_SHORT).show();
                } else if (year.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Passing Year", Toast.LENGTH_SHORT).show();
                } else {
                    sUniversity = university.getText().toString();
                    sDegree = degree.getText().toString();
                    sMajor = major.getText().toString();
                    sGpa = gpa.getText().toString();
                    sYear = year.getText().toString();
                    SaveQual();
                }
            }
        });
    }

    public void FillCollege(){
        if( (page-1)*500<count ){
            Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION + ".json")
                    .appendQueryParameter("page", Integer.toString(page))
                    .appendQueryParameter("type", "institute")
                    .appendQueryParameter("limit", "500").build();
            searchCollege(uri.toString());
        }
    }

    private void searchCollege (String url) {
        Log.v("MyApp", "searchCollege:" + url);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("MyApp", response.toString());
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    count = jsonObject.getInt("count");
                    JSONArray jsonArray = jsonObject.getJSONArray("organizations");
                    int i=0;
                    while( jsonArray.getJSONObject(i)!=null ){
                        college.add(jsonArray.getJSONObject(i).getString("_name"));
                        i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,college);
                university.setAdapter(adapter);
                university.setThreshold(1);
                page++;
                FillCollege();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("MyApp", "Error: " + error.getMessage());
                progressDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }

    void hidekeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void SaveQual(){
        hidekeyboard(view);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Saving Qualification");
        progressDialog.show();

        Uri uri = null;
        Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
        if(update) {
            uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS).appendPath(QUALIFICATION).appendPath(ID + ".json").build();
            Log.v("MyApp", getClass().toString() + "Update URL:" + uri.toString() );
        } else {
            uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS).appendPath(QUALIFICATION + ".json").build();
            Log.v("MyApp", getClass().toString() + "Not Update URL:" + uri.toString() );
        }
        saveMyQual(uri.toString());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void saveMyQual(String url){

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "saveQual Response" + response);
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("success")){
                        Toast.makeText(getActivity(),"Qualification Saved Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(),"Unable to Save Qualification", Toast.LENGTH_SHORT).show();
                    }
                    ShowQualification showQualification = new ShowQualification();
                    android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, showQualification);
                    fragmentTransaction.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "saveQual Error" + error.getMessage() );
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("institute", sUniversity );
                params.put("degree", sDegree);
                params.put("major", sMajor);
                params.put("gpa", sGpa);
                params.put("passing_year", sYear);
                params.put("action", "saveQual");
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
