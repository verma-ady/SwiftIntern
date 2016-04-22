package com.swiftintern.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.swiftintern.Activity.ViewIntern;
import com.swiftintern.Fragment.ShowWorkDetails;
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


public class WorkDetails extends Fragment {

    public WorkDetails() {
        // Required empty public constructor
    }

    View view;
    EditText duration, designation, responsibility;
    AutoCompleteTextView company;
    Button submit;
    ArrayList<String> college = new ArrayList<>();
    ArrayAdapter<String> adapter;
    int count, page=1;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    String sCompany, sDuration, sDesignation, sResponsibility, token;
    String ID;

    private final String BASE = "http://swiftintern.com";
    private final String ORGANISATION = "organizations";
    private final String WORK = "work";
    private final String STUDENTS = "students";
    private final String VOLLEY_REQUEST = "string_req_view_intern";
    boolean update = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_work_detials, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        sharedPreferences = getActivity().getSharedPreferences("UserInfo", getActivity().MODE_PRIVATE);
        token = sharedPreferences.getString("token", null );
        try {
            Bundle extras = getArguments();
            ID = extras.getString("ID");
            Log.v("MyApp", getClass().toString() + " Extras: " + ID);
            update = true;
        } catch (Exception e){
            update = false;
        }


        duration = (EditText) view.findViewById(R.id.text_userdata_duration_name);
        designation = (EditText) view.findViewById(R.id.text_userdata_designation_name);
        responsibility = (EditText) view.findViewById(R.id.text_userdata_responsibility_name);
        company = (AutoCompleteTextView) view.findViewById(R.id.text_userdata_company_name);
        submit = (Button) view.findViewById(R.id.button_submit_userdata);


        Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION + ".json")
                .appendQueryParameter("page", Integer.toString(page))
                .appendQueryParameter("type", "company")
                .appendQueryParameter("limit", "500").build();

        searchOrg(uri.toString());

        keyevent();
        submit();

        return view;
    }

    public void keyevent(){
        responsibility.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEND){
                    if(company.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Company", Toast.LENGTH_SHORT).show();
                    } else if(duration.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Duration", Toast.LENGTH_SHORT).show();
                    } else if(designation.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Designation", Toast.LENGTH_SHORT).show();
                    } else if(responsibility.getText().length()==0){
                        Toast.makeText(getActivity(), "Enter Responsibility", Toast.LENGTH_SHORT).show();
                    } else {
                        sCompany = company.getText().toString();
                        sDuration = duration.getText().toString();
                        sDesignation = designation.getText().toString();
                        sResponsibility = responsibility.getText().toString();
                        saveWork();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void submit(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (company.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Company", Toast.LENGTH_SHORT).show();
                } else if (duration.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Duration", Toast.LENGTH_SHORT).show();
                } else if (designation.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Designation", Toast.LENGTH_SHORT).show();
                } else if (responsibility.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Enter Responsibility", Toast.LENGTH_SHORT).show();
                } else {
                    sCompany = company.getText().toString();
                    sDuration = duration.getText().toString();
                    sDesignation = designation.getText().toString();
                    sResponsibility = responsibility.getText().toString();
                    saveWork();
                }
            }
        });
    }

    void hidekeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public void FillOrganisations(){
        if( (page-1)*500<count ){
            Uri uri = Uri.parse(BASE).buildUpon().appendPath(ORGANISATION + ".json")
                    .appendQueryParameter("page", Integer.toString(page))
                    .appendQueryParameter("type", "company")
                    .appendQueryParameter("limit", "500").build();
            searchOrg(uri.toString() );
        }
    }

    private void searchOrg (String url) {
        Log.v("MyApp", "searchOrg:" + url);
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
                adapter = new ArrayAdapter<>(getActivity() ,android.R.layout.simple_list_item_1,  college);
                company.setAdapter(adapter);
                company.setThreshold(1);
                page++;
                FillOrganisations();
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

    private void saveWork(){
        hidekeyboard(view);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Saving Qualification");
        progressDialog.show();

        Uri uri = null;
        Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
        if(update) {
            uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS).appendPath(WORK).appendPath(ID + ".json").build();
            Log.v("MyApp", getClass().toString() + "Update URL:" + uri.toString() );
        } else {
            uri = Uri.parse(BASE).buildUpon().appendPath(STUDENTS).appendPath(WORK + ".json").build();
            Log.v("MyApp", getClass().toString() + "Not Update URL:" + uri.toString() );
        }

        saveMyWork( uri.toString() );

    }

    private void saveMyWork(String url){

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MyApp", "saveWork Response" + response);
                progressDialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getBoolean("success")){
                        Toast.makeText(getActivity(),"Work Details Saved Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(),"Unable to Save work Details", Toast.LENGTH_SHORT).show();
                    }

                    ShowWorkDetails showWorkDetails = new ShowWorkDetails();
                    android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
                    android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment, showWorkDetails);
                    fragmentTransaction.commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e( "MyApp", "saveWork Response" + error.getMessage() );
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("institute", sCompany );
                params.put("duration", sDuration);
                params.put("designation", sDesignation);
                params.put("responsibility", sResponsibility);
                params.put("action", "saveWork");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("Content-Language", "en-US");
                params.put("acess-token", token);
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, VOLLEY_REQUEST);
    }

}
