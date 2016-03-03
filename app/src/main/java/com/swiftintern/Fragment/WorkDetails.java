package com.swiftintern.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.swiftintern.Fragment.ShowWorkDetails;
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
    SearchCompany searchCompany;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    String sCompany, sDuration, sDesignation, sResponsibility, token;
    String ID;
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
        responsibility = (EditText) view.findViewById(R.id.text_userdata_responsibilit_name);
        company = (AutoCompleteTextView) view.findViewById(R.id.text_userdata_company_name);
        submit = (Button) view.findViewById(R.id.button_submit_userdata);

        searchCompany = new SearchCompany();
        searchCompany.execute();
        fillcompany();
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


    private void saveWork(){
        hidekeyboard(view);
        SaveWork saveWork = new SaveWork();
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        progressDialog.setMessage("Saving Qualification");
        progressDialog.show();
        saveWork.execute();
    }

    public void fillcompany(){
        while( page*500<count ){
            searchCompany.execute();
        }
    }

    public class SearchCompany extends AsyncTask<Void, Void, String > {

        String LOG_CAT = "MyApp";

        @Override
        protected String doInBackground(Void... params) {

            String error = null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;

            String base = "http://swiftintern.com";
            String find = "organizations.json";
            URL url = null;
            try {

                Uri uri = Uri.parse(base).buildUpon().appendPath(find)
                        .appendQueryParameter("page", Integer.toString(page))
                        .appendQueryParameter("type", "company")
                        .appendQueryParameter("limit", "500").build();

//                Log.v("MyApp", getClass().toString() +"company URL : " + uri.toString());
                url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                if(isCancelled()){
                    return null;
                }

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return "null_inputstream";
                }



                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return "null_inputstream";
                }

                String stringJSON = buffer.toString();
//                Log.v(LOG_CAT, stringJSON );
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
//                Log.v("MyApp", "gothere");
                error = "null_internet";
                e.printStackTrace();
            } catch (IOException e) {
                error = "null_file";
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_CAT, "ErrorClosingStream", e);
                    }
                }
            }
            return error;
        }//doinbackground


        @Override
        protected void onPostExecute(String strJSON) {

            if(strJSON==null){
              return;
            } if (strJSON == "null_internet") {
//                dialog.dismiss();
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(strJSON);
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
            adapter = new ArrayAdapter<>(getActivity() ,android.R.layout.simple_list_item_1,college);
            company.setAdapter(adapter);
            company.setThreshold(1);
            page++;
            Log.v("MyApp", getClass().toString() + "Done autocomplete " + page );
        }

    }

    public class SaveWork extends AsyncTask<Void, Void, String > {

        //        String LOG_CAT = "MyApp";
        @Override
        protected String doInBackground(Void... params) {
            String error=null;

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String base=null;
            if(update) {
                base = "http://swiftintern.com/students/work/" + ID +".json";
                Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
            } else {
                base = "http://swiftintern.com/students/work.json";
                Log.v("MyApp", getClass().toString() + "Update: " + Boolean.toString(update));
            }
            URL url = null;
            try {
                url= new URL(base);
                //post parameters
                StringBuilder postDataString = new StringBuilder();
                postDataString.append(URLEncoder.encode("institute")); // key institute
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sCompany)); // value sCompany
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("duration"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sDuration));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("designation"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sDesignation));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("responsibility"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode(sResponsibility));
                postDataString.append("&");

                postDataString.append(URLEncoder.encode("action"));
                postDataString.append("=");
                postDataString.append(URLEncoder.encode("saveWork"));

                Log.v("MyApp", getClass().toString() + "post data " + postDataString);
                byte[] postData = postDataString.toString().getBytes("UTF-8"); //

                int postDataLength = postData.length;

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                // headers
                urlConnection.setRequestProperty("Content-Type", // key content,
                        "application/x-www-form-urlencoded"); // value application/x-www-form-urlencoded

                Log.v("MyApp", getClass().toString() + " Token is: " + token );
                urlConnection.setRequestProperty("acess-token",token); // key acess-token, value token

                urlConnection.setRequestProperty("Content-Length", "" + Integer.toString(postDataLength));
                urlConnection.setRequestProperty("Content-Language", "en-US");
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postData);

                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream==null){
                    return "null_inputstream";
                }

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line ;

                while ( (line=bufferedReader.readLine())!=null ){
                    buffer.append(line + '\n');
                }

                if (buffer.length() == 0) {
                    return "null_inputstream";
                }

                String stringJSON = buffer.toString();
//                Log.v(LOG_CAT, stringJSON );
                return stringJSON;
            } catch (UnknownHostException | ConnectException e) {
                error = "null_internet" ;
                e.printStackTrace();
            } catch (IOException e) {
                error= "null_file";
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
//                        Log.e(LOG_CAT, "ErrorClosingStream", e);
                    }
                }
            }
            return error;
        }//doinbackground

        @Override
        protected void onPostExecute(String strJSON) {
            if( strJSON=="null_inputstream" || strJSON=="null_file" ){
                Toast.makeText(getActivity(), "No Such User Id Found", Toast.LENGTH_SHORT).show();
                return  ;
            }

            if ( strJSON=="null_internet" ){
                Toast.makeText(getActivity(), "No Internet Connectivity", Toast.LENGTH_SHORT).show();
                return ;
            }

            progressDialog.dismiss();
//            Log.v("MyApp", getClass().toString() + "SaveWork Response: " + strJSON);
            try {
                JSONObject jsonObject = new JSONObject(strJSON);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        searchCompany.cancel(true);
    }

}
